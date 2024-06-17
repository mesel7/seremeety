package com.example.seremeety.ui.request;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.seremeety.R;
import com.example.seremeety.ui.detail_profile.DetailProfileActivity;
import com.example.seremeety.utils.DialogUtils;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class RequestCard extends LinearLayout {
    private Context context;
    private RequestViewModel requestViewModel;
    private ImageView requestProfilePicture;
    private TextView requestNickname;
    private TextView requestCreatedAt;
    private Button requestStatus;

    public RequestCard(Context context, RequestViewModel requestViewModel) {
        super(context);
        this.context = context;
        this.requestViewModel = requestViewModel;
        init(context);
    }

    private void init(Context context) {
        // request_card.xml 레이아웃 인플레이트
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.request_card, this, true);

        requestProfilePicture = view.findViewById(R.id.request_profile_picture);
        requestNickname = view.findViewById(R.id.request_nickname);
        requestCreatedAt = view.findViewById(R.id.request_createdAt);
        requestStatus = view.findViewById(R.id.request_status);
    }

    // 요청 카드에 데이터 출력, 프로필 사진, 요청 상태 버튼을 눌렀을 때의 동작 정의
    public void setRequestData(Map<String, Object> data, int requestType) {
        Map<String, Object> profile = (Map<String, Object>) data.get("profile");

        Glide.with(getContext())
                .load(String.valueOf(profile.get("profilePictureUrl")))
                .placeholder(R.drawable.img_default_profile)
                .error(R.drawable.img_default_profile)
                .into(requestProfilePicture);

        requestNickname.setText(String.valueOf(profile.get("nickname")));

        com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) data.get("createdAt");
        if (timestamp != null) {
            long milliseconds = timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000;
            Date date = new Date(milliseconds);

            SimpleDateFormat targetFormat = new SimpleDateFormat("yy/MM/dd/a hh:mm", Locale.KOREA);
            String formattedDate = targetFormat.format(date);
            requestCreatedAt.setText(formattedDate);
        } else {
            // 타임스탬프가 null인 경우 현재 시간 표시
            SimpleDateFormat targetFormat = new SimpleDateFormat("yy/MM/dd/a hh:mm", Locale.KOREA);
            String formattedDate = targetFormat.format(new Date());
            requestCreatedAt.setText(formattedDate);
        }

        String statusText;
        switch (String.valueOf(data.get("status"))) {
            case "pending":
                statusText = "매칭 대기";
                requestStatus.setBackgroundResource(R.drawable.button_unchecked_style);
                break;
            case "accepted":
                statusText = "매칭 수락";
                requestStatus.setBackgroundResource(R.drawable.button_checked_style);
                break;
            case "rejected":
                statusText = "매칭 거절";
                requestStatus.setBackgroundResource(R.drawable.button_rejected_style);
                break;
            default:
                statusText = "";
        }
        requestStatus.setText(statusText);

        requestProfilePicture.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DetailProfileActivity.class);
            intent.putExtra("profile", new Gson().toJson(profile));
            intent.putExtra("viewOnly", true); // 상세 프로필을 보기만 함(매칭 기능 비활성화)
            getContext().startActivity(intent);
        });
        requestStatus.setOnClickListener(v -> handleRequest(context, data, requestType));
    }

    private void handleRequest(Context context, Map<String, Object> requestData, int requestType) {
        String requestId = String.valueOf(requestData.get("requestId"));
        String requestFrom = String.valueOf(requestData.get("from"));
        String requestTo = String.valueOf(requestData.get("to"));
        String status = String.valueOf(requestData.get("status"));

        RequestViewModel.OnRequestUpdateCallback callback = new RequestViewModel.OnRequestUpdateCallback() {
            @Override
            public void onAcceptSuccess() {
                DialogUtils.showConfirmationDialog(context, "매칭 성공!", "채팅방이 생성되었어요!\n채팅 목록을 확인해보세요");
            }

            @Override
            public void onRejectSuccess() {
                DialogUtils.showConfirmationDialog(context, "매칭 거절", "매칭을 거절하셨어요");
            }

            @Override
            public void onUpdateFailure(String error) {
                DialogUtils.showConfirmationDialog(context, "요청 처리 실패", "다시 시도해주세요");
            }
        };

        if (requestType == R.id.radio_sent) {
            switch (status) {
                case "pending":
                    DialogUtils.showConfirmationDialog(context, "결정 대기", "결정 대기 중이에요\n조금만 기다려주세요");
                    break;
                case "accepted":
                    DialogUtils.showConfirmationDialog(context, "매칭 성공!", "채팅방이 생성되어있어요");
                    break;
                case "rejected":
                    DialogUtils.showConfirmationDialog(context, "매칭 실패", "거절된 매칭이에요");
                    break;
            }
        } else if (requestType == R.id.radio_received) {
            switch (status) {
                case "pending":
                    DialogUtils.showAcceptOrRejectDialog(context, "요청 수락", "요청을 수락하시겠어요?",
                            (dialog, which) -> {
                                requestViewModel.updateRequestStatus(requestId, requestTo, requestFrom,"accepted", requestType, callback);
                            },
                            (dialog, which) -> {
                                requestViewModel.updateRequestStatus(requestId, requestTo, requestFrom, "rejected", requestType, callback);
                            }
                    );
                    break;
                case "accepted":
                    DialogUtils.showConfirmationDialog(context, "매칭 성공!", "채팅방이 생성되어있어요");
                    break;
                case "rejected":
                    DialogUtils.showConfirmationDialog(context, "매칭 실패", "거절된 매칭이에요");
                    break;
            }
        }
    }
}


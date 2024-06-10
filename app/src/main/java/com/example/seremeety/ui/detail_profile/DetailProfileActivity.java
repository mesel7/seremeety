package com.example.seremeety.ui.detail_profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.seremeety.R;
import com.example.seremeety.databinding.ActivityDetailProfileBinding;
import com.example.seremeety.utils.DialogUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.Map;

public class DetailProfileActivity extends AppCompatActivity {
    private ActivityDetailProfileBinding binding;
    private DetailProfileViewModel detailProfileViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        detailProfileViewModel = new ViewModelProvider(this).get(DetailProfileViewModel.class);

        // Intent에서 가져온 JSON 문자열을 Map으로 변환
        String profileJson = getIntent().getStringExtra("profile");
        Map<String, Object> profile = new Gson().fromJson(profileJson, new TypeToken<Map<String, Object>>(){}.getType());

        displayProfileData(profile);

        // 매칭 요청
        binding.matchingRequest.setOnClickListener(v -> {
            DialogUtils.showDialog(DetailProfileActivity.this, "매칭 요청", "요청을 보내시겠어요?",
                    (dialog, which) -> {
                        confirmAndSendMatchingRequest(String.valueOf(profile.get("uid")));
                    },
                    (dialog, which) -> dialog.dismiss()
            );
        });
    }

    private void displayProfileData(Map<String, Object> userData) {
        String profilePictureUrl = String.valueOf(userData.get("profilePictureUrl"));
        if (!TextUtils.isEmpty(profilePictureUrl)) {
            Glide.with(this)
                    .load(profilePictureUrl)
                    .placeholder(R.drawable.img_default_profile)
                    .error(R.drawable.img_default_profile)
                    .into(binding.displayProfilePicture);
        } else {
            // 프로필 사진이 없을 경우 기본 이미지 설정
            binding.displayProfilePicture.setImageResource(R.drawable.img_default_profile);
        }
        Log.d("닉네임", String.valueOf(userData.get("nickname")));
        Log.d("나이", String.valueOf(userData.get("age")));

        binding.displayNickname.setText(String.valueOf(userData.get("nickname")));
        binding.displayAge.setText(String.valueOf(userData.get("age")));
        String gender = String.valueOf(userData.get("gender"));
        if (gender.equals("male")) {
            binding.displayGender.setText("남");
        } else if (gender.equals("female")) {
            binding.displayGender.setText("여");
        }
        binding.displayMbti.setText(String.valueOf(userData.get("mbti")));
        binding.displayUniversity.setText(String.valueOf(userData.get("university")));
        binding.displayPlace.setText(String.valueOf(userData.get("place")));
        binding.displayIntroduce.setText(String.valueOf(userData.get("introduce")));
    }

    private void confirmAndSendMatchingRequest(String profileUid) {
        detailProfileViewModel.sendMatchingRequest(profileUid, new DetailProfileViewModel.OnRequestCallback() {
            @Override
            public void onRequestExists() {
                DialogUtils.showDialog(DetailProfileActivity.this, "매칭 요청", "상대분께 이미 요청을 보내셨어요\n요청 페이지로 갈까요?",
                        (dialog, which) -> {
                            // Intent intent = new Intent(DetailProfileActivity.this, RequestPageActivity.class);
                            // startActivity(intent);
                        },
                        (dialog, which) -> dialog.dismiss()
                );
            }

            @Override
            public void onRequestReceived() {
                DialogUtils.showDialog(DetailProfileActivity.this, "매칭 요청", "상대분께 이미 요청을 받으셨어요\n요청 페이지로 갈까요?",
                        (dialog, which) -> {
                            // Intent intent = new Intent(DetailProfileActivity.this, RequestPageActivity.class);
                            // startActivity(intent);
                        },
                        (dialog, which) -> dialog.dismiss()
                );
            }

            @Override
            public void onRequestSent() {
                DialogUtils.showConfirmationDialog(DetailProfileActivity.this, "매칭 요청", "성공적으로 전송되었어요!");
            }

            @Override
            public void onRequestFailure(String error) {
                DialogUtils.showConfirmationDialog(DetailProfileActivity.this, "매칭 요청 오류", "다시 시도해주세요");
            }
        });
    }
}



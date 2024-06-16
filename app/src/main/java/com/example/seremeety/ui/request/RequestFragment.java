package com.example.seremeety.ui.request;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.seremeety.R;
import com.example.seremeety.databinding.FragmentRequestBinding;

import java.util.List;
import java.util.Map;

public class RequestFragment extends Fragment {

    private FragmentRequestBinding binding;
    private RequestViewModel requestViewModel;
    private Observer<List<Map<String, Object>>> requestsObserver;
    private int requestType;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        requestViewModel = new ViewModelProvider(this).get(RequestViewModel.class);

        binding = FragmentRequestBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (savedInstanceState != null) {
            // 복원된 상태에서 requestType 가져오기
            requestType = savedInstanceState.getInt("requestType");
        } else {
            // 초기 상태에서 requestType 가져오기
            requestType = binding.rgRequest.getCheckedRadioButtonId();
        }

        // requestType에 따라 라디오 버튼 체크 상태 설정
        binding.rgRequest.check(requestType);

        // 옵저버 등록
        requestsObserver = requests -> {
            if (requests != null) {
                displayRequests(requests);
            }
        };
        requestViewModel.getRequests().observe(getViewLifecycleOwner(), requestsObserver);

        requestViewModel.fetchRequests(requestType);

        // 받은 요청, 보낸 요청 전환
        binding.rgRequest.setOnCheckedChangeListener((group, checkedId) -> {
            binding.llRequest.removeAllViews();
            requestViewModel.clearRequests();
            requestType = checkedId; // 사용자가 선택한 라디오 버튼 상태 저장
            requestViewModel.fetchRequests(checkedId);
        });

        return root;
    }

    private void displayRequests(List<Map<String, Object>> requests) {
        Log.d("호출", "displayRequests");
        if (requests != null) {
            LinearLayout requestContainer = binding.llRequest;
            requestContainer.removeAllViews();

            for (Map<String, Object> request : requests) {
                RequestCard requestCard = new RequestCard(getContext(), requestViewModel);
                requestCard.setRequestData(request, requestType);
                requestContainer.addView(requestCard);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // requestType 상태 저장
        outState.putInt("requestType", requestType);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.llRequest.removeAllViews();
        binding = null;
        if (requestsObserver != null) {
            requestViewModel.getRequests().removeObserver(requestsObserver);
            Log.d(TAG, "destroy observer");
        }
        requestViewModel.clearRequests();
    }
}


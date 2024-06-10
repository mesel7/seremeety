package com.example.seremeety.ui.matching;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.seremeety.databinding.FragmentMatchingBinding;
import com.example.seremeety.ui.detail_profile.DetailProfileActivity;
import com.example.seremeety.utils.DialogUtils;
import com.google.gson.Gson;

import java.util.Map;

public class MatchingFragment extends Fragment {

    private FragmentMatchingBinding binding;
    private MatchingViewModel matchingViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        matchingViewModel = new ViewModelProvider(this).get(MatchingViewModel.class);

        binding = FragmentMatchingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        matchingViewModel.fetchProfiles();

        matchingViewModel.getProfiles().observe(getViewLifecycleOwner(), profiles -> {
            if (profiles != null) {
                for (Map<String, Object> profile : profiles) {
                    ProfileCard profileCard = new ProfileCard(getContext());
                    profileCard.setProfileData(profile);
                    binding.glMatching.addView(profileCard);

                    profileCard.setOnClickListener(v -> {
                        matchingViewModel.checkProfileStatus(new MatchingViewModel.OnProfileCheckCallback() {
                            @Override
                            public void onIncompleteProfile() {
                                DialogUtils.showConfirmationDialog(requireContext(), "프로필 열람", "먼저 프로필을 완성해주세요");
                            }

                            @Override
                            public void onCompleteProfile() {
                                Intent intent = new Intent(getContext(), DetailProfileActivity.class);
                                intent.putExtra("profile", new Gson().toJson(profile));
                                startActivity(intent);
                            }
                        });
                    });
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.glMatching.removeAllViews();
        binding = null;
        matchingViewModel.clearProfiles();
        Log.d("태그", "onDestroyView 호출됨");
    }
}


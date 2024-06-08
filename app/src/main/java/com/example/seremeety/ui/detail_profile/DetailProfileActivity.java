package com.example.seremeety.ui.detail_profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.seremeety.R;
import com.example.seremeety.databinding.ActivityDetailProfileBinding;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.Map;

public class DetailProfileActivity extends AppCompatActivity {
    private ActivityDetailProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Intent에서 가져온 JSON 문자열을 Map으로 변환
        String profileJson = getIntent().getStringExtra("profile");
        Map<String, Object> profile = new Gson().fromJson(profileJson, new TypeToken<Map<String, Object>>(){}.getType());

        displayProfileData(profile);
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
}



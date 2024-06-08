package com.example.seremeety.ui.matching;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.seremeety.R;

import java.util.Map;

public class ProfileCard extends LinearLayout {
    private ConstraintLayout layout;
    private ImageView profilePicture;
    private TextView nickname;
    private TextView ageGender;

    public ProfileCard(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);

        // ConstraintLayout을 생성합니다.
        layout = new ConstraintLayout(getContext());
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        layout.setId(View.generateViewId());

        // 프로필 사진의 크기를 설정합니다.
        profilePicture = new ImageView(getContext());
        profilePicture.setId(View.generateViewId());
        ConstraintLayout.LayoutParams profilePictureParams = new ConstraintLayout.LayoutParams(0, 0);
        profilePictureParams.dimensionRatio = "1:1";
        profilePictureParams.startToStart = layout.getId();
        profilePictureParams.endToEnd = layout.getId();
        profilePictureParams.topToTop = layout.getId();
        profilePictureParams.bottomToBottom = layout.getId();
        profilePicture.setLayoutParams(profilePictureParams);
        profilePicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
        profilePicture.setBackgroundResource(R.drawable.image_view_style);
        profilePicture.setClipToOutline(true);

        nickname = new TextView(getContext());
        ageGender = new TextView(getContext());

        // 닉네임과 나이/성별 텍스트의 크기를 설정합니다.
        nickname.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 0.125f));
        ageGender.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 0.125f));

        // 텍스트 색을 검정색으로 설정하고, 가운데 정렬합니다.
        nickname.setTextColor(Color.BLACK);
        nickname.setGravity(Gravity.CENTER_HORIZONTAL);
        ageGender.setTextColor(Color.BLACK);
        ageGender.setGravity(Gravity.CENTER_HORIZONTAL);

        layout.addView(profilePicture);
        addView(layout);
        addView(nickname);
        addView(ageGender);

        // 배경색을 하얀색으로 설정하고, 모서리를 둥글게 만듭니다.
        setPadding(10, 10, 10, 10);
        setBackgroundResource(R.drawable.profile_card_style);

        // 카드 크기 설정(화면 비율에서 조정)
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int size = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / 2;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size * 75/100, size * 95/100);

        // 각 카드 사이에 마진을 추가합니다.
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        layoutParams.setMargins(margin, margin, margin, margin);
        setLayoutParams(layoutParams);
    }

    public void setProfileData(Map<String, Object> data) {
        Glide.with(getContext())
                .load(String.valueOf(data.get("profilePictureUrl")))
                .placeholder(R.drawable.img_default_profile)
                .error(R.drawable.img_default_profile)
                .into(profilePicture);

        nickname.setText(String.valueOf(data.get("nickname")));
        String gender = String.valueOf(data.get("gender")).equals("male") ? "남" : "여";
        ageGender.setText(String.valueOf(data.get("age")) + " " + gender);
    }
}






package com.example.seremeety.ui.matching;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.seremeety.R;

import java.util.Map;

public class ProfileCard extends LinearLayout {
    private ImageView profilePicture;
    private TextView nickname;
    private TextView ageGender;

    public ProfileCard(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.profile_card, this, true);

        profilePicture = view.findViewById(R.id.profile_picture);
        nickname = view.findViewById(R.id.nickname);
        ageGender = view.findViewById(R.id.age_gender);
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







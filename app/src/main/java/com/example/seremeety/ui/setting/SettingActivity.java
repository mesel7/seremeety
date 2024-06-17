package com.example.seremeety.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.seremeety.MainActivity;
import com.example.seremeety.R;
import com.example.seremeety.ui.shop.ShopViewModel;
import com.example.seremeety.utils.DialogUtils;

public class SettingActivity extends AppCompatActivity {
    private SettingViewModel settingViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        settingViewModel = new ViewModelProvider(this).get(SettingViewModel.class);

        // 문의하기
        findViewById(R.id.cs).setOnClickListener(v-> {
            DialogUtils.showConfirmationDialog(SettingActivity.this, "문의하기", "cejhans1520@gmail.com 으로\n이메일 문의 부탁드립니다!");
        });

        // 로그아웃
        findViewById(R.id.sign_out).setOnClickListener(v -> {
            DialogUtils.showDialog(SettingActivity.this, "로그아웃", "로그아웃 할까요?",
                    // 확인 버튼 클릭
                    (dialog, which) -> {
                        settingViewModel.doSignOut();
                        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent); // MainActivity로 이동
                    },
                    // 취소 버튼 클릭
                    null);
        });
    }
}

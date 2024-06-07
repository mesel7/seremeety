package com.example.seremeety;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.seremeety.utils.DialogUtils;
import com.example.seremeety.utils.KeyboardVisibilityUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// 로그인 화면
public class MainActivity extends AppCompatActivity {
    private MainActivityViewModel mainActivityViewModel;
    private EditText userPhone, verificationCode;
    private KeyboardVisibilityUtils keyboardVisibilityUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 한국어 로케일 설정
        Locale locale = new Locale("ko");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        // 뷰모델 초기화
        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        // 로그인 검사
        if (mainActivityViewModel.isUserSignedIn()) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }

        // 키보드 유틸리티 설정
        keyboardVisibilityUtils = new KeyboardVisibilityUtils(getWindow(),
                keyboardHeight -> {
                    // 키보드가 나타날 때
                    ScrollView svMain = findViewById(R.id.sv_main);
                    svMain.smoothScrollTo(svMain.getScrollX(), svMain.getScrollY() + keyboardHeight);
                },
                () -> {
                    // 키보드가 숨겨질 때
                }
        );

        // 전화번호 입력 및 형식 지정
        userPhone = findViewById(R.id.user_phone);
        userPhone.setInputType(InputType.TYPE_CLASS_NUMBER);
        userPhone.addTextChangedListener(new TextWatcher() {
            private static final int MAX_LENGTH = 11;
            private static final int FIRST_HYPHEN_INDEX = 3;
            private static final int SECOND_HYPHEN_INDEX = 7;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // 하이픈 제거
                String cleanText = s.toString().replaceAll("-", "");

                // 최대 길이 체크
                if (cleanText.length() > MAX_LENGTH) {
                    cleanText = cleanText.substring(0, MAX_LENGTH);
                }

                // 하이픈 추가
                StringBuilder formattedText = new StringBuilder(cleanText);
                if (cleanText.length() > SECOND_HYPHEN_INDEX) {
                    formattedText.insert(SECOND_HYPHEN_INDEX, "-");
                }
                if (cleanText.length() > FIRST_HYPHEN_INDEX) {
                    formattedText.insert(FIRST_HYPHEN_INDEX, "-");
                }

                userPhone.removeTextChangedListener(this);
                userPhone.setText(formattedText.toString());
                userPhone.setSelection(formattedText.length());
                userPhone.addTextChangedListener(this);
            }
        });

        // 인증번호 입력 및 형식 지정
        verificationCode = findViewById(R.id.verification_code);
        verificationCode.setInputType(InputType.TYPE_CLASS_NUMBER);
        verificationCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        Button buttonSendCode = findViewById(R.id.button_send_code);
        Button buttonSignIn = findViewById(R.id.button_sign_in);

        // 인증번호 전송, 로그인
        buttonSendCode.setOnClickListener(view -> {
            String phoneNumber = userPhone.getText().toString();
            if (phoneNumber.isEmpty()) {
                DialogUtils.showConfirmationDialog(MainActivity.this, "전화번호 입력", "전화번호를 입력해주세요");
                return;
            }

            phoneNumber = formatPhoneNumber(phoneNumber);
            mainActivityViewModel.sendVerificationCode(phoneNumber, new MainActivityViewModel.OnVerificationCallback() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(Exception e) {
                    DialogUtils.showConfirmationDialog(MainActivity.this, "인증번호 전송 실패", "인증번호 전송에 실패했어요");
                }

                @Override
                public void onCodeSent() {
                    DialogUtils.showConfirmationDialog(MainActivity.this, "인증번호 전송", "인증번호가 전송되었어요");
                }
            });
        });

        buttonSignIn.setOnClickListener(view -> verifyCode(verificationCode.getText().toString()));
    }

    // 입력된 전화번호에서 하이픈을 제거하고 +82 추가
    private String formatPhoneNumber(String phoneNumber) {
        String formattedNumber = phoneNumber.replaceAll("-", "");
        formattedNumber = "+82" + formattedNumber.substring(1);
        return formattedNumber;
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mainActivityViewModel.signInWithPhoneAuthCredential(credential, new MainActivityViewModel.OnSignInCallback() {
            @Override
            public void onSignInSuccess() {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void onSignInFailure(Exception e) {
                DialogUtils.showConfirmationDialog(MainActivity.this, "로그인 실패", "로그인에 실패했어요");
            }
        });
    }

    private void verifyCode(String code) {
        if (!TextUtils.isEmpty(mainActivityViewModel.getVerificationId()) && !TextUtils.isEmpty(code)) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mainActivityViewModel.getVerificationId(), code);
            signInWithPhoneAuthCredential(credential);
        } else {
            // 인증 코드가 없거나 입력되지 않았을 경우 처리할 로직
            DialogUtils.showConfirmationDialog(MainActivity.this, "인증번호 오류", "올바른 인증번호를 입력해주세요");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        keyboardVisibilityUtils.detachKeyboardListeners();
    }
}
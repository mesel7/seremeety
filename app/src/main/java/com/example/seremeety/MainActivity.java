package com.example.seremeety;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

// 로그인 화면
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    String userEmail;
    String userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // 이미 로그인된 상태
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
        findViewById(R.id.button_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userEmail = ((EditText)findViewById(R.id.user_email)).getText().toString();
                userPassword = ((EditText)findViewById(R.id.user_password)).getText().toString();

                if (validateEmailAndPassword(userEmail, userPassword)) {
                    doSignIn(userEmail, userPassword);
                }
            }
        });
    }

    public boolean validateEmailAndPassword(String userEmail, String userPassword) {
        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void doSignIn(String userEmail, String userPassword) {
        auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        }
                        else {
                            // 로그인 실패
                            Toast.makeText(MainActivity.this, "로그인에 실패했습니다", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}
package com.example.seremeety;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivityViewModel extends ViewModel {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String verificationId;

    public boolean isUserSignedIn() {
        return (auth.getCurrentUser() != null);
    }

    public String getVerificationId() {
        return verificationId;
    }

    public void sendVerificationCode(String phoneNumber, OnVerificationCallback callback) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                callback.onVerificationCompleted(credential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                callback.onVerificationFailed(e);
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                super.onCodeSent(s, token);
                                verificationId = s;
                                callback.onCodeSent();
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential, OnSignInCallback callback) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            String phone = user.getPhoneNumber();
                            checkIfNewUser(uid, phone, callback);
                        } else {
                            callback.onSignInFailure(new Exception("로그인 사용자 확인 실패"));
                        }
                    } else {
                        callback.onSignInFailure(new Exception("로그인에 실패했습니다"));
                    }
                });
    }

    private void checkIfNewUser(String uid, String phone, OnSignInCallback callback) {
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    createNewUserData(uid, phone, callback);
                } else {
                    callback.onSignInSuccess();
                }
            } else {
                callback.onSignInFailure(new FirebaseFirestoreException("사용자 정보 확인 실패", FirebaseFirestoreException.Code.UNKNOWN));
            }
        });
    }

    private void createNewUserData(String uid, String phone, OnSignInCallback callback) {
        Map<String, Object> user = new HashMap<>();
        user.put("phone", phone);
        user.put("createdAt", FieldValue.serverTimestamp());
        user.put("profilePictureUrl", "https://firebasestorage.googleapis.com/v0/b/seremeety.appspot.com/o/img_default_profile.png?alt=media&token=79b48827-19d0-4bad-abb7-9bd12e8b133c");
        user.put("coin", 0);
        user.put("nickname", "");
        user.put("birthdate", "");
        user.put("age", "");
        user.put("gender", "");
        user.put("mbti", "");
        user.put("university", "미인증");
        user.put("place", "");
        user.put("introduce", "");
        user.put("profileStatus", 0);

        db.collection("users").document(uid).set(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSignInSuccess();
            } else {
                callback.onSignInFailure(new FirebaseFirestoreException("사용자 정보 저장 실패", FirebaseFirestoreException.Code.UNKNOWN));
            }
        });
    }

    public interface OnVerificationCallback {
        void onVerificationCompleted(PhoneAuthCredential credential);
        void onVerificationFailed(Exception e);
        void onCodeSent();
    }

    public interface OnSignInCallback {
        void onSignInSuccess();
        void onSignInFailure(Exception e);
    }
}

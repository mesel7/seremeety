package com.example.seremeety.ui.mypage;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class MypageViewModel extends ViewModel {
    private final MutableLiveData<Map<String, Object>> userData = new MutableLiveData<>();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    public LiveData<Map<String, Object>> getUserData() {
        return userData;
    }

    public interface UpdateUserDataCallback {
        void onComplete(boolean success);
    }

    public void fetchUserData() {
        DocumentReference userRef = db.collection("users").document(auth.getCurrentUser().getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userData.setValue(documentSnapshot.getData());
            } else {
                Map<String, Object> defaultData = new HashMap<>();
                defaultData.put("nickname", "");
                defaultData.put("age", "");
                defaultData.put("gender", "");
                defaultData.put("mbti", "");
                defaultData.put("university", "미인증");
                defaultData.put("place", "");
                defaultData.put("introduce", "");
                defaultData.put("coin", 0);
                userData.setValue(defaultData);
            }
        }).addOnFailureListener(e -> {
            // 에러 처리 로직
            userData.setValue(null);
        });
    }

    public void uploadProfilePicture(Uri imageUri, OnSuccessListener<Uri> onSuccess) {
        StorageReference fileReference = storage.getReference("profile_pictures").child(auth.getCurrentUser().getUid());
        fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            fileReference.getDownloadUrl().addOnSuccessListener(onSuccess);
        });
    }

    public void updateUserData(Map<String, Object> updatedUserData, UpdateUserDataCallback callback) {
        DocumentReference docRef = db.collection("users").document(auth.getCurrentUser().getUid());
        docRef.update(updatedUserData)
                .addOnSuccessListener(aVoid -> {
                    // 프로필 업데이트 성공
                    fetchUserData();
                    callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    // 프로필 업데이트 실패
                    callback.onComplete(false);
                });
    }

    public void doSignOut() {
        auth.signOut();
    }
}

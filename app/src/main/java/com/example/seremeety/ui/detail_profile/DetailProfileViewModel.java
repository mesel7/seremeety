package com.example.seremeety.ui.detail_profile;

import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DetailProfileViewModel extends ViewModel {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void sendMatchingRequest(String profileUid, int coinToDeduct, OnRequestCallback callback) {
        String currentUid = auth.getCurrentUser().getUid();

        // 사용자의 현재 코인량을 가져와서 차감량 이상일 때만 요청 생성 시도
        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long currentCoinCount = documentSnapshot.getLong("coin");
                    if (currentCoinCount == null || currentCoinCount < coinToDeduct) {
                        callback.onInsufficientCoin();
                    } else {
                        createRequest(profileUid, coinToDeduct, callback);
                    }
                })
                .addOnFailureListener(e -> callback.onRequestFailure(e.getMessage()));
    }

    private void createRequest(String profileUid, int coinToDeduct, OnRequestCallback callback) {
        String currentUid = auth.getCurrentUser().getUid();

        // 사용자가 해당 프로필에 보낸 요청이 있는지 확인
        db.collection("requests")
                .whereEqualTo("from", currentUid)
                .whereEqualTo("to", profileUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        callback.onRequestExists();
                    } else {
                        // 사용자가 해당 프로필에게 받은 요청이 있는지 확인
                        db.collection("requests")
                                .whereEqualTo("from", profileUid)
                                .whereEqualTo("to", currentUid)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                        callback.onRequestReceived();
                                    } else {
                                        // 새로운 요청 생성
                                        Map<String, Object> request = new HashMap<>();
                                        request.put("createdAt", FieldValue.serverTimestamp());
                                        request.put("from", currentUid);
                                        request.put("to", profileUid);
                                        request.put("status", "pending");

                                        db.collection("requests").add(request)
                                                .addOnSuccessListener(documentReference -> {
                                                    // 요청이 성공적으로 생성되었으므로 코인 차감
                                                    db.collection("users").document(currentUid).update("coin", FieldValue.increment(-coinToDeduct))
                                                            .addOnSuccessListener(aVoid -> callback.onRequestSent())
                                                            .addOnFailureListener(e -> callback.onRequestFailure(e.getMessage()));
                                                })
                                                .addOnFailureListener(e -> callback.onRequestFailure(e.getMessage()));
                                    }
                                });
                    }
                });
    }


    public interface OnRequestCallback {
        void onRequestExists();
        void onRequestReceived();
        void onRequestSent();
        void onRequestFailure(String error);
        void onInsufficientCoin();
    }
}

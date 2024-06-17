package com.example.seremeety.ui.shop;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ShopViewModel extends ViewModel {
    private final MutableLiveData<Long> userCoin = new MutableLiveData<>();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 결제 시 필요한 전화번호 정보로 형식을 바꿔서 반환
    public String getCurrentPhoneNumber() {
        String rawNumber = auth.getCurrentUser().getPhoneNumber(); // "+821012345678"
        if (rawNumber != null && rawNumber.startsWith("+82")) {
            rawNumber = "0" + rawNumber.substring(3); // "01012345678"
        }
        if (rawNumber != null && rawNumber.length() == 11) {
            rawNumber = rawNumber.substring(0, 3) + "-" + rawNumber.substring(3, 7) + "-" + rawNumber.substring(7); // "010-1234-5678"
        }
        return rawNumber;
    }

    public LiveData<Long> getUserCoin() {
        return userCoin;
    }

    public void fetchUserCoin() {
        DocumentReference userRef = db.collection("users").document(auth.getCurrentUser().getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userCoin.setValue(documentSnapshot.getLong("coin"));
            }
        }).addOnFailureListener(e -> {
            // 에러 처리 로직
            userCoin.setValue(null);
        });
    }

    public void updateCoinCount(double price) {
        int coinCount;
        switch ((int) price) {
            case 3000: coinCount = 15; break;
            case 9900: coinCount = 55; break;
            case 18400: coinCount = 115; break;
            case 49700: coinCount = 355; break;
            default: coinCount = 0;
        }

        String currentUid = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(currentUid);
        userRef.update("coin", FieldValue.increment(coinCount))
                .addOnSuccessListener(aVoid -> {
                    Log.d("ShopViewModel", "Coin count updated successfully.");
                    // UI 업데이트를 위해 호출
                    fetchUserCoin();
                })
                .addOnFailureListener(e -> Log.w("ShopViewModel", "Error updating coin count", e));
    }
}

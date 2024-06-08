package com.example.seremeety.ui.matching;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MatchingViewModel extends ViewModel {
    private final MutableLiveData<List<Map<String, Object>>> profiles = new MutableLiveData<>();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnProfileCheckCallback {
        void onIncompleteProfile();
        void onCompleteProfile();
    }

    public void fetchProfiles() {
        DocumentReference userRef = db.collection("users").document(auth.getCurrentUser().getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String gender = String.valueOf(documentSnapshot.getData().get("gender"));
                db.collection("users")
                        .whereNotEqualTo("gender", gender)
                        .whereEqualTo("profileStatus", 1)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                List<Map<String, Object>> profileList = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.getId().equals(auth.getCurrentUser().getUid())) {
                                        continue;
                                    }
                                    profileList.add(document.getData());
                                }
                                profiles.setValue(profileList);
                            } else {
                                Log.d("MatchingViewModel", "Error getting documents: ", task.getException());
                            }
                        });
            }
        });
    }

    public void checkProfileStatus(OnProfileCheckCallback callback) {
        DocumentReference userRef = db.collection("users").document(auth.getCurrentUser().getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                long profileStatus = (long) documentSnapshot.getData().get("profileStatus");
                if (profileStatus == 0) {
                    callback.onIncompleteProfile();
                } else {
                    callback.onCompleteProfile();
                }
            }
        });
    }

    public LiveData<List<Map<String, Object>>> getProfiles() {
        return profiles;
    }

    public void clearProfiles() {
        profiles.setValue(null);
    }
}



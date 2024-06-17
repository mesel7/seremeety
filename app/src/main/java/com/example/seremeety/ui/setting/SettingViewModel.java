package com.example.seremeety.ui.setting;

import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class SettingViewModel extends ViewModel {
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public void doSignOut() {
        auth.signOut();
    }
}

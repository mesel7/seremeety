package com.example.seremeety.ui.mypage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MypageViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MypageViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is my page fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}


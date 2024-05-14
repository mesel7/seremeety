package com.example.seremeety.ui.matching;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MatchingViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MatchingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is matching fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}


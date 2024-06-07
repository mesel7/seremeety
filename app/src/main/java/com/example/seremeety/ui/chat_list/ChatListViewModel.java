package com.example.seremeety.ui.chat_list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChatListViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ChatListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is chat list fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}


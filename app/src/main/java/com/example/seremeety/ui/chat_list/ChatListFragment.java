package com.example.seremeety.ui.chat_list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.seremeety.databinding.FragmentChatListBinding;

public class ChatListFragment extends Fragment {

    private FragmentChatListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ChatListViewModel chatListViewModel =
                new ViewModelProvider(this).get(ChatListViewModel.class);

        binding = FragmentChatListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textChatList;
        chatListViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


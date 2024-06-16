package com.example.seremeety.ui.chat_list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.seremeety.databinding.FragmentChatListBinding;
import com.example.seremeety.ui.matching.MatchingViewModel;
import com.example.seremeety.ui.request.RequestCard;

import java.util.List;
import java.util.Map;

public class ChatListFragment extends Fragment {
    private FragmentChatListBinding binding;
    private ChatListViewModel chatListViewModel;
    private Observer<List<Map<String, Object>>> chatListObserver;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        chatListViewModel = new ViewModelProvider(this).get(ChatListViewModel.class);

        binding = FragmentChatListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 옵저버 등록
        chatListObserver = chatList -> {
            if (chatList != null) {
                displayChatList(chatList);
            }
        };
        chatListViewModel.getChatList().observe(getViewLifecycleOwner(), chatListObserver);

        chatListViewModel.fetchChatList();

        return root;
    }

    private void displayChatList(List<Map<String, Object>> chatList) {
        if (chatList != null) {
            LinearLayout chatListContainer = binding.llChatList;
            chatListContainer.removeAllViews();

            for (Map<String, Object> chatRoom : chatList) {
                ChatListCard chatListCard = new ChatListCard(getContext(), chatListViewModel, chatRoom);
                chatListCard.setChatRoomData();
                chatListContainer.addView(chatListCard);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.llChatList.removeAllViews();
        binding = null;
        if (chatListObserver != null) {
            chatListViewModel.getChatList().removeObserver(chatListObserver);
        }
        chatListViewModel.clearChatList();
    }
}


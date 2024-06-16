package com.example.seremeety.ui.chat_room;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.seremeety.R;
import com.example.seremeety.databinding.ActivityChatRoomBinding;
import com.example.seremeety.utils.KeyboardVisibilityUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {
    private ActivityChatRoomBinding binding;
    private ChatRoomViewModel chatRoomViewModel;
    private ChatRoomAdapter chatRoomAdapter;
    private Observer<List<Map<String, Object>>> messagesObserver;
    private KeyboardVisibilityUtils keyboardVisibilityUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // 인텐트로부터 data 받아오기
        String chatRoomJson = getIntent().getStringExtra("chatRoom");
        Map<String, Object> chatRoom = new Gson().fromJson(chatRoomJson, new TypeToken<Map<String, Object>>(){}.getType());

        // ViewModel 초기화
        chatRoomViewModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);

        // RecyclerView 설정
        chatRoomAdapter = new ChatRoomAdapter(chatRoom, chatRoomViewModel.getCurrentUid(), chatRoomViewModel);
        binding.rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChatMessages.setAdapter(chatRoomAdapter);

        // 옵저버 초기화, 메시지 가져오기
        String chatRoomId = String.valueOf(chatRoom.get("chatRoomId"));
        chatRoomViewModel.fetchMessages(chatRoomId);
        messagesObserver = messages -> {
            chatRoomAdapter.notifyDataSetChanged();
            // 스크롤을 가장 아래로 내림
            binding.rvChatMessages.scrollToPosition(chatRoomAdapter.getItemCount() - 1);
        };
        chatRoomViewModel.getMessages().observe(this, messagesObserver);

        // 메시지 입력할 때 키보드 높이만큼 스크롤)
        keyboardVisibilityUtils = new KeyboardVisibilityUtils(getWindow(),
                keyboardHeight -> {
                    // 키보드가 나타날 때
                    RecyclerView rvChatMessages = binding.rvChatMessages;
                    rvChatMessages.post(() -> {
                        rvChatMessages.scrollBy(0, keyboardHeight);
                        // 또는 마지막 아이템으로 스크롤
                        rvChatMessages.smoothScrollToPosition(rvChatMessages.getAdapter().getItemCount() - 1);
                    });
                },
                () -> {
                    // 키보드가 숨겨질 때
                }
        );

        // 메시지 입력 필드와 전송 버튼 설정
        EditText inputMessage = findViewById(R.id.input_message);
        Button sendMessage = findViewById(R.id.send_message);

        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 텍스트가 입력되면 전송 버튼 활성화
                sendMessage.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 전송 버튼 누를 시 메시지 전송
        sendMessage.setOnClickListener(v -> {
            String text = inputMessage.getText().toString();
            chatRoomViewModel.sendMessage(chatRoom, text);
            inputMessage.setText("");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 옵저버 제거
        if (messagesObserver != null) {
            chatRoomViewModel.getMessages().removeObserver(messagesObserver);
        }
    }
}

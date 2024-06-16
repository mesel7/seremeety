package com.example.seremeety.ui.chat_list;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.seremeety.R;
import com.example.seremeety.ui.chat_room.ChatRoomActivity;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ChatListCard extends LinearLayout {
    private Context context;
    private ChatListViewModel chatListViewModel;
    private Map<String, Object> chatRoom;
    private ImageView chatListProfilePicture;
    private TextView chatListNickname;
    private TextView chatListLastMessageSentAt;
    private TextView chatListMessage;

    public ChatListCard(Context context, ChatListViewModel chatListViewModel, Map<String, Object> chatRoom) {
        super(context);
        this.context = context;
        this.chatListViewModel = chatListViewModel;
        this.chatRoom = chatRoom;
        init(context);

        // 각 채팅방 리스트를 클릭 시 해당 채팅방으로 이동
        setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatRoomActivity.class);
            intent.putExtra("chatRoom", new Gson().toJson(chatRoom));
            context.startActivity(intent);
        });
    }

    private void init(Context context) {
        // chat_list_card.xml 레이아웃 인플레이트
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_list_card, this, true);

        chatListProfilePicture = view.findViewById(R.id.chat_list_profile_picture);
        chatListNickname = view.findViewById(R.id.chat_list_nickname);
        chatListLastMessageSentAt = view.findViewById(R.id.chat_list_last_message_sent_at);
        chatListMessage = view.findViewById(R.id.chat_list_message);
    }

    public void setChatRoomData() {
        Map<String, Object> profile = (Map<String, Object>) chatRoom.get("profile");

        Glide.with(getContext())
                .load(String.valueOf(profile.get("profilePictureUrl")))
                .placeholder(R.drawable.img_default_profile)
                .error(R.drawable.img_default_profile)
                .into(chatListProfilePicture);

        chatListNickname.setText(String.valueOf(profile.get("nickname")));

        Map<String, Object> lastMessage = (Map<String, Object>) chatRoom.get("lastMessage");
        com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) lastMessage.get("sentAt");
        if (timestamp != null) {
            long milliseconds = timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000;
            Date date = new Date(milliseconds);

            SimpleDateFormat targetFormat = new SimpleDateFormat("yy/MM/dd/a hh:mm", Locale.KOREA);
            String formattedDate = targetFormat.format(date);
            chatListLastMessageSentAt.setText(formattedDate);
        } else {
            // 타임스탬프가 null인 경우 현재 시간 표시
            SimpleDateFormat targetFormat = new SimpleDateFormat("yy/MM/dd/a hh:mm", Locale.KOREA);
            String formattedDate = targetFormat.format(new Date());
            chatListLastMessageSentAt.setText(formattedDate);
        }

        chatListMessage.setText(String.valueOf(lastMessage.get("text")));
    }
}

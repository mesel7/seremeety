package com.example.seremeety.ui.chat_room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.seremeety.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;

    private static Map<String, Object> chatRoom;
    private String currentUid;
    private ChatRoomViewModel chatRoomViewModel;

    public ChatRoomAdapter(Map<String, Object> chatRoom, String currentUid, ChatRoomViewModel chatRoomViewModel) {
        this.chatRoom = chatRoom;
        this.currentUid = currentUid;
        this.chatRoomViewModel = chatRoomViewModel;
    }

    @Override
    public int getItemViewType(int position) {
        Map<String, Object> message = chatRoomViewModel.getMessages().getValue().get(position);
        if (message.get("sender").equals(currentUid)) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_OTHER_MESSAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_message_card, parent, false);
            return new MyMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_message_card, parent, false);
            return new OtherMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Map<String, Object> message = chatRoomViewModel.getMessages().getValue().get(position);
        if (holder.getItemViewType() == VIEW_TYPE_MY_MESSAGE) {
            ((MyMessageViewHolder) holder).bind(message);
        } else {
            ((OtherMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        List<Map<String, Object>> messages = chatRoomViewModel.getMessages().getValue();
        return messages != null ? messages.size() : 0;
    }

    static class MyMessageViewHolder extends RecyclerView.ViewHolder {
        TextView myMessageText, myMessageSentAt;

        MyMessageViewHolder(View itemView) {
            super(itemView);
            myMessageText = itemView.findViewById(R.id.my_message_text);
            myMessageSentAt = itemView.findViewById(R.id.my_message_sent_at);
        }

        void bind(Map<String, Object> message) {
            myMessageText.setText(String.valueOf(message.get("text")));

            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) message.get("sentAt");
            if (timestamp != null) {
                long milliseconds = timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000;
                Date date = new Date(milliseconds);

                SimpleDateFormat targetFormat = new SimpleDateFormat("yy/MM/dd/a hh:mm", Locale.KOREA);
                String formattedDate = targetFormat.format(date);
                myMessageSentAt.setText(formattedDate);
            } else {
                // 타임스탬프가 null인 경우 현재 시간 표시
                SimpleDateFormat targetFormat = new SimpleDateFormat("yy/MM/dd/a hh:mm", Locale.KOREA);
                String formattedDate = targetFormat.format(new Date());
                myMessageSentAt.setText(formattedDate);
            }
        }
    }

    static class OtherMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView messageProfilePicture;
        TextView otherMessageText, otherMessageSentAt;

        OtherMessageViewHolder(View itemView) {
            super(itemView);
            messageProfilePicture = itemView.findViewById(R.id.message_profile_picture);
            otherMessageText = itemView.findViewById(R.id.other_message_text);
            otherMessageSentAt = itemView.findViewById(R.id.other_message_sent_at);
        }

        void bind(Map<String, Object> message) {
            otherMessageText.setText(String.valueOf(message.get("text")));

            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) message.get("sentAt");
            if (timestamp != null) {
                long milliseconds = timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000;
                Date date = new Date(milliseconds);

                SimpleDateFormat targetFormat = new SimpleDateFormat("yy/MM/dd/a hh:mm", Locale.KOREA);
                String formattedDate = targetFormat.format(date);
                otherMessageSentAt.setText(formattedDate);
            } else {
                // 타임스탬프가 null인 경우 현재 시간 표시
                SimpleDateFormat targetFormat = new SimpleDateFormat("yy/MM/dd/a hh:mm", Locale.KOREA);
                String formattedDate = targetFormat.format(new Date());
                otherMessageSentAt.setText(formattedDate);
            }

            Glide.with(itemView)
                    .load(String.valueOf(((Map<String, Object>) chatRoom.get("profile")).get("profilePictureUrl")))
                    .placeholder(R.drawable.img_default_profile)
                    .error(R.drawable.img_default_profile)
                    .into(messageProfilePicture);
        }
    }
}



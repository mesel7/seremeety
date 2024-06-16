package com.example.seremeety.ui.chat_room;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomViewModel extends ViewModel {
    private final MutableLiveData<List<Map<String, Object>>> messages = new MutableLiveData<>();
    // 스냅샷 리스너 관리
    private ListenerRegistration messagesListener;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void fetchMessages(String chatRoomId) {
        if (messagesListener != null) {
            // 기존의 리스너가 있다면 제거
            messagesListener.remove();
        }

        messagesListener = db.collection("chat_rooms").document(chatRoomId).collection("messages")
                .orderBy("sentAt", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<Map<String, Object>> newMessages = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Map<String, Object> message = doc.getData();
                            newMessages.add(message);
                        }

                        messages.setValue(newMessages);
                    }
                });
    }

    public void sendMessage(Map<String, Object> chatRoom, String text) {
        if (text.trim().isEmpty()) {
            return;
        }

        String currentUid = auth.getCurrentUser().getUid();
        String chatRoomId = String.valueOf(chatRoom.get("chatRoomId"));
        db.collection("users").document(currentUid)
                .get()
                .addOnSuccessListener(profileSnapshot -> {
                    if (profileSnapshot.exists()) {
                        Map<String, Object> profile = profileSnapshot.getData();
                        String currentNickname = String.valueOf(profile.get("nickname"));

                        // 메시지 데이터 생성
                        Map<String, Object> messageData = new HashMap<>();
                        messageData.put("sender", currentUid);
                        messageData.put("senderNickname", currentNickname);
                        messageData.put("text", text);
                        messageData.put("sentAt", FieldValue.serverTimestamp());

                        // 파이어스토어에 메시지 저장
                        db.collection("chat_rooms").document(chatRoomId).collection("messages")
                                .add(messageData);

                        // 해당 대화방의 마지막 메시지 업데이트
                        DocumentReference chatRoomRef = db.collection("chat_rooms").document(chatRoomId);
                        Map<String, Object> lastMessage = new HashMap<>();
                        lastMessage.put("text", messageData.get("text"));
                        lastMessage.put("sentAt", messageData.get("sentAt"));
                        chatRoomRef.update("lastMessage", lastMessage);
                    }
                });
    }

    public String getCurrentUid() {
        return auth.getCurrentUser().getUid();
    }

    public LiveData<List<Map<String, Object>>> getMessages() {
        return messages;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        // ViewModel이 클리어될 때 리스너 제거
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}

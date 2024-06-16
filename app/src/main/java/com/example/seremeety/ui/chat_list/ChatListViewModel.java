package com.example.seremeety.ui.chat_list;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatListViewModel extends ViewModel {
    private final MutableLiveData<List<Map<String, Object>>> chatList = new MutableLiveData<>();

    // 스냅샷 리스너 관리
    private ListenerRegistration chatListListener;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 채팅방 id와 상대방의 프로필 정보도 포함시켜서 가져옴
    public void fetchChatList() {
        String currentUid = auth.getCurrentUser().getUid();

        if (chatListListener != null) {
            // 기존의 리스너가 있다면 제거
            chatListListener.remove();
        }

        chatListListener = db.collection("chat_rooms")
                .whereArrayContains("users", currentUid)
                .orderBy("lastMessage.sentAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<Map<String, Object>> chatRooms = new ArrayList<>();
                        List<Task<DocumentSnapshot>> profileTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Map<String, Object> chatRoom = doc.getData();
                            chatRoom.put("chatRoomId", doc.getId());
                            chatRooms.add(chatRoom);

                            // 상대방의 uid를 찾음
                            List<String> users = (List<String>) chatRoom.get("users");
                            String otherUid = users.get(0).equals(currentUid) ? users.get(1) : users.get(0);

                            // 프로필 정보를 가져옴
                            Task<DocumentSnapshot> profileTask = db.collection("users").document(otherUid)
                                    .get()
                                    .addOnSuccessListener(profileSnapshot -> {
                                        if (profileSnapshot.exists()) {
                                            Map<String, Object> profile = profileSnapshot.getData();
                                            chatRoom.put("profile", profile);
                                        }
                                    });
                            profileTasks.add(profileTask);
                        }

                        // 모든 프로필 정보 요청이 완료될 때까지 기다림
                        Tasks.whenAllSuccess(profileTasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                            @Override
                            public void onSuccess(List<Object> list) {
                                // 모든 채팅방 데이터가 처리되었을 때만 리스트 업데이트
                                chatList.setValue(chatRooms);
                            }
                        });
                    }
                });
    }

    public LiveData<List<Map<String, Object>>> getChatList() {
        return chatList;
    }
    public void clearChatList() {
        chatList.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        // ViewModel이 클리어될 때 리스너 제거
        if (chatListListener != null) {
            chatListListener.remove();
        }
    }
}


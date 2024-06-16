package com.example.seremeety.ui.request;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.seremeety.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestViewModel extends ViewModel {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<Map<String, Object>>> requests = new MutableLiveData<>();

    // 요청에 요청 id(requests 컬렉션의 문서 번호)와 상대방의 프로필 정보도 포함시켜서 가져옴
    public void fetchRequests(int requestType) {
        Log.d("호출", "fetchRequests");
        String currentUid = auth.getCurrentUser().getUid();
        String field = requestType == R.id.radio_sent ? "from" : "to";

        db.collection("requests")
                .whereEqualTo(field, currentUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> requestList = new ArrayList<>();
                        List<Task<DocumentSnapshot>> profileTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> request = document.getData();
                            request.put("requestId", document.getId());
                            requestList.add(request);

                            // 상대방의 프로필 정보를 가져옴
                            String profileUid = field.equals("from") ? String.valueOf(request.get("to")) : String.valueOf(request.get("from"));

                            Task<DocumentSnapshot> profileTask = db.collection("users").document(profileUid)
                                    .get()
                                    .addOnSuccessListener(profileSnapshot -> {
                                        if (profileSnapshot.exists()) {
                                            Map<String, Object> profile = profileSnapshot.getData();
                                            request.put("profile", profile);
                                        }
                                    });
                            profileTasks.add(profileTask);
                        }

                        // 모든 프로필 정보 요청이 완료될 때까지 기다림
                        Tasks.whenAllSuccess(profileTasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                            @Override
                            public void onSuccess(List<Object> list) {
                                // 모든 요청이 처리되었을 때 리스트 업데이트
                                requests.setValue(requestList);
                            }
                        });
                    } else {
                        Log.d("RequestViewModel", "Error getting documents: ", task.getException());
                    }
                });
    }

    // 요청 상태 업데이트 후 목록 갱신, 수락 시 채팅방 생성
    public void updateRequestStatus(String requestId, String user1, String user2, String status, int requestType, OnRequestUpdateCallback callback) {
        db.collection("requests").document(requestId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    // 요청 상태 업데이트 성공
                    fetchRequests(requestType);
                    if (callback != null) {
                        if (status.equals("accepted")) {
                            // 수락 시 채팅방 생성
                            createChatRoom(user1, user2);
                            callback.onAcceptSuccess();
                        } else if (status.equals("rejected")) {
                            callback.onRejectSuccess();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // 요청 상태 업데이트 실패
                    Log.w("RequestViewModel", "Error updating request status", e);
                    if (callback != null) {
                        callback.onUpdateFailure(e.getMessage());
                    }
                });
    }

    // 수락 시 최초 1회 채팅방 생성
    private void createChatRoom(String user1, String user2) {
        Map<String, Object> chatRoom = new HashMap<>();
        chatRoom.put("createdAt", FieldValue.serverTimestamp());

        Map<String, Object> lastMessage = new HashMap<>();
        lastMessage.put("sentAt", FieldValue.serverTimestamp());
        lastMessage.put("text", "");
        chatRoom.put("lastMessage", lastMessage);
        chatRoom.put("users", Arrays.asList(user1, user2));

        db.collection("chat_rooms")
                .add(chatRoom)
                .addOnSuccessListener(documentReference -> {
                    Log.d("RequestViewModel", "Chat room created with ID: " + documentReference.getId());

                    // 채팅방에 messages 컬렉션 추가(한 채팅방에서 주고 받은 메시지 저장)
                    Map<String, Object> message = new HashMap<>();
                    message.put("senderNickname", "");
                    message.put("sender", "");
                    message.put("sentAt", FieldValue.serverTimestamp());
                    message.put("text", "");

                    documentReference.collection("messages")
                            .add(message)
                            .addOnSuccessListener(messageDocumentReference -> {
                                Log.d("RequestViewModel", "Message added with ID: " + messageDocumentReference.getId());
                            })
                            .addOnFailureListener(e -> {
                                Log.w("RequestViewModel", "Error adding message", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w("RequestViewModel", "Error creating chat room", e);
                });
    }

    public interface OnRequestUpdateCallback {
        void onAcceptSuccess();
        void onRejectSuccess();
        void onUpdateFailure(String error);
    }

    public LiveData<List<Map<String, Object>>> getRequests() {
        return requests;
    }
    public void clearRequests() {
        requests.setValue(null);
    }
}



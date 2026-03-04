package com.example.myapp.Activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapp.R;
import com.example.myapp.RetrofitClient;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.Utils.NotificationHelper;
import com.example.myapp.adapters.ChatAdapter;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.ChatMessageResponse;
import com.example.myapp.models.response.ChatRoomResponse;
import com.example.myapp.models.response.PageResponse;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable; // Thêm cái này
import io.reactivex.disposables.Disposable;          // Thêm cái này
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class ChatActivity extends AppCompatActivity {
    private StompClient mStompClient;
    private String jwtToken = "";
    private Integer roomId;
    private ChatAdapter chatAdapter;
    private List<ChatMessageResponse> mMessages = new ArrayList<>();
    private final Gson gson = new Gson();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable(); // Dọn báo vàng
    private RecyclerView rvChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String token = SharedPrefsManager.getAccessToken();
        if (token != null) jwtToken = "Bearer " + token;
        else { finish(); return; }

        rvChat = findViewById(R.id.rvChat);
        TextView tvRoleNote = findViewById(R.id.tvRoleNote);
        String role = SharedPrefsManager.getUserRole();

        // Note vai trò người dùng
        if ("Admin".equalsIgnoreCase(role)) {
            tvRoleNote.setText("--- CHẾ ĐỘ QUẢN TRỊ VIÊN ---");
            tvRoleNote.setBackgroundColor(0xFFD1E9FF);
        } else {
            tvRoleNote.setText("--- CHẾ ĐỘ KHÁCH HÀNG ---");
            tvRoleNote.setBackgroundColor(0xFFE8F5E9);
        }

        setupRecyclerView();
        initWebSocket();

        if (getIntent().hasExtra("ROOM_ID")) {
            this.roomId = getIntent().getIntExtra("ROOM_ID", -1);
            String clientName = getIntent().getStringExtra("CLIENT_NAME");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Hỗ trợ: " + clientName);
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF1976D2));
            }
            subscribeToWebSocket(roomId);
            loadChatHistory(roomId);
            markAsRead(roomId);
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Hỗ trợ trực tuyến");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF388E3C));
            }
            fetchRoomAndSubscribe();
        }

        findViewById(R.id.btnSend).setOnClickListener(v -> {
            EditText edt = findViewById(R.id.edtMessage);
            String text = edt.getText().toString().trim();
            if (!text.isEmpty() && roomId != null) {
                sendMessage(text);
                edt.setText("");
            }
        });
    }

    private void subscribeToWebSocket(int roomId) {
        // Lưu kết quả vào Disposable để hết báo vàng
        Disposable disposable = mStompClient.topic("/topic/room." + roomId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    ChatMessageResponse msg = gson.fromJson(topicMessage.getPayload(), ChatMessageResponse.class);
                    runOnUiThread(() -> {
                        mMessages.add(msg);
                        chatAdapter.notifyItemInserted(mMessages.size() - 1);
                        rvChat.scrollToPosition(mMessages.size() - 1);
//
                        // CHỈ ADMIN MỚI NHẬN THÔNG BÁO BANNER (ĐÃ TẮT CHO KHÁCH)
                        String myRole = SharedPrefsManager.getUserRole();
                        String myUser = SharedPrefsManager.getUsername();
                        if (!msg.getSenderUsername().equals(myUser) && "Admin".equalsIgnoreCase(myRole)) {
                            NotificationHelper.showChatNotification(this, "Tin nhắn từ khách", msg.getMessage());
                        }
                    });
                }, throwable -> Log.e("CHAT", "Lỗi Subscribe: " + throwable.getMessage()));

        compositeDisposable.add(disposable);
    }

    private void loadChatHistory(int roomId) {
        RetrofitClient.getApiService().getMessages(jwtToken, roomId, 0, 50).enqueue(new Callback<ApiResponse<PageResponse<ChatMessageResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<ChatMessageResponse>>> call, Response<ApiResponse<PageResponse<ChatMessageResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatMessageResponse> history = response.body().getData().getContent();
                    Collections.reverse(history); // Cũ trên, mới dưới
                    mMessages.clear();
                    mMessages.addAll(history);
                    chatAdapter.notifyDataSetChanged();
                    if (!mMessages.isEmpty()) rvChat.scrollToPosition(mMessages.size() - 1);
                }
            }
            @Override public void onFailure(Call<ApiResponse<PageResponse<ChatMessageResponse>>> call, Throwable t) {}
        });
    }

    private void initWebSocket() {
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/ws");
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("Authorization", jwtToken));
        mStompClient.connect(headers);
    }

    private void fetchRoomAndSubscribe() {
        RetrofitClient.getApiService().getMyRoom(jwtToken).enqueue(new Callback<ApiResponse<ChatRoomResponse>>() {
            @Override public void onResponse(Call<ApiResponse<ChatRoomResponse>> call, Response<ApiResponse<ChatRoomResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    roomId = response.body().getData().getRoomId();
                    subscribeToWebSocket(roomId);
                    loadChatHistory(roomId);
                }
            }
            @Override public void onFailure(Call<ApiResponse<ChatRoomResponse>> call, Throwable t) {}
        });
    }

    private void sendMessage(String text) {
        JSONObject json = new JSONObject();
        try { json.put("roomId", roomId); json.put("message", text); } catch (JSONException e) {}
        mStompClient.send("/app/chat.send", json.toString()).subscribe();
    }

    private void markAsRead(int roomId) {
        RetrofitClient.getApiService().markAsRead(jwtToken, roomId).enqueue(new Callback<ApiResponse<Map<String, Integer>>>() {
            @Override public void onResponse(Call<ApiResponse<Map<String, Integer>>> call, Response<ApiResponse<Map<String, Integer>>> response) {}
            @Override public void onFailure(Call<ApiResponse<Map<String, Integer>>> call, Throwable t) {}
        });
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(mMessages);
        rvChat.setAdapter(chatAdapter);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        if (mStompClient != null) mStompClient.disconnect();
        compositeDisposable.clear(); // Hủy hết subscribe khi đóng activity
        super.onDestroy();
    }
}
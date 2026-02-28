package com.example.myapp.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapp.R;
import com.example.myapp.RetrofitClient;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.adapters.ChatAdapter;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.ChatMessageResponse;
import com.example.myapp.models.response.ChatRoomResponse;
import com.example.myapp.models.response.PageResponse;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String token = SharedPrefsManager.getAccessToken();
        if (token != null) {
            jwtToken = "Bearer " + token;
        } else {
            finish();
            return;
        }

        setupRecyclerView();
        initWebSocket();

        if (getIntent().hasExtra("ROOM_ID")) {
            this.roomId = getIntent().getIntExtra("ROOM_ID", -1);
            String clientName = getIntent().getStringExtra("CLIENT_NAME");
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Hỗ trợ: " + clientName);

            subscribeToWebSocket(roomId);
            loadChatHistory(roomId);
            markAsRead(roomId);
        } else {
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
                    loadChatHistory(roomId); // Load lịch sử cho khách
                }
            }
            @Override public void onFailure(Call<ApiResponse<ChatRoomResponse>> call, Throwable t) { Log.e("CHAT", "Lỗi lấy phòng"); }
        });
    }

    private void loadChatHistory(int roomId) {
        RetrofitClient.getApiService().getMessages(jwtToken, roomId, 0, 50).enqueue(new Callback<ApiResponse<PageResponse<ChatMessageResponse>>>() {
            @Override public void onResponse(Call<ApiResponse<PageResponse<ChatMessageResponse>>> call, Response<ApiResponse<PageResponse<ChatMessageResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mMessages.clear();
                    mMessages.addAll(response.body().getData().getContent());
                    chatAdapter.notifyDataSetChanged();
                    if (!mMessages.isEmpty()) ((RecyclerView)findViewById(R.id.rvChat)).scrollToPosition(mMessages.size() - 1);
                }
            }
            @Override public void onFailure(Call<ApiResponse<PageResponse<ChatMessageResponse>>> call, Throwable t) {}
        });
    }

    private void markAsRead(int roomId) {
        // FIX: Kiểu dữ liệu Map<String, Integer> đồng nhất với ApiService
        RetrofitClient.getApiService().markAsRead(jwtToken, roomId).enqueue(new Callback<ApiResponse<Map<String, Integer>>>() {
            @Override public void onResponse(Call<ApiResponse<Map<String, Integer>>> call, Response<ApiResponse<Map<String, Integer>>> response) {
                if (response.isSuccessful()) Log.d("CHAT", "Đã đọc");
            }
            @Override public void onFailure(Call<ApiResponse<Map<String, Integer>>> call, Throwable t) {}
        });
    }

    private void subscribeToWebSocket(int roomId) {
        mStompClient.topic("/topic/room." + roomId)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    ChatMessageResponse msg = gson.fromJson(topicMessage.getPayload(), ChatMessageResponse.class);
                    runOnUiThread(() -> {
                        mMessages.add(msg);
                        chatAdapter.notifyItemInserted(mMessages.size() - 1);
                        ((RecyclerView)findViewById(R.id.rvChat)).scrollToPosition(mMessages.size() - 1);
                    });
                }, throwable -> Log.e("CHAT", "Lỗi Subscribe"));
    }

    private void sendMessage(String text) {
        JSONObject json = new JSONObject();
        try { json.put("roomId", roomId); json.put("message", text); } catch (JSONException e) {}
        mStompClient.send("/app/chat.send", json.toString()).subscribe();
    }

    private void setupRecyclerView() {
        RecyclerView rvChat = findViewById(R.id.rvChat);
        chatAdapter = new ChatAdapter(mMessages, "CLIENT");
        rvChat.setAdapter(chatAdapter);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override protected void onDestroy() { if (mStompClient != null) mStompClient.disconnect(); super.onDestroy(); }
}
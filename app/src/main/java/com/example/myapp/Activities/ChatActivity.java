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
import com.example.myapp.api.ApiService;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.ChatMessageResponse;
import com.example.myapp.models.response.ChatRoomResponse;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
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

        // 1. Ánh xạ View và Setup UI
        setupRecyclerView();

        // 2. Lấy Token
        String token = SharedPrefsManager.getAccessToken();
        if (token != null) jwtToken = "Bearer " + token;

        // 3. Khởi tạo WebSocket (Chỉ gọi 1 lần ở đây)
        initWebSocket();

        // 4. FIX LOGIC: Kiểm tra Role Admin hay Customer ngay khi mở màn hình
        if (getIntent().hasExtra("ROOM_ID")) {
            // ROLE ADMIN: Lấy ID từ danh sách truyền sang
            this.roomId = getIntent().getIntExtra("ROOM_ID", -1);
            String clientName = getIntent().getStringExtra("CLIENT_NAME");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Hỗ trợ: " + clientName);
            }
            // Admin đã có roomId nên subscribe luôn
            subscribeToWebSocket(roomId);
            markAsRead(roomId); // Đánh dấu đã đọc khi Admin vào xem
        } else {
            // ROLE CUSTOMER: Phải gọi API lấy roomId của chính mình
            fetchRoomAndSubscribe();
        }

        // 5. Sự kiện gửi tin nhắn
        findViewById(R.id.btnSend).setOnClickListener(v -> {
            EditText edt = findViewById(R.id.edtMessage);
            String text = edt.getText().toString().trim();
            if (!text.isEmpty() && roomId != null) {
                sendMessage(text);
                edt.setText("");
            } else if (roomId == null) {
                Toast.makeText(this, "Đang kết nối...", Toast.LENGTH_SHORT).show();
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
            @Override
            public void onResponse(Call<ApiResponse<ChatRoomResponse>> call, Response<ApiResponse<ChatRoomResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    roomId = response.body().getData().getRoomId();
                    subscribeToWebSocket(roomId);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<ChatRoomResponse>> call, Throwable t) {
                Log.e("CHAT", "Lỗi lấy phòng: " + t.getMessage());
            }
        });
    }

    private void subscribeToWebSocket(int roomId) {
        mStompClient.topic("/topic/room." + roomId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
        try {
            json.put("roomId", roomId);
            json.put("message", text);
        } catch (JSONException e) { e.printStackTrace(); }

        mStompClient.send("/app/chat.send", json.toString()).subscribe();
    }

    private void markAsRead(int roomId) {
        // Gọi API báo cho BE là đã đọc tin nhắn trong phòng này
        RetrofitClient.getApiService().markAsRead(jwtToken, roomId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {}
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
        });
    }

    private void setupRecyclerView() {
        RecyclerView rvChat = findViewById(R.id.rvChat);
        chatAdapter = new ChatAdapter(mMessages, "CLIENT");
        rvChat.setAdapter(chatAdapter);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        if (mStompClient != null) mStompClient.disconnect();
        super.onDestroy();
    }
}
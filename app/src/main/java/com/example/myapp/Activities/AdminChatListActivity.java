package com.example.myapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapp.RetrofitClient;
import com.example.myapp.adapters.AdminChatAdapter;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.ChatRoomResponse;
import com.example.myapp.models.response.PageResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChatListActivity extends AppCompatActivity {
    private RecyclerView rvRooms;
    private AdminChatAdapter adapter;
    private List<ChatRoomResponse> roomList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.myapp.R.layout.activity_admin_chat_list); // Ông nhớ tạo layout này với 1 RecyclerView

        rvRooms = findViewById(com.example.myapp.R.id.rvChatRooms);
        adapter = new AdminChatAdapter(roomList, room -> {
            // Khi Admin chọn 1 khách, mở ChatActivity và truyền roomId sang
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("ROOM_ID", room.getRoomId());
            intent.putExtra("CLIENT_NAME", room.getClientName());
            startActivity(intent);
        });

        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRooms();
    }

    private void loadRooms() {
        RetrofitClient.getApiService().getAdminRooms(0, 20).enqueue(new Callback<ApiResponse<PageResponse<ChatRoomResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<ChatRoomResponse>>> call, Response<ApiResponse<PageResponse<ChatRoomResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    roomList.clear();
                    roomList.addAll(response.body().getData().getContent());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<PageResponse<ChatRoomResponse>>> call, Throwable t) {
                Log.e("ADMIN_CHAT", "Lỗi tải phòng: " + t.getMessage());
            }
        });
    }
}
package com.example.myapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapp.models.response.ChatRoomResponse;
import java.util.List;

public class AdminChatAdapter extends RecyclerView.Adapter<AdminChatAdapter.ViewHolder> {
    private List<ChatRoomResponse> rooms;
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(ChatRoomResponse room);
    }

    public AdminChatAdapter(List<ChatRoomResponse> rooms, OnRoomClickListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout có sẵn của hệ thống để hiển thị 2 dòng text (Tên + Tin nhắn cuối)
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRoomResponse room = rooms.get(position);
        holder.txtName.setText(room.getClientName()); // Hiển thị tên khách hàng

        // Hiển thị tin nhắn cuối hoặc số tin chưa đọc
        String subText = (room.getUnreadCount() > 0) ? "Có " + room.getUnreadCount() + " tin nhắn mới" : "Không có tin nhắn mới";
        holder.txtLastMsg.setText(subText);

        holder.itemView.setOnClickListener(v -> listener.onRoomClick(room));
    }

    @Override
    public int getItemCount() { return rooms.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtLastMsg;
        ViewHolder(View v) {
            super(v);
            txtName = v.findViewById(android.R.id.text1);
            txtLastMsg = v.findViewById(android.R.id.text2);
        }
    }
}
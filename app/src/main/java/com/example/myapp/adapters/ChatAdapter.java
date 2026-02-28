package com.example.myapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapp.R;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.models.response.ChatMessageResponse;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    private List<ChatMessageResponse> mMessages;
    private String currentUsername;

    public ChatAdapter(List<ChatMessageResponse> messages) {
        this.mMessages = messages;
        this.currentUsername = SharedPrefsManager.getUsername();
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessages.get(position).getSenderUsername().equals(currentUsername)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_sent, parent, false);
            return new SentViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessageResponse msg = mMessages.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).tvMsg.setText(msg.getMessage());
        } else {
            ((ReceivedViewHolder) holder).tvMsg.setText(msg.getMessage());
        }
    }

    @Override
    public int getItemCount() { return mMessages.size(); }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMsg;
        SentViewHolder(View v) { super(v); tvMsg = v.findViewById(R.id.tvMessageSent); }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvMsg;
        ReceivedViewHolder(View v) { super(v); tvMsg = v.findViewById(R.id.tvMessageReceived); }
    }
}
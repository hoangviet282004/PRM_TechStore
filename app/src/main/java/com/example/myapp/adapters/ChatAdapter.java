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
    private final List<ChatMessageResponse> mMessages;
    private final String currentUsername;

    public ChatAdapter(List<ChatMessageResponse> messages) {
        this.mMessages = messages;
        this.currentUsername = SharedPrefsManager.getUsername();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessageResponse msg = mMessages.get(position);
        if (msg.getSenderUsername() != null && msg.getSenderUsername().equals(currentUsername)) {
            return TYPE_SENT;
        }
        return TYPE_RECEIVED;
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
            SentViewHolder sent = (SentViewHolder) holder;
            sent.tvMsg.setText(msg.getMessage());
            sent.tvTime.setText(formatTime(msg.getSentAt()));
        } else {
            ReceivedViewHolder received = (ReceivedViewHolder) holder;
            received.tvMsg.setText(msg.getMessage());
            received.tvSenderName.setText(msg.getSenderUsername());
            received.tvTime.setText(formatTime(msg.getSentAt()));
        }
    }

    @Override
    public int getItemCount() { return mMessages.size(); }

    // Extracts "HH:mm" from ISO-8601 strings like "2024-01-15T14:30:00" or "2024-01-15T14:30:00.000"
    private static String formatTime(String sentAt) {
        if (sentAt == null || sentAt.isEmpty()) return "";
        int tIndex = sentAt.indexOf('T');
        if (tIndex > 0 && sentAt.length() > tIndex + 5) {
            return sentAt.substring(tIndex + 1, tIndex + 6);
        }
        return sentAt;
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime;
        SentViewHolder(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMessageSent);
            tvTime = v.findViewById(R.id.tvTimeSent);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvMsg, tvSenderName, tvTime;
        ReceivedViewHolder(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMessageReceived);
            tvSenderName = v.findViewById(R.id.tvSenderName);
            tvTime = v.findViewById(R.id.tvTimeReceived);
        }
    }
}

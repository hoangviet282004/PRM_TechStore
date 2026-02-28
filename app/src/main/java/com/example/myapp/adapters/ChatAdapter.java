package com.example.myapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.models.response.ChatMessageResponse;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private List<ChatMessageResponse> messageList;

    public ChatAdapter(List<ChatMessageResponse> list, String role) { this.messageList = list; }

    @Override
    public int getItemViewType(int position) {
        // Nếu role là CLIENT thì hiển thị bên phải
        return messageList.get(position).getSenderRole().equals("CLIENT") ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessageResponse msg = messageList.get(position);
        if (holder instanceof SentViewHolder) ((SentViewHolder) holder).bind(msg);
        else ((ReceivedViewHolder) holder).bind(msg);
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    class SentViewHolder extends RecyclerView.ViewHolder {
        TextView txt;
        SentViewHolder(View v) { super(v); txt = v.findViewById(R.id.txtMessageSent); }
        void bind(ChatMessageResponse m) { txt.setText(m.getMessage()); }
    }

    class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView txt;
        ReceivedViewHolder(View v) { super(v); txt = v.findViewById(R.id.txtMessageReceived); }
        void bind(ChatMessageResponse m) { txt.setText(m.getMessage()); }
    }
}
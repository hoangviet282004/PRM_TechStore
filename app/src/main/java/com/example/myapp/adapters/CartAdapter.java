package com.example.myapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapp.databinding.ItemCartBinding;
import com.example.myapp.models.response.CartItemResponse;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItemResponse> items = new ArrayList<>();
    private final OnCartAction listener;

    public interface OnCartAction {
        void onUpdateQty(int productId, int qty);
        void onDelete(int cartItemId);
    }

    public CartAdapter(OnCartAction listener) {
        this.listener = listener;
    }

    public void setData(List<CartItemResponse> newList) {
        this.items = (newList != null) ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItemResponse item = items.get(position);

        holder.binding.tvName.setText(item.getProductName());

        // FIX GIÁ TIỀN: Dùng item.getPrice() thay cho unitPrice
        if (item.getPrice() != null) {
            holder.binding.tvPrice.setText(String.format("%,.0f VNĐ", item.getPrice().doubleValue()));
        } else {
            holder.binding.tvPrice.setText("0 VNĐ");
        }

        holder.binding.tvQty.setText(String.valueOf(item.getQuantity()));

        // FIX ẢNH: Dùng item.getProductImage() thay cho getImageUrl()
        Glide.with(holder.itemView.getContext())
                .load(item.getProductImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.binding.ivProduct);

        // Các sự kiện nút bấm giữ nguyên
        holder.binding.btnPlus.setOnClickListener(v ->
                listener.onUpdateQty(item.getProductId(), item.getQuantity() + 1));

        holder.binding.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                listener.onUpdateQty(item.getProductId(), item.getQuantity() - 1);
            }
        });

        holder.binding.btnRemove.setOnClickListener(v ->
                listener.onDelete(item.getId()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemCartBinding binding;
        public ViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
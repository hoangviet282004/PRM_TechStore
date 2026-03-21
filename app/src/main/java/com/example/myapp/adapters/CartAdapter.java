package com.example.myapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapp.databinding.ItemCartBinding;
import com.example.myapp.models.response.CartItemResponse;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItemResponse> items = new ArrayList<>();
    private final OnCartAction listener;

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.US);

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
        double itemPrice = 0.01;
        if (item.getPrice() != null) {
            itemPrice = item.getPrice().doubleValue();
        }
        holder.binding.tvPrice.setText(CURRENCY.format(itemPrice));
        holder.binding.tvQty.setText(String.valueOf(item.getQuantity()));

        // FIX ẢNH: Dùng item.getProductImage() thay cho getImageUrl()
        Glide.with(holder.itemView.getContext())
                .load(item.getProductImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.binding.ivProduct);

        // Các sự kiện nút bấm giữ nguyên
        holder.binding.btnPlus.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_ID) return;
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(pos);
            listener.onUpdateQty(item.getId(), 1);
        });

        holder.binding.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_ID) return;
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(pos);
                listener.onUpdateQty(item.getId(), -1);
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
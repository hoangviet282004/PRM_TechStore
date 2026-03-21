package com.example.myapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapp.R;
import com.example.myapp.models.response.CartItemResponse;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderSummaryItemAdapter extends RecyclerView.Adapter<OrderSummaryItemAdapter.ViewHolder> {

    private final List<CartItemResponse> items;

    public OrderSummaryItemAdapter(List<CartItemResponse> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_summary_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItemResponse item = items.get(position);
        holder.tvName.setText(item.getProductName());
        holder.tvQty.setText("x" + item.getQuantity());

        if (item.getPrice() != null) {
            BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            var formatter = NumberFormat.getCurrencyInstance(Locale.US);
            holder.tvPrice.setText(formatter.format(total.doubleValue()));
        }

        if (item.getProductImage() != null && !item.getProductImage().isEmpty()) {
            Glide.with(holder.imgProduct.getContext())
                    .load(item.getProductImage())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgProduct);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvQty, tvPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvQty = itemView.findViewById(R.id.tvProductQty);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
        }
    }
}

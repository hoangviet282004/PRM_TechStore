package com.example.myapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapp.R;
import com.example.myapp.databinding.ItemProductBinding;
import com.example.myapp.models.response.ProductListResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<ProductListResponse> list = new ArrayList<>();

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.US);

    public void setData(List<ProductListResponse> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding b = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductListResponse p = list.get(position);
        holder.b.tvName.setText(p.getProductName());
        holder.b.tvDescription.setText(p.getBriefDescription());
        holder.b.tvPrice.setText(CURRENCY.format(p.getPrice().doubleValue()));

        // --- THÊM ĐOẠN NÀY ĐỂ MỞ CHI TIẾT ---
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), com.example.myapp.Activities.ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", p.getId()); // Gửi ID sản phẩm đi
            v.getContext().startActivity(intent);
        });
        // ------------------------------------

        // Trong hàm onBindViewHolder của ProductAdapter.java
        String imageUrl = p.getPrimaryImageUrl();
        Log.d("GLIDE_DEBUG", "URL ảnh hiện tại: " + imageUrl); // THÊM DÒNG NÀY

        if (imageUrl != null && imageUrl.contains("localhost")) {
            imageUrl = imageUrl.replace("localhost", "10.0.2.2");
        }
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground) // THÊM DÒNG NÀY: Hiện hình Android nếu lỗi
                .into(holder.b.ivProduct);
    }

    @Override public int getItemCount() { return list.size(); }
    class ProductViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding b;
        ProductViewHolder(ItemProductBinding b) { super(b.getRoot()); this.b = b; }
    }
}

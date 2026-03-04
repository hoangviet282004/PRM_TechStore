package com.example.myapp.adapters;

import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {
    private List<String> images;

    public ImageSliderAdapter(List<String> images) { this.images = images; }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(parent.getContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ViewHolder(iv);
    }

    // FIX LỖI: Thêm hàm setData để cập nhật dữ liệu từ Activity
    public void setData(List<String> newData) {
        this.images = newData;
        notifyDataSetChanged(); // Yêu cầu Adapter vẽ lại các ảnh mới
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = images.get(position);
        if (url != null && url.contains("localhost")) url = url.replace("localhost", "10.0.2.2");
        Glide.with(holder.itemView.getContext()).load(url).centerCrop().into((ImageView) holder.itemView);
    }

    @Override public int getItemCount() { return images != null ? images.size() : 0; }
    class ViewHolder extends RecyclerView.ViewHolder { public ViewHolder(@NonNull android.view.View v) { super(v); } }
}
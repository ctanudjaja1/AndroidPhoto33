package com.example.photos33.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos33.R;
import com.example.photos33.model.Photo;

import java.io.InputStream;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    // Split into two single-method interfaces so lambdas work
    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo, int position);
    }

    public interface OnPhotoLongClickListener {
        void onPhotoLongClick(Photo photo, int position);
    }

    private List<Photo> photos;
    private final OnPhotoClickListener clickListener;
    private final OnPhotoLongClickListener longClickListener;

    public PhotoAdapter(List<Photo> photos,
                        OnPhotoClickListener clickListener,
                        OnPhotoLongClickListener longClickListener) {
        this.photos = photos;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Photo photo = photos.get(position);

        try {
            Uri uri = Uri.parse(photo.getFilePath());
            InputStream stream = holder.itemView.getContext()
                    .getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            if (bitmap != null) {
                holder.thumbnail.setImageBitmap(bitmap);
            } else {
                holder.thumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } catch (Exception e) {
            holder.thumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v ->
                clickListener.onPhotoClick(photo, position));

        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onPhotoLongClick(photo, position);
            return true;
        });
    }

    @Override
    public int getItemCount() { return photos.size(); }

    public void updatePhotos(List<Photo> newPhotos) {
        this.photos = newPhotos;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.photo_thumbnail);
        }
    }
}
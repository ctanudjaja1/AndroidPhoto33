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
import com.google.android.material.card.MaterialCardView;

import java.io.InputStream;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo, int position);
    }

    public interface OnPhotoLongClickListener {
        void onPhotoLongClick(Photo photo, int position);
    }

    private List<Photo> photos;
    private final OnPhotoClickListener clickListener;
    private final OnPhotoLongClickListener longClickListener;
    private int selectedPosition = -1; // -1 = nothing selected

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

        // Load image
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

        // Highlight selected card with accent border
        if (position == selectedPosition) {
            holder.cardView.setStrokeColor(
                    holder.itemView.getContext().getColor(R.color.accent));
            holder.cardView.setStrokeWidth(6);
            holder.cardView.setCardBackgroundColor(
                    holder.itemView.getContext().getColor(R.color.primaryMid));
            // Dim the thumbnail slightly so the border pops
            holder.thumbnail.setAlpha(0.75f);
        } else {
            holder.cardView.setStrokeWidth(0);
            holder.cardView.setCardBackgroundColor(
                    holder.itemView.getContext().getColor(R.color.cardBg));
            holder.thumbnail.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v ->
                clickListener.onPhotoClick(photo, position));

        holder.itemView.setOnLongClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            // Refresh old and new so highlight moves instantly
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            longClickListener.onPhotoLongClick(photo, position);
            return true;
        });
    }

    @Override
    public int getItemCount() { return photos.size(); }

    public void updatePhotos(List<Photo> newPhotos) {
        this.photos = newPhotos;
        selectedPosition = -1; // clear selection on refresh
        notifyDataSetChanged();
    }

    // Call after remove or move to clear the highlight
    public void clearSelection() {
        int prev = selectedPosition;
        selectedPosition = -1;
        if (prev >= 0) notifyItemChanged(prev);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.photo_thumbnail);
            // The root of photo_view.xml is a FrameLayout wrapping a MaterialCardView
            // so we grab the card directly
            cardView = itemView.findViewById(R.id.photoCard);
        }
    }
}
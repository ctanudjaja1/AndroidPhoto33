package com.example.photos33.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos33.R;
import com.example.photos33.model.Album;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }

    public interface OnAlbumLongClickListener {
        void onAlbumLongClick(Album album);
    }

    private List<Album> albums;
    private final OnAlbumClickListener clickListener;
    private final OnAlbumLongClickListener longClickListener;
    private int selectedPosition = -1; // -1 = nothing selected

    public AlbumAdapter(List<Album> albums,
                        OnAlbumClickListener clickListener,
                        OnAlbumLongClickListener longClickListener) {
        this.albums = albums;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = albums.get(position);

        holder.albumName.setText(album.getAlbumName());
        holder.albumCount.setText(album.getPhotoCount() + " photo(s)");

        // Highlight selected card with accent border
        if (position == selectedPosition) {
            holder.cardView.setStrokeColor(
                    holder.itemView.getContext().getColor(R.color.accent));
            holder.cardView.setStrokeWidth(6);
            holder.cardView.setCardBackgroundColor(
                    holder.itemView.getContext().getColor(R.color.primaryMid));
        } else {
            holder.cardView.setStrokeWidth(0);
            holder.cardView.setCardBackgroundColor(
                    holder.itemView.getContext().getColor(R.color.cardBg));
        }

        holder.itemView.setOnClickListener(v -> clickListener.onAlbumClick(album));

        holder.itemView.setOnLongClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            // Refresh both the old and new card so highlight moves instantly
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            longClickListener.onAlbumLongClick(album);
            return true;
        });
    }

    @Override
    public int getItemCount() { return albums.size(); }

    public void updateAlbums(List<Album> newAlbums) {
        this.albums = newAlbums;
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    // Call after delete or rename to clear the highlight
    public void clearSelection() {
        int prev = selectedPosition;
        selectedPosition = -1;
        if (prev >= 0) notifyItemChanged(prev);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView albumName, albumCount;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.album_name);
            albumCount = itemView.findViewById(R.id.album_count);
            // The root of album_view.xml is a MaterialCardView
            cardView = (MaterialCardView) itemView;
        }
    }
}
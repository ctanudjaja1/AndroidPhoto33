package com.example.photos33.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos33.R;
import com.example.photos33.model.Album;

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

        holder.itemView.setOnClickListener(v -> clickListener.onAlbumClick(album));
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onAlbumLongClick(album);
            return true;
        });
    }

    @Override
    public int getItemCount() { return albums.size(); }

    public void updateAlbums(List<Album> newAlbums) {
        this.albums = newAlbums;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView albumName, albumCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumName  = itemView.findViewById(R.id.album_name);
            albumCount = itemView.findViewById(R.id.album_count);
        }
    }
}
package com.example.photos33.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos33.R;
import com.example.photos33.adapter.PhotoAdapter;
import com.example.photos33.model.Album;
import com.example.photos33.model.Photo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    private static final String DATA_FILE = "albums.ser";

    private List<Album> allAlbums;
    private Album currentAlbum;
    private PhotoAdapter photoAdapter;
    private Photo selectedPhoto = null;

    private final ActivityResultLauncher<String[]> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;

                getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                String path = uri.toString();

                for (Photo p : currentAlbum.getPhotos()) {
                    if (p.getFilePath().equals(path)) {
                        Toast.makeText(this, "Photo already in album", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Photo newPhoto = new Photo(path);
                currentAlbum.addPhoto(newPhoto);
                photoAdapter.updatePhotos(currentAlbum.getPhotos());
                saveAlbums();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        String albumName = getIntent().getStringExtra("albumName");

        allAlbums = loadAlbums();
        currentAlbum = findAlbum(albumName);

        if (currentAlbum == null) {
            Toast.makeText(this, "Album not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView title = findViewById(R.id.albumTitle);
        title.setText(currentAlbum.getAlbumName());

        RecyclerView recyclerView = findViewById(R.id.photoRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Split interfaces — click and long-click are separate lambdas
        photoAdapter = new PhotoAdapter(
                currentAlbum.getPhotos(),
                (photo, position) -> {
                    Intent intent = new Intent(AlbumActivity.this, PhotoActivity.class);
                    intent.putExtra("albumName", currentAlbum.getAlbumName());
                    intent.putExtra("photoIndex", position);
                    startActivity(intent);
                },
                (photo, position) -> {
                    selectedPhoto = photo;
                    Toast.makeText(AlbumActivity.this,
                            "Selected photo " + (position + 1), Toast.LENGTH_SHORT).show();
                }
        );

        recyclerView.setAdapter(photoAdapter);

        findViewById(R.id.btnAddPhoto).setOnClickListener(v ->
                photoPickerLauncher.launch(new String[]{"image/*"}));
        findViewById(R.id.btnRemovePhoto).setOnClickListener(v -> handleRemovePhoto());
        findViewById(R.id.btnMovePhoto).setOnClickListener(v -> handleMovePhoto());
    }

    @Override
    protected void onResume() {
        super.onResume();
        allAlbums = loadAlbums();
        currentAlbum = findAlbum(currentAlbum.getAlbumName());
        if (currentAlbum != null) {
            photoAdapter.updatePhotos(currentAlbum.getPhotos());
        }
    }

    private void handleRemovePhoto() {
        if (selectedPhoto == null) {
            Toast.makeText(this, "Long-press a photo first", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Remove Photo")
                .setMessage("Remove this photo from the album?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    currentAlbum.getPhotos().remove(selectedPhoto);
                    selectedPhoto = null;
                    photoAdapter.updatePhotos(currentAlbum.getPhotos());
                    saveAlbums();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleMovePhoto() {
        if (selectedPhoto == null) {
            Toast.makeText(this, "Long-press a photo first", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> otherAlbumNames = new ArrayList<>();
        for (Album a : allAlbums) {
            if (!a.getAlbumName().equals(currentAlbum.getAlbumName())) {
                otherAlbumNames.add(a.getAlbumName());
            }
        }

        if (otherAlbumNames.isEmpty()) {
            Toast.makeText(this, "No other albums to move to", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = otherAlbumNames.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Move to Album")
                .setItems(options, (dialog, which) -> {
                    Album destination = findAlbum(options[which]);
                    if (destination == null) return;

                    destination.addPhoto(selectedPhoto);
                    currentAlbum.getPhotos().remove(selectedPhoto);
                    selectedPhoto = null;
                    photoAdapter.updatePhotos(currentAlbum.getPhotos());
                    saveAlbums();
                    Toast.makeText(this, "Photo moved", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private Album findAlbum(String name) {
        for (Album a : allAlbums) {
            if (a.getAlbumName().equals(name)) return a;
        }
        return null;
    }

    private void saveAlbums() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                openFileOutput(DATA_FILE, MODE_PRIVATE))) {
            oos.writeObject(new ArrayList<>(allAlbums));
        } catch (IOException e) {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Album> loadAlbums() {
        try (ObjectInputStream ois = new ObjectInputStream(openFileInput(DATA_FILE))) {
            return (ArrayList<Album>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
package com.example.photos33.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos33.R;
import com.example.photos33.adapter.AlbumAdapter;
import com.example.photos33.model.Album;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String DATA_FILE = "albums.ser";

    private List<Album> albumList;
    private AlbumAdapter albumAdapter;
    private Album selectedAlbum = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        albumList = loadAlbums();

        RecyclerView recyclerView = findViewById(R.id.albumRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Split interfaces — each lambda maps to one method
        albumAdapter = new AlbumAdapter(
                albumList,
                album -> {
                    Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
                    intent.putExtra("albumName", album.getAlbumName());
                    startActivity(intent);
                },
                album -> {
                    selectedAlbum = album;
                    Toast.makeText(MainActivity.this,
                            "Selected: " + album.getAlbumName(), Toast.LENGTH_SHORT).show();
                }
        );

        recyclerView.setAdapter(albumAdapter);

        findViewById(R.id.btnCreateAlbum).setOnClickListener(v -> handleCreateAlbum());
        findViewById(R.id.btnRenameAlbum).setOnClickListener(v -> handleRenameAlbum());
        findViewById(R.id.btnDeleteAlbum).setOnClickListener(v -> handleDeleteAlbum());
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        albumList.clear();
        albumList.addAll(loadAlbums());
        albumAdapter.updateAlbums(albumList);
    }

    private void handleCreateAlbum() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Album name");

        new AlertDialog.Builder(this)
                .setTitle("New Album")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (albumExists(name)) {
                        Toast.makeText(this, "Album already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    albumList.add(new Album(name));
                    albumAdapter.updateAlbums(albumList);
                    saveAlbums();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleRenameAlbum() {
        if (selectedAlbum == null) {
            Toast.makeText(this, "Long-press an album first", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(selectedAlbum.getAlbumName());

        new AlertDialog.Builder(this)
                .setTitle("Rename Album")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (albumExists(newName)) {
                        Toast.makeText(this, "Album already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedAlbum.setName(newName);
                    selectedAlbum = null;
                    albumAdapter.updateAlbums(albumList);
                    saveAlbums();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleDeleteAlbum() {
        if (selectedAlbum == null) {
            Toast.makeText(this, "Long-press an album first", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Album")
                .setMessage("Delete \"" + selectedAlbum.getAlbumName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    albumList.remove(selectedAlbum);
                    selectedAlbum = null;
                    albumAdapter.updateAlbums(albumList);
                    saveAlbums();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean albumExists(String name) {
        for (Album a : albumList) {
            if (a.getAlbumName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private void saveAlbums() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                openFileOutput(DATA_FILE, MODE_PRIVATE))) {
            oos.writeObject(new ArrayList<>(albumList));
        } catch (IOException e) {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Album> loadAlbums() {
        try (ObjectInputStream ois = new ObjectInputStream(
                openFileInput(DATA_FILE))) {
            return (ArrayList<Album>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
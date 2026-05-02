package com.example.photos33.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photos33.R;
import com.example.photos33.model.Album;
import com.example.photos33.model.Photo;
import com.example.photos33.model.Tag;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PhotoActivity extends AppCompatActivity {

    private static final String DATA_FILE = "albums.ser";

    private List<Album> allAlbums;
    private Album currentAlbum;
    private List<Photo> photos;
    private int currentIndex;

    private ImageView photoDisplay;
    private TextView photoFilename;
    private TextView photoTags;
    private TextView photoIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // Get data passed from AlbumActivity
        String albumName = getIntent().getStringExtra("albumName");
        currentIndex = getIntent().getIntExtra("photoIndex", 0);

        allAlbums = loadAlbums();
        currentAlbum = findAlbum(albumName);

        if (currentAlbum == null) {
            Toast.makeText(this, "Album not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        photos = currentAlbum.getPhotos();

        // Wire views
        photoDisplay  = findViewById(R.id.photoDisplay);
        photoFilename = findViewById(R.id.photoFilename);
        photoTags     = findViewById(R.id.photoTags);
        photoIndex    = findViewById(R.id.photoIndex);

        // Slideshow buttons
        findViewById(R.id.btnPrevPhoto).setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                displayCurrentPhoto();
            } else {
                Toast.makeText(this, "Already at first photo", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnNextPhoto).setOnClickListener(v -> {
            if (currentIndex < photos.size() - 1) {
                currentIndex++;
                displayCurrentPhoto();
            } else {
                Toast.makeText(this, "Already at last photo", Toast.LENGTH_SHORT).show();
            }
        });

        // Tag buttons
        findViewById(R.id.btnAddTag).setOnClickListener(v -> handleAddTag());
        findViewById(R.id.btnDeleteTag).setOnClickListener(v -> handleDeleteTag());

        displayCurrentPhoto();
    }

    // ── Display ───────────────────────────────────────────────────────

    private void displayCurrentPhoto() {
        if (photos.isEmpty()) {
            Toast.makeText(this, "No photos in album", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Photo photo = photos.get(currentIndex);

        // Load image from URI
        try {
            Uri uri = Uri.parse(photo.getFilePath());
            InputStream stream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            if (bitmap != null) {
                photoDisplay.setImageBitmap(bitmap);
            } else {
                photoDisplay.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } catch (Exception e) {
            photoDisplay.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Show filename (last segment of URI)
        String path = photo.getFilePath();
        String filename = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;
        photoFilename.setText(filename);

        // Show index counter
        photoIndex.setText((currentIndex + 1) + " / " + photos.size());

        // Show tags
        refreshTagsDisplay(photo);
    }

    private void refreshTagsDisplay(Photo photo) {
        if (photo.getTags().isEmpty()) {
            photoTags.setText("No tags");
        } else {
            StringBuilder sb = new StringBuilder("Tags: ");
            for (Tag tag : photo.getTags()) {
                sb.append(tag.toString()).append("  ");
            }
            photoTags.setText(sb.toString().trim());
        }
    }

    // ── Tag Operations ────────────────────────────────────────────────

    private void handleAddTag() {
        Photo photo = photos.get(currentIndex);

        // Step 1: pick tag type (only person or location)
        String[] tagTypes = {"person", "location"};

        new AlertDialog.Builder(this)
                .setTitle("Select Tag Type")
                .setItems(tagTypes, (typeDialog, typeIndex) -> {
                    String selectedType = tagTypes[typeIndex];

                    // Step 2: enter tag value
                    EditText input = new EditText(this);
                    input.setHint("Enter " + selectedType);

                    new AlertDialog.Builder(this)
                            .setTitle("Add " + selectedType + " tag")
                            .setView(input)
                            .setPositiveButton("Add", (valueDialog, which) -> {
                                String value = input.getText().toString().trim();
                                if (value.isEmpty()) {
                                    Toast.makeText(this, "Value cannot be empty",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Tag newTag = new Tag(selectedType, value);
                                boolean added = photo.addTag(newTag);
                                if (!added) {
                                    Toast.makeText(this, "Tag already exists",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    refreshTagsDisplay(photo);
                                    saveAlbums();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleDeleteTag() {
        Photo photo = photos.get(currentIndex);

        if (photo.getTags().isEmpty()) {
            Toast.makeText(this, "No tags to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build list of tag strings to show in dialog
        String[] tagStrings = photo.getTags().stream()
                .map(Tag::toString)
                .toArray(String[]::new);

        new AlertDialog.Builder(this)
                .setTitle("Delete Tag")
                .setItems(tagStrings, (dialog, which) -> {
                    Tag tagToRemove = photo.getTags().get(which);
                    photo.removeTag(tagToRemove);
                    refreshTagsDisplay(photo);
                    saveAlbums();
                    Toast.makeText(this, "Tag deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private Album findAlbum(String name) {
        for (Album a : allAlbums) {
            if (a.getAlbumName().equals(name)) return a;
        }
        return null;
    }

    // ── Persistence ───────────────────────────────────────────────────

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
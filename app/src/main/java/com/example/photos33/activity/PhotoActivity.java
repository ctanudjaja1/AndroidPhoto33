package com.example.photos33.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

        String albumName = getIntent().getStringExtra("albumName");
        currentIndex = getIntent().getIntExtra("photoIndex", 0);

        allAlbums = loadAlbums();
        currentAlbum = findAlbum(albumName);

        if (currentAlbum == null) {
            Toast.makeText(this, "Album not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Use the list directly from allAlbums so mutations are reflected on save
        photos = currentAlbum.getPhotos();

        photoDisplay  = findViewById(R.id.photoDisplay);
        photoFilename = findViewById(R.id.photoFilename);
        photoTags     = findViewById(R.id.photoTags);
        photoIndex    = findViewById(R.id.photoIndex);

        findViewById(R.id.btnPrevPhoto).setOnClickListener(v -> {
            if (currentIndex > 0) { currentIndex--; displayCurrentPhoto(); }
            else Toast.makeText(this, "Already at first photo", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnNextPhoto).setOnClickListener(v -> {
            if (currentIndex < photos.size() - 1) { currentIndex++; displayCurrentPhoto(); }
            else Toast.makeText(this, "Already at last photo", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnAddTag).setOnClickListener(v -> handleAddTag());
        findViewById(R.id.btnDeleteTag).setOnClickListener(v -> handleDeleteTag());

        displayCurrentPhoto();
    }

    private void displayCurrentPhoto() {
        if (photos.isEmpty()) {
            Toast.makeText(this, "No photos in album", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Photo photo = photos.get(currentIndex);

        try {
            Uri uri = Uri.parse(photo.getFilePath());
            InputStream stream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            photoDisplay.setImageBitmap(bitmap != null ? bitmap :
                    android.graphics.BitmapFactory.decodeResource(getResources(),
                            android.R.drawable.ic_menu_gallery));
        } catch (Exception e) {
            photoDisplay.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        String path = photo.getFilePath();
        photoFilename.setText(path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path);
        photoIndex.setText((currentIndex + 1) + " / " + photos.size());
        refreshTagsDisplay(photo);
    }

    private void refreshTagsDisplay(Photo photo) {
        List<Tag> tags = photo.getTags();
        if (tags.isEmpty()) {
            photoTags.setText("No tags");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tags.size(); i++) {
                if (i > 0) sb.append("\n");
                sb.append(tags.get(i).toString());
            }
            photoTags.setText(sb.toString());
        }
    }

    private void handleAddTag() {
        Photo photo = photos.get(currentIndex);
        String[] tagTypes = {"person", "location"};

        new AlertDialog.Builder(this)
                .setTitle("Select Tag Type")
                .setItems(tagTypes, (typeDialog, typeIndex) -> {
                    String selectedType = tagTypes[typeIndex];
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
                                if (!photo.addTag(newTag)) {
                                    Toast.makeText(this, "Tag already exists",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    refreshTagsDisplay(photo);
                                    saveAlbums();
                                    Toast.makeText(this, "Tag added", Toast.LENGTH_SHORT).show();
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
        // Snapshot the tags list at dialog-open time into a separate list
        // so the index stays stable even if anything changes
        List<Tag> tagSnapshot = new ArrayList<>(photo.getTags());

        if (tagSnapshot.isEmpty()) {
            Toast.makeText(this, "No tags to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] tagStrings = new String[tagSnapshot.size()];
        for (int i = 0; i < tagSnapshot.size(); i++) {
            tagStrings[i] = tagSnapshot.get(i).toString();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Tag to Delete")
                .setItems(tagStrings, (dialog, which) -> {
                    Tag tagToRemove = tagSnapshot.get(which); // use snapshot index
                    // Remove by matching type+value directly, bypassing equals() ambiguity
                    photo.getTags().removeIf(t ->
                            t.getTagType().equalsIgnoreCase(tagToRemove.getTagType()) &&
                                    t.getValue().equalsIgnoreCase(tagToRemove.getValue())
                    );
                    refreshTagsDisplay(photo);
                    saveAlbums();
                    Toast.makeText(this, "Tag deleted", Toast.LENGTH_SHORT).show();
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
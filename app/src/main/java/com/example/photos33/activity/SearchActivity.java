package com.example.photos33.activity;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos33.R;
import com.example.photos33.adapter.PhotoAdapter;
import com.example.photos33.model.Album;
import com.example.photos33.model.Photo;
import com.example.photos33.model.Tag;

import java.io.*;
import java.util.*;

public class SearchActivity extends AppCompatActivity {

    private static final String DATA_FILE = "albums.ser";

    private AutoCompleteTextView autoValue1, autoValue2;
    private Spinner spinnerType1, spinnerType2;
    private RadioGroup radioGroup;
    private TextView resultCount;
    private PhotoAdapter resultAdapter;

    private List<Album> allAlbums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        allAlbums = loadAlbums();

        // Wire views
        autoValue1    = findViewById(R.id.autoCompleteValue1);
        autoValue2    = findViewById(R.id.autoCompleteValue2);
        spinnerType1  = findViewById(R.id.spinnerTagType1);
        spinnerType2  = findViewById(R.id.spinnerTagType2);
        radioGroup    = findViewById(R.id.radioGroupOperator);
        resultCount   = findViewById(R.id.searchResultCount);

        // Populate spinners with only person and location
        String[] tagTypes = {"person", "location"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, tagTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType1.setAdapter(spinnerAdapter);
        spinnerType2.setAdapter(spinnerAdapter);

        // Set up autocomplete for value fields
        setupAutoComplete(autoValue1, spinnerType1);
        setupAutoComplete(autoValue2, spinnerType2);

        // Results RecyclerView
        RecyclerView recyclerView = findViewById(R.id.searchResultsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        resultAdapter = new PhotoAdapter(
                new ArrayList<>(),
                (photo, position) -> {},   // OnPhotoClickListener
                (photo, position) -> {}    // OnPhotoLongClickListener
        );
        recyclerView.setAdapter(resultAdapter);

        // Search button
        findViewById(R.id.btnSearch).setOnClickListener(v -> handleSearch());
    }

    private void setupAutoComplete(AutoCompleteTextView field, Spinner spinner) {
        // Update autocomplete suggestions whenever the user types
        field.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                String tagType = spinner.getSelectedItem().toString();
                String prefix = s.toString().toLowerCase();
                List<String> suggestions = getAutoCompleteSuggestions(tagType, prefix);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        SearchActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        suggestions);
                field.setAdapter(adapter);
                field.showDropDown();
            }
            @Override public void afterTextChanged(android.text.Editable e) {}
        });
    }

    private List<String> getAutoCompleteSuggestions(String tagType, String prefix) {
        Set<String> seen = new HashSet<>();
        List<String> suggestions = new ArrayList<>();

        for (Album album : allAlbums) {
            for (Photo photo : album.getPhotos()) {
                for (Tag tag : photo.getTags()) {
                    if (tag.getTagType().equalsIgnoreCase(tagType)) {
                        String val = tag.getValue().toLowerCase();
                        if (val.startsWith(prefix) && seen.add(val)) {
                            suggestions.add(tag.getValue());
                        }
                    }
                }
            }
        }
        return suggestions;
    }

    private void handleSearch() {
        String type1  = spinnerType1.getSelectedItem().toString();
        String value1 = autoValue1.getText().toString().trim();
        String type2  = spinnerType2.getSelectedItem().toString();
        String value2 = autoValue2.getText().toString().trim();

        if (value1.isEmpty()) {
            Toast.makeText(this, "Enter at least one search value", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean useAnd = ((RadioButton) findViewById(R.id.radioAnd)).isChecked();
        boolean useTwoTerms = !value2.isEmpty();

        List<Photo> results = new ArrayList<>();
        Set<String> seen = new HashSet<>(); // avoid duplicate results

        for (Album album : allAlbums) {
            for (Photo photo : album.getPhotos()) {
                boolean matches;

                if (!useTwoTerms) {
                    // Single term: match prefix
                    matches = photoMatchesPrefix(photo, type1, value1);
                } else if (useAnd) {
                    // AND: photo must match both terms
                    matches = photoMatchesPrefix(photo, type1, value1)
                            && photoMatchesPrefix(photo, type2, value2);
                } else {
                    // OR: photo must match either term
                    matches = photoMatchesPrefix(photo, type1, value1)
                            || photoMatchesPrefix(photo, type2, value2);
                }

                if (matches && seen.add(photo.getFilePath())) {
                    results.add(photo);
                }
            }
        }

        resultCount.setText(results.size() + " result(s) found");
        resultAdapter.updatePhotos(results);
    }

    private boolean photoMatchesPrefix(Photo photo, String tagType, String prefix) {
        for (Tag tag : photo.getTags()) {
            if (tag.getTagType().equalsIgnoreCase(tagType) &&
                    tag.getValue().toLowerCase().startsWith(prefix.toLowerCase())) {
                return true;
            }
        }
        return false;
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
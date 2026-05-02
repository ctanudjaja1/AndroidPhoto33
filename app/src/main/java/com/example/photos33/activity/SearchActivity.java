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

        autoValue1   = findViewById(R.id.autoCompleteValue1);
        autoValue2   = findViewById(R.id.autoCompleteValue2);
        spinnerType1 = findViewById(R.id.spinnerTagType1);
        spinnerType2 = findViewById(R.id.spinnerTagType2);
        radioGroup   = findViewById(R.id.radioGroupOperator);
        resultCount  = findViewById(R.id.searchResultCount);

        String[] tagTypes = {"person", "location"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, tagTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType1.setAdapter(spinnerAdapter);
        spinnerType2.setAdapter(spinnerAdapter);

        // Set initial autocomplete suggestions
        bindAutoComplete(autoValue1, "person");
        bindAutoComplete(autoValue2, "person");

        // Rebuild suggestions when tag type changes — don't touch the text
        spinnerType1.setOnItemSelectedListener(spinnerListener(autoValue1, tagTypes));
        spinnerType2.setOnItemSelectedListener(spinnerListener(autoValue2, tagTypes));

        RecyclerView recyclerView = findViewById(R.id.searchResultsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        resultAdapter = new PhotoAdapter(
                new ArrayList<>(),
                (photo, position) -> {},
                (photo, position) -> {}
        );
        recyclerView.setAdapter(resultAdapter);

        findViewById(R.id.btnSearch).setOnClickListener(v -> handleSearch());
    }

    /**
     * Binds a static suggestion list to the AutoCompleteTextView.
     * AutoCompleteTextView handles prefix filtering automatically —
     * we just need to set the full list once per tag type.
     */
    private void bindAutoComplete(AutoCompleteTextView field, String tagType) {
        List<String> values = getAllValuesForType(tagType);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, values);
        field.setAdapter(adapter);
        field.setThreshold(1); // show after 1 character typed
    }

    private AdapterView.OnItemSelectedListener spinnerListener(
            AutoCompleteTextView field, String[] tagTypes) {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view,
                                       int position, long id) {
                bindAutoComplete(field, tagTypes[position]);
                field.setText(""); // clear stale value when type changes
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };
    }

    private List<String> getAllValuesForType(String tagType) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (Album album : allAlbums) {
            for (Photo photo : album.getPhotos()) {
                for (Tag tag : photo.getTags()) {
                    if (tag.getTagType().equalsIgnoreCase(tagType)) {
                        seen.add(tag.getValue());
                    }
                }
            }
        }
        return new ArrayList<>(seen);
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

        boolean useAnd      = ((RadioButton) findViewById(R.id.radioAnd)).isChecked();
        boolean useTwoTerms = !value2.isEmpty();

        List<Photo> results = new ArrayList<>();
        Set<String> seen    = new HashSet<>();

        for (Album album : allAlbums) {
            for (Photo photo : album.getPhotos()) {
                boolean matches;
                if (!useTwoTerms) {
                    matches = photoMatchesPrefix(photo, type1, value1);
                } else if (useAnd) {
                    matches = photoMatchesPrefix(photo, type1, value1)
                            && photoMatchesPrefix(photo, type2, value2);
                } else {
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
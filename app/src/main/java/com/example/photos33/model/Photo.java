package com.example.photos33.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.ArrayList;
import java.io.File;
import java.util.Objects;

public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String filePath;
    private String caption;
    private Calendar dateTime;
    private ArrayList<Tag> tags;

    // Original constructor - kept for compatibility
    public Photo(File file) {
        this.filePath = file.getAbsolutePath();
        this.caption = "";
        this.tags = new ArrayList<>();
        this.dateTime = Calendar.getInstance();
        if (file.exists()) {
            this.dateTime.setTimeInMillis(file.lastModified());
        }
        this.dateTime.set(Calendar.MILLISECOND, 0);
    }

    // New constructor for Android URI strings (content://...)
    public Photo(String uriString) {
        this.filePath = uriString;
        this.caption = "";
        this.tags = new ArrayList<>();
        this.dateTime = Calendar.getInstance();
        this.dateTime.set(Calendar.MILLISECOND, 0);
    }

    public boolean addTag(Tag newTag) {
        if (hasTag(newTag.getTagType(), newTag.getValue())) {
            return false;
        }
        tags.add(newTag);
        return true;
    }

    public boolean hasTag(String type, String value) {
        for (Tag t : tags) {
            if (t.getTagType().equalsIgnoreCase(type) && t.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public void removeTag(Tag targetTag) {
        tags.removeIf(t -> t.equals(targetTag));
    }

    public String getFilePath() { return filePath; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public Calendar getDateTime() { return dateTime; }
    public ArrayList<Tag> getTags() { return tags; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Photo)) return false;
        Photo photo = (Photo) o;
        return Objects.equals(filePath, photo.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }

    @Override
    public String toString() {
        return "Photo{" + "path='" + filePath + '\'' + ", caption='" + caption + '\'' + '}';
    }
}
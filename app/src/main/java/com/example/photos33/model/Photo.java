package com.example.photos33.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.ArrayList;
import java.io.File;
import java.util.Objects;


/**
 * This class represents a single image in the application.
 * It’s essentially a wrapper around a file path, but I’ve added
 * logic to handle captions, metadata tags, and the specific
 * modification date of the file on your computer.
 * @author Christopher Tanudjaja
 */

public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The file path to the photo image. */
    private String filePath;

    /** The caption for the photo. */
    private String caption;

    /** The date and time the photo was taken. */
    private Calendar dateTime;

    /** The list of tags associated with this photo. */
    private ArrayList<Tag> tags;

    /**
     * Creates a new Photo object based on a file.
     * I made sure to pull the 'lastModified' date directly from the
     * actual file on disk to satisfy the project requirements.
     * I also zero out the milliseconds—this is a little trick to make
     * sure that date comparisons during searches don't fail over
     * tiny fractions of a second.
     * @param file the {@link File} object representing the image on disk
     */
    public Photo(File file) {
        this.filePath = file.getAbsolutePath();
        this.caption = "";
        this.tags = new ArrayList<>();

        this.dateTime = Calendar.getInstance();
        // Requirement: use file modification date
        if (file.exists()) {
            this.dateTime.setTimeInMillis(file.lastModified());
        }
        // Requirement: set milliseconds to zero for consistent equality checks
        this.dateTime.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Adds a new tag to this specific photo.
     * I built in a check here so we don't accidentally add the exact
     * same tag twice, keeping the metadata clean for the user.
     * @param newTag the {@link Tag} to be added
     * @return true if the tag was added successfully; false if it already exists
     */
    public boolean addTag(Tag newTag) {
        if (hasTag(newTag.getTagType(), newTag.getValue())) {
            return false;
        }
        tags.add(newTag);
        return true;
    }

    /**
     * Checks if this photo already has a specific tag.
     * I'm using case-insensitive checks here so that "NYC" and "nyc"
     * are treated as the same location.
     * @param type the tag type/category to check
     * @param value the tag value to check
     * @return true if a matching tag is found; false otherwise
     */
    public boolean hasTag(String type, String value) {
        for (Tag t : tags) {
            if (t.getTagType().equalsIgnoreCase(type) && t.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a tag from the photo.
     * I'm using a lambda here (removeIf) because it's a much cleaner way
     * to strip out matching tags than a manual iterator loop.
     * @param targetTag the {@link Tag} object to remove
     */
    public void removeTag(Tag targetTag) {
        tags.removeIf(t -> t.equals(targetTag));
    }


    /**
     * Returns the absolute file path of the photo.
     * @return the file path string
     */
    public String getFilePath() { return filePath; }

    /**
     * Returns the caption associated with the photo.
     * @return the caption string
     */
    public String getCaption() { return caption; }

    /**
     * Sets the caption for the photo.
     * @param caption the new caption to set
     */
    public void setCaption(String caption) { this.caption = caption; }

    /**
     *  Returns the date and time the file was last modified.
     * @return a {@link Calendar} object representing the file date
     */
    public Calendar getDateTime() { return dateTime; }

    /**
     * Returns the list of tags associated with this photo.
     * @return an {@link ArrayList} of {@link Tag} objects
     */
    public ArrayList<Tag> getTags() { return tags; }

    /**
     * Defines what makes two photos "equal."
     * In this app, I decided that if the file path is the same,
     * it's the same photo. This prevents the user from adding
     * the exact same file to an album multiple times.
     * @param o the object to compare against
     * @return true if the object is a Photo with the same file path
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Photo)) return false;
        Photo photo = (Photo) o;
        return Objects.equals(filePath, photo.filePath);
    }

    /**
     * Generates a hash code for the Photo based on the file path.
     * @return the integer hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }

    /**
     * Formats the Photo data as a string.
     * @return a string containing the path and caption
     */
    @Override
    public String toString() {
        return "Photo{" + "path='" + filePath + '\'' + ", caption='" + caption + '\'' + '}';
    }
}

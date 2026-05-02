package com.example.photos33.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class acts as a dedicated folder within the app.
 * I designed it to keep track of a specific collection of photos.
 * Since users need to see their album names and how many pictures are
 * inside them on the main dashboard, this class handles both the
 * storage and the basic stats for each collection.
 * @author Christopher Tanudjaja
 */

public class Album implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The name of the album. */
    private String name;

    /** The list of photos in this album. */
    private ArrayList<Photo> photos;

    /**
     * Creates a new, empty album.
     * I initialized the photo list as an ArrayList here so we have
     * a flexible container that can grow as the user adds more images.
     * @param name the unique name to assign to this album
     */
    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
    }

    /**
     * Adds a photo to this album's collection.
     * This is usually called from the AlbumViewController after the
     * user selects a file from their computer.
     * @param photo the {@link Photo} object to be added
     */
    public void addPhoto(Photo photo) { photos.add(photo); }

    /**
     *Returns the display name of the album.
     *@return the string name of the album
     */
    public String getAlbumName() { return name; }

    /**
     *Returns the list of all Photo objects stored in this album.
     *@return an {@link ArrayList} of photos in this album
     */
    public ArrayList<Photo> getPhotos() { return photos; }

    /**
     * Returns the total number of photos in the album.
     * I use this to populate the 'Count' column in the Album List view,
     * making it easy for the user to see which albums are empty or full.
     *@return the integer count of photos
     */
    public int getPhotoCount() { return photos.size(); }
    // Add to Album.java
    public void setName(String name) { this.name = name; }
}
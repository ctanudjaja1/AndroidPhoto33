package com.example.photos33.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents a single metadata tag attached to a photo.
 * I designed it to be a simple key-value pair (like 'location' = 'New York').
 * It's Serializable so that once a user labels their photos, those labels
 * actually stick around after the app closes.
 * @author Christopher Tanudjaja
 */
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The category of the tag. */
    private String tagType;
    /** The value of the tag. */
    private String value;

    /**
     * Creates a new tag instance.
     * I kept this simple, but I rely on the logic in the Photo and User
     * classes to make sure we aren't creating a mess of duplicate tags.
     *
     * @param tagType the category of the tag
     * @param value the specific information for the tag
     */
    public Tag(String tagType, String value) {
        this.tagType = tagType;
        this.value = value;
    }

    /**
     * Returns the type/category of this tag.
     * @return the tag type string
     */
    public String getTagType() { return tagType; }

    /**
     * Returns the value of this tag.
     * @return the tag value string
     */
    public String getValue() { return value; }

    /**
     * Checks if two tags are effectively the same.
     * I made this case-insensitive for both the type and the value.
     * This way, if someone tags a photo with "Person=Bob", they can't
     * accidentally add "person=bob" as a separate tag on the same image.
     *
     * @param o the object to compare against
     * @return true if the object is a Tag with matching type and value (case-insensitive)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Tag) {
            Tag other = (Tag) o;
            // A tag is the same if BOTH type and value match (case-insensitive is usually best)
            return this.tagType.equalsIgnoreCase(other.tagType) &&
                    this.value.equalsIgnoreCase(other.value);
        }
        return false;
    }

    /**
     * Generates a hash code for the tag.
     * Overridden to maintain consistency with the case-insensitive equals method.
     * @return the integer hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(tagType.toLowerCase(), value.toLowerCase());
    }

    /**
     * Formats the tag for the UI.
     * This is what the user will see in the photo info area or the
     * search results list, so I made it look clean with a colon separator.
     * @return a formatted string representation of the tag
     */
    @Override
    public String toString() {
        return tagType + ": " + value;
    }
}

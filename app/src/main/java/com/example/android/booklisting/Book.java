package com.example.android.booklisting;

/**
 * An {@link Book} object contains information related to a single book.
 */

public class Book {

    //Title of the book
    private String mTitle;

    /**
     * Author of the book
     */
    private String mAuthor;

    /**
     * URL of the book
     */
    private String mUrl;

    /**
     * Date of the book
     */
    private String mDate;

    /**
     * Constructs a new {@link Book} object.
     *
     * @param title  is the title of the book
     * @param author is the author of the book
     * @param url    is the URL of the book
     */
    public Book(String title, String author, String url, String date) {
        mTitle = title;
        mAuthor = author;
        mUrl = url;
        mDate = date;
    }

    /**
     * Returns the title of the book
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns the author of the book
     */
    public String getAuthor() {
        return mAuthor;
    }

    /**
     * Returns the URL of the book.
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Returns the date of the book.
     */
    public String getDate() {
        return mDate;
    }
}
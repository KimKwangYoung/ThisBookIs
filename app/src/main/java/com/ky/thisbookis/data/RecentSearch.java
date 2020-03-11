package com.ky.thisbookis.data;

import androidx.annotation.Keep;

@Keep
public class RecentSearch {

    String ISBN;
    String title;
    String authors;
    String addTime;
    String thumbnail;

    public RecentSearch() {
    }

    public RecentSearch(String ISBN, String title, String authors, String addTime, String thumbnail) {
        this.ISBN = ISBN;
        this.title = title;
        this.authors = authors;
        this.addTime = addTime;
        this.thumbnail = thumbnail;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}

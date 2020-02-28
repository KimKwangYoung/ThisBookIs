package com.example.thisbookis.data;

import androidx.annotation.Keep;

@Keep
public class MyBook implements Comparable<MyBook>{
    private String isbn;
    private String thumbnail;
    private String title;
    private String authors;
    private String registrationTime;

    public String getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(String registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
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

    @Override
    public int compareTo(MyBook myBook) {
        return -(this.registrationTime.compareTo(myBook.getRegistrationTime()));
    }
}

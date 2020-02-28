package com.example.thisbookis.data;

import androidx.annotation.Keep;

import java.util.LinkedHashMap;
import java.util.Map;

@Keep
public class Book {
    String title;
    String isbn;
    String thumbnail;
    String url;
    LinkedHashMap<String, String> readUsers;
    LinkedHashMap<String, Report> reportsOfBook;


    public Book() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public LinkedHashMap<String, String> getReadUsers() {
        return readUsers;
    }

    public void setReadUsers(Map<String, String> readUsers) {
        this.readUsers = new LinkedHashMap<>(readUsers);
    }

    public LinkedHashMap<String, Report> getReportsOfBook() {
        return reportsOfBook;
    }

    public void setReportsOfBook(Map<String, Report> reportsOfBook) {
        this.reportsOfBook = new LinkedHashMap<>(reportsOfBook);
    }
}

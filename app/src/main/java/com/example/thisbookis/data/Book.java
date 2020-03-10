package com.example.thisbookis.data;

import androidx.annotation.Keep;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Keep
public class Book {
   private String title;
   private String isbn;
   private String thumbnail;
   private String url;
   private String authors;
   private Map<String, Boolean> readUsers = new HashMap<>();
   private LinkedHashMap<String, Report> reportsOfBook = new LinkedHashMap<>();
   private int readUserCount = 0;

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

    public Map<String, Boolean> getReadUsers() {
        return readUsers;
    }

    public void setReadUsers(Map<String, Boolean> readUsers) {
        this.readUsers = readUsers;
    }

    public int getReadUserCount() {
        return readUserCount;
    }

    public void setReadUserCount(int readUserCount) {
        this.readUserCount = readUserCount;
    }

    public LinkedHashMap<String, Report> getReportsOfBook() {
        return reportsOfBook;
    }

    public void setReportsOfBook(Map<String, Report> reportsOfBook) {
        this.reportsOfBook = new LinkedHashMap<>(reportsOfBook);
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }
}

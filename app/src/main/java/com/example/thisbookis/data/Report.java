package com.example.thisbookis.data;

import androidx.annotation.Keep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Keep
public class Report implements Serializable{
    private String title;
    private String contents;
    private boolean shouldShare;
    private String bookISBN;
    private String bookTitle;
    private String bookAuthors;
    private String bookThumbnail;
    private String writer;
    private String writeTime;
    private String reportKey;
    private LinkedHashMap<String, Comment> comments;
    private LinkedHashMap<String, String> likes;

    public Report(){

    }

    public LinkedHashMap<String, Comment> getComments() {
        return comments;
    }

    public void setComments(Map<String, Comment> comments) {
        this.comments = new LinkedHashMap<>(comments);
    }

    public LinkedHashMap<String, String> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, String> likes) {
        this.likes = new LinkedHashMap<>(likes);
    }

    public String getBookThumbnail() {
        return bookThumbnail;
    }

    public void setBookThumbnail(String bookThumbnail) {
        this.bookThumbnail = bookThumbnail;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthors() {
        return bookAuthors;
    }

    public void setBookAuthors(String bookAuthors) {
        this.bookAuthors = bookAuthors;
    }

    public String getReportKey() {
        return reportKey;
    }

    public void setReportKey(String reportKey) {
        this.reportKey = reportKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public boolean isShouldShare() {
        return shouldShare;
    }

    public void setShouldShare(boolean shouldShare) {
        this.shouldShare = shouldShare;
    }

    public String getBookISBN() {
        return bookISBN;
    }

    public void setBookISBN(String bookISBN) {
        this.bookISBN = bookISBN;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getWriteTime() {
        return writeTime;
    }

    public void setWriteTime(String writeTime) {
        this.writeTime = writeTime;
    }
}

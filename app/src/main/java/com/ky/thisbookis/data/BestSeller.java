package com.ky.thisbookis.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BestSeller {
    @SerializedName("item")
    ArrayList<Item> items;

    public class Item{
        @SerializedName("title")
        String title;
        @SerializedName("isbn")
        String isbn;
        @SerializedName("coverSmallUrl")
        String thumbnail;
        @SerializedName("author")
        String author;
        @SerializedName("publisher")
        String publisher;

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
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
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }
}

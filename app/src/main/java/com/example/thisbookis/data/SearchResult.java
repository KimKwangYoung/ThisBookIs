package com.example.thisbookis.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SearchResult {

    @SerializedName("meta")
    public Meta meta;
    @SerializedName("documents")
    private List<Document> documents;

    public class Meta{
        @SerializedName("total_count") Integer total_count;
        @SerializedName("pageable_count") Integer pageable_count;
        @SerializedName("is_end") Boolean is_end;

        public Integer getTotal_count() {
            return total_count;
        }

        public void setTotal_count(Integer total_count) {
            this.total_count = total_count;
        }

        public Integer getPageable_count() {
            return pageable_count;
        }

        public void setPageable_count(Integer pageable_count) {
            this.pageable_count = pageable_count;
        }

        public Boolean getIs_end() {
            return is_end;
        }

        public void setIs_end(Boolean is_end) {
            this.is_end = is_end;
        }
    }
    public class Document implements Serializable {
        @SerializedName("title") private String title;
        @SerializedName("thumbnail") private String thumbnail;
        @SerializedName("price") private Integer price;
        @SerializedName("publisher") private String publisher;
        @SerializedName("authors") private String[] authors;
        @SerializedName("isbn") private String isbn;
        @SerializedName("contents") private String contents;
        @SerializedName("url") private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        public String getContents() {
            return contents;
        }

        public void setContents(String contents) {
            this.contents = contents;
        }

        public String[] getAuthors() {
            return authors;
        }

        public void setAuthors(String[] authors) {
            this.authors = authors;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
            this.price = price;
        }
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}

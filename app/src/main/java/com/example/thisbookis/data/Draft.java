package com.example.thisbookis.data;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class Draft implements Serializable {
   private String title;
   private String content;
   private Boolean shouldShare;
   private String saveTime;
   private String bookISBN;
   private String bookTitle;
   private String draftKey;

    public Draft(String title, String content, Boolean shouldShare, String saveTime, String bookISBN, String bookTitle) {
        this.title = title;
        this.content = content;
        this.shouldShare = shouldShare;
        this.saveTime = saveTime;
        this.bookISBN = bookISBN;
        this.bookTitle = bookTitle;
    }

    public String getDraftKey() {
        return draftKey;
    }

    public void setDraftKey(String draftKey) {
        this.draftKey = draftKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getShouldShare() {
        return shouldShare;
    }

    public void setShouldShare(Boolean shouldShare) {
        this.shouldShare = shouldShare;
    }

    public String getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(String saveTime) {
        this.saveTime = saveTime;
    }
}

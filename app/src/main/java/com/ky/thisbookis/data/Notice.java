package com.ky.thisbookis.data;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class Notice implements Serializable {
    String title;
    String content;
    String time;

    public Notice() {
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

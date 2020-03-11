package com.ky.thisbookis.data;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class Comment implements Comparable<Comment>, Serializable{
    private String writer;
    private String content;
    private String timeAddComments;

    public Comment() {
    }

    public String getTimeAddComments() {
        return timeAddComments;
    }

    public void setTimeAddComments(String timeAddComments) {
        this.timeAddComments = timeAddComments;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int compareTo(Comment comment) {
        return 0;
    }
}

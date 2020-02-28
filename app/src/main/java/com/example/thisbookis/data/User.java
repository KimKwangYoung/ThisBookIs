package com.example.thisbookis.data;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Keep
public class User {
    String userId;
    String nickname;
    String profileURL;
    String profilePath;
    boolean shouldShareReport;
    LinkedHashMap<String, Report> reports = new LinkedHashMap<>();
    LinkedHashMap<String, MyBook> myBooks = new LinkedHashMap<>();
    LinkedHashMap<String, Draft> temporaryStroages = new LinkedHashMap<>();

    public User() {
    }

    public LinkedHashMap<String, Draft> getTemporaryStroages() {
        return temporaryStroages;
    }

    public void setTemporaryStroages(LinkedHashMap<String, Draft> temporaryStroages) {
        this.temporaryStroages = temporaryStroages;
    }

    public LinkedHashMap<String, MyBook> getMyBooks() {
        return myBooks;
    }

    public void setMyBooks(Map<String, MyBook> myBooks) {
        this.myBooks = new LinkedHashMap<>(myBooks);
    }

    public LinkedHashMap<String, Report> getReports() {
        return reports;
    }

    public void setReports(Map<String, Report> reports) {
        this.reports = new LinkedHashMap<>(reports);
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfileURL() {
        return profileURL;
    }

    public void setProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }

    public boolean isShouldShareReport() {
        return shouldShareReport;
    }

    public void setShouldShareReport(boolean shouldShareReport) {
        this.shouldShareReport = shouldShareReport;
    }
}

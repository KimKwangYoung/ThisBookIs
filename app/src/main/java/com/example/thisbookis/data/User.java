package com.example.thisbookis.data;

import androidx.annotation.Keep;

import java.util.LinkedHashMap;
import java.util.Map;

@Keep
public class User {
    private String userId;
    private String nickname;
    private String profileURL;
    private String profilePath;
    private boolean shouldShareReport;
    private LinkedHashMap<String, Report> reports = new LinkedHashMap<>();
    private LinkedHashMap<String, MyBook> myBooks = new LinkedHashMap<>();
    private LinkedHashMap<String, Draft> temporaryStorages = new LinkedHashMap<>();
    private LinkedHashMap<String, RecentSearch> recentSearches = new LinkedHashMap<>();

    public User() {
    }

    public LinkedHashMap<String, RecentSearch> getRecentSearches() {
        return recentSearches;
    }

    public void setRecentSearches(Map<String, RecentSearch> recentSearches) {
        this.recentSearches = new LinkedHashMap<>(recentSearches);
    }

    public LinkedHashMap<String, Draft> getTemporaryStorages() {
        return temporaryStorages;
    }

    public void setTemporaryStorages(Map<String, Draft> temporaryStorages) {
        this.temporaryStorages = new LinkedHashMap<>(temporaryStorages);
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

package com.notehub.module.scheduler.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * 事件实体，对应 events.json 中的每一条记录。
 */
public class Event {

    private String id;
    private String name;
    private LocalDate date;

    @JsonProperty("is_annual")
    private Boolean isAnnual;

    @JsonProperty("enable_count_up")
    private Boolean enableCountUp;

    @JsonProperty("enable_countdown")
    private Boolean enableCountdown;

    @JsonProperty("enable_notification")
    private Boolean enableNotification;

    @JsonProperty("notify_advance_days")
    private List<Integer> notifyAdvanceDays;

    private String category;
    private List<String> tags;
    private String color;
    private Boolean pinned;

    @JsonProperty("show_in_timeline")
    private Boolean showInTimeline;

    private String story;
    private String location;
    private String mood;
    private List<String> photos;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Boolean getIsAnnual() {
        return isAnnual;
    }

    public void setIsAnnual(Boolean annual) {
        isAnnual = annual;
    }

    public Boolean getEnableCountUp() {
        return enableCountUp;
    }

    public void setEnableCountUp(Boolean enableCountUp) {
        this.enableCountUp = enableCountUp;
    }

    public Boolean getEnableCountdown() {
        return enableCountdown;
    }

    public void setEnableCountdown(Boolean enableCountdown) {
        this.enableCountdown = enableCountdown;
    }

    public Boolean getEnableNotification() {
        return enableNotification;
    }

    public void setEnableNotification(Boolean enableNotification) {
        this.enableNotification = enableNotification;
    }

    public List<Integer> getNotifyAdvanceDays() {
        return notifyAdvanceDays;
    }

    public void setNotifyAdvanceDays(List<Integer> notifyAdvanceDays) {
        this.notifyAdvanceDays = notifyAdvanceDays;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public Boolean getShowInTimeline() {
        return showInTimeline;
    }

    public void setShowInTimeline(Boolean showInTimeline) {
        this.showInTimeline = showInTimeline;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }
}

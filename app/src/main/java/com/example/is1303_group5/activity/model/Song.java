package com.example.is1303_group5.activity.model;

public class Song {
    private long id;
    private String title;
    private int duration;
    private String path;

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", duration=" + duration +
                ", path='" + path + '\'' +
                '}';
    }

    public Song(long id, String title, int duration, String path) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

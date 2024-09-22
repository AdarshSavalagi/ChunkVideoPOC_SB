package com.devappsys.videouploader.models;

public class Survey {
    private String id;
    private String videoId;
    private int chunkCount;

    public int getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    public Survey(String id, String videoId, int chunkCount) {
        this.id = id;
        this.videoId = videoId;
        this.chunkCount = chunkCount;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}

package com.devappsys.videouploader.DTO;

public class VerifyDTO {
    private String videoId;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public VerifyDTO(String videoId) {
        this.videoId = videoId;
    }
}

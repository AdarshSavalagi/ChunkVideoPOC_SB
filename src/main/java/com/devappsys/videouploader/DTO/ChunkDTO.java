package com.devappsys.videouploader.DTO;

import org.springframework.web.multipart.MultipartFile;

public class ChunkDTO {
    private String videoId;
    private int SequenceNumber;
    private MultipartFile file;

    public ChunkDTO(String videoId, MultipartFile file, int sequenceNumber) {
        this.videoId = videoId;
        this.file = file;
        SequenceNumber = sequenceNumber;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public int getSequenceNumber() {
        return SequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        SequenceNumber = sequenceNumber;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}

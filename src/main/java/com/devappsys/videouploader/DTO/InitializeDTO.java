package com.devappsys.videouploader.DTO;

public class InitializeDTO {
    private String surveyId;
    private int chunkCount;


    public String getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    public InitializeDTO(String surveyId, int chunkCount) {
        this.surveyId = surveyId;
        this.chunkCount = chunkCount;
    }
}

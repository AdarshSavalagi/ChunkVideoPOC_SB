package com.devappsys.videouploader.Service;


import com.devappsys.videouploader.models.Survey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class SurveyService {
    ArrayList<Survey> surveys;

    public SurveyService() {
        surveys = new ArrayList<>();
    }

    public Survey createNewSurvey(String surveyId,String videoId,int totalChunks) {
        Survey survey = new Survey(surveyId,videoId,totalChunks);
        surveys.add(survey);
        return survey;
    }


    public int getChunksCount(String videoId) {
        System.out.println("survey count: "+surveys.size());
        for (Survey survey : surveys) {
            if (survey.getVideoId().equals(videoId)) {
                return survey.getChunkCount();
            }
        }
        return -1;
    }
}

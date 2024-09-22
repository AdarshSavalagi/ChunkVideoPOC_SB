package com.devappsys.videouploader.Controller;


import com.devappsys.videouploader.DTO.ChunkDTO;
import com.devappsys.videouploader.DTO.InitializeDTO;
import com.devappsys.videouploader.DTO.VerifyDTO;
import com.devappsys.videouploader.Service.SurveyService;
import com.devappsys.videouploader.Service.UploadService;
import com.devappsys.videouploader.models.Survey;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
public class ChunkHandlerController {


    SurveyService surveyService;
    UploadService uploadService;

    public ChunkHandlerController(SurveyService surveyService, UploadService uploadService) {
        this.surveyService = surveyService;
        this.uploadService = uploadService;
    }

    @PostMapping("/init")
    public ResponseEntity<Survey> uploadInit(@RequestBody InitializeDTO body){
        System.out.println("upload initialized....."+body.getChunkCount());
        Map<String,String> map = new HashMap<>();
        try{
            String videoId = uploadService.initiateUpload();
            Survey survey = surveyService.createNewSurvey(body.getSurveyId(),videoId, body.getChunkCount());
            return ResponseEntity.ok(survey);
        }catch(Exception e){
            map.put("status","error");
            map.put("message",e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }


    @PostMapping("chunk")
    public ResponseEntity<Map<String,String>> uploadChunk(@ModelAttribute ChunkDTO body){
        System.out.println("upload chunk requested : sequence number: "+body.getSequenceNumber()+" : video id : "+body.getVideoId());
        Map<String,String> map = new HashMap<>();
        try{
            uploadService.saveChunk(body.getVideoId(), body.getSequenceNumber(), body.getFile());
            map.put("status","success");
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            map.put("status","error");
            map.put("message",e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(map);
        }
    }

    @PostMapping("verify")
    public ResponseEntity<Map<String,Object>> verifyChunk(@RequestBody Map<String,String> body){
        System.out.println("verify chunk requested"+body.get("videoId"));
        Map<String,Object> map = new HashMap<>();
        try {
            String videoId = body.get("videoId");
             if (uploadService.isUploadComplete(videoId)) {
                 map.put("status","success");
                 map.put("ChunkCount",surveyService.getChunksCount(videoId));
                 return ResponseEntity.ok(map);
             }else{
                 map.put("status","pending");
                 map.put("ChunkCount",surveyService.getChunksCount(videoId));
                 map.put("pending",uploadService.getPendingChunks(videoId));
                 return ResponseEntity.ok(map);
             }
        }catch (Exception e){
            map.put("status","error");
            map.put("message",e.getMessage());
            return ResponseEntity.internalServerError().body(map);
        }
    }
}

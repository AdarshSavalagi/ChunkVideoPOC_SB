package com.devappsys.videouploader.Controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {
    @GetMapping("test")
    public ResponseEntity<Map<String,String>> test(){
            Map<String, String> map = new HashMap<>();
        try {
            map.put("message","working");
            return ResponseEntity.ok(map);
        }catch (Exception e){
            map.put("key", "error");
            map.put("messsage", e.getMessage());
            return ResponseEntity.internalServerError().body(map);
        }
    }
}

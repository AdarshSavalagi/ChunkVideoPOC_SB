package com.devappsys.videouploader.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

@Service
public class UploadService {
    SurveyService surveyService;

    public UploadService(SurveyService surveyService) {
        this.surveyService = surveyService;
        try {
            Files.createDirectories(uploadBaseDir);
            Files.createDirectories(uploadChunkDir);
            Files.createDirectories(uploadVideoDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create base directories: " + e.getMessage());
        }
    }


    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Path uploadBaseDir = Paths.get("uploads");
    private final Path uploadChunkDir = uploadBaseDir.resolve("chunks");
    private final Path uploadVideoDir = uploadBaseDir.resolve("video");

    public String initiateUpload() {
        String uploadId = UUID.randomUUID().toString();
        try {
            Path uploadDir = uploadChunkDir.resolve(uploadId);
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + e.getMessage());
        }
        return uploadId;
    }

    public void saveChunk(String uploadId, int sequenceNumber, MultipartFile chunk) {
            uploadId = uploadId.replaceAll("[<>:\"/\\|?*]", "") ;
        System.out.println(uploadId);
        if (!"video/mp4".equals(chunk.getContentType())) {
            throw new RuntimeException("Invalid chunk format: Expected MP4");
        }

        Path chunkDir = uploadChunkDir.resolve(uploadId);
        try {
            Path chunkFile = chunkDir.resolve(sequenceNumber + ".mp4");
            Files.write(chunkFile, chunk.getBytes()); // Save the chunk
            if (isUploadComplete(uploadId)) {
                System.out.println("Upload complete.");
                String finalUploadId = uploadId;
                executorService.submit(() -> combineChunks(finalUploadId));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error saving chunk: " + e.getMessage());
        }
    }

    public boolean isUploadComplete(String uploadId) {
        Path chunkDir = uploadChunkDir.resolve(uploadId);
        try {
            long chunkCount = Files.list(chunkDir).count(); // Count the chunks
            System.out.println("chunk count: " + chunkCount);
            System.out.println("Expected count: "+ expectedChunkCount(uploadId));
            return chunkCount >= expectedChunkCount(uploadId); // Implement a method to track the expected chunk count
        } catch (IOException e) {
            throw new RuntimeException("Could not list chunks: " + e.getMessage());
        }
    }




    private void combineChunks(String uploadId) {
        Path chunkDir = uploadChunkDir.resolve(uploadId);
        Path finalVideo = uploadVideoDir.resolve(uploadId + ".mp4"); // Final combined file path
        Path concatFile = chunkDir.resolve("concat.txt"); // File for FFmpeg to list the chunks

        try {
            // Append each chunk file path to the concat.txt as the chunks are uploaded
            Files.list(chunkDir)
                    .filter(p -> !p.getFileName().toString().equals("concat.txt")) // Ignore existing concat.txt
                    .sorted(Comparator.comparing(p -> Integer.parseInt(p.getFileName().toString().split("\\.")[0])))
                    .forEach(chunk -> {
                        String chunkEntry = "file '" + chunk.toAbsolutePath().toString() + "'";
                        try {
                            // Append to concat.txt file
                            Files.writeString(concatFile, chunkEntry + System.lineSeparator(),
                                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        } catch (IOException e) {
                            throw new RuntimeException("Error writing to concat.txt: " + e.getMessage());
                        }
                    });

            // FFmpeg command to concatenate the chunks
            String command = String.format("ffmpeg -f concat -safe 0 -i %s -c copy -progress progress.txt %s",
                    concatFile.toAbsolutePath(),
                    finalVideo.toAbsolutePath());

            System.out.println("command: " + command);

            // Execute the FFmpeg command in Windows
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Capture the FFmpeg output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Successfully combined all chunks for uploadId: " + uploadId);
            } else {
                throw new RuntimeException("FFmpeg failed with exit code " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error combining chunks using FFmpeg: " + e.getMessage());
        }
    }




    public ArrayList<Integer> getPendingChunks(String uploadId) {
        ArrayList<Integer> pendingChunks = new ArrayList<>();
        Path chunkDir = uploadChunkDir.resolve(uploadId);

        try {
            // Get the expected chunk count
            int expectedCount = expectedChunkCount(uploadId);

            // Get the existing chunks in the directory
            Set<Integer> existingChunks = Files.list(chunkDir)
                    .filter(p -> p.toFile().isFile()) // Only consider files
                    .map(p -> Integer.parseInt(p.getFileName().toString().split("\\.")[0])) // Get sequence number
                    .collect(Collectors.toSet());

            // Check for missing chunks
            for (int i = 0; i < expectedCount; i++) {
                if (!existingChunks.contains(i)) {
                    pendingChunks.add(i); // Add missing chunk index
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving pending chunks: " + e.getMessage());
        }

        return pendingChunks; // Return the list of pending chunks
    }




    private void cleanupChunks(Path chunkDir) {
        try {
            Files.walk(chunkDir)
                    .sorted(Comparator.reverseOrder()) // Ensure directory is deleted last
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException("Error cleaning up chunks: " + e.getMessage());
        }
    }

    // Implement a method to determine the expected chunk count
    private int expectedChunkCount(String uploadId) {
        return surveyService.getChunksCount(uploadId);
    }
}

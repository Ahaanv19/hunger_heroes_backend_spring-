package com.open.spring.mvc.backups;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class SpecificBackupsController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${backup.base.path:./backups}")
    private String backupBasePath;

    @Value("${backup.max.files:3}")
    private int maxBackupFiles;

    @Value("${server.port:8080}")
    private String serverPort;

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // Configuration for API endpoints and their corresponding directories
    private final List<BackupEndpoint> endpoints = Arrays.asList(
        new BackupEndpoint("/api/people/bulk/extract", "person"),
        new BackupEndpoint("/api/groups/bulk/extract", "groups"),
        new BackupEndpoint("/api/tinkle/bulk/extract", "tinkle"),
        new BackupEndpoint("/api/calendar/events", "calendar"),
        new BackupEndpoint("/bank/bulk/extract", "bank")
    );

    @EventListener
    public void handleContextClose(ContextClosedEvent event) {
        for (BackupEndpoint endpoint : endpoints) {
            try {
                backupEndpoint(endpoint);
            } catch (Exception e) {
                System.err.println("Failed to backup endpoint " + endpoint.getPath() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("Backup process completed.");
    }

    private void backupEndpoint(BackupEndpoint endpoint) throws IOException {
        String url = "http://localhost:" + serverPort + endpoint.getPath();
        
        try {
            // Make API call
            System.out.println("Calling API: " + url);
            String jsonResponse = restTemplate.getForObject(url, String.class);
            
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                System.out.println("Empty response from " + url + ", skipping backup");
                return;
            }

            // Validate JSON
            objectMapper.readTree(jsonResponse);
            
            // Create directory structure
            Path backupDir = createBackupDirectory(endpoint.getDirectoryName());
            
            // Generate filename with timestamp
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = endpoint.getDirectoryName() + "_backup_" + timestamp + ".json";
            Path filePath = backupDir.resolve(filename);
            
            // Write JSON to file
            Files.write(filePath, jsonResponse.getBytes());
            
            // Manage file rotation
            manageFileRotation(backupDir, endpoint.getDirectoryName());
            
        } catch (Exception e) {
            System.err.println("Error during backup of " + endpoint.getPath() + ": " + e.getMessage());
            throw new RuntimeException("Backup failed for " + endpoint.getPath(), e);
        }
    }

    private Path createBackupDirectory(String subdirectory) throws IOException {
        Path backupPath = Paths.get(backupBasePath, subdirectory);
        
        if (!Files.exists(backupPath)) {
            Files.createDirectories(backupPath);
            System.out.println("Created backup directory: " + backupPath.toString());
        }
        
        return backupPath;
    }

    private void manageFileRotation(Path directory, String prefix) throws IOException {
        File[] files = directory.toFile().listFiles((dir, name) -> 
            name.startsWith(prefix + "_backup_") && name.endsWith(".json"));
        
        if (files == null) return;
        
        if (files.length > maxBackupFiles) {
            // Sort files by last modified time (oldest first)
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            
            // Delete oldest files to maintain the limit
            int filesToDelete = files.length - maxBackupFiles;
            for (int i = 0; i < filesToDelete; i++) {
                boolean deleted = files[i].delete();
                if (! deleted) {
                    System.err.println("Failed to delete old backup: " + files[i].getName());
                }
            }
        }
    }

    // Inner class to hold endpoint configuration
    private static class BackupEndpoint {
        private final String path;
        private final String directoryName;

        public BackupEndpoint(String path, String directoryName) {
            this.path = path;
            this.directoryName = directoryName;
        }

        public String getPath() {
            return path;
        }

        public String getDirectoryName() {
            return directoryName;
        }
    }
}

// Configuration class for RestTemplate bean
@Component
class BackupConfiguration {
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add timeout configuration
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setConnectionRequestTimeout(10000);
        restTemplate.setRequestFactory(factory);
        
        return restTemplate;
    }
}
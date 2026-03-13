package com.open.spring.mvc.backups;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/api/backup-imports")
@Component
public class SpecificImportsController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${backup.base.path:./backups}")
    private String backupBasePath;

    @Value("${server.port:8585}")
    private String serverPort;

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // Configuration for import endpoints and their corresponding directories
    private final List<ImportEndpoint> endpoints = Arrays.asList(
        new ImportEndpoint("/api/people/bulk/import", "person"),
        new ImportEndpoint("/api/groups/bulk/create", "groups"),
        new ImportEndpoint("/api/tinkle/bulk/create", "tinkle"),
        new ImportEndpoint("/api/calendar/add_bulk", "calendar"),
        new ImportEndpoint("/bank/bulk/create", "bank")
    );

    /**
     * Import data from the most recent backup file for all endpoints
     */
    @PostMapping("/import-all-latest")
    public ResponseEntity<Map<String, Object>> importAllFromLatestBackups() {
        System.out.println("Starting import process from latest backups...");
        
        Map<String, Object> results = new HashMap<>();
        List<String> successes = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        
        for (ImportEndpoint endpoint : endpoints) {
            try {
                importFromLatestBackup(endpoint);
                successes.add(endpoint.getDirectoryName());
            } catch (Exception e) {
                System.err.println("Failed to import to endpoint " + endpoint.getPath() + ": " + e.getMessage());
                failures.add(endpoint.getDirectoryName() + ": " + e.getMessage());
            }
        }
        
        results.put("successes", successes);
        results.put("failures", failures);
        results.put("message", "Import process completed");
        
        System.out.println("Import process completed.");
        return ResponseEntity.ok(results);
    }

    /**
     * Import data from a specific backup file
     */
    @PostMapping("/import-specific")
    public ResponseEntity<Map<String, Object>> importFromSpecificFile(
            @RequestParam String directoryName, 
            @RequestParam String filename) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            ImportEndpoint endpoint = findEndpointByDirectory(directoryName);
            if (endpoint == null) {
                result.put("error", "No endpoint configured for directory: " + directoryName);
                return ResponseEntity.badRequest().body(result);
            }

            Path filePath = Paths.get(backupBasePath, directoryName, filename);
            if (!Files.exists(filePath)) {
                result.put("error", "Backup file not found: " + filePath);
                return ResponseEntity.badRequest().body(result);
            }

            importFromFile(endpoint, filePath);
            result.put("success", true);
            result.put("message", "Successfully imported from " + filename);
            result.put("directory", directoryName);
            result.put("filename", filename);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("error", "Import failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * List available backup files for a specific directory
     */
    @GetMapping("/list-backups/{directoryName}")
    public ResponseEntity<Map<String, Object>> listBackupFiles(@PathVariable String directoryName) {
        try {
            List<BackupFileInfo> backupFiles = getBackupFilesWithInfo(directoryName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("directory", directoryName);
            result.put("files", backupFiles);
            result.put("count", backupFiles.size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "Failed to list backup files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * List all available backup directories and their file counts
     */
    @GetMapping("/list-all-backups")
    public ResponseEntity<Map<String, Object>> listAllBackups() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> directories = new HashMap<>();
        
        for (ImportEndpoint endpoint : endpoints) {
            try {
                List<BackupFileInfo> files = getBackupFilesWithInfo(endpoint.getDirectoryName());
                Map<String, Object> dirInfo = new HashMap<>();
                dirInfo.put("fileCount", files.size());
                dirInfo.put("endpoint", endpoint.getPath());
                dirInfo.put("files", files);
                directories.put(endpoint.getDirectoryName(), dirInfo);
            } catch (Exception e) {
                Map<String, Object> dirInfo = new HashMap<>();
                dirInfo.put("error", e.getMessage());
                dirInfo.put("endpoint", endpoint.getPath());
                directories.put(endpoint.getDirectoryName(), dirInfo);
            }
        }
        
        result.put("directories", directories);
        result.put("totalDirectories", endpoints.size());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get all configured import endpoints
     */
    @GetMapping("/endpoints")
    public ResponseEntity<List<ImportEndpoint>> getConfiguredEndpoints() {
        return ResponseEntity.ok(endpoints);
    }

    /**
     * Validate that all import endpoints are accessible
     */
    @GetMapping("/validate-endpoints")
    public ResponseEntity<Map<String, Object>> validateEndpoints() {
        System.out.println("Validating import endpoints...");
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> endpointStatuses = new ArrayList<>();
        
        for (ImportEndpoint endpoint : endpoints) {
            String url = "http://localhost:" + serverPort + endpoint.getPath();
            Map<String, Object> status = new HashMap<>();
            status.put("endpoint", endpoint.getPath());
            status.put("directory", endpoint.getDirectoryName());
            
            try {
                // Try to make a HEAD request to check if endpoint exists
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Validation", "endpoint-check");
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                ResponseEntity<String> response = restTemplate.exchange(
                    url, 
                    HttpMethod.HEAD, 
                    entity, 
                    String.class
                );
                
                status.put("accessible", true);
                status.put("statusCode", response.getStatusCodeValue());
                System.out.println("✓ Endpoint " + endpoint.getPath() + " is accessible");
                
            } catch (Exception e) {
                status.put("accessible", false);
                status.put("error", e.getMessage());
                System.err.println("✗ Endpoint " + endpoint.getPath() + " is not accessible: " + e.getMessage());
            }
            
            endpointStatuses.add(status);
        }
        
        result.put("endpoints", endpointStatuses);
        result.put("totalEndpoints", endpoints.size());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public String backupManagementPage() {
        return "db_management/specific_backups"; // This will resolve to backup-management.html in templates folder
    }
    
    /**
     * Alternative endpoint for backward compatibility
     */
    @GetMapping("/database")
    public String databaseBackupPage() {
        return "backup-management";
    }

    // Private helper methods
    
    private List<BackupFileInfo> getBackupFilesWithInfo(String directoryName) throws IOException {
        Path backupDir = Paths.get(backupBasePath, directoryName);
        
        if (!Files.exists(backupDir)) {
            throw new IllegalArgumentException("Backup directory not found: " + backupDir);
        }

        File[] files = backupDir.toFile().listFiles((dir, name) -> 
            name.startsWith(directoryName + "_backup_") && name.endsWith(".json"));
        
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }

        // Sort files by last modified time (newest first)
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        
        return Arrays.stream(files)
                .map(file -> new BackupFileInfo(
                    file.getName(),
                    file.lastModified(),
                    file.length(),
                    directoryName
                ))
                .collect(Collectors.toList());
    }

    private void importFromLatestBackup(ImportEndpoint endpoint) throws IOException {
        Path backupDir = Paths.get(backupBasePath, endpoint.getDirectoryName());
        
        if (!Files.exists(backupDir)) {
            System.out.println("No backup directory found for " + endpoint.getDirectoryName() + ", skipping import");
            return;
        }

        File[] files = backupDir.toFile().listFiles((dir, name) -> 
            name.startsWith(endpoint.getDirectoryName() + "_backup_") && name.endsWith(".json"));
        
        if (files == null || files.length == 0) {
            System.out.println("No backup files found for " + endpoint.getDirectoryName() + ", skipping import");
            return;
        }

        // Sort files by last modified time (newest first)
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        
        Path latestFile = files[0].toPath();
        importFromFile(endpoint, latestFile);
    }

    private void importFromFile(ImportEndpoint endpoint, Path filePath) throws IOException {
        String url = "http://localhost:" + serverPort + endpoint.getPath();
        
        try {
            // Read JSON from file
            System.out.println("Reading backup file: " + filePath.toString());
            String jsonContent = Files.readString(filePath);
            
            if (jsonContent == null || jsonContent.trim().isEmpty()) {
                System.out.println("Empty backup file " + filePath + ", skipping import");
                return;
            }

            // Validate JSON
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            // Prepare HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Import-Source", "backup-restore");
            headers.add("X-Import-Timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
            
            // Create HTTP entity
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonContent, headers);
            
            // Make API call
            System.out.println("Importing to API: " + url);
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Successfully imported data from " + filePath.getFileName() + 
                                 " to " + endpoint.getPath());
                System.out.println("Response: " + response.getBody());
            } else {
                System.err.println("Import failed with status: " + response.getStatusCode());
                System.err.println("Response: " + response.getBody());
            }
            
        } catch (Exception e) {
            System.err.println("Error during import from " + filePath + " to " + endpoint.getPath() + ": " + e.getMessage());
            throw new RuntimeException("Import failed for " + endpoint.getPath(), e);
        }
    }

    private ImportEndpoint findEndpointByDirectory(String directoryName) {
        return endpoints.stream()
                .filter(endpoint -> endpoint.getDirectoryName().equals(directoryName))
                .findFirst()
                .orElse(null);
    }

    // Inner classes and DTOs
    
    public static class ImportEndpoint {
        private final String path;
        private final String directoryName;

        public ImportEndpoint(String path, String directoryName) {
            this.path = path;
            this.directoryName = directoryName;
        }

        public String getPath() {
            return path;
        }

        public String getDirectoryName() {
            return directoryName;
        }

        @Override
        public String toString() {
            return "ImportEndpoint{" +
                    "path='" + path + '\'' +
                    ", directoryName='" + directoryName + '\'' +
                    '}';
        }
    }
    
    public static class BackupFileRequest {
        private String directoryName;
        private String filename;
        
        // Constructors
        public BackupFileRequest() {}
        
        public BackupFileRequest(String directoryName, String filename) {
            this.directoryName = directoryName;
            this.filename = filename;
        }
        
        // Getters and setters
        public String getDirectoryName() { return directoryName; }
        public void setDirectoryName(String directoryName) { this.directoryName = directoryName; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
    }
    
    public static class BackupFileInfo {
        private String filename;
        private long lastModified;
        private long size;
        private String directory;
        
        public BackupFileInfo(String filename, long lastModified, long size, String directory) {
            this.filename = filename;
            this.lastModified = lastModified;
            this.size = size;
            this.directory = directory;
        }
        
        // Getters
        public String getFilename() { return filename; }
        public long getLastModified() { return lastModified; }
        public long getSize() { return size; }
        public String getDirectory() { return directory; }
    }
}
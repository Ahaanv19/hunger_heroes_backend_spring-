package com.open.spring.mvc.hardAssets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/assets")
public class HardAssetsController {

    private static final String UPLOAD_DIR = "./uploads/";

    @Autowired
    private HardAssetsRepository repository;

    @GetMapping("/upload/{id}")
    public ResponseEntity<HardAsset> getAsset(@PathVariable Long id) {
        Optional<HardAsset> optional = repository.findById(id);
        if (optional.isPresent()) { // Good ID
            HardAsset asset = optional.get(); // value from findByID
            return new ResponseEntity<>(asset, HttpStatus.OK); // OK HTTP response: status code, headers, and body
        }
        // Bad ID
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/uploads")
    public ResponseEntity<List<HardAsset>> getUploads(@RequestParam(required = false) String uid, @AuthenticationPrincipal UserDetails userDetails) {
        String targetUid = uid;
        if (targetUid == null) {
            if (userDetails == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            targetUid = userDetails.getUsername();
        }
        
        List<HardAsset> assets = repository.findByOwnerUID(targetUid);
        return new ResponseEntity<>(assets, HttpStatus.OK);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> postUpload(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("POST request for /api/upload");
        if (file.isEmpty()) {
            return new ResponseEntity<>("Please select a file to upload.", HttpStatus.BAD_REQUEST);
        }
        try {
            System.out.println("UserDetails: " + userDetails);
            String uid = userDetails.getUsername();
            System.out.println("Extracted UID: " + uid);

            // Prevent path traversal attacks and create directory
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String originalFilename = file.getOriginalFilename();
            // Extract basename to strip any path components
            String baseName = Paths.get(originalFilename).getFileName().toString();
            
            // Reject filenames containing traversal patterns
            if (baseName.contains("..") || baseName.contains("/") || baseName.contains("\\")) {
                return new ResponseEntity<>("Invalid file name.", HttpStatus.BAD_REQUEST);
            }
            
            // Resolve and verify final path stays within uploads directory
            String localFileUUID = java.util.UUID.randomUUID().toString() + "_" + baseName;
            Path filePath = uploadPath.resolve(localFileUUID).normalize();
            
            if (!filePath.startsWith(uploadPath)) {
                return new ResponseEntity<>("Invalid target path.", HttpStatus.BAD_REQUEST);
            }
            
            Files.copy(file.getInputStream(), filePath);

            HardAsset newAsset = new HardAsset(originalFilename, localFileUUID, uid);
            repository.save(newAsset);
            System.out.println("File uploaded and saved to database: " + localFileUUID);
            return new ResponseEntity<>("Successfully uploaded and saved '" + localFileUUID + "'", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to upload file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to upload file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
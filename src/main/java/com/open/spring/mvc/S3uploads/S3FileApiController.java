package com.open.spring.mvc.S3uploads;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class S3FileApiController {

    @Autowired
    private FileHandler fileHandler;

    /**
     * Upload a file to S3
     * @param uid User ID
     * @param filename Name of the file
     * @param base64Data Base64 encoded file content
     * @return Response with filename or error
     */
    @PostMapping("/upload/{uid}")
    public ResponseEntity<?> uploadFile(
            @PathVariable String uid,
            @RequestParam String filename,
            @RequestBody String base64Data) {
        
        try {
            String result = fileHandler.uploadFile(base64Data, filename, uid);
            
            if (result != null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "File uploaded successfully");
                response.put("filename", result);
                response.put("uid", uid);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error uploading file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Download a file from S3
     * @param uid User ID
     * @param filename Name of the file to download
     * @return Base64 encoded file content
     */
    @GetMapping("/download/{uid}/{filename}")
    public ResponseEntity<?> downloadFile(
            @PathVariable String uid,
            @PathVariable String filename) {
        
        try {
            String base64Data = fileHandler.decodeFile(uid, filename);
            
            if (base64Data != null) {
                Map<String, String> response = new HashMap<>();
                response.put("filename", filename);
                response.put("uid", uid);
                response.put("base64Data", base64Data);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error downloading file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete all files for a user
     * @param uid User ID
     * @return Success or failure message
     */
    @DeleteMapping("/delete/{uid}")
    public ResponseEntity<?> deleteUserFiles(@PathVariable String uid) {
        try {
            boolean result = fileHandler.deleteFiles(uid);
            
            if (result) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Files deleted successfully");
                response.put("uid", uid);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Delete failed", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error deleting files: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}

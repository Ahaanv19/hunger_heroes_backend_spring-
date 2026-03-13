package com.open.spring.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DatabaseMigrator.java
 * 
 * Migrates database with optional data extraction from remote source
 * - Initializes Users, Sections, and other defined tables
 * - Can import data from old/remote database
 * 
 * Usage:
 * > scripts/db_migrate.sh
 * 
 * Or directly:
 * > java -cp ... com.open.spring.system.DatabaseMigrator [--import-remote]
 * 
 * Options:
 *   --import-remote : Fetch and import data from remote database
 * 
 * Process:
 * 0. Warning to the user
 * 1. Authenticate to remote database (if --import-remote)
 * 2. Extract old data via API (if --import-remote)
 * 3. Backup current database
 * 4. Drop all tables
 * 5. Recreate schema (via Spring Boot startup with ModelInit SKIPPED)
 * 6. Load remote data (if --import-remote)
 */
public class DatabaseMigrator {
    
    private static final String BACKUP_DIR = "volumes/backups/";
    private static final String PROPERTIES_FILE = "src/main/resources/application.properties";
    private static final String JSON_DATA_FILE = "volumes/data.json";
    private static final String SKIP_FLAG_FILE = "volumes/.skip-modelinit";
    
    // Remote database configuration
    // Note: The deployed server doesn't require authentication for /api/exports/getAll
    // This endpoint exports all tables from the remote database
    private static final String DATA_URL = "https://spring.opencodingsociety.com/api/exports/getAll";
    
    private static boolean importRemote = false;
    
    /**
     * Main method - runs as standalone application
     */
    public static void main(String[] args) {
        // Check for --import-remote flag
        for (String arg : args) {
            if (arg.equals("--import-remote")) {
                importRemote = true;
                break;
            }
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DATABASE MIGRATION" + (importRemote ? " WITH REMOTE IMPORT" : ""));
        System.out.println("=".repeat(60) + "\n");
        
        try {
            // Load database URL from properties
            String databaseUrl = loadDatabaseUrl();
            
            // Step 0: Warning and confirmation
            if (!getUserConfirmation(databaseUrl)) {
                System.out.println("Exiting without making changes.");
                System.exit(0);
            }
            
            // Step 1 & 2: Fetch remote data (if needed)
            String remoteData = null;
            if (importRemote) {
                System.out.println("\nFetching remote data...");
                remoteData = fetchRemoteData();
                if (remoteData != null) {
                    saveDataToJson(remoteData);
                    System.out.println("Remote data fetched and saved to " + JSON_DATA_FILE);
                } else {
                    System.out.println("WARNING: Could not fetch remote data, will use local JSON if available");
                    remoteData = loadLocalJson();
                }
            }
            
            // Step 3: Backup the database
            backupDatabase(databaseUrl);
            
            // Step 4: Drop all tables
            dropAllTables(databaseUrl);
            
            // Step 5: Recreate schema WITHOUT loading default data (skip ModelInit)
            System.out.println("\nStarting Spring Boot to recreate schema...");
            System.out.println("(ModelInit will be skipped to avoid conflicts with imported data)");
            System.out.println("(This will take a few seconds...)");
            
            // Create skip flag BEFORE starting Spring Boot
            createSkipModelInitFlag();
            
            try {
                recreateDatabase();
            } finally {
                // Always remove flag, even if recreation fails
                removeSkipModelInitFlag();
            }
            
            // Step 6: Load remote data if we have it
            if (importRemote && remoteData != null) {
                System.out.println("\nLoading remote data into new database...");
                loadRemoteData(remoteData);
            }
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("DATABASE MIGRATION COMPLETE");
            System.out.println("=".repeat(60) + "\n");
            
            if (importRemote && remoteData != null) {
                System.out.println("Database migrated with:");
                System.out.println("  Fresh schema (created by Hibernate)");
                System.out.println("  Remote data imported (ModelInit was skipped)");
            } else {
                System.out.println("Database migrated with:");
                System.out.println("  Fresh schema (created by Hibernate)");
                System.out.println("  No data loaded");
                System.out.println("\nNote: ModelInit was skipped. Start the application normally to load default data.");
            }
            
            System.out.println("\nYou can now start your application:");
            System.out.println("  ./mvnw spring-boot:run\n");
            
        } catch (Exception e) {
            // Clean up flag on error
            removeSkipModelInitFlag();
            System.err.println("\nAn error occurred during database migration:");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Create a flag file to tell ModelInit to skip initialization
     */
    private static void createSkipModelInitFlag() {
        try {
            File flagFile = new File(SKIP_FLAG_FILE);
            // Create parent directory if it doesn't exist
            File parentDir = flagFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            flagFile.createNewFile();
            System.out.println("Created skip-modelinit flag at " + SKIP_FLAG_FILE);
        } catch (IOException e) {
            System.err.println("Warning: Could not create skip flag: " + e.getMessage());
        }
    }
    
    /**
     * Remove the skip flag file
     */
    private static void removeSkipModelInitFlag() {
        File flagFile = new File(SKIP_FLAG_FILE);
        if (flagFile.exists()) {
            if (flagFile.delete()) {
                System.out.println("Removed skip-modelinit flag");
            } else {
                System.err.println("Warning: Could not remove skip flag file");
            }
        }
    }
    
    /**
     * Load database URL from application.properties
     */
    private static String loadDatabaseUrl() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
            props.load(fis);
        }
        
        String url = props.getProperty("spring.datasource.url");
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("Could not find spring.datasource.url in " + PROPERTIES_FILE);
        }
        
        return url;
    }
    
    /**
     * Check if database has existing tables and get user confirmation
     */
    private static boolean getUserConfirmation(String databaseUrl) {
        try {
            String dbPath = databaseUrl.replace("jdbc:sqlite:", "").split("\\?")[0];
            
            // Check if database file exists
            File dbFile = new File(dbPath);
            if (!dbFile.exists()) {
                System.out.println("No existing database found. Will create new database.");
                return true;
            }
            
            // Check if any tables exist
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                boolean hasTables = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"}).next();
                
                if (hasTables) {
                    System.out.println("WARNING: You are about to lose all data in the database!");
                    System.out.println("WARNING: This operation will:");
                    System.out.println("   - Backup the current database");
                    System.out.println("   - Drop all existing tables");
                    System.out.println("   - Recreate schema from scratch");
                    if (importRemote) {
                        System.out.println("   - Import data from remote database");
                    } else {
                        System.out.println("   - Leave database empty (run app normally to load defaults)");
                    }
                    System.out.println();
                    
                    // Check for FORCE_YES environment variable
                    String forceYes = System.getenv("FORCE_YES");
                    if ("true".equalsIgnoreCase(forceYes)) {
                        System.out.println("FORCE_YES detected, proceeding automatically...");
                        return true;
                    }
                    
                    System.out.print("Do you want to continue? (y/n): ");
                    try (Scanner scanner = new Scanner(System.in)) {
                        String response = scanner.nextLine().trim().toLowerCase();
                        return response.equals("y") || response.equals("yes");
                    }
                }
            }
            
            return true; // No tables, safe to proceed
            
        } catch (SQLException e) {
            System.err.println("Error checking database: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Fetch data from remote database
     * Note: Using /api/exports/getAll which doesn't require authentication
     */
    private static String fetchRemoteData() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // The /api/exports/getAll endpoint is public and doesn't require authentication
            System.out.println("  Fetching data from remote database...");
            System.out.println("  URL: " + DATA_URL);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                DATA_URL,
                HttpMethod.GET,
                request,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String data = response.getBody();
                System.out.println("  Data fetched successfully");
                
                // Show preview of what was fetched
                if (data != null && data.length() > 0) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<?, ?> dataMap = mapper.readValue(data, Map.class);
                        System.out.println("  Tables found: " + dataMap.size());
                    } catch (Exception e) {
                        // Just show size if we can't parse as JSON
                        System.out.println("  Data size: " + data.length() + " bytes");
                    }
                }
                
                return data;
            } else {
                System.err.println("  Failed to fetch data: HTTP " + response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("  Failed to fetch remote data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Save data to JSON file
     */
    private static void saveDataToJson(String data) throws IOException {
        File jsonFile = new File(JSON_DATA_FILE);
        
        // Backup existing JSON if it exists
        if (jsonFile.exists()) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String backupFile = JSON_DATA_FILE + "." + timestamp + ".bak";
            Files.copy(jsonFile.toPath(), Paths.get(backupFile), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  Existing JSON backed up to " + backupFile);
        }
        
        // Write new data
        try (FileWriter writer = new FileWriter(jsonFile)) {
            // Pretty print JSON
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(data, Object.class);
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, json);
        }
    }
    
    /**
     * Load data from local JSON file
     */
    private static String loadLocalJson() {
        File jsonFile = new File(JSON_DATA_FILE);
        if (jsonFile.exists()) {
            try {
                return new String(Files.readAllBytes(jsonFile.toPath()));
            } catch (IOException e) {
                System.err.println("  Failed to read local JSON: " + e.getMessage());
            }
        }
        return null;
    }
    
    /**
     * Backup the current database file
     */
    private static void backupDatabase(String databaseUrl) {
        try {
            String dbPath = databaseUrl.replace("jdbc:sqlite:", "").split("\\?")[0];
            File dbFile = new File(dbPath);
            
            if (!dbFile.exists()) {
                System.out.println("No existing database file to backup");
                return;
            }
            
            // Create backup directory
            File backupDir = new File(BACKUP_DIR);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            // Create backup with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "sqlite_backup_" + timestamp + ".db";
            Path backupPath = Paths.get(BACKUP_DIR, backupFileName);
            
            Files.copy(dbFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Database backed up to: " + backupPath);
            
            // Backup WAL files if they exist
            backupWalFiles(dbFile, timestamp);
            
        } catch (IOException e) {
            System.err.println("Warning: Failed to backup database: " + e.getMessage());
        }
    }
    
    /**
     * Backup WAL and SHM files if they exist
     */
    private static void backupWalFiles(File dbFile, String timestamp) {
        try {
            File walFile = new File(dbFile.getAbsolutePath() + "-wal");
            if (walFile.exists()) {
                Path walBackupPath = Paths.get(BACKUP_DIR, "sqlite_backup_" + timestamp + ".db-wal");
                Files.copy(walFile.toPath(), walBackupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("WAL file backed up");
            }
            
            File shmFile = new File(dbFile.getAbsolutePath() + "-shm");
            if (shmFile.exists()) {
                Path shmBackupPath = Paths.get(BACKUP_DIR, "sqlite_backup_" + timestamp + ".db-shm");
                Files.copy(shmFile.toPath(), shmBackupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("SHM file backed up");
            }
        } catch (IOException e) {
            System.out.println("Note: Could not backup WAL/SHM files: " + e.getMessage());
        }
    }
    
    /**
     * Drop all tables in the database
     */
    private static void dropAllTables(String databaseUrl) throws SQLException {
        String dbPath = databaseUrl.replace("jdbc:sqlite:", "").split("\\?")[0];
        
        System.out.println("\nRemoving old database...");
        File dbFile = new File(dbPath);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        File walFile = new File(dbPath + "-wal");
        if (walFile.exists()) {
            walFile.delete();
        }
        File shmFile = new File(dbPath + "-shm");
        if (shmFile.exists()) {
            shmFile.delete();
        }
        System.out.println("Old database removed");
    }
    
    /**
     * Recreate database by starting Spring Boot temporarily
     * NOTE: ModelInit will be skipped via the flag file
     */
    private static void recreateDatabase() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("./mvnw", "spring-boot:run", 
            "-Dspring-boot.run.arguments=--spring.jpa.hibernate.ddl-auto=create");
        pb.redirectOutput(new File("/tmp/db_migrate.log"));
        pb.redirectError(new File("/tmp/db_migrate.log"));
        
        Process process = pb.start();
        
        // Wait for application to start
        System.out.print("Waiting for application");
        int counter = 0;
        int maxWait = 60;
        
        while (counter < maxWait) {
            try {
                // Check if port is open
                ProcessBuilder checkPort = new ProcessBuilder("lsof", "-Pi", ":8585", "-sTCP:LISTEN", "-t");
                Process portCheck = checkPort.start();
                portCheck.waitFor();
                
                if (portCheck.exitValue() == 0) {
                    System.out.println(" OK");
                    Thread.sleep(3000); // Give it time to complete schema creation
                    break;
                }
            } catch (Exception e) {
                // Port not open yet
            }
            
            System.out.print(".");
            Thread.sleep(1000);
            counter++;
            
            // Check if process died
            if (!process.isAlive()) {
                System.out.println("\nApplication failed to start. Check /tmp/db_migrate.log");
                System.exit(1);
            }
        }
        
        if (counter == maxWait) {
            System.out.println("\nTimeout waiting for application");
            process.destroy();
            System.exit(1);
        }
        
        // Stop the application
        System.out.println("Stopping temporary instance...");
        process.destroy();
        process.waitFor();
        Thread.sleep(2000); // Give it time to shutdown cleanly
    }
    
    /**
     * Load remote data into the new database using Python script
     */
    private static void loadRemoteData(String data) {
        try {
            System.out.println("  Importing remote data using Python script...");
            
            // Use the Python import script
            ProcessBuilder pb = new ProcessBuilder(
                "python3",
                "scripts/import_json_to_sqlite.py",
                JSON_DATA_FILE,
                "volumes/sqlite.db"
            );
            
            pb.inheritIO(); // Show output in console
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("  Remote data imported successfully");
            } else {
                System.out.println("  Import script exited with code: " + exitCode);
                System.out.println("  You can manually run: python3 scripts/import_json_to_sqlite.py");
            }
            
        } catch (Exception e) {
            System.err.println("  Error importing remote data: " + e.getMessage());
            System.out.println("  You can manually run: python3 scripts/import_json_to_sqlite.py");
        }
    }
}
package com.open.spring.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Scanner;

/**
 * DatabaseInitializer.java
 * 
 * Standalone utility to initialize/reset the database
 * - Drops all existing tables
 * - Creates new tables based on entity definitions
 * - Initializes tables with test data
 * 
 * Usage: Run from the terminal as such:
 * 
 * From the scripts directory:
 * > cd scripts; ./db_init.sh
 * 
 * Or from the root of the project:
 * > scripts/db_init.sh
 * 
 * Or directly with Java:
 * > java -cp target/classes com.open.spring.system.DatabaseInitializer
 * 
 * Or with force flag (skip confirmation):
 * > FORCE_YES=true scripts/db_init.sh
 * 
 * General Process outline:
 * 0. Warning to the user
 * 1. Check if database has existing tables
 * 2. Backup the current database if it exists
 * 3. Drop all existing tables
 * 4. Create new schema (tables are auto-created by Hibernate on next startup)
 * 5. Load test data using ModelInit logic
 */
public class DatabaseInitializer {
    
    private static final String BACKUP_DIR = "volumes/backups/";
    private static final String PROPERTIES_FILE = "src/main/resources/application.properties";
    
    /**
     * Main method - runs as standalone application
     */
    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DATABASE INITIALIZATION STARTING");
        System.out.println("=".repeat(60) + "\n");
        
        try {
            // Load database URL from properties
            String databaseUrl = loadDatabaseUrl();
            
            // Step 0: Warning and confirmation
            if (!getUserConfirmation(databaseUrl)) {
                System.out.println("Exiting without making changes.");
                System.exit(0);
            }
            
            // Step 1: Backup the database
            backupDatabase(databaseUrl);
            
            // Step 2: Drop all tables
            dropAllTables(databaseUrl);
            
            // Step 3: Tables will be auto-created by Hibernate on next startup
            System.out.println("\nDatabase has been reset!");
            System.out.println("\nNext steps:");
            System.out.println("  1. Start your Spring Boot application normally");
            System.out.println("  2. Hibernate will auto-create the schema");
            System.out.println("  3. ModelInit will populate test data");
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("DATABASE INITIALIZATION COMPLETE");
            System.out.println("=".repeat(60) + "\n");
            
        } catch (Exception e) {
            System.err.println("\nAn error occurred during database initialization:");
            e.printStackTrace();
            System.exit(1);
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
                System.out.println("No existing database found. Will create new database on next startup.");
                return true;
            }
            
            // Check if any tables exist
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                boolean hasTables = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"}).next();
                
                if (hasTables) {
                    System.out.println("WARNING: You are about to lose all data in the database!");
                    System.out.println("This operation will:");
                    System.out.println("   - Backup the current database");
                    System.out.println("   - Drop all existing tables");
                    System.out.println("   - Reset the database schema");
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
     * Backup the current database file
     */
    private static void backupDatabase(String databaseUrl) {
        try {
            // Extract database path from JDBC URL
            // Format: jdbc:sqlite:volumes/sqlite.db?journal_mode=WAL
            String dbPath = databaseUrl.replace("jdbc:sqlite:", "").split("\\?")[0];
            File dbFile = new File(dbPath);
            
            if (!dbFile.exists()) {
                System.out.println("No existing database file found at: " + dbPath);
                return;
            }
            
            // Create backup directory if it doesn't exist
            File backupDir = new File(BACKUP_DIR);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            // Create backup filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "sqlite_backup_" + timestamp + ".db";
            Path backupPath = Paths.get(BACKUP_DIR, backupFileName);
            
            // Copy database file to backup
            Files.copy(dbFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("Database backed up to: " + backupPath);
            
            // Also backup WAL and SHM files if they exist
            backupWalFiles(dbFile, timestamp);
            
        } catch (IOException e) {
            System.err.println("Warning: Failed to backup database: " + e.getMessage());
            // Continue anyway - this is not critical
        }
    }
    
    /**
     * Backup WAL and SHM files if they exist
     */
    private static void backupWalFiles(File dbFile, String timestamp) {
        try {
            // Backup WAL file
            File walFile = new File(dbFile.getAbsolutePath() + "-wal");
            if (walFile.exists()) {
                Path walBackupPath = Paths.get(BACKUP_DIR, "sqlite_backup_" + timestamp + ".db-wal");
                Files.copy(walFile.toPath(), walBackupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("WAL file backed up");
            }
            
            // Backup SHM file
            File shmFile = new File(dbFile.getAbsolutePath() + "-shm");
            if (shmFile.exists()) {
                Path shmBackupPath = Paths.get(BACKUP_DIR, "sqlite_backup_" + timestamp + ".db-shm");
                Files.copy(shmFile.toPath(), shmBackupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("SHM file backed up");
            }
        } catch (IOException e) {
            // Non-critical, just log
            System.out.println("Note: Could not backup WAL/SHM files: " + e.getMessage());
        }
    }
    
    /**
     * Drop all tables in the database
     */
    private static void dropAllTables(String databaseUrl) throws SQLException {
        String dbPath = databaseUrl.replace("jdbc:sqlite:", "").split("\\?")[0];
        
        // First connection: checkpoint WAL and close cleanly
        System.out.println("\nPreparing database...");
        try (Connection conn1 = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            Statement stmt1 = conn1.createStatement();
            // Checkpoint the WAL file
            stmt1.execute("PRAGMA wal_checkpoint(TRUNCATE)");
            System.out.println("WAL checkpoint completed");
        } catch (SQLException e) {
            System.out.println("Note: WAL checkpoint skipped (" + e.getMessage() + ")");
        }
        
        // Small delay to ensure file handles are released
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Second connection: drop tables
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            Statement stmt = conn.createStatement();
            
            // Set long timeout to avoid locks
            stmt.execute("PRAGMA busy_timeout = 30000");
            
            // Disable foreign key constraints
            stmt.execute("PRAGMA foreign_keys = OFF");
            
            // Disable journal for this session (faster)
            stmt.execute("PRAGMA journal_mode = OFF");
            
            // Get all table names
            var tables = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
            int tableCount = 0;
            
            System.out.println("\nDropping tables:");
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                // Skip SQLite internal tables
                if (!tableName.startsWith("sqlite_")) {
                    stmt.execute("DROP TABLE IF EXISTS \"" + tableName + "\"");
                    System.out.println("Dropped table: " + tableName);
                    tableCount++;
                }
            }
            
            // Re-enable foreign key constraints
            stmt.execute("PRAGMA foreign_keys = ON");
            
            System.out.println("Total tables dropped: " + tableCount);
            
            // Vacuum the database to reclaim space
            System.out.println("\nOptimizing database...");
            stmt.execute("VACUUM");
            System.out.println("Database optimized");
            
            // Restore WAL mode for next startup
            stmt.execute("PRAGMA journal_mode = WAL");
            System.out.println("WAL mode restored");
            
        } catch (SQLException e) {
            System.err.println("Error dropping tables: " + e.getMessage());
            System.err.println("\nTroubleshooting:");
            System.err.println("  1. Make sure the Spring Boot application is NOT running");
            System.err.println("  2. Close any database tools (DB Browser, etc.)");
            System.err.println("  3. Try deleting the WAL files manually:");
            System.err.println("     rm volumes/sqlite.db-wal volumes/sqlite.db-shm");
            throw e;
        }
    }
}
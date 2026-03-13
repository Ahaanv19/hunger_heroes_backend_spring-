package com.open.spring.mvc.javarunner;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/run")
@CrossOrigin(origins = {"http://127.0.0.1:4500","https://pages.opencodingsociety.com"}, allowCredentials = "true")
public class JavaRunnerApiController {

    // Blacklist dangerous classes and methods
    private static final String[] FORBIDDEN_KEYWORDS = {
        "System.exit", "Runtime.getRuntime", "ProcessBuilder",
        "File", "FileReader", "FileWriter", "RandomAccessFile",
        "socket", "ServerSocket", "DatagramSocket",
        "URLConnection", "HttpURLConnection",
        "reflection", "invoke", "getDeclaredMethod", "getDeclaredField",
        "ClassLoader", "defineClass", "forName",
        "SecurityManager", "setSecurityManager",
        "Thread", "ThreadGroup", "sleep", "interrupt"
    };

    private static final long TIMEOUT_MS = 3000; // 3-second timeout

    @PostMapping("/java")
    public ResponseEntity<Map<String, String>> runJava(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.trim().isEmpty()) {
            return new ResponseEntity<>(Map.of("output", "No code provided."), HttpStatus.BAD_REQUEST);
        }

        // Check for forbidden keywords (case-insensitive)
        String codeUpper = code.toUpperCase();
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (codeUpper.contains(keyword.toUpperCase())) {
                return new ResponseEntity<>(Map.of("output", "Code contains forbidden operation: " + keyword), HttpStatus.BAD_REQUEST);
            }
        }

        try {
            // Extract class name from code
            String className = extractClassName(code);
            if (className == null) {
                return new ResponseEntity<>(Map.of("output", "No public class found in code."), HttpStatus.BAD_REQUEST);
            }

            // Create a temporary directory and file
            Path tempDir = Files.createTempDirectory("java-run-");
            Path javaFile = tempDir.resolve(className + ".java");
            Files.writeString(javaFile, code);

            // Step 1: Compile Java code
            Process compileProcess = new ProcessBuilder("javac", javaFile.toString())
                    .directory(tempDir.toFile())
                    .redirectErrorStream(true)
                    .start();

            String compileOutput = new String(compileProcess.getInputStream().readAllBytes());
            compileProcess.waitFor();

            if (compileProcess.exitValue() != 0) {
                cleanup(tempDir);
                return new ResponseEntity<>(Map.of("output", "Compilation error:\n" + compileOutput), HttpStatus.OK);
            }

            // Step 2: Run compiled Java code
            Process runProcess = new ProcessBuilder("java", "-cp", tempDir.toString(), className)
                    .redirectErrorStream(true)
                    .start();

            boolean finished = runProcess.waitFor(TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS);
            String runOutput = new String(runProcess.getInputStream().readAllBytes());

            if (!finished) {
                runProcess.destroyForcibly();
                cleanup(tempDir);
                return new ResponseEntity<>(Map.of("output", "Execution timed out (" + (TIMEOUT_MS / 1000) + "s limit)."), HttpStatus.OK);
            }

            cleanup(tempDir);
            return new ResponseEntity<>(Map.of("output", runOutput), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("output", "Error running code: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper: extract public class name from code
    private String extractClassName(String code) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("public\\s+class\\s+(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // Helper: safely delete temporary files
    private void cleanup(Path dir) {
        try {
            Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try { Files.delete(path); } catch (IOException ignored) {}
                });
        } catch (IOException ignored) {}
    }
}
package com.open.spring.mvc.mortevision;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        // Prepare a map to hold error details
        Map<String, String> errorDetails = new HashMap<>();

        // Extract variable name, value, and expected type
        errorDetails.put("error", "Type Mismatch");
        errorDetails.put("parameterName", ex.getName());
        errorDetails.put("parameterValue", String.valueOf(ex.getValue()));
        errorDetails.put("expectedType", ex.getRequiredType().getSimpleName());

        // Print the error details (optional, for debugging)
        System.out.println("Type Mismatch Error: " + errorDetails);

        // Return the error details in the response
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
}

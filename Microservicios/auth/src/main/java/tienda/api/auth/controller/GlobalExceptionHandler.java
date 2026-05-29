package tienda.api.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, WebRequest request, HttpServletRequest servletRequest) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getClass().getSimpleName());
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));
        
        System.err.println("Exception: " + ex.getMessage());
        System.err.println("Path: " + servletRequest.getRequestURI());
        ex.printStackTrace();

        if (ex instanceof AccessDeniedException) {
            body.put("status", HttpStatus.FORBIDDEN.value());
            return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
        }
        
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

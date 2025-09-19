package com.nic.nerie.configs;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.nic.nerie.exceptions.MyAuthenticationCredentialsNotFoundException;
import com.nic.nerie.exceptions.MyAuthorizationDeniedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class ControllerAdviceConfig {
    private static final Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private static final Logger authenticationLogger = LoggerFactory.getLogger("AUTHENTICATION_LOGGER");
    private static final Logger authorizationLogger = LoggerFactory.getLogger("AUTHORIZATION_LOGGER");
    private static final Logger dataAccessLogger = LoggerFactory.getLogger("DATA_ACCESS_LOGGER");
    
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public String handleAuthenticationNotFoundException(AuthenticationCredentialsNotFoundException ex) {
        authenticationLogger.error("Error retrieving authentication information.\nMessage {} \nException {}", ex.getMessage(), ex);
        return "redirect:/nerie/login?msg=unauthenticated";
    }
    
    @ExceptionHandler(MyAuthenticationCredentialsNotFoundException.class)
    public Object handleMyAuthenticationCredentialsNotFoundException(MyAuthenticationCredentialsNotFoundException ex) {
        authenticationLogger.error(ex.getMessage());

        if (ex.getResourceType().equalsIgnoreCase("page"))
            return "redirect:/nerie/login?msg=unauthenticated";
        else if (ex.getResourceType().equalsIgnoreCase("json"))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        else
            throw new RuntimeException("Resource type " + ex.getResourceType() + " is not allowed.");
    }

    @ExceptionHandler(MyAuthorizationDeniedException.class)
    public Object handleMyAuthorizationDeniedException(MyAuthorizationDeniedException ex) {
        authorizationLogger.error(ex.getMessage());

        if (ex.getResourceType().equalsIgnoreCase("page"))
            return "redirect:/nerie/error/404";
        else if (ex.getResourceType().equalsIgnoreCase("json"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        else
            throw new RuntimeException("Resource type " + ex.getResourceType() + " is not allowed.");
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<String> handleDataAccessFailureException(DataAccessException ex) {
        dataAccessLogger.error("Error accessing data source.\nMessage {} \nException {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body("Something went wrong. " + ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        rootLogger.error("Constraint violation.\nMessage {} \nException {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body("Invalid Request: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        rootLogger.error("Something went wrong.\nMessage {} \nException {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred. Please try again later.");
    }
} 

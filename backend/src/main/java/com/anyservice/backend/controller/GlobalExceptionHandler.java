package com.anyservice.backend.controller;

import com.anyservice.backend.controller.dto.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Intercepts exceptions thrown by controllers and translates them into uniform JSON responses. */
@ControllerAdvice
public class GlobalExceptionHandler {

    /** Handles business logic exceptions, returning 400 Bad Request with the error message. */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponse> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
    }

    /** Handles missing static resources (like old avatars), returning 404 Not Found. */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<MessageResponse> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        return ResponseEntity.status(404).body(new MessageResponse("Recurso não encontrado."));
    }

    /** Catches all other unhandled exceptions, returning a generic 500 Internal Server Error. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGeneralException(Exception ex) {
        return ResponseEntity.internalServerError().body(new MessageResponse("Ocorreu um erro interno no servidor."));
    }
}

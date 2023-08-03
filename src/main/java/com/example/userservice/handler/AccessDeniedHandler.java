package com.example.userservice.handler;

import com.example.userservice.dto.response.MessageResponse;
import com.google.rpc.BadRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class AccessDeniedHandler {

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public MessageResponse handleAccessDeniedException(AccessDeniedException exception){
        log.error("AccessDenied exception: {}", exception.getMessage());
        return new MessageResponse("Access denied");
    }
}

package com.example.userservice.handler;

import com.example.userservice.dto.response.MessageResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class BadJwtTokenHandler {

    @ExceptionHandler(JwtException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public MessageResponse handle(JwtException jwtException){
        return new MessageResponse(jwtException.getMessage());
    }
}

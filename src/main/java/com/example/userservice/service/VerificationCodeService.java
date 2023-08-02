package com.example.userservice.service;

import com.example.userservice.exception.EntityNotFoundException;
import com.example.userservice.model.User;
import com.example.userservice.model.VerificationCode;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.repository.VerificationCodeRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class VerificationCodeService {
    @Autowired
    private VerificationCodeRepository codeRepository;
    @Autowired
    private UserRepository userRepository;
    @Value("${verification.code.ttl}")
    private long codeTTL;

    public UUID resetCode(String userEmail){
        VerificationCode code = codeRepository.findByUserEmail(userEmail);
        if(code!=null){
            code.update();
            return codeRepository.save(code).getValue();
        } else {
            throw new EntityNotFoundException("verification code for username with email '"+userEmail+"' not found");
        }
    }

    public VerificationCode createCode(String userEmail){
        User byEmail = userRepository.findByEmail(userEmail);
        if(byEmail!=null){
            VerificationCode code = new VerificationCode(byEmail);
            return codeRepository.save(code);
        } else {
            throw new EntityNotFoundException("Username with email '"+userEmail+"' not found");
        }
    }

    public User findByCode(UUID code){
        VerificationCode byValue = codeRepository.findByValue(code);
        if(byValue!=null){
            return byValue.getUser();
        } else {
            throw new EntityNotFoundException("User with verification code ["+code.toString().substring(0,5)+"...] not found");
        }
    }

    public boolean isExpired(UUID codeValue){
        VerificationCode code = codeRepository.findByValue(codeValue);
        if(code==null) throw new EntityNotFoundException("Verification code ["+codeValue+"] was not found");
        return new Date().after(new Date(code.getCreatedAt().getTime()+codeTTL));
    }
}

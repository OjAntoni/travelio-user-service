package com.example.userservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender emailSender;
    @Value("${smtp.username}")
    private String emailUsername;
    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Async
    @SneakyThrows
    public void sendVerificationEmail(String email, UUID verificationCode){
        Context context = new Context();
        context.setVariable("url", "http://localhost:8082/api/auth/verify?code="+verificationCode);
        String html = springTemplateEngine.process("email-verification.html", context);

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

        helper.setFrom(emailUsername);
        helper.setTo(email);
        helper.setText(html, true);

        FileSystemResource res = new FileSystemResource(new File("src/main/resources/static/logo.png"));
        helper.addInline("logo", res);

        emailSender.send(message);
    }
}

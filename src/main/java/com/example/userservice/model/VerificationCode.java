package com.example.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private UUID value;
    @OneToOne
    @NotNull
    private User user;
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public VerificationCode(@NotNull User user) {
        this.value = UUID.randomUUID();
        this.user = user;
    }

    public void update(){
        value = UUID.randomUUID();
    }
}

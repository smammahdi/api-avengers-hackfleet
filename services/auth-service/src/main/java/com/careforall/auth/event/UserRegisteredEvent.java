package com.careforall.auth.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * UserRegisteredEvent
 *
 * Published when a new user registers.
 * Consumed by donation-service to link any existing guest donations with the new user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String email;
    private String name;
    private LocalDateTime registeredAt;
}

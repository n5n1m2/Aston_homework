package ru.aston.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @NotEmpty(message = "name don't should be empty")
    private String name;
    @NotEmpty(message = "email cannot be blank")
    @Email(message = "enter valid email")
    private String email;
    @NotNull(message = "age cannot be null")
    private Short age;
    private Instant createdAt;
}

package ru.netology.cloudservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    /**
     * Хранится BCrypt-хеш пароля, никогда не открытый текст.
     */
    @Column(nullable = false)
    private String password;
}

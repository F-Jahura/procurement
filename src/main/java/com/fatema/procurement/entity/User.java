package com.fatema.procurement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @NotBlank(message = "Логин обязателен")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "is_active")
    private boolean isActive = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (roles == null || roles.isEmpty()) {
            // По умолчанию даём роль USER
            Role userRole = new Role();
            userRole.setId(1L); // ID роли USER
            userRole.setName(ERole.ROLE_USER);
            roles.add(userRole);
        }
    }
}


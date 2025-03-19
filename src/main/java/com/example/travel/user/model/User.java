package com.example.travel.user.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String nickname;

	@Column(nullable = false, length = 10)
	private String gender;

	@Column(nullable = false)
	private LocalDate birthdate;

	@Column(nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	private RoleType role; // ROLE_USER, ROLE_ADMIN 등

	@Column(nullable = false)
	private String provider; // 소셜 로그인 제공자 정보 (예: "google")

	@Column(name = "created_at", nullable = true, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {this.createdAt = LocalDateTime.now();
	}
}
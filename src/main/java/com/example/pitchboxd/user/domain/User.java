package com.example.pitchboxd.user.domain;

import com.example.pitchboxd.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private Long favoriteTeamId;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    public User(String nickname, String email, String password) {
        validateNickname(nickname);
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }

    public User(String nickname, String email, String password, Long favoriteTeamId) {
        validateNickname(nickname);
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.favoriteTeamId = favoriteTeamId;
    }

    public User(String nickname, String email, String password, Long favoriteTeamId, Provider provider) {
        validateNickname(nickname);
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.favoriteTeamId = favoriteTeamId;
        this.provider = provider;
    }

    public User(String nickname, String email, String password, Long favoriteTeamId, Provider provider,
                String providerKey) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.favoriteTeamId = favoriteTeamId;
        this.provider = provider;
        this.providerKey = providerKey;
    }

    private void validateNickname(String nickname) {
        if (nickname == null) {
            throw new IllegalArgumentException("닉네임이 입력되지 않았습니다.");
        }

        if (nickname.isBlank() || nickname.length() > 20) {
            throw new IllegalArgumentException("닉네임은 1자 이상, 20자 이하여야 합니다.");
        }
    }

    public boolean matchId(Long id) {
        return this.id.equals(id);
    }

    public boolean isFanOf(Long teamId) {
        return this.favoriteTeamId.equals(teamId);
    }

    public void assignRole(UserRole role) {
        this.role = role;
    }
}

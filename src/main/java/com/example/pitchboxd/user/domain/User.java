package com.example.pitchboxd.user.domain;

import com.example.pitchboxd.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private Long favoriteTeamId;

    private Provider provider;

    public User(String nickname, String email, String password) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }

    public User(String nickname, String email, String password, Long favoriteTeamId) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.favoriteTeamId = favoriteTeamId;
    }

    public User(String nickname, String email, String password, Long favoriteTeamId, Provider provider) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.favoriteTeamId = favoriteTeamId;
        this.provider = provider;
    }

    public boolean matchId(Long id) {
        return this.id.equals(id);
    }

    public boolean isFanOf(Long teamId) {
        return this.favoriteTeamId.equals(teamId);
    }
}

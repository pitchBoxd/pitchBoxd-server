package com.example.pitchboxd.admin.presentation;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.season.domain.Season;
import com.example.pitchboxd.season.infrastructure.SeasonRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.domain.UserRole;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.hamcrest.Matchers.containsString;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AdminViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();

        User admin = new User("관리자", "admin@example.com", "password123!");
        admin.assignRole(UserRole.ADMIN);
        admin = userRepository.save(admin);
        adminToken = tokenManager.createAccessToken(admin.getId(), admin.getEmail());

        User user = userRepository.save(new User("일반유저", "user@example.com", "password123!"));
        userToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 어드민_쿠키가_있으면_어드민_대시보드_HTML을_조회한다() throws Exception {
        mockMvc.perform(get("/admin")
                        .cookie(new Cookie("access_token", adminToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    void 일반_사용자_쿠키로_접근_시_인가_에러가_발생한다() throws Exception {
        mockMvc.perform(get("/admin")
                        .cookie(new Cookie("access_token", userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 비로그인_사용자_접근_시_인증_에러가_발생한다() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 대시보드_HTML에_주요_UI_요소들이_포함되어_있다() throws Exception {
        mockMvc.perform(get("/admin")
                        .cookie(new Cookie("access_token", adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"dashboard-tab\"")))
                .andExpect(content().string(containsString("id=\"sync-tab\"")))
                .andExpect(content().string(containsString("id=\"users-tab\"")));
    }

    @Test
    void 시즌_파라미터가_있으면_해당_시즌의_경기를_모델에_담아_대시보드를_반환한다() throws Exception {
        // given
        Season season = seasonRepository.save(new Season("2026"));
        Team home = teamRepository.save(new Team("울산", "n1"));
        Team away = teamRepository.save(new Team("전북", "n2"));
        matchRepository.save(new Match(season.getId(), "1", home.getId(), away.getId(), LocalDateTime.now(), MatchStatus.SCHEDULED, "울산 문수", "naver-m1"));

        // when & then
        mockMvc.perform(get("/admin")
                        .param("seasonId", season.getId().toString())
                        .cookie(new Cookie("access_token", adminToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("selectedSeasonId", season.getId()))
                .andExpect(model().attributeExists("seasons"))
                .andExpect(model().attributeExists("matches"));
    }

    @Test
    void 시즌_파라미터가_없으면_최근_시즌의_경기를_모델에_담아_대시보드를_반환한다() throws Exception {
        // given
        Season season1 = seasonRepository.save(new Season("2025"));
        Season season2 = seasonRepository.save(new Season("2026"));
        Team home = teamRepository.save(new Team("울산", "n1"));
        Team away = teamRepository.save(new Team("전북", "n2"));
        
        matchRepository.save(new Match(season2.getId(), "1", home.getId(), away.getId(), LocalDateTime.now(), MatchStatus.SCHEDULED, "울산 문수", "naver-m1"));
        
        // when & then
        mockMvc.perform(get("/admin")
                        .cookie(new Cookie("access_token", adminToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("selectedSeasonId", season2.getId()))
                .andExpect(model().attributeExists("seasons"))
                .andExpect(model().attributeExists("matches"));
    }

    @Test
    void 시즌_파라미터가_있으면_해당_시즌의_경기를_HTML에_렌더링하여_반환한다() throws Exception {
        // given
        Season season = seasonRepository.save(new Season("2026"));
        Team home = teamRepository.save(new Team("울산", "n1"));
        Team away = teamRepository.save(new Team("전북", "n2"));
        matchRepository.save(new Match(season.getId(), "1", home.getId(), away.getId(), LocalDateTime.now(), MatchStatus.SCHEDULED, "울산 문수", "naver-m1"));

        // when & then
        mockMvc.perform(get("/admin")
                        .param("seasonId", season.getId().toString())
                        .cookie(new Cookie("access_token", adminToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(content().string(containsString("id=\"matches-tab\"")))
                .andExpect(content().string(containsString("울산")))
                .andExpect(content().string(containsString("전북")));
    }
}


package com.example.pitchboxd.team.infrastructure;

import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.dto.response.TeamDetailResponse;
import com.example.pitchboxd.team.dto.response.TeamFollowerCountResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByNaverId(String naverCode);

    @Query("select new com.example.pitchboxd.team.dto.response.TeamFollowerCountResponse(t.id, t.name, count(u.id)) " +
           "from Team t " +
           "left join User u on u.favoriteTeamId = t.id " +
           "group by t.id, t.name")
    List<TeamFollowerCountResponse> findTeamFollowerCounts();

    @Query("select new com.example.pitchboxd.team.dto.response.TeamDetailResponse(t.id, t.name, t.stadium, count(u.id)) " +
           "from Team t " +
           "left join User u on u.favoriteTeamId = t.id " +
           "group by t.id, t.name, t.stadium")
    List<TeamDetailResponse> findTeamDetails();
}

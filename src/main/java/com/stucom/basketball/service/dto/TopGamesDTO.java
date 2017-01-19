package com.stucom.basketball.service.dto;

import com.stucom.basketball.domain.Team;

/**
 * Created by Y3895917F on 16/01/2017.
 */
public class TopGamesDTO {
    private int score;
    private Team team;

    public TopGamesDTO(int score, Team team) {
        this.score = score;
        this.team = team;
    }

    public TopGamesDTO() {

    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopGamesDTO that = (TopGamesDTO) o;

        if (score != that.score) return false;
        return team != null ? team.equals(that.team) : that.team == null;

    }

    @Override
    public int hashCode() {
        int result = score;
        result = 31 * result + (team != null ? team.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TopGamesDTO{" +
            "score=" + score +
            ", team=" + team +
            '}';
    }
}

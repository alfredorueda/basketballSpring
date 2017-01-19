package com.stucom.basketball.service.dto;

import com.stucom.basketball.domain.Game;

/**
 * Created by Y3895917F on 13/01/2017.
 */
public class GameRatingDTO {
    private Game game;
    private Double avgScore;

    public GameRatingDTO(Game game, Double avgScore) {
        this.game = game;
        this.avgScore = avgScore;
    }

    public GameRatingDTO() {
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Double getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(Double avgScore) {
        this.avgScore = avgScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameRatingDTO that = (GameRatingDTO) o;

        if (game != null ? !game.equals(that.game) : that.game != null) return false;
        return avgScore != null ? avgScore.equals(that.avgScore) : that.avgScore == null;

    }

    @Override
    public int hashCode() {
        int result = game != null ? game.hashCode() : 0;
        result = 31 * result + (avgScore != null ? avgScore.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GameRatingDTO{" +
            "game=" + game +
            ", avgScore=" + avgScore +
            '}';
    }
}

package com.stucom.basketball.repository;

import com.stucom.basketball.domain.Game;
import com.stucom.basketball.domain.GameRating;

import com.stucom.basketball.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the GameRating entity.
 */
@SuppressWarnings("unused")
public interface GameRatingRepository extends JpaRepository<GameRating,Long> {

    @Query("select gameRating from GameRating gameRating where gameRating.user.login = ?#{principal.username}")
    List<GameRating> findByUserIsCurrentUser();

    @Query ("select avg(gameRating.score) from GameRating gameRating where gameRating.game = :game")
    Double avgGameRating(@Param("game")Game game);
    /*
    @Query ("select avg(gameRating.score) from GameRating gameRating group by gameRating.game order by desc limit 5")
    List<GameRating> findTopGameRating();
    */
    /*
     @Query("select favouritePlayer.player, count(favouritePlayer) from FavouritePlayer favouritePlayer " +
        "group by favouritePlayer.player order by count(favouritePlayer) desc ")
    List<Object[]> findTopFivePlayers(Pageable pageable);

    @Query ("select gameRating.game, sum(gameRating.score) from GameRating gameRating" +
        "group by gameRating.game order by count(gameRaing) desc")
    List<Object[]> findTopGameRatings(Pageable pageable);
     */
    //Se crea la query que ordena los juegos por Score paginados
    List<GameRating> findAllByOrderByScoreDesc(Pageable pageable);

    //Creamos la consulta para saber si un user ha votado a un game.
    Optional<GameRating> findByUserAndGame(User user, Game game);

}



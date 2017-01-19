package com.stucom.basketball.repository;

import com.stucom.basketball.domain.FavouritePlayer;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the FavouritePlayer entity.
 */
@SuppressWarnings("unused")
public interface FavouritePlayerRepository extends JpaRepository<FavouritePlayer,Long> {

    @Query("select favouritePlayer from FavouritePlayer favouritePlayer where favouritePlayer.user.login = ?#{principal.username}")
    List<FavouritePlayer> findByUserIsCurrentUser();

    @Query("select favouritePlayer.player, count(favouritePlayer) from FavouritePlayer favouritePlayer " +
           "group by favouritePlayer.player order by count(favouritePlayer) desc ")
    List<Object[]> findTopPlayers();

}

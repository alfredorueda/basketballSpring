package com.stucom.basketball.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.stucom.basketball.domain.Game;
import com.stucom.basketball.domain.GameRating;
import com.stucom.basketball.repository.GameRatingRepository;
import com.stucom.basketball.repository.GameRepository;
import com.stucom.basketball.repository.UserRepository;
import com.stucom.basketball.security.SecurityUtils;
import com.stucom.basketball.service.GameRatingService;
import com.stucom.basketball.service.GameService;
import com.stucom.basketball.service.dto.GameRatingDTO;
import com.stucom.basketball.web.rest.util.HeaderUtil;
import com.stucom.basketball.web.rest.util.PaginationUtil;

import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing GameRating.
 */
@RestController
@RequestMapping("/api")
public class GameRatingResource {

    private final Logger log = LoggerFactory.getLogger(GameRatingResource.class);

    @Inject
    private GameRatingService gameRatingService;

    @Inject
    private GameRepository gameRepository;

    @Inject
    private GameRatingRepository gameRatingRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GameService gameService;

    /**
     * POST  /game-ratings : Create a new gameRating.
     *
     * @param gameRating the gameRating to create
     * @return the ResponseEntity with status 201 (Created) and with body the new gameRating, or with status 400 (Bad Request) if the gameRating has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/game-ratings")
    @Timed
    public ResponseEntity<GameRating> createGameRating(@RequestBody GameRating gameRating) throws URISyntaxException {
        log.debug("REST request to save GameRating : {}", gameRating);
        if (gameRating.getId() != null) {
            //Comprobamos si el id de la valoracion existe, si existe regresamos un Failure Alert
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("gameRating", "idexists", "A new gameRating cannot already have an ID")).body(null);
        }

        if(gameRepository.findOne(gameRating.getGame().getId())==null){
            //Comprobamos si el objeto gameRating existe, si no existe regresamos un 403 bad request
            return ResponseEntity.badRequest().
                headers(HeaderUtil.createFailureAlert("gameRating","gameNotExistant","Game doesn't exists")).body(null);
        }

        gameRating.setUser(userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get());
        //Colocamos dentro del gameRating que pasamos EL USUARIO QUE ESTA LOGGEADO
        gameRating.setScoreDateTime(ZonedDateTime.now());
        //Le colocamos el tiempo de hoy, ya que bien vaya a actualizar o crear, será hoy cuando lo haga

        Optional<GameRating> gameRatingOptional = gameRatingRepository.findByUserAndGame(gameRating.getUser(),gameRating.getGame());

        //buscamos una valoracion de ese usuario y del juego que pasamos por parametro
        //Lo envolvemos en un Optional porque puede regresar un valor o un null
        //OJO dentro del repository la consulta tambien debe tener un Optional

        GameRating result = null;

        if(gameRatingOptional.isPresent()){
            //Si gameRatingOptional tiene valor, existe la valoracion, entonces actualizamos los datos
            result = gameRatingOptional.get();
            result.setScore(gameRating.getScore());
            //le colocamos como score al objeto result el score del gameRating que pasamos por parametro
            result.setScoreDateTime(gameRating.getScoreDateTime());
            //Podriamos colocar result.setScoreDateTime(ZonedDateTime.now()), pero tendria mas coste, asi que aprovechamos que ya
            //le colocamos a gameRating un ZonedDateTime.now()
             return updateGameRating(result);
            //De aqui lo enviamos al PUT, que se encargará de actualizar los datos
        }else{
            //si no existe una valoracion, es decir, gameRatingOptional es null, llamamos al repository y guardamos el gameRating
            //Finalmente regresamos la URL con la ruta del gameRating creado
            result = gameRatingRepository.save(gameRating);
            return ResponseEntity.created(new URI("/api/game-ratings/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("gameRating", result.getId().toString()))
                .body(result);
        }
    }

    /**
     * PUT  /game-ratings : Updates an existing gameRating.
     *
     * @param gameRating the gameRating to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated gameRating,
     * or with status 400 (Bad Request) if the gameRating is not valid,
     * or with status 500 (Internal Server Error) if the gameRating couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/game-ratings")
    @Timed
    public ResponseEntity<GameRating> updateGameRating(@RequestBody GameRating gameRating) throws URISyntaxException {
        log.debug("REST request to update GameRating : {}", gameRating);
        if (gameRating.getId() == null) {
            return createGameRating(gameRating);
        }
        GameRating result = gameRatingService.save(gameRating);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("gameRating", gameRating.getId().toString()))
            .body(result);
    }

    /**
     * GET  /game-ratings : get all the gameRatings.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of gameRatings in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/game-ratings")
    @Timed
    public ResponseEntity<List<GameRating>> getAllGameRatings(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of GameRatings");
        Page<GameRating> page = gameRatingService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/game-ratings");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /game-ratings/:id : get the "id" gameRating.
     *
     * @param id the id of the gameRating to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the gameRating, or with status 404 (Not Found)
     */
    @GetMapping("/game-ratings/{id}")
    @Timed
    public ResponseEntity<GameRating> getGameRating(@PathVariable Long id) {
        log.debug("REST request to get GameRating : {}", id);
        GameRating gameRating = gameRatingService.findOne(id);
        return Optional.ofNullable(gameRating)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/game-rating/avgGameRating/{id}")
    @Timed
    public ResponseEntity<GameRatingDTO> avgGameRating(@PathVariable Long id){
        log.debug("REST request to get avgGameRating : {}", id);
        //Buscamos un game desde el repository que contenga el id que pasamos por parametro
        Game game = gameRepository.findOne(id);
        if(game==null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            //Si el juego no existe, regresamos un not found
        }else{
            //si el juego existe, regresamos un objeto GameRatingDTO que contenga
            //1) El juego en si con todos sus datos
            //2) La consulta donde obtenemos el AVG de todas las valoraciones para ese juego

            GameRatingDTO gameRatingDTO = new GameRatingDTO(game,gameRatingRepository.avgGameRating(game));
            return new ResponseEntity<>(gameRatingDTO,HttpStatus.OK);
            //Regresamos el objeto gameRatingDTO con un HttpStatus OK

            //Por que un GameRatingDTO?
            //--------------------------
            /*
                Si necesitamos regresar el juego + sus estadisticas, no podemos regresar un objeto gameRating, ya que no existe
                un atributo media o algo parecido.

                Desde el repository hacemos la consulta
                @Query ("select avg(gameRating.score) from GameRating gameRating where gameRating.game = :game")
                Double avgGameRating(@Param("game")Game game);

                Donde seleccionamos el avg del gameRating (las valoraciones de ese partido) y que el juego sea igual al juego que queremos consultar.
                De esto regresamos un Double, el cual usaremos para crear un nuevo objeto GameRatingDTO que tendrá:
                    - Todos los datos del Juego
                    - Media del Juego

             */
        }
    }

    @GetMapping("/game-rating/topGames/")
    @Timed
    public ResponseEntity<GameRating> topGameRatings(){
        log.debug("REST request to get topGameRatings : {}");

        //Add pageable result

        Pageable topTen = new PageRequest(0, 5);

        //Se agrega el Pageable, lo cual limita los resultados. Seria como un limit 5 en SQL
        List<GameRating> topGameList = gameRatingRepository.findAllByOrderByScoreDesc(topTen);
        //Obtenemos la lista de los juegos y regresamos la lista en el body y el Status OK en el HTTP
        return new ResponseEntity(topGameList,HttpStatus.OK);

    }

    /**
     * DELETE  /game-ratings/:id : delete the "id" gameRating.
     *
     * @param id the id of the gameRating to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/game-ratings/{id}")
    @Timed
    public ResponseEntity<Void> deleteGameRating(@PathVariable Long id) {
        log.debug("REST request to delete GameRating : {}", id);
        gameRatingService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("gameRating", id.toString())).build();
    }

}

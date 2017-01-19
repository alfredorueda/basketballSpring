package com.stucom.basketball.web.rest;

import com.stucom.basketball.BasketballApp;

import com.stucom.basketball.domain.GameRating;
import com.stucom.basketball.repository.GameRatingRepository;
import com.stucom.basketball.service.GameRatingService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;

import static com.stucom.basketball.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the GameRatingResource REST controller.
 *
 * @see GameRatingResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BasketballApp.class)
public class GameRatingResourceIntTest {

    private static final Integer DEFAULT_SCORE = 1;
    private static final Integer UPDATED_SCORE = 2;

    private static final ZonedDateTime DEFAULT_SCORE_DATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_SCORE_DATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Inject
    private GameRatingRepository gameRatingRepository;

    @Inject
    private GameRatingService gameRatingService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restGameRatingMockMvc;

    private GameRating gameRating;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        GameRatingResource gameRatingResource = new GameRatingResource();
        ReflectionTestUtils.setField(gameRatingResource, "gameRatingService", gameRatingService);
        this.restGameRatingMockMvc = MockMvcBuilders.standaloneSetup(gameRatingResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GameRating createEntity(EntityManager em) {
        GameRating gameRating = new GameRating()
                .score(DEFAULT_SCORE)
                .scoreDateTime(DEFAULT_SCORE_DATE_TIME);
        return gameRating;
    }

    @Before
    public void initTest() {
        gameRating = createEntity(em);
    }

    @Test
    @Transactional
    public void createGameRating() throws Exception {
        int databaseSizeBeforeCreate = gameRatingRepository.findAll().size();

        // Create the GameRating

        restGameRatingMockMvc.perform(post("/api/game-ratings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gameRating)))
            .andExpect(status().isCreated());

        // Validate the GameRating in the database
        List<GameRating> gameRatingList = gameRatingRepository.findAll();
        assertThat(gameRatingList).hasSize(databaseSizeBeforeCreate + 1);
        GameRating testGameRating = gameRatingList.get(gameRatingList.size() - 1);
        assertThat(testGameRating.getScore()).isEqualTo(DEFAULT_SCORE);
        assertThat(testGameRating.getScoreDateTime()).isEqualTo(DEFAULT_SCORE_DATE_TIME);
    }

    @Test
    @Transactional
    public void createGameRatingWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = gameRatingRepository.findAll().size();

        // Create the GameRating with an existing ID
        GameRating existingGameRating = new GameRating();
        existingGameRating.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restGameRatingMockMvc.perform(post("/api/game-ratings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingGameRating)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<GameRating> gameRatingList = gameRatingRepository.findAll();
        assertThat(gameRatingList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllGameRatings() throws Exception {
        // Initialize the database
        gameRatingRepository.saveAndFlush(gameRating);

        // Get all the gameRatingList
        restGameRatingMockMvc.perform(get("/api/game-ratings?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(gameRating.getId().intValue())))
            .andExpect(jsonPath("$.[*].score").value(hasItem(DEFAULT_SCORE)))
            .andExpect(jsonPath("$.[*].scoreDateTime").value(hasItem(sameInstant(DEFAULT_SCORE_DATE_TIME))));
    }

    @Test
    @Transactional
    public void getGameRating() throws Exception {
        // Initialize the database
        gameRatingRepository.saveAndFlush(gameRating);

        // Get the gameRating
        restGameRatingMockMvc.perform(get("/api/game-ratings/{id}", gameRating.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(gameRating.getId().intValue()))
            .andExpect(jsonPath("$.score").value(DEFAULT_SCORE))
            .andExpect(jsonPath("$.scoreDateTime").value(sameInstant(DEFAULT_SCORE_DATE_TIME)));
    }

    @Test
    @Transactional
    public void getNonExistingGameRating() throws Exception {
        // Get the gameRating
        restGameRatingMockMvc.perform(get("/api/game-ratings/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateGameRating() throws Exception {
        // Initialize the database
        gameRatingService.save(gameRating);

        int databaseSizeBeforeUpdate = gameRatingRepository.findAll().size();

        // Update the gameRating
        GameRating updatedGameRating = gameRatingRepository.findOne(gameRating.getId());
        updatedGameRating
                .score(UPDATED_SCORE)
                .scoreDateTime(UPDATED_SCORE_DATE_TIME);

        restGameRatingMockMvc.perform(put("/api/game-ratings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedGameRating)))
            .andExpect(status().isOk());

        // Validate the GameRating in the database
        List<GameRating> gameRatingList = gameRatingRepository.findAll();
        assertThat(gameRatingList).hasSize(databaseSizeBeforeUpdate);
        GameRating testGameRating = gameRatingList.get(gameRatingList.size() - 1);
        assertThat(testGameRating.getScore()).isEqualTo(UPDATED_SCORE);
        assertThat(testGameRating.getScoreDateTime()).isEqualTo(UPDATED_SCORE_DATE_TIME);
    }

    @Test
    @Transactional
    public void updateNonExistingGameRating() throws Exception {
        int databaseSizeBeforeUpdate = gameRatingRepository.findAll().size();

        // Create the GameRating

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restGameRatingMockMvc.perform(put("/api/game-ratings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gameRating)))
            .andExpect(status().isCreated());

        // Validate the GameRating in the database
        List<GameRating> gameRatingList = gameRatingRepository.findAll();
        assertThat(gameRatingList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteGameRating() throws Exception {
        // Initialize the database
        gameRatingService.save(gameRating);

        int databaseSizeBeforeDelete = gameRatingRepository.findAll().size();

        // Get the gameRating
        restGameRatingMockMvc.perform(delete("/api/game-ratings/{id}", gameRating.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<GameRating> gameRatingList = gameRatingRepository.findAll();
        assertThat(gameRatingList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

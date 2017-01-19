package com.stucom.basketball.web.rest;

import com.stucom.basketball.BasketballApp;

import com.stucom.basketball.domain.FavouritePlayer;
import com.stucom.basketball.repository.FavouritePlayerRepository;
import com.stucom.basketball.service.FavouritePlayerService;

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
 * Test class for the FavouritePlayerResource REST controller.
 *
 * @see FavouritePlayerResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BasketballApp.class)
public class FavouritePlayerResourceIntTest {

    private static final ZonedDateTime DEFAULT_FAVOURITE_DATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_FAVOURITE_DATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Inject
    private FavouritePlayerRepository favouritePlayerRepository;

    @Inject
    private FavouritePlayerService favouritePlayerService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restFavouritePlayerMockMvc;

    private FavouritePlayer favouritePlayer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        FavouritePlayerResource favouritePlayerResource = new FavouritePlayerResource();
        ReflectionTestUtils.setField(favouritePlayerResource, "favouritePlayerService", favouritePlayerService);
        this.restFavouritePlayerMockMvc = MockMvcBuilders.standaloneSetup(favouritePlayerResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FavouritePlayer createEntity(EntityManager em) {
        FavouritePlayer favouritePlayer = new FavouritePlayer()
                .favouriteDateTime(DEFAULT_FAVOURITE_DATE_TIME);
        return favouritePlayer;
    }

    @Before
    public void initTest() {
        favouritePlayer = createEntity(em);
    }

    @Test
    @Transactional
    public void createFavouritePlayer() throws Exception {
        int databaseSizeBeforeCreate = favouritePlayerRepository.findAll().size();

        // Create the FavouritePlayer

        restFavouritePlayerMockMvc.perform(post("/api/favourite-players")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(favouritePlayer)))
            .andExpect(status().isCreated());

        // Validate the FavouritePlayer in the database
        List<FavouritePlayer> favouritePlayerList = favouritePlayerRepository.findAll();
        assertThat(favouritePlayerList).hasSize(databaseSizeBeforeCreate + 1);
        FavouritePlayer testFavouritePlayer = favouritePlayerList.get(favouritePlayerList.size() - 1);
        assertThat(testFavouritePlayer.getFavouriteDateTime()).isEqualTo(DEFAULT_FAVOURITE_DATE_TIME);
    }

    @Test
    @Transactional
    public void createFavouritePlayerWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = favouritePlayerRepository.findAll().size();

        // Create the FavouritePlayer with an existing ID
        FavouritePlayer existingFavouritePlayer = new FavouritePlayer();
        existingFavouritePlayer.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restFavouritePlayerMockMvc.perform(post("/api/favourite-players")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingFavouritePlayer)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<FavouritePlayer> favouritePlayerList = favouritePlayerRepository.findAll();
        assertThat(favouritePlayerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllFavouritePlayers() throws Exception {
        // Initialize the database
        favouritePlayerRepository.saveAndFlush(favouritePlayer);

        // Get all the favouritePlayerList
        restFavouritePlayerMockMvc.perform(get("/api/favourite-players?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(favouritePlayer.getId().intValue())))
            .andExpect(jsonPath("$.[*].favouriteDateTime").value(hasItem(sameInstant(DEFAULT_FAVOURITE_DATE_TIME))));
    }

    @Test
    @Transactional
    public void getFavouritePlayer() throws Exception {
        // Initialize the database
        favouritePlayerRepository.saveAndFlush(favouritePlayer);

        // Get the favouritePlayer
        restFavouritePlayerMockMvc.perform(get("/api/favourite-players/{id}", favouritePlayer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(favouritePlayer.getId().intValue()))
            .andExpect(jsonPath("$.favouriteDateTime").value(sameInstant(DEFAULT_FAVOURITE_DATE_TIME)));
    }

    @Test
    @Transactional
    public void getNonExistingFavouritePlayer() throws Exception {
        // Get the favouritePlayer
        restFavouritePlayerMockMvc.perform(get("/api/favourite-players/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateFavouritePlayer() throws Exception {
        // Initialize the database
        favouritePlayerService.save(favouritePlayer);

        int databaseSizeBeforeUpdate = favouritePlayerRepository.findAll().size();

        // Update the favouritePlayer
        FavouritePlayer updatedFavouritePlayer = favouritePlayerRepository.findOne(favouritePlayer.getId());
        updatedFavouritePlayer
                .favouriteDateTime(UPDATED_FAVOURITE_DATE_TIME);

        restFavouritePlayerMockMvc.perform(put("/api/favourite-players")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedFavouritePlayer)))
            .andExpect(status().isOk());

        // Validate the FavouritePlayer in the database
        List<FavouritePlayer> favouritePlayerList = favouritePlayerRepository.findAll();
        assertThat(favouritePlayerList).hasSize(databaseSizeBeforeUpdate);
        FavouritePlayer testFavouritePlayer = favouritePlayerList.get(favouritePlayerList.size() - 1);
        assertThat(testFavouritePlayer.getFavouriteDateTime()).isEqualTo(UPDATED_FAVOURITE_DATE_TIME);
    }

    @Test
    @Transactional
    public void updateNonExistingFavouritePlayer() throws Exception {
        int databaseSizeBeforeUpdate = favouritePlayerRepository.findAll().size();

        // Create the FavouritePlayer

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restFavouritePlayerMockMvc.perform(put("/api/favourite-players")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(favouritePlayer)))
            .andExpect(status().isCreated());

        // Validate the FavouritePlayer in the database
        List<FavouritePlayer> favouritePlayerList = favouritePlayerRepository.findAll();
        assertThat(favouritePlayerList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteFavouritePlayer() throws Exception {
        // Initialize the database
        favouritePlayerService.save(favouritePlayer);

        int databaseSizeBeforeDelete = favouritePlayerRepository.findAll().size();

        // Get the favouritePlayer
        restFavouritePlayerMockMvc.perform(delete("/api/favourite-players/{id}", favouritePlayer.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<FavouritePlayer> favouritePlayerList = favouritePlayerRepository.findAll();
        assertThat(favouritePlayerList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

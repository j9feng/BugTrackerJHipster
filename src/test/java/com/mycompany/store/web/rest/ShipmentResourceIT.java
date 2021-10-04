package com.mycompany.store.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.mycompany.store.IntegrationTest;
import com.mycompany.store.domain.Shipment;
import com.mycompany.store.repository.ShipmentRepository;
import com.mycompany.store.service.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link ShipmentResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class ShipmentResourceIT {

    private static final String DEFAULT_TRACKING_CODE = "AAAAAAAAAA";
    private static final String UPDATED_TRACKING_CODE = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_DETAILS = "AAAAAAAAAA";
    private static final String UPDATED_DETAILS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/shipments";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Shipment shipment;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Shipment createEntity(EntityManager em) {
        Shipment shipment = new Shipment().trackingCode(DEFAULT_TRACKING_CODE).date(DEFAULT_DATE).details(DEFAULT_DETAILS);
        return shipment;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Shipment createUpdatedEntity(EntityManager em) {
        Shipment shipment = new Shipment().trackingCode(UPDATED_TRACKING_CODE).date(UPDATED_DATE).details(UPDATED_DETAILS);
        return shipment;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Shipment.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        shipment = createEntity(em);
    }

    @Test
    void createShipment() throws Exception {
        int databaseSizeBeforeCreate = shipmentRepository.findAll().collectList().block().size();
        // Create the Shipment
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(shipment))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Shipment in the database
        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeCreate + 1);
        Shipment testShipment = shipmentList.get(shipmentList.size() - 1);
        assertThat(testShipment.getTrackingCode()).isEqualTo(DEFAULT_TRACKING_CODE);
        assertThat(testShipment.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testShipment.getDetails()).isEqualTo(DEFAULT_DETAILS);
    }

    @Test
    void createShipmentWithExistingId() throws Exception {
        // Create the Shipment with an existing ID
        shipment.setId(1L);

        int databaseSizeBeforeCreate = shipmentRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(shipment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Shipment in the database
        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = shipmentRepository.findAll().collectList().block().size();
        // set the field null
        shipment.setDate(null);

        // Create the Shipment, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(shipment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllShipments() {
        // Initialize the database
        shipmentRepository.save(shipment).block();

        // Get all the shipmentList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(shipment.getId().intValue()))
            .jsonPath("$.[*].trackingCode")
            .value(hasItem(DEFAULT_TRACKING_CODE))
            .jsonPath("$.[*].date")
            .value(hasItem(DEFAULT_DATE.toString()))
            .jsonPath("$.[*].details")
            .value(hasItem(DEFAULT_DETAILS));
    }

    @Test
    void getShipment() {
        // Initialize the database
        shipmentRepository.save(shipment).block();

        // Get the shipment
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, shipment.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(shipment.getId().intValue()))
            .jsonPath("$.trackingCode")
            .value(is(DEFAULT_TRACKING_CODE))
            .jsonPath("$.date")
            .value(is(DEFAULT_DATE.toString()))
            .jsonPath("$.details")
            .value(is(DEFAULT_DETAILS));
    }

    @Test
    void getNonExistingShipment() {
        // Get the shipment
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewShipment() throws Exception {
        // Initialize the database
        shipmentRepository.save(shipment).block();

        int databaseSizeBeforeUpdate = shipmentRepository.findAll().collectList().block().size();

        // Update the shipment
        Shipment updatedShipment = shipmentRepository.findById(shipment.getId()).block();
        updatedShipment.trackingCode(UPDATED_TRACKING_CODE).date(UPDATED_DATE).details(UPDATED_DETAILS);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedShipment.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedShipment))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Shipment in the database
        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeUpdate);
        Shipment testShipment = shipmentList.get(shipmentList.size() - 1);
        assertThat(testShipment.getTrackingCode()).isEqualTo(UPDATED_TRACKING_CODE);
        assertThat(testShipment.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testShipment.getDetails()).isEqualTo(UPDATED_DETAILS);
    }

    @Test
    void putNonExistingShipment() throws Exception {
        int databaseSizeBeforeUpdate = shipmentRepository.findAll().collectList().block().size();
        shipment.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, shipment.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(shipment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Shipment in the database
        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchShipment() throws Exception {
        int databaseSizeBeforeUpdate = shipmentRepository.findAll().collectList().block().size();
        shipment.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(shipment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Shipment in the database
        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamShipment() throws Exception {
        int databaseSizeBeforeUpdate = shipmentRepository.findAll().collectList().block().size();
        shipment.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(shipment))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Shipment in the database
        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateShipmentWithPatch() throws Exception {
        // Initialize the database
        shipmentRepository.save(shipment).block();

        int databaseSizeBeforeUpdate = shipmentRepository.findAll().collectList().block().size();

        // Update the shipment using partial update
        Shipment partialUpdatedShipment = new Shipment();
        partialUpdatedShipment.setId(shipment.getId());

        partialUpdatedShipment.trackingCode(UPDATED_TRACKING_CODE).date(UPDATED_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedShipment.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedShipment))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Shipment in the database
        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeUpdate);
        Shipment testShipment = shipmentList.get(shipmentList.size() - 1);
        assertThat(testShipment.getTrackingCode()).isEqualTo(UPDATED_TRACKING_CODE);
        assertThat(testShipment.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testShipment.getDetails()).isEqualTo(DEFAULT_DETAILS);
    }

    @Test
    void fullUpdateShipmentWithPatch() throws Exception {
        // Initialize the database
        shipmentRepository.save(shipment).block();

        int databaseSizeBeforeUpdate = shipmentRepository.findAll().collectList().block().size();

        // Update the shipment using partial update
        Shipment partialUpdatedShipment = new Shipment();
        partialUpdatedShipment.setId(shipment.getId());

        partialUpdatedShipment.trackingCode(UPDATED_TRACKING_CODE).date(UPDATED_DATE).details(UPDATED_DETAILS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedShipment.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedShipment))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Shipment in the database
        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeUpdate);
        Shipment testShipment = shipmentList.get(shipmentList.size() - 1);
        assertThat(testShipment.getTrackingCode()).isEqualTo(UPDATED_TRACKING_CODE);
        assertThat(testShipment.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testShipment.getDetails()).isEqualTo(UPDATED_DETAILS);
    }

    @Test
    void patchNonExistingShipment() throws Exception {
        int databaseSizeBeforeUpdate = shipmentRepository.findAll().collectList().block().size();
        shipment.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, shipment.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(shipment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Shipment in the database
        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchShipment() throws Exception {
        int databaseSizeBeforeUpdate = shipmentRepository.findAll().collectList().block().size();
        shipment.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(shipment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Shipment in the database
        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamShipment() throws Exception {
        int databaseSizeBeforeUpdate = shipmentRepository.findAll().collectList().block().size();
        shipment.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(shipment))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Shipment in the database
        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteShipment() {
        // Initialize the database
        shipmentRepository.save(shipment).block();

        int databaseSizeBeforeDelete = shipmentRepository.findAll().collectList().block().size();

        // Delete the shipment
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, shipment.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Shipment> shipmentList = shipmentRepository.findAll().collectList().block();
        assertThat(shipmentList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

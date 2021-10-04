package com.mycompany.store.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.mycompany.store.IntegrationTest;
import com.mycompany.store.domain.ProductOrder;
import com.mycompany.store.domain.enumeration.OrderStatus;
import com.mycompany.store.repository.ProductOrderRepository;
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
 * Integration tests for the {@link ProductOrderResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class ProductOrderResourceIT {

    private static final Instant DEFAULT_PLACED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_PLACED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final OrderStatus DEFAULT_STATUS = OrderStatus.COMPLETED;
    private static final OrderStatus UPDATED_STATUS = OrderStatus.PENDING;

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/product-orders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private ProductOrder productOrder;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductOrder createEntity(EntityManager em) {
        ProductOrder productOrder = new ProductOrder().placedDate(DEFAULT_PLACED_DATE).status(DEFAULT_STATUS).code(DEFAULT_CODE);
        return productOrder;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductOrder createUpdatedEntity(EntityManager em) {
        ProductOrder productOrder = new ProductOrder().placedDate(UPDATED_PLACED_DATE).status(UPDATED_STATUS).code(UPDATED_CODE);
        return productOrder;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(ProductOrder.class).block();
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
        productOrder = createEntity(em);
    }

    @Test
    void createProductOrder() throws Exception {
        int databaseSizeBeforeCreate = productOrderRepository.findAll().collectList().block().size();
        // Create the ProductOrder
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(productOrder))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeCreate + 1);
        ProductOrder testProductOrder = productOrderList.get(productOrderList.size() - 1);
        assertThat(testProductOrder.getPlacedDate()).isEqualTo(DEFAULT_PLACED_DATE);
        assertThat(testProductOrder.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testProductOrder.getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    void createProductOrderWithExistingId() throws Exception {
        // Create the ProductOrder with an existing ID
        productOrder.setId(1L);

        int databaseSizeBeforeCreate = productOrderRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(productOrder))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkPlacedDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = productOrderRepository.findAll().collectList().block().size();
        // set the field null
        productOrder.setPlacedDate(null);

        // Create the ProductOrder, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(productOrder))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = productOrderRepository.findAll().collectList().block().size();
        // set the field null
        productOrder.setStatus(null);

        // Create the ProductOrder, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(productOrder))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = productOrderRepository.findAll().collectList().block().size();
        // set the field null
        productOrder.setCode(null);

        // Create the ProductOrder, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(productOrder))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllProductOrders() {
        // Initialize the database
        productOrderRepository.save(productOrder).block();

        // Get all the productOrderList
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
            .value(hasItem(productOrder.getId().intValue()))
            .jsonPath("$.[*].placedDate")
            .value(hasItem(DEFAULT_PLACED_DATE.toString()))
            .jsonPath("$.[*].status")
            .value(hasItem(DEFAULT_STATUS.toString()))
            .jsonPath("$.[*].code")
            .value(hasItem(DEFAULT_CODE));
    }

    @Test
    void getProductOrder() {
        // Initialize the database
        productOrderRepository.save(productOrder).block();

        // Get the productOrder
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, productOrder.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(productOrder.getId().intValue()))
            .jsonPath("$.placedDate")
            .value(is(DEFAULT_PLACED_DATE.toString()))
            .jsonPath("$.status")
            .value(is(DEFAULT_STATUS.toString()))
            .jsonPath("$.code")
            .value(is(DEFAULT_CODE));
    }

    @Test
    void getNonExistingProductOrder() {
        // Get the productOrder
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewProductOrder() throws Exception {
        // Initialize the database
        productOrderRepository.save(productOrder).block();

        int databaseSizeBeforeUpdate = productOrderRepository.findAll().collectList().block().size();

        // Update the productOrder
        ProductOrder updatedProductOrder = productOrderRepository.findById(productOrder.getId()).block();
        updatedProductOrder.placedDate(UPDATED_PLACED_DATE).status(UPDATED_STATUS).code(UPDATED_CODE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedProductOrder.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedProductOrder))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeUpdate);
        ProductOrder testProductOrder = productOrderList.get(productOrderList.size() - 1);
        assertThat(testProductOrder.getPlacedDate()).isEqualTo(UPDATED_PLACED_DATE);
        assertThat(testProductOrder.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testProductOrder.getCode()).isEqualTo(UPDATED_CODE);
    }

    @Test
    void putNonExistingProductOrder() throws Exception {
        int databaseSizeBeforeUpdate = productOrderRepository.findAll().collectList().block().size();
        productOrder.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, productOrder.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(productOrder))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchProductOrder() throws Exception {
        int databaseSizeBeforeUpdate = productOrderRepository.findAll().collectList().block().size();
        productOrder.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(productOrder))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamProductOrder() throws Exception {
        int databaseSizeBeforeUpdate = productOrderRepository.findAll().collectList().block().size();
        productOrder.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(productOrder))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateProductOrderWithPatch() throws Exception {
        // Initialize the database
        productOrderRepository.save(productOrder).block();

        int databaseSizeBeforeUpdate = productOrderRepository.findAll().collectList().block().size();

        // Update the productOrder using partial update
        ProductOrder partialUpdatedProductOrder = new ProductOrder();
        partialUpdatedProductOrder.setId(productOrder.getId());

        partialUpdatedProductOrder.placedDate(UPDATED_PLACED_DATE).status(UPDATED_STATUS).code(UPDATED_CODE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedProductOrder.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedProductOrder))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeUpdate);
        ProductOrder testProductOrder = productOrderList.get(productOrderList.size() - 1);
        assertThat(testProductOrder.getPlacedDate()).isEqualTo(UPDATED_PLACED_DATE);
        assertThat(testProductOrder.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testProductOrder.getCode()).isEqualTo(UPDATED_CODE);
    }

    @Test
    void fullUpdateProductOrderWithPatch() throws Exception {
        // Initialize the database
        productOrderRepository.save(productOrder).block();

        int databaseSizeBeforeUpdate = productOrderRepository.findAll().collectList().block().size();

        // Update the productOrder using partial update
        ProductOrder partialUpdatedProductOrder = new ProductOrder();
        partialUpdatedProductOrder.setId(productOrder.getId());

        partialUpdatedProductOrder.placedDate(UPDATED_PLACED_DATE).status(UPDATED_STATUS).code(UPDATED_CODE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedProductOrder.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedProductOrder))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeUpdate);
        ProductOrder testProductOrder = productOrderList.get(productOrderList.size() - 1);
        assertThat(testProductOrder.getPlacedDate()).isEqualTo(UPDATED_PLACED_DATE);
        assertThat(testProductOrder.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testProductOrder.getCode()).isEqualTo(UPDATED_CODE);
    }

    @Test
    void patchNonExistingProductOrder() throws Exception {
        int databaseSizeBeforeUpdate = productOrderRepository.findAll().collectList().block().size();
        productOrder.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, productOrder.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(productOrder))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchProductOrder() throws Exception {
        int databaseSizeBeforeUpdate = productOrderRepository.findAll().collectList().block().size();
        productOrder.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(productOrder))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamProductOrder() throws Exception {
        int databaseSizeBeforeUpdate = productOrderRepository.findAll().collectList().block().size();
        productOrder.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(productOrder))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteProductOrder() {
        // Initialize the database
        productOrderRepository.save(productOrder).block();

        int databaseSizeBeforeDelete = productOrderRepository.findAll().collectList().block().size();

        // Delete the productOrder
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, productOrder.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<ProductOrder> productOrderList = productOrderRepository.findAll().collectList().block();
        assertThat(productOrderList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

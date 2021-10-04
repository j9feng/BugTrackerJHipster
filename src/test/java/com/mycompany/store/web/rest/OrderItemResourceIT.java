package com.mycompany.store.web.rest;

import static com.mycompany.store.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.mycompany.store.IntegrationTest;
import com.mycompany.store.domain.OrderItem;
import com.mycompany.store.domain.enumeration.OrderItemStatus;
import com.mycompany.store.repository.OrderItemRepository;
import com.mycompany.store.service.EntityManager;
import java.math.BigDecimal;
import java.time.Duration;
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
 * Integration tests for the {@link OrderItemResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class OrderItemResourceIT {

    private static final Integer DEFAULT_QUANTITY = 0;
    private static final Integer UPDATED_QUANTITY = 1;

    private static final BigDecimal DEFAULT_TOTAL_PRICE = new BigDecimal(0);
    private static final BigDecimal UPDATED_TOTAL_PRICE = new BigDecimal(1);

    private static final OrderItemStatus DEFAULT_STATUS = OrderItemStatus.AVAILABLE;
    private static final OrderItemStatus UPDATED_STATUS = OrderItemStatus.OUT_OF_STOCK;

    private static final String ENTITY_API_URL = "/api/order-items";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private OrderItem orderItem;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static OrderItem createEntity(EntityManager em) {
        OrderItem orderItem = new OrderItem().quantity(DEFAULT_QUANTITY).totalPrice(DEFAULT_TOTAL_PRICE).status(DEFAULT_STATUS);
        return orderItem;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static OrderItem createUpdatedEntity(EntityManager em) {
        OrderItem orderItem = new OrderItem().quantity(UPDATED_QUANTITY).totalPrice(UPDATED_TOTAL_PRICE).status(UPDATED_STATUS);
        return orderItem;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(OrderItem.class).block();
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
        orderItem = createEntity(em);
    }

    @Test
    void createOrderItem() throws Exception {
        int databaseSizeBeforeCreate = orderItemRepository.findAll().collectList().block().size();
        // Create the OrderItem
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(orderItem))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeCreate + 1);
        OrderItem testOrderItem = orderItemList.get(orderItemList.size() - 1);
        assertThat(testOrderItem.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testOrderItem.getTotalPrice()).isEqualByComparingTo(DEFAULT_TOTAL_PRICE);
        assertThat(testOrderItem.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    void createOrderItemWithExistingId() throws Exception {
        // Create the OrderItem with an existing ID
        orderItem.setId(1L);

        int databaseSizeBeforeCreate = orderItemRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(orderItem))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = orderItemRepository.findAll().collectList().block().size();
        // set the field null
        orderItem.setQuantity(null);

        // Create the OrderItem, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(orderItem))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkTotalPriceIsRequired() throws Exception {
        int databaseSizeBeforeTest = orderItemRepository.findAll().collectList().block().size();
        // set the field null
        orderItem.setTotalPrice(null);

        // Create the OrderItem, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(orderItem))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = orderItemRepository.findAll().collectList().block().size();
        // set the field null
        orderItem.setStatus(null);

        // Create the OrderItem, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(orderItem))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllOrderItems() {
        // Initialize the database
        orderItemRepository.save(orderItem).block();

        // Get all the orderItemList
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
            .value(hasItem(orderItem.getId().intValue()))
            .jsonPath("$.[*].quantity")
            .value(hasItem(DEFAULT_QUANTITY))
            .jsonPath("$.[*].totalPrice")
            .value(hasItem(sameNumber(DEFAULT_TOTAL_PRICE)))
            .jsonPath("$.[*].status")
            .value(hasItem(DEFAULT_STATUS.toString()));
    }

    @Test
    void getOrderItem() {
        // Initialize the database
        orderItemRepository.save(orderItem).block();

        // Get the orderItem
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, orderItem.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(orderItem.getId().intValue()))
            .jsonPath("$.quantity")
            .value(is(DEFAULT_QUANTITY))
            .jsonPath("$.totalPrice")
            .value(is(sameNumber(DEFAULT_TOTAL_PRICE)))
            .jsonPath("$.status")
            .value(is(DEFAULT_STATUS.toString()));
    }

    @Test
    void getNonExistingOrderItem() {
        // Get the orderItem
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewOrderItem() throws Exception {
        // Initialize the database
        orderItemRepository.save(orderItem).block();

        int databaseSizeBeforeUpdate = orderItemRepository.findAll().collectList().block().size();

        // Update the orderItem
        OrderItem updatedOrderItem = orderItemRepository.findById(orderItem.getId()).block();
        updatedOrderItem.quantity(UPDATED_QUANTITY).totalPrice(UPDATED_TOTAL_PRICE).status(UPDATED_STATUS);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedOrderItem.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedOrderItem))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate);
        OrderItem testOrderItem = orderItemList.get(orderItemList.size() - 1);
        assertThat(testOrderItem.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testOrderItem.getTotalPrice()).isEqualTo(UPDATED_TOTAL_PRICE);
        assertThat(testOrderItem.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    void putNonExistingOrderItem() throws Exception {
        int databaseSizeBeforeUpdate = orderItemRepository.findAll().collectList().block().size();
        orderItem.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, orderItem.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(orderItem))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchOrderItem() throws Exception {
        int databaseSizeBeforeUpdate = orderItemRepository.findAll().collectList().block().size();
        orderItem.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(orderItem))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamOrderItem() throws Exception {
        int databaseSizeBeforeUpdate = orderItemRepository.findAll().collectList().block().size();
        orderItem.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(orderItem))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateOrderItemWithPatch() throws Exception {
        // Initialize the database
        orderItemRepository.save(orderItem).block();

        int databaseSizeBeforeUpdate = orderItemRepository.findAll().collectList().block().size();

        // Update the orderItem using partial update
        OrderItem partialUpdatedOrderItem = new OrderItem();
        partialUpdatedOrderItem.setId(orderItem.getId());

        partialUpdatedOrderItem.quantity(UPDATED_QUANTITY).status(UPDATED_STATUS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedOrderItem.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedOrderItem))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate);
        OrderItem testOrderItem = orderItemList.get(orderItemList.size() - 1);
        assertThat(testOrderItem.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testOrderItem.getTotalPrice()).isEqualByComparingTo(DEFAULT_TOTAL_PRICE);
        assertThat(testOrderItem.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    void fullUpdateOrderItemWithPatch() throws Exception {
        // Initialize the database
        orderItemRepository.save(orderItem).block();

        int databaseSizeBeforeUpdate = orderItemRepository.findAll().collectList().block().size();

        // Update the orderItem using partial update
        OrderItem partialUpdatedOrderItem = new OrderItem();
        partialUpdatedOrderItem.setId(orderItem.getId());

        partialUpdatedOrderItem.quantity(UPDATED_QUANTITY).totalPrice(UPDATED_TOTAL_PRICE).status(UPDATED_STATUS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedOrderItem.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedOrderItem))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate);
        OrderItem testOrderItem = orderItemList.get(orderItemList.size() - 1);
        assertThat(testOrderItem.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testOrderItem.getTotalPrice()).isEqualByComparingTo(UPDATED_TOTAL_PRICE);
        assertThat(testOrderItem.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    void patchNonExistingOrderItem() throws Exception {
        int databaseSizeBeforeUpdate = orderItemRepository.findAll().collectList().block().size();
        orderItem.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, orderItem.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(orderItem))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchOrderItem() throws Exception {
        int databaseSizeBeforeUpdate = orderItemRepository.findAll().collectList().block().size();
        orderItem.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(orderItem))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamOrderItem() throws Exception {
        int databaseSizeBeforeUpdate = orderItemRepository.findAll().collectList().block().size();
        orderItem.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(orderItem))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteOrderItem() {
        // Initialize the database
        orderItemRepository.save(orderItem).block();

        int databaseSizeBeforeDelete = orderItemRepository.findAll().collectList().block().size();

        // Delete the orderItem
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, orderItem.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<OrderItem> orderItemList = orderItemRepository.findAll().collectList().block();
        assertThat(orderItemList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

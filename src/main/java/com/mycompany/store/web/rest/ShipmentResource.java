package com.mycompany.store.web.rest;

import com.mycompany.store.domain.Shipment;
import com.mycompany.store.repository.ShipmentRepository;
import com.mycompany.store.service.ShipmentService;
import com.mycompany.store.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.store.domain.Shipment}.
 */
@RestController
@RequestMapping("/api")
public class ShipmentResource {

    private final Logger log = LoggerFactory.getLogger(ShipmentResource.class);

    private static final String ENTITY_NAME = "shipment";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ShipmentService shipmentService;

    private final ShipmentRepository shipmentRepository;

    public ShipmentResource(ShipmentService shipmentService, ShipmentRepository shipmentRepository) {
        this.shipmentService = shipmentService;
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * {@code POST  /shipments} : Create a new shipment.
     *
     * @param shipment the shipment to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new shipment, or with status {@code 400 (Bad Request)} if the shipment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/shipments")
    public Mono<ResponseEntity<Shipment>> createShipment(@Valid @RequestBody Shipment shipment) throws URISyntaxException {
        log.debug("REST request to save Shipment : {}", shipment);
        if (shipment.getId() != null) {
            throw new BadRequestAlertException("A new shipment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return shipmentService
            .save(shipment)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/shipments/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /shipments/:id} : Updates an existing shipment.
     *
     * @param id the id of the shipment to save.
     * @param shipment the shipment to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shipment,
     * or with status {@code 400 (Bad Request)} if the shipment is not valid,
     * or with status {@code 500 (Internal Server Error)} if the shipment couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/shipments/{id}")
    public Mono<ResponseEntity<Shipment>> updateShipment(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Shipment shipment
    ) throws URISyntaxException {
        log.debug("REST request to update Shipment : {}, {}", id, shipment);
        if (shipment.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, shipment.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return shipmentRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return shipmentService
                    .save(shipment)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /shipments/:id} : Partial updates given fields of an existing shipment, field will ignore if it is null
     *
     * @param id the id of the shipment to save.
     * @param shipment the shipment to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shipment,
     * or with status {@code 400 (Bad Request)} if the shipment is not valid,
     * or with status {@code 404 (Not Found)} if the shipment is not found,
     * or with status {@code 500 (Internal Server Error)} if the shipment couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/shipments/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Shipment>> partialUpdateShipment(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Shipment shipment
    ) throws URISyntaxException {
        log.debug("REST request to partial update Shipment partially : {}, {}", id, shipment);
        if (shipment.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, shipment.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return shipmentRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Shipment> result = shipmentService.partialUpdate(shipment);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /shipments} : get all the shipments.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of shipments in body.
     */
    @GetMapping("/shipments")
    public Mono<ResponseEntity<List<Shipment>>> getAllShipments(Pageable pageable, ServerHttpRequest request) {
        log.debug("REST request to get a page of Shipments");
        return shipmentService
            .countAll()
            .zipWith(shipmentService.findAll(pageable).collectList())
            .map(countWithEntities -> {
                return ResponseEntity
                    .ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            UriComponentsBuilder.fromHttpRequest(request),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2());
            });
    }

    /**
     * {@code GET  /shipments/:id} : get the "id" shipment.
     *
     * @param id the id of the shipment to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the shipment, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/shipments/{id}")
    public Mono<ResponseEntity<Shipment>> getShipment(@PathVariable Long id) {
        log.debug("REST request to get Shipment : {}", id);
        Mono<Shipment> shipment = shipmentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(shipment);
    }

    /**
     * {@code DELETE  /shipments/:id} : delete the "id" shipment.
     *
     * @param id the id of the shipment to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/shipments/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteShipment(@PathVariable Long id) {
        log.debug("REST request to delete Shipment : {}", id);
        return shipmentService
            .delete(id)
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                    .build()
            );
    }
}

package com.mycompany.store.repository;

import com.mycompany.store.domain.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the OrderItem entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long>, OrderItemRepositoryInternal {
    Flux<OrderItem> findAllBy(Pageable pageable);

    @Query("SELECT * FROM order_item entity WHERE entity.product_id = :id")
    Flux<OrderItem> findByProduct(Long id);

    @Query("SELECT * FROM order_item entity WHERE entity.product_id IS NULL")
    Flux<OrderItem> findAllWhereProductIsNull();

    @Query("SELECT * FROM order_item entity WHERE entity.order_id = :id")
    Flux<OrderItem> findByOrder(Long id);

    @Query("SELECT * FROM order_item entity WHERE entity.order_id IS NULL")
    Flux<OrderItem> findAllWhereOrderIsNull();

    // just to avoid having unambigous methods
    @Override
    Flux<OrderItem> findAll();

    @Override
    Mono<OrderItem> findById(Long id);

    @Override
    <S extends OrderItem> Mono<S> save(S entity);
}

interface OrderItemRepositoryInternal {
    <S extends OrderItem> Mono<S> insert(S entity);
    <S extends OrderItem> Mono<S> save(S entity);
    Mono<Integer> update(OrderItem entity);

    Flux<OrderItem> findAll();
    Mono<OrderItem> findById(Long id);
    Flux<OrderItem> findAllBy(Pageable pageable);
    Flux<OrderItem> findAllBy(Pageable pageable, Criteria criteria);
}

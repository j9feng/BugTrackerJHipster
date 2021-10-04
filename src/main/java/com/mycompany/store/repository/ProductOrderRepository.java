package com.mycompany.store.repository;

import com.mycompany.store.domain.ProductOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the ProductOrder entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProductOrderRepository extends R2dbcRepository<ProductOrder, Long>, ProductOrderRepositoryInternal {
    Flux<ProductOrder> findAllBy(Pageable pageable);

    @Query("SELECT * FROM product_order entity WHERE entity.customer_id = :id")
    Flux<ProductOrder> findByCustomer(Long id);

    @Query("SELECT * FROM product_order entity WHERE entity.customer_id IS NULL")
    Flux<ProductOrder> findAllWhereCustomerIsNull();

    // just to avoid having unambigous methods
    @Override
    Flux<ProductOrder> findAll();

    @Override
    Mono<ProductOrder> findById(Long id);

    @Override
    <S extends ProductOrder> Mono<S> save(S entity);
}

interface ProductOrderRepositoryInternal {
    <S extends ProductOrder> Mono<S> insert(S entity);
    <S extends ProductOrder> Mono<S> save(S entity);
    Mono<Integer> update(ProductOrder entity);

    Flux<ProductOrder> findAll();
    Mono<ProductOrder> findById(Long id);
    Flux<ProductOrder> findAllBy(Pageable pageable);
    Flux<ProductOrder> findAllBy(Pageable pageable, Criteria criteria);
}

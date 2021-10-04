package com.mycompany.store.repository;

import com.mycompany.store.domain.Invoice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Invoice entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InvoiceRepository extends R2dbcRepository<Invoice, Long>, InvoiceRepositoryInternal {
    Flux<Invoice> findAllBy(Pageable pageable);

    @Query("SELECT * FROM invoice entity WHERE entity.order_id = :id")
    Flux<Invoice> findByOrder(Long id);

    @Query("SELECT * FROM invoice entity WHERE entity.order_id IS NULL")
    Flux<Invoice> findAllWhereOrderIsNull();

    // just to avoid having unambigous methods
    @Override
    Flux<Invoice> findAll();

    @Override
    Mono<Invoice> findById(Long id);

    @Override
    <S extends Invoice> Mono<S> save(S entity);
}

interface InvoiceRepositoryInternal {
    <S extends Invoice> Mono<S> insert(S entity);
    <S extends Invoice> Mono<S> save(S entity);
    Mono<Integer> update(Invoice entity);

    Flux<Invoice> findAll();
    Mono<Invoice> findById(Long id);
    Flux<Invoice> findAllBy(Pageable pageable);
    Flux<Invoice> findAllBy(Pageable pageable, Criteria criteria);
}

package com.mycompany.store.repository;

import com.mycompany.store.domain.ProductCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the ProductCategory entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProductCategoryRepository extends R2dbcRepository<ProductCategory, Long>, ProductCategoryRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<ProductCategory> findAll();

    @Override
    Mono<ProductCategory> findById(Long id);

    @Override
    <S extends ProductCategory> Mono<S> save(S entity);
}

interface ProductCategoryRepositoryInternal {
    <S extends ProductCategory> Mono<S> insert(S entity);
    <S extends ProductCategory> Mono<S> save(S entity);
    Mono<Integer> update(ProductCategory entity);

    Flux<ProductCategory> findAll();
    Mono<ProductCategory> findById(Long id);
    Flux<ProductCategory> findAllBy(Pageable pageable);
    Flux<ProductCategory> findAllBy(Pageable pageable, Criteria criteria);
}

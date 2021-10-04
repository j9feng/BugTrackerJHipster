package com.mycompany.store.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import com.mycompany.store.domain.Shipment;
import com.mycompany.store.repository.rowmapper.InvoiceRowMapper;
import com.mycompany.store.repository.rowmapper.ShipmentRowMapper;
import com.mycompany.store.service.EntityManager;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive custom repository implementation for the Shipment entity.
 */
@SuppressWarnings("unused")
class ShipmentRepositoryInternalImpl implements ShipmentRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final InvoiceRowMapper invoiceMapper;
    private final ShipmentRowMapper shipmentMapper;

    private static final Table entityTable = Table.aliased("shipment", EntityManager.ENTITY_ALIAS);
    private static final Table invoiceTable = Table.aliased("invoice", "invoice");

    public ShipmentRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        InvoiceRowMapper invoiceMapper,
        ShipmentRowMapper shipmentMapper
    ) {
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.invoiceMapper = invoiceMapper;
        this.shipmentMapper = shipmentMapper;
    }

    @Override
    public Flux<Shipment> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<Shipment> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<Shipment> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = ShipmentSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(InvoiceSqlHelper.getColumns(invoiceTable, "invoice"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(invoiceTable)
            .on(Column.create("invoice_id", entityTable))
            .equals(Column.create("id", invoiceTable));

        String select = entityManager.createSelect(selectFrom, Shipment.class, pageable, criteria);
        String alias = entityTable.getReferenceName().getReference();
        String selectWhere = Optional
            .ofNullable(criteria)
            .map(crit ->
                new StringBuilder(select)
                    .append(" ")
                    .append("WHERE")
                    .append(" ")
                    .append(alias)
                    .append(".")
                    .append(crit.toString())
                    .toString()
            )
            .orElse(select); // TODO remove once https://github.com/spring-projects/spring-data-jdbc/issues/907 will be fixed
        return db.sql(selectWhere).map(this::process);
    }

    @Override
    public Flux<Shipment> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<Shipment> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    private Shipment process(Row row, RowMetadata metadata) {
        Shipment entity = shipmentMapper.apply(row, "e");
        entity.setInvoice(invoiceMapper.apply(row, "invoice"));
        return entity;
    }

    @Override
    public <S extends Shipment> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    @Override
    public <S extends Shipment> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity);
        } else {
            return update(entity)
                .map(numberOfUpdates -> {
                    if (numberOfUpdates.intValue() <= 0) {
                        throw new IllegalStateException("Unable to update Shipment with id = " + entity.getId());
                    }
                    return entity;
                });
        }
    }

    @Override
    public Mono<Integer> update(Shipment entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }
}

class ShipmentSqlHelper {

    static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("tracking_code", table, columnPrefix + "_tracking_code"));
        columns.add(Column.aliased("date", table, columnPrefix + "_date"));
        columns.add(Column.aliased("details", table, columnPrefix + "_details"));

        columns.add(Column.aliased("invoice_id", table, columnPrefix + "_invoice_id"));
        return columns;
    }
}

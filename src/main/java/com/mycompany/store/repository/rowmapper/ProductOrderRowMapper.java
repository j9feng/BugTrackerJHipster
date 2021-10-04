package com.mycompany.store.repository.rowmapper;

import com.mycompany.store.domain.ProductOrder;
import com.mycompany.store.domain.enumeration.OrderStatus;
import com.mycompany.store.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link ProductOrder}, with proper type conversions.
 */
@Service
public class ProductOrderRowMapper implements BiFunction<Row, String, ProductOrder> {

    private final ColumnConverter converter;

    public ProductOrderRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link ProductOrder} stored in the database.
     */
    @Override
    public ProductOrder apply(Row row, String prefix) {
        ProductOrder entity = new ProductOrder();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setPlacedDate(converter.fromRow(row, prefix + "_placed_date", Instant.class));
        entity.setStatus(converter.fromRow(row, prefix + "_status", OrderStatus.class));
        entity.setCode(converter.fromRow(row, prefix + "_code", String.class));
        entity.setCustomerId(converter.fromRow(row, prefix + "_customer_id", Long.class));
        return entity;
    }
}

package com.mycompany.store.service;

import com.mycompany.store.domain.Invoice;
import com.mycompany.store.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Invoice}.
 */
@Service
@Transactional
public class InvoiceService {

    private final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Save a invoice.
     *
     * @param invoice the entity to save.
     * @return the persisted entity.
     */
    public Mono<Invoice> save(Invoice invoice) {
        log.debug("Request to save Invoice : {}", invoice);
        return invoiceRepository.save(invoice);
    }

    /**
     * Partially update a invoice.
     *
     * @param invoice the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Invoice> partialUpdate(Invoice invoice) {
        log.debug("Request to partially update Invoice : {}", invoice);

        return invoiceRepository
            .findById(invoice.getId())
            .map(existingInvoice -> {
                if (invoice.getDate() != null) {
                    existingInvoice.setDate(invoice.getDate());
                }
                if (invoice.getDetails() != null) {
                    existingInvoice.setDetails(invoice.getDetails());
                }
                if (invoice.getStatus() != null) {
                    existingInvoice.setStatus(invoice.getStatus());
                }
                if (invoice.getPaymentMethod() != null) {
                    existingInvoice.setPaymentMethod(invoice.getPaymentMethod());
                }
                if (invoice.getPaymentDate() != null) {
                    existingInvoice.setPaymentDate(invoice.getPaymentDate());
                }
                if (invoice.getPaymentAmount() != null) {
                    existingInvoice.setPaymentAmount(invoice.getPaymentAmount());
                }

                return existingInvoice;
            })
            .flatMap(invoiceRepository::save);
    }

    /**
     * Get all the invoices.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Invoice> findAll(Pageable pageable) {
        log.debug("Request to get all Invoices");
        return invoiceRepository.findAllBy(pageable);
    }

    /**
     * Returns the number of invoices available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return invoiceRepository.count();
    }

    /**
     * Get one invoice by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<Invoice> findOne(Long id) {
        log.debug("Request to get Invoice : {}", id);
        return invoiceRepository.findById(id);
    }

    /**
     * Delete the invoice by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Invoice : {}", id);
        return invoiceRepository.deleteById(id);
    }
}

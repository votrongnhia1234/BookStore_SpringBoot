package com.example.book_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.book_management.models.ItemInvoice;

@Repository
public interface IItemInvoiceRepository extends
                JpaRepository<ItemInvoice, Long> {
}

package com.example.book_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.book_management.models.Invoice;

@Repository
public interface IInvoiceRepository extends JpaRepository<Invoice, Long> {
}

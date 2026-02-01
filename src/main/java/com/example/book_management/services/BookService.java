package com.example.book_management.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import com.example.book_management.models.Book;
import com.example.book_management.repositories.IBookRepository;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
public class BookService {
    private final IBookRepository bookRepository;

    // Get all books
    public List<Book> getAllBooks(
            Integer pageNo,
            Integer pageSize,
            String sortBy) {
        return bookRepository.findAllBooks(pageNo, pageSize, sortBy);
    }

    // Get a book by ID
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    // add a new book
    public void addBook(Book book) {
        bookRepository.save(book);
    }

    // delete a book by ID
    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }

    // update an existing book use method stream
    public void updateBook(@NotNull Book book) {
        Book existingBook = bookRepository.findById(book.getId())
                .orElse(null);
        Objects.requireNonNull(existingBook).setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());
        existingBook.setCategory(book.getCategory());
        bookRepository.save(existingBook);
    }

    public List<Book> searchBook(String keyword) {
        return bookRepository.searchBook(keyword);
    }
}

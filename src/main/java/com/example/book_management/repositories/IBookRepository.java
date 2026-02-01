package com.example.book_management.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.example.book_management.models.Book;

@Repository
public interface IBookRepository extends PagingAndSortingRepository<Book, Long>, JpaRepository<Book, Long> {
    default List<Book> findAllBooks(
            Integer pageNo,
            Integer pageSize,
            String sortBy) {
        return findAll(PageRequest.of(pageNo, pageSize, Sort.by(sortBy))).getContent();
    }

    @Query("""
                SELECT b FROM Book b
                WHERE b.title LIKE %?1%
                OR b.author LIKE %?1%
                OR b.category.name LIKE %?1%
            """)
    List<Book> searchBook(String keyword);

}

package com.example.book_management.repositories;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.book_management.models.Category;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, Long> {
    default List<Category> findAllCategories(Integer pageNo,
            Integer pageSize,
            String sortBy) {
        return findAll(PageRequest.of(pageNo, pageSize, Sort.by(sortBy))).getContent();
    }
}

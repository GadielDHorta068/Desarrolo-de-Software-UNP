package com.desarrollo.raffy.business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.Categories;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, Long>{
    
    public Categories findByName(String name);

    public boolean existsByName(String name);

    @Query("SELECT c FROM Categories c ORDER BY c.name ASC")
    public List<Categories> findAllByOrderByNameAsc();
}

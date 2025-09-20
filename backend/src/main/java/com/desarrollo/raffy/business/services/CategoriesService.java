package com.desarrollo.raffy.business.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.CategoriesRepository;
import com.desarrollo.raffy.model.Categories;

@Service
public class CategoriesService {
    
    @Autowired
    private CategoriesRepository categoriesRepository;

    @Transactional
    public Categories save(Categories categories){
        if(categoriesRepository.existsByName(categories.getName())){
            throw new RuntimeException("La categoría con nombre '" + categories.getName() + "' ya existe.");
        }
        return categoriesRepository.save(categories);
    }

    
    public Categories findById(Long id){
        Optional<Categories> categories = categoriesRepository.findById(id);
        if(categories.isEmpty()){
            throw new RuntimeException("Categoría no encontrada");
        }
        return categories.get();
    }

    
    public Categories findByName(String name){
        return categoriesRepository.findByName(name);
    }

    public List<Categories> findAll(){
        return categoriesRepository.findAllByOrderByNameAsc();
    }

    
}

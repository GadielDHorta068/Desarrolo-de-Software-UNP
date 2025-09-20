package com.desarrollo.raffy.presenter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.services.CategoriesService;
import com.desarrollo.raffy.model.Categories;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/categories")
public class CategoriesController {
    
    @Autowired
    private CategoriesService categoriesService;

    @PostMapping("/save")
    public ResponseEntity<Categories> save(@Valid @RequestBody Categories categories){
        Categories saveCategories = categoriesService.save(categories);
            return new ResponseEntity<>(saveCategories, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable @NotNull @Positive Long id){
        if (id <= 0) {
            return new ResponseEntity<>("No se encontro la categoria", HttpStatus.BAD_REQUEST);
        }

        Categories categories = categoriesService.findById(id);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    

    @GetMapping("/searchAll")
    public ResponseEntity<?> findAllCategories(){
        List<Categories> categories = categoriesService.findAll();
        if(categories.isEmpty()){
            return new ResponseEntity<>("No se encontraron categorias", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }
}

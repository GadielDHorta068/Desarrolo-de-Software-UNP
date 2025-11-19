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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categorías", description = "Gestión de categorías de eventos")
public class CategoriesController {
    
    @Autowired
    private CategoriesService categoriesService;

    @PostMapping("/save")
    @Operation(summary = "Guardar categoría", description = "Crea una nueva categoría")
    public ResponseEntity<Categories> save(@Valid @RequestBody Categories categories){
        Categories saveCategories = categoriesService.save(categories);
            return new ResponseEntity<>(saveCategories, HttpStatus.CREATED);
    }

    // Ruta alternativa para compatibilidad: POST /categories
    @PostMapping("")
    @Operation(summary = "Guardar categoría (compat)", description = "Ruta alternativa para crear categoría")
    public ResponseEntity<Categories> saveRoot(@Valid @RequestBody Categories categories){
        Categories saveCategories = categoriesService.save(categories);
        return new ResponseEntity<>(saveCategories, HttpStatus.CREATED);
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Categoría por ID", description = "Obtiene una categoría por su identificador")
    public ResponseEntity<?> findById(@PathVariable @NotNull @Positive Long id){
        if (id <= 0) {
            return new ResponseEntity<>("No se encontro la categoria", HttpStatus.BAD_REQUEST);
        }

        Categories categories = categoriesService.findById(id);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // Ruta alternativa para compatibilidad: GET /categories/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Categoría por ID (compat)", description = "Ruta alternativa para obtener categoría por ID")
    public ResponseEntity<?> findByIdAlt(@PathVariable @NotNull @Positive Long id){
        if (id <= 0) {
            return new ResponseEntity<>("No se encontro la categoria", HttpStatus.BAD_REQUEST);
        }
        Categories categories = categoriesService.findById(id);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/searchAll")
    @Operation(summary = "Listar categorías", description = "Lista todas las categorías disponibles")
    public ResponseEntity<?> findAllCategories(){
        List<Categories> categories = categoriesService.findAll();
        if(categories.isEmpty()){
            return new ResponseEntity<>("No se encontraron categorias", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }
}

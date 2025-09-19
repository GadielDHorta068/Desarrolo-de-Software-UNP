package com.desarrollo.raffy.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.services.CategoriesService;

@RestController
@RequestMapping("categories")
public class CategoriesController {
    
    @Autowired
    private CategoriesService categoriesService;
}

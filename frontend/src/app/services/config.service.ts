import { Injectable } from '@angular/core';
import { Category, CategoryService } from './category.service';

@Injectable({
  providedIn: 'root'
})
export class configService {

  // esto deberia ser un OBSERVER
  categories: Category[] = [];

  constructor(
    private categoryService: CategoryService
  ) {}

  initData(){
    // recuperamos todos las categorias
    this.categoryService.getAll().subscribe({
      next: (response) => {
        // console.log('[initConfig] => categorias recuperadas: ', response);
        this.categories = response;
      },
      error: (error) => {
        // Usar el mensaje específico del backend si está disponible
        // this.errorMessage = error.userMessage || 'Error al registrar usuario. Por favor, intenta de nuevo.';
        console.warn('[Eventos]: error al recuperar las categorias: ', error);
      }
    });
  }

  public getAllCategories(){
    return this.categories;
  }
}
import { Injectable } from '@angular/core';
import { Category, CategoryService } from './category.service';
import { EventsService } from './events.service';
import { BehaviorSubject } from 'rxjs';
import { EventType } from '../models/events.model';

@Injectable({
  providedIn: 'root'
})
export class configService {

  // esto deberia ser un OBSERVER
  // categories: Category[] = [];
  private categoriesSubject = new BehaviorSubject<Category[] | null>(null);
  public categories$ = this.categoriesSubject.asObservable();
  // esto deberia ser un OBSERVER
  // typesEvent: string[] = [];
  private typeEventsSubject = new BehaviorSubject<EventType[] | null>(null);
  public typeEvents$ = this.typeEventsSubject.asObservable();

  constructor(
    private categoryService: CategoryService,
    private eventService: EventsService
  ) {}

  initData(){
    // recuperamos todos las categorias
    this.categoryService.getAll().subscribe({
      next: (response) => {
        // console.log('[initConfig] => categorias recuperadas: ', response);
        // this.categories = response;
        this.setCategories(response);
      },
      error: (error) => {
        // Usar el mensaje específico del backend si está disponible
        // this.errorMessage = error.userMessage || 'Error al registrar usuario. Por favor, intenta de nuevo.';
        console.warn('[Eventos]: error al recuperar las categorias: ', error);
      }
    });
    // recuperamos todos los tipos de eventos
    this.eventService.getTypesEvent().subscribe({
      next: (response) => {
        console.log('[initConfig] => tipos de eventos recuperados: ', response);
        this.setTypesEvent(this.parseTypeEvents(response));
        console.log('[initConfig] => tipos de eventos recuperados: ', this.typeEventsSubject.getValue());
      },
      error: (error) => {
        // Usar el mensaje específico del backend si está disponible
        // this.errorMessage = error.userMessage || 'Error al registrar usuario. Por favor, intenta de nuevo.';
        console.warn('[Eventos]: error al recuperar las tipos de eventos: ', error);
      }
    });
  }

  public setCategories(categories: Category[]|null){
    this.categoriesSubject.next(categories ? categories : []);
  }

  public getCategories(){
    const categories = this.categoriesSubject.getValue();
    return categories ? categories : [];
  }

  public setTypesEvent(types: EventType[]|null){
    this.typeEventsSubject.next(types ? types : []);
  }

  public parseTypeEvents(nameEvents: string[]){
    if(nameEvents){
      const objTypes = nameEvents.map(name => this.getObjcType(name));
      console.log("[typeEvent] => array de objetos: ", objTypes);
      return objTypes;
    }
    return [];
  }

  // devuelve todos los tipos de eventos recuperados del server
  public getEventTypes(){
    const types = this.typeEventsSubject.getValue();
    return types ? types : [];
  }

  // public getAllCategories(){
  //   return this.categories;
  // }

  private getObjcType(codeType: string){
    let name: string = "";
    if(codeType == "GIVEAWAYS"){
      name = "SORTEO";
    }
    if(codeType == "GUESSING_CONTEST"){
      name = "ADIVINANZAS";
    }
    if(codeType == "RAFFLES"){
      name = "RIFA";
    }
    return {code: codeType, name: name};
  }
}
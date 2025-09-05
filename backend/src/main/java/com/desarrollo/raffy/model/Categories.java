package com.desarrollo.raffy.model;

public class Categories {
    
    private int id;

    private String name;


    public Categories(int id, String name){
        this.id = id;
        this.name = name;
    }

    public int getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }
}

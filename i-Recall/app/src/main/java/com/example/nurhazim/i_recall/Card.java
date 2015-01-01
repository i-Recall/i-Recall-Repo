package com.example.nurhazim.i_recall;

/**
 * Created by NurHazim on 27-Dec-14.
 */
public class Card {
    private int id;
    private String term;
    private String description;

    public Card(){
        id = 0;
        term = "";
        description = "";
    }

    public Card(int id, String term, String description){
        this.id = id;
        this.term = term;
        this.description = description;
    }

    public int getId(){
        return id;
    }

    public void setId(int newId){
        id = newId;
    }

    public String getTerm(){
        return term;
    }

    public void setTerm(String newTerm){
        term = newTerm;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String newDescription){
        description = newDescription;
    }
}

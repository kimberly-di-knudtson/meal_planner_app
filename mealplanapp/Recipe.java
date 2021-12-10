package edu.ucdenver.knudtson.mealplanapp;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.*;

public class Recipe {
    static final int MAX_KEYWORDS = 16;
    private long _id;
    private String title;
    private String ingredients;
    private CookingMethod cookingMethod;
    private String instructions;
    private String notes;
    private int prepHours;
    private int prepMinutes;
    private int cookHours;
    private int cookMinutes;

    //Do I need to initialize other parameters or not?
    public Recipe(long id){
        this._id = id;
        this.title = "";
        this.ingredients = "";
        this.cookingMethod = CookingMethod.NONE;
        this.instructions="";
        this.notes="";
        this.prepHours=0;
        this.prepMinutes=0;
        this.cookHours=0;
        this.cookMinutes=0;
    }

    public long getId() {
        return _id;
    }
    public String getTitle() {
        return title;
    }
    public String getIngredients() {return ingredients;}
    public String getInstructions() {
        return instructions;
    }
    public String getNotes() {
        return notes;
    }
    public CookingMethod getCookingMethod() {
        return cookingMethod;
    }
    public int getPrepHours() {
        return prepHours;
    }
    public int getPrepMinutes() {
        return prepMinutes;
    }
    public int getCookHours() {
        return cookHours;
    }
    public int getCookMinutes() {
        return cookMinutes;
    }

    public void setId(long _id) { this._id = _id; }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setIngredients (String ingredients) {this.ingredients = ingredients;}
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    public void setNotes(String instructions) {
        this.notes = notes;
    }
    public void setCookingMethod(CookingMethod cookingMethod) {
        this.cookingMethod = cookingMethod;
    }
    public void setPrepHours(int prepHours) {
        this.prepHours = prepHours;
    }
    public void setPrepMinutes(int prepMinutes) {
        this.prepMinutes = prepMinutes;
    }
    public void setCookHours(int cookHours) {
        this.cookHours = cookHours;
    }
    public void setCookMinutes(int cookMinutes) {
        this.cookMinutes = cookMinutes;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recipe)) return false;
        Recipe recipe = (Recipe) o;
        return _id == recipe._id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id);
    }
}

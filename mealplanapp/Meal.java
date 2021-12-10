package edu.ucdenver.knudtson.mealplanapp;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.*;
import static edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.MealColor.NONE;

public class Meal {
    static final int MAX_RECIPES = 8;

    private long _id;
    private String title;
    private Category category;
    private MealColor color;
    private long recipeListId;  //non-empty ref list gets an ID when persisted, data rx by ID from DB may not match in flight state of ref list
    private long[] references;

    public Meal(long id){
        this._id = id;
        this.title ="";
        this.category = Category.NONE;
        this.color = MealColor.NONE;
        this.recipeListId = 0;
        this.references = new long[MAX_RECIPES];
    }

    public void setId(long id) {this._id = id;}
    public void setTitle(String title) {
        this.title = title;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
    public void setColor(MealColor color) { this.color = color; }
    public void setRecipeListId(long recipeListId) {
        this.recipeListId = recipeListId;
    }
    public void setRecipeIdsArray(long [] recipeIds) {references = recipeIds;}
    public int addRecipeReference(long recipeId) {
        //find next empty reference
        int i = 0;
        boolean inserted = false;
        while (i<MAX_RECIPES) {
            if (references[i] == 0){
                references[i] = recipeId;
                inserted = true;
                break;
            }
            i++;
        }
        if (!inserted) {
            Log.w("Meal - AddRecipeReference", "Ref list full, not inserted");
            i=-1;
        }
        return i;
    }
    public int removeRecipeReference (long recipeId){
        int length = references.length;
        int i = 0;
        while (i<length){
            if (references[i] == recipeId){
                for(int j = i; j < length - 1; j++){
                    references[j] = references[j+1];
                }
                return (length-1);
            }
            i++;
        }
        return length;
    }

    public long getId() {
        return _id;
    }
    public String getTitle() {
        return title;
    }
    public Category getCategory(){
        return category;
    }
    public MealColor getColor() {
        return color;
    }
    public long getRecipeListId() {return recipeListId;}

    public long[] getRecipeIdArray(){return references;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Meal)) return false;
        Meal meal = (Meal) o;
        return (_id == meal._id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id);
    }
}

package edu.ucdenver.knudtson.mealplanapp;

import android.util.Log;

import java.time.LocalDate;
import java.util.ArrayList;

public class PlanDay {
    static final int MAX_MEALS = 8;
    static final String DATE_FORMAT = "EEEE, dd MMMM yyyy";

    private LocalDate date;
    private long mealListId; // I think I need to store this with PlanDay, else I lose it upon data loading
    private long[] references;

    public PlanDay(LocalDate date){
        this.date = date;
        this.mealListId=0;
        this.references = new long[8];
    }

    public LocalDate getDate(){return this.date;}
    public long getDaysSinceEpoch() {return this.date.toEpochDay();}
    public long getMealListId() {return this.mealListId;}

    public void setMealListId(long mealListId) {this.mealListId = mealListId;}

    public long[] getMealIdArray(){return references;}

    public void setMealIdsArray(long [] mealIds) {references = mealIds;
    Log.i("PlanDay", "Size of references: "+references.length);}
    public int addMealReference(long mealId) { //returns index
        int length = references.length;
        //find next empty reference
        int i = 0;
        boolean inserted = false;
        while (i<MAX_MEALS) {
            if (references[i] == 0){
                references[i] = mealId;
                inserted = true;
                break;
            }
            i++;
        }
        if (!inserted) {
            Log.w("PlanDay - AddMealReference", "Ref list full, not inserted");
            i=-1;
        }
        return i;
    }

 }

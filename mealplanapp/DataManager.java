package edu.ucdenver.knudtson.mealplanapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.LocaleList;
import android.provider.BaseColumns;
import android.util.Log;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;

import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.PlanDayEntry.*;
import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.MealListEntry.*;
import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.MealEntry.*;
import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.RecipeListEntry.*;
import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.RecipeEntry.*;
import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.KeywordListEntry.*;
import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.RecipeSearchEntry.*;
import static edu.ucdenver.knudtson.mealplanapp.Meal.MAX_RECIPES;
import static edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.*;
import static edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.PlanComponent.MEAL;
import static edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.PlanComponent.RECIPE;
import static edu.ucdenver.knudtson.mealplanapp.PlanDay.MAX_MEALS;
import static edu.ucdenver.knudtson.mealplanapp.Recipe.MAX_KEYWORDS;

public class DataManager {
    private SQLiteDatabase db;

    public DataManager (Context context){
        MealPlanDbHelper dbHelper = new MealPlanDbHelper(context);
        db = dbHelper.getWritableDatabase();
        Log.i("info", "In DataManager constructor");

    }

    private class MealPlanDbHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 2;
        public static final String DATABASE_NAME = "MealPlan.db";

        public MealPlanDbHelper(Context context) {
            //Call SQLiteOpenHelper constructor
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //Create table for storing contacts
            //best to put SQL exec in try/catch
            Log.i("info", "In MealPlanDbHelper class onCreate method");
            //Create order matters due to foreign keys - basic to complex
            //Create keyword list table
            try {
                db.execSQL(SQL_CREATE_KEYWORD_LIST_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_CREATE_KEYWORD_LIST_TABLE);
                Log.i("info", e.getMessage());
            }
            //Add keyword columns
            for(int i=0; i< MAX_KEYWORDS;) {
                try {
                    String cmd = "ALTER TABLE " + KEYWORD_LIST_TABLENAME +
                            " ADD COLUMN " + KEYWORD_COL_BASENAME + ++i +
                            " TEXT DEFAULT ''";
                    db.execSQL(cmd);
                } catch (SQLException e) {
                    Log.i("info", "In MealPlanDbHelper adding keyword columns");
                    Log.i("info", e.getMessage());
                }
            }

            //Create recipes table
            try {
                db.execSQL(SQL_CREATE_RECIPES_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_CREATE_RECIPES_TABLE);
                Log.i("info", e.getMessage());
            }

            //Create recipe list table
            try {
                db.execSQL(SQL_CREATE_RECIPE_LIST_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_CREATE_RECIPE_LIST_TABLE);
                Log.i("info", e.getMessage());
            }

            //Add keyword columns
            for(int i=0; i< MAX_RECIPES;) {
                try {
                    String cmd = "ALTER TABLE " + RECIPE_LIST_TABLENAME +
                            " ADD COLUMN " + RECIPE_ID_COL_BASENAME + ++i +
                            " INTEGER DEFAULT 0";
                    db.execSQL(cmd);
                } catch (SQLException e) {
                    Log.i("info", "In MealPlanDbHelper adding recipe id columns");
                    Log.i("info", e.getMessage());
                }
            }

            //Create recipe search table
            try {
                db.execSQL(SQL_CREATE_RECIPE_SEARCH_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_CREATE_RECIPE_SEARCH_TABLE);
                Log.i("info", e.getMessage());
            }

            //Create meals table
            try {
                db.execSQL(SQL_CREATE_MEALS_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_CREATE_MEALS_TABLE);
                Log.i("info", e.getMessage());
            }

            //Create meal list table
            try {
                db.execSQL(SQL_CREATE_MEAL_LIST_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_CREATE_MEAL_LIST_TABLE);
                Log.i("info", e.getMessage());
            }

            //Add meal columns
            for(int i=0; i< MAX_MEALS;) {
                try {
                    String cmd = "ALTER TABLE " + MEAL_LIST_TABLENAME +
                            " ADD COLUMN " + MEAL_ID_COL_BASENAME + ++i +
                            " INTEGER DEFAULT 0";
                    db.execSQL(cmd);
                } catch (SQLException e) {
                    Log.i("info", "In MealPlanDbHelper adding meal id columns");
                    Log.i("info", e.getMessage());
                }
            }

            //Create plan_days table
            try {
                db.execSQL(SQL_CREATE_PLAN_DAYS_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_CREATE_PLAN_DAYS_TABLE);
                Log.i("info", e.getMessage());
            }

            //Create index recipe search table
            try {
                db.execSQL(SQL_CREATE_RECIPE_SEARCH_TABLE_INDEX);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_CREATE_RECIPE_SEARCH_TABLE_INDEX);
                Log.i("info", e.getMessage());
            }

            //Create index plan day table
            try {
                db.execSQL(SQL_CREATE_PLAN_DAY_INDEX);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_CREATE_PLAN_DAY_INDEX);
                Log.i("info", e.getMessage());
            }
        }

        //Put these here so I can change DB version to start over
        @Override
        //Delete and re-create DB
        //Delete order matters due to foreign keys - complex to basic
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //Delete plan_days table
            try {
                db.execSQL(SQL_DELETE_PLAN_DAYS_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_DELETE_PLAN_DAYS_TABLE);
                Log.i("info", e.getMessage());
            }

            //Delete meal list table
            try {
                db.execSQL(SQL_DELETE_MEAL_LIST_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_DELETE_MEAL_LIST_TABLE);
                Log.i("info", e.getMessage());
            }

            //Delete meals table
            try {
                db.execSQL(SQL_DELETE_MEALS_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_DELETE_MEALS_TABLE);
                Log.i("info", e.getMessage());
            }

            //Delete recipe list table
            try {
                db.execSQL(SQL_DELETE_RECIPE_LIST_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_DELETE_RECIPE_LIST_TABLE);
                Log.i("info", e.getMessage());
            }

            //Delete recipe search table
            try {
                db.execSQL(SQL_DELETE_RECIPE_SEARCH_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_DELETE_RECIPE_SEARCH_TABLE);
                Log.i("info", e.getMessage());
            }

            //Delete recipes table
            /*
            try {
                db.execSQL(SQL_DELETE_RECIPES_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_DELETE_RECIPES_TABLE);
                Log.i("info", e.getMessage());
            }*/

            //Delete keyword list table
            try {
                db.execSQL(SQL_DELETE_KEYWORD_LIST_TABLE);
            } catch (SQLException e) {
                Log.i("info", "In MealPlanDbHelper exec " + SQL_DELETE_KEYWORD_LIST_TABLE);
                Log.i("info", e.getMessage());
            }

            //Call create
            onCreate(db);
        }

        @Override
        //Same as upgrade, delete and re-create DB
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

    }

    //developer.android.com/training/data-storage/sqlite
    //A good way to organize a contract class is to put defs that are global to
    //the whole DB in the root level and create inner lass for each table
    public final class MealPlanDbContract {
        //private to prevent someone from accidentally instantiating the class
        private MealPlanDbContract() { }

        //Define table contents -- inner class of MealPlanDbContract
        public class PlanDayEntry implements BaseColumns {
            public static final String PLAN_DAY_TABLENAME = "plan_days";
            public static final String DATE_LONG_COL = "date_long";
            public static final String MEAL_LIST_COL = "meal_list_id";
            public static final String PLAN_DAY_INDEX = "plan_day_index";
        }
        public class MealListEntry implements BaseColumns {
            public static final String MEAL_LIST_TABLENAME = "meal_lists";
            public static final String MEAL_ID_COL_BASENAME = "meal_id";
        }
        public class MealEntry implements BaseColumns {
            public static final String MEAL_TABLENAME = "meals";
            public static final String MEAL_TITLE_COL = "title";
            public static final String CATEGORY_COL = "category";
            public static final String COLOR_CODE_COL = "color_code";
            public static final String RECIPE_LIST_COL = "recipe_list_id";

        }
        public class RecipeListEntry implements BaseColumns {
            public static final String RECIPE_LIST_TABLENAME = "recipe_lists";
            public static final String RECIPE_ID_COL_BASENAME = "recipe_id";
        }
        public class RecipeEntry implements BaseColumns {
            public static final String RECIPE_TABLENAME = "recipes";
            public static final String RECIPE_TITLE_COL = "title";
            public static final String METHOD_COL = "method";
            public static final String INSTRUCTIONS_COL = "instructions";
            public static final String NOTES_COL = "notes";
            public static final String INGREDIENTS_COL = "ingredients";
            public static final String KEYWORD_LIST_COL = "keyword_list_id";
            public static final String PREP_HOURS_COL = "prep_hours";
            public static final String PREP_MINUTES_COL = "prep_minutes";
            public static final String COOK_HOURS_COL = "cook_hours";
            public static final String COOK_MINUTES_COL = "cook_minutes";
        }
        public class KeywordListEntry implements BaseColumns {
            public static final String KEYWORD_LIST_TABLENAME = "keyword_lists";
            public static final String KEYWORD_COL_BASENAME = "keyword";
        }
        public class RecipeSearchEntry implements BaseColumns {
            public static final String RECIPE_SEARCH_TABLENAME = "recipe_search";
            public static final String SEARCH_KEY_COL = "search_key";
            public static final String RECIPE_ID_COL = "recipe_id";
            public static final String SEARCH_KEY_INDEX = "recipe_search_index";
        }
    }

    private static final String SQL_CREATE_PLAN_DAYS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + PLAN_DAY_TABLENAME + " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    DATE_LONG_COL + " INTEGER NOT NULL," +
                    MEAL_LIST_COL + " INTEGER, " +
                    "FOREIGN KEY(" + MEAL_LIST_COL + ") REFERENCES " +
                        MEAL_TABLENAME + "(" + _ID + "))";

    private static final String SQL_CREATE_MEAL_LIST_TABLE =
            "CREATE TABLE IF NOT EXISTS " + MEAL_LIST_TABLENAME + " (" +
                    _ID + " INTEGER PRIMARY KEY)";

    private static final String SQL_CREATE_MEALS_TABLE =
            "CREATE TABLE IF NOT EXISTS  " + MEAL_TABLENAME + " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    MEAL_TITLE_COL + " TEXT," +
                    CATEGORY_COL + " TEXT, " +
                    COLOR_CODE_COL + " TEXT, " +
                    RECIPE_LIST_COL + " INTEGER, " +
                    "FOREIGN KEY(" + RECIPE_LIST_COL + ") REFERENCES " +
                        RECIPE_TABLENAME + "(" + _ID + "))";

    private static final String SQL_CREATE_RECIPE_LIST_TABLE =
            "CREATE TABLE IF NOT EXISTS " + RECIPE_LIST_TABLENAME + " (" +
                    _ID + " INTEGER PRIMARY KEY)";

    private static final String SQL_CREATE_RECIPES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + RECIPE_TABLENAME + " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    RECIPE_TITLE_COL + " TEXT NOT NULL," +
                    METHOD_COL + " TEXT, " +
                    INSTRUCTIONS_COL + " TEXT, " +
                    NOTES_COL + " TEXT, " +
                    INGREDIENTS_COL + " TEXT, " +
                    KEYWORD_LIST_COL + " INTEGER, " +
                    PREP_HOURS_COL + " INTEGER, " +
                    PREP_MINUTES_COL + " INTEGER, " +
                    COOK_HOURS_COL + " INTEGER, " +
                    COOK_MINUTES_COL + " INTEGER, " +
                    "FOREIGN KEY(" + KEYWORD_LIST_COL + ") REFERENCES " +
                        KEYWORD_LIST_TABLENAME + "(" + _ID + "))";

    private static final String SQL_CREATE_KEYWORD_LIST_TABLE =
            "CREATE TABLE IF NOT EXISTS " + KEYWORD_LIST_TABLENAME + " (" +
                    _ID + " INTEGER PRIMARY KEY)";

    private static final String SQL_CREATE_RECIPE_SEARCH_TABLE =
            "CREATE TABLE IF NOT EXISTS " + RECIPE_SEARCH_TABLENAME + " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    SEARCH_KEY_COL + " TEXT, " +
                    RECIPE_ID_COL + " INTEGER, " +
                    "FOREIGN KEY(" + RECIPE_ID_COL + ") REFERENCES " +
                        RECIPE_TABLENAME + "(" + _ID + "))";

    private static final String SQL_CREATE_RECIPE_SEARCH_TABLE_INDEX =
            "CREATE INDEX " + SEARCH_KEY_INDEX +
                    " ON " + RECIPE_SEARCH_TABLENAME + "(" + SEARCH_KEY_COL + ")";

    private static final String SQL_CREATE_PLAN_DAY_INDEX =
            "CREATE UNIQUE INDEX " + PLAN_DAY_INDEX +
                    " ON " + PLAN_DAY_TABLENAME + "(" + DATE_LONG_COL + ")";

    private static final String SQL_DELETE_PLAN_DAYS_TABLE =
            "DROP TABLE IF EXISTS " + PLAN_DAY_TABLENAME;

    private static final String SQL_DELETE_MEAL_LIST_TABLE =
            "DROP TABLE IF EXISTS " + MEAL_LIST_TABLENAME;

    private static final String SQL_DELETE_MEALS_TABLE =
            "DROP TABLE IF EXISTS " + MEAL_TABLENAME;

    private static final String SQL_DELETE_RECIPE_LIST_TABLE =
            "DROP TABLE IF EXISTS " + RECIPE_LIST_TABLENAME;

    private static final String SQL_DELETE_RECIPE_SEARCH_TABLE =
            "DROP TABLE IF EXISTS " + RECIPE_SEARCH_TABLENAME;

    private static final String SQL_DELETE_RECIPES_TABLE =
            "DROP TABLE IF EXISTS " + RECIPE_TABLENAME;

    private static final String SQL_DELETE_KEYWORD_LIST_TABLE =
            "DROP TABLE IF EXISTS " + KEYWORD_LIST_TABLENAME;

    //Insert Methods

    public long saveReferenceList(long listId, long[] referenceIdArray, PlanComponent type) {
        //listId may be zero - new list - need to return listId
        //referenceIdArrayList may be empty - if listId is not, remove the list - return 0
        //if both listId is non-zero and referenceIdList are are non-zero, update an existing list
        int max;
        String colBaseName;
        String tablename;
        switch (type) {
            case RECIPE:
                max = MAX_RECIPES;
                colBaseName = RECIPE_ID_COL_BASENAME;
                tablename = RECIPE_LIST_TABLENAME;
                break;
            case MEAL:
                max = MAX_MEALS;
                colBaseName = MEAL_ID_COL_BASENAME;
                tablename = MEAL_LIST_TABLENAME;
                break;
            default:
                max = 0;
                tablename = "error";
                colBaseName = "error";
                Log.w("saveReferenceList", "Invalid type");
        }
        ContentValues values = new ContentValues();
        int numItems = referenceIdArray.length; //may be 0
        if (numItems > max) {//avoid SQLException
            numItems = max;
            Log.w("saveReferenceList", "Only the first" + max + "items of given " + numItems + "inserted");
        }
        if (numItems > 0) {
            for (int i = 0; i < max; i++) {
                String colName = colBaseName + (i + 1);
                if (i < numItems) {
                    values.put(colName, referenceIdArray[i]);
                } else {
                    values.put(colName, 0);
                }
            }
            if (listId == 0) { //insert new list
                try {
                    listId = db.insert(tablename, null, values); //Values should not be empty, numItems is > 0
                } catch (SQLException e) {
                    Log.e("saveReferenceList", e.getMessage());
                    e.printStackTrace();
                }
                Log.i("saveReferenceList", "Inserted new ReferenceList Type: " + type + ", ID: " + listId);
            } else { //update, and insert NULL rows
                try {
                    int numRowsUpdated = db.update(tablename, values, "_ID = " + listId, null);
                    if (numRowsUpdated != 1) {
                        Log.w("saveReferenceList", "Update was not successful, count of rows updated=" + numRowsUpdated);
                    }
                    Log.i("saveReferenceList", "Updated ReferenceList Type: " + type + ", ID: " + listId);
                } catch (SQLException e) {
                    Log.e("saveReferenceList", e.getMessage());
                    e.printStackTrace();
                }
            }
        } else { //given list is empty
            if (listId > 0) { //remove list with no references
                Log.i("saveReferenceList", "Removed empty " + type + " list, ID: " + listId);
                db.delete(tablename, "_ID = " + listId, null);
                listId=0;
            }
        }
        return listId; //will return back existing ID (which may be 0) or ID for new item (if list was not empty)
    }

    public long[] getReferenceList(long listId, PlanComponent type) {
        // returns array of references is listId > 0 (and it's in the DB)
        // returns empty list if listId = 0 or if data is not found
        int list_size = 0;
        if (type == RECIPE) {list_size = MAX_RECIPES;}
        if (type == MEAL) {list_size = MAX_MEALS;}
        long[] list = new long[list_size];
        if (listId > 0) {
            Cursor listCursor = null;
            try {
                listCursor = selectListItems(listId, type);
            } catch (Exception e) {
                Log.e("getReferenceList", e.getLocalizedMessage());
                e.printStackTrace();
            }
            if ( listCursor != null ) {
                int numListsReturned = listCursor.getCount();
                if (numListsReturned == 1) { //expected case - listId is unique primary key
                    listCursor.moveToFirst();
                    int numItems = listCursor.getColumnCount() - 1; //Less the ID column
                    int columnIndex = 1; //column 0 is id
                    while (columnIndex <= numItems) {
                        //I think null values return 0
                        long id = listCursor.getLong(columnIndex);
                        if (id == 0) { //lists are "front-loaded" once an id is 0, all subsequent ids are 0
                            Log.i("getReferenceList", "Reached end of list, column index: " + columnIndex);
                            break;
                        } else {
                            Log.i("getReferenceList", "Found reference: " + id + " in list: " + listId);
                            list[columnIndex-1] = id;
                            columnIndex++;
                        }
                    }
                } else if (numListsReturned == 0) {
                    Log.w("getReferenceList", "No " + type + " List found with ID: " + listId);
                } else { //returned more than one list
                    Log.w("getReferenceList", "Recieved " + numListsReturned + "supposedly unique lists");
                }
                listCursor.close();
            } else {
                Log.w("getReferenceList", "List Cursor was NULL");
            }
        }
        return list;
    }

    //Save app object methods
    public long saveRecipe (Recipe recipe) { //a Recipe object contains its ID, 0 if never saved to DB
        String tablename = RECIPE_TABLENAME;
        long id = recipe.getId();
        ContentValues values = new ContentValues();
        values.put(RECIPE_TITLE_COL, recipe.getTitle());
        values.put(METHOD_COL, recipe.getCookingMethod().toString());
        values.put(INSTRUCTIONS_COL, recipe.getInstructions());
        values.put(NOTES_COL, recipe.getNotes());
        values.put(INGREDIENTS_COL, recipe.getIngredients());
        values.put(PREP_HOURS_COL, recipe.getPrepHours());
        values.put(PREP_MINUTES_COL, recipe.getPrepMinutes());
        values.put(COOK_HOURS_COL, recipe.getCookHours());
        values.put(COOK_MINUTES_COL, recipe.getCookMinutes());

        if (id == 0) { //new one
            try {
                id = db.insert(tablename, null, values);
            } catch (SQLException e) {
                Log.e("saveRecipe", e.getMessage());
                e.printStackTrace();
            }
            Log.i ("saveRecipe", "Inserted Recipe, ID: " + id );
        } else {
            int numRowsUpdated = 0;
            try {
                numRowsUpdated = db.update(tablename,values,"_ID = " + id, null);
            } catch (SQLException e) {
                Log.e("saveRecipe", e.getMessage());
                e.printStackTrace();
            }
            if (numRowsUpdated > 1) {
                Log.w("saveRecipe", "More than one row updated: " +numRowsUpdated);
            } else if (numRowsUpdated < 1) {
                Log.w("saveRecipe", "Less than one row updated: " +numRowsUpdated);
            } else {
                Log.i("saveRecipe", "Updated Recipe, ID: "+id );
            }
        }
        return id; //will return back existing ID or ID for new item
    }

    public long saveMeal (Meal meal) {
        String tablename = MEAL_TABLENAME;
        long id = meal.getId();
        long recipeListId = meal.getRecipeListId();
        long[] recipeList = meal.getRecipeIdArray();
        recipeListId = saveReferenceList(recipeListId, recipeList, RECIPE);
        ContentValues values = new ContentValues();
        values.put(MEAL_TITLE_COL, meal.getTitle());
        values.put(CATEGORY_COL, meal.getCategory().toString());
        values.put(COLOR_CODE_COL, meal.getColor().toString());
        if (recipeListId > 0) { //update existing list
            values.put(RECIPE_LIST_COL, recipeListId);
        } else { //no list
            values.putNull(RECIPE_LIST_COL);
        }
        if (id == 0) { //new one, insert it
            try {
                id = db.insert(tablename, null, values);
            } catch (SQLException e) {
                Log.e("saveMeal", e.getMessage());
                e.printStackTrace();
            }
            Log.i("saveMeal", "Inserted Meal, ID: " + id);
        } else {
            int numRowsUpdated = 0;
            try {
                numRowsUpdated = db.update(tablename,values,"_ID = " + id, null);
            } catch (SQLException e) {
                Log.e("saveMeal", e.getMessage());
                e.printStackTrace();
            }
            if (numRowsUpdated > 1) {
                Log.w("saveMeal", "More than one row updated: " +numRowsUpdated);
            } else if (numRowsUpdated < 1) {
                Log.w("saveMeal", "Less than one row updated: " +numRowsUpdated);
            } else {
                Log.i("saveMeal", "Updated Meal, ID: "+id );
            }
        }
        return id; //will return back existing ID or ID for new item
    }

    public void savePlanDay (PlanDay planDay) {
        long uniqueKey = planDay.getDaysSinceEpoch(); //table has an Id, but planDaySinceEpoch is unique index
        long mealListId = planDay.getMealListId();
        long[] mealList = planDay.getMealIdArray();
        mealListId = saveReferenceList(mealListId, mealList, MEAL);
        ContentValues values = new ContentValues();
        values.put(DATE_LONG_COL, uniqueKey);
        if (mealListId > 0) {
            values.put(MEAL_LIST_COL, mealListId);
        } else {
            values.putNull(MEAL_LIST_COL);
        }
        try {
            db.replace(PLAN_DAY_TABLENAME, null, values);
        } catch (SQLException e) {
            Log.e("savePlanDay", e.getMessage());
            e.printStackTrace();
        }
        Log.i ("savePlanDay", "Inserted/Updated plan day "+ LocalDate.ofEpochDay(uniqueKey));
    }

    public Recipe getRecipe (long recipeId) { //a Recipe object contains its ID, 0 if never saved to DB
        Recipe recipe = new Recipe(recipeId);
        if (recipeId > 0) {
            Log.i("getRecipe", "Loading Recipe from DB, recipeId: "+ recipeId);
            Cursor recipeCursor = selectItem(recipeId, RECIPE);
            //Expect 1 row, unique id
            int numRecipesReturned = recipeCursor.getCount(); //should only be one
            if (numRecipesReturned == 1) {
                Log.i("getRecipe", "Loading recipe ID: "+recipeId);
                recipeCursor.moveToFirst();
                recipe.setTitle(recipeCursor.getString(recipeCursor.getColumnIndex(RECIPE_TITLE_COL)));
                recipe.setCookingMethod(CookingMethod.valueOf(recipeCursor.getString(recipeCursor.getColumnIndex(METHOD_COL))));
                recipe.setIngredients(recipeCursor.getString(recipeCursor.getColumnIndex(INGREDIENTS_COL)));
                recipe.setNotes(recipeCursor.getString(recipeCursor.getColumnIndex(NOTES_COL)));
                recipe.setInstructions(recipeCursor.getString(recipeCursor.getColumnIndex(INSTRUCTIONS_COL)));
                recipe.setPrepHours(recipeCursor.getInt(recipeCursor.getColumnIndex(PREP_HOURS_COL)));
                recipe.setPrepMinutes(recipeCursor.getInt(recipeCursor.getColumnIndex(PREP_MINUTES_COL)));
                recipe.setCookHours(recipeCursor.getInt(recipeCursor.getColumnIndex(COOK_HOURS_COL)));
                recipe.setCookMinutes(recipeCursor.getInt(recipeCursor.getColumnIndex(COOK_MINUTES_COL)));
            } else if (numRecipesReturned == 0){
                Log.i("getRecipe", "Recipe " +recipeId + " is not in DB");
                recipe.setId(0);
            } else { //this should be impossible
                Log.w("getRecipe", "Returned " + numRecipesReturned + "for supposedly unique id: " + recipeId);
            }
            recipeCursor.close();
        } else {
            Log.w("getRecipe", "Returning empty Recipe object, recipeId is 0");
        }
        return recipe;
    }

    public Meal getMeal (long mealId) {
        Meal meal = new Meal(mealId);
        if (mealId > 0) {
            Log.i("getMeal", "Loading Meal from DB, mealId: " + mealId);
            Cursor mealCursor = selectItem(mealId, MEAL);
            //Expect 1 row, unique id
            int numMealsReturned = mealCursor.getCount(); //should only be one
            if (numMealsReturned == 1) {
                Log.i("getMeal", "Loading meal ID: " + mealId);
                mealCursor.moveToFirst();
                meal.setTitle(mealCursor.getString(mealCursor.getColumnIndex(MEAL_TITLE_COL)));
                meal.setCategory(Category.valueOf(mealCursor.getString(mealCursor.getColumnIndex(CATEGORY_COL))));
                meal.setColor(MealColor.valueOf(mealCursor.getString(mealCursor.getColumnIndex(COLOR_CODE_COL))));
                meal.setRecipeListId(mealCursor.getLong(mealCursor.getColumnIndex(RECIPE_LIST_COL)));
            } else if (numMealsReturned == 0) {
                Log.i("getMeal", "Meal " + mealId + " is not in DB");
                meal.setId(0);
            } else { //this should be impossible
                Log.w("getMeal", "Returned " + numMealsReturned + "for supposedly unique id: " + mealId);
            }
            mealCursor.close();
            //Now load references
            long [] recipeIdList = getReferenceList(meal.getRecipeListId(), RECIPE);
            meal.setRecipeIdsArray(recipeIdList);
        } else {
            Log.w("getMeal", "Returning empty Meal object, mealId was 0");
        }
        return meal;
    }

    public PlanDay getPlanDay (long daySinceEpoch) {
        LocalDate planDate = LocalDate.ofEpochDay(daySinceEpoch);
        PlanDay planDay = new PlanDay(planDate);
        Log.i("getPlanDay", "Loading Plan Day from DB, date: " + planDate);
        Cursor planDayCursor = selectPlanRange(daySinceEpoch, daySinceEpoch);
        //Expect 1 row, unique id
        int numDaysReturned = planDayCursor.getCount(); //1 if it is in DB, 0 if not
        if (numDaysReturned == 1) {  //get mealListId from DB (still may be 0 if empty day was saved)
            planDayCursor.moveToFirst();
            planDay.setMealListId(planDayCursor.getLong(planDayCursor.getColumnIndex(MEAL_LIST_COL)));
        } else if (numDaysReturned == 0) {
            Log.i("getPlanDay", "Plan Day " + planDate + " is not in DB");
        } else { //this should be impossible
            Log.w("getPlanDay", "Returned " + numDaysReturned + "for supposedly unique id: " + planDate);
        }
        planDayCursor.close();
        //Now load references
        long mealListId = planDay.getMealListId();
        long[] mealIdList = planDay.getMealIdArray();//this is initialized array of size MAX_MEALS, it is empty at this point
        long[] storedMealIdList = getReferenceList(planDay.getMealListId(), MEAL);
        Log.i("getPlanDay", "Loading MealIdList from DB, mealIdList.length: "+mealIdList.length+", storedMealIdList.length "+storedMealIdList.length);
        for(int i=0; i<storedMealIdList.length; i++){
            mealIdList[i]=storedMealIdList[i];
        }
        planDay.setMealIdsArray(mealIdList);
        return planDay;
    }

    public ArrayList<PlanDay> getMealPlan (long startEpochDay, long endEpochDay){
        ArrayList<PlanDay> mealPlan = new ArrayList<>();
        for (long d = startEpochDay; d <= endEpochDay; d++) {
            PlanDay day = new PlanDay(LocalDate.ofEpochDay(d)); //init object and mealArrayList
            mealPlan.add(day);
        }
        Log.i("getMealPlan", "Initialized mealPlan, size: " + mealPlan.size());

        //Get whatever days are in the DB
        Cursor planDaysCursor = selectPlanRange(startEpochDay, endEpochDay);
        int numDaysReturned = planDaysCursor.getCount();
        if (numDaysReturned <= 0) {
            Log.i("getMealPlan", "No Days in Plan stored in DB");
            planDaysCursor.close();
        } else {
            Log.i("getMealPlan", "Found " + numDaysReturned + " days in window in DB");
            while (planDaysCursor.moveToNext()) {
                long daysSinceEpoch = planDaysCursor.getLong(planDaysCursor.getColumnIndex(DATE_LONG_COL));
                long mealListId = planDaysCursor.getLong(planDaysCursor.getColumnIndex(MEAL_LIST_COL));
                if (mealListId > 0) {
                    mealPlan.forEach((day) -> {
                        if (day.getDaysSinceEpoch() == daysSinceEpoch) {
                            Log.i("getMealPlan", "Setting meal list " + mealListId + "for day" + day.getDate());
                            day.setMealListId(mealListId);
                            if (mealListId > 0){
                                Log.i("getMealPlan", "Loading meal references");
                                day.setMealIdsArray(getReferenceList(mealListId, MEAL));
                            }
                        }
                    });
                }
            }
            planDaysCursor.close();
        }
        return mealPlan;
    }

    public ArrayList<Meal> getPlanDayMeals (PlanDay planDay){
        ArrayList<Meal> meals = new ArrayList<>();
        for(long mealId : planDay.getMealIdArray()) {
            if (mealId != 0) {
                Log.i("getPlanDayMeals", "Loading Meal, ID: " + mealId);
                Meal meal = getMeal(mealId);
                meals.add(meal);
            } //else do not add the meal
        }
        Log.i("getPlanDayMeals", "Loaded " + meals.size()+ " meals to PlanDay "+ planDay.getDate());
        return meals;
    }

    public ArrayList<Recipe> getMealRecipes (Meal meal){
        ArrayList<Recipe> recipes = new ArrayList<>();
        for(long recipeId : meal.getRecipeIdArray()) {
            if (recipeId != 0) {
                Log.i("getMealsRecipes", "Loading Recipe, ID: " + recipeId);
                Recipe recipe = getRecipe(recipeId);
                recipes.add(recipe);
            }
        }
        Log.i("getMealRecipes", "Loaded " + recipes.size()+ " to Meal "+ meal.getId());
        return recipes;
    }

    public void removeRecipe(long recipeId){
        deleteItem(recipeId, RECIPE);
    }

    public void removeMeal(long mealId){
        deleteItem(mealId, MEAL);
    }

    //Select methods - return cursors
    public Cursor selectAllPlanDays () {
        Cursor results = null;
        try {
            results = db.query(
                    PLAN_DAY_TABLENAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }
        catch (SQLException e){
            Log.e ("selectAllPlanDays", e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    public Cursor selectAllRecipes () {
        Cursor results = null;
        try {
            results = db.query(
                    RECIPE_TABLENAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }
        catch (SQLException e){
            Log.e ("selectAllRecipes", e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    public Cursor selectPlanRange (long startDay, long endDay) {
        Cursor results = null;
        String selection = DATE_LONG_COL + ">= ? AND " + DATE_LONG_COL + " <= ?";
        String[] selectionArgs = { Long.toString(startDay), Long.toString(endDay)};
        try {
            results = db.query(
                    PLAN_DAY_TABLENAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    DATE_LONG_COL);
        }
        catch (SQLException e){
            Log.e ("selectPlanRange", e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    public Cursor selectListItems(long listId, PlanComponent type) {
        //type - Recipe, Meal
        //Should return one Row, multiple columns
        //for recipe, meal - returns id references into recipe,meal tables respectively
        Cursor results = null;
        String tablename;
        String selection = "_ID =" + listId;

        switch(type){
            case RECIPE:
                //Each column is a Long - ID into Recipe table
                tablename = RECIPE_LIST_TABLENAME;
                break;
            case MEAL:
                //Each column is a Long - ID into Meal table
                tablename = MEAL_LIST_TABLENAME;
                break;
            default:
                tablename = "error";
                Log.w("selectListItems", "Invalid type");
        }

        try {
            results = db.query(
                    tablename,
                    null,
                    selection,
                    null,
                    null,
                    null,
                    null);
        }
        catch (SQLException e){
            Log.i ("selectListItems", e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    public Cursor selectItem (long id, PlanComponent type ) {
        Cursor results = null;
        String tablename;
        String selection = "_ID =" + id;

        switch(type){
            case RECIPE:
                tablename = RECIPE_TABLENAME;
                break;
            case MEAL:
                tablename = MEAL_TABLENAME;
                break;
            default:
                tablename = "error";
                Log.w("selectItem", "Invalid type");
        }
        try {
            results = db.query(
                    tablename,
                    null,
                    selection,
                    null,
                    null,
                    null,
                    null);
        }
        catch (SQLException e){
            Log.e ("selectItem", e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    public void deleteItem (long id, PlanComponent type ) {
        String tablename;
        String selection = "_ID =" + id;

        switch(type){
            case RECIPE:
                tablename = RECIPE_TABLENAME;
                break;
            case MEAL:
                tablename = MEAL_TABLENAME;
                break;
            default:
                tablename = "error";
                Log.w("deleteItem", "Invalid type");
        }
        int numDeleted=0;
        try {
            numDeleted = db.delete(
                            tablename,
                            selection,
                            null);
        }
        catch (SQLException e){
            Log.e ("deleteItem", e.getMessage());
            e.printStackTrace();
        }
        if (numDeleted >1){
            Log.w("deleteItem", "More than one supposedly unique item deleted");
        }
        else if (numDeleted == 0 ){
            Log.w("deleteItem", "Found no "+type+ " to delete for ID: " + id);
        } else {
            Log.i("deleteItem", "Deleted "+type+ " ID: "+ id);
        }
    }
}

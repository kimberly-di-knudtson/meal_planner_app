package edu.ucdenver.knudtson.mealplanapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.ArrayList;

import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.MealEntry.CATEGORY_COL;
import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.MealEntry.COLOR_CODE_COL;
import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.MealEntry.MEAL_TITLE_COL;
import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.PlanDayEntry.DATE_LONG_COL;
import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.PlanDayEntry.MEAL_LIST_COL;
import static edu.ucdenver.knudtson.mealplanapp.DataManager.MealPlanDbContract.RecipeEntry.RECIPE_TITLE_COL;
import static edu.ucdenver.knudtson.mealplanapp.Meal.MAX_RECIPES;
import static edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.PlanComponent.MEAL;
import static edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.PlanComponent.RECIPE;
import static edu.ucdenver.knudtson.mealplanapp.PlanDay.MAX_MEALS;

public class RecipeBoxActivity extends AppCompatActivity {
    //Primary Class objects
    private ArrayList<Recipe> recipes;
    private int selectedRecipeIndex;
    private boolean hasSelection;
    private final String RECIPE_STATE = "recipeId";

    //Activity's Views
    private RecyclerView recipeRecyclerView;
    private FloatingActionButton dismissFAB;
    private FloatingActionButton addRecipeFAB;
    private FloatingActionButton doneFAB;
    private FloatingActionButton deleteRecipeFAB;

    //Data helpers
    private RecipeAdapter recipeAdapter;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("info", "onCreate RecipeBoxActivity");
        setContentView(R.layout.activity_recipe_box);

        selectedRecipeIndex = -1;
        hasSelection = false;
        //Buttons
        doneFAB = findViewById(R.id.done_fab); //dismiss
        addRecipeFAB = findViewById(R.id.add_recipe_fab); //Add
        deleteRecipeFAB = findViewById(R.id.delete_recipe_fab); //Delete selected recipe

        doneFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                long selectedRecipeId=0;
                if(selectedRecipeIndex != RecyclerView.NO_POSITION) {
                    selectedRecipeId = recipes.get(selectedRecipeIndex).getId();
                }
                intent.putExtra(RECIPE_STATE, selectedRecipeId);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        addRecipeFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent recipeIntent = new Intent(RecipeBoxActivity.this, RecipeActivity.class);
                startActivity(recipeIntent);
            }
        });

        deleteRecipeFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if(selectedRecipeIndex != RecyclerView.NO_POSITION) {
                    new AlertDialog.Builder(RecipeBoxActivity.this)
                            .setMessage(R.string.delete_recipe + " " + recipes.get(selectedRecipeIndex).getTitle()+ " ?")
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dataManager.removeRecipe(recipes.get(selectedRecipeIndex).getId());
                                    recipes.remove(selectedRecipeIndex);
                                    recipeAdapter.notifyItemRemoved(selectedRecipeIndex);
                                    selectedRecipeIndex = RecyclerView.NO_POSITION;
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //do nothing
                                }
                            })
                            .show();
                } //else do nothing
            }
        });

        recipes = new ArrayList<Recipe>();
        dataManager = new DataManager(this);

        recipeRecyclerView = findViewById(R.id.recipeRecyclerView);
        recipeAdapter = new RecipeAdapter(recipes);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recipeRecyclerView.setLayoutManager(layoutManager);
        //makes a separator between items in recycler view
        recipeRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recipeRecyclerView.setAdapter(recipeAdapter);

        recipeAdapter.OnRecyclerViewClickListener(new RecipeAdapter.OnRecyclerViewClickListener() {
            @Override
            public void OnItemClick(int position) {
                selectedRecipeIndex = position;
            }
        });

        recipeAdapter.OnRecyclerViewLongClickListener(new RecipeAdapter.OnRecyclerViewLongClickListener() {
            @Override
            public void OnItemLongClick(int position) {
                editRecipe(recipes.get(position).getId());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("info", "onResume RecipeBoxActivity");
        try {
            loadRecipes();
        }
        catch (Exception e) {
            Log.i ("info", "Loading data on resume");
            Log.i ("info", e.getMessage());
        }
    }

    //Methods to populate data from database
    //Load recipes
    public void loadRecipes() throws Exception {
        recipes.clear();
        Cursor recipesCursor = dataManager.selectAllRecipes();
        int numRecipesReturned = recipesCursor.getCount();
        if (numRecipesReturned == 0) {
            Log.i("info", "No Recipes found in DB");
        } else {
            Log.i("info", "Found "+numRecipesReturned+" recipes in DB");
            while (recipesCursor.moveToNext()) {
                Recipe recipe = new Recipe(0);
                recipe.setId(recipesCursor.getLong(0));
                recipe.setTitle(recipesCursor.getString(recipesCursor.getColumnIndex(RECIPE_TITLE_COL)));
                recipes.add(recipe);
            }
        }
        recipesCursor.close();
        recipeAdapter.notifyDataSetChanged();
    }

    public void editRecipe(long recipeId) {
        Intent recipeIntent = new Intent(RecipeBoxActivity.this, RecipeActivity.class);
        recipeIntent.putExtra("recipeId", recipeId);
        startActivity(recipeIntent);
    }
}
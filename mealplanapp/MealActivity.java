package edu.ucdenver.knudtson.mealplanapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.Category;
import edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.MealColor;

import static edu.ucdenver.knudtson.mealplanapp.Meal.MAX_RECIPES;
import static edu.ucdenver.knudtson.mealplanapp.PlanDay.MAX_MEALS;

public class MealActivity extends AppCompatActivity {
    //Primary class objects
    private Meal meal;
    private ArrayList<Recipe> recipes;
    private final String MEAL_STATE = "mealId";

    private static final int MEAL_RECIPE_REQUEST = 2;

    //Activity's Views
    private TextView textViewMealHeader;
    private EditText editTextMealTitle;

    private TextView categorySelection;
    private ListPopupWindow categoryListPopup;
    private List<Category> categories;

    private TextView colorSelection;
    private ListPopupWindow colorListPopup;
    private List<MealColor> colors;


    //Data helpers
    private RecipeAdapter recipeAdapter;
    private DataManager dataManager;

    //constructor is empty
    public MealActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("info", "onCreate Meal Activity");

        setContentView(R.layout.activity_meal);

        //Find reference to views on the form
        textViewMealHeader = findViewById(R.id.textViewMealHeader);
        editTextMealTitle = findViewById(R.id.editTextMealTitle);
        RecyclerView recipeRecyclerView = findViewById(R.id.recipeRecyclerView);
        categorySelection = findViewById(R.id.categorySelection);
        categories = Arrays.asList(Category.values());
        categoryListPopup = new ListPopupWindow(categorySelection.getContext());

        colorSelection = findViewById(R.id.colorSelection);
        colors = Arrays.asList(MealColor.values());
        colorListPopup = new ListPopupWindow(colorSelection.getContext());

        FloatingActionButton doneFAB = findViewById(R.id.done_fab);
        FloatingActionButton addRecipeFAB = findViewById(R.id.add_recipe_fab);
        FloatingActionButton dismissFAB = findViewById(R.id.dismiss_fab);

        recipes = new ArrayList<>();
        dataManager = new DataManager(this);
        recipeAdapter = new RecipeAdapter(recipes);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recipeRecyclerView.setLayoutManager(layoutManager);
        recipeRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
        recipeRecyclerView.setAdapter(recipeAdapter);

        ArrayAdapter<Category> categoryArrayAdapter = new ArrayAdapter<Category>(this, R.layout.list_item_category, R.id.textViewCategory, categories);
        categoryListPopup.setAdapter(categoryArrayAdapter);
        categoryListPopup.setAnchorView(categorySelection);
        categoryListPopup.setWidth(300);
        categoryListPopup.setHeight(400);
        categoryListPopup.setModal(true); //this makes it selectable and go away when selected outside
        categoryListPopup.setOnItemClickListener(this::onCategoryItemClick);

        //Click on category selection - bring up popup
        categorySelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryListPopup.show();
            }
        });

        ArrayAdapter<MealColor> colorArrayAdapter = new ArrayAdapter<MealColor>(this, R.layout.list_item_color, R.id.textViewMealColor, colors);
        colorListPopup.setAdapter(colorArrayAdapter);
        colorListPopup.setAnchorView(colorSelection);
        colorListPopup.setWidth(300);
        colorListPopup.setHeight(500);
        colorListPopup.setModal(true); //this makes it selectable and go away when selected outside
        colorListPopup.setOnItemClickListener(this::onColorItemClick);

        colorSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorListPopup.show();
            }
        });

        //button functionality
        doneFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view) {
                meal.setTitle(editTextMealTitle.getText().toString());
                long mealId = dataManager.saveMeal(meal);
                Intent intent = new Intent();
                intent.putExtra(MEAL_STATE, mealId);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        addRecipeFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int numRecipes = recipes.size();
                if (numRecipes == MAX_MEALS) {
                    Toast.makeText(getApplicationContext(), "Recipe list is full, MAX_RECIPES=" + MAX_RECIPES, Toast.LENGTH_LONG).show();
                } else {
                    Intent recipeIntent = new Intent(MealActivity.this, RecipeBoxActivity.class);
                    startActivityForResult(recipeIntent, MEAL_RECIPE_REQUEST);
                }
            }
        });

        dismissFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Meal changes not saved
                finish();
            }
        });
        recipeAdapter.OnRecyclerViewClickListener(new RecipeAdapter.OnRecyclerViewClickListener() {
            @Override
            public void OnItemClick(int position) {
                viewRecipe(recipes.get(position).getId());
            }
        });

        recipeAdapter.OnRecyclerViewLongClickListener(new RecipeAdapter.OnRecyclerViewLongClickListener() {
            @Override
            public void OnItemLongClick(int position) {
                recipes.remove(position);
                recipeAdapter.notifyItemRemoved(position);
            }
        });
        //Load meal from last saved state - may not match state in DB
        if(savedInstanceState != null) {
            long mealId = savedInstanceState.getLong(MEAL_STATE);
            Log.i("info", "Restoring MealActivity from savedState, mealId: " + mealId);
            meal = new Meal(mealId);
            meal.setTitle(savedInstanceState.getString("title"));
            meal.setCategory(Category.valueOf(savedInstanceState.getString("category")));
            meal.setColor(MealColor.valueOf(savedInstanceState.getString("colorCode")));
            meal.setRecipeListId(savedInstanceState.getLong("recipeListId"));
            meal.setRecipeIdsArray(savedInstanceState.getLongArray("references"));
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i("info", "Meal-onActivityResult- requestCode: "+requestCode+" resultCode: "+resultCode);
        if (requestCode == MEAL_RECIPE_REQUEST && resultCode == RESULT_OK) {
            String RECIPE_STATE = "recipeId";
            long recipeId = intent.getLongExtra(RECIPE_STATE, 0);
            Log.i("info", "Received from AddRecipe: recipeId: "+recipeId);
            if (recipeId != 0) {
                    meal.addRecipeReference(recipeId);
                    displayMeal();
            } //else do nothing
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MealActivity", "onResume()");
        Intent intent = getIntent();
        long mealId = intent.getLongExtra(MEAL_STATE, 0); //intent cannot be null - that's what default is for
        if (meal == null) { //might have been restored from saved state or return from addrecipe and don't want to reload from DB
            Log.i("onResume", "Meal State loaded from DB, given from intent: " + mealId);
            meal = new Meal(mealId);
            loadMeal();
        } else {
            Log.i("onResume", "Meal activity intent was non-null, meal was already initialized: " + mealId);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        long mealId = meal.getId();
        Log.i("info", "Saving Meal State, mealId: " + mealId);
        state.putString("title", meal.getTitle());
        state.putString("category", meal.getCategory().toString());
        state.putString("colorCode", meal.getColor().toString());
        state.putLong("recipeListId", meal.getRecipeListId());
        state.putLongArray("references", meal.getRecipeIdArray());
        super.onSaveInstanceState(state);
    }

    public void loadMeal() {
        meal = dataManager.getMeal(meal.getId()); //shoudl return empty meal
        displayMeal();
    }

    public void displayMeal(){ //populate the display with meal data & references
        String headerText = getFullTitle();
        if (!headerText.equals("")) {
            textViewMealHeader.setText(getFullTitle());
        }
        Context context = getApplicationContext();
        int default_color = ContextCompat.getColor(context,R.color.colorPrimary);
        textViewMealHeader.setBackgroundColor(getColorValue(context, default_color));

        categorySelection.setText(meal.getCategory().toString());

        editTextMealTitle.setText(meal.getTitle());

        default_color = ContextCompat.getColor(context,R.color.NONE);
        colorSelection.setBackgroundColor(getColorValue(context, default_color));
        colorSelection.setText(meal.getColor().toString());

        loadRecipes();
    }

    public void loadRecipes() {
        recipes.clear();
        for(long recipeId : meal.getRecipeIdArray()) {
            if (recipeId != 0) {
                Log.i("loadRecipes", "Loading Recipe, ID: " + recipeId);
                Recipe recipe = dataManager.getRecipe(recipeId);
                recipes.add(recipe);
            } //else do not add
        }
        Log.i("MealActivity - loadRecipes","loaded: "+recipes.size()+" recipes.");
        recipeAdapter.notifyDataSetChanged();
    }

    public void onCategoryItemClick(AdapterView<?> parent, View view, int position, long id) {
        Category category = categories.get(position);
        meal.setCategory(category);
        categorySelection.setText(category.toString());
        categoryListPopup.dismiss();
        textViewMealHeader.setText(getFullTitle());
    }

    public void onColorItemClick(AdapterView<?> parent, View view, int position, long id) {
        MealColor color = colors.get(position);
        meal.setColor(color);
        Context context = view.getContext();
        colorSelection.setBackgroundColor(getColorValue(context, ContextCompat.getColor(context, R.color.NONE)));
        colorSelection.setText(meal.getColor().toString());
        colorListPopup.dismiss();
        textViewMealHeader.setBackgroundColor(getColorValue(context, ContextCompat.getColor(context,R.color.colorPrimary)));
    }

    public String getFullTitle(){
        String mealFullTitle = ""; //Category: Title, or Category, or Title, or "";
        String title = meal.getTitle();
        Category category = meal.getCategory();

        if (category != null && category != Category.NONE) {
            if (!title.isEmpty()) {
                mealFullTitle = category + ": " + title;
            } else { //no title, but category
                mealFullTitle = category.toString();
            }
        } else { // no category
            if (!title.isEmpty()){ //yes title
                mealFullTitle = title;
            } //else do nothing mealFullTitle initialized to ""
        }
        Log.i("info", "Meal Full Title: " + mealFullTitle);
        return mealFullTitle;
    }

    public int getColorValue(Context context, int defaultColor){
        MealColor color = meal.getColor();
        int colorValue;
        switch (color){
            case RED:
                colorValue = ContextCompat.getColor(context,R.color.RED);
                break;
            case ORANGE:
                colorValue = ContextCompat.getColor(context,R.color.ORANGE);
                break;
            case YELLOW:
                colorValue = ContextCompat.getColor(context,R.color.YELLOW);
                break;
            case GREEN:
                colorValue = ContextCompat.getColor(context,R.color.GREEN);
                break;
            case BLUE:
                colorValue = ContextCompat.getColor(context,R.color.BLUE);
                break;
            case INDIGO:
                colorValue = ContextCompat.getColor(context,R.color.INDIGO);
                break;
            case VIOLET:
                colorValue = ContextCompat.getColor(context,R.color.VIOLET);
                break;
            default:
                colorValue = defaultColor;
                break;
        }
    return colorValue;
    }

    public void viewRecipe(long recipeId){
        ViewRecipeDialog viewRecipeDialog = new ViewRecipeDialog();
        Recipe selectedRecipe = dataManager.getRecipe(recipeId);
        viewRecipeDialog.sendSelectedRecipe(selectedRecipe);
        viewRecipeDialog.show(getSupportFragmentManager(), "");
    }

    public void removeRecipe(int index){
        meal.removeRecipeReference(index);
        recipes.remove(index);
    }
}


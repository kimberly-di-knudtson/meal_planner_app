package edu.ucdenver.knudtson.mealplanapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

import edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.CookingMethod;

public class RecipeActivity extends AppCompatActivity {
    //Primary class objects
    private Recipe recipe;
    private final String RECIPE_STATE = "recipeId";

    //Activity's Views
    private EditText editTextRecipeTitle;
    private EditText editTextPrepHours;
    private EditText editTextPrepMinutes;
    private EditText editTextCookHours;
    private EditText editTextCookMinutes;
    private EditText editTextNotes;
    private EditText editTextInstructions;
    private EditText editTextIngredients;

    private ArrayAdapter<CookingMethod> methodArrayAdapter;
    private TextView methodSelection;
    private ListPopupWindow methodListPopup;
    private List<CookingMethod> methods;

    private FloatingActionButton doneFAB;
    private FloatingActionButton dismissFAB;

    //Data helpers
    private DataManager dataManager;


    //constructor is empty
    public RecipeActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("RecipeActivity", "onCreate");

        setContentView(R.layout.activity_recipe);

        //Find reference to views on the form
        editTextRecipeTitle = findViewById(R.id.editTextRecipeTitle);
        editTextPrepHours = findViewById(R.id.editTextPrepHours);
        editTextPrepMinutes = findViewById(R.id.editTextPrepMinutes);
        editTextCookHours = findViewById(R.id.editTextCookHours);
        editTextCookMinutes = findViewById(R.id.editTextCookMinutes);
        editTextNotes = findViewById(R.id.editTextNotes);
        editTextInstructions = findViewById(R.id.editTextInstructions);
        editTextIngredients = findViewById(R.id.editTextIngredients);

        methodSelection = findViewById(R.id.methodSelection);

        doneFAB = findViewById(R.id.done_fab);
        dismissFAB = findViewById(R.id.dismiss_fab);

        dataManager = new DataManager(this);

        methods = Arrays.asList(CookingMethod.values());
        methodListPopup = new ListPopupWindow(methodSelection.getContext());
        methodArrayAdapter = new ArrayAdapter<CookingMethod>(this, R.layout.list_item_method, R.id.textViewCookingMethod, methods);
        methodListPopup.setAdapter(methodArrayAdapter);
        methodListPopup.setAnchorView(methodSelection);
        methodListPopup.setWidth(350);
        methodListPopup.setHeight(400);
        methodListPopup.setModal(true); //this makes it selectable and go away when selected outside
        methodListPopup.setOnItemClickListener(this::onMethodItemClick);

        methodSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methodListPopup.show();
            }
        });

        editTextPrepHours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int prepHours = Integer.parseInt(editTextPrepHours.getText().toString());
                    recipe.setPrepHours(prepHours);
                } catch (NumberFormatException nfe) {
                    Toast.makeText(editTextPrepHours.getContext(), "Prep Hours must be integer", Toast.LENGTH_SHORT).show();
                    editTextPrepHours.setText("0");
                }
            }
        });

        editTextPrepMinutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int prepMinutes = Integer.parseInt(editTextPrepMinutes.getText().toString());
                    recipe.setPrepMinutes(prepMinutes);
                } catch (NumberFormatException nfe) {
                    Toast.makeText(editTextPrepMinutes.getContext(), "Prep Minutes must be integer", Toast.LENGTH_SHORT).show();
                    editTextPrepMinutes.setText("0");
                }
            }
        });

        editTextCookHours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int cookHours = Integer.parseInt(editTextCookHours.getText().toString());
                    recipe.setCookHours(cookHours);
                } catch (NumberFormatException nfe) {
                    Toast.makeText(editTextCookHours.getContext(), "Cook Hours must be integer", Toast.LENGTH_SHORT).show();
                    editTextCookHours.setText("0");
                }
            }
        });

        editTextCookMinutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int cookMinutes = Integer.parseInt(editTextCookMinutes.getText().toString());
                    recipe.setCookMinutes(cookMinutes);
                } catch (NumberFormatException nfe) {
                    Toast.makeText(editTextCookMinutes.getContext(), "Cook Minutes must be integer", Toast.LENGTH_SHORT).show();
                    editTextCookMinutes.setText("0");
                }
            }
        });

        //button functionality
        doneFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (recipe.getTitle().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Recipe requires a Title", Toast.LENGTH_SHORT).show();
                } else {
                    saveRecipe();
                    Intent intent = new Intent();
                    intent.putExtra(RECIPE_STATE, recipe.getId());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        dismissFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        if (savedInstanceState != null) {
            long recipeId = savedInstanceState.getLong(RECIPE_STATE);
            recipe = new Recipe(recipeId);
            Log.i("onCreate", "Restoring Recipe Activity from savedState, recipeId: " + recipeId);
            recipe.setTitle(savedInstanceState.getString("title"));
            recipe.setIngredients(savedInstanceState.getString("ingredients"));
            recipe.setCookingMethod(CookingMethod.valueOf(savedInstanceState.getString("cookingMethod")));
            recipe.setInstructions(savedInstanceState.getString ("instructions"));
            recipe.setNotes(savedInstanceState.getString("notes"));
            recipe.setPrepHours(savedInstanceState.getInt("prepHours"));
            recipe.setPrepMinutes(savedInstanceState.getInt("prepMinutes"));
            recipe.setCookHours(savedInstanceState.getInt("cookHours"));
            recipe.setCookHours(savedInstanceState.getInt("cookMinutes"));
        }

    }
//Getting Intent happens in the background here between onCreate and onResume
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("RecipeActivity", "onResume()");
        Intent intent = getIntent();
        long recipeId = intent.getLongExtra(RECIPE_STATE, 0); //intent cannot be null - that's what default is for
        if (recipe == null) { //might have been restored from saved state and don't want to reload from DB
            Log.i("onResume", "Recipe Activity loaded from intent: " + recipeId);
            recipe = new Recipe(recipeId);
            loadRecipe();
        } else {
            Log.w("onResume", "Recipe activity intent was non-null, but recipe was already initialized: " + recipeId);
        }
        displayRecipe();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) { //this is in case activity destroyed by OS and I cant get it off the stack
        long recipeId = recipe.getId();
        getTextBoxValues();
        Log.i("info", "Saving Recipe State, recipeId: " + recipeId);
        state.putLong(RECIPE_STATE, recipeId);
        state.putString("title", recipe.getTitle());
        state.putString("ingredients", recipe.getIngredients());
        state.putString("cookingMethod", recipe.getCookingMethod().toString());
        state.putString ("instructions", recipe.getInstructions());
        state.putString("notes", recipe.getNotes());
        state.putInt("prepHours", recipe.getPrepHours());
        state.putInt("prepMinutes", recipe.getPrepMinutes());
        state.putInt("cookHours", recipe.getCookHours());
        state.putInt("cookMinutes", recipe.getCookMinutes());
        super.onSaveInstanceState(state);
    }

    public void loadRecipe() {
        dataManager.getRecipe(recipe.getId());
        displayRecipe();
    }

    public void displayRecipe() {
        String title = recipe.getTitle();
        editTextRecipeTitle.setText(title);

        CookingMethod cookingMethod = recipe.getCookingMethod();
        if (cookingMethod != CookingMethod.NONE) {
            methodSelection.setText(cookingMethod.toString());
        }

        int prepHours = recipe.getPrepHours();
        if (prepHours > 0) {
            editTextPrepHours.setText(Integer.toString(prepHours));
        }
        int prepMinutes = recipe.getPrepMinutes();
        if (prepMinutes > 0) {
            editTextPrepMinutes.setText(Integer.toString(prepMinutes));
        }
        int cookHours = recipe.getCookHours();
        if (cookHours > 0) {
            editTextCookHours.setText(Integer.toString(cookHours));
        }
        int cookMinutes = recipe.getCookMinutes();
        if (cookMinutes > 0) {
            editTextCookMinutes.setText(Integer.toString(cookMinutes));
        }

        editTextNotes.setText(recipe.getNotes());
        editTextInstructions.setText(recipe.getInstructions());
        editTextIngredients.setText(recipe.getIngredients());
    }

    public void onMethodItemClick(AdapterView<?> parent, View view, int position, long id) {
        CookingMethod method = methods.get(position);
        recipe.setCookingMethod(method);
        methodSelection.setText(method.toString());
        methodListPopup.dismiss();
    }

    public void getTextBoxValues() {
        //Text changes are not captured until we need to save state
        //Title, Ingredients, Instructions, Notes
        String title = editTextRecipeTitle.getText().toString();
        recipe.setTitle(title);
        String ingredients = editTextIngredients.getText().toString();
        recipe.setIngredients(ingredients);
        String instructions = editTextInstructions.getText().toString();
        recipe.setInstructions(instructions);
        String notes = editTextNotes.getText().toString();
        recipe.setNotes(notes);
    }

    public void saveRecipe() {
        getTextBoxValues();
        recipe.setId(dataManager.saveRecipe(recipe)); //Data manager handles insert or update logic
    }
}



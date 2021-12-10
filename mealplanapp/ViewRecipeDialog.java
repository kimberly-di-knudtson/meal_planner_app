package edu.ucdenver.knudtson.mealplanapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

import edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.CookingMethod;

public class ViewRecipeDialog extends DialogFragment {
    //Primary class objects
    private Recipe recipe;
//Stateless

    //Activity's Views
    private TextView textViewRecipeTitle;
    private TextView textViewPrepHours;
    private TextView textViewPrepMinutes;
    private TextView textViewCookHours;
    private TextView textViewCookMinutes;
    private TextView textViewNotes;
    private TextView textViewInstructions;
    private TextView textViewIngredients;
    private TextView methodSelection;

    private FloatingActionButton dismissFAB;

    //constructor is empty
    public ViewRecipeDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_view_recipe, null);

        //Find reference to views on the form
        textViewRecipeTitle = dialogView.findViewById(R.id.textViewRecipeTitle);
        textViewPrepHours = dialogView.findViewById(R.id.textViewPrepHours);
        textViewPrepMinutes = dialogView.findViewById(R.id.textViewPrepMinutes);
        textViewCookHours = dialogView.findViewById(R.id.textViewCookHours);
        textViewCookMinutes = dialogView.findViewById(R.id.textViewCookMinutes);
        textViewNotes = dialogView.findViewById(R.id.textViewNotes);
        textViewInstructions = dialogView.findViewById(R.id.textViewInstructions);
        textViewIngredients = dialogView.findViewById(R.id.textViewIngredients);

        methodSelection = dialogView.findViewById(R.id.methodSelection);

        dismissFAB = dialogView.findViewById(R.id.dismiss_fab);

        displayRecipe();
        builder.setView(dialogView).setMessage(" ");


        dismissFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return builder.create();
    }

    public void displayRecipe() {
        String title = recipe.getTitle();
        textViewRecipeTitle.setText(title);

        CookingMethod cookingMethod = recipe.getCookingMethod();
        if (cookingMethod != CookingMethod.NONE) {
            methodSelection.setText(cookingMethod.toString());
        }

        int prepHours = recipe.getPrepHours();
        if (prepHours > 0) {
            textViewPrepHours.setText(Integer.toString(prepHours));
        }
        int prepMinutes = recipe.getPrepMinutes();
        if (prepMinutes > 0) {
            textViewPrepMinutes.setText(Integer.toString(prepMinutes));
        }
        int cookHours = recipe.getCookHours();
        if (cookHours > 0) {
            textViewCookHours.setText(Integer.toString(cookHours));
        }
        int cookMinutes = recipe.getCookMinutes();
        if (cookMinutes > 0) {
            textViewCookMinutes.setText(Integer.toString(cookMinutes));
        }

        textViewNotes.setText(recipe.getNotes());
        textViewInstructions.setText(recipe.getInstructions());
        textViewIngredients.setText(recipe.getIngredients());
    }

    public void sendSelectedRecipe (Recipe recipe) {
        this.recipe = recipe;
    }

}



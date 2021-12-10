package edu.ucdenver.knudtson.mealplanapp;

import android.content.Context;
import android.content.Intent;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.MealAppActivityType;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeHolder> {
    private ArrayList<Recipe> recipeArrayList;
    //private AppCompatActivity callingActivity;
    private int selectedPosition;

    private OnRecyclerViewClickListener clickListener;
    private OnRecyclerViewLongClickListener longClickListener;


    public interface OnRecyclerViewClickListener {
        void OnItemClick(int position);
    }

    public interface OnRecyclerViewLongClickListener {
        void OnItemLongClick(int position);
    }

    public void OnRecyclerViewClickListener(OnRecyclerViewClickListener listener){
        clickListener = listener;
    }

    public void OnRecyclerViewLongClickListener(OnRecyclerViewLongClickListener listener){
        longClickListener = listener;
    }

    //Constructor
    public RecipeAdapter(ArrayList<Recipe> recipeArrayList) {
        this.recipeArrayList = recipeArrayList;
        //this.callingActivity = callingActivity;
        selectedPosition = RecyclerView.NO_POSITION;

        //Meal Activity - click to view, long click to delete (meals reference)
        //Recipe Box Activity - click to select, captures ID, can add, delete, modify object
    }

    @NonNull
    @Override
    public RecipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //create listitem from definition
        View recipe = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_recipe, parent, false);

        //return holder of list items
        return new RecipeHolder(recipe, clickListener, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeHolder holder, int position) {
        Log.i("info", "Position: " + position);
        Recipe recipe = recipeArrayList.get(position);

        long id = recipe.getId();
        String title = recipe.getTitle();

        Context context = holder.textViewRecipeTitle.getContext();
        Log.i("onBindViewHolder", "Recipe Title: " + title + " ID: " + id);

        if (title.isEmpty()) {
            title = "<Untitled Recipe, ID: " + id + ">";
            holder.textViewRecipeTitle.setTextColor(ContextCompat.getColor(context, R.color.ltgray));
        }
        holder.textViewRecipeTitle.setText(title);
    }


    @Override
    public int getItemCount() {
        return recipeArrayList.size();
    }

    //implements View.OnClickListener so we can launch PlanDay dialog when
    //user clicks a Meal (full title value)
    public class RecipeHolder extends RecyclerView.ViewHolder {
        //Vars for item attributes to display
        private TextView textViewRecipeTitle;

        //Constructor
        public RecipeHolder(View view, OnRecyclerViewClickListener clickListener,
                            OnRecyclerViewLongClickListener longClickListener) {
            super(view);

            //item attributes on the view
            view.setClickable(true);
            //Defined in this class
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //deselect
                    if (selectedPosition == getAbsoluteAdapterPosition()) {
                        selectedPosition = RecyclerView.NO_POSITION;
                        v.setBackgroundColor(ContextCompat.getColor(v.getContext(), (R.color.NONE)));
                    } else { //select
                        selectedPosition = getAbsoluteAdapterPosition();
                        v.setBackgroundColor(ContextCompat.getColor(v.getContext(), (R.color.colorAccentSecondary)));
                    }
                    if (clickListener != null) {
                        clickListener.OnItemClick(selectedPosition);
                    }
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (longClickListener != null && getAbsoluteAdapterPosition() != RecyclerView.NO_POSITION) {
                        longClickListener.OnItemLongClick(getAbsoluteAdapterPosition());
                    }
                    return true;
                }
            });
            textViewRecipeTitle = view.findViewById(R.id.textViewRecipeTitle);

        }
    }

}

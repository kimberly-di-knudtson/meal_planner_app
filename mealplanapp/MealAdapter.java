package edu.ucdenver.knudtson.mealplanapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static edu.ucdenver.knudtson.mealplanapp.MealPlanEnums.*;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealHolder> {
    private ArrayList<Meal> mealArrayList;
    int selectedPosition;

    private MealAdapter.OnRecyclerViewClickListener clickListener;
    private MealAdapter.OnRecyclerViewLongClickListener longClickListener;


    public interface OnRecyclerViewClickListener {
        void OnItemClick(int position);
    }

    public interface OnRecyclerViewLongClickListener {
        void OnItemLongClick(int position);
    }

    public void OnRecyclerViewClickListener(MealAdapter.OnRecyclerViewClickListener listener){
        clickListener = listener;
    }

    public void OnRecyclerViewLongClickListener(MealAdapter.OnRecyclerViewLongClickListener listener){
        longClickListener = listener;
    }

    //Constructor
    public MealAdapter(ArrayList<Meal> mealArrayList) {
        this.mealArrayList = mealArrayList;
        selectedPosition = RecyclerView.NO_POSITION;
    }

    @NonNull
    @Override
    public MealHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //create listitem from definition
        View meal = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_meal, parent, false);

        //return holder of list items
        return new MealHolder(meal, clickListener, longClickListener);
    }

    @Override

    public void onBindViewHolder(@NonNull MealHolder holder, int position) {
        Log.i("MealAdapter", "Position: " + position);
        Meal meal = mealArrayList.get(position);

        Category category = meal.getCategory();
        String title = meal.getTitle();
        MealColor color = meal.getColor();
        Context context = holder.textViewMealFullTitle.getContext();

        Log.i("MealAdapter", "Meal Category: " + category);
        Log.i("MealAdapter", "Meal Title: " + title);
        holder.textViewMealFullTitle.setText(getFullTitle(title, category));
        holder.textViewMealFullTitle.setBackgroundColor(getColorValue(context, color));
    }

    @Override
    public int getItemCount() {
        return mealArrayList.size();
    }

    //implements View.OnClickListener so we can launch PlanDay dialog when
    //user clicks a Meal (full title value)
    public class MealHolder extends RecyclerView.ViewHolder {
        //Vars for item attributes to display
        private TextView textViewMealFullTitle;

        //Constructor
        public MealHolder (View view,
                           MealAdapter.OnRecyclerViewClickListener clickListener,
                           MealAdapter.OnRecyclerViewLongClickListener longClickListener) {
            super(view);

            //item attributes on the view
            textViewMealFullTitle = view.findViewById(R.id.textViewMealFullTitle);
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
        }
    }

    public String getFullTitle(String title, Category category){
        String mealFullTitle = ""; //Category: Title, or Category, or Title, or "";

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

    public int getColorValue(Context context, MealColor color){
        int colorValue;
        switch (color){
            case RED:
                colorValue = ContextCompat.getColor(context,(R.color.RED));
                break;
            case ORANGE:
                colorValue = ContextCompat.getColor(context,(R.color.ORANGE));
                break;
            case YELLOW:
                colorValue = ContextCompat.getColor(context,(R.color.YELLOW));
                break;
            case GREEN:
                colorValue = ContextCompat.getColor(context,(R.color.GREEN));
                break;
            case BLUE:
                colorValue = ContextCompat.getColor(context,(R.color.BLUE));
                break;
            case INDIGO:
                colorValue = ContextCompat.getColor(context,(R.color.INDIGO));
                break;
            case VIOLET:
                colorValue = ContextCompat.getColor(context,(R.color.VIOLET));
                break;
            default:
                colorValue = ContextCompat.getColor(context,(R.color.NONE));
                break;
        }
        return colorValue;
    }

}

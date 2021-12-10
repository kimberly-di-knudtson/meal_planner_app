package edu.ucdenver.knudtson.mealplanapp;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

//geeksforgeeks.org/how-to-create-a-nested-recyclerview-in-android
public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.MealPlanDayHolder> {
    private ArrayList<PlanDay> mealPlan;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(PlanDay.DATE_FORMAT);
    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    private MealPlanAdapter.OnRecyclerViewClickListener clickListener;

    public interface OnRecyclerViewClickListener {
        void OnPlanDayClick(int position);
    }
    //Constructor
    public MealPlanAdapter (ArrayList<PlanDay> mealPlan) {
        this.mealPlan = mealPlan;
        Log.i("MealPlanAdapterConstructor", "MealPlanSize is:"+ mealPlan.size());
    }
    public void OnRecyclerViewClickListener(MealPlanAdapter.OnRecyclerViewClickListener listener){
        clickListener = listener;
    }

    @NonNull
    @Override
    public MealPlanDayHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //create listitem from definition
        View planDayView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_plan_day, viewGroup, false);

        //return holder of list items
        return new MealPlanDayHolder(planDayView, clickListener);
    }


    @Override
//Populates the data
    public void onBindViewHolder(@NonNull MealPlanDayHolder mealPlanDayHolder, int position) {
        PlanDay day = mealPlan.get(position);

        Log.i("MealPlanAdapter", "Date: " + day.getDate() + " Position: " + position);
        mealPlanDayHolder.textViewDateString.setText(day.getDate().format(dateTimeFormatter));

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                                                mealPlanDayHolder.mealRecyclerView.getContext(),
                                                LinearLayoutManager.VERTICAL, false);

        layoutManager.setInitialPrefetchItemCount(mealPlan.size());
        DataManager dataManager = new DataManager(mealPlanDayHolder.itemView.getContext());
        ArrayList<Meal> meals = new ArrayList<>();
        meals.addAll(dataManager.getPlanDayMeals(day));
        if(meals.size() == 0) {
            mealPlanDayHolder.textViewDateString.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        }

        MealAdapter mealAdapter = new MealAdapter(meals);
        mealPlanDayHolder.mealRecyclerView.setLayoutManager(layoutManager);
        mealPlanDayHolder.mealRecyclerView.setAdapter(mealAdapter);
        mealPlanDayHolder.mealRecyclerView.setRecycledViewPool(viewPool);

    }

    @Override
    public int getItemCount() {
        return mealPlan.size();
    }

    //implements View.OnClickListener so we can launch PlanDay dialog when
    //user clicks a PlanDay (date value)
    public class MealPlanDayHolder extends RecyclerView.ViewHolder{
        //Vars for item attributes to display
        private TextView textViewDateString;
        private RecyclerView mealRecyclerView;

        //Constructor
        public MealPlanDayHolder (View view, MealPlanAdapter.OnRecyclerViewClickListener clickListener) {
            super(view);

            //item attributes on the view
            textViewDateString = view.findViewById(R.id.textViewDateString);
            mealRecyclerView = view.findViewById(R.id.mealRecyclerView);

            view.setClickable(true);
            //Defined in this class
            //view.setOnClickListener(this);
           view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if (clickListener != null && getAbsoluteAdapterPosition() != RecyclerView.NO_POSITION) {
                        clickListener.OnPlanDayClick(getAbsoluteAdapterPosition());
                    }
                }
            });
        }
    }
}

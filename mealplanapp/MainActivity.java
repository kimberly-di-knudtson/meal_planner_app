package edu.ucdenver.knudtson.mealplanapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //Primary Class objects
    private LocalDate planStartDate;
    private ArrayList<PlanDay> mealPlan;
    private final String START_STATE = "startDay";


    //Activity's Views
    private RecyclerView mealPlanRecyclerView;
    private FloatingActionButton eventFAB;
    private FloatingActionButton recipeBoxFab;

    //Data helpers
    private MealPlanAdapter mealPlanAdapter;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity", "onCreate MainActivity");
        setContentView(R.layout.activity_main);


        //Floating action button to Select Meal Plan start Date
        eventFAB = findViewById(R.id.event_fab);
        //Date Selection Calendar - FAB
        eventFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("MainActivity", "Launching select date dialog");
                SelectDateDialog selectDateDialog = new SelectDateDialog();
                //if I set this tag, does it change the title of the dialog?
                selectDateDialog.show(getSupportFragmentManager(), " ");
            }
        });
        recipeBoxFab = findViewById(R.id.recipe_box_fab);
        recipeBoxFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recipeBoxIntent = new Intent(MainActivity.this, RecipeBoxActivity.class);
                startActivity(recipeBoxIntent);
            }
        });
        //Main has the Meal Plan, recyclerView of PlanDays and dataManager
        mealPlan = new ArrayList<PlanDay>();
        dataManager = new DataManager(this);

        mealPlanRecyclerView = findViewById(R.id.planDayRecyclerView);
        mealPlanAdapter = new MealPlanAdapter(mealPlan);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mealPlanRecyclerView.setLayoutManager(layoutManager);
        //makes a separator between items in recycler view
        mealPlanRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mealPlanRecyclerView.setAdapter(mealPlanAdapter);

        mealPlanAdapter.OnRecyclerViewClickListener(new MealPlanAdapter.OnRecyclerViewClickListener() {
            public void OnPlanDayClick(int position) {
                editPlanDay(mealPlan.get(position).getDaysSinceEpoch());
            }
        });

        //This is to get the date back from the SelectDate Dialog
        //reference - developer.android.com/guide/fragments/communicate
        //Set listener on the child fragmentManager (why "the", one per what?)
        getSupportFragmentManager().setFragmentResultListener("dateSelection", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                planStartDate = LocalDate.ofEpochDay(result.getLong("selected"));
                Log.i("MainActivity", "Setting planStartDate from user selection: " + planStartDate);
                loadMealPlan();
            }
        });
        //Load saved state, if applicable
        if (savedInstanceState != null) {
            long daysSinceEpoch = savedInstanceState.getLong(START_STATE);
            planStartDate = LocalDate.ofEpochDay(daysSinceEpoch);
            Log.i("MainActivity", "Restoring planStartDate from savedState: " + planStartDate);
        }
        if (planStartDate == null) {
            planStartDate = LocalDate.now();
            Log.i("MainActivity", "planStartDate is null, defaulted to now(): " + planStartDate);
        }
    }

    @Override
    //Is there an onResume with savedInstanceState?
    protected void onResume() {
        super.onResume();
        Log.i("MainActivity", "onResume MainActivity - loading MealPlan");
            loadMealPlan();
    }

    //Save the plan start day so that when we return to MealPlan, it's where we left it
    //https://developer.android.com/guide/components/activities/activity-lifecycle#restore-activity-ui-state-using-saved-instance-state

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        long startDaySinceEpoch = planStartDate.toEpochDay();
        Log.i("info", "Saving start date: " + startDaySinceEpoch);
        state.putLong(START_STATE, startDaySinceEpoch);
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        long startDaySinceEpoch = savedInstanceState.getLong(START_STATE);
        Log.i("info", "Restoring start date: " + startDaySinceEpoch);
        planStartDate = LocalDate.ofEpochDay(startDaySinceEpoch);
    }

    //Methods to Launch Activities - TODO - Implement Click on RV


    //Methods to populate data from database
    //Load 7 PlanDays at a time, starting at startDate into MealPlan
    public void loadMealPlan() {
        long startDaysSinceEpoch = planStartDate.toEpochDay();
        long endDaysSinceEpoch = startDaysSinceEpoch + 6;
        Log.i("MainActivity", "Loading plan from: " + startDaysSinceEpoch + " to: " + endDaysSinceEpoch);

        //Initialize 7 Days - unplanned days are not stored in DB
        mealPlan.clear();
        mealPlanAdapter.notifyDataSetChanged();
        mealPlan.addAll(dataManager.getMealPlan(startDaysSinceEpoch, startDaysSinceEpoch + 6));
        //mealPlan = dataManager.getMealPlan(startDaysSinceEpoch, startDaysSinceEpoch+7); - this was bad, caused adapter to lose its reference
        mealPlanAdapter.notifyDataSetChanged();

    }

    public void editPlanDay(long daySinceEpoch) {
        Intent intent = new Intent(MainActivity.this, PlanDayActivity.class);
        intent.putExtra("daySinceEpoch", daySinceEpoch);
        startActivity(intent);
    }
}
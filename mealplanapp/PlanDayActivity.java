package edu.ucdenver.knudtson.mealplanapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static edu.ucdenver.knudtson.mealplanapp.PlanDay.MAX_MEALS;

public class PlanDayActivity extends AppCompatActivity {
    //Primary class objects
    //private long daySinceEpoch;     //primary id
    private PlanDay planDay;        //object
    private ArrayList<Meal> meals;  //list of sub-objects

    private final String PLAN_DAY_STATE = "daySinceEpoch";
    private static final int ADD_MEAL_REQUEST = 1;

    //Activity's Views
    private TextView textViewPlanDayHeader;     //Populate with the date

    //Data helpers
    private MealAdapter mealAdapter;        //meal list ops
    private DataManager dataManager;        //database ops

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(PlanDay.DATE_FORMAT);

    //Do something with constructor?
    public PlanDayActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("info", "onCreate PlanDay Activity");
        setContentView(R.layout.activity_plan_day);

        //content_plan_day.xml references
        textViewPlanDayHeader = findViewById(R.id.textViewPlanDayHeader);
        //List of Meals (in color code, Category: Title)
        RecyclerView mealRecyclerView = findViewById(R.id.mealRecyclerView);

        //activity_plan_day.xml references
        //save mealList
        FloatingActionButton doneFAB = findViewById(R.id.done_fab);
        //launch add meal
        FloatingActionButton addMealFAB = findViewById(R.id.add_meal_fab);
        //close
        FloatingActionButton dismissFAB = findViewById(R.id.dismiss_fab);

        meals = new ArrayList<>();
        dataManager = new DataManager(this);
        mealAdapter = new MealAdapter(meals);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mealRecyclerView.setLayoutManager(layoutManager);
        mealRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
        mealRecyclerView.setAdapter(mealAdapter);

        //button onClickListeners
        doneFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dataManager.savePlanDay(planDay);
                finish();
            }
        });

        //Launch MealActivity - New meal
        addMealFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int numMeals = meals.size();
                if (numMeals == MAX_MEALS) {
                    Toast.makeText(getApplicationContext(), "Meal list is full, MAX_MEALS=" + MAX_MEALS, Toast.LENGTH_LONG).show();
                } else {
                    Intent mealIntent = new Intent(PlanDayActivity.this, MealActivity.class);
                    startActivityForResult(mealIntent, ADD_MEAL_REQUEST);
                }
            }
        });

        dismissFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        mealAdapter.OnRecyclerViewClickListener(new MealAdapter.OnRecyclerViewClickListener() {
            @Override
            public void OnItemClick(int position) {
                Intent mealIntent = new Intent(PlanDayActivity.this, MealActivity.class);
                mealIntent.putExtra("mealId", meals.get(position).getId());
                startActivity(mealIntent);
            }
        });

        mealAdapter.OnRecyclerViewLongClickListener(new MealAdapter.OnRecyclerViewLongClickListener() {
            @Override
            public void OnItemLongClick(int position) {
                meals.remove(position);
                mealAdapter.notifyItemRemoved(position);
            }
        });

        //Load planDay from saved state
        if (savedInstanceState != null) {
            long daySinceEpoch = savedInstanceState.getLong(PLAN_DAY_STATE);
            Log.i("info", "Restoring PlanDay from savedState: " + planDay.getDate());
            planDay = new PlanDay(LocalDate.ofEpochDay(daySinceEpoch));
            planDay.setMealListId(savedInstanceState.getLong("mealListId"));
            planDay.setMealIdsArray(savedInstanceState.getLongArray("references"));
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i("info", "PlanDay-onActivityResult- requestCode: "+requestCode+" resultCode: "+resultCode);
        if (requestCode == ADD_MEAL_REQUEST && resultCode == RESULT_OK) {
            String MEAL_STATE = "mealId";
            long mealId = intent.getLongExtra(MEAL_STATE, 0);
            Log.i("info", "Received from AddMeal: mealId: "+mealId);
            if (mealId != 0) {
                planDay.addMealReference(mealId);
                displayPlanDay();
            } //else do nothing
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("PlanDayActivity", "onResume()");
        Intent intent = getIntent();
        long daySinceEpoch = intent.getLongExtra(PLAN_DAY_STATE, 0); //intent cannot be null - that's what default is for
        if (planDay == null) { //might have been restored from saved state or return from addrecipe and don't want to reload from DB
            Log.i("onResume", "PlanDay State given from intent: " + LocalDate.ofEpochDay(daySinceEpoch));
            planDay = new PlanDay(LocalDate.ofEpochDay(daySinceEpoch));
            loadPlanDay();
        } else {
            Log.i("onResume", "PlanDay activity intent was non-null, day was already initialized: " + LocalDate.ofEpochDay(daySinceEpoch));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        long daySinceEpoch = planDay.getDaysSinceEpoch();
        Log.i("info", "Saving start date: " + daySinceEpoch);
        state.putLong(PLAN_DAY_STATE, daySinceEpoch);
        state.putLong("mealListId", planDay.getMealListId());
        state.putLongArray("references", planDay.getMealIdArray());
        super.onSaveInstanceState(state);
    }

    //Load PlanDay
    public void loadPlanDay(){
        planDay = dataManager.getPlanDay(planDay.getDaysSinceEpoch());
        displayPlanDay();
    }

    public void displayPlanDay(){
        Log.i("PlanDayActivity-displayPlanDay", "Calls loadMeals() to populate ArrayList");
        textViewPlanDayHeader.setText(planDay.getDate().format(dateTimeFormatter));
        loadMeals();
    }

    public void loadMeals() { //I can't call the one in the DB that returns Meals array, b/c then my adapter loses its reference.
        meals.clear();
        for(long mealId : planDay.getMealIdArray()) {
            if (mealId != 0) {
                Log.i("getPlanDayMeals", "Loading Meal, ID: " + mealId);
                Meal meal = dataManager.getMeal(mealId);
                meals.add(meal);
            } //else do not add the meal
        }
        Log.i("PlanDayActivity - loadMeals","loaded: "+meals.size()+" meals.");
        mealAdapter.notifyDataSetChanged();
    }
}


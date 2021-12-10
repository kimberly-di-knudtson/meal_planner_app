package edu.ucdenver.knudtson.mealplanapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

//developer.android.com/guide/fragments/communicate
//Keep "generic" so I can use to select any date I need
public class SelectDateDialog extends DialogFragment {

    //Variables to store reference to edit views
    private CalendarView calendarView;
    private TextView selectedDateView;
    private FloatingActionButton doneFAB;

    //private Calendar calendar;
    private LocalDate selectedDate;
    private long selectedDateSinceEpoch;
    private Instant instant; //needed to get time in Millis from LocalDate...
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(PlanDay.DATE_FORMAT);

    public SelectDateDialog() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        //getActivity() is calling activity
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflator = getActivity().getLayoutInflater();
        //Don't really understand root parameter...,
        View dialogView = inflator.inflate(R.layout.dialog_select_date, null);

        //Find reference to views on the form
        calendarView = dialogView.findViewById(R.id.calendarView);
        selectedDateView = dialogView.findViewById(R.id.selectedDateView);
        doneFAB = dialogView.findViewById(R.id.done_fab);

        //calendar = Calendar.getInstance();
        //Initialize to today
        selectedDate = LocalDate.now();
        selectedDateSinceEpoch = selectedDate.toEpochDay();
        instant = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Log.i("info", "Initialize calendar to : "+ selectedDate.format(dateTimeFormatter));
        //Default bring up now -- consider saving "lastPlanDate" in user prefs
        calendarView.setDate(instant.toEpochMilli(), false,true);
        selectedDateView.setText(selectedDate.format(dateTimeFormatter));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                //Set this to the start day of PlanDay Recycler View
                //calendar.set(year, month, dayOfMonth);
                selectedDate = LocalDate.of(year, month+1, dayOfMonth);
                selectedDateSinceEpoch = selectedDate.toEpochDay();
                instant = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

                Log.i("info", "Set calendar to user selected: "+ selectedDate.format(dateTimeFormatter));
                calendarView.setDate(instant.toEpochMilli(), false,true);
                selectedDateView.setText(selectedDate.format(dateTimeFormatter));
            }
        });
        doneFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("info", "In SelectDateDialog, doneFAB.onClick");
                //Pass back the date to the caller
                Bundle result = new Bundle();
                result.putLong("selected",selectedDateSinceEpoch);
                getParentFragmentManager().setFragmentResult("dateSelection",result);
                dismiss();
            }
        });
        //Does this .setMessage do the dialog title?
        builder.setView(dialogView);  //.setMessage(" ");
        return builder.create();
    }
}
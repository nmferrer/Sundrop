package com.nmferrer.sundrop;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Locale;

//Modelled after the following helper for TimePickerDialog.
//https://stackoverflow.com/questions/17901946/timepicker-dialog-from-clicking-edittext
public class SetDate implements View.OnFocusChangeListener, DatePickerDialog.OnDateSetListener {
    private EditText editText;
    private Calendar myCalendar;
    private Context ctx;

    public SetDate(EditText editText, Context ctx) {
        this.editText = editText;
        this.editText.setOnFocusChangeListener(this);
        this.myCalendar = Calendar.getInstance();
        this.ctx = ctx;
    }
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        Log.d("SETTIME_DEBUG", "focusChange");
        if(hasFocus){
            Log.d("SETTIME_DEBUG", "focusChange:hasFocus:true");
            int year  = myCalendar.get(Calendar.YEAR);
            int month = myCalendar.get(Calendar.MONTH);
            int day = myCalendar.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(ctx, this, year, month, day ).show();
        }
    }
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        month+=1;
        this.editText.setText(String.format(Locale.getDefault(), "%d/%d/%d", month, day, year)); //Calendar.MONTH is 0-based
    }


}

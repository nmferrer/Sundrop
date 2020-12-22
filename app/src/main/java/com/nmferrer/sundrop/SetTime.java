package com.nmferrer.sundrop;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Locale;

//helper for TimePickerDialog. This is really clever.
//https://stackoverflow.com/questions/17901946/timepicker-dialog-from-clicking-edittext
public class SetTime implements View.OnFocusChangeListener, TimePickerDialog.OnTimeSetListener {
    private EditText editText;
    private Calendar myCalendar;
    private Context ctx;

    public SetTime(EditText editText, Context ctx) {
        this.editText = editText;
        this.editText.setOnFocusChangeListener(this);
        this.myCalendar = Calendar.getInstance();
        this.ctx = ctx;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.d("SETTIME_DEBUG", "focusChange");
        if (hasFocus) {
            Log.d("SETTIME_DEBUG", "focusChange:hasFocus:true");
            int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = myCalendar.get(Calendar.MINUTE);
            new TimePickerDialog(ctx, this, hour, minute, false).show();
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String timeSetString = "'";
        if (hourOfDay == 0) {
            timeSetString = 12 + ":" + String.format(Locale.US,"%02d", minute) + " AM";
        }
        else if ( hourOfDay > 0 && hourOfDay < 12) {
            timeSetString = hourOfDay + ":" + String.format(Locale.US, "%02d", minute) + " AM";
        } else if (hourOfDay == 12) {
            timeSetString = hourOfDay + ":" + String.format(Locale.US,"%02d", minute) + " PM";
        } else {
            timeSetString = hourOfDay-12 + ":" + String.format(Locale.US, "%02d", minute) + " PM";
        }
        this.editText.setText(timeSetString);
    }
}
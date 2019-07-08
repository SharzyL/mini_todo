package com.example.empty;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AddItemActivity extends AppCompatActivity {

    TodoItemDB db;
    TextView editItemTitle;
    TextView editItemDetail;
    TextView dateView;
    Date alarmDate = null;
    LinearLayout alarmRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        db = TodoItemDB.getInstance(getApplicationContext());
        editItemTitle = findViewById(R.id.editItemTitle);
        editItemDetail = findViewById(R.id.editItemDetail);
        dateView = findViewById(R.id.dateView);
        alarmRow =findViewById(R.id.alarmRow);

        Toolbar myToolbar = findViewById(R.id.addItemToolBar);
        myToolbar.inflateMenu(R.menu.menu_add_item);
        setSupportActionBar(myToolbar);

        if (itemCache.state == itemCache.STATE.BEFORE_EDIT) {
            TodoItem item = itemCache.get();
            editItemTitle.setText(item.title);
            editItemDetail.setText(item.detail);
            if (item.setAlarmed) {
                alarmDate = item.alarmDate;
                dateView.setText(DateConverter.toString(alarmDate));
            } else {
                alarmRow.setVisibility(View.GONE);
            }
        }

        if (itemCache.state == itemCache.STATE.BEFORE_ADD) {
            findViewById(R.id.toolAlarm).setVisibility(View.GONE);
            alarmRow.setVisibility(View.GONE);
        }

        findViewById(R.id.btnDeleteAlarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmDate = null;
                alarmRow.setVisibility(View.GONE);
                findViewById(R.id.toolAlarm).setVisibility(View.VISIBLE);
            }
        });

    }

    void alert(String text) {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string._warning))
                .setMessage(text)
                .setPositiveButton(
                        getResources().getText(R.string._get_it),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                .create()
                .show();
    }

    void setAlarmDate() {
        final Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, final int year, final int month, final int day) {

                new TimePickerDialog(AddItemActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {

                        alarmDate = new GregorianCalendar(year, month, day, hour, minute, 0).getTime();
                        findViewById(R.id.toolAlarm).setVisibility(View.GONE);
                        alarmRow.setVisibility(View.VISIBLE);
                        dateView.setText(
                                DateConverter.toString(alarmDate)
                        );

                    }
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();

            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    void submit() {
        String itemTitle = editItemTitle.getText().toString();
        String itemDetail = editItemDetail.getText().toString();
        String alertText = "";
        if (alarmDate != null && alarmDate.before(new Date())) {
            alertText += getResources().getString(R.string._early_alarm) + "\n";
        }
        if (itemTitle.isEmpty()) {
            alertText += getResources().getString(R.string._empty_title) + "\n";
        }
        if (!alertText.isEmpty()) {
            alert(alertText.substring(0, alertText.length() - 1));
            return;
        }
        switch (itemCache.state) {
            case BEFORE_ADD:
                itemCache.set(new TodoItem(itemTitle, itemDetail, alarmDate));
                itemCache.state = itemCache.STATE.AFTER_ADD;
                AddItemActivity.this.finish();
                break;
            case BEFORE_EDIT:
                TodoItem item = itemCache.get();
                item.title = itemTitle;
                item.detail = itemDetail;
                item.setAlarmDate(alarmDate);
                itemCache.state = itemCache.STATE.AFTER_EDIT;
                AddItemActivity.this.finish();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolAlarm:
                setAlarmDate();
                return true;
            case R.id.toolDelete:
                if (itemCache.state == itemCache.STATE.BEFORE_EDIT) {
                    itemCache.state = itemCache.STATE.TO_BE_DELETED;
                }
                AddItemActivity.this.finish();
                return true;
            case R.id.toolSubmit:
                submit();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}

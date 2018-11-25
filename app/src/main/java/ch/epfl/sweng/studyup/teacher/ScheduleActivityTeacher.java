package ch.epfl.sweng.studyup.teacher;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ch.epfl.sweng.studyup.R;
import ch.epfl.sweng.studyup.utils.Utils;
import ch.epfl.sweng.studyup.utils.navigation.NavigationTeacher;

import static ch.epfl.sweng.studyup.utils.Constants.MONTH_OF_SCHEDULE;
import static ch.epfl.sweng.studyup.utils.Constants.YEAR_OF_SCHEDULE;
import static ch.epfl.sweng.studyup.utils.GlobalAccessVariables.MOCK_ENABLED;

public class ScheduleActivityTeacher extends NavigationTeacher {
    private List<WeekViewEvent> weekViewEvents;
    private WeekView weekView;
    private int id = 0;
    private String courseName;
    public static final String COURSE_NAME_INTENT_SCHEDULE = "CourseName";

    private final MonthLoader.MonthChangeListener monthChangeListener = new MonthLoader.MonthChangeListener() {
        @Override
        public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
            if(newMonth == MONTH_OF_SCHEDULE + 1 && newYear == YEAR_OF_SCHEDULE) {
                return weekViewEvents;
            }else{
                return new ArrayList<>();
            }
        }
    };

    private final WeekView.EventClickListener eventClickListener = new WeekView.EventClickListener() {
        @Override
        public void onEventClick(WeekViewEvent event, RectF eventRect) {
            Log.d("ScheduleActivityTeacher", "Clicked on event with id " + event.getId());
            weekViewEvents.remove(event);
            weekView.notifyDatasetChanged();
        }
    };


    private final WeekView.EmptyViewClickListener emptyViewClickListener = new WeekView.EmptyViewClickListener() {
        @Override
        public void onEmptyViewClicked(Calendar time) {
            Log.d("ScheduleActivityTeacher", "time = " + time.toString());
            Log.d("ScheduleActivityTeacher", "Day of month = " + time.get(Calendar.DAY_OF_MONTH));
            Log.d("ScheduleActivityTeacher", "Hour = " + time.get(Calendar.HOUR_OF_DAY));
            int day = time.get(Calendar.DAY_OF_MONTH);
            int hour = time.get(Calendar.HOUR_OF_DAY);

            Calendar eventStart = Calendar.getInstance();
            eventStart.set(Calendar.YEAR, YEAR_OF_SCHEDULE);
            eventStart.set(Calendar.MONTH, MONTH_OF_SCHEDULE);
            eventStart.set(Calendar.DAY_OF_MONTH, day);
            eventStart.set(Calendar.HOUR_OF_DAY, hour);
            eventStart.set(Calendar.MINUTE, 0);

            Calendar eventEnd = Calendar.getInstance();
            eventEnd.set(Calendar.YEAR, YEAR_OF_SCHEDULE);
            eventEnd.set(Calendar.MONTH, MONTH_OF_SCHEDULE);
            eventEnd.set(Calendar.DAY_OF_MONTH, day);
            eventEnd.set(Calendar.HOUR_OF_DAY, hour);
            eventEnd.set(Calendar.MINUTE, 59);

            weekViewEvents.add(new WeekViewEvent(id, courseName + '\n' + "CO_0_1", eventStart, eventEnd));
            id += 1;
            weekView.notifyDatasetChanged();
        }
    };


    private final WeekView.EventLongPressListener eventLongPressListener = new WeekView.EventLongPressListener() {
        @Override
        public void onEventLongPress(WeekViewEvent event, RectF eventRect) {}};

    private final DateTimeInterpreter dateTimeInterpreter = new DateTimeInterpreter() {
        @Override
        public String interpretDate(Calendar date) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
                return sdf.format(date.getTime()).toUpperCase();
            } catch (Exception e) {
                e.printStackTrace(); return "";
            }
        }

        @Override
        public String interpretTime(int hour, int minutes) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, 0);

            try {
                SimpleDateFormat sdf = DateFormat.is24HourFormat(getApplicationContext()) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("hh a", Locale.getDefault());
                return sdf.format(calendar.getTime());
            } catch (Exception e) {
                e.printStackTrace(); return "";
            }
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_teacher);

        weekViewEvents = new ArrayList<>();
        weekView = findViewById(R.id.weekView);

        if(MOCK_ENABLED){
            weekView.setNumberOfVisibleDays(1);
        }

        courseName = getIntent().getStringExtra(COURSE_NAME_INTENT_SCHEDULE);
        Utils.setupWeekView(weekView, eventLongPressListener, dateTimeInterpreter, monthChangeListener, eventClickListener, emptyViewClickListener);
    }



    public void updateSchedule(List<WeekViewEvent> events){
        weekViewEvents.clear();
        for(WeekViewEvent event : events){
            weekViewEvents.add(event);
        }
        id += events.size();
        weekView.notifyDatasetChanged();
    }

    public List<WeekViewEvent> getWeekViewEvents(){
        return new ArrayList<>(weekViewEvents);
    }

    public void onSaveButtonClick(View view){
        //Save WeekViewEvents on firebase
    }
}

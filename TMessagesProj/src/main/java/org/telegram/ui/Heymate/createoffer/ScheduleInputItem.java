package org.telegram.ui.Heymate.createoffer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import works.heymate.beta.R;
import works.heymate.core.Texts;

public class ScheduleInputItem extends ExpandableItem {

    private static final int REMOVE = 1;
    private static final int DATE = 2;
    private static final int START_TIME = 3;
    private static final int END_TIME = 4;

    private LinearLayout mContainerTimeSlot;

    public ScheduleInputItem(@NonNull Context context) {
        super(context);
        setTitle("Schedule"); // TODO Texts
        setIcon(AppCompatResources.getDrawable(context, R.drawable.watch_later_24_px_1));
    }

    @Override
    protected View createContent() {
        mContainerTimeSlot = new LinearLayout(getContext());
        mContainerTimeSlot.setOrientation(LinearLayout.VERTICAL);

        LinearLayout addNewSchedule = new LinearLayout(getContext());
        addNewSchedule.setOrientation(LinearLayout.HORIZONTAL);
        addNewSchedule.setOnClickListener(this::addNewTimeSlot);

        AppCompatImageView addIcon = new AppCompatImageView(getContext());
        addIcon.setImageDrawable(Theme.getThemedDrawable(getContext(), R.drawable.add, Theme.key_dialogFloatingIcon));
        addIcon.setPadding(AndroidUtilities.dp(6), AndroidUtilities.dp(6), AndroidUtilities.dp(6), AndroidUtilities.dp(6));
        addIcon.setBackgroundDrawable(Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(20), ContextCompat.getColor(getContext(), R.color.ht_theme), ContextCompat.getColor(getContext(), R.color.ht_theme)));
        addNewSchedule.addView(addIcon, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, HEADER_LEFT_MARGIN + 8, 12, 16, 12));

        TextView addText = new TextView(getContext());
        addText.setTextSize(14);
        addText.setTextColor(ContextCompat.getColor(getContext(), R.color.ht_theme));
        addText.setText("Add New Schedule");
        addNewSchedule.addView(addText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

        mContainerTimeSlot.addView(addNewSchedule, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        return mContainerTimeSlot;
    }

    private void addTimeSlot(long startTime, long endTime) {
        List<Long> times = Arrays.asList(startTime, endTime);

        LinearLayout timeSlotLayout = new LinearLayout(getContext());
        timeSlotLayout.setOrientation(LinearLayout.HORIZONTAL);
        timeSlotLayout.setTag(times);

        ImageView remove = new ImageView(getContext());
        remove.setId(REMOVE);
        remove.setImageDrawable(Theme.getThemedDrawable(getContext(), R.drawable.ic_close_white, Theme.key_windowBackgroundWhiteGrayText));
        remove.setOnClickListener(v -> {
            mContainerTimeSlot.removeView(timeSlotLayout);
            updateLayoutHeight();
        });
        remove.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        timeSlotLayout.addView(remove, LayoutHelper.createLinear(36, 36, Gravity.CENTER_VERTICAL));

        TextView date = new TextView(getContext());
        date.setId(DATE);
        date.setTextSize(14);
        date.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        date.setMaxLines(1);
        date.setSingleLine();
        date.setEllipsize(TextUtils.TruncateAt.START);
        timeSlotLayout.addView(date, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1, Gravity.CENTER_VERTICAL, AndroidUtilities.dp(4), 0, 0, 0));

        TextView start = new TextView(getContext());
        start.setId(START_TIME);
        start.setTextSize(14);
        start.setTextColor(ContextCompat.getColor(getContext(), R.color.ht_theme));
        start.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
        start.setGravity(Gravity.CENTER_VERTICAL);
        start.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(((List<Long>) timeSlotLayout.getTag()).get(0));

            TimePickerDialog picker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                List<Long> originalTimes = (List<Long>) timeSlotLayout.getTag();

                if (originalTimes.get(1) < calendar.getTimeInMillis()) {
                    Toast.makeText(getContext(), "Start time can not be after end time.", Toast.LENGTH_SHORT).show();
                    return;
                }

                originalTimes.set(0, calendar.getTimeInMillis());

                timeSlotLayout.setTag(originalTimes);
                updateTimeSlot(timeSlotLayout);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            picker.setTitle("Start time");
            picker.show();
        });
        timeSlotLayout.addView(start, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT));

        TextView to = new TextView(getContext());
        to.setTextSize(14);
        to.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        to.setText("to");
        to.setGravity(Gravity.CENTER_VERTICAL);
        timeSlotLayout.addView(to, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT));

        TextView end = new TextView(getContext());
        end.setId(END_TIME);
        end.setTextSize(14);
        end.setTextColor(ContextCompat.getColor(getContext(), R.color.ht_theme));
        end.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
        end.setGravity(Gravity.CENTER_VERTICAL);
        end.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(((List<Long>) timeSlotLayout.getTag()).get(1));

            TimePickerDialog picker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                List<Long> originalTimes = (List<Long>) timeSlotLayout.getTag();

                if (originalTimes.get(0) > calendar.getTimeInMillis()) {
                    Toast.makeText(getContext(), "End time can not be before start time.", Toast.LENGTH_SHORT).show();
                    return;
                }

                originalTimes.set(1, calendar.getTimeInMillis());

                timeSlotLayout.setTag(originalTimes);
                updateTimeSlot(timeSlotLayout);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            picker.setTitle("End time");
            picker.show();
        });
        timeSlotLayout.addView(end, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT));

        updateTimeSlot(timeSlotLayout);

        mContainerTimeSlot.addView(timeSlotLayout, 0, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 56, HEADER_LEFT_MARGIN, 0, HEADER_RIGHT_MARGIN, 0));

        updateLayoutHeight();
    }

    private void updateTimeSlot(View view) {
        List<Long> times = (List<Long>) view.getTag();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(times.get(0));

        StringBuilder date = new StringBuilder();
        date.append(Texts.get(Texts.WEEK_DAYS_SHORT[calendar.get(Calendar.DAY_OF_WEEK) - 1]))
                .append(", ")
                .append(calendar.get(Calendar.MONTH))
                .append(".")
                .append(calendar.get(Calendar.DAY_OF_MONTH))
                .append(".")
                .append(calendar.get(Calendar.YEAR));
        ((TextView) view.findViewById(DATE)).setText(date);

        updateTime(view.findViewById(START_TIME), calendar);

        calendar.setTimeInMillis(times.get(1));
        updateTime(view.findViewById(END_TIME), calendar);
    }

    private void updateTime(TextView text, Calendar calendar) {
        StringBuilder time = new StringBuilder();
        time.append(ensureNumber(calendar.get(Calendar.HOUR_OF_DAY)))
                .append(":")
                .append(ensureNumber(calendar.get(Calendar.MINUTE)));
        text.setText(time);
    }

    private void addNewTimeSlot(View v) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            TimePickerDialog startTimePicker = new TimePickerDialog(getContext(), (view1, startHour, startMinute) -> {
                TimePickerDialog endTimePicker = new TimePickerDialog(getContext(), (view2, endHour, endMinute) -> {
                    if (endHour < startHour || (endHour == startHour && endMinute <= startMinute)) {
                        Toast.makeText(getContext(), "End time must be after start time.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    calendar.set(Calendar.HOUR_OF_DAY, startHour);
                    calendar.set(Calendar.MINUTE, startMinute);
                    long startTime = calendar.getTimeInMillis();

                    calendar.set(Calendar.HOUR_OF_DAY, endHour);
                    calendar.set(Calendar.MINUTE, endMinute);
                    long endTime = calendar.getTimeInMillis();

                    addTimeSlot(startTime, endTime);
                }, 0, 0, true);
                endTimePicker.setTitle("End time");
                endTimePicker.show();
            }, 0, 0, true);
            startTimePicker.setTitle("Start time");
            startTimePicker.show();
        }, currentYear, currentMonth, currentDay);
        datePicker.setTitle(works.heymate.beta.R.string.HtSelectDate);
        datePicker.show();
    }

    public ArrayList<Long> getTimeSlots() {
        ArrayList<Long> times = new ArrayList<>((mContainerTimeSlot.getChildCount() - 1) * 2);

        for (int i = 0; i < mContainerTimeSlot.getChildCount() - 1; i++) {
            times.addAll((List<Long>) mContainerTimeSlot.getChildAt(i).getTag());
        }

        return times;
    }

    public void setTimeSlots(List<Long> times) {
        while (mContainerTimeSlot.getChildCount() > 1) {
            mContainerTimeSlot.removeViewAt(0);
        }

        if (times == null) {
            return;
        }

        for (int i = times.size() - 2; i >= 0; i -= 2) {
            addTimeSlot(times.get(i), times.get(i + 1));
        }
    }

    private static String ensureNumber(int number) {
        if (number >= 10) {
            return String.valueOf(number);
        }
        else {
            return "0" + number;
        }
    }

}

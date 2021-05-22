package org.telegram.ui.Heymate.createoffer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;


import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadioButton;
import org.telegram.ui.Heymate.createoffer.HtCreateOfferActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HtCalendarBottomSheet extends BottomSheet implements NotificationCenter.NotificationCenterDelegate, BottomSheet.BottomSheetDelegateInterface {

    private Context context;
    private RelativeLayout parentLayout;
    private int i = 0;
    private View prevView;
    private HashMap<Integer, Boolean> checkBoxesState;
    private HashMap<Integer, Long> selectedStartDates;
    private HashMap<Integer, Long> selectedEndDates;

    public HtCalendarBottomSheet(Context context, boolean needFocus, HtCreateOfferActivity parent) {
        super(context, needFocus);
        this.context = context;
        initSheet(context, parent);
    }

    public void initSheet(Context context, HtCreateOfferActivity parent) {
        setDisableScroll(true);
        containerView = new LinearLayout(context);
        checkBoxesState = new HashMap<>();
        selectedStartDates = new HashMap<>();
        selectedEndDates = new HashMap<>();

        ScrollView scrollView = new ScrollView(context);

        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setMinimumHeight(500);
        mainLayout.setBackgroundColor(Theme.getColor(Theme.key_wallet_whiteBackground));
        mainLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(18), Theme.getColor(Theme.key_wallet_whiteBackground)));
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout applyLayout = new LinearLayout(context);
        applyLayout.setGravity(Gravity.RIGHT);

        ImageView applyImage = new ImageView(context);
        Drawable applyDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.ht_check_circle);
        applyDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(works.heymate.beta.R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        applyImage.setImageDrawable(applyDrawable);
        applyLayout.addView(applyImage, LayoutHelper.createLinear(30, 30));

        applyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.setDateSlots(getDates());
                dismiss();
            }
        });

        mainLayout.addView(applyLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 20, 20, 0));

        RelativeLayout calendarAddLayout = new RelativeLayout(context);

        ImageView addDateImage = new ImageView(context);
        addDateImage.setId(++i);
        Drawable addDateDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.menu_add);
        addDateDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(works.heymate.beta.R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        addDateImage.setImageDrawable(addDateDrawable);
        RelativeLayout.LayoutParams addDateImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(25), AndroidUtilities.dp(25));
        addDateImageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        addDateImageLayoutParams.setMargins(AndroidUtilities.dp(15), 0, 0, AndroidUtilities.dp(5));
        calendarAddLayout.addView(addDateImage, addDateImageLayoutParams);

        ImageView calendarImage = new ImageView(context);
        calendarImage.setId(++i);
        Drawable calendarDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.msg_calendar);
        calendarDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(works.heymate.beta.R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        calendarImage.setImageDrawable(calendarDrawable);
        addDateImage.setOnClickListener(new AddDateOnClickListener(context, calendarAddLayout));
        calendarImage.setOnClickListener(new AddDateOnClickListener(context, calendarAddLayout));
        parentLayout = calendarAddLayout;
        RelativeLayout.LayoutParams calendarImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(25), AndroidUtilities.dp(25));
        calendarImageLayoutParams.addRule(RelativeLayout.RIGHT_OF, addDateImage.getId());
        calendarImageLayoutParams.setMargins(AndroidUtilities.dp(5), 0, 0, AndroidUtilities.dp(15));
        calendarAddLayout.addView(calendarImage, calendarImageLayoutParams);

        TextView addDateText = new TextView(context);
        addDateText.setId(++i);
        addDateText.setText(LocaleController.getString("HtAddNewDate", works.heymate.beta.R.string.HtAddNewDate));
        addDateText.setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        addDateText.setOnClickListener(new AddDateOnClickListener(context, calendarAddLayout));
        RelativeLayout.LayoutParams addDateTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addDateTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, calendarImage.getId());
        addDateTextLayoutParams.setMargins(AndroidUtilities.dp(5), 0, 0, AndroidUtilities.dp(15));
        addDateText.setOnClickListener(new AddDateOnClickListener(context, calendarAddLayout));
        calendarAddLayout.addView(addDateText, addDateTextLayoutParams);

        prevView = calendarImage;

        mainLayout.addView(calendarAddLayout, LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        scrollView.addView(mainLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        containerView.addView(scrollView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

    }

    @Override
    public void onOpenAnimationStart() {

    }

    @Override
    public void onOpenAnimationEnd() {

    }

    @Override
    public boolean canDismiss() {
        return false;
    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class AddDateOnClickListener implements View.OnClickListener {

        private Context context;
        private RelativeLayout parentLayout;

        public AddDateOnClickListener(Context context, RelativeLayout parentLayout) {
            this.context = context;
            this.parentLayout = parentLayout;
        }

        @Override
        public void onClick(View v) {
            Calendar mcurrentTime = Calendar.getInstance();
            int cyear = mcurrentTime.get(Calendar.YEAR);
            int cmonth = mcurrentTime.get(Calendar.MONTH);
            int cday = mcurrentTime.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog mTimePicker;
            mTimePicker = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    HashMap<String, Integer> extras = new HashMap<>();
                    extras.put("DateYear", year);
                    extras.put("DateMonth", month);
                    extras.put("DateDay", dayOfMonth);
                    HtTimeSlotView timeSlotView = new HtTimeSlotView(context, parentLayout, extras);
                }
            }, cyear, cmonth, cday);
            mTimePicker.setTitle(LocaleController.getString("HtSelectDate", works.heymate.beta.R.string.HtSelectDate));
            mTimePicker.show();


        }
    }

    private class SetTimeOnClickListener implements View.OnClickListener {

        private Context context;
        private TextView target;

        public SetTimeOnClickListener(Context context, TextView target) {
            this.context = context;
            this.target = target;
        }

        @Override
        public void onClick(View v) {
            TimePickerDialog mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                    target.setText(simpleDateFormat.format(new Date(0, 0, 0, hourOfDay, minute)));
                }
            }, 0, 0, true);

            mTimePicker.show();
        }
    }

    public class HtTimeSlotView {
        public HtTimeSlotView(Context context, RelativeLayout parentLayout, HashMap<String, Integer> extras) {
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd-MM-yyyy");
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(Calendar.YEAR, extras.get("DateYear"));
            selectedDate.set(Calendar.MONTH, extras.get("DateMonth"));
            selectedDate.set(Calendar.DAY_OF_MONTH, extras.get("DateDay"));

            RadioButton newDateCheckBox = new RadioButton(context);
            newDateCheckBox.setSize(AndroidUtilities.dp(20));
            newDateCheckBox.setColor(Theme.getColor(Theme.key_graySection), context.getResources().getColor(works.heymate.beta.R.color.ht_green));
            newDateCheckBox.setChecked(true, true);
            newDateCheckBox.setId(++i);
            newDateCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBoxesState.put(newDateCheckBox.getId(), !checkBoxesState.get(newDateCheckBox.getId()));
                    newDateCheckBox.setChecked(checkBoxesState.get(newDateCheckBox.getId()), true);
                }
            });
            checkBoxesState.put(newDateCheckBox.getId(), true);
            RelativeLayout.LayoutParams newDateCheckBoxLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(20), AndroidUtilities.dp(20));
            newDateCheckBoxLayoutParams.addRule(RelativeLayout.BELOW, prevView.getId());
            newDateCheckBoxLayoutParams.setMargins(AndroidUtilities.dp(45), AndroidUtilities.dp(10), 0, AndroidUtilities.dp(25));
            parentLayout.addView(newDateCheckBox, newDateCheckBoxLayoutParams);

            TextView newDateText = new TextView(context);
            newDateText.setTextSize(16);
            newDateText.setId(++i);
            newDateText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            newDateText.setText(selectedDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + " " + simpleDateFormat2.format(selectedDate.getTime()));
            RelativeLayout.LayoutParams newDateTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            newDateTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, newDateCheckBox.getId());
            newDateTextLayoutParams.addRule(RelativeLayout.BELOW, prevView.getId());
            newDateTextLayoutParams.setMargins(AndroidUtilities.dp(10), AndroidUtilities.dp(10), 0, AndroidUtilities.dp(25));
            parentLayout.addView(newDateText, newDateTextLayoutParams);

            EditTextBoldCursor startTimeText = new EditTextBoldCursor(context);
            startTimeText.setClickable(true);
            startTimeText.setFocusable(false);
            startTimeText.setInputType(InputType.TYPE_NULL);
            startTimeText.setTextSize(16);
            startTimeText.setId(++i);
            startTimeText.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
            startTimeText.setText("00:00");
            startTimeText.setBackgroundResource(works.heymate.beta.R.drawable.border);
            RelativeLayout.LayoutParams startTimeTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            startTimeTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, newDateText.getId());
            startTimeTextLayoutParams.addRule(RelativeLayout.BELOW, prevView.getId());
            startTimeTextLayoutParams.setMargins(AndroidUtilities.dp(10), 0, 0, AndroidUtilities.dp(45));
            parentLayout.addView(startTimeText, startTimeTextLayoutParams);

            startTimeText.setOnClickListener(new SetTimeOnClickListener(context, startTimeText));

            TextView toText = new TextView(context);
            toText.setTextSize(16);
            toText.setId(++i);
            toText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            toText.setText("to");
            RelativeLayout.LayoutParams toTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            toTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, startTimeText.getId());
            toTextLayoutParams.addRule(RelativeLayout.BELOW, prevView.getId());
            toTextLayoutParams.setMargins(AndroidUtilities.dp(10), AndroidUtilities.dp(10), 0, AndroidUtilities.dp(25));
            parentLayout.addView(toText, toTextLayoutParams);

            EditTextBoldCursor endTimeText = new EditTextBoldCursor(context);
            endTimeText.setClickable(true);
            endTimeText.setFocusable(false);
            endTimeText.setInputType(InputType.TYPE_NULL);
            endTimeText.setTextSize(16);
            endTimeText.setId(++i);
            endTimeText.setBackgroundResource(works.heymate.beta.R.drawable.border);
            endTimeText.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
            endTimeText.setText("00:00");
            RelativeLayout.LayoutParams endTimeTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            endTimeTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, toText.getId());
            endTimeTextLayoutParams.addRule(RelativeLayout.BELOW, prevView.getId());
            endTimeTextLayoutParams.setMargins(AndroidUtilities.dp(10), 0, 0, AndroidUtilities.dp(55));
            parentLayout.addView(endTimeText, endTimeTextLayoutParams);

            prevView = newDateCheckBox;

            endTimeText.setOnClickListener(new SetTimeOnClickListener(context, endTimeText));

            TimePickerDialog mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    extras.put("StartYear", extras.get("DateYear"));
                    extras.put("StartMonth", extras.get("DateMonth"));
                    extras.put("StartDay", extras.get("DateDay"));
                    extras.put("StartHour", hourOfDay);
                    extras.put("StartMinute", minute);
                    startDateSelected(context, startTimeText, endTimeText, newDateCheckBox, extras);
                }
            }, 0, 0, true);
            mTimePicker.setTitle(LocaleController.getString("HtSelectStartTime", works.heymate.beta.R.string.HtSelectStartTime));
            if (extras.get("StartYear") == null)
                mTimePicker.show();
            else {
                startDateSelected(context, startTimeText, endTimeText, newDateCheckBox, extras);
            }
        }

        public void startDateSelected(Context context, TextView startTimeText, TextView endTimeText, RadioButton newDateCheckBox, HashMap<String, Integer> extras) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            startTimeText.setText(simpleDateFormat.format(new Date(0, 0, 0, extras.get("StartHour"), extras.get("StartMinute"))));
            Calendar startTimeCal = Calendar.getInstance();
            startTimeCal.set(Calendar.YEAR, extras.get("StartYear"));
            startTimeCal.set(Calendar.MONTH, extras.get("StartMonth"));
            startTimeCal.set(Calendar.DAY_OF_MONTH, extras.get("StartDay"));
            startTimeCal.set(Calendar.HOUR_OF_DAY, extras.get("StartHour"));
            startTimeCal.set(Calendar.MINUTE, extras.get("StartMinute"));
            selectedStartDates.put(newDateCheckBox.getId(), startTimeCal.toInstant().toEpochMilli());
            TimePickerDialog mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    extras.put("EndYear", extras.get("DateYear"));
                    extras.put("EndMonth", extras.get("DateMonth"));
                    extras.put("EndDay", extras.get("DateDay"));
                    extras.put("EndHour", hourOfDay);
                    extras.put("EndMinute", minute);
                    endDateSelected(endTimeText, newDateCheckBox, extras);
                }
            }, 0, 0, true);
            mTimePicker.setTitle(LocaleController.getString("HtSelectEndTime", works.heymate.beta.R.string.HtSelectEndTime));
            if (extras.get("EndYear") == null)
                mTimePicker.show();
            else {
                endDateSelected(endTimeText, newDateCheckBox, extras);
            }

        }
    }

    public void endDateSelected(TextView endTimeText, RadioButton newDateCheckBox, HashMap<String, Integer> extras) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        endTimeText.setText(simpleDateFormat.format(new Date(0, 0, 0, extras.get("EndHour"), extras.get("EndMinute"))));
        Calendar endTimeCal = Calendar.getInstance();
        endTimeCal.set(Calendar.YEAR, extras.get("EndYear"));
        endTimeCal.set(Calendar.MONTH, extras.get("EndMonth"));
        endTimeCal.set(Calendar.DAY_OF_MONTH, extras.get("EndDay"));
        endTimeCal.set(Calendar.HOUR_OF_DAY, extras.get("EndHour"));
        endTimeCal.set(Calendar.MINUTE, extras.get("EndMinute"));
        selectedEndDates.put(newDateCheckBox.getId(), endTimeCal.toInstant().toEpochMilli());
    }

    public ArrayList<Long> getDates() {
        ArrayList<Long> dates = new ArrayList<>();
        for (int key : checkBoxesState.keySet()) {
            if (checkBoxesState.get(key)) {
                dates.add(selectedStartDates.get(key));
                dates.add(selectedEndDates.get(key));
            }
        }
        return dates;
    }

    public void setDates(ArrayList<Long> dates) {
        for (int i = 0; i < dates.size(); i += 2) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTimeInMillis(dates.get(i));
            HashMap<String, Integer> extras = new HashMap<>();

            extras.put("DateYear", cal1.get(Calendar.YEAR));
            extras.put("DateMonth", cal1.get(Calendar.MONTH));
            extras.put("DateDay", cal1.get(Calendar.DAY_OF_MONTH));

            extras.put("StartYear", cal1.get(Calendar.YEAR));
            extras.put("StartMonth", cal1.get(Calendar.MONTH));
            extras.put("StartDay", cal1.get(Calendar.DAY_OF_MONTH));
            extras.put("StartHour", cal1.get(Calendar.HOUR_OF_DAY));
            extras.put("StartMinute", cal1.get(Calendar.MINUTE));

            cal1.setTimeInMillis(dates.get(i+1));
            extras.put("EndYear", cal1.get(Calendar.YEAR));
            extras.put("EndMonth", cal1.get(Calendar.MONTH));
            extras.put("EndDay", cal1.get(Calendar.DAY_OF_MONTH));
            extras.put("EndHour", cal1.get(Calendar.HOUR_OF_DAY));
            extras.put("EndMinute", cal1.get(Calendar.MINUTE));

            HtTimeSlotView timeSlotView = new HtTimeSlotView(context, parentLayout, extras);
        }

    }
}

package org.telegram.ui.Heymate;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.amplifyframework.datastore.generated.model.TimeSlot;

import org.telegram.messenger.AndroidUtilities;
import works.heymate.beta.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.widget.TimeSlotPicker;
import org.telegram.ui.Heymate.widget.TimeSlotPickerAdapter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import works.heymate.core.Texts;

public class TimeSlotSelectionActivity extends BaseFragment {

    public interface ResultReceiver {

        void onResult(TimeSlot timeSlot);

    }

    private ResultReceiver mResultReceiver;

    private TimeZone mTimeZone;

    private List<TimeSlotPickerAdapter.TimeSlot> mTimeSlots;
    private Map<TimeSlotPickerAdapter.TimeSlot, TimeSlot> mTimeSlotsMap;

    private TimeSlotPicker mTimeSlotPicker;

    public TimeSlotSelectionActivity(TimeZone timeZone, ArrayList<TimeSlot> timeSlots, ResultReceiver resultReceiver) {
        mResultReceiver = resultReceiver;

        mTimeZone = timeZone;
        mTimeSlots = new ArrayList<>(timeSlots.size());
        mTimeSlotsMap = new Hashtable<>(timeSlots.size());

        for (TimeSlot timeSlot: timeSlots) {
            long start = timeSlot.getStartTime() * 1000L;
            long end = timeSlot.getEndTime() * 1000L;
            int duration = (int) ((end - start) / 1000L / 60L);
            boolean reserved = timeSlot.getRemainingReservations() <= 0;

            TimeSlotPickerAdapter.TimeSlot pickerTimeSlot = new TimeSlotPickerAdapter.TimeSlot(start, duration, reserved);

            mTimeSlots.add(pickerTimeSlot);
            mTimeSlotsMap.put(pickerTimeSlot, timeSlot);
        }
    }

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        FrameLayout content = new FrameLayout(context);

        mTimeSlotPicker = new TimeSlotPicker(context);
        mTimeSlotPicker.setAvailableTimeSlotColor(ContextCompat.getColor(context, works.heymate.beta.R.color.ht_theme));
        mTimeSlotPicker.setButtonsBackgroundColor(Theme.getColor(Theme.key_chats_actionBackground));
        mTimeSlotPicker.setButtonsColor(Theme.getColor(Theme.key_chats_actionIcon));
        mTimeSlotPicker.setDateColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        mTimeSlotPicker.setDayOfWeekColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mTimeSlotPicker.setDayTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTimeSlotPicker.setHourTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mTimeSlotPicker.setReservedTimeSlotColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mTimeSlotPicker.setSelectedTimeSlotColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
        mTimeSlotPicker.setUnavailableTimeSlotColor(Theme.getColor(Theme.key_divider));
        mTimeSlotPicker.setDateTextSize(AndroidUtilities.dp(10));
        mTimeSlotPicker.setDayOfWeekTextSize(AndroidUtilities.dp(13));
        mTimeSlotPicker.setDayTextSize(AndroidUtilities.dp(14));
        mTimeSlotPicker.setHourTextSize(AndroidUtilities.dp(12));
//        mTimeSlotPicker.setDayTypeface(AndroidUtilities.getTypeface("fonts/mw_bold.ttf"));
//        mTimeSlotPicker.setDayOfWeekTypeface(AndroidUtilities.getTypeface("fonts/mw_bold.ttf"));
//        mTimeSlotPicker.setDateTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        mTimeSlotPicker.setHourTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

        mTimeSlotPicker.setTimeZone(mTimeZone);

        content.addView(mTimeSlotPicker, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0, 16, 16, 16, 16));

        ActionBar actionBar = getActionBar();
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setTitle(Texts.get(Texts.TIMESLOTSSELECTED_TITLE));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (getParentActivity() == null) {
                    return;
                }

                if (id == -1) {
                    finishFragment();
                }
            }
        });

        mTimeSlotPicker.setSelectionMode(TimeSlotPicker.SELECTION_MODE_SINGLE);

        mTimeSlotPicker.setAdapter(new TimeSlotPickerAdapter() {

            @Override
            public TimeZone getTimeZone() {
                return mTimeZone;
            }

            @Override
            public void setTimeSlotReceiver(TimeSlotReceiver receiver) {
                receiver.onNewTimeSlots(mTimeSlots);
            }

            @Override
            public void getTimeSlotsForTimeRange(long from, long to) {

            }

        });

        mTimeSlotPicker.setOnTimeSlotSelectedListener(timeSlot -> {
            if (timeSlot.reserved) {
                Toast.makeText(getParentActivity(), "This time slot is fully reserved.", Toast.LENGTH_SHORT).show(); // TODO Put Texts resource.
                return;
            }

            mResultReceiver.onResult(mTimeSlotsMap.get(timeSlot));
            finishFragment();
        });

        return content;
    }

    @Override
    public boolean onBackPressed() {
        finishFragment(true);
        return super.onBackPressed();
    }

}

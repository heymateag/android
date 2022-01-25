package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.payment.PaymentController;
import org.telegram.ui.Heymate.widget.TimeSlotPicker;
import org.telegram.ui.Heymate.widget.TimeSlotPickerAdapter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import works.heymate.api.APIArray;
import works.heymate.api.APIObject;
import works.heymate.api.APIs;
import works.heymate.core.Texts;
import works.heymate.model.Offer;
import works.heymate.model.TimeSlot;

public class TimeSlotSelectionActivity extends BaseFragment {

    private static final String TAG = "TimeSlotSelection";

    public static final String HOST = "timeslot";

    public static Intent getIntent(Context context, String offerId) {
        Bundle args = new Bundle();
        args.putString(Constants.OFFER_ID, offerId);

        return HeymateRouter.createIntent(context, HOST, args);
    }

    private TimeZone mTimeZone;

    private List<TimeSlotPickerAdapter.TimeSlot> mTimeSlots;
    private Map<TimeSlotPickerAdapter.TimeSlot, APIObject> mTimeSlotsMap;

    private TimeSlotPicker mTimeSlotPicker;

    public TimeSlotSelectionActivity(Bundle args) {
        super(args);
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
        mTimeSlotPicker.setReservedTimeSlotColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
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

        mTimeSlotPicker.setOnTimeSlotSelectedListener(timeSlot -> {
            if (timeSlot.reserved) {
                Toast.makeText(getParentActivity(), "This time slot is fully reserved.", Toast.LENGTH_SHORT).show(); // TODO Put Texts resource.
                return false;
            }

            APIObject selectedTimeSlot = mTimeSlotsMap.get(timeSlot);
            PaymentController.get(getParentActivity()).resumeTimeSlotPurchase(selectedTimeSlot);

            finishFragment();
            return true;
        });

        String offerId = getArguments().getString(Constants.OFFER_ID);

        APIs.get().getOffer(offerId, result -> {
            if (result.response != null) {
                APIArray timeSlots = result.response.getArray(Offer.TIMESLOTS);

                mTimeZone = TimeZone.getDefault();
                mTimeSlots = new ArrayList<>(timeSlots.size());
                mTimeSlotsMap = new Hashtable<>(timeSlots.size());

                mTimeSlotPicker.setTimeZone(mTimeZone);

                for (int i = 0; i < timeSlots.size(); i++) {
                    APIObject timeSlot = timeSlots.getObject(i);

                    long start = timeSlot.getLong(TimeSlot.FROM_TIME) * 1000L;
                    long end = timeSlot.getLong(TimeSlot.TO_TIME) * 1000L;
                    int duration = (int) ((end - start) / 1000L / 60L);
                    boolean reserved = timeSlot.getInt(TimeSlot.REMAINING_RESERVATIONS) <= 0;

                    TimeSlotPickerAdapter.TimeSlot pickerTimeSlot = new TimeSlotPickerAdapter.TimeSlot(start, duration, reserved);

                    mTimeSlots.add(pickerTimeSlot);
                    mTimeSlotsMap.put(pickerTimeSlot, timeSlot);
                }

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
            }
            else {
                Log.e(TAG, "Failed to get time slots", result.error);
                Toast.makeText(getParentActivity(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();

                finishFragment();
            }
        });

        fragmentView = content;

        return content;
    }

    @Override
    protected void clearViews() {
        ((ViewGroup) fragmentView).removeAllViews();
        super.clearViews();
    }

    @Override
    public boolean onBackPressed() {
        finishFragment(true);
        return super.onBackPressed();
    }

}

package org.telegram.ui.Heymate.myschedule;

import android.content.Context;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Heymate.HtAmplify;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import works.heymate.api.APIArray;
import works.heymate.api.APIObject;
import works.heymate.api.APIs;
import works.heymate.core.Texts;
import works.heymate.model.Offer;
import works.heymate.model.TimeSlot;

public class MyOffersAdapter extends MyScheduleAdapter {

    private static final String TAG = "MyOffersAdapter";

    private final Context mContext;
    private BaseFragment mParent;
    private final RecyclerListView mListView;

    private SparseArray<List<APIObject>> mSections = new SparseArray<>();

    private Map<String, APIObject> mOffers = new HashMap<>();
    private Map<String, List<APIObject>> mTimeSlotReservations = new HashMap<>();

    public MyOffersAdapter(RecyclerListView listView, BaseFragment parent) {
        mContext = listView.getContext();
        mParent = parent;
        mListView = listView;
    }

    @Override
    public void getData() {
        APIs.get().getMyOffers(result -> {
            if (!result.success) {
                return;
            }

            List<APIObject> timeSlots = new LinkedList<>();

            APIArray offers = result.response.getArray("data");

            for (int i = 0; i < offers.size(); i++) {
                APIObject offer = offers.getObject(i);

                APIArray offerSchedule = offer.getArray(Offer.TIMESLOTS);

                for (int j = 0; j < offerSchedule.size(); j++) {
                    APIObject timeSlot = offerSchedule.getObject(j);

                    if (timeSlot.getInt(TimeSlot.COMPLETED_RESERVATIONS) > 0) {
                        timeSlots.add(timeSlot);

                        mOffers.put(timeSlot.getString(TimeSlot.ID), offer);
                    }
                }
            }

            // TODO Sort with some context
            // Collections.sort(reservations, (o1, o2) -> o2.getStartTime() - o1.getStartTime());

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long baseTime = calendar.getTimeInMillis();

            mSections.clear();

            for (APIObject timeSlot: timeSlots) {
                long slotTime = timeSlot.getLong(TimeSlot.FROM_TIME) * 1000L;

                int dayDiff;

                if (baseTime > slotTime) {
                    dayDiff = (int) ((baseTime - slotTime) / MyScheduleUtils.ONE_DAY + 1);
                }
                else {
                    dayDiff = (int) -((slotTime - baseTime) / MyScheduleUtils.ONE_DAY);
                }

                List<APIObject> list = mSections.get(dayDiff);

                if (list == null) {
                    list = new ArrayList<>();
                    mSections.put(dayDiff, list);
                }

                list.add(timeSlot);

                updateItem(timeSlot, true);
            }

            notifyDataSetChanged();
        });
    }

    private void updateItem(APIObject timeSlot, boolean queryReservations) {
        for (int i = 0; i < mListView.getChildCount(); i++) {
            if (mListView.getChildAt(i) instanceof MyOfferItem) {
                MyOfferItem item = (MyOfferItem) mListView.getChildAt(i);

                if (timeSlot.getString(TimeSlot.ID).equals(item.getTimeSlotId())) {
                    item.setTimeSlot(timeSlot);
                    item.setOffer(mOffers.get(timeSlot.getString(TimeSlot.ID)));
                    item.setReservations(mTimeSlotReservations.get(timeSlot.getString(TimeSlot.ID)));
                }
            }
        }

        if (!queryReservations) {
            return;
        }

        APIs.get().getTimeSlotReservations(timeSlot.getString(TimeSlot.ID), result -> {
            if (!result.success) {
                Log.e(TAG, "Failed to get reservations for time lost.", result.error);
                return;
            }

            APIArray reservations = result.response.getArray("data");

            mTimeSlotReservations.put(timeSlot.getString(TimeSlot.ID), reservations.asObjectList());
            updateItem(timeSlot, false);
        });
    }

    protected void updateTimeSlot(APIObject timeSlot) {
        boolean found = false;

        for (int i = 0; i < mSections.size(); i++) {
            List<APIObject> timeSlots = mSections.get(mSections.keyAt(i));

            if (timeSlots != null) {
                for (int index = 0; index < timeSlots.size(); index++) {
                    if (timeSlot.getString(TimeSlot.ID).equals(timeSlots.get(index).getString(TimeSlot.ID))) {
                        found = true;

                        timeSlots.set(index, timeSlot);
                        break;
                    }
                }
            }

            if (found) {
                break;
            }
        }

        if (found) {
            updateItem(timeSlot, true);
        }
    }

    @Override
    public int getSectionCount() {
        return mSections.size();
    }

    @Override
    public int getCountForSection(int section) {
        return mSections.get(mSections.keyAt(section)).size();
    }

    @Override
    public View getSectionHeaderView(int section, View view) {
        TextView textView;

        if (view == null) {
            textView = new TextView(mContext);
            textView.setTextColor(ContextCompat.getColor(mContext, works.heymate.beta.R.color.ht_theme));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setPadding(
                    AndroidUtilities.dp(20),
                    AndroidUtilities.dp(16),
                    AndroidUtilities.dp(20),
                    AndroidUtilities.dp(12)
            );
        }
        else {
            textView = (TextView) view;
        }

        textView.setText(getTitleForDayDifference(mSections.keyAt(section)));

        return textView;
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder, int section, int row) {
        return row != 0;
    }

    @Override
    public int getItemViewType(int section, int position) {
        return 0;
    }

    @Override
    public Object getItem(int section, int position) {
        return mSections.get(mSections.keyAt(section)).get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyOfferItem view = new MyOfferItem(mContext, mParent);

        return new RecyclerView.ViewHolder(view) { };
    }

    @Override
    public void onBindViewHolder(int section, int position, RecyclerView.ViewHolder holder) {
        MyOfferItem item = (MyOfferItem) holder.itemView;

        APIObject timeSlot = (APIObject) getItem(section, position);

        item.setTimeSlot(timeSlot);
        item.setOffer(mOffers.get(timeSlot.getString(TimeSlot.ID)));
        item.setReservations(mTimeSlotReservations.get(timeSlot.getString(TimeSlot.ID)));
    }

    @Override
    public String getLetter(int position) {
        return null;
    }

    @Override
    public void getPositionForScrollProgress(RecyclerListView listView, float progress, int[] position) {

    }

    private CharSequence getTitleForDayDifference(int dayDiff) {
        switch (dayDiff) {
            case 0:
                return Texts.get(Texts.TODAY);
            case 1:
                return Texts.get(Texts.YESTERDAY);
            default:
                List<APIObject> timeSlots = mSections.get(dayDiff);

                if (timeSlots == null || timeSlots.isEmpty()) {
                    return "";
                }

                return FastDateFormat.getDateInstance(FastDateFormat.MEDIUM).format(timeSlots.get(0).getLong(TimeSlot.FROM_TIME) * 1000L);
        }
    }

}
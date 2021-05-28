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

import com.amplifyframework.api.ApiException;
import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.Reservation;
import com.amplifyframework.datastore.generated.model.TimeSlot;
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
import java.util.List;
import java.util.Map;

import works.heymate.core.Texts;

public class MyOffersAdapter extends MyScheduleAdapter {

    private static final String TAG = "MyOffersAdapter";

    private final Context mContext;
    private BaseFragment mParent;
    private final RecyclerListView mListView;

    private SparseArray<List<TimeSlot>> mSections = new SparseArray<>();

    private Map<String, Offer> mOffers = new HashMap<>();
    private Map<String, List<Reservation>> mTimeSlotReservations = new HashMap<>();

    public MyOffersAdapter(RecyclerListView listView, BaseFragment parent) {
        mContext = listView.getContext();
        mParent = parent;
        mListView = listView;
    }

    @Override
    public void getData() {
        HtAmplify.getInstance(mContext).getMyReservedTimeSlots(this::onReservedTimeSlotsQueryResult);
    }

    private void onReservedTimeSlotsQueryResult(boolean success, List<TimeSlot> timeSlots, ApiException exception) {
        if (!success) {
            return;
        }

//        while (timeSlots.size() > 1) {
//            timeSlots.remove(1);
//        }
        // TODO Sort with some context
        // Collections.sort(reservations, (o1, o2) -> o2.getStartTime() - o1.getStartTime());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long baseTime = calendar.getTimeInMillis();

        mSections.clear();

        for (TimeSlot timeSlot: timeSlots) {
            long slotTime = timeSlot.getStartTime() * 1000L;

            int dayDiff;

            if (baseTime > slotTime) {
                dayDiff = (int) ((baseTime - slotTime) / MyScheduleUtils.ONE_DAY + 1);
            }
            else {
                dayDiff = (int) -((slotTime - baseTime) / MyScheduleUtils.ONE_DAY);
            }

            List<TimeSlot> list = mSections.get(dayDiff);

            if (list == null) {
                list = new ArrayList<>();
                mSections.put(dayDiff, list);
            }

            list.add(timeSlot);

            if (!mOffers.containsKey(timeSlot.getId())) {
                HtAmplify.getInstance(mContext).getOffer(timeSlot.getOfferId(), (success1, data, exception1) -> {
                    if (success1) {
                        mOffers.put(timeSlot.getId(), data);
                        updateItem(timeSlot, true);
                    }
                });
            }
        }

        notifyDataSetChanged();
    }

    private void updateItem(TimeSlot timeSlot, boolean queryReservations) {
        for (int i = 0; i < mListView.getChildCount(); i++) {
            if (mListView.getChildAt(i) instanceof MyOfferItem) {
                MyOfferItem item = (MyOfferItem) mListView.getChildAt(i);

                if (timeSlot.getId().equals(item.getTimeSlotId())) {
                    item.setTimeSlot(timeSlot);
                    item.setOffer(mOffers.get(timeSlot.getId()));
                    item.setReservations(mTimeSlotReservations.get(timeSlot.getId()));
                }
            }
        }

        if (!queryReservations) {
            return;
        }

        HtAmplify.getInstance(mContext).getTimeSlotReservations(timeSlot.getId(), (success, result, exception) -> {
            if (!success) {
                Log.e(TAG, "Failed to get reservations for time lost.", exception);
                return;
            }

            mTimeSlotReservations.put(timeSlot.getId(), result);
            updateItem(timeSlot, false);
        });
    }

    protected void updateTimeSlot(TimeSlot timeSlot) {
        boolean found = false;

        for (int i = 0; i < mSections.size(); i++) {
            List<TimeSlot> timeSlots = mSections.get(mSections.keyAt(i));

            if (timeSlots != null) {
                for (int index = 0; index < timeSlots.size(); index++) {
                    if (timeSlot.getId().equals(timeSlots.get(index).getId())) {
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
            updateItem(timeSlot, false);
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
    public boolean isEnabled(int section, int row) {
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

        TimeSlot timeSlot = (TimeSlot) getItem(section, position);

        item.setTimeSlot(timeSlot);
        item.setOffer(mOffers.get(timeSlot.getId()));
    }

    @Override
    public String getLetter(int position) {
        return null;
    }

    @Override
    public int getPositionForScrollProgress(float progress) {
        return 0;
    }

    private CharSequence getTitleForDayDifference(int dayDiff) {
        switch (dayDiff) {
            case 0:
                return Texts.get(Texts.TODAY);
            case 1:
                return Texts.get(Texts.YESTERDAY);
            default:
                List<TimeSlot> reservations = mSections.get(dayDiff);

                if (reservations == null || reservations.isEmpty()) {
                    return "";
                }

                return FastDateFormat.getDateInstance(FastDateFormat.MEDIUM).format(reservations.get(0).getStartTime() * 1000L);
        }
    }

}
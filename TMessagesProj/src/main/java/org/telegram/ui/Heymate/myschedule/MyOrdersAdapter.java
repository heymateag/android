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

public class MyOrdersAdapter extends MyScheduleAdapter {

    private final Context mContext;
    private final BaseFragment mParent;
    private final RecyclerListView mListView;

    private SparseArray<List<Reservation>> mSections = new SparseArray<>();

    private Map<String, Offer> mOffers = new HashMap<>();

    public MyOrdersAdapter(RecyclerListView listView, BaseFragment parent) {
        mContext = listView.getContext();
        mParent = parent;
        mListView = listView;
    }

    @Override
    public void getData() {
        HtAmplify.getInstance(mContext).getMyOrders(this::onReservationsQueryResult);
    }

    private void onReservationsQueryResult(boolean success, List<Reservation> reservations, ApiException exception) {
        if (!success) {
            return;
        }

        Collections.sort(reservations, (o1, o2) -> o2.getStartTime() - o1.getStartTime());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long baseTime = calendar.getTimeInMillis();

        mSections.clear();

        for (Reservation reservation: reservations) {
            long slotTime = reservation.getStartTime() * 1000L;

            int dayDiff;

            if (baseTime > slotTime) {
                dayDiff = (int) ((baseTime - slotTime) / MyScheduleUtils.ONE_DAY + 1);
            }
            else {
                dayDiff = (int) -((slotTime - baseTime) / MyScheduleUtils.ONE_DAY);
            }

            List<Reservation> list = mSections.get(dayDiff);

            if (list == null) {
                list = new ArrayList<>();
                mSections.put(dayDiff, list);
            }

            list.add(reservation);

            if (!mOffers.containsKey(reservation.getId())) {
                HtAmplify.getInstance(mContext).getOffer(reservation.getOfferId(), (success1, data, exception1) -> {
                    if (success1) {
                        mOffers.put(reservation.getId(), data);
                        updateItem(reservation);
                    }
                });
            }
        }

        notifyDataSetChanged();
    }

    private void updateItem(Reservation reservation) {
        for (int i = 0; i < mListView.getChildCount(); i++) {
            if (mListView.getChildAt(i) instanceof MyOrderItem) {
                MyOrderItem item = (MyOrderItem) mListView.getChildAt(i);

                if (reservation.getId().equals(item.getReservationId())) {
                    item.setReservation(reservation);
                    item.setOffer(mOffers.get(reservation.getId()));
                }
            }
        }
    }

    protected void updateReservation(Reservation reservation) {
        boolean found = false;

        for (int i = 0; i < mSections.size(); i++) {
            List<Reservation> reservations = mSections.get(mSections.keyAt(i));

            if (reservations != null) {
                for (int index = 0; index < reservations.size(); index++) {
                    if (reservation.getId().equals(reservations.get(index).getId())) {
                        found = true;

                        reservations.set(index, reservation);
                        break;
                    }
                }
            }

            if (found) {
                break;
            }
        }

        if (found) {
            updateItem(reservation);
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
        MyOrderItem view = new MyOrderItem(parent.getContext(), mParent);

        return new RecyclerView.ViewHolder(view) { };
    }

    @Override
    public void onBindViewHolder(int section, int position, RecyclerView.ViewHolder holder) {
        MyOrderItem item = (MyOrderItem) holder.itemView;

        Reservation reservation = (Reservation) getItem(section, position);

        item.setReservation(reservation);
        item.setOffer(mOffers.get(reservation.getId()));
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
                List<Reservation> reservations = mSections.get(dayDiff);

                if (reservations == null || reservations.isEmpty()) {
                    return "";
                }

                return FastDateFormat.getDateInstance(FastDateFormat.MEDIUM).format(reservations.get(0).getStartTime() * 1000L);
        }
    }

}
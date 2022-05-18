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

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import works.heymate.api.APIObject;
import works.heymate.api.APIs;
import works.heymate.core.Texts;
import works.heymate.model.Reservation;

public class MyOrdersAdapter extends MyScheduleAdapter {

    private final Context mContext;
    private final BaseFragment mParent;
    private final RecyclerListView mListView;

    private SparseArray<List<APIObject>> mSections = new SparseArray<>();

    private Map<String, APIObject> mOffers = new HashMap<>();

    public MyOrdersAdapter(RecyclerListView listView, BaseFragment parent) {
        mContext = listView.getContext();
        mParent = parent;
        mListView = listView;
    }

    @Override
    public void getData() {
        APIs.get().getMyOrders(result -> {
            if (!result.success) {
                return;
            }

            List<APIObject> reservations = result.response.getArray("data").asObjectList();

            // TODO sort
//            Collections.sort(reservations, (o1, o2) -> o2.getStartTime() - o1.getStartTime());

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long baseTime = calendar.getTimeInMillis();

            mSections.clear();

            for (APIObject reservation: reservations) {
                long slotTime = 0;//reservation.getStartTime() * 1000L;

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

                list.add(reservation);

                if (!mOffers.containsKey(reservation.getString(Reservation.ID))) {
                    APIObject offer = reservation.getObject(Reservation.OFFER);

                    if (offer != null) {
                        mOffers.put(reservation.getString(Reservation.ID), offer);
                    }
                    else {
                        String offerId = reservation.getString(Reservation.OFFER_ID);

                        if (offerId != null) {
                            APIs.get().getOffer(offerId, offerResult -> {
                                if (offerResult.success) {
                                    mOffers.put(reservation.getString(Reservation.ID), offerResult.response);
                                    updateItem(reservation);
                                }
                            });
                        }
                    }
                }
            }

            notifyDataSetChanged();
        });
    }

    private void updateItem(APIObject reservation) {
        for (int i = 0; i < mListView.getChildCount(); i++) {
            if (mListView.getChildAt(i) instanceof MyOrderItem) {
                MyOrderItem item = (MyOrderItem) mListView.getChildAt(i);

                if (reservation.getString(Reservation.ID).equals(item.getReservationId())) {
                    item.setReservation(reservation);
                    item.setOffer(mOffers.get(reservation.getString(Reservation.ID)));
                }
            }
        }
    }

    protected void updateReservation(APIObject reservation) {
        boolean found = false;

        for (int i = 0; i < mSections.size(); i++) {
            List<APIObject> reservations = mSections.get(mSections.keyAt(i));

            if (reservations != null) {
                for (int index = 0; index < reservations.size(); index++) {
                    if (reservation.getString(Reservation.ID).equals(reservations.get(index).getString(Reservation.ID))) {
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

        APIObject reservation = (APIObject) getItem(section, position);

        item.setReservation(reservation);
        item.setOffer(mOffers.get(reservation.getString(Reservation.ID)));
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
                List<APIObject> reservations = mSections.get(dayDiff);

                if (reservations == null || reservations.isEmpty()) {
                    return "";
                }

                return "";//FastDateFormat.getDateInstance(FastDateFormat.MEDIUM).format(reservations.get(0).getStartTime() * 1000L); TODO
        }
    }

}
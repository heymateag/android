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
import com.amplifyframework.datastore.generated.model.PurchasedPlan;
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

public class SubscriptionsAdapter extends MyScheduleAdapter {

    private final Context mContext;
    private final BaseFragment mParent;
    private final RecyclerListView mListView;

    private SparseArray<List<PurchasedPlan>> mSections = new SparseArray<>();

    private Map<String, Offer> mOffers = new HashMap<>();

    public SubscriptionsAdapter(RecyclerListView listView, BaseFragment parent) {
        mContext = listView.getContext();
        mParent = parent;
        mListView = listView;
    }

    @Override
    public void getData() {
        HtAmplify.getInstance(mContext).getPurchasedPlans(this::onPurchasedPlansQueryResult);
    }

    private void onPurchasedPlansQueryResult(boolean success, List<PurchasedPlan> purchasedPlans, ApiException exception) {
        if (!success) {
            return;
        }

        Collections.sort(purchasedPlans, (o1, o2) -> (int) (o2.getPurchaseTime().toDate().getTime() - o1.getPurchaseTime().toDate().getTime()));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long baseTime = calendar.getTimeInMillis();

        mSections.clear();

        for (PurchasedPlan purchasedPlan: purchasedPlans) {
            long slotTime = purchasedPlan.getPurchaseTime().toDate().getTime();

            int dayDiff;

            if (baseTime > slotTime) {
                dayDiff = (int) ((baseTime - slotTime) / MyScheduleUtils.ONE_DAY + 1);
            }
            else {
                dayDiff = (int) -((slotTime - baseTime) / MyScheduleUtils.ONE_DAY);
            }

            List<PurchasedPlan> list = mSections.get(dayDiff);

            if (list == null) {
                list = new ArrayList<>();
                mSections.put(dayDiff, list);
            }

            list.add(purchasedPlan);

            if (!mOffers.containsKey(purchasedPlan.getId())) {
                HtAmplify.getInstance(mContext).getOffer(purchasedPlan.getOfferId(), (success1, data, exception1) -> {
                    if (success1) {
                        mOffers.put(purchasedPlan.getId(), data);
                        updateItem(purchasedPlan);
                    }
                });
            }
        }

        notifyDataSetChanged();
    }

    private void updateItem(PurchasedPlan purchasedPlan) {
        for (int i = 0; i < mListView.getChildCount(); i++) {
            if (mListView.getChildAt(i) instanceof SubscriptionItem) {
                SubscriptionItem item = (SubscriptionItem) mListView.getChildAt(i);

                if (purchasedPlan.getId().equals(item.getPurchasedPlanId())) {
                    item.setPurchasedPlan(purchasedPlan);
                    item.setOffer(mOffers.get(purchasedPlan.getId()));
                }
            }
        }
    }

    protected void updatePurchasedPlan(PurchasedPlan purchasedPlan) {
        boolean found = false;

        for (int i = 0; i < mSections.size(); i++) {
            List<PurchasedPlan> purchasedPlans = mSections.get(mSections.keyAt(i));

            if (purchasedPlans != null) {
                for (int index = 0; index < purchasedPlans.size(); index++) {
                    if (purchasedPlan.getId().equals(purchasedPlans.get(index).getId())) {
                        found = true;

                        purchasedPlans.set(index, purchasedPlan);
                        break;
                    }
                }
            }

            if (found) {
                break;
            }
        }

        if (found) {
            updateItem(purchasedPlan);
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
        SubscriptionItem view = new SubscriptionItem(parent.getContext(), mParent);

        return new RecyclerView.ViewHolder(view) { };
    }

    @Override
    public void onBindViewHolder(int section, int position, RecyclerView.ViewHolder holder) {
        SubscriptionItem item = (SubscriptionItem) holder.itemView;

        PurchasedPlan purchasedPlan = (PurchasedPlan) getItem(section, position);

        item.setPurchasedPlan(purchasedPlan);
        item.setOffer(mOffers.get(purchasedPlan.getId()));
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
                List<PurchasedPlan> reservations = mSections.get(dayDiff);

                if (reservations == null || reservations.isEmpty()) {
                    return "";
                }

                return FastDateFormat.getDateInstance(FastDateFormat.MEDIUM).format(reservations.get(0).getPurchaseTime().toDate().getTime());
        }
    }

}
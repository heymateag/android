package org.telegram.ui.Heymate.myschedule;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ViewPagerFixed;
import org.telegram.ui.Heymate.HtAmplify;

import works.heymate.core.HeymateEvents;
import works.heymate.core.Texts;

public class MyScheduleActivity extends BaseFragment implements HeymateEvents.HeymateEventObserver {

    private MyOrdersAdapter mMyOrdersAdapter;
    private MyOffersAdapter mMyOffersAdapter;

    @Override
    public boolean onFragmentCreate() {
        HeymateEvents.register(HeymateEvents.RESERVATION_STATUS_UPDATED, this);

        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        HeymateEvents.unregister(HeymateEvents.RESERVATION_STATUS_UPDATED, this);

        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        ActionBar actionBar = getActionBar();
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setTitle(Texts.get(Texts.MY_SCHEDULE));
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

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        ViewPagerFixed viewPager = new ViewPagerFixed(context);
        viewPager.setAdapter(new ViewPagerFixed.Adapter() {

            @Override
            public int getItemCount() {
                return 2;
            }

            @Override
            public View createView(int viewType) {
                RecyclerListView view = new RecyclerListView(context);
                view.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                return view;
            }

            @Override
            public void bindView(View view, int position, int viewType) {
                bindAdapter((RecyclerListView) view, position);
            }

            @Override
            public String getItemTitle(int position) {
                return Texts.get(position == 0 ? Texts.MY_SCHEDULE_OFFERS : Texts.MY_SCHEDULE_ORDERS).toString();
            }

        });

        ViewPagerFixed.TabsView tabsView = viewPager.createTabsView();

        content.addView(tabsView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44));
        content.addView(viewPager, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        fragmentView = content;

        return fragmentView;
    }

    @Override
    protected void clearViews() {
        ((ViewGroup) fragmentView).removeAllViews();
        mMyOrdersAdapter = null;
        mMyOffersAdapter = null;
        super.clearViews();
    }

    private void bindAdapter(RecyclerListView listView, int position) {
        if (listView.getAdapter() == null) {
            if (position == 0) {
                mMyOffersAdapter = new MyOffersAdapter(listView, this);
                listView.setAdapter(mMyOffersAdapter);
            }
            else {
                mMyOrdersAdapter = new MyOrdersAdapter(listView, this);
                listView.setAdapter(mMyOrdersAdapter);
            }
        }

        ((MyScheduleAdapter) listView.getAdapter()).getData();
    }

    @Override
    public boolean onBackPressed() {
        finishFragment(true);
        return super.onBackPressed();
    }

    @Override
    public void onHeymateEvent(int event, Object... args) {
        String id = (String) args[0];

        HtAmplify.getInstance(getParentActivity()).getReservation(id, (success, result, exception) -> {
            if (success && result != null) {
                if (mMyOrdersAdapter != null) {
                    mMyOrdersAdapter.updateReservation(result);
                }

                HtAmplify.getInstance(getParentActivity()).getTimeSlot(result.getTimeSlotId(), (success1, result1, exception1) -> {
                    if (success1 && result1 != null && mMyOffersAdapter != null) {
                        mMyOffersAdapter.updateTimeSlot(result1);
                    }
                });
            }
        });
    }

}
package org.telegram.ui.Heymate.myschedule;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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

    public static final String HOST = "myschedule";

    public static final int MY_OFFERS = 0;
    public static final int MY_ORDERS = 1;
    public static final int SUBSCRIPTIONS = 2;

    public static Bundle createBundle(int tab) {
        Bundle bundle = new Bundle();
        bundle.putInt("tab", tab);
        return bundle;
    }

    private int mDefaultTab = MY_OFFERS;
    private boolean mDefaultTabApplied = false;

    private ViewPagerFixed mViewPager;
    private ViewPagerFixed.TabsView mTabsView;

    private MyOffersAdapter mMyOffersAdapter;
    private MyOrdersAdapter mMyOrdersAdapter;
    private SubscriptionsAdapter mSubscriptionsAdapter;

    public MyScheduleActivity() {

    }

    public MyScheduleActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        Bundle args = getArguments();

        if (args != null) {
            if (args.containsKey("tab")) {
                mDefaultTab = args.getInt("tab");
            }
        }

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

        mViewPager = new ViewPagerFixed(context);
        mViewPager.setAdapter(new ViewPagerFixed.Adapter() {

            @Override
            public int getItemCount() {
                return 3;
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
                switch (position) {
                    case MY_OFFERS:
                        return Texts.get(Texts.MY_SCHEDULE_OFFERS).toString();
                    case MY_ORDERS:
                        return Texts.get(Texts.MY_SCHEDULE_ORDERS).toString();
                    case SUBSCRIPTIONS:
                        return "Subscriptions"; // TODO Texts
                }

                return "";
            }

        });

        mTabsView = mViewPager.createTabsView();

        content.addView(mTabsView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44));
        content.addView(mViewPager, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        fragmentView = content;

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mDefaultTabApplied) {
            mDefaultTabApplied = true;
            mTabsView.scrollToTab(mDefaultTab, mDefaultTab);
        }
    }

    @Override
    protected void clearViews() {
        ((ViewGroup) fragmentView).removeAllViews();
        mMyOrdersAdapter = null;
        mMyOffersAdapter = null;
        mSubscriptionsAdapter = null;
        super.clearViews();
    }

    private void bindAdapter(RecyclerListView listView, int position) {
        MyScheduleAdapter adapter = null;

        switch (position) {
            case MY_OFFERS:
                if (mMyOffersAdapter == null) {
                    mMyOffersAdapter = new MyOffersAdapter(listView, this);
                }
                adapter = mMyOffersAdapter;
                break;
            case MY_ORDERS:
                if (mMyOrdersAdapter == null) {
                    mMyOrdersAdapter = new MyOrdersAdapter(listView, this);
                }
                adapter = mMyOrdersAdapter;
                break;
            case SUBSCRIPTIONS:
                if (mSubscriptionsAdapter == null) {
                    mSubscriptionsAdapter = new SubscriptionsAdapter(listView, this);
                }
                adapter = mSubscriptionsAdapter;
                break;
        }

        if (listView.getAdapter() != adapter) {
            listView.setAdapter(adapter);
        }

        adapter.getData();
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

                HtAmplify.getInstance(getParentActivity()).getPurchasedPlan(result.getPurchasedPlanId(), (success1, result1, exception1) -> {
                    if (success1 && result1 != null && mSubscriptionsAdapter != null) {
                        mSubscriptionsAdapter.updatePurchasedPlan(result1);
                    }
                });
            }
        });
    }

}

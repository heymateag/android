package org.telegram.ui.Heymate;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.Offer;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Heymate.createoffer.HtCreateOfferActivity;
import org.telegram.ui.Heymate.myschedule.MyScheduleActivity;

import java.util.ArrayList;
import java.util.List;

import works.heymate.core.Texts;
import works.heymate.core.wallet.Wallet;

public class OffersActivity extends BaseFragment {

    private static final int VIEW_TYPE_OFFER = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private static final String ACTIVE_OFFERS_PLACE_HOLDER = "{active_offers}";

    private String categoryFilter = null;
    private String subCategoryFilter = null;
    private OfferStatus statusFilter = null;

    private TextView mTextStatus;
    private ImageView mButtonSchedule;

    private OfferAdapter mAdapter;

    public OffersActivity() {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(works.heymate.beta.R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("HtManageOffers", works.heymate.beta.R.string.HtManageOffers));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = LayoutInflater.from(context).inflate(works.heymate.beta.R.layout.activity_offers, null, false);

        TextView textTitle = fragmentView.findViewById(works.heymate.beta.R.id.text_title);
        textTitle.setTypeface(textTitle.getTypeface(), Typeface.BOLD);
        textTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textTitle.setText(Texts.get(Texts.OFFERS));
        Drawable myOffersDrawable = ContextCompat.getDrawable(context, works.heymate.beta.R.drawable.offer);
        myOffersDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(works.heymate.beta.R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        textTitle.setCompoundDrawablePadding(AndroidUtilities.dp(5));
        textTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, myOffersDrawable, null);

        mTextStatus = fragmentView.findViewById(works.heymate.beta.R.id.text_status);
        mTextStatus.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));

        mButtonSchedule = fragmentView.findViewById(works.heymate.beta.R.id.button_schedule);
        mButtonSchedule.setOnClickListener(v -> presentFragment(new MyScheduleActivity()));
        mButtonSchedule.setVisibility(View.INVISIBLE); // TODO Temporary code

        TextView buttonAdd = fragmentView.findViewById(works.heymate.beta.R.id.button_add);
        buttonAdd.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));
        buttonAdd.setTypeface(buttonAdd.getTypeface(), Typeface.BOLD);
        buttonAdd.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), ContextCompat.getColor(context, works.heymate.beta.R.color.ht_theme)));
        buttonAdd.setText(Texts.get(Texts.ADD));
        buttonAdd.setOnClickListener(v -> {
            presentFragment(new HtCreateOfferActivity());
//            ReferralUtils.Referrer referrer = new ReferralUtils.Referrer(
//                    String.valueOf(UserConfig.getInstance(getCurrentAccount()).clientUserId),
//                    Wallet.get(getParentActivity(), TG2HM.getCurrentPhoneNumber()).getAddress(),
//                    FirebaseInstanceId.getInstance().getToken()
//            );
//            JSONArray jReferrers = new JSONArray();
//            jReferrers.put(referrer.asJSON());
//            HtAmplify.getInstance(getParentActivity()).createReferral("sadada", jReferrers.toString(), (success, result, exception) -> {
//                if (success) {
//                    HtAmplify.getInstance(getParentActivity()).notifyReferralPrizeWon(result);
//                }
//            });
        });

        HtFiltersCell filters = fragmentView.findViewById(works.heymate.beta.R.id.filters);
        filters.setBaseFragment(this);

        RecyclerView listOffer = fragmentView.findViewById(works.heymate.beta.R.id.list_offer);
        listOffer.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        listOffer.setLayoutManager(new LinearLayoutManager(getParentActivity()));

        mAdapter = new OfferAdapter();
        listOffer.setAdapter(mAdapter);

        onActiveOffersUpdated(0);

        return fragmentView;
    }

    private void checkBalance() {
        Wallet wallet = Wallet.get(getParentActivity(), TG2HM.getCurrentPhoneNumber());

        if (!wallet.isCreated()) {
            mTextStatus.setText("Current balance is: [No wallet detected]");
        }
        else {
            mTextStatus.setText("Current balance is:");

            wallet.getBalance((success, cents, errorCause) -> {
                if (success) {
                    mTextStatus.setText("Current balance is: $" + (cents / 100f));
                }
                else {
                    mTextStatus.setText("Current balance is: [Connection problem]");
                }
            });
        }
    }

    private void onActiveOffersUpdated(int activeOffers) {
        String activeOffersStatus = Texts.get(Texts.OFFERS_ACTIVE_OFFERS).toString().replace(ACTIVE_OFFERS_PLACE_HOLDER, String.valueOf(activeOffers));

        mTextStatus.setText(activeOffersStatus);

        Drawable icon = ContextCompat.getDrawable(getParentActivity(), works.heymate.beta.R.drawable.input_calendar1).mutate();
        icon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText), PorterDuff.Mode.SRC_IN);

        if (activeOffers == 0) {
            mButtonSchedule.setImageDrawable(icon);
        }
        else {
            Drawable notif = ContextCompat.getDrawable(getParentActivity(), works.heymate.beta.R.drawable.input_calendar2).mutate();
            notif.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteRedText), PorterDuff.Mode.SRC_IN);

            CombinedDrawable drawable = new CombinedDrawable(icon, notif);
            drawable.setFullsize(true);
            mButtonSchedule.setImageDrawable(drawable);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        checkBalance();

        mAdapter.getData();
    }

    public void setStatusFilter(String status) {
        statusFilter = status == null ? null : OfferStatus.valueOf(status.toUpperCase());
        mAdapter.applyFilter();
    }

    public void setCategoryFilter(String category) {
        categoryFilter = category;
        mAdapter.applyFilter();
    }

    public void setSubCategoryFilter(String subCategory) {
        subCategoryFilter = subCategory;
        mAdapter.applyFilter();
    }

    private class OfferAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Offer> mOffers = null;
        private List<Offer> mFilteredOffers = new ArrayList<>();

        private boolean mLoading = false;

        public OfferAdapter() {

        }

        private boolean shouldShowLoading() {
            return mLoading && mOffers == null;
        }

        public void getData() {
            mLoading = true;

            HtAmplify.getInstance(getParentActivity()).getMyOffers((success, result, exception) -> {
                mLoading = false;

                if (success) {
                    if (mOffers == null) {
                        mOffers = new ArrayList<>(result.size());
                    }

                    mOffers.clear();
                    mOffers.addAll(result);

                    applyFilter();
                }
                else if (mOffers == null) {
                    notifyDataSetChanged();
                }
            });

            if (mOffers == null) {
                notifyDataSetChanged();
            }
        }

        public void applyFilter() {
            if (mOffers == null) {
                return;
            }

            mFilteredOffers.clear();

            for (Offer offer: mOffers) {
                if (categoryFilter != null && !categoryFilter.equals(offer.getCategory())) {
                    continue;
                }
                else if (subCategoryFilter != null && !subCategoryFilter.equals(offer.getSubCategory())) {
                    continue;
                }
                else if (statusFilter != null && offer.getStatus() != null && statusFilter.ordinal() != offer.getStatus()) {
                    continue;
                }

                mFilteredOffers.add(offer);
            }

            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return shouldShowLoading() ? VIEW_TYPE_LOADING : VIEW_TYPE_OFFER;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_OFFER:
                    OfferMessageItem item = new OfferMessageItem(getParentActivity());
                    item.setParent(OffersActivity.this);
                    return new RecyclerView.ViewHolder(item) { };
                case VIEW_TYPE_LOADING:
                    FlickerLoadingView flickerLoadingView = new FlickerLoadingView(getParentActivity());
                    flickerLoadingView.setIsSingleCell(true);
                    flickerLoadingView.setViewType(FlickerLoadingView.OFFER_TYPE);
                    flickerLoadingView.setItemsCount(4);
                    flickerLoadingView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    return new RecyclerView.ViewHolder(flickerLoadingView) { };
            }

            throw new RuntimeException("Unknown view type.");
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (shouldShowLoading()) {
                return;
            }

            Offer offer = mFilteredOffers.get(position);

            OfferMessageItem item = (OfferMessageItem) holder.itemView;

            item.setOffer(offer, true);
        }

        @Override
        public int getItemCount() {
            return shouldShowLoading() ? 1 : mFilteredOffers.size();
        }

    };

}

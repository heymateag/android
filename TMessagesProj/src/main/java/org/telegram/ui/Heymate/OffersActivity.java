package org.telegram.ui.Heymate;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.TimeSlot;
import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Heymate.createoffer.HtCreateOfferActivity;
import org.telegram.ui.Heymate.myschedule.MyScheduleActivity;

import java.util.ArrayList;

import works.heymate.core.Texts;
import works.heymate.core.offer.OfferUtils;
import works.heymate.core.wallet.Wallet;

public class OffersActivity extends BaseFragment {

    private static final String ACTIVE_OFFERS_PLACE_HOLDER = "{active_offers}";

    private boolean inited = false;
    private LinearLayout offersLayout;
    private Context context;

    private String categoryFilter = "All";
    private String subCategoryFilter = "All";
    private OfferStatus statusFilter = OfferStatus.ALL;

    private TextView mTextStatus;
    private ImageView mButtonSchedule;

    public OffersActivity(Context context){
        ArrayList<Offer> fetchedOffers = HtAmplify.getInstance(context).getOffers(UserConfig.getInstance(currentAccount).clientUserId, currentAccount);
        HtSQLite.getInstance().updateOffers(fetchedOffers, UserConfig.getInstance(currentAccount).clientUserId);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View createView(Context context) {
        this.context = context;

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

        offersLayout = fragmentView.findViewById(works.heymate.beta.R.id.list_offer);
        offersLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        offersLayout.setOrientation(LinearLayout.VERTICAL);

        // ------------ DATABASE DEMO ----------------
        ArrayList<OfferDto> offers = HtSQLite.getInstance().getAllOffers(UserConfig.getInstance(currentAccount).clientUserId);
        // --------------------------------------------

        addOffersToLayout(offers);

        onActiveOffersUpdated(0);

        inited = true;
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

    public void addOffersToLayout(ArrayList<OfferDto> offers) {
        if(!inited)
            return;
        offersLayout.removeAllViews();
        if (offers == null)
            return;
        for (OfferDto offerDto : offers) {
            OfferMessageItem offerCell1 = new OfferMessageItem(context);
            offerCell1.setOffer(offerDto.asOffer(), true);
            offerCell1.setParent(this);
            offersLayout.addView(offerCell1);
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        offersLayout.removeAllViews();
        ArrayList<OfferDto> offers = HtSQLite.getInstance().getAllOffers(UserConfig.getInstance(currentAccount).clientUserId);
        addOffersToLayout(offers);

        checkBalance();
    }

    public void setStatusFilter(String status) {
        statusFilter = OfferStatus.valueOf(status.toUpperCase());
        setFilters();
    }

    public void setCategoryFilter(String category) {
        categoryFilter = category;
        setFilters();
    }

    public void setSubCategoryFilter(String subCategory) {
        subCategoryFilter = subCategory;
        setFilters();
    }

    private void setFilters() {
        ArrayList<OfferDto> offers;
        if (categoryFilter.equalsIgnoreCase("all")) {
            if (subCategoryFilter.equalsIgnoreCase("all")) {
                if (statusFilter.ordinal() == 0) {
                    offers = HtSQLite.getInstance().getAllOffers(UserConfig.getInstance(currentAccount).clientUserId);
                } else {
                    offers = HtSQLite.getInstance().getOffers(statusFilter.ordinal(), UserConfig.getInstance(currentAccount).clientUserId);
                }
            } else {
                if (statusFilter.ordinal() == 0) {
                    offers = HtSQLite.getInstance().getOffers(subCategoryFilter, UserConfig.getInstance(currentAccount).clientUserId);

                } else {
                    offers = HtSQLite.getInstance().getOffers(subCategoryFilter, statusFilter.ordinal(), UserConfig.getInstance(currentAccount).clientUserId);
                }
            }
        } else {
            if (subCategoryFilter.equalsIgnoreCase("all")) {
                if (statusFilter.ordinal() == 0) {
                    offers = HtSQLite.getInstance().getOffers(categoryFilter, UserConfig.getInstance(currentAccount).clientUserId);
                } else {
                    offers = HtSQLite.getInstance().getOffers(categoryFilter, statusFilter.ordinal(), UserConfig.getInstance(currentAccount).clientUserId);
                }
            } else {
                if (statusFilter.ordinal() == 0) {
                    offers = HtSQLite.getInstance().getOffers(categoryFilter, subCategoryFilter, UserConfig.getInstance(currentAccount).clientUserId);

                } else {
                    offers = HtSQLite.getInstance().getOffers(categoryFilter, subCategoryFilter, statusFilter.ordinal(), UserConfig.getInstance(currentAccount).clientUserId);
                }
            }
        }
        addOffersToLayout(offers);
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        return themeDescriptions;
    }

}



/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Heymate;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.AmplifyModels.Offer;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import static org.telegram.ui.Heymate.HtCreateOfferActivity.OFFER_MESSAGE_PREFIX;

public class OffersActivity extends BaseFragment {

    private boolean inited = false;
    private OfferController offerController = OfferController.getInstance();
    private LinearLayout offersLayout;
    private Context context;
    private final static int search_button = 1;
    private String categoryFilter = "All";
    private String subCategoryFilter = "All";
    private OfferStatus statusFilter = OfferStatus.ALL;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View createView(Context context) {
        DatabaseWatchDog.getInstance().config(currentAccount);
        this.context = context;
        Configuration configuration = context.getResources().getConfiguration();
        int dpWidth = configuration.screenWidthDp;
        int dpHeight = configuration.screenHeightDp;

//        HtAmplify.getInstance(context).signUp(currentAccount);
//        HtAmplify.getInstance(context).signIn(currentAccount);
        ArrayList<Offer> fetchedOffers = HtAmplify.getInstance(context).getOffers(UserConfig.getInstance(currentAccount).clientUserId, currentAccount);
        HtSQLite.getInstance().updateOffers(fetchedOffers, UserConfig.getInstance(currentAccount).clientUserId);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setSearchTextColor(0xff4488, true);
        actionBar.setTitle(LocaleController.getString("HtManageOffers", R.string.HtManageOffers));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == search_button) {
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        fragmentView = new LinearLayout(context);
        LinearLayout linearLayout = (LinearLayout) fragmentView;
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout relativeLayout2 = new LinearLayout(context);
        relativeLayout2.setOrientation(LinearLayout.VERTICAL);
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        fragmentView.setOnTouchListener((v, event) -> true);


        FrameLayout fieldContainer = new FrameLayout(context);
        linearLayout2.addView(fieldContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 24, 24, 20, 0));

        LinearLayout offerCreationLayout = new LinearLayout(context);

        LinearLayout pageTitleLayout = new LinearLayout(context);
        pageTitleLayout.setOrientation(LinearLayout.VERTICAL);

        TextView myOffersLabel = new TextView(context);
        myOffersLabel.setText(LocaleController.getString("HtMyOffers", R.string.HtMyOffers));
        myOffersLabel.setTextSize(18);
        myOffersLabel.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        myOffersLabel.setTypeface(myOffersLabel.getTypeface(), Typeface.BOLD);

        Drawable myOffersDrawable = context.getResources().getDrawable(R.drawable.offer);
        myOffersDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        myOffersLabel.setCompoundDrawablePadding(AndroidUtilities.dp(5));
        myOffersLabel.setCompoundDrawablesWithIntrinsicBounds(null, null, myOffersDrawable, null);
        pageTitleLayout.addView(myOffersLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 15, 2, 2, 5));

        TextView myActiveOffersLabel = new TextView(context);
        myActiveOffersLabel.setText(LocaleController.getString("HtTotalIncome", R.string.HtTotalIncome) + ": 3744.86$");
        myActiveOffersLabel.setTextSize(14);
        myActiveOffersLabel.setTypeface(myOffersLabel.getTypeface(), Typeface.BOLD);
        myActiveOffersLabel.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
        pageTitleLayout.addView(myActiveOffersLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 15, 2, 2, 5));
        offerCreationLayout.addView(pageTitleLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 5, 5, 45, 5));

        FrameLayout offerCreationButtonFrame = new FrameLayout(context);
        LinearLayout offerCreationButtonLayout = new LinearLayout(context);
        ImageView offerCreationDrawableView = new ImageView(context);
        offerCreationDrawableView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        offerCreationButtonLayout.addView(offerCreationDrawableView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 50, 17, 0, 0));

        TextView offerCreationButton = new TextView(context) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 1.0f : 0.5f);
            }

            @Override
            public void setTextColor(int color) {
                super.setTextColor(color);
                setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(color));
            }
        };
        offerCreationButton.setText(LocaleController.getString("HtNewOffer", R.string.HtNewOffer));
        offerCreationButton.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        offerCreationButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        offerCreationButton.setTypeface(offerCreationButton.getTypeface(), Typeface.BOLD);
        offerCreationButton.setEnabled(true);
        offerCreationButton.setHovered(true);
        offerCreationButton.setElevation(6.0f);
        offerCreationButton.setGravity(Gravity.CENTER);

        HtCreateOfferActivity fragment = new HtCreateOfferActivity();
        offerCreationButton.setOnClickListener((v) -> {
            presentFragment(fragment);
            fragment.setActionType(HtCreateOfferActivity.ActionType.CREATE);
        });
        offerCreationButtonLayout.setBackgroundColor(context.getResources().getColor(R.color.ht_green));
        offerCreationButtonLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), context.getResources().getColor(R.color.ht_green)));
        offerCreationButtonLayout.setGravity(Gravity.CENTER);
        offerCreationButtonLayout.addView(offerCreationButton, LayoutHelper.createLinear(80, 50, Gravity.CENTER, -50, 0, 0, 0));
        offerCreationButtonFrame.addView(offerCreationButtonLayout, LayoutHelper.createLinear(120, 50, Gravity.CENTER, 0, 0, 0, 0));
        offerCreationLayout.addView(offerCreationButtonFrame, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 0, 0, 0));
        relativeLayout2.addView(offerCreationLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 20, 0, 0));
        relativeLayout2.addView(new HtFiltersCell(context, this), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 20, 0, 0));

        ScrollView scrollView = new ScrollView(context);
        LinearLayout scrollviewLayout = new LinearLayout(context);
        scrollviewLayout.setOrientation(LinearLayout.VERTICAL);
//        scrollviewLayout.addView(new OfferFiltersLayout(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.START, 0,20, 0, 20));
//        scrollviewLayout.addView(new HtDividerCell(context));

        // ------------ DATABASE DEMO ----------------
        ArrayList<OfferDto> offers = HtSQLite.getInstance().getAllOffers(UserConfig.getInstance(currentAccount).clientUserId);
        // --------------------------------------------


        offersLayout = new LinearLayout(context);
        offersLayout.setBackgroundColor(Theme.getColor(Theme.key_graySection));
        offersLayout.setOrientation(LinearLayout.VERTICAL);
        addOffersToLayout(offers);
        scrollviewLayout.addView(offersLayout);

        scrollView.addView(scrollviewLayout);
        relativeLayout2.addView(scrollView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.START, 0, 0, 0, 0));
        relativeLayout2.addView(new HtDividerCell(context), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 20, Gravity.BOTTOM, 0, 20, 0, 20));


        LinearLayout viewOffersLayout = new LinearLayout(context);
        viewOffersLayout.setOrientation(LinearLayout.VERTICAL);

        relativeLayout2.addView(new HtDividerCell(context), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 45, Gravity.BOTTOM, 0, dpHeight - 70 - 55, 0, 0));

        linearLayout.addView(relativeLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.END, 0, 0, 0, 0));
        inited = true;
        return fragmentView;
    }

    public void addOffersToLayout(ArrayList<OfferDto> offers) {
        if(!inited)
            return;
        offersLayout.removeAllViews();
        if (offers == null)
            return;
        for (OfferDto offerDto : offers) {
//            OfferCell offerCell1 = new OfferCell<OffersActivity>(context, this, offerDto, 1)  {
            HtChatMessageCell offerCell1 = new HtChatMessageCell(context) {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    setAlpha(enabled ? 1.0f : 0.5f);
                    setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(Theme.getColor(Theme.key_dialogTextGray)));
                }
            };
            offerCell1.setOfferUUID(offerDto.getServerUUID());
            offerCell1.setOut(true);
            offerCell1.setStatus(offerDto.getStatus());
            offerCell1.setOfferUUID(offerDto.getServerUUID());
            offerCell1.setArchived(offerDto.getStatus() == OfferStatus.ARCHIVED);
            offerCell1.titleLabel.setText(offerDto.getTitle());
            offerCell1.descriptionLabel.setText(offerDto.getDescription());
            offerCell1.rateLabel.setText(offerDto.getRate() + offerDto.getCurrency() + " " + offerDto.getRateType());
            try {
                String[] exp = offerDto.getTime().split("-");
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(exp[0]));
                cal.set(Calendar.MONTH, Integer.parseInt(exp[1]));
                cal.set(Calendar.YEAR, Integer.parseInt(exp[2]));
                if(((new Date()).toInstant().toEpochMilli()) > cal.getTimeInMillis())
                    offerCell1.msgTimeLabel.setText(LocaleController.getString("HtExpired", R.string.HtExpired));
                else
                    offerCell1.msgTimeLabel.setText(LocaleController.getString("HtValidUntil", R.string.HtValidUntil) + "\n" + offerDto.getTime());
            } catch (Exception e){
                offerCell1.msgTimeLabel.setText(LocaleController.getString("HtValidUntil", R.string.HtValidUntil) + "\n" + offerDto.getTime());
            }
            offerCell1.addressLabel.setText(offerDto.getLocation());
            offerCell1.setRate("" + offerDto.getRate());
            offerCell1.setRateType(offerDto.getRateType());
            offerCell1.setCurrency(offerDto.getCurrency());
            offerCell1.setCategory(offerDto.getCategory());
            offerCell1.setSubCategory(offerDto.getSubCategory());
            offerCell1.setPaymentConfig(offerDto.getConfigText());
            offerCell1.setTerms(offerDto.getTerms());
            offerCell1.expireLabel.setText(offerDto.getTime());
            offerCell1.setParent(this);
            offerCell1.setMessageText("https://ht.me/" + OFFER_MESSAGE_PREFIX + Base64.getEncoder().encodeToString((offerDto.getTitle() + "___" + offerDto.getRate() + "___" + offerDto.getRateType() + "___" + offerDto.getCurrency() + "___" + offerDto.getLocation() + "___" + offerDto.getTime() + "___" + offerDto.getCategory() + "___" + offerDto.getSubCategory() + "___" + offerDto.getConfigText() + "___" + offerDto.getTerms() + "___" + offerDto.getDescription()).getBytes()));
            offersLayout.addView(offerCell1);
            ObjectAnimator anim1 = ObjectAnimator.ofFloat(offerCell1, "scaleX", 0, 1);
            anim1.start();
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        offersLayout.removeAllViews();
        ArrayList<OfferDto> offers = HtSQLite.getInstance().getAllOffers(UserConfig.getInstance(currentAccount).clientUserId);
        addOffersToLayout(offers);
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

    public LinearLayout getOffersLayout() {
        return offersLayout;
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



package org.telegram.ui.Heymate;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.amplifyframework.api.graphql.PaginatedResult;
import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.AmplifyModels.Offer;
import org.telegram.ui.Heymate.AmplifyModels.TimeSlot;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import works.heymate.core.Texts;
import works.heymate.core.Utils;
import works.heymate.core.offer.OfferUtils;
import works.heymate.core.wallet.Wallet;

public class OffersActivity extends BaseFragment {

    private static final String ACTIVE_OFFERS_PLACE_HOLDER = "{active_offers}";

    private boolean inited = false;
    private OfferController offerController = OfferController.getInstance();
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
        DatabaseWatchDog.getInstance().config(currentAccount);
        this.context = context;

//        HtAmplify.getInstance(context).signUp(currentAccount);
//        HtAmplify.getInstance(context).signIn(currentAccount);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("HtManageOffers", R.string.HtManageOffers));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = LayoutInflater.from(context).inflate(R.layout.activity_offers, null, false);

        TextView textTitle = fragmentView.findViewById(R.id.text_title);
        textTitle.setTypeface(textTitle.getTypeface(), Typeface.BOLD);
        textTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textTitle.setText(Texts.get(Texts.OFFERS));
        Drawable myOffersDrawable = ContextCompat.getDrawable(context, R.drawable.offer);
        myOffersDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        textTitle.setCompoundDrawablePadding(AndroidUtilities.dp(5));
        textTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, myOffersDrawable, null);

        mTextStatus = fragmentView.findViewById(R.id.text_status);
        mTextStatus.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));

        mButtonSchedule = fragmentView.findViewById(R.id.button_schedule);
        mButtonSchedule.setOnClickListener(v -> presentFragment(new MyScheduleActivity()));
        mButtonSchedule.setVisibility(View.INVISIBLE); // TODO Temporary code

        TextView buttonAdd = fragmentView.findViewById(R.id.button_add);
        buttonAdd.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));
        buttonAdd.setTypeface(buttonAdd.getTypeface(), Typeface.BOLD);
        buttonAdd.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), ContextCompat.getColor(context, R.color.ht_theme)));
        buttonAdd.setText(Texts.get(Texts.ADD));
        buttonAdd.setOnClickListener(v -> presentFragment(new HtCreateOfferActivity()));

        HtFiltersCell filters = fragmentView.findViewById(R.id.filters);
        filters.setBaseFragment(this);

        offersLayout = fragmentView.findViewById(R.id.list_offer);
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

    private void onActiveOffersUpdated(int activeOffers) {
        String activeOffersStatus = Texts.get(Texts.OFFERS_ACTIVE_OFFERS).toString().replace(ACTIVE_OFFERS_PLACE_HOLDER, String.valueOf(activeOffers));

        mTextStatus.setText(activeOffersStatus);

        Drawable icon = ContextCompat.getDrawable(getParentActivity(), R.drawable.input_calendar1).mutate();
        icon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText), PorterDuff.Mode.SRC_IN);

        if (activeOffers == 0) {
            mButtonSchedule.setImageDrawable(icon);
        }
        else {
            Drawable notif = ContextCompat.getDrawable(getParentActivity(), R.drawable.input_calendar2).mutate();
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
//            OfferCell offerCell1 = new OfferCell<OffersActivity>(context, this, offerDto, 1)  {
            HtChatMessageCell offerCell1 = new HtChatMessageCell(context) {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    setAlpha(enabled ? 1.0f : 0.5f);
                    setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(Theme.getColor(Theme.key_dialogTextGray)));
                }
            };
            offerCell1.setOffer(offerDto.asOffer());
            offerCell1.setOut(true);
            offerCell1.setStatus(offerDto.getStatus());
            offerCell1.setOfferUUID(offerDto.getServerUUID());
            offerCell1.setArchived(offerDto.getStatus() == OfferStatus.ARCHIVED);
            offerCell1.titleLabel.setText(offerDto.getTitle());
            offerCell1.descriptionLabel.setText(offerDto.getDescription());
            offerCell1.rateLabel.setText(offerDto.getRate() + offerDto.getCurrency() + " " + offerDto.getRateType());
            TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
            String name;

            if (user.username != null) {
                name = "@" + user.username;
            }
            else {
                name = user.first_name;

                if (!TextUtils.isEmpty(user.last_name)) {
                    name = name + " " + user.last_name;
                }
            }
            String message = OfferUtils.serializeBeautiful(offerDto.asOffer(), name , OfferUtils.CATEGORY, OfferUtils.EXPIRY);
            offerCell1.configLabel.setText(message);
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
            ArrayList<TimeSlot> timeSlots = new ArrayList<>();
            HtAmplify.getInstance(context).getAvailableTimeSlots(offerDto.getServerUUID(), ((success, data, exception) -> {
                Utils.runOnUIThread(() -> {
                    if (!success) {
                        if (exception != null) {
                            Log.e("HtAmplify", "Failed to get time slots for offer with id " + offerDto.getServerUUID(), exception);
                        }
                        return;
                    }
                    for (TimeSlot timeSlot : ((PaginatedResult<TimeSlot>)(data)).getItems()) {
                        timeSlots.add(timeSlot);
                    }
                    ArrayList<Long> dates = new ArrayList<>();
                    for(TimeSlot timeSlot : timeSlots){
                        dates.add(((long) (timeSlot.getStartTime())) * 1000);
                        dates.add(((long) (timeSlot.getEndTime())) * 1000);
                    }
                    offerCell1.setDateSlots(dates);
                });
            }));
            ArrayList<Long> dates = new ArrayList<>();
            offerCell1.setDateSlots(dates);
            offerCell1.setParent(this);
            // TODO
//            offerCell1.setMessageText("https://ht.me/" + OFFER_MESSAGE_PREFIX + Base64.getEncoder().encodeToString((offerDto.getTitle() + "___" + offerDto.getRate() + "___" + offerDto.getRateType() + "___" + offerDto.getCurrency() + "___" + offerDto.getLocation() + "___" + offerDto.getTime() + "___" + offerDto.getCategory() + "___" + offerDto.getSubCategory() + "___" + offerDto.getConfigText() + "___" + offerDto.getTerms() + "___" + offerDto.getDescription()).getBytes()));
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



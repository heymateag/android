package org.telegram.ui.Heymate;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;

import com.amplifyframework.datastore.generated.model.Offer;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import works.heymate.beta.R;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.createoffer.PriceInputItem;
import org.telegram.ui.Heymate.widget.RoundedCornersImageView;

import works.heymate.core.Texts;
import works.heymate.core.offer.OfferUtils;
import works.heymate.core.offer.PurchasePlanInfo;
import works.heymate.core.offer.PurchasePlanTypes;

public class HtOfferDetailsPopUp extends AlertDialog.Builder {

    private static final int IMAGE_WIDTH_DP = 360 - 16 - 16;

    private int idCounter = 1;
    public ImageView closeImage;

    private Offer offer;
    private OfferUtils.PhraseInfo phraseInfo;

    public HtOfferDetailsPopUp(Context context, BaseFragment parent, Offer offer, OfferUtils.PhraseInfo phraseInfo) {
        super(context, 0);
        AlertDialog.Builder builder = this;

        this.offer = offer;
        this.phraseInfo = phraseInfo;

        ScrollView scrollView = new ScrollView(context);
        RelativeLayout holderLayout = new RelativeLayout(context);
        RelativeLayout mainLayout = new RelativeLayout(context);

        closeImage = new ImageView(context);
        closeImage.setId(idCounter++);
        Drawable closeDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.pip_close);
        closeDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(works.heymate.beta.R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        closeImage.setImageDrawable(closeDrawable);
        RelativeLayout.LayoutParams closeImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(20), AndroidUtilities.dp(20));
        closeImageLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        closeImageLayoutParams.setMargins(0, AndroidUtilities.dp(10), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(closeImage, closeImageLayoutParams);

        LinearLayout statusLayout = new LinearLayout(context);
        statusLayout.setId(idCounter++);
        statusLayout.setBackgroundColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        RelativeLayout.LayoutParams statusLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(2), AndroidUtilities.dp(45));
        statusLayoutParams.setMargins(AndroidUtilities.dp(20), 0, 0, AndroidUtilities.dp(10));
        statusLayoutParams.addRule(RelativeLayout.BELOW, closeImage.getId());
        mainLayout.addView(statusLayout, statusLayoutParams);

        TextView titleText = new TextView(context);
        titleText.setId(idCounter++);
        titleText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleText.setTextSize(16);
        titleText.setText(offer.getTitle());
        titleText.setTypeface(titleText.getTypeface(), Typeface.BOLD);
        RelativeLayout.LayoutParams titleTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleTextLayoutParams.addRule(RelativeLayout.BELOW, closeImage.getId());
        titleTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, statusLayout.getId());
        titleTextLayoutParams.setMargins(AndroidUtilities.dp(8), 0 , 0 , AndroidUtilities.dp(3));
        mainLayout.addView(titleText, titleTextLayoutParams);

        TextView categoryText = new TextView(context);
        categoryText.setId(idCounter++);
        categoryText.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        categoryText.setTextSize(14);
        categoryText.setText(offer.getCategory() + " - " + offer.getSubCategory());
        RelativeLayout.LayoutParams categoryTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        categoryTextLayoutParams.addRule(RelativeLayout.BELOW, titleText.getId());
        categoryTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, statusLayout.getId());
        categoryTextLayoutParams.setMargins(AndroidUtilities.dp(8), 0 , 0 , AndroidUtilities.dp(5));
        mainLayout.addView(categoryText, categoryTextLayoutParams);

        ImageView archiveImage = new ImageView(context);
        archiveImage.setId(idCounter++);
//        Drawable archiveDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.chats_archive);
//        archiveDrawable.setColorFilter(context.getResources().getColor(works.heymate.beta.R.color.ht_green), PorterDuff.Mode.MULTIPLY);
//        archiveImage.setImageDrawable(archiveDrawable);
        RelativeLayout.LayoutParams archiveImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(25), AndroidUtilities.dp(25));
        archiveImageLayoutParams.addRule(RelativeLayout.BELOW, closeImage.getId());
        archiveImageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        archiveImageLayoutParams.setMargins(0, 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(archiveImage, archiveImageLayoutParams);

        RoundedCornersImageView offerImage = new RoundedCornersImageView(context);
        offerImage.setId(idCounter++);

        if (offer != null && offer.getHasImage() != null && offer.getHasImage()) {
            offerImage.setImageDrawable(null);

            String offerId = offer.getId();
            int size = AndroidUtilities.dp(IMAGE_WIDTH_DP);

            FileCache.get().getImage(offerId, size, (success, drawable, exception) -> {
                offerImage.setImageDrawable(drawable);
            });
        }
        else {
            offerImage.setVisibility(View.GONE);
        }
        offerImage.setCornerRadius(AndroidUtilities.dp(4));
        RelativeLayout.LayoutParams offerImageLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(150));
        offerImageLayoutParams.addRule(RelativeLayout.BELOW, statusLayout.getId());
        offerImageLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(5), AndroidUtilities.dp(20), AndroidUtilities.dp(20));
        mainLayout.addView(offerImage, offerImageLayoutParams);

        TextView descriptionText = new TextView(context);
        descriptionText.setId(idCounter++);
        descriptionText.setTextSize(15);
        descriptionText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        descriptionText.setText(offer.getDescription());
        RelativeLayout.LayoutParams descriptionTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descriptionTextLayoutParams.addRule(RelativeLayout.BELOW, offerImage.getId());
        descriptionTextLayoutParams.setMargins(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(descriptionText, descriptionTextLayoutParams);

        ImageView addressImage = new ImageView(context);
        addressImage.setId(idCounter++);
        Drawable addressDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_location);
        addressDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray2), PorterDuff.Mode.MULTIPLY));
        addressImage.setImageDrawable(addressDrawable);
        RelativeLayout.LayoutParams addressImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(20), AndroidUtilities.dp(20));
        addressImageLayoutParams.addRule(RelativeLayout.BELOW, descriptionText.getId());
        addressImageLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(10), AndroidUtilities.dp(10));
        mainLayout.addView(addressImage, addressImageLayoutParams);

        TextView addressText = new TextView(context);
        addressText.setId(idCounter++);
        if (MeetingType.ONLINE_MEETING.equals(offer.getMeetingType())) {
            addressText.setText(Texts.get(Texts.ONLINE_MEETING));
        }
        else {
            addressText.setText(offer.getLocationData());
        }
        addressText.setTextSize(13);
        addressText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        RelativeLayout.LayoutParams addressTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addressTextLayoutParams.addRule(RelativeLayout.BELOW, descriptionText.getId());
        addressTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, addressImage.getId());
        addressTextLayoutParams.setMargins(AndroidUtilities.dp(2), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(addressText, addressTextLayoutParams);

        ImageView expiryImage = new ImageView(context);
        expiryImage.setId(idCounter++);
        Drawable expiryDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_schedule);
        expiryDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray2), PorterDuff.Mode.MULTIPLY));
        expiryImage.setImageDrawable(expiryDrawable);
        RelativeLayout.LayoutParams expiryImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(20), AndroidUtilities.dp(20));
        expiryImageLayoutParams.addRule(RelativeLayout.BELOW, addressText.getId());
        expiryImageLayoutParams.setMargins(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(10), AndroidUtilities.dp(10));
        mainLayout.addView(expiryImage, expiryImageLayoutParams);

        TextView expiryText = new TextView(context);
        expiryText.setId(idCounter++);
        expiryText.setText(offer.getExpiry().format());
        expiryText.setTextSize(14);
        expiryText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        RelativeLayout.LayoutParams expiryTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        expiryTextLayoutParams.addRule(RelativeLayout.BELOW, addressText.getId());
        expiryTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, expiryImage.getId());
        expiryTextLayoutParams.setMargins(AndroidUtilities.dp(2), 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(expiryText, expiryTextLayoutParams);

        ImageView priceImage = new ImageView(context);
        priceImage.setId(idCounter++);
        Drawable priceDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_pricing);
        priceDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray2), PorterDuff.Mode.MULTIPLY));
        priceImage.setImageDrawable(priceDrawable);
        RelativeLayout.LayoutParams priceImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(20), AndroidUtilities.dp(20));
        priceImageLayoutParams.addRule(RelativeLayout.BELOW, expiryText.getId());
        priceImageLayoutParams.setMargins(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(10), AndroidUtilities.dp(10));
        mainLayout.addView(priceImage, priceImageLayoutParams);

        TextView priceText = new TextView(context);
        priceText.setId(idCounter++);
        try {
            PriceInputItem.PricingInfo pricingInfo = new PriceInputItem.PricingInfo(new JSONObject(offer.getPricingInfo()));
            priceText.setText(pricingInfo.price + pricingInfo.currency + " " + pricingInfo.rateType);
        } catch (Throwable t) { }
        priceText.setTextSize(14);
        priceText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        RelativeLayout.LayoutParams priceTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        priceTextLayoutParams.addRule(RelativeLayout.BELOW, expiryText.getId());
        priceTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, priceImage.getId());
        priceTextLayoutParams.setMargins(AndroidUtilities.dp(2), 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(priceText, priceTextLayoutParams);

        int userId = 0;

        try {
            userId = Integer.parseInt(offer.getUserId());
        } catch (NumberFormatException e) { }

        TLRPC.User user = null;

        if (userId != 0) {
            user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(userId);
        }

        String name;

        if (user != null) {
            if (user.username != null) {
                name = "@" + user.username;
            }
            else {
                name = user.first_name;

                if (!TextUtils.isEmpty(user.last_name)) {
                    name = name + " " + user.last_name;
                }
            }
        }
        else {
            name = "Service provider"; // TODO Texts
        }

        // TODO Make a clean text for offer details!
        String message = OfferUtils.serializeBeautiful(offer, null, name , OfferUtils.CATEGORY, OfferUtils.EXPIRY);

        TextView delayText = new TextView(context);
        delayText.setId(idCounter++);
        delayText.setTextSize(14);
        delayText.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        delayText.setText(message);
        RelativeLayout.LayoutParams delayTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        delayTextLayoutParams.addRule(RelativeLayout.BELOW, priceText.getId());
        delayTextLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(30), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(delayText, delayTextLayoutParams);

        TextView termsLinkText = new TextView(context);
        termsLinkText.setId(idCounter++);
        termsLinkText.setTextSize(14);
        termsLinkText.setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        termsLinkText.setText("Terms And Conditions Link");
        termsLinkText.setPaintFlags(termsLinkText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        RelativeLayout.LayoutParams termsLinkTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        termsLinkTextLayoutParams.addRule(RelativeLayout.BELOW, delayText.getId());
        termsLinkTextLayoutParams.setMargins(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(termsLinkText, termsLinkTextLayoutParams);

        LinearLayout buyButtonLayout = new LinearLayout(context);
        buyButtonLayout.setId(idCounter++);
        buyButtonLayout.setBackgroundColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        buyButtonLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), context.getResources().getColor(works.heymate.beta.R.color.ht_green)));
        buyButtonLayout.setGravity(Gravity.CENTER);
        buyButtonLayout.setOnClickListener(v -> {
            try {
                PriceInputItem.PricingInfo pricingInfo = new PriceInputItem.PricingInfo(new JSONObject(offer.getPricingInfo()));
                PurchasePlanInfo purchasePlanInfo = pricingInfo.getPurchasePlanInfo(PurchasePlanTypes.SINGLE);
                HeymatePayment.initPayment(parent, offer.getId(), purchasePlanInfo, phraseInfo == null ? null : phraseInfo.referralId);
            } catch (JSONException e) { }
        });
        RelativeLayout.LayoutParams buyButtonLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(120), AndroidUtilities.dp(50));
        buyButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buyButtonLayoutParams.addRule(RelativeLayout.BELOW, termsLinkText.getId());
        buyButtonLayoutParams.setMargins(0, AndroidUtilities.dp(20), AndroidUtilities.dp(20), 0);

        TextView buyText = new TextView(context);
        buyText.setText(LocaleController.getString("HtBuy", works.heymate.beta.R.string.HtBuy));
        buyText.setTextSize(17);
        buyText.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        buyText.setTypeface(buyText.getTypeface(), Typeface.BOLD);
        buyButtonLayout.addView(buyText);

        mainLayout.addView(buyButtonLayout, buyButtonLayoutParams);

        LinearLayout promoteButtonLayout = new LinearLayout(context);
        promoteButtonLayout.setId(idCounter++);
        promoteButtonLayout.setBackgroundColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        promoteButtonLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), context.getResources().getColor(works.heymate.beta.R.color.ht_green)));
        promoteButtonLayout.setGravity(Gravity.CENTER);
        promoteButtonLayout.setOnClickListener(v -> HeymatePayment.ensureWalletExistence(context, this::promote));
        RelativeLayout.LayoutParams promoteButtonLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(120), AndroidUtilities.dp(50));
        promoteButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        promoteButtonLayoutParams.addRule(RelativeLayout.BELOW, termsLinkText.getId());
        promoteButtonLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(20), 0, 0);

        TextView promoteText = new TextView(context);
        promoteText.setText(LocaleController.getString("HtPromote", R.string.HtPromote));
        promoteText.setTextSize(17);
        promoteText.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        promoteText.setTypeface(buyText.getTypeface(), Typeface.BOLD);
        promoteButtonLayout.addView(promoteText);

        mainLayout.addView(promoteButtonLayout, promoteButtonLayoutParams);

        RelativeLayout termsLayout = new RelativeLayout(context);
        termsLayout.setVisibility(View.GONE);

        ImageView backImage = new ImageView(context);
        backImage.setId(idCounter++);
        Drawable backDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.ic_ab_back);
        backDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(works.heymate.beta.R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        backImage.setImageDrawable(backDrawable);
        RelativeLayout.LayoutParams backImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(20), AndroidUtilities.dp(20));
        backImageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        backImageLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        termsLayout.addView(backImage, backImageLayoutParams);

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator anim1 = ObjectAnimator.ofFloat(termsLayout, "alpha", 1f, 0f);
                anim1.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        termsLayout.setVisibility(View.GONE);
                        mainLayout.setVisibility(View.VISIBLE);
                        ObjectAnimator anim2 = ObjectAnimator.ofFloat(mainLayout, "alpha", 0f, 1f);
                        anim2.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                anim1.start();
            }
        });

        TextView termsTitleText = new TextView(context);
        termsTitleText.setId(idCounter++);
        termsTitleText.setTextSize(18);
        termsTitleText.setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        termsTitleText.setText("Terms and Conditions");
        termsTitleText.setTypeface(termsTitleText.getTypeface(), Typeface.BOLD);
        RelativeLayout.LayoutParams termsTitleTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        termsTitleTextLayoutParams.addRule(RelativeLayout.BELOW, backImage.getId());
        termsTitleTextLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        termsLayout.addView(termsTitleText, termsTitleTextLayoutParams);

        TextView heymateTermsText = new TextView(context);
        heymateTermsText.setId(idCounter++);
        heymateTermsText.setTextSize(16);
        heymateTermsText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        heymateTermsText.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n" +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
        RelativeLayout.LayoutParams heymateTermsTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        heymateTermsTextLayoutParams.addRule(RelativeLayout.BELOW, termsTitleText.getId());
        heymateTermsTextLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        termsLayout.addView(heymateTermsText, heymateTermsTextLayoutParams);

        TextView termsText = new TextView(context);
        termsText.setId(idCounter++);
        termsText.setTextSize(16);
        termsText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        termsText.setText(offer.getTerms());
        RelativeLayout.LayoutParams termsTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        termsTextLayoutParams.addRule(RelativeLayout.BELOW, heymateTermsText.getId());
        termsTextLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        termsLayout.addView(termsText, termsTextLayoutParams);

        termsLinkText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator anim1 = ObjectAnimator.ofFloat(mainLayout, "alpha", 1f, 0f);
                anim1.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mainLayout.setVisibility(View.GONE);
                        termsLayout.setVisibility(View.VISIBLE);
                        ObjectAnimator anim2 = ObjectAnimator.ofFloat(termsLayout, "alpha", 0f, 1f);
                        anim2.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                anim1.start();
            }
        });

        holderLayout.addView(mainLayout);
        holderLayout.addView(termsLayout);
        scrollView.addView(holderLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        builder.setView(scrollView);
    }

    private void promote() {
        if (offer == null) {
            return;
        }

        if (phraseInfo == null || offer.getUserId() != null && offer.getUserId().equals(String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId))) {
            doPromote(null);
            return;
        }

        LoadingUtil.onLoadingStarted(getContext());

        ReferralUtils.getReferralId(phraseInfo, (success, referralId, exception) -> {
            LoadingUtil.onLoadingFinished();

            if (!success) {
                // TODO Organize error messages
                Toast.makeText(getContext(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
                return;
            }

            doPromote(referralId);
        });
    }

    private void doPromote(String referralId) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");

        TLRPC.User user = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();
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
        String message = OfferUtils.serializeBeautiful(offer, referralId, name, OfferUtils.CATEGORY, OfferUtils.EXPIRY);
        share.putExtra(Intent.EXTRA_TEXT, message);
        getContext().startActivity(Intent.createChooser(share, LocaleController.getString("HtPromoteYourOffer", works.heymate.beta.R.string.HtPromoteYourOffer)));
    }

}

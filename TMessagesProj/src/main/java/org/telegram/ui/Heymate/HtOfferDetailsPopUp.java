package org.telegram.ui.Heymate;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class HtOfferDetailsPopUp extends AlertDialog.Builder {

    private int idCounter = 1;
    public ImageView closeImage;

    public HtOfferDetailsPopUp(Context context, int progressStyle, String offerUUID) {
        super(context, progressStyle);
        AlertDialog.Builder builder = this;

        OfferDto dto = HtSQLite.getInstance().getOffer(offerUUID);

        ScrollView scrollView = new ScrollView(context);
        RelativeLayout holderLayout = new RelativeLayout(context);
        RelativeLayout mainLayout = new RelativeLayout(context);

        closeImage = new ImageView(context);
        closeImage.setId(idCounter++);
        Drawable closeDrawable = context.getResources().getDrawable(R.drawable.pip_close);
        closeDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        closeImage.setImageDrawable(closeDrawable);
        RelativeLayout.LayoutParams closeImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(20), AndroidUtilities.dp(20));
        closeImageLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        closeImageLayoutParams.setMargins(0, AndroidUtilities.dp(10), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(closeImage, closeImageLayoutParams);

        LinearLayout statusLayout = new LinearLayout(context);
        statusLayout.setId(idCounter++);
        statusLayout.setBackgroundColor(context.getResources().getColor(R.color.ht_green));
        RelativeLayout.LayoutParams statusLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(2), AndroidUtilities.dp(45));
        statusLayoutParams.setMargins(AndroidUtilities.dp(20), 0, 0, AndroidUtilities.dp(10));
        statusLayoutParams.addRule(RelativeLayout.BELOW, closeImage.getId());
        mainLayout.addView(statusLayout, statusLayoutParams);

        TextView titleText = new TextView(context);
        titleText.setId(idCounter++);
        titleText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleText.setTextSize(16);
        titleText.setText(dto.getTitle());
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
        categoryText.setText(dto.getCategory() + " - " + dto.getSubCategory());
        RelativeLayout.LayoutParams categoryTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        categoryTextLayoutParams.addRule(RelativeLayout.BELOW, titleText.getId());
        categoryTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, statusLayout.getId());
        categoryTextLayoutParams.setMargins(AndroidUtilities.dp(8), 0 , 0 , AndroidUtilities.dp(5));
        mainLayout.addView(categoryText, categoryTextLayoutParams);

        ImageView archiveImage = new ImageView(context);
        archiveImage.setId(idCounter++);
        Drawable archiveDrawable = context.getResources().getDrawable(R.drawable.chats_archive);
        archiveDrawable.setColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY);
        archiveImage.setImageDrawable(archiveDrawable);
        RelativeLayout.LayoutParams archiveImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(25), AndroidUtilities.dp(25));
        archiveImageLayoutParams.addRule(RelativeLayout.BELOW, closeImage.getId());
        archiveImageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        archiveImageLayoutParams.setMargins(0, 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(archiveImage, archiveImageLayoutParams);

        BackupImageView offerImage = new BackupImageView(context);
        offerImage.setId(idCounter++);
        offerImage.setImageDrawable(context.getResources().getDrawable(R.drawable.np));
        offerImage.setRoundRadius(AndroidUtilities.dp(4));
        RelativeLayout.LayoutParams offerImageLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(150));
        offerImageLayoutParams.addRule(RelativeLayout.BELOW, statusLayout.getId());
        offerImageLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(5), AndroidUtilities.dp(20), AndroidUtilities.dp(20));
        mainLayout.addView(offerImage, offerImageLayoutParams);

        TextView descriptionText = new TextView(context);
        descriptionText.setId(idCounter++);
        descriptionText.setTextSize(15);
        descriptionText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        descriptionText.setText(dto.getDescription());
        RelativeLayout.LayoutParams descriptionTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descriptionTextLayoutParams.addRule(RelativeLayout.BELOW, offerImage.getId());
        descriptionTextLayoutParams.setMargins(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(descriptionText, descriptionTextLayoutParams);

        ImageView addressImage = new ImageView(context);
        addressImage.setId(idCounter++);
        Drawable addressDrawable = context.getResources().getDrawable(R.drawable.location_on_24_px_1);
        addressDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray2), PorterDuff.Mode.MULTIPLY));
        addressImage.setImageDrawable(addressDrawable);
        RelativeLayout.LayoutParams addressImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(20), AndroidUtilities.dp(20));
        addressImageLayoutParams.addRule(RelativeLayout.BELOW, descriptionText.getId());
        addressImageLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(10), AndroidUtilities.dp(10));
        mainLayout.addView(addressImage, addressImageLayoutParams);

        TextView addressText = new TextView(context);
        addressText.setId(idCounter++);
        addressText.setText(dto.getLocation());
        addressText.setTextSize(13);
        addressText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        RelativeLayout.LayoutParams addressTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addressTextLayoutParams.addRule(RelativeLayout.BELOW, descriptionText.getId());
        addressTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, addressImage.getId());
        addressTextLayoutParams.setMargins(AndroidUtilities.dp(2), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(addressText, addressTextLayoutParams);

        ImageView expiryImage = new ImageView(context);
        expiryImage.setId(idCounter++);
        Drawable expiryDrawable = context.getResources().getDrawable(R.drawable.watch_later_24_px_1);
        expiryDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray2), PorterDuff.Mode.MULTIPLY));
        expiryImage.setImageDrawable(expiryDrawable);
        RelativeLayout.LayoutParams expiryImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(20), AndroidUtilities.dp(20));
        expiryImageLayoutParams.addRule(RelativeLayout.BELOW, addressText.getId());
        expiryImageLayoutParams.setMargins(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(10), AndroidUtilities.dp(10));
        mainLayout.addView(expiryImage, expiryImageLayoutParams);

        TextView expiryText = new TextView(context);
        expiryText.setId(idCounter++);
        expiryText.setText(dto.getTime());
        expiryText.setTextSize(14);
        expiryText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        RelativeLayout.LayoutParams expiryTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        expiryTextLayoutParams.addRule(RelativeLayout.BELOW, addressText.getId());
        expiryTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, expiryImage.getId());
        expiryTextLayoutParams.setMargins(AndroidUtilities.dp(2), 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(expiryText, expiryTextLayoutParams);

        ImageView priceImage = new ImageView(context);
        priceImage.setId(idCounter++);
        Drawable priceDrawable = context.getResources().getDrawable(R.drawable.money);
        priceDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray2), PorterDuff.Mode.MULTIPLY));
        priceImage.setImageDrawable(priceDrawable);
        RelativeLayout.LayoutParams priceImageLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(20), AndroidUtilities.dp(20));
        priceImageLayoutParams.addRule(RelativeLayout.BELOW, expiryText.getId());
        priceImageLayoutParams.setMargins(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(10), AndroidUtilities.dp(10));
        mainLayout.addView(priceImage, priceImageLayoutParams);

        TextView priceText = new TextView(context);
        priceText.setId(idCounter++);
        priceText.setText(dto.getRate() + dto.getCurrency() + " " + dto.getRateType());
        priceText.setTextSize(14);
        priceText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        RelativeLayout.LayoutParams priceTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        priceTextLayoutParams.addRule(RelativeLayout.BELOW, expiryText.getId());
        priceTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, priceImage.getId());
        priceTextLayoutParams.setMargins(AndroidUtilities.dp(2), 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(priceText, priceTextLayoutParams);

        JSONObject json = null;
        try {
            json = new JSONObject(dto.getConfigText());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView delayText = new TextView(context);
        delayText.setId(idCounter++);
        delayText.setTextSize(14);
        delayText.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        try {
            delayText.setText("" + json.get("arg0"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RelativeLayout.LayoutParams delayTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        delayTextLayoutParams.addRule(RelativeLayout.BELOW, priceText.getId());
        delayTextLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(30), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(delayText, delayTextLayoutParams);

        TextView delayValueText = new TextView(context);
        delayValueText.setId(idCounter++);
        delayValueText.setTextSize(14);
        delayValueText.setTextColor(context.getResources().getColor(R.color.ht_green));
        try {
            delayValueText.setText("" + json.get("arg1"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        delayValueText.setPaintFlags(delayValueText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        RelativeLayout.LayoutParams delayValueTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        delayValueTextLayoutParams.addRule(RelativeLayout.BELOW, priceText.getId());
        delayValueTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, delayText.getId());
        delayValueTextLayoutParams.setMargins(0, AndroidUtilities.dp(30), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(delayValueText, delayValueTextLayoutParams);

        TextView depositText = new TextView(context);
        depositText.setId(idCounter++);
        depositText.setTextSize(14);
        depositText.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        depositText.setText("Deposit");
        RelativeLayout.LayoutParams depositTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        depositTextLayoutParams.addRule(RelativeLayout.BELOW, delayText.getId());
        depositTextLayoutParams.setMargins(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(depositText, depositTextLayoutParams);

        TextView depositValueText = new TextView(context);
        depositValueText.setId(idCounter++);
        depositValueText.setTextSize(14);
        depositValueText.setTextColor(context.getResources().getColor(R.color.ht_green));
        try {
            depositValueText.setText("" + json.get("arg2"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        depositValueText.setPaintFlags(depositValueText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        RelativeLayout.LayoutParams depositValueTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        depositValueTextLayoutParams.addRule(RelativeLayout.BELOW, delayText.getId());
        depositValueTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, depositText.getId());
        depositValueTextLayoutParams.setMargins(0, 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(depositValueText, depositValueTextLayoutParams);

        TextView cancellation1Text = new TextView(context);
        cancellation1Text.setId(idCounter++);
        cancellation1Text.setTextSize(14);
        cancellation1Text.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        try {
            cancellation1Text.setText("" + json.get("arg3"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RelativeLayout.LayoutParams cancellation1TextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cancellation1TextLayoutParams.addRule(RelativeLayout.BELOW, depositText.getId());
        cancellation1TextLayoutParams.setMargins(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(cancellation1Text, cancellation1TextLayoutParams);

        TextView cancellation1ValueText = new TextView(context);
        cancellation1ValueText.setId(idCounter++);
        cancellation1ValueText.setTextSize(14);
        cancellation1ValueText.setTextColor(context.getResources().getColor(R.color.ht_green));
        try {
            cancellation1ValueText.setText("" + json.get("arg4"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cancellation1ValueText.setPaintFlags(cancellation1ValueText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        RelativeLayout.LayoutParams cancellation1ValueTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cancellation1ValueTextLayoutParams.addRule(RelativeLayout.BELOW, depositText.getId());
        cancellation1ValueTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, cancellation1Text.getId());
        cancellation1ValueTextLayoutParams.setMargins(0, 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(cancellation1ValueText, cancellation1ValueTextLayoutParams);

        TextView cancellation2Text = new TextView(context);
        cancellation2Text.setId(idCounter++);
        cancellation2Text.setTextSize(14);
        cancellation2Text.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        try {
            cancellation2Text.setText("" + json.get("arg5"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RelativeLayout.LayoutParams cancellation2TextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cancellation2TextLayoutParams.addRule(RelativeLayout.BELOW, cancellation1Text.getId());
        cancellation2TextLayoutParams.setMargins(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(cancellation2Text, cancellation2TextLayoutParams);

        TextView cancellation2ValueText = new TextView(context);
        cancellation2ValueText.setId(idCounter++);
        cancellation2ValueText.setTextSize(14);
        cancellation2ValueText.setTextColor(context.getResources().getColor(R.color.ht_green));
        try {
            cancellation2ValueText.setText("" + json.get("arg6"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cancellation2ValueText.setPaintFlags(cancellation2ValueText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        RelativeLayout.LayoutParams cancellation2ValueTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cancellation2ValueTextLayoutParams.addRule(RelativeLayout.BELOW, cancellation1Text.getId());
        cancellation2ValueTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, cancellation2Text.getId());
        cancellation2ValueTextLayoutParams.setMargins(0, 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(cancellation2ValueText, cancellation2ValueTextLayoutParams);

        TextView termsLinkText = new TextView(context);
        termsLinkText.setId(idCounter++);
        termsLinkText.setTextSize(14);
        termsLinkText.setTextColor(context.getResources().getColor(R.color.ht_green));
        termsLinkText.setText("Terms And Conditions Link");
        termsLinkText.setPaintFlags(termsLinkText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        RelativeLayout.LayoutParams termsLinkTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        termsLinkTextLayoutParams.addRule(RelativeLayout.BELOW, cancellation2Text.getId());
        termsLinkTextLayoutParams.setMargins(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        mainLayout.addView(termsLinkText, termsLinkTextLayoutParams);

        LinearLayout buyButtonLayout = new LinearLayout(context);
        buyButtonLayout.setId(idCounter++);
        buyButtonLayout.setBackgroundColor(context.getResources().getColor(R.color.ht_green));
        buyButtonLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), context.getResources().getColor(R.color.ht_green)));
        buyButtonLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams buyButtonLayoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(150), AndroidUtilities.dp(50));
        buyButtonLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        buyButtonLayoutParams.addRule(RelativeLayout.BELOW, termsLinkText.getId());
        buyButtonLayoutParams.setMargins(0, AndroidUtilities.dp(20), 0, 0);

        TextView buyText = new TextView(context);
        buyText.setText(LocaleController.getString("HtBuy", R.string.HtBuy));
        buyText.setTextSize(17);
        buyText.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        buyText.setTypeface(buyText.getTypeface(), Typeface.BOLD);
        buyButtonLayout.addView(buyText);

        mainLayout.addView(buyButtonLayout, buyButtonLayoutParams);

        RelativeLayout termsLayout = new RelativeLayout(context);
        termsLayout.setVisibility(View.GONE);

        ImageView backImage = new ImageView(context);
        backImage.setId(idCounter++);
        Drawable backDrawable = context.getResources().getDrawable(R.drawable.ic_ab_back);
        backDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY));
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
        termsTitleText.setTextColor(context.getResources().getColor(R.color.ht_green));
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
        termsText.setText(dto.getTerms());
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
}
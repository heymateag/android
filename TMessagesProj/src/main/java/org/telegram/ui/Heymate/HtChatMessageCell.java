package org.telegram.ui.Heymate;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.Heymate.AmplifyModels.Offer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import works.heymate.core.offer.OfferUtils;

public class HtChatMessageCell extends FrameLayout {

    private Context context;
    private BackupImageView image;
    private ImageView editIcon;
    private boolean showingDetails = false;
    public TextView titleLabel;
    public TextView descriptionLabel;
    public TextView msgTimeLabel;
    public TextView rateLabel;
    public TextView configLabel;
    public TextView addressLabel;
    public TextView expireLabel;
    private boolean out = false;
    private LinearLayout promoteLayout;
    private LinearLayout buyLayout;
    private LinearLayout viewLayout;
    private MessageObject message;
    private BaseFragment parent;
    private String messageText;
    private String rate = "";
    private String rateType = "";
    private String currency = "";
    private String category = "";
    private String subCategory = "";
    private String paymentConfig = "{\"arg1\": \"1\",\"arg2\": \"2\",\"arg3\": \"3\",\"arg4\": \"4\",\"arg5\": \"5\",\"arg6\": \"6\",\"arg7\": \"7\"}";
    private String terms = "";
    private OfferStatus status;
    private String offerUUID = "";
    private LinearLayout statusLayout;
    private boolean archived;
    private Drawable archiveDrawable;
    private ImageView archiveIcon;
    private ArrayList<Long> dateSlots = new ArrayList<>();

    public ArrayList<Long> getDateSlots() {
        return dateSlots;
    }

    public void setDateSlots(ArrayList<Long> dateSlots) {
        this.dateSlots = dateSlots;
    }

    private Offer offer;

    public void setOffer(Offer offer) {
        this.offer = offer;
        offerUUID = offer.getId();
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public void setRateType(String rateType) {
        this.rateType = rateType;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public void setPaymentConfig(String paymentConfig) {
        this.paymentConfig = paymentConfig;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }


    public HtChatMessageCell(@NonNull Context context) {
        super(context);
        this.context = context;
        Configuration configuration = context.getResources().getConfiguration();
        int dpWidth = configuration.screenWidthDp;
        int dpHeight = configuration.screenHeightDp;

        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_chat_topPanelBackground)));
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Theme.getColor(Theme.key_chat_topPanelBackground));

        LinearLayout topLayer = new LinearLayout(context);
        statusLayout = new LinearLayout(context);
        LinearLayout titleLayout = new LinearLayout(context);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        statusLayout.setBackgroundColor(context.getResources().getColor(R.color.ht_green));
        topLayer.addView(statusLayout, LayoutHelper.createFrame(2, LayoutHelper.MATCH_PARENT, Gravity.LEFT, 0, 0, 20, 0));

        titleLabel = new TextView(context);
        titleLabel.setText("Nail Polish");
        titleLabel.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
        titleLabel.setTextSize(16);
        titleLayout.addView(titleLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        TextView ownerLabel = new TextView(context);
        ownerLabel.setText("Owner");
        ownerLabel.setTypeface(ownerLabel.getTypeface(), Typeface.BOLD);
        ownerLabel.setTextSize(15);
        ownerLabel.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        titleLayout.addView(ownerLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 10, 0, 0));
        topLayer.addView(titleLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.25f));

        editIcon = new ImageView(context);
        editIcon.setVisibility(GONE);

        Drawable editDrawable = context.getResources().getDrawable(R.drawable.msg_edit);
        editDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_statisticChartLine_lightblue), PorterDuff.Mode.MULTIPLY));
        editIcon.setImageDrawable(editDrawable);
        editIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OfferDto offerDto = HtSQLite.getInstance().getOffer(offerUUID);
                HtCreateOfferActivity fragment = new HtCreateOfferActivity();
                parent.presentFragment(fragment);
                fragment.setActionType(HtCreateOfferActivity.ActionType.EDIT);
                fragment.setDescription(offerDto.getDescription());
                fragment.setRateType(offerDto.getRateType(), 0);
                fragment.setFee("" + offerDto.getRate(), 0);
                fragment.setCurrency(offerDto.getCurrency(), 0);
                fragment.setCanEdit(true);
                fragment.setLocation(offerDto.getLocation(), offerDto.getLatitude(), offerDto.getLongitude());
                fragment.setCategory(offerDto.getCategory());
                fragment.setSubCategory(offerDto.getSubCategory());
                fragment.setTitle(offerDto.getTitle());
                fragment.setTerms(offerDto.getTerms());
                fragment.setPaymentConfig(offerDto.getConfigText());
                fragment.setOfferUUID(offerUUID);
                fragment.setDateSlots(dateSlots);
            }
        });
        topLayer.addView(editIcon, LayoutHelper.createFrame(25, 25, Gravity.RIGHT, 0, 0, 20, 0));

        archiveIcon = new ImageView(context);
        archiveDrawable = context.getResources().getDrawable(R.drawable.chats_archive);
        archiveDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        archiveIcon.setImageDrawable(archiveDrawable);
        archiveIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HtSQLite.getInstance().archiveOffer(offerUUID);
                if(parent instanceof OffersActivity){
                    ((OffersActivity) parent).addOffersToLayout(HtSQLite.getInstance().getAllOffers(UserConfig.getInstance(parent.getCurrentAccount()).clientUserId));
                }
            }
        });
        topLayer.addView(archiveIcon, LayoutHelper.createFrame(25, 25, Gravity.RIGHT, 0, 0, 20, 0));
        mainLayout.addView(topLayer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 20, 0, 0));

        LinearLayout photoLayout = new LinearLayout(context);
        photoLayout.setGravity(Gravity.CENTER);
        image = new BackupImageView(context);
        Bitmap imageBitmap = HtStorage.getInstance().getOfferImage(context, offerUUID);
        if(imageBitmap == null){
            image.setImageDrawable(context.getResources().getDrawable(R.drawable.np));
        } else {
            image.setImageBitmap(imageBitmap);
        }
        image.setRoundRadius(AndroidUtilities.dp(4));

        photoLayout.addView(image, LayoutHelper.createLinear((int) (dpWidth * 0.82), 150));
        mainLayout.addView(photoLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0,12,0,8));

        descriptionLabel = new TextView(context);
        descriptionLabel.setText("Lorem ipsum some text some text some text some text some text some text some text");
        descriptionLabel.setTextSize(15);
        descriptionLabel.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        descriptionLabel.setMaxLines(5);
        mainLayout.addView(descriptionLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 20, 20, 20));

        LinearLayout showPropsLayout = new LinearLayout(context);
        showPropsLayout.setGravity(Gravity.CENTER);
        ImageView showPropsIcon = new ImageView(context);
        Drawable showPropsDrawable = context.getResources().getDrawable(R.drawable.arrow_more);
        showPropsDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray), PorterDuff.Mode.MULTIPLY));
        showPropsIcon.setImageDrawable(showPropsDrawable);

        LinearLayout viewDetailsFrame = new LinearLayout(context) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 1.0f : 0.5f);
                setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(Theme.getColor(Theme.key_switchTrackBlueSelector)));
            }
        };
        viewDetailsFrame.setEnabled(true);
        viewDetailsFrame.setGravity(Gravity.CENTER);

        TextView viewDetailsLabel = new TextView(context);
        viewDetailsLabel.setText(LocaleController.getString("HtViewDetails", R.string.HtViewDetails));
        viewDetailsLabel.setTextColor(context.getResources().getColor(R.color.ht_green));
        viewDetailsLabel.setTextSize(15);
        viewDetailsLabel.setTypeface(viewDetailsLabel.getTypeface(), Typeface.BOLD);
        viewDetailsFrame.addView(showPropsIcon, LayoutHelper.createFrame(20, 20, Gravity.LEFT, 20, 20, 20, 20));
        viewDetailsFrame.addView(viewDetailsLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 20, 20, 20));
        showPropsLayout.addView(viewDetailsFrame, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.75f));

        msgTimeLabel = new TextView(context);
        msgTimeLabel.setText("Valid until\n23:45 01-01-2021");
        msgTimeLabel.setMinLines(2);
        msgTimeLabel.setTextSize(12);
        msgTimeLabel.setTextColor(Theme.getColor(Theme.key_dialogTextGray4));
        showPropsLayout.addView(msgTimeLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 15, 15, 20, 5));
        mainLayout.addView(showPropsLayout);
        mainLayout.addView(new DividerCell(context), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 5,20,5));

        LinearLayout expandableDetailsLayout = new LinearLayout(context);
        expandableDetailsLayout.setOrientation(LinearLayout.VERTICAL);
        addressLabel = new TextView(context);
        addressLabel.setText("No. 489, 13th Street, Yousefabad District, Tehran, Iran");
        addressLabel.setTextSize(15);
        addressLabel.setMaxLines(5);
        addressLabel.setTypeface(addressLabel.getTypeface(), Typeface.BOLD);
        addressLabel.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        Drawable addressDrawable = context.getResources().getDrawable(R.drawable.msg_location);
        addressDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        addressLabel.setCompoundDrawablePadding(AndroidUtilities.dp(4));
        addressLabel.setCompoundDrawablesWithIntrinsicBounds(addressDrawable, null, null, null);
        expandableDetailsLayout.addView(addressLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 20, 20, 20));

        LinearLayout midLayer = new LinearLayout(context);
        expireLabel = new TextView(context);
        expireLabel.setText("01-01-2021");
        expireLabel.setTextSize(14);
        expireLabel.setTypeface(expireLabel.getTypeface(), Typeface.BOLD);
        expireLabel.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));

        Drawable timeDrawable = context.getResources().getDrawable(R.drawable.msg_timer);
        timeDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        expireLabel.setCompoundDrawablePadding(AndroidUtilities.dp(4));
        expireLabel.setCompoundDrawablesWithIntrinsicBounds(timeDrawable, null, null, null);
        midLayer.addView(expireLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 30, 20, 0, 20));

        rateLabel = new TextView(context);
        rateLabel.setText("50$ Per 1 Lesson");
        rateLabel.setTextSize(14);
        rateLabel.setTypeface(expireLabel.getTypeface(), Typeface.BOLD);
        rateLabel.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        Drawable rateDrawable = context.getResources().getDrawable(R.drawable.offer);
        rateDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        rateLabel.setCompoundDrawablePadding(AndroidUtilities.dp(4));
        rateLabel.setCompoundDrawablesWithIntrinsicBounds(rateDrawable, null, null, null);
        midLayer.addView(rateLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 20, 0, 0));
        expandableDetailsLayout.addView(midLayer, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 0, 0, 0));

        configLabel = new TextView(context);
        configLabel.setText("50$ Per 1 Lesson");
        configLabel.setTextSize(14);
        configLabel.setTypeface(expireLabel.getTypeface(), Typeface.BOLD);
        configLabel.setTextColor(Theme.getColor(Theme.key_wallet_grayText2));
        expandableDetailsLayout.addView(configLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 0, 20, 20));


        TextView termsLinkText = new TextView(context);
        termsLinkText.setTextSize(14);
        termsLinkText.setTextColor(context.getResources().getColor(R.color.ht_green));
        termsLinkText.setText("Terms And Conditions Link");
        termsLinkText.setPaintFlags(termsLinkText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        expandableDetailsLayout.addView(termsLinkText, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 20, 0, 20));

        termsLinkText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                RelativeLayout mainLayoout = new RelativeLayout(context);
                TextView termsTitleText = new TextView(context);
                termsTitleText.setId(1);
                termsTitleText.setTextSize(18);
                termsTitleText.setTextColor(context.getResources().getColor(R.color.ht_green));
                termsTitleText.setText("Terms and Conditions");
                termsTitleText.setTypeface(termsTitleText.getTypeface(), Typeface.BOLD);
                RelativeLayout.LayoutParams termsTitleTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                termsTitleTextLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                termsTitleTextLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
                mainLayoout.addView(termsTitleText, termsTitleTextLayoutParams);

                TextView heymateTermsText = new TextView(context);
                heymateTermsText.setId(2);
                heymateTermsText.setTextSize(16);
                heymateTermsText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
                heymateTermsText.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n" +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
                RelativeLayout.LayoutParams heymateTermsTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                heymateTermsTextLayoutParams.addRule(RelativeLayout.BELOW, termsTitleText.getId());
                heymateTermsTextLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
                mainLayoout.addView(heymateTermsText, heymateTermsTextLayoutParams);

                TextView termsText = new TextView(context);
                termsText.setId(3);
                termsText.setTextSize(16);
                termsText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
                termsText.setText(terms);
                RelativeLayout.LayoutParams termsTextLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                termsTextLayoutParams.addRule(RelativeLayout.BELOW, heymateTermsText.getId());
                termsTextLayoutParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
                mainLayoout.addView(termsText, termsTextLayoutParams);

                builder.setView(mainLayoout);

                AlertDialog dialog = builder.create();
                parent.showDialog(dialog);
            }
        });


        LinearLayout bottomLayer = new LinearLayout(context);
        bottomLayer.setGravity(Gravity.CENTER);
        expandableDetailsLayout.addView(new DividerCell(context), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 60, 0, 0));
        mainLayout.addView(expandableDetailsLayout);
        showPropsIcon.setEnabled(true);
        expandableDetailsLayout.setVisibility(GONE);
        showPropsLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!showingDetails){
                    ObjectAnimator anim3 = ObjectAnimator.ofFloat(expandableDetailsLayout, "scaleY" , 0f, 1f);
                    anim3.setDuration(500);
                    anim3.start();
                    ObjectAnimator anim2 = ObjectAnimator.ofFloat(showPropsIcon, "rotation", 0, 180);
                    anim2.start();
                    ObjectAnimator anim4 = ObjectAnimator.ofFloat(expandableDetailsLayout, "alpha", 0f, 1f);
                    anim4.setDuration(750);
                    anim4.start();
                    TranslateAnimation anim1 = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, -1f,
                            Animation.RELATIVE_TO_SELF,0f);
                    anim1.setDuration(300);
                    expandableDetailsLayout.setVisibility(VISIBLE);

                    expandableDetailsLayout.startAnimation(anim1);
                } else {
                    expandableDetailsLayout.setVisibility(VISIBLE);
                    TranslateAnimation anim1 = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF,-1f);
                    anim1.setDuration(300);
                    expandableDetailsLayout.startAnimation(anim1);
                    ObjectAnimator anim3 = ObjectAnimator.ofFloat(expandableDetailsLayout, "scaleY" , 1f, 0f);
                    anim3.setDuration(500);
                    anim3.start();
                    ObjectAnimator anim4 = ObjectAnimator.ofFloat(expandableDetailsLayout, "alpha", 1f, 0f);
                    anim4.setDuration(750);
                    anim4.start();
                    anim1.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            expandableDetailsLayout.setVisibility(GONE);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    ObjectAnimator anim2 = ObjectAnimator.ofFloat(showPropsIcon, "rotation", 180, 0);
                    anim2.start();
                }
                showingDetails = !showingDetails;
            }
        });
        buyLayout = new LinearLayout(context) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 1.0f : 0.5f);
            }
        };
        buyLayout.setGravity(Gravity.CENTER);

        TextView buyLabel = new TextView(context);
        buyLabel.setText(LocaleController.getString("HtBuy", R.string.HtBuy));
        buyLabel.setTextSize(16);
        buyLayout.setBackgroundColor(context.getResources().getColor(R.color.ht_green));
        ShapeDrawable defaultDrawable3 = new ShapeDrawable(new RoundRectShape(new float[]{AndroidUtilities.dp(4), AndroidUtilities.dp(4), 0, 0, 0, 0, AndroidUtilities.dp(4), AndroidUtilities.dp(4)}, null, null));
        defaultDrawable3.getPaint().setColor(context.getResources().getColor(R.color.ht_green));
        buyLayout.setBackground(defaultDrawable3);
        buyLabel.setTypeface(buyLabel.getTypeface(), Typeface.BOLD);
        Drawable buyDrawable = context.getResources().getDrawable(R.drawable.pay);
        buyLabel.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        buyDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_wallet_whiteText), PorterDuff.Mode.MULTIPLY));
        buyLabel.setCompoundDrawablesWithIntrinsicBounds(buyDrawable, null, null, null);
        buyLabel.setCompoundDrawablePadding(AndroidUtilities.dp(6));
        buyLayout.addView(buyLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 12,6,12,6));
        buyLayout.setEnabled(true);
        buyLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HeymatePayment.initPayment(parent, offer.getId());
            }
        });
        bottomLayer.addView(buyLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 0.25f));
        promoteLayout = new LinearLayout(context) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 1.0f : 0.5f);
            }
        };

        promoteLayout.setGravity(Gravity.CENTER);
        TextView promoteLabel = new TextView(context);
        promoteLabel.setText(LocaleController.getString("HtPromote", R.string.HtPromote));
        promoteLabel.setTextSize(16);
        promoteLayout.setBackgroundColor(context.getResources().getColor(R.color.ht_green));
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{AndroidUtilities.dp(4), AndroidUtilities.dp(4), 0, 0, 0, 0, AndroidUtilities.dp(4), AndroidUtilities.dp(4)}, null, null));
        defaultDrawable.getPaint().setColor(context.getResources().getColor(R.color.ht_green));
        promoteLayout.setBackground(defaultDrawable);
        promoteLabel.setTypeface(promoteLabel.getTypeface(), Typeface.BOLD);
        Drawable promoteDrawable = context.getResources().getDrawable(R.drawable.share);
        promoteLabel.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        promoteDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_wallet_whiteText), PorterDuff.Mode.MULTIPLY));
        promoteLabel.setCompoundDrawablesWithIntrinsicBounds(promoteDrawable, null, null, null);
        promoteLabel.setCompoundDrawablePadding(AndroidUtilities.dp(6));
        promoteLayout.addView(promoteLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 12,6,12,6));
        promoteLayout.setEnabled(true);
        promoteLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        bottomLayer.addView(promoteLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 0.25f));
        promoteLayout.setVisibility(GONE);
        promoteLayout.setOnClickListener(v -> {
            try{
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");

                TLRPC.User user = UserConfig.getInstance(parent.getCurrentAccount()).getCurrentUser();
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
                String message = OfferUtils.serializeBeautiful(offer, name, OfferUtils.CATEGORY, OfferUtils.EXPIRY);
                share.putExtra(Intent.EXTRA_TEXT, message);
                context.startActivity(Intent.createChooser(share, LocaleController.getString("HtPromoteYourOffer", R.string.HtPromoteYourOffer)));

            } catch (Exception e){

            }
        });

        viewLayout = new LinearLayout(context) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 1.0f : 0.5f);
            }
        };
        viewLayout.setGravity(Gravity.CENTER);
        TextView viewLabel = new TextView(context);
        viewLabel.setText(LocaleController.getString("HtView", R.string.HtView));
        viewLabel.setTextSize(16);
        viewLayout.setBackgroundColor(Theme.getColor(Theme.key_statisticChartLine_blue));
        ShapeDrawable defaultDrawable2 = new ShapeDrawable(new RoundRectShape(new float[]{0, 0, AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4), 0, 0}, null, null));
        defaultDrawable2.getPaint().setColor(Theme.getColor(Theme.key_statisticChartLine_blue));
        viewLayout.setBackground(defaultDrawable2);
        viewLabel.setTypeface(viewLabel.getTypeface(), Typeface.BOLD);

        Drawable viewDrawable = context.getResources().getDrawable(R.drawable.msg_views);
        viewLabel.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        viewDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_wallet_whiteText), PorterDuff.Mode.MULTIPLY));
        viewLabel.setCompoundDrawablesWithIntrinsicBounds(viewDrawable, null, null, null);
        viewLabel.setCompoundDrawablePadding(AndroidUtilities.dp(6));
        viewLayout.addView(viewLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 12,6,12,6));
        viewLayout.setEnabled(true);
        viewLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        bottomLayer.addView(viewLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 0.25f));
        viewLayout.setEnabled(true);
        viewLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HtOfferDetailsPopUp detailsPopUp = new HtOfferDetailsPopUp(context, parent,  0, offerUUID);
                AlertDialog dialog = detailsPopUp.create();
                detailsPopUp.closeImage.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                parent.showDialog(dialog);
            }
        });

        LinearLayout forwardLayout = new LinearLayout(context);
        forwardLayout.setGravity(Gravity.CENTER);
        ImageView forwardImage = new ImageView(context);
        Drawable forwardDrawable = context.getResources().getDrawable(R.drawable.msg_forward);
        forwardDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        forwardImage.setImageDrawable(forwardDrawable);
        forwardLayout.addView(forwardImage);
        forwardLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<MessageObject> arrayList = new ArrayList<>();
                arrayList.add(message);
                if(parent instanceof ChatActivity) {
                    Bundle args = new Bundle();
                    args.putBoolean("onlySelect", true);
                    args.putInt("dialogsType", 3);
                    args.putInt("messagesCount", 1);
                    args.putInt("hasPoll", 0);
                    DialogsActivity fragment = new DialogsActivity(args);
                    fragment.setDelegate(new HtOfferChatDelegate(parent, message));
                    parent.presentFragment(fragment);
                }
            }
        });
        bottomLayer.addView(forwardLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT,LayoutHelper.WRAP_CONTENT));
        mainLayout.addView(bottomLayer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 40));
        mainLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_chat_topPanelBackground)));
        addView(mainLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 10, 20, 10));
    }

    public class HtOfferChatDelegate implements DialogsActivity.DialogsActivityDelegate {
        private BaseFragment parent;
        private MessageObject messageObject;

        public HtOfferChatDelegate(BaseFragment parent, MessageObject messageObject){
            this.parent = parent;
            this.messageObject = messageObject;
        }

        @Override
        public void didSelectDialogs(DialogsActivity fragment, ArrayList<Long> dids, CharSequence message, boolean param) {
            for(Long did : dids){
                SendMessagesHelper.getInstance(parent.getCurrentAccount()).processForwardFromMyName(messageObject, did);
            }
        }
    }

    public void setOut(Boolean out){
        this.out = out;
        if(out){
            buyLayout.setVisibility(GONE);
            promoteLayout.setVisibility(VISIBLE);
        }
    }

    public void setMessage(MessageObject message){
        this.message = message;
    }

    public void setParent(BaseFragment parent){
        this.parent = parent;
    }

    public void setMessageText(String messageText){
        this.messageText = messageText;
    }

    public void setStatus(OfferStatus status){
        this.status = status;
        if(status == OfferStatus.DRAFTED){
            editIcon.setVisibility(VISIBLE);
        }
    }

    public void setArchived(boolean archived){
        this.archived = archived;
        if(archived){
            statusLayout.setBackgroundColor(Theme.getColor(Theme.key_graySection));
            archiveDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_graySection), PorterDuff.Mode.MULTIPLY));
            archiveIcon.setOnClickListener(null);
            archiveIcon.setVisibility(GONE);
        }
    }

    public String getOfferUUID() {
        return offerUUID;
    }

    public void setOfferUUID(String offerUUID) {
        this.offerUUID = offerUUID;
        Bitmap imageBitmap = HtStorage.getInstance().getOfferImage(context, offerUUID);
        if(imageBitmap == null){
            image.setImageDrawable(context.getResources().getDrawable(R.drawable.np));
        } else {
            image.setImageBitmap(imageBitmap);
        }
        image.setRoundRadius(AndroidUtilities.dp(4));
    }
}

/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Heymate;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.ProfileActivity;

import works.heymate.core.offer.OfferUtils;

public class OfferCell extends FrameLayout {

    private Context context;
    private int place;
    private BaseFragment parentFragment;
    private OfferDto dto;
    private LinearLayout holderLayout;
    private LinearLayout mainLayout;
    private RelativeLayout detailLayout;
    private LinearLayout secondHolder;
    private ImageView infoIcon;
    private ImageView editIcon;
    private ImageView archiveIcon;
    private boolean showingOptions = false;
    private boolean duringShowingOptions = false;
    private boolean showingDetails = false;
    private boolean duringShowingDetails = false;
    private OfferDetailsLayout offerDetailsLayout;
    private ImageView slideImage;

    private int getOfferStatusColor(OfferStatus offerStatus){
        switch (offerStatus){
            case ACTIVE: return context.getResources().getColor(R.color.ht_green);
            case EXPIRED: return Theme.getColor(Theme.key_wallet_redText);
            default: return Theme.getColor(Theme.key_dialogGrayLine);
        }
    }

    public OfferCell(Context context, BaseFragment parentFragment) {
        super(context);
        this.place = place;
        this.context = context;
        this.parentFragment = parentFragment;
        this.dto = dto;
        holderLayout = new LinearLayout(context);
        holderLayout.setOrientation(LinearLayout.VERTICAL);
        detailLayout = new RelativeLayout(context);
        mainLayout = new LinearLayout(context);
        secondHolder = new LinearLayout(context);

        LinearLayout statusLayout = new LinearLayout(context);
        statusLayout.setBackgroundColor(getOfferStatusColor(dto.getStatus()));
        mainLayout.addView(statusLayout, LayoutHelper.createFrame(AndroidUtilities.dp(2), LayoutHelper.MATCH_PARENT, Gravity.LEFT, 0,0,15,0));

        LinearLayout titleLayout = new LinearLayout(context);
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        TextView offerTitle = new TextView(context);
        offerTitle.setText(dto.getTitle());
        offerTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

        Drawable offerTitleDrawable = context.getResources().getDrawable(R.drawable.menu_jobtitle);
        offerTitle.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        offerTitle.setTypeface(offerTitle.getTypeface(), Typeface.BOLD);
        offerTitleDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
        offerTitle.setCompoundDrawablesWithIntrinsicBounds(offerTitleDrawable, null, null, null);
        offerTitle.setCompoundDrawablePadding(AndroidUtilities.dp(10));
        offerTitle.setGravity(Gravity.CENTER_HORIZONTAL | RelativeLayout.CENTER_VERTICAL);
        titleLayout.addView(offerTitle, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        TextView offerRate = new TextView(context);
        offerRate.setText(dto.getRate() + dto.getCurrency());
        offerRate.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
        offerRate.setGravity(Gravity.CENTER);
        offerRate.setTypeface(offerRate.getTypeface(), Typeface.BOLD);
        titleLayout.addView(offerRate, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 40, 0, 0, 0));

        TextView offerStatus = new TextView(context);
        offerStatus.setText(dto.getStatus().toString());
        offerStatus.setTextColor(getOfferStatusColor(dto.getStatus()));
        offerStatus.setGravity(Gravity.CENTER);
        offerStatus.setTypeface(offerRate.getTypeface(), Typeface.BOLD);
        titleLayout.addView(offerStatus, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 40, 0, 0, 0));

        slideImage = new ImageView(context);
        Drawable slideDrawable = context.getResources().getDrawable(R.drawable.arrow_more);
        slideDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray), PorterDuff.Mode.MULTIPLY));
        slideImage.setImageDrawable(slideDrawable);
        slideImage.setRotation(270);
        titleLayout.addView(slideImage, LayoutHelper.createLinear(17,17));

        if(place == 2){
            ImageView galleryImage = new ImageView(context) {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    setAlpha(enabled ? 1.0f : 0.5f);
                }
            };
            Drawable galleryDrawable = context.getResources().getDrawable(R.drawable.msg_gallery);
            galleryDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_avatar_subtitleInProfileBlue), PorterDuff.Mode.MULTIPLY));
            galleryImage.setImageDrawable(galleryDrawable);
            galleryImage.setEnabled(true);
            galleryImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(parentFragment instanceof ProfileActivity){
                        OfferGalleryActivity fragment = new OfferGalleryActivity(context);
                        ((ProfileActivity) parentFragment).presentFragment(fragment);
                        fragment.setDto(dto);
                    }
                }
            });
            titleLayout.addView(galleryImage, LayoutHelper.createFrame(30,30));
        }

        LinearLayout infoLayout = new LinearLayout(context);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(titleLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        TextView offerTime = new TextView(context);
        offerTime.setText(dto.getTime());
        offerTime.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
        offerTime.setGravity(Gravity.CENTER);
        offerTime.setTypeface(offerTime.getTypeface(), Typeface.BOLD);
        offerTime.setMinLines(2);
        offerTime.setMaxLines(3);

        Drawable offerTimeDrawable = context.getResources().getDrawable(R.drawable.msg_timer);
        Bitmap b = ((BitmapDrawable) offerTimeDrawable).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 60, 60, false);
        BitmapDrawable gdrawable = new BitmapDrawable(context.getResources(), bitmapResized);
        gdrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray), PorterDuff.Mode.MULTIPLY));
        offerTime.setCompoundDrawablesWithIntrinsicBounds(gdrawable, null, null, null);
        offerTime.setCompoundDrawablePadding(AndroidUtilities.dp(4));
        infoLayout.addView(offerTime, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 20, 0, 0, 20));

        TextView offerLocation = new TextView(context);
        offerLocation.setText("2KM");
        offerLocation.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
        offerLocation.setGravity(Gravity.CENTER);
        offerLocation.setTypeface(offerLocation.getTypeface(), Typeface.BOLD);

        Drawable offerLocationDrawable = context.getResources().getDrawable(R.drawable.msg_location);
        Bitmap b4 = ((BitmapDrawable) offerLocationDrawable).getBitmap();
        Bitmap bitmapResized4 = Bitmap.createScaledBitmap(b4, 60, 60, false);
        BitmapDrawable gdrawable4 = new BitmapDrawable(context.getResources(), bitmapResized4);
        gdrawable4.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray), PorterDuff.Mode.MULTIPLY));
        offerLocation.setCompoundDrawablesWithIntrinsicBounds(gdrawable4, null, null, null);
        offerLocation.setCompoundDrawablePadding(AndroidUtilities.dp(4));
        infoLayout.addView(offerLocation, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 20, 0, 0, 20));
        mainLayout.addView(infoLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 40, 0, 0, 0));
        detailLayout.addView(mainLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT, 20,0,0,0));
        holderLayout.addView(detailLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER, 0,0,0,0));
        holderLayout.addView(secondHolder, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER, 0,0,0,0));
        holderLayout.addView(new DividerCell(context), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER, 40,0,40,0));
        addView(holderLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.START, 0, 40, 0, 0));
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setEnabled(isEnabled());
    }

    public void onClick() {
        if (showingOptions)
            hideOptions();
        else
            showOptions();
    }

    private void showOptions() {
        if(duringShowingOptions)
            return;
        duringShowingOptions = true;
        ObjectAnimator slideAnim = ObjectAnimator.ofFloat(slideImage, "rotation", 90);
        slideAnim.setDuration(300);
        slideAnim.start();
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(mainLayout, "x", place == 1 ? AndroidUtilities.dp(180) : AndroidUtilities.dp(70));
        anim1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim1.setDuration(place == 1 ? 600 : 200);
        anim1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                infoIcon = new ImageView(context);
                RotateAnimation rotate = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF,
                        0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(600);
                infoIcon.startAnimation(rotate);
                Animation colorAnim = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        infoIcon.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY));
                    }
                };
                infoIcon.startAnimation(colorAnim);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showingOptions = true;
                duringShowingOptions = false;
                Drawable infoIconDrawable = context.getResources().getDrawable(R.drawable.menu_info);
                infoIconDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue), PorterDuff.Mode.MULTIPLY));
                infoIcon.setImageDrawable(infoIconDrawable);
                infoIcon.setMaxWidth(AndroidUtilities.dp(25));
                infoIcon.setMaxHeight(AndroidUtilities.dp(25));
                infoIcon.setEnabled(true);
                infoIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ScaleAnimation anim3 = new ScaleAnimation(1f, 1.2f, 1f, 1.2f);
                        anim3.setDuration(150);
                        infoIcon.startAnimation(anim3);
                        if(showingDetails){
                            hideDetails();
                        } else {
                            showDetails();
                        }
                        showingDetails = !showingDetails;
                    }
                });
                detailLayout.addView(infoIcon, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 0, 0, 0));

                ObjectAnimator anim2 = ObjectAnimator.ofFloat(infoIcon, View.ALPHA, 0, 1);
                anim2.setDuration(350);
                anim2.start();

                editIcon = new ImageView(context);
                if(place == 2)
                    editIcon.setVisibility(GONE);

                Drawable editIconDrawable = context.getResources().getDrawable(R.drawable.msg_edit);
                editIconDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue), PorterDuff.Mode.MULTIPLY));
                editIcon.setImageDrawable(editIconDrawable);
                editIcon.setMaxWidth(AndroidUtilities.dp(25));
                editIcon.setMaxHeight(AndroidUtilities.dp(25));
                editIcon.setEnabled(true);
                editIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ScaleAnimation anim3 = new ScaleAnimation(1f, 1.2f, 1f, 1.2f);
                        anim3.setDuration(150);
                        editIcon.startAnimation(anim3);
                    }
                });
                detailLayout.addView(editIcon, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 70, 0, 0, 0));

                ObjectAnimator anim4 = ObjectAnimator.ofFloat(editIcon, View.ALPHA, 0, 1);
                anim4.setDuration(350);
                anim4.start();

                archiveIcon = new ImageView(context);
                if(place == 2)
                    archiveIcon.setVisibility(GONE);

                Drawable archiveIconDrawable = context.getResources().getDrawable(R.drawable.share);
                archiveIconDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY));
                archiveIcon.setImageDrawable(archiveIconDrawable);
                archiveIcon.setMaxWidth(AndroidUtilities.dp(25));
                archiveIcon.setMaxHeight(AndroidUtilities.dp(25));
                archiveIcon.setEnabled(true);
                if(dto.getStatus() == OfferStatus.ARCHIVED)
                    archiveIcon.setVisibility(GONE);
                archiveIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ScaleAnimation anim3 = new ScaleAnimation(1f, 1.2f, 1f, 1.2f);
                        anim3.setDuration(150);
                        archiveIcon.startAnimation(anim3);
                        ObjectAnimator anim5 = ObjectAnimator.ofFloat(mainLayout, "x", AndroidUtilities.dp(400));
                        anim5.setDuration(600);

                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("text/plain");

                        TLRPC.User user = UserConfig.getInstance(parentFragment.getCurrentAccount()).getCurrentUser();
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
                        String message = OfferUtils.serializeBeautiful(dto.asOffer(), name, OfferUtils.CATEGORY, OfferUtils.EXPIRY);
                        share.putExtra(Intent.EXTRA_TEXT, message);
                        context.startActivity(Intent.createChooser(share, LocaleController.getString("HtPromoteYourOffer", R.string.HtPromoteYourOffer)));

                        anim5.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {

                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        anim5.start();
                    }
                });
                detailLayout.addView(archiveIcon, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 120, 0, 0, 0));
                ObjectAnimator anim5 = ObjectAnimator.ofFloat(archiveIcon, View.ALPHA, 0, 1);
                anim5.setDuration(350);
                anim5.start();
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

    private void hideOptions() {
        if(duringShowingOptions)
            return;
        duringShowingOptions = true;
        hideDetails();
        ObjectAnimator slideAnim = ObjectAnimator.ofFloat(slideImage, "rotation", 270);
        slideAnim.setDuration(300);
        slideAnim.start();

        ObjectAnimator anim5 = ObjectAnimator.ofFloat(infoIcon, View.ALPHA, 1, 0);
        anim5.setDuration(350);
        anim5.start();

        ObjectAnimator anim6 = ObjectAnimator.ofFloat(editIcon, View.ALPHA, 1, 0);
        anim6.setDuration(350);
        anim6.start();

        ObjectAnimator anim7 = ObjectAnimator.ofFloat(archiveIcon, View.ALPHA, 1, 0);
        anim7.setDuration(350);
        anim7.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator anim1 = ObjectAnimator.ofFloat(mainLayout, "x", AndroidUtilities.dp(20));
                anim1.setInterpolator(new AccelerateDecelerateInterpolator());
                anim1.setDuration(600);
                anim1.start();
                showingOptions = false;
                duringShowingOptions = false;
                detailLayout.removeView(infoIcon);
                detailLayout.removeView(editIcon);
                detailLayout.removeView(archiveIcon);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim7.start();
    }

    private void showDetails(){
        if(duringShowingDetails)
            return;
        duringShowingDetails = true;
        offerDetailsLayout = new OfferDetailsLayout(context, dto);
        secondHolder.addView(offerDetailsLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0,0,0,0));
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(offerDetailsLayout, "scaleX", 0, 1);
        anim1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                duringShowingDetails = false;
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

    private void hideDetails(){
        if(duringShowingDetails)
            return;
        duringShowingDetails = true;
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(offerDetailsLayout, "scaleX", 1, 0);
        anim1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                secondHolder.removeView(offerDetailsLayout);
                duringShowingDetails = false;
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



    private class OfferDetailsLayout extends LinearLayout {
        private Context context;

        public OfferDetailsLayout(Context context, OfferDto dto) {
            super(context);
            this.context = context;
            this.setOrientation(VERTICAL);
            LinearLayout titleLayout = new LinearLayout(context);
            titleLayout.setOrientation(VERTICAL);

            TextView titleLabel = new TextView(context);
            titleLabel.setText(dto.getTitle());
            titleLabel.setTextColor(context.getResources().getColor(R.color.ht_green));
            titleLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            titleLabel.setTextSize(16);
            titleLayout.addView(titleLabel, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));

            LinearLayout rateLayout = new LinearLayout(context);
            TextView rateLabel = new TextView(context);
            rateLabel.setText(""+ dto.getRate());
            rateLabel.setTextColor(Theme.getColor(Theme.key_avatar_backgroundOrange));
            rateLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            rateLabel.setTextSize(16);
            rateLayout.addView(rateLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));

            TextView currencyLabel = new TextView(context);
            currencyLabel.setText(dto.getCurrency());
            currencyLabel.setTextColor(Theme.getColor(Theme.key_avatar_backgroundOrange));
            currencyLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            currencyLabel.setTextSize(16);
            rateLayout.addView(currencyLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));

            TextView rateTypeLabel = new TextView(context);
            rateTypeLabel.setText("" + dto.getRateType());
            rateTypeLabel.setTextColor(Theme.getColor(Theme.key_avatar_backgroundOrange));
            rateTypeLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            rateTypeLabel.setTextSize(16);
            rateLayout.addView(rateTypeLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));
            LinearLayout locationLayout = new LinearLayout(context);

            TextView locationLabel = new TextView(context);
            Drawable locationDrawable = context.getResources().getDrawable(R.drawable.menu_location);
            locationDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_avatar_backgroundCyan), PorterDuff.Mode.MULTIPLY));
            locationLabel.setCompoundDrawablePadding(AndroidUtilities.dp(4));
            locationLabel.setCompoundDrawablesWithIntrinsicBounds(locationDrawable, null, null, null);
            locationLabel.setText(dto.getLocation());
            locationLabel.setTextColor(Theme.getColor(Theme.key_avatar_backgroundCyan));
            locationLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            locationLabel.setTextSize(16);
            locationLabel.setMaxLines(5);
            locationLayout.addView(locationLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));

            LinearLayout timeLayout = new LinearLayout(context);
            TextView timeLabel = new TextView(context);

            Drawable timeDrawable = context.getResources().getDrawable(R.drawable.msg_timer);
            timeDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_avatar_backgroundViolet), PorterDuff.Mode.MULTIPLY));
            timeLabel.setCompoundDrawablePadding(AndroidUtilities.dp(4));
            timeLabel.setCompoundDrawablesWithIntrinsicBounds(timeDrawable, null, null, null);
            timeLabel.setText(dto.getTime());
            timeLabel.setTextColor(Theme.getColor(Theme.key_avatar_backgroundViolet));
            timeLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            timeLabel.setTextSize(16);
            timeLayout.addView(timeLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));
            LinearLayout categoryLayout = new LinearLayout(context);

            TextView categoryLabel = new TextView(context);
            categoryLabel.setText(dto.getCategory());
            categoryLabel.setTextColor(Theme.getColor(Theme.key_avatar_backgroundSaved));
            categoryLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            categoryLabel.setTextSize(16);
            categoryLayout.addView(categoryLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));

            TextView subCategoryLabel = new TextView(context);
            subCategoryLabel.setText(dto.getSubCategory());
            subCategoryLabel.setTextColor(Theme.getColor(Theme.key_avatar_nameInMessageRed));
            subCategoryLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            subCategoryLabel.setTextSize(16);
            categoryLayout.addView(subCategoryLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));

            LinearLayout statusLayout = new LinearLayout(context);
            TextView statusLabel = new TextView(context);
            statusLabel.setText(dto.getStatus().toString());
            statusLabel.setTextColor(getOfferStatusColor(dto.getStatus()));
            statusLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            statusLabel.setTextSize(16);
            statusLayout.addView(statusLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));

            this.addView(new HtDividerCell(context), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            this.addView(titleLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            this.addView(rateLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            this.addView(locationLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            this.addView(timeLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            this.addView(categoryLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            this.addView(statusLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            this.addView(new HtDividerCell(context), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        }
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public void setDto(OfferDto dto) {
        this.dto = dto;
    }
}

/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Heymate;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
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
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

public class OfferCell extends FrameLayout {

    private Context context;
    private OffersActivity parentFragment;
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

    public OfferCell(Context context, OffersActivity parentFragment, OfferDto dto) {
        super(context);
        this.context = context;
        this.parentFragment = parentFragment;
        this.dto = dto;
        holderLayout = new LinearLayout(context);
        holderLayout.setOrientation(LinearLayout.VERTICAL);
        detailLayout = new RelativeLayout(context);
        mainLayout = new LinearLayout(context);
        secondHolder = new LinearLayout(context);
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
        mainLayout.addView(offerTitle, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        TextView offerRate = new TextView(context);
        offerRate.setText(dto.getRate() + dto.getCurrency());
        offerRate.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
        offerRate.setGravity(Gravity.CENTER);
        offerRate.setTypeface(offerRate.getTypeface(), Typeface.BOLD);
        LinearLayout infoLayout = new LinearLayout(context);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        TextView offerTime = new TextView(context);
        offerTime.setText(dto.getTime());
        offerTime.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
        offerTime.setGravity(Gravity.CENTER);
        offerTime.setTypeface(offerRate.getTypeface(), Typeface.BOLD);
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
        offerLocation.setTypeface(offerRate.getTypeface(), Typeface.BOLD);
        Drawable offerLocationDrawable = context.getResources().getDrawable(R.drawable.msg_location);
        Bitmap b4 = ((BitmapDrawable) offerLocationDrawable).getBitmap();
        Bitmap bitmapResized4 = Bitmap.createScaledBitmap(b4, 60, 60, false);
        BitmapDrawable gdrawable4 = new BitmapDrawable(context.getResources(), bitmapResized4);
        gdrawable4.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray), PorterDuff.Mode.MULTIPLY));
        offerLocation.setCompoundDrawablesWithIntrinsicBounds(gdrawable4, null, null, null);
        offerLocation.setCompoundDrawablePadding(AndroidUtilities.dp(4));
        infoLayout.addView(offerLocation, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 20, 0, 0, 20));
        mainLayout.addView(offerRate, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 40, 0, 0, 0));
        mainLayout.addView(infoLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 40, 0, 0, 0));
        detailLayout.addView(mainLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT, 40,0,0,0));
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
        OfferCell father = this;
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(mainLayout, "x", AndroidUtilities.dp(180));
        anim1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim1.setDuration(600);
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
                        infoIcon.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_inGreenCall), PorterDuff.Mode.MULTIPLY));
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
                Drawable archiveIconDrawable = context.getResources().getDrawable(R.drawable.chats_archive);
                archiveIconDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogRedIcon), PorterDuff.Mode.MULTIPLY));
                archiveIcon.setImageDrawable(archiveIconDrawable);
                archiveIcon.setMaxWidth(AndroidUtilities.dp(25));
                archiveIcon.setMaxHeight(AndroidUtilities.dp(25));
                archiveIcon.setEnabled(true);
                archiveIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ScaleAnimation anim3 = new ScaleAnimation(1f, 1.2f, 1f, 1.2f);
                        anim3.setDuration(150);
                        archiveIcon.startAnimation(anim3);
                        ObjectAnimator anim5 = ObjectAnimator.ofFloat(mainLayout, "x", AndroidUtilities.dp(400));
                        anim5.setDuration(600);
                        anim5.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                parentFragment.getOffersLayout().removeView(father);
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
                ObjectAnimator anim1 = ObjectAnimator.ofFloat(mainLayout, "x", AndroidUtilities.dp(40));
                anim1.setInterpolator(new AccelerateDecelerateInterpolator());
                anim1.setDuration(600);
                anim1.start();
                showingOptions = false;
                duringShowingOptions = false;
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
        duringShowingOptions = true;
        offerDetailsLayout = new OfferDetailsLayout(context, dto);
        secondHolder.addView(offerDetailsLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0,0,0,0));
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(offerDetailsLayout, "scaleX", 0, 1);
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
            titleLabel.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
            titleLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            titleLabel.setTextSize(16);
            titleLayout.addView(titleLabel, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));
            LinearLayout rateLayout = new LinearLayout(context);
            TextView rateLabel = new TextView(context);
            rateLabel.setText(""+ dto.getRate());
            rateLabel.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
            rateLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            rateLabel.setTextSize(16);
            rateLayout.addView(rateLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));
            TextView currencyLabel = new TextView(context);
            currencyLabel.setText(dto.getCurrency());
            currencyLabel.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
            currencyLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            currencyLabel.setTextSize(16);
            rateLayout.addView(currencyLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));
            TextView rateTypeLabel = new TextView(context);
            rateTypeLabel.setText("" + dto.getRateType());
            rateTypeLabel.setTextColor(Theme.getColor(Theme.key_wallet_redText));
            rateTypeLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            rateTypeLabel.setTextSize(16);
            rateLayout.addView(rateTypeLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));
            LinearLayout locationLayout = new LinearLayout(context);
            TextView locationLabel = new TextView(context);
            Drawable locationDrawable = context.getResources().getDrawable(R.drawable.menu_location);
            locationDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_inGreenCall), PorterDuff.Mode.MULTIPLY));
            locationLabel.setCompoundDrawablePadding(AndroidUtilities.dp(4));
            locationLabel.setCompoundDrawablesWithIntrinsicBounds(locationDrawable, null, null, null);
            locationLabel.setText(dto.getLocation());
            locationLabel.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
            locationLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            locationLabel.setTextSize(16);
            locationLabel.setMaxLines(5);
            locationLayout.addView(locationLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));
            LinearLayout timeLayout = new LinearLayout(context);
            TextView timeLabel = new TextView(context);
            Drawable timeDrawable = context.getResources().getDrawable(R.drawable.msg_timer);
            timeDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_inGreenCall), PorterDuff.Mode.MULTIPLY));
            timeLabel.setCompoundDrawablePadding(AndroidUtilities.dp(4));
            timeLabel.setCompoundDrawablesWithIntrinsicBounds(timeDrawable, null, null, null);
            timeLabel.setText(dto.getTime());
            timeLabel.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
            timeLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
            timeLabel.setTextSize(16);
            timeLayout.addView(timeLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 40, 20, 0, 20));

            this.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
            this.addView(titleLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            this.addView(rateLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            this.addView(locationLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            this.addView(timeLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
    }
}

package org.telegram.ui.Heymate.createoffer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.HashMap;

import works.heymate.core.Texts;

public class HtTermsInputCell extends LinearLayout {

    private HashMap<String, Object> paremetersValues;
    private TextView[] parametersViews;
    private Drawable iconValue;

    public HtTermsInputCell(Context context, HtCreateOfferActivity parent, String title, HashMap<String, Runnable> args, int icon, boolean canEdit) {
        super(context);
        paremetersValues = new HashMap<>();
        parametersViews = new TextView[args.size()];
        setMinimumHeight(500);

        LinearLayout titleLayout = new LinearLayout(context);

        ImageView titleImage = new ImageView(context);
        iconValue = AppCompatResources.getDrawable(context, icon);
        iconValue.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4), PorterDuff.Mode.MULTIPLY));
        titleImage.setImageDrawable(iconValue);
        titleLayout.addView(titleImage, LayoutHelper.createLinear(20, 20, AndroidUtilities.dp(9), AndroidUtilities.dp(4), 15, 15));

        LinearLayout titleLayout2 = new LinearLayout(context);
        titleLayout2.setOrientation(LinearLayout.VERTICAL);
        LinearLayout titleLayout3 = new LinearLayout(context);

        TextView titleLabel = new TextView(context);
        titleLabel.setText(title);
        titleLabel.setTextSize(16);
        titleLabel.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleLabel.setPadding(15, 0, 0, 0);
        titleLayout3.addView(titleLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 1f, AndroidUtilities.dp(9), AndroidUtilities.dp(4), 0, 15));

        ImageView expandIcon = new ImageView(context);
        Drawable expandDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.arrow_more);
        expandDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4), PorterDuff.Mode.MULTIPLY));
        expandIcon.setImageDrawable(expandDrawable);
        titleLayout3.addView(expandIcon, LayoutHelper.createLinear(15, 15, AndroidUtilities.dp(20), AndroidUtilities.dp(4), 30, 15));
        titleLayout2.addView(titleLayout3);

        LinearLayout categoryLayout = new LinearLayout(context);
        categoryLayout.setOrientation(VERTICAL);
        expandIcon.setEnabled(true);
        expandIcon.setHovered(true);
        final boolean[] isOpen = {false};
        titleLayout3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.titleTextField.clearFocus();
                parent.titleTextField.hideActionMode();
                AndroidUtilities.hideKeyboard(parent.titleTextField);
                parent.descriptionTextField.clearFocus();
                parent.descriptionTextField.hideActionMode();
                AndroidUtilities.hideKeyboard(parent.descriptionTextField);

                if (!isOpen[0]) {
                    ObjectAnimator anim1 = ObjectAnimator.ofFloat(categoryLayout, "scaleY", 0f, 1f);
                    anim1.setDuration(250);
                    anim1.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            ObjectAnimator anim2 = ObjectAnimator.ofFloat(categoryLayout, "alpha", 0f, 1f);
                            anim2.setDuration(500);
                            anim2.start();
                            categoryLayout.setVisibility(VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            isOpen[0] = true;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    anim1.start();
                    ObjectAnimator anim3 = ObjectAnimator.ofFloat(expandIcon, "rotationX", 0, 180);
                    anim3.setDuration(250);
                    anim3.start();
                } else {
                    ObjectAnimator anim1 = ObjectAnimator.ofFloat(categoryLayout, "scaleY", 1f, 0f);
                    anim1.setDuration(250);
                    anim1.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            ObjectAnimator anim2 = ObjectAnimator.ofFloat(categoryLayout, "alpha", 1f, 0f);
                            anim2.setDuration(500);
                            anim2.start();
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            isOpen[0] = false;
                            categoryLayout.setVisibility(GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    anim1.start();
                    ObjectAnimator anim3 = ObjectAnimator.ofFloat(expandIcon, "rotationX", 180, 0);
                    anim3.setDuration(250);
                    anim3.start();
                }
            }
        });
        int i = 0;

        LinearLayout selectedTermsLayout = new LinearLayout(context);
        selectedTermsLayout.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground));
        selectedTermsLayout.setGravity(Gravity.CENTER);

        TextView heymateTerms = new TextView(context);
        heymateTerms.setText(Texts.get(Texts.CREATE_OFFER_HEYMATE_TERMS));
        heymateTerms.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        heymateTerms.setPaintFlags(heymateTerms.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        heymateTerms.setTypeface(heymateTerms.getTypeface(), Typeface.BOLD);
        selectedTermsLayout.addView(heymateTerms, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 10, 5, 10, 5));
        categoryLayout.addView(selectedTermsLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 25, 25, 0));

        for (Object arg : args.keySet().stream().sorted().toArray()) {
            LinearLayout parametersLayout = new LinearLayout(context);
            parametersLayout.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground));
            parametersLayout.setGravity(Gravity.CENTER);
            parametersViews[i] = new TextView(context);
            parametersViews[i].setText(((String) arg).substring(2));
            parametersViews[i].setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
//            parametersViews[i].setPaintFlags(parametersViews[i].getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            parametersViews[i].setTypeface(parametersViews[i].getTypeface(), Typeface.BOLD);
            parametersLayout.addView(parametersViews[i], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 10, 5, 10, 5));

            if (canEdit) {
                titleLayout3.setEnabled(true);
                titleLayout3.setHovered(true);
                parametersLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        args.get(arg).run();
                    }
                });
            }
            categoryLayout.addView(parametersLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 25, 25, 0));
            i++;
        }
        titleLayout2.addView(categoryLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 20));
        categoryLayout.setVisibility(GONE);
        titleLayout2.addView(new Divider(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 15, 0, 0));
        titleLayout.addView(titleLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 20));
        addView(titleLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

    }

    public void setRes(String arg, Object value, int position) {
        paremetersValues.put(arg, value);
        parametersViews[position].setText(value.toString());
        parametersViews[position].setTextColor(getContext().getResources().getColor(works.heymate.beta.R.color.ht_green));
    }

    public String getRes(String arg) {
        return (String) paremetersValues.get(arg);
    }


    public void setError(boolean error, int position) {
        if (error)
            iconValue.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_inRedCall), PorterDuff.Mode.MULTIPLY));
        else
            iconValue.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue2), PorterDuff.Mode.MULTIPLY));
    }
}

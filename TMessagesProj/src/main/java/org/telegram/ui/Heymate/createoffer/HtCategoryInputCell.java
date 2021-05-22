package org.telegram.ui.Heymate.createoffer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.HashMap;

public class HtCategoryInputCell extends LinearLayout {

    private String title;
    private HashMap<String, Object> parameterValues;
    private TextView[] parameterViews;
    private Drawable iconValue;
    private HashMap<String, Runnable> args;

    public HtCategoryInputCell(Context context, HtCreateOfferActivity parent, String title, HashMap<String, Runnable> args, int icon, boolean canEdit) {
        super(context);
        parameterValues = new HashMap<>();
        parameterViews = new TextView[args.size()];
        this.args = args;
        this.title = title;

        LinearLayout titleLayout = new LinearLayout(context);
        ImageView titleImage = new ImageView(context);
        iconValue = context.getResources().getDrawable(icon);
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

                if(!isOpen[0]){
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
        for (Object arg : args.keySet().stream().sorted().toArray()) {
            LinearLayout outerLayout = new LinearLayout(context);
            outerLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(5), context.getResources().getColor(works.heymate.beta.R.color.ht_green)));
            outerLayout.setGravity(Gravity.CENTER);

            LinearLayout parametersLayout = new LinearLayout(context);
            parametersLayout.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground));
            parametersLayout.setGravity(Gravity.CENTER);

            parameterViews[i] = new TextView(context);
            parameterViews[i].setText(((String) arg).substring(2));
            parameterViews[i].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            parameterViews[i].setTypeface(parameterViews[i].getTypeface(), Typeface.BOLD);
            parametersLayout.addView(parameterViews[i], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 10,5,10,5));
            outerLayout.addView(parametersLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 2, 2, 2, 2));

            if(canEdit) {
                titleLayout3.setEnabled(true);
                titleLayout3.setHovered(true);
                parametersLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        args.get(arg).run();
                    }
                });
            }
            categoryLayout.addView(outerLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0,25,25,0));
            i++;
        }
        titleLayout2.addView(categoryLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        categoryLayout.setVisibility(GONE);
        titleLayout2.addView(new HtDividerCell(context, true), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0,15,0,0));
        titleLayout.addView(titleLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addView(titleLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

    }

    public void setRes(String arg, Object value, int position) {
        parameterValues.put(arg, value);
        parameterViews[position].setText(value.toString());
        parameterViews[position].setTextColor(getContext().getResources().getColor(works.heymate.beta.R.color.ht_green));
    }

    public String getRes(String arg){
        return (String) parameterValues.get(arg);
    }


    public void setError(boolean error, int position){
        if(error)
            iconValue.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_inRedCall), PorterDuff.Mode.MULTIPLY));
        else
            iconValue.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue2), PorterDuff.Mode.MULTIPLY));
    }
}

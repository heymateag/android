package org.telegram.ui.Heymate.createoffer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

import works.heymate.core.offer.OfferUtils;

public class HtPaymentConfigInputCell extends LinearLayout {

    private static final int DELAY_TIME = 0;
    private static final int DELAY_PERCENT = 1;
    private static final int INITIAL_DEPOSIT = 2;
    private static final int CANCEL_HOURS1 = 3;
    private static final int CANCEL_PERCENT1 = 4;
    private static final int CANCEL_HOURS2 = 5;
    private static final int CANCEL_PERCENT2 = 6;

    private final TextView[] parametersViews;

    private int delayMinutes;
    private int delayPercent;
    private int initialDeposit;
    private int cancelHours1;
    private int cancelPercent1;
    private int cancelHours2;
    private int cancelPercent2;

    public HtPaymentConfigInputCell(Context context, String title, int icon, HtCreateOfferActivity parent, HtCreateOfferActivity.ActionType actionType) {
        super(context);
        LinearLayout paymentLayout = new LinearLayout(context);
        paymentLayout.setOrientation(VERTICAL);
        parametersViews = new TextView[7];

        LinearLayout titleLayout = new LinearLayout(context);
        ImageView titleImage = new ImageView(context);
        Drawable titleDrawable = context.getResources().getDrawable(icon);
        titleDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4), PorterDuff.Mode.MULTIPLY));
        titleImage.setImageDrawable(titleDrawable);
        titleLayout.addView(titleImage, LayoutHelper.createLinear(20, 20, AndroidUtilities.dp(9), AndroidUtilities.dp(4), 15, 15));

        LinearLayout titleLayout2 = new LinearLayout(context) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 0.5f : 1f);
            }
        };
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
        titleLayout2.addView(titleLayout3);
        titleLayout.addView(titleLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        paymentLayout.addView(titleLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout paymentLayout2 = new LinearLayout(context) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 0.5f : 1f);
            }
        };

        paymentLayout2.setOrientation(LinearLayout.VERTICAL);
        TextView configTitle1 = new TextView(context);
        configTitle1.setText(LocaleController.getString("HtServiceProviderPromises", works.heymate.beta.R.string.HtServiceProviderPromises));
        configTitle1.setTextColor(Theme.getColor(Theme.key_dialogTextBlue2));
        configTitle1.setTypeface(configTitle1.getTypeface(), Typeface.BOLD);
        paymentLayout2.addView(configTitle1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 25, 0, 0, 0));

        LinearLayout configLayout1 = new LinearLayout(context);
        parametersViews[DELAY_TIME] = new TextView(context);
        parametersViews[DELAY_TIME].setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        parametersViews[DELAY_TIME].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(LocaleController.getString("HtDelayTime", works.heymate.beta.R.string.HtDelayTime));
                LinearLayout mainLayout = new LinearLayout(context);
                EditTextBoldCursor feeTextField = new EditTextBoldCursor(context);
                feeTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                feeTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
                feeTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
                feeTextField.setMaxLines(4);
                feeTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
                feeTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                feeTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                feeTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                feeTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
                feeTextField.setMinHeight(AndroidUtilities.dp(36));
                feeTextField.setHint(LocaleController.getString("HtAmount", works.heymate.beta.R.string.HtAmount));
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton(LocaleController.getString("HtApply", works.heymate.beta.R.string.HtApply), (dialog, which) -> {
                    try {
                        delayMinutes = Integer.parseInt(feeTextField.getText().toString());
                    } catch (Throwable t) {
                        delayMinutes = 0;
                    }
                    updateValues();
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout1.addView(parametersViews[DELAY_TIME], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 35, 15, 0, 0));

        parametersViews[DELAY_PERCENT] = new TextView(context);
        parametersViews[DELAY_PERCENT].setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        parametersViews[DELAY_PERCENT].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(LocaleController.getString("HtPercentage", works.heymate.beta.R.string.HtPercentage));
                LinearLayout mainLayout = new LinearLayout(context);
                EditTextBoldCursor feeTextField = new EditTextBoldCursor(context);
                feeTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                feeTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
                feeTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
                feeTextField.setMaxLines(4);
                feeTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
                feeTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                feeTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                feeTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                feeTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
                feeTextField.setMinHeight(AndroidUtilities.dp(36));
                feeTextField.setHint(LocaleController.getString("HtAmount", works.heymate.beta.R.string.HtAmount));
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton(LocaleController.getString("HtApply", works.heymate.beta.R.string.HtApply), (dialog, which) -> {
                    try {
                        delayPercent = Integer.parseInt(feeTextField.getText().toString());
                    } catch (Throwable t) {
                        delayPercent = 0;
                    }
                    updateValues();
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout1.addView(parametersViews[DELAY_PERCENT], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 15, 0, 0));
        paymentLayout2.addView(configLayout1);

        TextView configTitle2 = new TextView(context);
        configTitle2.setText(LocaleController.getString("HtAdvancedPayment", works.heymate.beta.R.string.HtAdvancedPayment));
        configTitle2.setTextColor(Theme.getColor(Theme.key_dialogTextBlue2));
        configTitle2.setTypeface(configTitle2.getTypeface(), Typeface.BOLD);
        paymentLayout2.addView(configTitle2, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 25, 15, 15, 0));

        LinearLayout configLayout2 = new LinearLayout(context);
        TextView depositLabel = new TextView(context);
        depositLabel.setText(LocaleController.getString("HtDeposit", works.heymate.beta.R.string.HtDeposit));
        depositLabel.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        configLayout2.addView(depositLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 35, 15, 0, 0));
        parametersViews[INITIAL_DEPOSIT] = new TextView(context);
        parametersViews[INITIAL_DEPOSIT].setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        parametersViews[INITIAL_DEPOSIT].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(LocaleController.getString("HtPercentage", works.heymate.beta.R.string.HtPercentage));
                LinearLayout mainLayout = new LinearLayout(context);
                EditTextBoldCursor feeTextField = new EditTextBoldCursor(context);
                feeTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                feeTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
                feeTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
                feeTextField.setMaxLines(4);
                feeTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
                feeTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                feeTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                feeTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                feeTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
                feeTextField.setMinHeight(AndroidUtilities.dp(36));
                feeTextField.setHint(LocaleController.getString("HtAmount", works.heymate.beta.R.string.HtAmount));
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton(LocaleController.getString("HtApply", works.heymate.beta.R.string.HtApply), (dialog, which) -> {
                    try {
                        initialDeposit = Integer.parseInt(feeTextField.getText().toString());
                    } catch (Throwable t) {
                        initialDeposit = 0;
                    }
                    updateValues();
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout2.addView(parametersViews[INITIAL_DEPOSIT], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 15, 0, 0));
        paymentLayout2.addView(configLayout2);
        LinearLayout configLayout3 = new LinearLayout(context);

        parametersViews[CANCEL_HOURS1] = new TextView(context);
        parametersViews[CANCEL_HOURS1].setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        parametersViews[CANCEL_HOURS1].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(LocaleController.getString("HtCancellationTime", works.heymate.beta.R.string.HtCancellationTime));
                LinearLayout mainLayout = new LinearLayout(context);
                EditTextBoldCursor feeTextField = new EditTextBoldCursor(context);
                feeTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                feeTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
                feeTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
                feeTextField.setMaxLines(4);
                feeTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
                feeTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                feeTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                feeTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                feeTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
                feeTextField.setMinHeight(AndroidUtilities.dp(36));
                feeTextField.setHint(LocaleController.getString("HtAmount", works.heymate.beta.R.string.HtAmount));
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton(LocaleController.getString("HtApply", works.heymate.beta.R.string.HtApply), (dialog, which) -> {
                    try {
                        cancelHours1 = Integer.parseInt(feeTextField.getText().toString());
                    } catch (Throwable t) {
                        cancelHours1 = 0;
                    }
                    updateValues();
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        parametersViews[CANCEL_HOURS1].setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        configLayout3.addView(parametersViews[CANCEL_HOURS1], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 35, 15, 0, 0));

        parametersViews[CANCEL_PERCENT1] = new TextView(context);
        parametersViews[CANCEL_PERCENT1].setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        parametersViews[CANCEL_PERCENT1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(LocaleController.getString("HtPercentage", works.heymate.beta.R.string.HtPercentage));
                LinearLayout mainLayout = new LinearLayout(context);
                EditTextBoldCursor feeTextField = new EditTextBoldCursor(context);
                feeTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                feeTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
                feeTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
                feeTextField.setMaxLines(4);
                feeTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
                feeTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                feeTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                feeTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                feeTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
                feeTextField.setMinHeight(AndroidUtilities.dp(36));
                feeTextField.setHint(LocaleController.getString("HtAmount", works.heymate.beta.R.string.HtAmount));
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton(LocaleController.getString("HtApply", works.heymate.beta.R.string.HtApply), (dialog, which) -> {
                    try {
                        cancelPercent1 = Integer.parseInt(feeTextField.getText().toString());
                    } catch (Throwable t) {
                        cancelPercent1 = 0;
                    }
                    updateValues();
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout3.addView(parametersViews[CANCEL_PERCENT1], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 15, 0, 0));
        paymentLayout2.addView(configLayout3);
        LinearLayout configLayout4 = new LinearLayout(context);

        parametersViews[CANCEL_HOURS2] = new TextView(context);
        parametersViews[CANCEL_HOURS2].setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        parametersViews[CANCEL_HOURS2].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(LocaleController.getString("HtCancellationTime", works.heymate.beta.R.string.HtCancellationTime));
                LinearLayout mainLayout = new LinearLayout(context);
                EditTextBoldCursor feeTextField = new EditTextBoldCursor(context);
                feeTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                feeTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
                feeTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
                feeTextField.setMaxLines(4);
                feeTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
                feeTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                feeTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                feeTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                feeTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
                feeTextField.setMinHeight(AndroidUtilities.dp(36));
                feeTextField.setHint(LocaleController.getString("HtAmount", works.heymate.beta.R.string.HtAmount));
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton(LocaleController.getString("HtApply", works.heymate.beta.R.string.HtApply), (dialog, which) -> {
                    try {
                        cancelHours2 = Integer.parseInt(feeTextField.getText().toString());
                    } catch (Throwable t) {
                        cancelHours2 = 0;
                    }
                    updateValues();
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);

            }
        });
        configLayout4.addView(parametersViews[CANCEL_HOURS2], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 35, 15, 0, 0));

        parametersViews[CANCEL_PERCENT2] = new TextView(context);
        parametersViews[CANCEL_PERCENT2].setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        parametersViews[CANCEL_PERCENT2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(LocaleController.getString("HtPercentage", works.heymate.beta.R.string.HtPercentage));
                LinearLayout mainLayout = new LinearLayout(context);
                EditTextBoldCursor feeTextField = new EditTextBoldCursor(context);
                feeTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                feeTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
                feeTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
                feeTextField.setMaxLines(4);
                feeTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
                feeTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                feeTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                feeTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                feeTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
                feeTextField.setMinHeight(AndroidUtilities.dp(36));
                feeTextField.setHint(LocaleController.getString("HtAmount", works.heymate.beta.R.string.HtAmount));
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton(LocaleController.getString("HtApply", works.heymate.beta.R.string.HtApply), (dialog, which) -> {
                    try {
                        cancelPercent2 = Integer.parseInt(feeTextField.getText().toString());
                    } catch (Throwable t) {
                        cancelPercent2 = 0;
                    }
                    updateValues();
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout4.addView(parametersViews[CANCEL_PERCENT2], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 15, 0, 0));
        paymentLayout2.addView(configLayout4,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        categoryLayout.addView(paymentLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        categoryLayout.setVisibility(GONE);
        titleLayout2.addView(categoryLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        titleLayout2.addView(new Divider(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0,15,0,0));
        addView(paymentLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        setDefaultConfig();
    }

    private void updateValues() {
        parametersViews[DELAY_TIME].setText(LocaleController.getString("HtDelayInStart", works.heymate.beta.R.string.HtDelayInStart) + " > " + delayMinutes + " " + LocaleController.getString("HtMinutes", works.heymate.beta.R.string.HtMinutes));
        parametersViews[DELAY_PERCENT].setText(delayPercent + "%");
        parametersViews[INITIAL_DEPOSIT].setText(initialDeposit + "%");
        parametersViews[CANCEL_HOURS1].setText(LocaleController.getString("HtCancellationIn", works.heymate.beta.R.string.HtCancellationIn) + " < " + cancelHours1 + " " + LocaleController.getString("HtHoursOfStart", works.heymate.beta.R.string.HtHoursOfStart));
        parametersViews[CANCEL_PERCENT1].setText(cancelPercent1 + "%");
        parametersViews[CANCEL_HOURS2].setText(LocaleController.getString("HtCancellationIn", works.heymate.beta.R.string.HtCancellationIn) + " " + cancelHours1 + " - " + cancelHours2 + " " + LocaleController.getString("HtHoursOfStart", works.heymate.beta.R.string.HtHoursOfStart));
        parametersViews[CANCEL_PERCENT2].setText(cancelPercent2 + "%");
    }

    public void setConfig(JSONObject config) {
        if (config == null) {
            setDefaultConfig();
            return;
        }

        try {
            delayMinutes = config.getInt(OfferUtils.DELAY_TIME);
            delayPercent = config.getInt(OfferUtils.DELAY_PERCENT);
            initialDeposit = config.getInt(OfferUtils.INITIAL_DEPOSIT);
            cancelHours1 = config.getInt(OfferUtils.CANCEL_HOURS1);
            cancelPercent1 = config.getInt(OfferUtils.CANCEL_PERCENT1);
            cancelHours2 = config.getInt(OfferUtils.CANCEL_HOURS2);
            cancelPercent2 = config.getInt(OfferUtils.CANCEL_PERCENT2);

            updateValues();
        } catch (JSONException e) {
            setConfig(null);
        }
    }

    private void setDefaultConfig() {
        delayMinutes = 30;
        delayPercent = 0;
        initialDeposit = 0;
        cancelHours1 = 2;
        cancelPercent1 = 0;
        cancelHours2 = 6;
        cancelPercent2 = 0;

        updateValues();
    }

    public JSONObject getConfig() {
        JSONObject json = new JSONObject();

        try {
            json.put(OfferUtils.DELAY_TIME, delayMinutes);
            json.put(OfferUtils.DELAY_PERCENT, delayPercent);
            json.put(OfferUtils.INITIAL_DEPOSIT, initialDeposit);
            json.put(OfferUtils.CANCEL_HOURS1, cancelHours1);
            json.put(OfferUtils.CANCEL_PERCENT1, cancelPercent1);
            json.put(OfferUtils.CANCEL_HOURS2, cancelHours2);
            json.put(OfferUtils.CANCEL_PERCENT2, cancelPercent2);
        } catch (JSONException e) { }

        return json;
    }

    public void setActionType(HtCreateOfferActivity.ActionType actionType){
        if(actionType == HtCreateOfferActivity.ActionType.VIEW) {
            for (TextView argValue : parametersViews) {
                argValue.setOnClickListener(null);
            }
        }
    }
}

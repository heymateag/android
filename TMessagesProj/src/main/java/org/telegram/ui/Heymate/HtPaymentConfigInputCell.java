package org.telegram.ui.Heymate;

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

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import works.heymate.beta.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

import java.util.HashMap;

import works.heymate.core.Utils;
import works.heymate.core.offer.OfferUtils;

public class HtPaymentConfigInputCell extends LinearLayout {

    private String title;
    private HashMap<String, Object> paremetersValues;
    private TextView[] parametersViews;
    private HashMap<String, Runnable> args;
    private HtCreateOfferActivity.ActionType actionType;
    private String delay1 = "30";
    private String cancellation1 = "2";
    private String cancellation2 = "6";

    private String delayMinutes = "30";
    private String delayPercent = null;
    private String initialDeposit = null;
    private String cancelHours1 = "2";
    private String cancelPercent1 = null;
    private String cancelHours2 = "6";
    private String cancelPercent2 = null;

    public HtPaymentConfigInputCell(Context context, String title, HashMap<String, Runnable> args, int icon, HtCreateOfferActivity parent, HtCreateOfferActivity.ActionType actionType) {
        super(context);
        LinearLayout paymentLayout = new LinearLayout(context);
        paymentLayout.setOrientation(VERTICAL);
        paremetersValues = new HashMap<>();
        parametersViews = new TextView[7];
        this.args = args;
        this.title = title;

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
        int i = 0;
        parametersViews[i] = new TextView(context);
        parametersViews[i].setText(LocaleController.getString("HtDelayInStart", works.heymate.beta.R.string.HtDelayInStart) + " > 30 " + LocaleController.getString("HtMinutes", works.heymate.beta.R.string.HtMinutes));
        parametersViews[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        int finalI2 = i;
        parametersViews[i].setOnClickListener(new OnClickListener() {
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
                    if (feeTextField.getText().toString().length() > 0) {
                        parametersViews[finalI2].setText(LocaleController.getString("HtDelayInStart", works.heymate.beta.R.string.HtDelayInStart) + " > " + feeTextField.getText().toString() + " " + LocaleController.getString("HtMinutes", works.heymate.beta.R.string.HtMinutes));
                        delay1 = feeTextField.getText().toString();
                        delayMinutes = delay1;
                    }
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        parametersViews[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        configLayout1.addView(parametersViews[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 35, 15, 0, 0));

        parametersViews[i] = new TextView(context);
        parametersViews[i].setText(LocaleController.getString("HtSelect", works.heymate.beta.R.string.HtSelect));
        parametersViews[i].setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        final int i1 = i;
        parametersViews[i].setOnClickListener(new View.OnClickListener() {
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
                    if (feeTextField.getText().toString().length() > 0) {
                        parametersViews[i1].setText(feeTextField.getText().toString() + "%");
                    }
                    delayPercent = feeTextField.getText().toString();
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout1.addView(parametersViews[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 15, 0, 0));
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
        parametersViews[i] = new TextView(context);
        parametersViews[i].setText(LocaleController.getString("HtSelect", works.heymate.beta.R.string.HtSelect));
        parametersViews[i].setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        final int i2 = i;
        parametersViews[i].setOnClickListener(new View.OnClickListener() {
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
                    if (feeTextField.getText().toString().length() > 0)
                        parametersViews[i2].setText(feeTextField.getText().toString() + "%");
                    initialDeposit = feeTextField.getText().toString();
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout2.addView(parametersViews[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 15, 0, 0));
        paymentLayout2.addView(configLayout2);
        LinearLayout configLayout3 = new LinearLayout(context);

        parametersViews[i] = new TextView(context);
        parametersViews[i].setText(LocaleController.getString("HtCancellationIn", works.heymate.beta.R.string.HtCancellationIn) + " < 2 " + LocaleController.getString("HtHoursOfStart", works.heymate.beta.R.string.HtHoursOfStart));
        parametersViews[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        int finalI1 = i;
        parametersViews[i].setOnClickListener(new OnClickListener() {
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
                    if (feeTextField.getText().toString().length() > 0) {
                        parametersViews[finalI1].setText(LocaleController.getString("HtCancellationIn", works.heymate.beta.R.string.HtCancellationIn) + " < " + feeTextField.getText().toString() + " " + LocaleController.getString("HtHoursOfStart", works.heymate.beta.R.string.HtHoursOfStart));
                        cancellation1 = feeTextField.getText().toString();
                        cancelHours1 = cancellation1;
                    }
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        parametersViews[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        configLayout3.addView(parametersViews[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 35, 15, 0, 0));

        parametersViews[i] = new TextView(context);
        parametersViews[i].setText(LocaleController.getString("HtSelect", works.heymate.beta.R.string.HtSelect));
        parametersViews[i].setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        final int i3 = i;
        parametersViews[i].setOnClickListener(new View.OnClickListener() {
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
                    if (feeTextField.getText().toString().length() > 0)
                        parametersViews[i3].setText(feeTextField.getText().toString() + "%");
                    cancelPercent1 = feeTextField.getText().toString();
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout3.addView(parametersViews[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 15, 0, 0));
        paymentLayout2.addView(configLayout3);
        LinearLayout configLayout4 = new LinearLayout(context);

        parametersViews[i] = new TextView(context);
        parametersViews[i].setText(LocaleController.getString("HtCancellationIn", works.heymate.beta.R.string.HtCancellationIn) + " 2 - 6 " + LocaleController.getString("HtHoursOfStart", works.heymate.beta.R.string.HtHoursOfStart));
        parametersViews[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        int finalI = i;
        parametersViews[i].setOnClickListener(new OnClickListener() {
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
                    if (feeTextField.getText().toString().length() > 0) {
                        parametersViews[finalI].setText(LocaleController.getString("HtCancellationIn", works.heymate.beta.R.string.HtCancellationIn) + " " + cancellation1 + " - " + feeTextField.getText().toString() + " " + LocaleController.getString("HtHoursOfStart", works.heymate.beta.R.string.HtHoursOfStart));
                        cancellation2 = feeTextField.getText().toString();
                        cancelHours2 = cancellation2;
                    }
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);

            }
        });
        parametersViews[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        configLayout4.addView(parametersViews[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 35, 15, 0, 0));

        parametersViews[i] = new TextView(context);
        parametersViews[i].setText(LocaleController.getString("HtSelect", works.heymate.beta.R.string.HtSelect));
        parametersViews[i].setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        final int i4 = i;
        parametersViews[i].setOnClickListener(new View.OnClickListener() {
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
                    if (feeTextField.getText().toString().length() > 0)
                        parametersViews[i4].setText(feeTextField.getText().toString() + "%");
                    cancelPercent2 = feeTextField.getText().toString();
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout4.addView(parametersViews[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 15, 0, 0));
        paymentLayout2.addView(configLayout4,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        categoryLayout.addView(paymentLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        categoryLayout.setVisibility(GONE);
        titleLayout2.addView(categoryLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        titleLayout2.addView(new HtDividerCell(context, true), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0,15,0,0));
        addView(paymentLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

    }

    public void setRes(Object value, int position) {
        parametersViews[position].setText(value.toString());
        parametersViews[position].setTextColor(getContext().getResources().getColor(works.heymate.beta.R.color.ht_green));
    }

    public JSONObject getRes() {
        JSONObject json = new JSONObject();

        Utils.putValues(json,
                OfferUtils.DELAY_TIME, delayMinutes,
                OfferUtils.DELAY_PERCENT, delayPercent,
                OfferUtils.INITIAL_DEPOSIT, initialDeposit,
                OfferUtils.CANCEL_HOURS1, cancelHours1,
                OfferUtils.CANCEL_PERCENT1, cancelPercent1,
                OfferUtils.CANCEL_HOURS2, cancelHours2,
                OfferUtils.CANCEL_PERCENT2, cancelPercent2
        );

        return json;
    }

    public void setActionType(HtCreateOfferActivity.ActionType actionType){
        this.actionType = actionType;
        if(actionType == HtCreateOfferActivity.ActionType.VIEW) {
            for (TextView argValue : parametersViews) {
                argValue.setOnClickListener(null);
            }
        }
    }
}

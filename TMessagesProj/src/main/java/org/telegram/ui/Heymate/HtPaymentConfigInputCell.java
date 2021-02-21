package org.telegram.ui.Heymate;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Arrays;
import java.util.HashMap;

public class HtPaymentConfigInputCell extends LinearLayout {
    private RectF rect = new RectF();
    Paint paint = new Paint();
    TextPaint textPaint = new TextPaint();
    String title;
    private HashMap<String, Object> argsRes;
    private TextView[] argValues;
    private HashMap<String, Runnable> args;
    private HtCreateOfferActivity.ActionType actionType;
    private String delay1 = "30";
    private String cancellation1 = "2";
    private String cancellation2 = "6";

    public HtPaymentConfigInputCell(Context context, String title, HashMap<String, Runnable> args, int icon, BaseFragment parent, HtCreateOfferActivity.ActionType actionType) {
        super(context);
        LinearLayout paymentLayout = new LinearLayout(context);
        paymentLayout.setOrientation(VERTICAL);
        setWillNotDraw(false);
        argsRes = new HashMap<>();
        argValues = new TextView[7];
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
        Drawable expandDrawable = context.getResources().getDrawable(R.drawable.arrow_more);
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
        configTitle1.setText("Service Provider promises");
        configTitle1.setTextColor(Theme.getColor(Theme.key_dialogTextBlue2));
        configTitle1.setTypeface(configTitle1.getTypeface(), Typeface.BOLD);
        paymentLayout2.addView(configTitle1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 25, 0, 0, 0));
        LinearLayout configLayout1 = new LinearLayout(context);
        int i = 0;
        argValues[i] = new TextView(context);
        argValues[i].setText("Delays in start by > 30 mins");
        argValues[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        int finalI2 = i;
        argValues[i].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delay Time");
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
                feeTextField.setHint("Amount");
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton("Apply", (dialog, which) -> {
                    if (feeTextField.getText().toString().length() > 0) {
                        argValues[finalI2].setText("Delays in start by > " + feeTextField.getText().toString() + " mins");
                        delay1 = feeTextField.getText().toString();
                    }
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        argValues[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        configLayout1.addView(argValues[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 35, 15, 0, 0));
        argValues[i] = new TextView(context);
        argValues[i].setText("Please select");
        argValues[i].setTextColor(context.getResources().getColor(R.color.ht_green));
        final int i1 = i;
        argValues[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Percentage");
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
                feeTextField.setHint("Amount");
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton("Apply", (dialog, which) -> {
                    if (feeTextField.getText().toString().length() > 0)
                        argValues[i1].setText(feeTextField.getText().toString() + "%");
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout1.addView(argValues[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 15, 0, 0));
        paymentLayout2.addView(configLayout1);
        TextView configTitle2 = new TextView(context);
        configTitle2.setText("Advanced payment & related cancellation conditions");
        configTitle2.setTextColor(Theme.getColor(Theme.key_dialogTextBlue2));
        configTitle2.setTypeface(configTitle2.getTypeface(), Typeface.BOLD);
        paymentLayout2.addView(configTitle2, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 25, 15, 15, 0));
        LinearLayout configLayout2 = new LinearLayout(context);
        TextView depositLabel = new TextView(context);
        depositLabel.setText("Deposit");
        depositLabel.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        configLayout2.addView(depositLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 35, 15, 0, 0));
        argValues[i] = new TextView(context);
        argValues[i].setText("Please select");
        argValues[i].setTextColor(context.getResources().getColor(R.color.ht_green));
        final int i2 = i;
        argValues[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Percentage");
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
                feeTextField.setHint("Amount");
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton("Apply", (dialog, which) -> {
                    if (feeTextField.getText().toString().length() > 0)
                        argValues[i2].setText(feeTextField.getText().toString() + "%");
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout2.addView(argValues[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 15, 0, 0));
        paymentLayout2.addView(configLayout2);
        LinearLayout configLayout3 = new LinearLayout(context);
        argValues[i] = new TextView(context);
        argValues[i].setText("Cancellation in < 2 hrs of start");
        argValues[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        int finalI1 = i;
        argValues[i].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Cancelation Time");
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
                feeTextField.setHint("Amount");
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton("Apply", (dialog, which) -> {
                    if (feeTextField.getText().toString().length() > 0) {
                        argValues[finalI1].setText("Cancellation in < " + feeTextField.getText().toString() + " hrs of start");
                        cancellation1 = feeTextField.getText().toString();
                    }
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        argValues[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        configLayout3.addView(argValues[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 35, 15, 0, 0));
        argValues[i] = new TextView(context);
        argValues[i].setText("Please select");
        argValues[i].setTextColor(context.getResources().getColor(R.color.ht_green));
        final int i3 = i;
        argValues[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Percentage");
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
                feeTextField.setHint("Amount");
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton("Apply", (dialog, which) -> {
                    if (feeTextField.getText().toString().length() > 0)
                        argValues[i3].setText(feeTextField.getText().toString() + "%");
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout3.addView(argValues[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 15, 0, 0));
        paymentLayout2.addView(configLayout3);
        LinearLayout configLayout4 = new LinearLayout(context);
        argValues[i] = new TextView(context);
        argValues[i].setText("Cancellation in 2 - 6 hrs of start");
        argValues[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        int finalI = i;
        argValues[i].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Cancelation Time");
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
                feeTextField.setHint("Amount");
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton("Apply", (dialog, which) -> {
                    if (feeTextField.getText().toString().length() > 0) {
                        argValues[finalI].setText("Cancellation in " + cancellation1 + " - " + feeTextField.getText().toString() + " hrs of start");
                        cancellation2 = feeTextField.getText().toString();
                    }
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);

            }
        });
        argValues[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        configLayout4.addView(argValues[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 35, 15, 0, 0));
        argValues[i] = new TextView(context);
        argValues[i].setText("Please select");
        argValues[i].setTextColor(context.getResources().getColor(R.color.ht_green));
        final int i4 = i;
        argValues[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionType == HtCreateOfferActivity.ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Percentage");
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
                feeTextField.setHint("Amount");
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton("Apply", (dialog, which) -> {
                    if (feeTextField.getText().toString().length() > 0)
                        argValues[i4].setText(feeTextField.getText().toString() + "%");
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            }
        });
        configLayout4.addView(argValues[i++], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 15, 0, 0));
        paymentLayout2.addView(configLayout4,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        categoryLayout.addView(paymentLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        categoryLayout.setVisibility(GONE);
        titleLayout2.addView(categoryLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        titleLayout2.addView(new HtDividerCell(context, true), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0,15,0,0));
        addView(paymentLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

    }

    @Override
    protected void onDraw(Canvas canvas) {
/*        paint.setColor(Theme.getColor(Theme.key_wallet_grayBackground));
        paint.setStrokeWidth(3);
        rect.set(AndroidUtilities.dp(5), AndroidUtilities.dp(5), getMeasuredWidth() - AndroidUtilities.dp(5), getMeasuredHeight() - AndroidUtilities.dp(5));
        canvas.drawRoundRect(rect, AndroidUtilities.dp(6), AndroidUtilities.dp(6), paint);
        paint.setColor(Theme.getColor(Theme.key_graySection));
        paint.setStrokeWidth(3);
        rect.set(AndroidUtilities.dp(9), AndroidUtilities.dp(9), getMeasuredWidth() - AndroidUtilities.dp(9), getMeasuredHeight() - AndroidUtilities.dp(9));
        textPaint.setTextSize(16);
        textPaint.setColor(Theme.getColor(Theme.key_dialogTextBlack));
        canvas.drawRoundRect(rect, AndroidUtilities.dp(6), AndroidUtilities.dp(6), paint);*/
    }

    public void setRes(Object value, int position) {
        argValues[position].setText(value.toString());
        argValues[position].setTextColor(getContext().getResources().getColor(R.color.ht_green));
    }

    public Object[] getRes() {
        return Arrays.stream(argValues).map((a) -> a.getText().toString()).toArray();
    }

    public void setActionType(HtCreateOfferActivity.ActionType actionType){
        this.actionType = actionType;
        if(actionType == HtCreateOfferActivity.ActionType.VIEW) {
            for (TextView argValue : argValues) {
                argValue.setOnClickListener(null);
            }
        }
    }
}

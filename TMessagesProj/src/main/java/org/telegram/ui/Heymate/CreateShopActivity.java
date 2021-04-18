package org.telegram.ui.Heymate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioButtonCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.ChannelCreateActivity;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadioButton;
import org.telegram.ui.GroupCreateActivity;

import works.heymate.core.Texts;
import works.heymate.core.wallet.Security;

public class CreateShopActivity extends BaseFragment {

    public static final int TYPE_MARKETPLACE = 0;
    public static final int TYPE_SHOP = 1;
    public static final int TYPE_NONE = -1;

    private Runnable mFinishTask = null;

    private RadioButtonCellWithIcon mButtonMarketplace;
    private RadioButtonCellWithIcon mButtonShop;

    private int mType = TYPE_NONE;

    public CreateShopActivity() {

    }

    public CreateShopActivity(Runnable finishTask) {
        mFinishTask = finishTask;
    }

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        FrameLayout container = new FrameLayout(context);
        container.addView(content, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        TextView textTitle = new TextView(context);
        textTitle.setTextColor(ContextCompat.getColor(context, R.color.ht_theme));
        textTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textTitle.setTypeface(textTitle.getTypeface(), Typeface.BOLD);
        textTitle.setText(Texts.get(Texts.CREATE_SHOP_SHOP_TYPE));
        content.addView(textTitle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 20, 0, 8));

        mButtonMarketplace = new RadioButtonCellWithIcon(context);
        mButtonMarketplace.setTextAndValue(Texts.get(Texts.CREATE_SHOP_MARKETPLACE).toString(), Texts.get(Texts.CREATE_SHOP_MARKETPLACE_DESCRIPTION).toString(), false, false);
        mButtonMarketplace.setIcon(R.drawable.ic_addshop);
        content.addView(mButtonMarketplace, LayoutHelper.createLinear(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0, 8, 0, 0));

        mButtonShop = new RadioButtonCellWithIcon(context);
        mButtonShop.setTextAndValue(Texts.get(Texts.CREATE_SHOP_SHOP).toString(), Texts.get(Texts.CREATE_SHOP_SHOP_DESCRIPTION).toString(), false, false);
        mButtonShop.setIcon(R.drawable.offer);
        content.addView(mButtonShop, LayoutHelper.createLinear(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0));

        content.addView(new ShadowSectionCell(context, 12, Theme.getColor(Theme.key_windowBackgroundGray)), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        ActionBar actionBar = getActionBar();
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setTitle(Texts.get(Texts.CREATE_SHOP_TITLE));
        actionBar.createMenu().addItem(0, R.drawable.checkbig);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (getParentActivity() == null) {
                    return;
                }

                if (id == -1) {
                    finishFragment();

                    if (mFinishTask != null) {
                        mFinishTask.run();
                    }
                    return;
                }

                if (id == 0) {
                    Bundle args = new Bundle();

                    switch (mType) {
                        case TYPE_MARKETPLACE:
                            args.putInt("chatType", ChatObject.CHAT_TYPE_MEGAGROUP);
                            args.putInt("heymateType", TYPE_MARKETPLACE);
                            presentFragment(new GroupCreateActivity(args), true);
                            break;
                        case TYPE_SHOP:
                            args.putInt("step", 0);
                            args.putInt("heymateType", TYPE_SHOP);
                            presentFragment(new ChannelCreateActivity(args), true);
                            break;
                        case TYPE_NONE:
                            break;
                    }
                }
            }
        });

        mButtonMarketplace.setOnClickListener(v -> setType(TYPE_MARKETPLACE));
        mButtonShop.setOnClickListener(v -> setType(TYPE_SHOP));

        updateState();

        return container;
    }

    private void setType(int type) {
        mType = type;
        updateState();
    }

    private void updateState() {
        switch (mType) {
            case TYPE_MARKETPLACE:
                mButtonMarketplace.setChecked(true, true);
                mButtonShop.setChecked(false, true);
                break;
            case TYPE_SHOP:
                mButtonMarketplace.setChecked(false, true);
                mButtonShop.setChecked(true, true);
                break;
            case TYPE_NONE:
                mButtonMarketplace.setChecked(false, false);
                mButtonShop.setChecked(false, false);
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        finishFragment(true);

        if (mFinishTask != null) {
            mFinishTask.run();
        }

        return super.onBackPressed();
    }

    private static class RadioButtonCellWithIcon extends FrameLayout {

        private TextView textView;
        private TextView valueTextView;
        private RadioButton radioButton;
        private boolean needDivider;

        private ImageView mImageIcon;

        public RadioButtonCellWithIcon(Context context) {
            super(context);

            final boolean dialog = false;

            radioButton = new RadioButton(context);
            radioButton.setSize(AndroidUtilities.dp(20));
            if (dialog) {
                radioButton.setColor(Theme.getColor(Theme.key_dialogRadioBackground), ContextCompat.getColor(context, R.color.ht_theme));
            } else {
                radioButton.setColor(Theme.getColor(Theme.key_radioBackground), ContextCompat.getColor(context, R.color.ht_theme));
            }
            addView(radioButton, LayoutHelper.createFrame(22, 22, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 0 : 20), 10, (LocaleController.isRTL ? 20 : 0), 0));

            textView = new TextView(context);
            if (dialog) {
                textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            } else {
                textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 23 : 61), 10, (LocaleController.isRTL ? 61 : 23), 0));

            valueTextView = new TextView(context);
            if (dialog) {
                valueTextView.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
            } else {
                valueTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            }
            valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            valueTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            valueTextView.setLines(0);
            valueTextView.setMaxLines(0);
            valueTextView.setSingleLine(false);
            valueTextView.setPadding(0, 0, 0, AndroidUtilities.dp(12));
            addView(valueTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 17 : 61), 35, (LocaleController.isRTL ? 61 : 17), 12));

            mImageIcon = new ImageView(context);
            mImageIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mImageIcon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteHintText), PorterDuff.Mode.SRC_IN);
            addView(mImageIcon, LayoutHelper.createFrame(24, 24, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, (LocaleController.isRTL ? 23 : 61), 9, (LocaleController.isRTL ? 61 : 23), 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        }

        public void setIcon(int resId) {
            mImageIcon.setImageResource(resId);
        }

        public void setTextAndValue(String text, String value, boolean divider, boolean checked) {
            textView.setText(text);
            valueTextView.setText(value);
            radioButton.setChecked(checked, false);
            needDivider = divider;
        }

        public void setChecked(boolean checked, boolean animated) {
            radioButton.setChecked(checked, animated);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (needDivider) {
                canvas.drawLine(AndroidUtilities.dp(LocaleController.isRTL ? 0 : 60), getHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(LocaleController.isRTL ? 60 : 0), getHeight() - 1, Theme.dividerPaint);
            }
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName("android.widget.RadioButton");
            info.setCheckable(true);
            info.setChecked(radioButton.isChecked());
        }

    }

}

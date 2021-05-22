package org.telegram.ui.Heymate.widget;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import works.heymate.beta.R;

public class MultiChoicePopup {

    public interface OnChoiceSelectedListener {

        void onChoiceSelected(int index, String choice);

    }

    private final View mAnchor;

    private final PopupWindow mPopup;

    public MultiChoicePopup(View anchor, String[] choices, OnChoiceSelectedListener listener) {
        mAnchor = anchor;

        mPopup = new PopupWindow(anchor.getContext());
        mPopup.setBackgroundDrawable(new ColorDrawable(Theme.getColor(Theme.key_dialogBackground)));
        mPopup.setAnimationStyle(R.style.PopupAnimation);
        mPopup.setOutsideTouchable(false);
        mPopup.setFocusable(true);
        mPopup.setTouchable(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPopup.setElevation(AndroidUtilities.dp(4));
        }

        LinearLayout content = new LinearLayout(anchor.getContext());
        content.setOrientation(LinearLayout.VERTICAL);

        int width = AndroidUtilities.dp(72);
        TextView[] texts = new TextView[choices.length];

        for (int i = 0; i < choices.length; i++) {
            int index = i;
            TextView text = new TextView(anchor.getContext());
            text.setTextSize(16);
            text.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            text.setGravity(Gravity.CENTER);
            text.setPadding(
                    AndroidUtilities.dp(12),
                    AndroidUtilities.dp(6),
                    AndroidUtilities.dp(12),
                    AndroidUtilities.dp(6)
                    );
            text.setBackground(Theme.getSelectorDrawable(true));
            text.setText(choices[i]);
            text.setOnClickListener(v -> {
                listener.onChoiceSelected(index, choices[index]);
                mPopup.dismiss();
            });

            text.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );

            width = Math.max(width, text.getMeasuredWidth());
            texts[i] = text;

            content.addView(text, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        mPopup.setWidth(width);
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        mPopup.setContentView(content);
    }

    public void show() {
        mPopup.showAsDropDown(mAnchor);
    }

}

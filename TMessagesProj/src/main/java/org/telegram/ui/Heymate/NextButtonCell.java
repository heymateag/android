/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Heymate;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import works.heymate.beta.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class NextButtonCell extends FrameLayout {

    private TextView textView;

    public NextButtonCell(Context context) {
        super(context);

        textView = new TextView(context) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 1.0f : 0.5f);
            }

            @Override
            public void setTextColor(int color) {
                super.setTextColor(color);
                setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(color));
            }
        };
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setLines(1);
        textView.setMaxLines(1);
        textView.setSingleLine(true);
        textView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        textView.setCompoundDrawablePadding(AndroidUtilities.dp(5));
        addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 23, 0, 16, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        textView.setText(LocaleController.getString("HtNext", works.heymate.beta.R.string.HtNext));
        Drawable drawable = getResources().getDrawable(works.heymate.beta.R.drawable.actionbtn_next);
        if (drawable != null) {
            drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue), PorterDuff.Mode.MULTIPLY));
        }
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }
}

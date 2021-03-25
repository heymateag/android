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
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class ServiceProviderPromiseRowCell extends FrameLayout {

    private TextView textView;
    private TextView textView2;
    private String title;
    private String value;
    private String symbol;

    public ServiceProviderPromiseRowCell(Context context, String title, String value, String symbol) {
        super(context);
        this.title = title;
        this.value = value;
        this.symbol = symbol;
        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setLines(5);
        textView.setMaxLines(5);
        textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        textView.setCompoundDrawablePadding(AndroidUtilities.dp(28));
        textView.setMinLines(3);
        textView.setPadding(0,0,0,10);
        addView(textView, LayoutHelper.createFrame(250, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 23, 0, 16, 0));
        textView2 = new TextView(context);
        textView2.setTextColor(Theme.getColor(Theme.key_statisticChartNightIconColor));
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView2.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView2.setLines(1);
        textView2.setMaxLines(1);
        textView2.setSingleLine(true);
        textView2.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        textView2.setCompoundDrawablePadding(AndroidUtilities.dp(80));
        addView(textView2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 23, 0, 16, 0));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        textView.setText(title);
        textView2.setText(value + symbol);
        Drawable drawable = getResources().getDrawable(R.drawable.msg_arrowright);
        if (drawable != null) {
            drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuItemIcon), PorterDuff.Mode.MULTIPLY));
        }
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        textView.setCompoundDrawablePadding(70);
    }
}

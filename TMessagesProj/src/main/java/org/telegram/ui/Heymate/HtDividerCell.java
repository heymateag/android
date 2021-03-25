/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Heymate;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

public class HtDividerCell extends FrameLayout {

    private TextView valueTextView;
    private boolean narrow = false;
    public HtDividerCell(Context context, boolean narrow) {
        super(context);

        valueTextView = new TextView(context);
        valueTextView.setBackgroundColor(Theme.getColor(Theme.key_graySection));
        valueTextView.setVisibility(VISIBLE);
        if(!narrow)
            addView(valueTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 15));
        else
            addView(valueTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1));
        requestLayout();
    }

    public HtDividerCell(Context context) {
        this(context, false);
    }

    public HtDividerCell(Context context, int padding) {
        this(context, false);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setEnabled(isEnabled());
    }
}

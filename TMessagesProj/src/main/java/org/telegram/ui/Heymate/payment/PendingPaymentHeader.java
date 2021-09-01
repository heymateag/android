package org.telegram.ui.Heymate.payment;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.LayoutHelper;

public class PendingPaymentHeader extends FrameLayout {

    public PendingPaymentHeader(@NonNull Context context) {
        super(context);
        setBackgroundColor(0xFFCDEAFA);

        TextView text = new TextView(context);
        text.setTextSize(14);
        text.setTextColor(0xff292929);
        // text.setTypeface(AndroidUtilities.getTypeface("fonts/ritalic.ttf"));
        text.setText("Processing your payment..."); // TODO Texts and colors?
        addView(text, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 16, 0, 0, 0));
    }

}

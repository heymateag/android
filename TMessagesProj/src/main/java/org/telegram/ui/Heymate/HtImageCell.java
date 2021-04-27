package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import works.heymate.beta.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;

public class HtImageCell extends ImageView {

    public HtImageCell(Context context, BaseFragment parentFragment, boolean inner) {
        super(context);
        Drawable drawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.theme_preview_image);
        setImageDrawable(drawable);
        setEnabled(true);
        if (!inner) {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(new HtImageCell(context, parentFragment, true));
                    AlertDialog alert = builder.create();
                    alert.setBackgroundColor(0x11333333);
                    parentFragment.showDialog(alert);
                }
            });
        }
    }
}

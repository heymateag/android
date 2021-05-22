package org.telegram.ui.Heymate.createoffer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import works.heymate.beta.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.HashMap;

public class HtInputCell extends LinearLayout {

    private String title;
    private HashMap<String, Object> paremetersValues;
    private TextView[] parametersViews;
    private Drawable[] iconValues;
    private HashMap<String, Runnable> args;

    public HtInputCell(Context context, String title, HashMap<String, Runnable> args,int icon, boolean canEdit) {
        super(context);
        setWillNotDraw(false);
        paremetersValues = new HashMap<>();
        parametersViews = new TextView[args.size()];
        iconValues = new Drawable[args.size()];
        this.args = args;
        setMinimumHeight(AndroidUtilities.dp(100 + args.size() * 30 + 45));
        this.title = title;

        LinearLayout titleLayout = new LinearLayout(context);
        ImageView titleImage = new ImageView(context);
        Drawable titleDrawable = context.getResources().getDrawable(icon);
        titleDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray), PorterDuff.Mode.MULTIPLY));
        titleImage.setImageDrawable(titleDrawable);
        titleLayout.addView(titleImage, LayoutHelper.createLinear(20, 20, AndroidUtilities.dp(9), AndroidUtilities.dp(9), 15, 15));

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
        titleLayout3.addView(titleLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 1f, AndroidUtilities.dp(9), AndroidUtilities.dp(9), 0, 15));
        titleLayout2.addView(titleLayout3);
        int i = 0;

        for (Object arg : args.keySet().stream().sorted().toArray()) {
            LinearLayout parametersLayout = new LinearLayout(context);
            parametersViews[i] = new TextView(context);
            parametersViews[i].setText(((String) arg).substring(2));
            parametersViews[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlue2));
            parametersViews[i].setTypeface(parametersViews[i].getTypeface(), Typeface.BOLD);
            Drawable drawable;
            if(canEdit)
                drawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.ht_touch);
            else
                drawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.menu_info);
            iconValues[i] = drawable.getConstantState().newDrawable().mutate();
            parametersViews[i].setCompoundDrawablePadding(AndroidUtilities.dp(5));
            iconValues[i].setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue2), PorterDuff.Mode.MULTIPLY));
            parametersViews[i].setCompoundDrawablesWithIntrinsicBounds(iconValues[i], null, null, null);
            parametersLayout.addView(parametersViews[i], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 0, 15, 15));
            titleLayout2.addView(parametersLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 25, 0, 0, 0));
            if(canEdit) {
                titleLayout3.setEnabled(true);
                titleLayout3.setHovered(true);
                parametersLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        args.get(arg).run();
                    }
                });
            }
            i++;
        }
        titleLayout.addView(titleLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addView(titleLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

    }

    public void setRes(String arg, Object value, int position) {
        paremetersValues.put(arg, value);
        parametersViews[position].setText(value.toString());
        parametersViews[position].setTextColor(getContext().getResources().getColor(works.heymate.beta.R.color.ht_green));
    }

    public String getRes(String arg){
        return (String) paremetersValues.get(arg);
    }


    public void setError(boolean error, int position){
        if(error)
            iconValues[position].setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_inRedCall), PorterDuff.Mode.MULTIPLY));
        else
            iconValues[position].setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue2), PorterDuff.Mode.MULTIPLY));
    }
}

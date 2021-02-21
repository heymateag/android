package org.telegram.ui.Heymate;

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
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.HashMap;

public class HtInputCell extends LinearLayout {
    private RectF rect = new RectF();
    Paint paint = new Paint();
    TextPaint textPaint = new TextPaint();
    String title;
    private HashMap<String, Object> argsRes;
    private TextView[] argValues;
    private Drawable[] iconValues;
    private HashMap<String, Runnable> args;

    public HtInputCell(Context context, String title, HashMap<String, Runnable> args,int icon, boolean canEdit) {
        super(context);
        setWillNotDraw(false);
        argsRes = new HashMap<>();
        argValues = new TextView[args.size()];
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
            LinearLayout selectedArgLayout = new LinearLayout(context);
            argValues[i] = new TextView(context);
            argValues[i].setText(((String) arg).substring(2));
            argValues[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlue2));
            argValues[i].setTypeface(argValues[i].getTypeface(), Typeface.BOLD);
            Drawable drawable;
            if(canEdit)
                drawable = context.getResources().getDrawable(R.drawable.ht_touch);
            else
                drawable = context.getResources().getDrawable(R.drawable.menu_info);
            iconValues[i] = drawable.getConstantState().newDrawable().mutate();
            argValues[i].setCompoundDrawablePadding(AndroidUtilities.dp(5));
            iconValues[i].setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue2), PorterDuff.Mode.MULTIPLY));
            argValues[i].setCompoundDrawablesWithIntrinsicBounds(iconValues[i], null, null, null);
            selectedArgLayout.addView(argValues[i], LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f, 0, 0, 15, 15));
            titleLayout2.addView(selectedArgLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 25, 0, 0, 0));
            if(canEdit) {
                titleLayout3.setEnabled(true);
                titleLayout3.setHovered(true);
                selectedArgLayout.setOnClickListener(new View.OnClickListener() {
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

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(Theme.getColor(Theme.key_wallet_grayBackground));
        paint.setStrokeWidth(3);
        rect.set(AndroidUtilities.dp(5), AndroidUtilities.dp(5), getMeasuredWidth() - AndroidUtilities.dp(5), getMeasuredHeight() - AndroidUtilities.dp(5));
        canvas.drawRoundRect(rect, AndroidUtilities.dp(6), AndroidUtilities.dp(6), paint);
        paint.setColor(Theme.getColor(Theme.key_graySection));
        paint.setStrokeWidth(3);
        rect.set(AndroidUtilities.dp(9), AndroidUtilities.dp(9), getMeasuredWidth() - AndroidUtilities.dp(9), getMeasuredHeight() - AndroidUtilities.dp(9));
        textPaint.setTextSize(16);
        textPaint.setColor(Theme.getColor(Theme.key_dialogTextBlack));
        canvas.drawRoundRect(rect, AndroidUtilities.dp(6), AndroidUtilities.dp(6), paint);
    }

    public void setRes(String arg, Object value, int position) {
        argsRes.put(arg, value);
        argValues[position].setText(value.toString());
        argValues[position].setTextColor(getContext().getResources().getColor(R.color.ht_green));
    }

    public String getRes(String arg){
        return (String) argsRes.get(arg);
    }


    public void setError(boolean error, int position){
        if(error)
            iconValues[position].setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_inRedCall), PorterDuff.Mode.MULTIPLY));
        else
            iconValues[position].setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue2), PorterDuff.Mode.MULTIPLY));
    }
}

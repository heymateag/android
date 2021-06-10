package org.telegram.ui.Heymate.createoffer;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class HtScheduleInputCell extends LinearLayout {

    private String title;
    private HashMap<String, Object> paremetersValues;
    private TextView[] parametersViews;
    private Drawable[] iconValues;
    private HashMap<String, Runnable> args;
    private ArrayList<Long> dateSlots = new ArrayList<>();

    public void setDateSlots(ArrayList<Long> dateSlots) {
        this.dateSlots = dateSlots;
    }

    public HtScheduleInputCell(Context context, String title, HashMap<String, Runnable> args, int icon, boolean canEdit, HtCreateOfferActivity parent) {
        super(context);
        paremetersValues = new HashMap<>();
        parametersViews = new TextView[args.size()];
        iconValues = new Drawable[args.size()];
        this.args = args;
        this.title = title;

        LinearLayout titleLayout = new LinearLayout(context);
        ImageView titleImage = new ImageView(context);
        Drawable titleDrawable = context.getResources().getDrawable(icon);
        titleDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4), PorterDuff.Mode.MULTIPLY));
        titleImage.setImageDrawable(titleDrawable);
        titleLayout.addView(titleImage, LayoutHelper.createLinear(20, 20, AndroidUtilities.dp(9), AndroidUtilities.dp(4), 15, 15));
        LinearLayout titleLayout2 = new LinearLayout(context);
        titleLayout2.setOrientation(LinearLayout.VERTICAL);
        LinearLayout titleLayout3 = new LinearLayout(context);

        TextView titleLabel = new TextView(context);
        titleLabel.setText(title);
        titleLabel.setTextSize(16);
        titleLabel.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleLabel.setPadding(15, 0, 0, 0);
        titleLayout3.addView(titleLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 1f, AndroidUtilities.dp(9), AndroidUtilities.dp(4), 0, 15));

        ImageView expandIcon = new ImageView(context);
        Drawable expandDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.arrow_more);
        expandDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4), PorterDuff.Mode.MULTIPLY));
        expandIcon.setImageDrawable(expandDrawable);
        titleLayout3.addView(expandIcon, LayoutHelper.createLinear(15, 15, AndroidUtilities.dp(20), AndroidUtilities.dp(4), 30, 15));
        titleLayout2.addView(titleLayout3);

        LinearLayout categoryLayout = new LinearLayout(context);
        expandIcon.setEnabled(true);
        expandIcon.setHovered(true);
        final boolean[] isOpen = {false};
        titleLayout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.titleTextField.clearFocus();
                parent.titleTextField.hideActionMode();
                AndroidUtilities.hideKeyboard(parent.titleTextField);
                parent.descriptionTextField.clearFocus();
                parent.descriptionTextField.hideActionMode();
                AndroidUtilities.hideKeyboard(parent.descriptionTextField);

                HtCalendarBottomSheet calendarBottomSheet = new HtCalendarBottomSheet(context, true, parent);
                calendarBottomSheet.setDates(dateSlots);
                parent.showDialog(calendarBottomSheet);
            }
        });
        int i = 0;

        for (Object arg : args.keySet().stream().sorted().toArray()) {
            LinearLayout parametersLayout = new LinearLayout(context);
            parametersLayout.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground));
            parametersLayout.setGravity(Gravity.CENTER);
            ImageView mapImage = new ImageView(context);
            Drawable mapDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.google_maps_logo);
            mapImage.setImageDrawable(mapDrawable);
            parametersLayout.addView(mapImage, LayoutHelper.createLinear(200, 120, 10,5,10,5));
            if(canEdit) {
                titleLayout3.setEnabled(true);
                titleLayout3.setHovered(true);
                parametersLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        args.get(arg).run();
                    }
                });
            }
            categoryLayout.addView(parametersLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0,25,25,0));
            i++;
        }
        titleLayout2.addView(categoryLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        categoryLayout.setVisibility(GONE);
        titleLayout2.addView(new HtDividerCell(context, true), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0,15,0,0));
        titleLayout.addView(titleLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addView(titleLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

    }

    public void setError(boolean error, int position){
        if(error)
            iconValues[position].setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_inRedCall), PorterDuff.Mode.MULTIPLY));
        else
            iconValues[position].setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue2), PorterDuff.Mode.MULTIPLY));
    }
}

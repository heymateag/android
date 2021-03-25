package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.DialogsActivity;

import java.util.ArrayList;

public class HtFiltersCell extends LinearLayout {

    private String categorySelect = "All";
    private String subCategorySelect = "All";
    private int statusSelect = 0;
    private ArrayList<String> subCategories = new ArrayList<String>();


    public HtFiltersCell(@NonNull Context context, BaseFragment parent) {
        super(context);
        Object[] categories =  DummyCategories.categories.keySet().stream().sorted().toArray();
        for(Object category: categories){
            subCategories.addAll(DummyCategories.categories.get(category.toString()));
        }

        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);

        LinearLayout upperLayout = new LinearLayout(context);
        if(!(parent instanceof DialogsActivity)){
            Button statusFilter = new Button(context, LocaleController.getString("HtStatus", R.string.HtStatus)) {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    setAlpha(enabled ? 1.0f : 0.5f);
                }
            };
            statusFilter.setEnabled(true);
            statusFilter.setOnClickListener((v) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                String[] items = new String[5];
                int[] icons = new int[5];
                for (int i = 0; i < 5; i++) {
                    icons[i] = R.drawable.msg_arrowright;
                }
                items[0] = "All";
                items[1] = "Active";
                items[2] = "Drafted";
                items[3] = "Expired";
                items[4] = "Archived";
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        statusFilter.titleLabel.setText(items[which]);
                        statusFilter.titleLabel.setTextColor(context.getResources().getColor(R.color.ht_green));
                        if(parent instanceof OffersActivity) {
                            ((OffersActivity) parent).setStatusFilter(items[which]);
                        }
                    }
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            });
            upperLayout.addView(statusFilter, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.33f,0, 5, 0, 5));
        }

        Button categoryFilter = new Button(context, LocaleController.getString("HtCategory", R.string.HtCategory)) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 1.0f : 0.5f);
            }
        };
        Button subCategoryFilter = new Button(context, LocaleController.getString("HtSubCategory", R.string.HtSubCategory)) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 1.0f : 0.5f);
            }
        };;
        categoryFilter.setEnabled(true);
        categoryFilter.setOnClickListener((v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(LocaleController.getString("HtChooseCategory", R.string.HtChooseCategory));
            String[] items = new String[categories.length];
            items[0] = "All";
            int[] icons = new int[categories.length];
            for (int i = 1; i < categories.length; i++) {
                items[i] = categories[i - 1].toString();
                icons[i] = R.drawable.msg_arrowright;
            }
            builder.setNegativeButton(LocaleController.getString("HtCancel", R.string.HtCancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    categoryFilter.setText(items[which]);
                    categoryFilter.titleLabel.setTextColor(context.getResources().getColor(R.color.ht_green));
                    if(parent instanceof OffersActivity){
                        ((OffersActivity) parent).setCategoryFilter(items[which]);
                    }
                    subCategories.clear();
                    subCategories.addAll(DummyCategories.categories.get(items[which]));
                    subCategoryFilter.setText("All");
                }
            });
            AlertDialog alertDialog = builder.create();
            parent.showDialog(alertDialog);
        });

        subCategoryFilter.setEnabled(true);
        subCategoryFilter.setOnClickListener((v) -> {
            if(subCategories.size() == 0)
                return;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(LocaleController.getString("HtChooseCategory", R.string.HtChooseCategorySub));
            String[] items = new String[subCategories.size()];
            items[0] = "All";
            int[] icons = new int[subCategories.size()];
            for (int i = 1; i < subCategories.size(); i++) {
                items[i] = subCategories.get(i - 1);
                icons[i] = R.drawable.msg_arrowright;
            }
            builder.setNegativeButton(LocaleController.getString("HtCancel", R.string.HtCancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    subCategoryFilter.setText(items[which]);
                    subCategoryFilter.titleLabel.setTextColor(context.getResources().getColor(R.color.ht_green));
                    if(parent instanceof OffersActivity){
                        ((OffersActivity) parent).setSubCategoryFilter(items[which]);
                    }

                }
            });
            AlertDialog alertDialog = builder.create();
            parent.showDialog(alertDialog);
        });
        upperLayout.addView(categoryFilter, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,0.33f,0, 5, 0, 5));
        upperLayout.addView(subCategoryFilter, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.33f,0, 5, 0, 5));

        mainLayout.addView(upperLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addView(mainLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    static private class Button extends LinearLayout {
        public TextView titleLabel;

        public Button(Context context, String title) {
            super(context);
            titleLabel = new TextView(context);
            titleLabel.setText(title);
            titleLabel.setTextSize(17);
            titleLabel.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
            titleLabel.setCompoundDrawablePadding(AndroidUtilities.dp(6));
            Drawable titleDrawable = context.getResources().getDrawable(R.drawable.arrow_more);
            titleLabel.setScaleX(0.7f);
            titleLabel.setScaleY(0.7f);
            titleLabel.setPaintFlags(titleLabel.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            titleDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray), PorterDuff.Mode.MULTIPLY));
            titleLabel.setCompoundDrawablesWithIntrinsicBounds(null, null, titleDrawable, null);
            addView(titleLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 5, 0, 5));
        }

        public void setText(String text){
            titleLabel.setText(text.length() < 10 ? text : text.substring(0,10) + "...");
            titleLabel.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
        }
    }
}

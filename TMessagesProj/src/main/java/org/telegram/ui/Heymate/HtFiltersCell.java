package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.Heymate.log.LogToGroup;

import java.util.ArrayList;

public class HtFiltersCell extends LinearLayout {

    private String categorySelect = "All";
    private String subCategorySelect = "All";
    private int statusSelect = 0;
    private ArrayList<String> subCategories = new ArrayList<String>();

    private ArrayList<String> allSubCategories = new ArrayList<>();
    
    public HtFiltersCell(@NonNull Context context) {
        super(context);
    }

    public HtFiltersCell(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HtFiltersCell(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBaseFragment(BaseFragment parent) {
        setGravity(Gravity.CENTER_VERTICAL);
        final Context context = getContext();
        Object[] categories =  DummyCategories.categories.keySet().stream().sorted().toArray();
        for(Object category: categories){
            allSubCategories.addAll(DummyCategories.categories.get(category.toString()));
        }
        subCategories.addAll(allSubCategories);

        if(!(parent instanceof DialogsActivity)){
            Button statusFilter = new Button(context, LocaleController.getString("HtStatus", works.heymate.beta.R.string.HtStatus)) {
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
                    icons[i] = works.heymate.beta.R.drawable.msg_arrowright;
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
                        if (which == 0) {
                            statusFilter.titleLabel.setText(LocaleController.getString("HtStatus", works.heymate.beta.R.string.HtStatus));
                            statusFilter.titleLabel.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
                        }
                        else {
                            statusFilter.titleLabel.setText(items[which]);
                            statusFilter.titleLabel.setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
                        }

                        if(parent instanceof OffersActivity) {
                            ((OffersActivity) parent).setStatusFilter(which == 0 ? null : items[which]);
                        }
                    }
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            });
            addView(statusFilter, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.1f,10, 0, 0, 0));
        }

        Button categoryFilter = new Button(context, context.getString(works.heymate.beta.R.string.HtCategory)) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 1.0f : 0.5f);
            }
        };
        Button subCategoryFilter = new Button(context, context.getString(works.heymate.beta.R.string.HtSubCategory)) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 1.0f : 0.5f);
            }
        };
        categoryFilter.setEnabled(true);
        categoryFilter.setOnClickListener((v) -> {
            LogToGroup.logIfCrashed(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(LocaleController.getString("HtChooseCategory", works.heymate.beta.R.string.HtChooseCategory));
                String[] items = new String[categories.length];
                items[0] = "All";
                int[] icons = new int[categories.length];
                for (int i = 1; i < categories.length; i++) {
                    items[i] = categories[i - 1].toString();
                    icons[i] = works.heymate.beta.R.drawable.msg_arrowright;
                }
                builder.setNegativeButton(LocaleController.getString("HtCancel", works.heymate.beta.R.string.HtCancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        subCategories.clear();

                        if (which == 0) {
                            if(parent instanceof OffersActivity){
                                ((OffersActivity) parent).setCategoryFilter(null);
                                ((OffersActivity) parent).setSubCategoryFilter(null);
                            }

                            categoryFilter.setText("Category");
                            categoryFilter.titleLabel.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
                            subCategories.addAll(allSubCategories);
                            subCategoryFilter.setText(LocaleController.getString("HtSubCategory", works.heymate.beta.R.string.HtSubCategory));
                            subCategoryFilter.titleLabel.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
                            subCategorySelect = null;
                        }
                        else {
                            if(parent instanceof OffersActivity){
                                ((OffersActivity) parent).setCategoryFilter(items[which]);
                            }

                            categoryFilter.setText(items[which]);
                            categoryFilter.titleLabel.setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
                            subCategories.addAll(DummyCategories.categories.get(items[which]));
                            subCategoryFilter.setText("All");

                            if (!subCategories.contains(subCategorySelect)) {
                                subCategorySelect = null;
                                subCategoryFilter.setText(LocaleController.getString("HtSubCategory", works.heymate.beta.R.string.HtSubCategory));

                                if(parent instanceof OffersActivity){
                                    ((OffersActivity) parent).setSubCategoryFilter(null);
                                }
                            }
                        }
                    }
                });
                AlertDialog alertDialog = builder.create();
                parent.showDialog(alertDialog);
            });
        });

        subCategoryFilter.setEnabled(true);
        subCategoryFilter.setOnClickListener((v) -> {
            if(subCategories.size() == 0)
                return;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(LocaleController.getString("HtChooseCategory", works.heymate.beta.R.string.HtChooseCategorySub));
            String[] items = new String[subCategories.size()];
            items[0] = "All";
            int[] icons = new int[subCategories.size()];
            for (int i = 1; i < subCategories.size(); i++) {
                items[i] = subCategories.get(i - 1);
                icons[i] = works.heymate.beta.R.drawable.msg_arrowright;
            }
            builder.setNegativeButton(LocaleController.getString("HtCancel", works.heymate.beta.R.string.HtCancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    subCategoryFilter.setText(which == 0 ? context.getString(works.heymate.beta.R.string.HtSubCategory) : items[which]);
                    subCategoryFilter.titleLabel.setTextColor(which == 0 ? Theme.getColor(Theme.key_dialogTextGray) : context.getResources().getColor(works.heymate.beta.R.color.ht_green));
                    if(parent instanceof OffersActivity){
                        ((OffersActivity) parent).setSubCategoryFilter(which == 0 ? null : items[which]);
                    }

                }
            });
            AlertDialog alertDialog = builder.create();
            parent.showDialog(alertDialog);
        });
        addView(categoryFilter, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,0.1f,0, 0, 0, 0));
        addView(subCategoryFilter, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.1f,0, 0, 0, 0));

        if(parent instanceof DialogsActivity){
            TextView trendingText = new TextView(context);
            trendingText.setText("Trending");
            trendingText.setTextSize(13);
            trendingText.setTextColor(Theme.getColor(Theme.key_wallet_redText));
            trendingText.setCompoundDrawablePadding(AndroidUtilities.dp(6));
            Drawable trendingDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.ht_sort);
            trendingDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_wallet_redText), PorterDuff.Mode.MULTIPLY));
            trendingText.setCompoundDrawablesWithIntrinsicBounds(trendingDrawable, null,null, null);
            addView(trendingText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.1f,20, 0, 0, 0));
        }
    }

    static private class Button extends LinearLayout {
        public TextView titleLabel;

        public Button(Context context, String title) {
            super(context);
            titleLabel = new TextView(context);
            titleLabel.setText(title);
            titleLabel.setTextSize(13);
            titleLabel.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
            titleLabel.setCompoundDrawablePadding(AndroidUtilities.dp(6));
            Drawable titleDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.arrow_more);
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

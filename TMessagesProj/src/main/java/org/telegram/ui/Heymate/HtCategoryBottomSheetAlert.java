/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Heymate;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.annotation.Nullable;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SearchField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HtCategoryBottomSheetAlert extends BottomSheet implements NotificationCenter.NotificationCenterDelegate, BottomSheet.BottomSheetDelegateInterface {

    private BaseFragment parent;
    private LinearLayout categoryLayout;
    private LinearLayout subCategoryLayout;

    public HtCategoryBottomSheetAlert(Context context, boolean needFocus, BaseFragment parent) {
        super(context, needFocus);
        this.parent = parent;
        initSheet(context);
    }

    public HtCategoryBottomSheetAlert(Context context, boolean needFocus) {
        super(context, needFocus);
        initSheet(context);
    }

    public void initSheet(Context context){
        setDisableScroll(true);
        containerView = new LinearLayout(context);

        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setMinimumHeight(700);
        mainLayout.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        mainLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(18), Theme.getColor(Theme.key_actionBarDefault)));

        LinearLayout upperLayout = new LinearLayout(context);
        upperLayout.setGravity(Gravity.RIGHT);

        ImageView applyImage = new ImageView(context);
        Drawable applyDrawable = context.getResources().getDrawable(R.drawable.ht_check_circle);
        applyDrawable.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.ht_green), PorterDuff.Mode.MULTIPLY));
        applyImage.setImageDrawable(applyDrawable);
        applyImage.setEnabled(true);
        applyImage.setHovered(true);
        applyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        upperLayout.addView(applyImage, LayoutHelper.createLinear(45, 45, 15,35,15,15));

        mainLayout.addView(upperLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout mainLayout2 = new LinearLayout(context);

        LinearLayout categoryMainLayout = new LinearLayout(context);
        categoryMainLayout.setOrientation(LinearLayout.VERTICAL);

        ScrollView categoryScroll = new ScrollView(context);

        categoryLayout = new LinearLayout(context);

        categoryLayout.setOrientation(LinearLayout.VERTICAL);

        final ArrayList<HtTextCell>[] categories = new ArrayList[]{new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
        final ArrayList<HtTextCell>[] subCategories = new ArrayList[]{new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
        final HashMap<String, ArrayList<HtTextCell>> subCategoriesBase = new HashMap<>();

        SearchField categorySearch = new SearchField(context){
            @Override
            public void onTextChange(String text) {
                super.onTextChange(text);
                if(text.length() > 2) {
                    categories[1].clear();
                    for(HtTextCell category : categories[2]){
                        if(category.getText().toLowerCase().contains(text.toLowerCase())){
                            categories[1].add(category);
                        }
                    }
                    updateCategories(categories[0], categories[1]);
                    categories[0].clear();
                    categories[0].addAll(categories[1]);
                }
            }
        };
        categorySearch.setHint(LocaleController.getString("HtCategory", R.string.HtCategory));
        categoryMainLayout.addView(categorySearch, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 3,10,3,10));

        final HtTextCell[] prev = {null};
        Object[] categoryStore = DummyCategories.categories.keySet().stream().sorted().toArray();
        ArrayList<String> subCategoriesStore = new ArrayList<String>();
        ArrayList<String> subCategoriesRootStore = new ArrayList<String>();
        for(Object cat : categoryStore){
            for(Object sub : DummyCategories.categories.get(cat.toString())){
                subCategoriesStore.add(sub.toString());
                subCategoriesRootStore.add(cat.toString());
            }
        }

        for(int i = 0;i < categoryStore.length; i++){
            subCategoriesBase.put(categoryStore[i].toString(), new ArrayList<>());
            HtTextCell categoryCell = new HtTextCell(context);
            categoryCell.setEnabled(true);
            categoryCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(prev[0] != null)
                        prev[0].unSelect();
                    categoryCell.select();
                    prev[0] = categoryCell;
                    if(parent instanceof HtCreateOfferActivity){
                        ((HtCreateOfferActivity) parent).setCategory(categoryCell.getText());
                        ((HtCreateOfferActivity) parent).setSubCategory("All");
                    }
                    updateSubCategories(subCategories[2], subCategoriesBase.get(categoryCell.getText()));
                }
            });
            categoryCell.setTextColor(context.getResources().getColor(R.color.ht_green));
            categoryCell.setText(categoryStore[i].toString());
            categories[2].add(categoryCell);
        }
        updateCategories(categories[1], categories[2]);

        categoryScroll.addView(categoryLayout);
        categoryMainLayout.addView(categoryScroll);
        mainLayout2.addView(categoryMainLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f));

        LinearLayout subCategoryMainLayout = new LinearLayout(context);
        subCategoryMainLayout.setOrientation(LinearLayout.VERTICAL);
        ScrollView subCategoryScroll = new ScrollView(context);
        subCategoryLayout = new LinearLayout(context);
        subCategoryLayout.setOrientation(LinearLayout.VERTICAL);
        SearchField subCategorySearch = new SearchField(context){
            @Override
            public void onTextChange(String text) {
                super.onTextChange(text);
                if(text.length() > 2) {
                    subCategories[1].clear();
                    for(HtTextCell category : subCategories[2]){
                        if(category.getText().toLowerCase().contains(text.toLowerCase())){
                            subCategories[1].add(category);
                        }
                    }
                    updateSubCategories(subCategories[0], subCategories[1]);
                    subCategories[0].clear();
                    subCategories[0].addAll(subCategories[1]);
                }
            }
        };;
        subCategorySearch.setHint(LocaleController.getString("HtSubCategory", R.string.HtSubCategory));
        final HtTextCell[] previousSelectedSubCategory = {null};
        subCategoryMainLayout.addView(subCategorySearch, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 3,10,3,10));

        for(int i = 0;i < subCategoriesStore.size(); i++){
            HtTextCell subCategoryCell = new HtTextCell(context);
            subCategoriesBase.get(subCategoriesRootStore.get(i)).add(subCategoryCell);
            subCategoryCell.setEnabled(true);
            subCategoryCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(previousSelectedSubCategory[0] != null)
                        previousSelectedSubCategory[0].unSelect();
                    subCategoryCell.select();
                    previousSelectedSubCategory[0] = subCategoryCell;
                    if(parent instanceof HtCreateOfferActivity){
                        ((HtCreateOfferActivity) parent).setSubCategory(subCategoryCell.getText());
                    }
                    hide();
                }
            });
            subCategoryCell.setTextColor(context.getResources().getColor(R.color.ht_green));
            subCategoryCell.setText(subCategoriesStore.get(i));
            subCategories[2].add(subCategoryCell);
        }
        updateSubCategories(subCategories[1], subCategories[2]);
        subCategoryScroll.addView(subCategoryLayout);
        subCategoryMainLayout.addView(subCategoryScroll);
        mainLayout2.addView(subCategoryMainLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0.5f));
        mainLayout.addView(mainLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        containerView.addView(mainLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 700));
    }

    @Override
    public void onOpenAnimationStart() {

    }

    @Override
    public void onOpenAnimationEnd() {

    }

    @Override
    public boolean canDismiss() {
        return false;
    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

    }

    public void updateCategories(ArrayList<HtTextCell> oldCategories, ArrayList<HtTextCell> categories){
        for(HtTextCell category : oldCategories){
            ObjectAnimator anim1 = ObjectAnimator.ofFloat(category, "scaleY", 1f, 0f);
            anim1.setDuration(180);
            anim1.start();
        }
        categoryLayout.removeAllViews();
        for(HtTextCell category : categories){
            categoryLayout.addView(category, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,2,2,2,2));
            ObjectAnimator anim1 = ObjectAnimator.ofFloat(category, "scaleY", 0f, 1f);
            anim1.setDuration(180);
            anim1.start();
        }
    }

    public void updateSubCategories(ArrayList<HtTextCell> oldSubCategories, ArrayList<HtTextCell> subCategories){
        for(HtTextCell category : oldSubCategories){
            ObjectAnimator anim1 = ObjectAnimator.ofFloat(category, "scaleY", 1f, 0f);
            anim1.setDuration(180);
            anim1.start();
        }
        subCategoryLayout.removeAllViews();
        for(HtTextCell category : subCategories){
            subCategoryLayout.addView(category, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,2,2,2,2));
            ObjectAnimator anim1 = ObjectAnimator.ofFloat(category, "scaleY", 0f, 1f);
            anim1.setDuration(180);
            anim1.start();
        }
    }

}

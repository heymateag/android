package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

public class HtOfferHelperActivity extends BaseFragment {

    private View searchButton;
    private ImageView nextBtn;



    private final static int search_button = 1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View createView(Context context) {

        Configuration configuration = context.getResources().getConfiguration();
        int dpWidth = configuration.screenWidthDp;
        int dpHeight = configuration.screenHeightDp;
        nextBtn = new ImageView(context);
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_profile_actionBackground), Theme.getColor(Theme.key_profile_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }
        nextBtn.setBackgroundDrawable(drawable);
        nextBtn.setImageResource(R.drawable.ic_ab_back);
        nextBtn.setRotation(180f);
        nextBtn.setContentDescription(LocaleController.getString("AccDescrOpenChat", R.string.AccDescrOpenChat));
        nextBtn.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_profile_actionIcon), PorterDuff.Mode.MULTIPLY));
        nextBtn.setScaleType(ImageView.ScaleType.CENTER);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setSearchTextColor(0xff4488, true);
        actionBar.setTitle(LocaleController.getString("HtCreateOffer", R.string.HtCreateOffer));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        searchButton = menu.addItemWithWidth(search_button, R.drawable.menu_search, AndroidUtilities.dp(56));
        searchButton.setContentDescription(LocaleController.getString("Search", R.string.Search));

        fragmentView = new LinearLayout(context);
        LinearLayout mainLayout = (LinearLayout) fragmentView;
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        ImageView helperImage = new ImageView(context);
        helperImage.setScaleType(ImageView.ScaleType.FIT_XY);
        Drawable helperDrawable = context.getResources().getDrawable(R.drawable.ht_helper);
        helperImage.setImageDrawable(helperDrawable);
        mainLayout.addView(helperImage, LayoutHelper.createLinear(270,320));

        TextView text1 = new TextView(context);
        text1.setText(LocaleController.getString("HtWhatAnOffer", R.string.HtWhatAnOffer));
        text1.setTextSize(19);
        text1.setTypeface(text1.getTypeface(), Typeface.BOLD);
        text1.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        mainLayout.addView(text1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 15,17,15,7));

        TextView text2 = new TextView(context);
        text2.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit,\nsed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
        text2.setMinLines(2);
        text2.setTextSize(14);
        text2.setTextColor(Theme.getColor(Theme.key_wallet_grayText2));
        mainLayout.addView(text2, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 15,17,15,17));

        LinearLayout buyLayout = new LinearLayout(context) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 1.0f : 0.5f);
            }
        };
        buyLayout.setGravity(Gravity.CENTER);

        TextView buyLabel = new TextView(context);
        buyLabel.setText(LocaleController.getString("HtLetsGo", R.string.HtLetsGo));
        buyLabel.setTextSize(16);
        buyLayout.setBackgroundColor(context.getResources().getColor(R.color.ht_green));
        buyLabel.setTypeface(buyLabel.getTypeface(), Typeface.BOLD);
        buyLabel.setTextColor(Theme.getColor(Theme.key_wallet_whiteBackground));
        buyLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(5), context.getResources().getColor(R.color.ht_green)));
        buyLayout.addView(buyLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 15,10,15,10));
        buyLayout.setEnabled(true);
        buyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentFragment(new OffersActivity(context));
                finishFragment();
            }
        });
        mainLayout.addView(buyLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        return fragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();
    }
}
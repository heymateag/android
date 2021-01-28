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
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.android.material.snackbar.BaseTransientBottomBar;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

public class OffersActivity extends BaseFragment {

    private TextView offersFilters1;
    private TextView offersFilters2;
    private TextView offersFilters21;
    private TextView offersFilters22;
    private OfferController offerController = OfferController.getInstance();
    private LinearLayout offersLayout;

    private final static int search_button = 1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View createView(Context context) {

        Configuration configuration = context.getResources().getConfiguration();
        int dpWidth = configuration.screenWidthDp;
        int dpHeight = configuration.screenHeightDp;

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setSearchTextColor(0xff4488, true);
        actionBar.setTitle("Manage Offers");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == search_button) {
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        fragmentView = new LinearLayout(context);
        LinearLayout linearLayout = (LinearLayout) fragmentView;
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout relativeLayout2 = new LinearLayout(context);
        relativeLayout2.setOrientation(LinearLayout.VERTICAL);
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        fragmentView.setOnTouchListener((v, event) -> true);


        FrameLayout fieldContainer = new FrameLayout(context);
        linearLayout2.addView(fieldContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 24, 24, 20, 0));

        LinearLayout offerCreationLayout = new LinearLayout(context);
        FrameLayout offerCreationButtonFrame = new FrameLayout(context);
        LinearLayout offerCreationButtonLayout = new LinearLayout(context);
        offerCreationLayout.setOrientation(LinearLayout.VERTICAL);
        Drawable offerCreationButtonDrawable = context.getResources().getDrawable(R.drawable.plus);
        offerCreationButtonDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue), PorterDuff.Mode.MULTIPLY));
        ImageView offerCreationDrawableView = new ImageView(context);
        offerCreationDrawableView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        offerCreationDrawableView.setImageDrawable(offerCreationButtonDrawable);
        offerCreationButtonLayout.addView(offerCreationDrawableView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 50, 17, 0, 0));
        TextView offerCreationButton = new TextView(context) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setAlpha(enabled ? 1.0f : 0.5f);
            }

            @Override
            public void setTextColor(int color) {
                super.setTextColor(color);
                setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(color));
            }
        };
        offerCreationButton.setText("New Offer");
        offerCreationButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        offerCreationButton.setTypeface(offerCreationButton.getTypeface(), Typeface.BOLD);
        offerCreationButton.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        offerCreationButton.setEnabled(true);
        offerCreationButton.setHovered(true);
        offerCreationButton.setElevation(6.0f);
        offerCreationButton.setGravity(Gravity.CENTER);
        offerCreationButton.setOnClickListener((v) -> presentFragment(new CreateOfferActivityPage1()));
        offerCreationButtonLayout.addView(offerCreationButton,LayoutHelper.createLinear(200, 50, Gravity.CENTER, -50, 0, 0, 0));
        offerCreationButtonFrame.addView(offerCreationButtonLayout,LayoutHelper.createLinear(200, 50, Gravity.CENTER, 0, 0, 0, 0));
        offerCreationLayout.addView(offerCreationButtonFrame,LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 0, 0, 0));
        offerCreationLayout.addView(new HtDividerCell(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 20, Gravity.LEFT, 0, 20, 0, 0));
        relativeLayout2.addView(offerCreationLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT,0 , 20, 0, 0));

        ScrollView scrollView = new ScrollView(context);
        LinearLayout scrollviewLayout = new LinearLayout(context);
        scrollviewLayout.setOrientation(LinearLayout.VERTICAL);
        scrollviewLayout.addView(new OfferFiltersLayout(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.START, 0,20, 0, 20));
        scrollviewLayout.addView(new HtDividerCell(context));

        // ------------ DATABASE DEMO ----------------
        ArrayList<OfferDto> offers = offerController.getAllOffers();
        // --------------------------------------------


        offersLayout = new LinearLayout(context);
        offersLayout.setOrientation(LinearLayout.VERTICAL);
        for(OfferDto offerDto : offers){
            OfferCell offerCell1 = new OfferCell(context, this, offerDto)  {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    setAlpha(enabled ? 1.0f : 0.5f);
                    setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(Theme.getColor(Theme.key_dialogTextGray)));
                }
            };;
            offerCell1.setEnabled(true);
            offerCell1.setHovered(true);
            offerCell1.setOnClickListener((v) -> {
                offerCell1.onClick();
            });
            offersLayout.addView(offerCell1);
        }
        scrollviewLayout.addView(offersLayout);

        scrollView.addView(scrollviewLayout);
        relativeLayout2.addView(scrollView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.START, 0,0, 0, 0));
        relativeLayout2.addView(new HtDividerCell(context), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 20, Gravity.BOTTOM, 0, 20, 0, 20));


        LinearLayout viewOffersLayout = new LinearLayout(context);
        viewOffersLayout.setOrientation(LinearLayout.VERTICAL);

        relativeLayout2.addView(new HtDividerCell(context), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 45, Gravity.BOTTOM, 0, dpHeight - 70 - 55, 0, 0));

        linearLayout.addView(relativeLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.END, 0, 0, 0, 0));
        return fragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public LinearLayout getOffersLayout(){
        return offersLayout;
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        return themeDescriptions;
    }

    private class OfferFiltersLayout extends LinearLayout{
        private Context context;

        public OfferFiltersLayout(Context context){
            super(context);
            this.context = context;
            TextView offersFilterTitle = new TextView(context);
            offersFilterTitle.setText("Filters");
            offersFilterTitle.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
            offersFilterTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            Drawable offersFilterTitleDrawable = context.getResources().getDrawable(R.drawable.menu_newfilter);
            offersFilterTitleDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_inGreenCall), PorterDuff.Mode.MULTIPLY));
            offersFilterTitle.setCompoundDrawablePadding(AndroidUtilities.dp(4));
            offersFilterTitle.setCompoundDrawablesWithIntrinsicBounds(offersFilterTitleDrawable, null, null, null);
            offersFilterTitle.setGravity(Gravity.START);
            this.addView(offersFilterTitle, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START,20, 20, 0, 20));
            LinearLayout offersFiltersLayout = new LinearLayout(context);
            offersFiltersLayout.setOrientation(LinearLayout.VERTICAL);
            FrameLayout offersFilters1Frame = new FrameLayout(context);
            offersFilters1 = new TextView(context) {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    setAlpha(enabled ? 1.0f : 0.5f);
                }

                @Override
                public void setTextColor(int color) {
                    super.setTextColor(color);
                    setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(color));
                }
            };
            offersFilters1.setText("Active");
            offersFilters1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            offersFilters1.setTypeface(offersFilters1.getTypeface(), Typeface.BOLD);
            offersFilters1.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
            offersFilters1.setEnabled(true);
            offersFilters1.setHovered(true);
            offersFilters1.setElevation(2.0f);
            offersFilters1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            offersFilters1.setOnClickListener((v) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle("Filter #1");
                String[] items = new String[2];
                int[] icons = new int[2];
                for (int i = 0; i < 2; i++) {
                    icons[i] = R.drawable.msg_arrowright;
                }
                items[0] = "Active";
                items[1] = "All";
                builder.setSubtitle("Current Filter: Active");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        offersFilters1.setText(items[which]);
                    }
                });
                AlertDialog alertDialog = builder.create();
                showDialog(alertDialog);
            });
            offersFilters1Frame.addView(offersFilters1,LayoutHelper.createLinear(100, 35, 0, 0, 0, 0));
            offersFiltersLayout.addView(offersFilters1Frame,LayoutHelper.createLinear(100, 35, Gravity.TOP | Gravity.RIGHT, 10, 10, 10, 10));
            FrameLayout offersFilters2Frame = new FrameLayout(context);
            offersFilters2 = new TextView(context) {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    setAlpha(enabled ? 1.0f : 0.5f);
                }

                @Override
                public void setTextColor(int color) {
                    super.setTextColor(color);
                    setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(color));
                }
            };
            offersFilters2.setText("Filter #2");
            offersFilters2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            offersFilters2.setTypeface(offersFilters2.getTypeface(), Typeface.BOLD);
            offersFilters2.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
            offersFilters2.setEnabled(true);
            offersFilters2.setHovered(true);
            offersFilters2.setElevation(2.0f);
            offersFilters2.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            offersFilters2.setOnClickListener((v) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle("Filter #2");
                String[] items = new String[2];
                int[] icons = new int[2];
                for (int i = 0; i < 2; i++) {
                    icons[i] = R.drawable.msg_arrowright;
                }
                items[0] = "Active";
                items[1] = "All";
                builder.setSubtitle("Current Filter: Active");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        offersFilters2.setText(items[which]);
                    }
                });
                AlertDialog alertDialog = builder.create();
                showDialog(alertDialog);
            });
            offersFilters2Frame.addView(offersFilters2,LayoutHelper.createLinear(100, 35, 0, 0, 0, 0));
            offersFiltersLayout.addView(offersFilters2Frame,LayoutHelper.createLinear(100, 35, Gravity.CENTER_VERTICAL | Gravity.RIGHT, 10, 10, 10, 10));
            offersFiltersLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            this.addView(offersFiltersLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 30,40, 0, 20));

            LinearLayout offersFilters2Layout = new LinearLayout(context);
            offersFilters2Layout.setOrientation(LinearLayout.VERTICAL);
            FrameLayout offersFilters21Frame = new FrameLayout(context);
            offersFilters21 = new TextView(context) {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    setAlpha(enabled ? 1.0f : 0.5f);
                }

                @Override
                public void setTextColor(int color) {
                    super.setTextColor(color);
                    setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(color));
                }
            };
            offersFilters21.setText("Filter #3");
            offersFilters21.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            offersFilters21.setTypeface(offersFilters21.getTypeface(), Typeface.BOLD);
            offersFilters21.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
            offersFilters21.setEnabled(true);
            offersFilters21.setHovered(true);
            offersFilters21.setElevation(2.0f);
            offersFilters21.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            offersFilters21.setOnClickListener((v) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle("Filter #3");
                String[] items = new String[2];
                int[] icons = new int[2];
                for (int i = 0; i < 2; i++) {
                    icons[i] = R.drawable.msg_arrowright;
                }
                items[0] = "Active";
                items[1] = "All";
                builder.setSubtitle("Current Filter: Active");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        offersFilters21.setText(items[which]);
                    }
                });
                AlertDialog alertDialog = builder.create();
                showDialog(alertDialog);
            });
            offersFilters21Frame.addView(offersFilters21,LayoutHelper.createLinear(100, 35, 0, 0, 0, 0));
            offersFilters2Layout.addView(offersFilters21Frame,LayoutHelper.createLinear(100, 35, Gravity.TOP | Gravity.RIGHT, 10, 10, 10, 10));

            FrameLayout offersFilters22Frame = new FrameLayout(context);
            offersFilters22 = new TextView(context) {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    setAlpha(enabled ? 1.0f : 0.5f);
                }

                @Override
                public void setTextColor(int color) {
                    super.setTextColor(color);
                    setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(color));
                }
            };
            offersFilters22.setText("Filter #4");
            offersFilters22.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            offersFilters22.setTypeface(offersFilters22.getTypeface(), Typeface.BOLD);
            offersFilters22.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
            offersFilters22.setEnabled(true);
            offersFilters22.setHovered(true);
            offersFilters22.setElevation(2.0f);
            offersFilters22.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            offersFilters22.setOnClickListener((v) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle("Filter #4");
                String[] items = new String[2];
                int[] icons = new int[2];
                for (int i = 0; i < 2; i++) {
                    icons[i] = R.drawable.msg_arrowright;
                }
                items[0] = "Active";
                items[1] = "All";
                builder.setSubtitle("Current Filter: Active");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        offersFilters22.setText(items[which]);
                    }
                });
                AlertDialog alertDialog = builder.create();
                showDialog(alertDialog);
            });
            offersFilters22Frame.addView(offersFilters22,LayoutHelper.createLinear(100, 35, 0, 0, 0, 0));
            offersFilters2Layout.addView(offersFilters22Frame,LayoutHelper.createLinear(100, 35, Gravity.CENTER_VERTICAL | Gravity.RIGHT, 10, 10, 10, 10));

            offersFiltersLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            this.addView(offersFilters2Layout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0,40, 0, 20));
        }
    }
}



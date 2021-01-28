/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.maps.GoogleMap;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.Intro;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.DrawerManageOffersCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Cells.WallpaperCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BottomPagesView;
import org.telegram.ui.Components.CodepointsLengthInputFilter;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.Paint.Views.EntityView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.TableLayout;
import org.telegram.ui.IntroActivity;
import org.telegram.ui.LocationActivity;
import org.telegram.ui.NotificationsCustomSettingsActivity;
import org.telegram.ui.NotificationsSettingsActivity;
import org.telegram.ui.WallpapersListActivity;

import java.util.ArrayList;
import java.util.Calendar;

import static org.telegram.messenger.AndroidUtilities.displayMetrics;

public class CreateOfferActivityPage1 extends BaseFragment {

    private EditTextBoldCursor firstNameField;
    private EditTextBoldCursor rateTextField;
    private EditTextBoldCursor locationTextField;
    private EditTextBoldCursor availablityTextField;
    private View searchButton;
    private NextButtonCell nextBtn;
    private ViewPager viewPager;
    private BottomPagesView bottomPages;
    private TextView categoryTextMain;
    private TextView categoryTextSub;
    private TextView rateSelectText;
    private TextView rateSymbolSelect;


    private final static int search_button = 1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View createView(Context context) {

        Configuration configuration = context.getResources().getConfiguration();
        int dpWidth = configuration.screenWidthDp;
        int dpHeight = configuration.screenHeightDp;

        nextBtn = new NextButtonCell(context);
        nextBtn.setEnabled(true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setSearchTextColor(0xff4488, true);
        actionBar.setTitle(LocaleController.getString("CreateOffer", R.string.CreateOffer));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == search_button) {
                    saveName();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        searchButton = menu.addItemWithWidth(search_button, R.drawable.menu_search, AndroidUtilities.dp(56));
        searchButton.setContentDescription(LocaleController.getString("Search", R.string.Search));
        fragmentView = new LinearLayout(context);
        firstNameField = new EditTextBoldCursor(context);
        firstNameField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        firstNameField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        firstNameField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        firstNameField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        firstNameField.setMaxLines(4);
        firstNameField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
        firstNameField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        firstNameField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        firstNameField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        firstNameField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new CodepointsLengthInputFilter(250) {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source != null && TextUtils.indexOf(source, '\n') != -1) {
//                    searchButton.performClick();
                    return "";
                }
                CharSequence result = super.filter(source, start, end, dest, dstart, dend);
                if (result != null && source != null && result.length() != source.length()) {
                    Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    if (v != null) {
                        v.vibrate(200);
                    }
                }
                return result;
            }
        };
        firstNameField.setFilters(inputFilters);
        firstNameField.setMinHeight(AndroidUtilities.dp(36));
        firstNameField.setHint(LocaleController.getString("HtShortTitle", R.string.HtShortTitle));
        firstNameField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        firstNameField.setCursorSize(AndroidUtilities.dp(15));
        firstNameField.setCursorWidth(1.5f);
        firstNameField.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE && searchButton != null) {
//                searchButton.performClick();
                return true;
            }
            return false;
        });
        firstNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        LinearLayout linearLayout = (LinearLayout) fragmentView;
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout relativeLayout2 = new RelativeLayout(context);
        ScrollView scrollView = new ScrollView(context);
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        fragmentView.setOnTouchListener((v, event) -> true);

        FrameLayout fieldContainer = new FrameLayout(context);
        linearLayout2.addView(fieldContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 24, 24, 20, 0));


        nextBtn.setElevation(2.0f);
        nextBtn.setHovered(true);

        LinearLayout linearLayout1 = new LinearLayout(context);
        TextView text1 = new TextView(context);
        text1.setText("Offer title");
        text1.setTypeface(text1.getTypeface(), Typeface.BOLD);
        text1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        text1.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
        linearLayout1.addView(text1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 15, 20, 20));
        linearLayout1.addView(firstNameField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 60, 0, 60, 40));
        linearLayout1.addView(new HtDividerCell(context, dpHeight));
        linearLayout1.setOrientation(LinearLayout.VERTICAL);
        TextView text2 = new TextView(context);
        text2.setText("Category");
        text2.setTypeface(text2.getTypeface(), Typeface.BOLD);
        text2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        text2.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
        linearLayout1.addView(text2, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 15, 20, 0));

        LinearLayout linearLayout12 = new LinearLayout(context);
        linearLayout12.setOrientation(LinearLayout.VERTICAL);
        categoryTextMain = new TextView(context);
        categoryTextMain.setText("Main Category");
        categoryTextMain.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        categoryTextMain.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        categoryTextMain.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        categoryTextMain.setLines(1);
        categoryTextMain.setMaxLines(1);
        categoryTextMain.setSingleLine(true);
        categoryTextMain.setCompoundDrawablePadding(AndroidUtilities.dp(34));
        categoryTextMain.setEnabled(true);
        categoryTextMain.setHovered(true);
        categoryTextMain.setOnClickListener((v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("HtChooseCategory", R.string.HtChooseCategory));
            String[] items = new String[20];
            int[] icons = new int[20];
            for (int i = 1; i < 20; i++) {
                items[i] = "Category" + i;
                icons[i] = R.drawable.msg_arrowright;
            }
            builder.setSubtitle("Current Category: Default");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    categoryTextMain.setText(items[which]);
                }
            });
            AlertDialog alertDialog = builder.create();
            showDialog(alertDialog);
        });

        linearLayout12.addView(categoryTextMain, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, (int) (dpWidth / 2 - 50), 25, 0, 0));

        categoryTextSub = new TextView(context);
        categoryTextSub.setText("Sub Category");
        categoryTextSub.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        categoryTextSub.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        categoryTextSub.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        categoryTextSub.setLines(1);
        categoryTextSub.setMaxLines(1);
        categoryTextSub.setSingleLine(true);
        categoryTextSub.setCompoundDrawablePadding(AndroidUtilities.dp(34));
        categoryTextSub.setEnabled(true);
        categoryTextSub.setHovered(true);
        categoryTextSub.setOnClickListener((v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("HtChooseCategorySub", R.string.HtChooseCategorySub));
            AlertDialog alertDialog = builder.create();
            String[] subItems = new String[20];
            int[] icons = new int[20];

            for (int i = 1; i < 20; i++) {
                subItems[i] = "Sub Category" + i;
                alertDialog.setItemColor(i, Theme.getColor(Theme.key_dialogTextBlue), i);
                icons[i] = R.drawable.msg_arrowright;
            }
            builder.setSubtitle("Current Sub Category: Default");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setItems(subItems, icons, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    categoryTextSub.setText(subItems[which]);
                }
            });
            showDialog(alertDialog);
        });

        linearLayout12.addView(categoryTextSub, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, (int) (dpWidth / 2 - 50), 25, 0, 20));
        linearLayout1.addView(linearLayout12);
        linearLayout1.addView(new HtDividerCell(context, dpHeight));
        linearLayout2.addView(linearLayout1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 0, 0, 0));

        LinearLayout linearLayout3 = new LinearLayout(context);
        linearLayout3.setOrientation(LinearLayout.VERTICAL);
        TextView text22 = new TextView(context);
        text22.setText("Rate");
        text22.setTypeface(text2.getTypeface(), Typeface.BOLD);
        text22.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        text22.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
        linearLayout3.addView(text22, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 15, 20, 0));

        rateSelectText = new TextView(context);
        rateSelectText.setText("Select Rate");
        rateSelectText.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        rateSelectText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        rateSelectText.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        rateSelectText.setLines(1);
        rateSelectText.setMaxLines(1);
        rateSelectText.setSingleLine(true);
        rateSelectText.setEnabled(true);
        rateSelectText.setHovered(true);
        rateSelectText.setOnClickListener((v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("HtRate", R.string.HtRate));
            String[] subItems = new String[3];
            int[] icons = new int[3];

            for (int i = 0; i < 3; i++) {
                icons[i] = R.drawable.msg_arrowright;
            }
            subItems[0] = "Per Item";
            subItems[1] = "Per Hour";
            subItems[2] = "Range";
            builder.setSubtitle("Current Rate: $");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setItems(subItems, icons, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    rateSelectText.setText(subItems[which]);
                }
            });
            AlertDialog alertDialog = builder.create();
            showDialog(alertDialog);
        });

        linearLayout3.addView(rateSelectText, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, (int) (dpWidth / 2 - 50), 15, 0, 15));

        rateTextField = new EditTextBoldCursor(context);
        rateTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        rateTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        rateTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        rateTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        rateTextField.setMaxLines(4);
        rateTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
        rateTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        rateTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        rateTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        rateTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        rateTextField.setMinHeight(AndroidUtilities.dp(36));
        rateTextField.setHint(LocaleController.getString("HtRate", R.string.HtRate));
        rateTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        rateTextField.setCursorSize(AndroidUtilities.dp(15));
        rateTextField.setCursorWidth(1.5f);
        linearLayout3.addView(rateTextField, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, (int) (dpWidth / 2 - 50), 10, 0, 0));

        rateSymbolSelect = new TextView(context);
        rateSymbolSelect.setText("Select Currency");
        rateSymbolSelect.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        rateSymbolSelect.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        rateSymbolSelect.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        rateSymbolSelect.setLines(1);
        rateSymbolSelect.setMaxLines(1);
        rateSymbolSelect.setSingleLine(true);
        rateSymbolSelect.setEnabled(true);
        rateSymbolSelect.setHovered(true);
        rateSymbolSelect.setOnClickListener((v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("HtRateSymbol", R.string.HtRateSymbol));
            String[] subItems = new String[3];
            int[] icons = new int[3];

            for (int i = 0; i < 3; i++) {
                icons[i] = R.drawable.msg_arrowright;
            }
            subItems[0] = "R$";
            subItems[1] = "US$";
            subItems[2] = "CA$";
            builder.setSubtitle("Current Rate Symbol: R$");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setItems(subItems, icons, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    rateSymbolSelect.setText(subItems[which]);
                }
            });
            AlertDialog alertDialog = builder.create();
            showDialog(alertDialog);
        });

        linearLayout3.addView(rateSymbolSelect, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL, (int) (dpWidth / 2 - 50), 25, 0, 15));


        linearLayout2.addView(linearLayout3, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, (int) (dpHeight * 0.3), Gravity.LEFT, 0, 0, 0, 0));
        linearLayout2.addView(new HtDividerCell(context, dpHeight));

        LinearLayout linearLayout4 = new LinearLayout(context);
        TextView text44 = new TextView(context);
        text44.setText("Location");
        text44.setTypeface(text2.getTypeface(), Typeface.BOLD);
        text44.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        text44.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
        linearLayout4.addView(text44, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 15, 20, 0));

        linearLayout2.addView(linearLayout4,LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 20, 0, 0));

        LinearLayout linearLayout5 = new LinearLayout(context);
        TextView text33 = new TextView(context);
        text33.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        Drawable drawable3 = context.getResources().getDrawable(R.drawable.google_maps_logo);
        Bitmap b = ((BitmapDrawable)drawable3).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 345, 150, false);
        BitmapDrawable gdrawable = new BitmapDrawable(context.getResources(), bitmapResized);
        gdrawable.setBounds(0,345,150,0);
        text33.setEnabled(true);
        text33.setHovered(true);
        text33.setCompoundDrawablesWithIntrinsicBounds(null, null, gdrawable, null);
        text33.setOnClickListener((v) -> {
            presentFragment(new LocationActivity(LocationActivity.LOCATION_TYPE_LIVE_VIEW));
        });
        linearLayout5.addView(text33, LayoutHelper.createLinear(150, 100, Gravity.LEFT, 20, -130, 0, 0));
        locationTextField = new EditTextBoldCursor(context);
        locationTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        locationTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        locationTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        locationTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        locationTextField.setMaxLines(8);
        locationTextField.setMinLines(4);
        locationTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
        locationTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        locationTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        locationTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        locationTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        locationTextField.setMinHeight(AndroidUtilities.dp(36));
        locationTextField.setHint(LocaleController.getString("HtLocation", R.string.HtLocation));
        locationTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        locationTextField.setCursorSize(AndroidUtilities.dp(15));
        locationTextField.setCursorWidth(1.5f);
        linearLayout5.addView(locationTextField, LayoutHelper.createFrame(200, 90, Gravity.CENTER, -20, 0, 40, 0));
        linearLayout2.addView(linearLayout5, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 100, Gravity.LEFT | Gravity.CENTER_VERTICAL, 0, 20, 0, 20));
        linearLayout2.addView(new HtDividerCell(context, dpHeight));


        LinearLayout linearLayout44 = new LinearLayout(context);
        TextView text55 = new TextView(context);
        text55.setText("Time");
        text55.setTypeface(text55.getTypeface(), Typeface.BOLD);
        text55.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        text55.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
        linearLayout44.addView(text55, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 15, 20, 0));

        LinearLayout linearLayout6 = new LinearLayout(context);
        linearLayout2.addView(linearLayout44, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 30, 20, 30, 0));

        availablityTextField = new EditTextBoldCursor(context);
        availablityTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        availablityTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        availablityTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        availablityTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        availablityTextField.setMaxLines(8);
        availablityTextField.setMinLines(4);
        availablityTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
        availablityTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        availablityTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        availablityTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        availablityTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        availablityTextField.setMinHeight(AndroidUtilities.dp(36));
        availablityTextField.setHint(LocaleController.getString("HtAvailability", R.string.HtAvailability));
        availablityTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        availablityTextField.setCursorSize(AndroidUtilities.dp(15));
        availablityTextField.setCursorWidth(1.5f);
        linearLayout6.addView(availablityTextField, LayoutHelper.createFrame(200, 90, Gravity.CENTER, 10, 25, 0, 0));
        TextView text4 = new TextView(context);
        text4.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        Drawable drawable4 = context.getResources().getDrawable(R.drawable.google_calendar_logo);
        Bitmap b4 = ((BitmapDrawable)drawable4).getBitmap();
        Bitmap bitmapResized4 = Bitmap.createScaledBitmap(b4, 150, 150, false);
        BitmapDrawable gdrawable4 = new BitmapDrawable(context.getResources(), bitmapResized4);
        gdrawable4.setBounds(0,150,150,0);
        text4.setEnabled(true);
        text4.setHovered(true);
        text4.setCompoundDrawablesWithIntrinsicBounds(null, null, gdrawable4, null);
        text4.setOnClickListener((v) -> {
            presentFragment(new LocationActivity(LocationActivity.LOCATION_TYPE_LIVE_VIEW));
        });
        text4.setOnClickListener((v) -> {
            Calendar cal = Calendar.getInstance();
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType("vnd.android.cursor.item/event");
            intent.putExtra("beginTime", cal.getTimeInMillis());
            intent.putExtra("allDay", true);
            intent.putExtra("rrule", "FREQ=YEARLY");
            intent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
            intent.putExtra("title", "Offer time");
            context.startActivity(intent);
        });
        linearLayout6.addView(text4, LayoutHelper.createLinear(150, 150, Gravity.LEFT | Gravity.CENTER_VERTICAL, -40, -20, 10, 0));

        linearLayout2.addView(linearLayout6, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 40, 0, 0, 0));




        RelativeLayout relativeLayout = new RelativeLayout(context);
        viewPager = new ViewPager(context);
        viewPager.setPageMargin(0);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(0);
        bottomPages = new BottomPagesView(context, viewPager, 2);
        relativeLayout.setGravity(Gravity.END);
        nextBtn.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("title", firstNameField.getText().toString());
            bundle.putString("rate", rateTextField.getText().toString());
            bundle.putString("rateType", rateSelectText.getText().toString());
            bundle.putString("currency",rateSymbolSelect.getText().toString());
            bundle.putString("location", locationTextField.getText().toString());
            bundle.putString("time", availablityTextField.getText().toString());
            presentFragment(new CreateOfferActivityPage2(bundle));
        });

        relativeLayout.addView(nextBtn, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.END, 24, 10, 24, 20));
        relativeLayout.addView(viewPager, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.END, 24, 10, 24, 20));
        relativeLayout.addView(bottomPages, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 120, Gravity.END,  (int) (dpWidth * 0.5 - 13), 35, 24, 0));
        scrollView.addView(linearLayout2);


        relativeLayout2.addView(scrollView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.START, 0, 0, 0, (int) (dpHeight * 0.17)));

        relativeLayout2.addView(new HtDividerCell(context), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 45, Gravity.BOTTOM, 0, dpHeight - 70 - 55, 0, 0));

        relativeLayout2.addView(relativeLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 45, Gravity.BOTTOM, 0, dpHeight - 70 - 45, 0, 0));

        linearLayout.addView(relativeLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.END, 0, 0, 0, 0));
        return fragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    private void saveName() {
        final TLRPC.UserFull userFull = MessagesController.getInstance(currentAccount).getUserFull(UserConfig.getInstance(currentAccount).getClientUserId());
        if (getParentActivity() == null || userFull == null) {
            return;
        }
        String currentName = userFull.about;
        if (currentName == null) {
            currentName = "";
        }
        final String newName = firstNameField.getText().toString().replace("\n", "");
        if (currentName.equals(newName)) {
            finishFragment();
            return;
        }

        final AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);

        final TLRPC.TL_account_updateProfile req = new TLRPC.TL_account_updateProfile();
        req.about = newName;
        req.flags |= 4;

    }


    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        themeDescriptions.add(new ThemeDescription(firstNameField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(firstNameField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText));
        themeDescriptions.add(new ThemeDescription(firstNameField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundWhiteInputField));
        themeDescriptions.add(new ThemeDescription(firstNameField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_windowBackgroundWhiteInputFieldActivated));

        return themeDescriptions;
    }
}



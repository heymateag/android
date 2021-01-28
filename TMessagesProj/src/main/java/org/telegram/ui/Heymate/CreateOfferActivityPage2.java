/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.ArchiveHintInnerCell;
import org.telegram.ui.Components.BottomPagesView;
import org.telegram.ui.Components.CodepointsLengthInputFilter;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ProfileGalleryView;

import java.util.ArrayList;

public class CreateOfferActivityPage2 extends BaseFragment {

    Bundle bundle;

    private EditTextBoldCursor firstNameField;
    private View searchButton;
    private TextView serviceProviderTextView;
    private TextView advancedPaymentOptionTextView;
    private FinishButtonCell finishBtn;
    private ViewPager viewPager;
    private BottomPagesView bottomPages;
    private final static int search_button = 1;

    public CreateOfferActivityPage2(Bundle bundle){
            this.bundle = bundle;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View createView(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        int dpWidth = configuration.screenWidthDp;
        int dpHeight = configuration.screenHeightDp;

        finishBtn = new FinishButtonCell(context);
        finishBtn.setEnabled(true);
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
        searchButton = menu.addItemWithWidth(search_button, R.drawable.ic_ab_search, AndroidUtilities.dp(56));
        searchButton.setContentDescription(LocaleController.getString("Search", R.string.Search));

        fragmentView = new LinearLayout(context);
        LinearLayout linearLayout = (LinearLayout) fragmentView;
        RelativeLayout relativeLayout2 = new RelativeLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ScrollView scrollView = new ScrollView(context);
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        fragmentView.setOnTouchListener((v, event) -> true);

        FrameLayout fieldContainer = new FrameLayout(context);
        linearLayout2.addView(fieldContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 10, 0, 0));

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
        firstNameField.setHint("Place the policy here");
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

        LinearLayout linearLayout1 = new LinearLayout(context);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);
        LinearLayout linearLayout44 = new LinearLayout(context);
        TextView text55 = new TextView(context);
        text55.setText("Terms And Conditions");
        text55.setTypeface(text55.getTypeface(), Typeface.BOLD);
        text55.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        text55.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
        linearLayout44.addView(text55, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 20, 0, 20, 15));
        linearLayout1.addView(linearLayout44, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 20, 0, 0));
        linearLayout1.addView(firstNameField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 120, Gravity.LEFT, 30, 0, 30, 20));
        linearLayout1.addView(new HtDividerCell(context));

        fieldContainer.addView(linearLayout1, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 4, 0));

        serviceProviderTextView = new TextView(context);
        serviceProviderTextView.setFocusable(true);
        serviceProviderTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        serviceProviderTextView.setTextColor(Theme.getColor(Theme.key_chat_outGreenCall));
        serviceProviderTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        serviceProviderTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("HtServiceProvider", R.string.HtServiceProvider)));
        serviceProviderTextView.setTypeface(serviceProviderTextView.getTypeface(), Typeface.BOLD);
        linearLayout2.addView(serviceProviderTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 10, 24, 0));
        linearLayout2.addView(new ServiceProviderPromiseRowCell(context, "Delay in start by > 30 mins", "10", "%"), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 0, 24, 0));
        linearLayout2.addView(new ServiceProviderPromiseRowCell(context, "Offer terms", "20", "%"), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 0, 24, 0));
        linearLayout2.addView(new ServiceProviderPromiseRowCell(context, "Some texts may exceed from one line so they will be multilined", "600", "$"), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 0, 24, 0));
        linearLayout2.addView(new ServiceProviderPromiseRowCell(context, "Delay in start by > 30 mins", "10", "%"), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 0, 24, 0));
        linearLayout2.addView(new ServiceProviderPromiseRowCell(context, "Offer terms", "20", "%"), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 0, 24, 0));
        linearLayout2.addView(new HtDividerCell(context), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 0, 20, 0, 0));
        advancedPaymentOptionTextView = new TextView(context);
        advancedPaymentOptionTextView.setFocusable(true);
        advancedPaymentOptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        advancedPaymentOptionTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        advancedPaymentOptionTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        advancedPaymentOptionTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("HtAdvancedPaymentOptions", R.string.HtAdvancedPaymentOptions)));
        advancedPaymentOptionTextView.setTextColor(Theme.getColor(Theme.key_chat_inGreenCall));
        advancedPaymentOptionTextView.setTypeface(advancedPaymentOptionTextView.getTypeface(), Typeface.BOLD);
        linearLayout2.addView(advancedPaymentOptionTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 10, 24, 0));
        linearLayout2.addView(new ServiceProviderPromiseRowCell(context, "Deposit", "70", "%"), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 0, 24, 0));
        linearLayout2.addView(new ServiceProviderPromiseRowCell(context, "Cancellation in > 2 of start", "100" , "%"), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 0, 24, 0));
        linearLayout2.addView(new ServiceProviderPromiseRowCell(context, "Cancellation in 2 - 6 hours of start", "50" , "%"), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 0, 24, 0));
        linearLayout2.addView(new ServiceProviderPromiseRowCell(context, "Deposit", "70", "%"), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 0, 24, 0));
        linearLayout2.addView(new ServiceProviderPromiseRowCell(context, "Cancellation in > 2 hours of start", "100", "%"), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 0, 24, 0));
        linearLayout2.addView(new ServiceProviderPromiseRowCell(context, "Cancellation in 2 - 6 hours of start", "50" , "%"), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 0, 24, 0));
        finishBtn.setElevation(2.0f);
        finishBtn.setHovered(true);
        finishBtn.setOnClickListener(v -> {
            OfferController.getInstance().addOffer(bundle.getString("title"), Integer.parseInt(bundle.getString("rate")),bundle.getString("rateType"),bundle.getString("currency"), bundle.getString("location"), bundle.getString("time"));
            parentLayout.fragmentsStack.remove(parentLayout.fragmentsStack.size() - 2);
            finishFragment();
        });
        RelativeLayout relativeLayout = new RelativeLayout(context);
        viewPager = new ViewPager(context);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                return null;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                super.setPrimaryItem(container, position, object);
                bottomPages.setCurrentPage(position);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view.equals(object);
            }

            @Override
            public void restoreState(Parcelable arg0, ClassLoader arg1) {
            }

            @Override
            public Parcelable saveState() {
                return null;
            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {
                if (observer != null) {
                    super.unregisterDataSetObserver(observer);
                }
            }
        });
        viewPager.setPageMargin(0);
        viewPager.setOffscreenPageLimit(1);
        bottomPages = new BottomPagesView(context, viewPager, 2);
        viewPager.setCurrentItem(1);
        bottomPages.setCurrentPage(1);
        bottomPages.setPageOffset(0,0);
        relativeLayout.addView(finishBtn, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.END : Gravity.END, 24, 10, 24, 20));
        relativeLayout.addView(viewPager, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 24, 10, 24, 20));
        relativeLayout.addView(bottomPages, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, (int) (dpWidth * 0.5 - 13), 35, 24, 0));
        scrollView.addView(linearLayout2);
        relativeLayout2.addView(scrollView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START, 0, 10, 0, (int) (dpHeight * 0.17)));
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

        themeDescriptions.add(new ThemeDescription(serviceProviderTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(advancedPaymentOptionTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(finishBtn, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(bottomPages, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(viewPager, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));

        return themeDescriptions;
    }
}



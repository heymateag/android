/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Heymate;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import works.heymate.beta.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SearchField;
import org.telegram.ui.LocationActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class HtLocationBottomSheetAlert extends BottomSheet implements NotificationCenter.NotificationCenterDelegate, BottomSheet.BottomSheetDelegateInterface {

    private HtCreateOfferActivity parent;
    private double longitude;
    private double latitude;

    public HtLocationBottomSheetAlert(Context context, boolean needFocus, HtCreateOfferActivity parent) {
        super(context, needFocus);
        this.parent = parent;
        initSheet(context);
    }

    public HtLocationBottomSheetAlert(Context context, boolean needFocus) {
        super(context, needFocus);
        initSheet(context);
    }

    public void initSheet(Context context){
        setDisableScroll(true);
        containerView = new LinearLayout(context);

        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setMinimumHeight(700);
        mainLayout.setBackgroundColor(Theme.getColor(Theme.key_wallet_whiteBackground));
        mainLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(18), Theme.getColor(Theme.key_wallet_whiteBackground)));

        ImageView closeImage = new ImageView(context);
        Drawable closeDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.close_bottom);
        closeDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextRed), PorterDuff.Mode.MULTIPLY));
        closeImage.setImageDrawable(closeDrawable);
        closeImage.setEnabled(true);
        closeImage.setHovered(true);
        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        mainLayout.addView(closeImage, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 15,15,15,15));

        LinearLayout mainLayout2 = new LinearLayout(context);
        mainLayout2.setOrientation(LinearLayout.VERTICAL);

        EditTextBoldCursor descriptionTextField = new EditTextBoldCursor(context);
        descriptionTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        descriptionTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        descriptionTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        descriptionTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        descriptionTextField.setMaxLines(4);
        descriptionTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
        descriptionTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        descriptionTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        descriptionTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        descriptionTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        descriptionTextField.setMinHeight(AndroidUtilities.dp(36));
        descriptionTextField.setHint(LocaleController.getString("HtLocation", works.heymate.beta.R.string.HtLocation));
        descriptionTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        descriptionTextField.setCursorSize(AndroidUtilities.dp(15));
        descriptionTextField.setCursorWidth(1.5f);
        descriptionTextField.setMaxLines(1);
        descriptionTextField.setLines(1);
        descriptionTextField.setSingleLine(true);
        descriptionTextField.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_SEARCH || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                descriptionTextField.hideActionMode();
                AndroidUtilities.hideKeyboard(descriptionTextField);
            }
            return false;
        });
        mainLayout2.addView(descriptionTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 15, 15, 15, 0));

        ImageView locationImage = new ImageView(context);
        Drawable locationDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.google_maps_logo);
        locationImage.setImageDrawable(locationDrawable);
        mainLayout2.addView(locationImage, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        locationImage.setEnabled(true);

        locationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AndroidUtilities.isGoogleMapsInstalled(parent)) {
                    return;
                }
                LocationActivity fragment = new LocationActivity(LocationActivity.LOCATION_TYPE_SEND);
                TLRPC.TL_channelLocation loc = new TLRPC.TL_channelLocation();
                loc.geo_point = new TLRPC.TL_geoPoint();
                loc.geo_point._long = longitude;
                loc.geo_point.lat = latitude;
                fragment.setInitialLocation(loc);
                fragment.setDelegate((location, live, notify, scheduleDate) -> {
//                    parent.setLongitude(location.geo._long);
//                    parent.setLongitude(location.geo.lat);
                    // parent.setLocationAddress(descriptionTextField.getText().toString()); // TODO No longer used
                });
                parent.presentFragment(fragment);
                dismiss();
            }
        });
        mainLayout.addView(mainLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        containerView.addView(mainLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
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

    public void setLocation(double longitude, double latitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }
}

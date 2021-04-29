package org.telegram.ui.Heymate;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import works.heymate.beta.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.Heymate.AmplifyModels.Offer;
import org.telegram.ui.Heymate.widget.LocationInputItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import works.heymate.core.Texts;
import works.heymate.core.offer.OfferUtils;
import works.heymate.core.wallet.Wallet;

public class HtCreateOfferActivity extends BaseFragment {

    public static final String ARGUMENTS_CATEGORY = "0_Category";
    public static final String ARGUMENTS_SUB_CATEGORY = "1_Sub-Category";
    public static final String ARGUMENTS_ADDRESS = "0_Address";
    public static final String ARGUMENTS_RATE_TYPE = "0_Rate Type";
    public static final String ARGUMENTS_PRICE = "1_Price";
    public static final String ARGUMENTS_CURRENCY = "2_Currency";
    public static final String ARGUMENTS_EXPIRE = "0_Expire";
    public static final String ARGUMENTS_EXPIRE_DATE = "1_Expire";
    public static final String ARGUMENTS_TERMS = "0_Terms";

    private Context context;
    private ImageView cameraImage;
    public EditTextBoldCursor titleTextField;
    public EditTextBoldCursor descriptionTextField;
    private HtCategoryInputCell categoryInputCell;
    private LocationInputItem locationInputCell;
    private HtScheduleInputCell scheduleInputCell;
    private HtPriceInputCell priceInputCell;
    private ArrayList<HtPriceInputCell> pricesInputCell = new ArrayList<>();
    private HtExpireInputCell expireInputCell;
    private HtTermsInputCell termsInputCell;
    private HtPaymentConfigInputCell paymentInputCell;
    private int priceCellsCount = 0;
    private boolean canEdit = true;
    private ActionType actionType;
    private LinearLayout addPriceLayout;
    private LinearLayout actionLayout;
    private String offerUUID;
    private Uri pickedImage;
    private Date expireDate;
    private ArrayList<Long> dateSlots = new ArrayList<>();

    public enum ActionType {
        CREATE,
        EDIT,
        VIEW
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        super.onActivityResultFragment(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            cameraImage.setImageURI(data.getData());
            cameraImage.getLayoutParams().height = 180;
            cameraImage.getLayoutParams().width = 120;
            cameraImage.requestLayout();
            pickedImage = data.getData();
        }
    }

    @Override
    public View createView(Context context) {
        super.createView(context);
        this.context = context;
        if (canEdit)
            actionBar.setTitle(LocaleController.getString("HtCreateOffer", works.heymate.beta.R.string.HtCreateOffer));
        else
            actionBar.setTitle(LocaleController.getString("HtViewOffer", works.heymate.beta.R.string.HtViewOffer));
        fragmentView = new LinearLayout(context);

        actionBar.setBackButtonImage(works.heymate.beta.R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setSearchTextColor(0xff4488, true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        LinearLayout fragmentMainLayout = (LinearLayout) fragmentView;
        ScrollView mainScrollView = new ScrollView(context);
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground));

        LinearLayout titleLayout = new LinearLayout(context);
        LinearLayout imageLayout = new LinearLayout(context);
        imageLayout.setGravity(Gravity.CENTER);
        imageLayout.setOrientation(LinearLayout.VERTICAL);
        imageLayout.setBackgroundColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        imageLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), context.getResources().getColor(works.heymate.beta.R.color.ht_green)));
        cameraImage = new ImageView(context);
        Drawable cameraDrawable;
        if(actionType != ActionType.EDIT){
            cameraDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.instant_camera);
            cameraImage.setImageDrawable(cameraDrawable);
            cameraImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_wallet_whiteText), PorterDuff.Mode.MULTIPLY));
        }
        imageLayout.addView(cameraImage, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 15, 15, 15, 0));

        TextView cameraLabel = new TextView(context);
        cameraLabel.setText(LocaleController.getString("HtAddOffer", works.heymate.beta.R.string.HtAddPhoto));
        cameraLabel.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        cameraLabel.setLines(2);
        imageLayout.addView(cameraLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 15, 5, 15, 15));
        titleLayout.addView(imageLayout, LayoutHelper.createLinear(80, 130, 15, 15, 15, 0));
        imageLayout.setEnabled(true);
        if (actionType != ActionType.VIEW) {
            imageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    titleTextField.clearFocus();
                    titleTextField.hideActionMode();
                    AndroidUtilities.hideKeyboard(titleTextField);
                    descriptionTextField.clearFocus();
                    descriptionTextField.hideActionMode();
                    AndroidUtilities.hideKeyboard(descriptionTextField);

                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, LocaleController.getString("HtSelectImage", works.heymate.beta.R.string.HtSelectImage));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                    startActivityForResult(chooserIntent, 1);


                }
            });
        }

        LinearLayout titleInputLayout = new LinearLayout(context);
        titleInputLayout.setOrientation(LinearLayout.VERTICAL);

        titleTextField = new EditTextBoldCursor(context);
        titleTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        titleTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        titleTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        titleTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
        titleTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        titleTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        titleTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        titleTextField.setMinHeight(AndroidUtilities.dp(36));
        titleTextField.setHint(LocaleController.getString("HtShortTitle", works.heymate.beta.R.string.HtShortTitle));
        titleTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleTextField.setCursorSize(AndroidUtilities.dp(15));
        titleTextField.setCursorWidth(1.5f);
        titleTextField.setMaxLines(1);
        titleTextField.setLines(1);
        titleTextField.setSingleLine(true);
        titleTextField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || event != null && (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_SEARCH || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.FLAG_EDITOR_ACTION)) {
                titleTextField.hideActionMode();
                AndroidUtilities.hideKeyboard(titleTextField);
                titleTextField.clearFocus();
            }
            return false;
        });
        if (actionType == ActionType.VIEW) {
            titleTextField.setKeyListener(null);
            titleTextField.setEnabled(false);
            titleTextField.setClickable(false);
            titleTextField.setFocusable(false);
        }
        titleTextField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    titleTextField.clearFocus();
                    titleTextField.hideActionMode();
                    AndroidUtilities.hideKeyboard(titleTextField);
                }
            }
        });

        titleInputLayout.addView(titleTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 15, 15, 15, 0));
        descriptionTextField = new EditTextBoldCursor(context);
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
        descriptionTextField.setHint(LocaleController.getString("HtDescription", works.heymate.beta.R.string.HtDescription));
        descriptionTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        descriptionTextField.setCursorSize(AndroidUtilities.dp(15));
        descriptionTextField.setCursorWidth(1.5f);
        descriptionTextField.setMaxLines(5);
        descriptionTextField.setLines(2);
        descriptionTextField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || event != null && (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_SEARCH || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.FLAG_EDITOR_ACTION)) {
                descriptionTextField.clearFocus();
                descriptionTextField.hideActionMode();
                AndroidUtilities.hideKeyboard(descriptionTextField);
            }
            return false;
        });
        descriptionTextField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    descriptionTextField.clearFocus();
                    descriptionTextField.hideActionMode();
                    AndroidUtilities.hideKeyboard(descriptionTextField);
                }
            }
        });
        if (actionType == ActionType.VIEW) {
            descriptionTextField.setKeyListener(null);
            descriptionTextField.setEnabled(false);
            descriptionTextField.setClickable(false);
            descriptionTextField.setFocusable(false);
        }

        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleTextField.clearFocus();
                titleTextField.hideActionMode();
                AndroidUtilities.hideKeyboard(titleTextField);
                descriptionTextField.clearFocus();
                descriptionTextField.hideActionMode();
                AndroidUtilities.hideKeyboard(descriptionTextField);
            }
        });

        titleInputLayout.addView(descriptionTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 15, 15, 15, 0));
        titleLayout.addView(titleInputLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        mainLayout.addView(titleLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        mainLayout.addView(new HtDividerCell(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 25, 0, 25));

        TextView detailsLabel = new TextView(context);
        detailsLabel.setText(LocaleController.getString("HtDetails", works.heymate.beta.R.string.HtDetails));
        detailsLabel.setTextColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
        mainLayout.addView(detailsLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 15, 0, 15, 15));

        HashMap<String, Runnable> categoryArgs = new HashMap<>();
        HtCreateOfferActivity parent = this;

        categoryArgs.put(ARGUMENTS_CATEGORY, new Runnable() {
            @Override
            public void run() {
                if (actionType != ActionType.VIEW) {
                    HtCategoryBottomSheetAlert bottomSheetAlert = new HtCategoryBottomSheetAlert(context, true, parent);
                    showDialog(bottomSheetAlert);
                }
            }
        });
        categoryArgs.put(ARGUMENTS_SUB_CATEGORY, new Runnable() {
            @Override
            public void run() {
                if (actionType != ActionType.VIEW) {
                    HtCategoryBottomSheetAlert bottomSheetAlert = new HtCategoryBottomSheetAlert(context, true, parent);
                    showDialog(bottomSheetAlert);
                }
            }
        });
        categoryInputCell = new HtCategoryInputCell(context, this, LocaleController.getString("HtCategory", works.heymate.beta.R.string.HtCategory), categoryArgs, works.heymate.beta.R.drawable.category, canEdit);
        mainLayout.addView(categoryInputCell);

        locationInputCell = new LocationInputItem(context);
        mainLayout.addView(locationInputCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        HashMap<String, Runnable> scheduleArgs = new HashMap<>();
        scheduleInputCell = new HtScheduleInputCell(context, LocaleController.getString("HtSchedule", works.heymate.beta.R.string.HtSchedule), scheduleArgs, works.heymate.beta.R.drawable.watch_later_24_px_1, canEdit, this);
        mainLayout.addView(scheduleInputCell);

        HashMap<String, Runnable> priceArgs = new HashMap<>();
        priceArgs.put(ARGUMENTS_RATE_TYPE, new Runnable() {
            @Override
            public void run() {
                if (actionType == ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("HtRateType", works.heymate.beta.R.string.HtRateType));
                String[] subItems = new String[3];
                int[] icons = new int[3];

                for (int i = 0; i < 3; i++) {
                    icons[i] = works.heymate.beta.R.drawable.msg_arrowright;
                }
                subItems[0] = LocaleController.getString("HtPerItem", works.heymate.beta.R.string.HtPerItem);
                subItems[1] = LocaleController.getString("HtPerHour", works.heymate.beta.R.string.HtPerHour);
                subItems[2] = LocaleController.getString("HtRate", works.heymate.beta.R.string.HtRange);
                builder.setNegativeButton(LocaleController.getString("HtCancel", works.heymate.beta.R.string.HtCancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setItems(subItems, icons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setRateType(subItems[which], 0);
                    }
                });
                AlertDialog alertDialog = builder.create();
                showDialog(alertDialog);
            }
        });
        priceArgs.put(ARGUMENTS_PRICE, new Runnable() {
            @Override
            public void run() {
                if (actionType == ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(LocaleController.getString("HtPrice", works.heymate.beta.R.string.HtPrice));
                LinearLayout mainLayout = new LinearLayout(context);
                EditTextBoldCursor feeTextField = new EditTextBoldCursor(context);
                feeTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                feeTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
                feeTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
                feeTextField.setMaxLines(4);
                feeTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
                feeTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                feeTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                feeTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                feeTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
                feeTextField.setMinHeight(AndroidUtilities.dp(36));
                feeTextField.setHint(LocaleController.getString("HtAmount", works.heymate.beta.R.string.HtAmount));
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton(LocaleController.getString("HtApply", works.heymate.beta.R.string.HtApply), (dialog, which) -> {
                    if (feeTextField.getText().toString().length() > 0)
                        setFee(feeTextField.getText().toString(), 0);
                });
                AlertDialog alertDialog = builder.create();
                showDialog(alertDialog);

            }
        });
        priceArgs.put(ARGUMENTS_CURRENCY, new Runnable() {
            @Override
            public void run() {
                if (actionType == ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("HtCurrency", works.heymate.beta.R.string.HtCurrency));
                String[] subItems = new String[3];
                int[] icons = new int[3];

                for (int i = 0; i < 3; i++) {
                    icons[i] = works.heymate.beta.R.drawable.msg_arrowright;
                }
                subItems[0] = "R$";
                subItems[1] = "US$";
                subItems[2] = "EUR";
                builder.setNegativeButton(LocaleController.getString("HtCancel", works.heymate.beta.R.string.HtCancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setItems(subItems, icons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setCurrency(subItems[which], 0);
                    }
                });
                AlertDialog alertDialog = builder.create();
                showDialog(alertDialog);
            }
        });

        priceInputCell = new HtPriceInputCell(context, this, LocaleController.getString("HtPrice", works.heymate.beta.R.string.HtPrice), priceArgs, works.heymate.beta.R.drawable.money, canEdit, 0);
        priceInputCell.setRes(ARGUMENTS_RATE_TYPE, LocaleController.getString("HtPerItem", works.heymate.beta.R.string.HtPerItem), 0);
        priceInputCell.setRes(ARGUMENTS_CURRENCY, "R$", 2);
        mainLayout.addView(priceInputCell);

        HashMap<String, Runnable> paymentArgs = new HashMap<>();
        paymentInputCell = new HtPaymentConfigInputCell(context, LocaleController.getString("HtPaymentTerms", works.heymate.beta.R.string.HtPaymentTerms), paymentArgs, works.heymate.beta.R.drawable.pay, this, actionType);
        mainLayout.addView(paymentInputCell);

        HashMap<String, Runnable> expireArgs = new HashMap<>();
        Calendar mcurrentTime = Calendar.getInstance();
        int day2 = mcurrentTime.get(Calendar.DAY_OF_MONTH);
        mcurrentTime.add(Calendar.DATE, 7);
        int day1 = mcurrentTime.get(Calendar.DAY_OF_MONTH);
        if (day1 < day2)
            mcurrentTime.add(Calendar.MONTH, 1);
        int year = mcurrentTime.get(Calendar.YEAR);
        int month = mcurrentTime.get(Calendar.MONTH);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        expireArgs.put(ARGUMENTS_EXPIRE, new Runnable() {
            @Override
            public void run() {
                if (actionType == ActionType.VIEW)
                    return;
                Calendar mcurrentTime = Calendar.getInstance();
                int year = mcurrentTime.get(Calendar.YEAR);
                int month = mcurrentTime.get(Calendar.MONTH);
                int day = mcurrentTime.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog mTimePicker;
                mTimePicker = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar expireDateCal = Calendar.getInstance();
                        expireDateCal.set(Calendar.YEAR, year);
                        expireDateCal.set(Calendar.MONTH, month);
                        expireDateCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        expireInputCell.setRes(ARGUMENTS_EXPIRE, simpleDateFormat.format(expireDateCal.getTime()), 0);
                        expireDate = expireDateCal.getTime();
                    }
                }, year, month, day);
                mTimePicker.setTitle(LocaleController.getString("HtSelectDate", works.heymate.beta.R.string.HtSelectDate));
                mTimePicker.show();
            }
        });

        expireInputCell = new HtExpireInputCell(context, this, LocaleController.getString("HtExpiration", works.heymate.beta.R.string.HtExpiration), expireArgs, works.heymate.beta.R.drawable.alarm_off_24_px, canEdit);
        expireInputCell.setRes(ARGUMENTS_EXPIRE, simpleDateFormat.format(mcurrentTime.getTime()), 0);
        expireDate = mcurrentTime.getTime();
        mainLayout.addView(expireInputCell);

        HashMap<String, Runnable> termsArgs = new HashMap<>();
        termsArgs.put(ARGUMENTS_TERMS, new Runnable() {
            @Override
            public void run() {
                if (actionType == ActionType.VIEW)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(LocaleController.getString("HtTermsAndConditions", works.heymate.beta.R.string.HtTermsAndConditions));
                LinearLayout mainLayout = new LinearLayout(context);
                EditTextBoldCursor feeTextField = new EditTextBoldCursor(context);
                feeTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                feeTextField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
                feeTextField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
                feeTextField.setMaxLines(14);
                feeTextField.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0), 0, AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24), AndroidUtilities.dp(6));
                feeTextField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                feeTextField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                feeTextField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                feeTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
                feeTextField.setMinLines(4);
                feeTextField.setMinHeight(AndroidUtilities.dp(36));
                feeTextField.setHint(LocaleController.getString("HtPlacePolicy", works.heymate.beta.R.string.HtPlacePolicy));
                feeTextField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                feeTextField.setCursorSize(AndroidUtilities.dp(15));
                feeTextField.setCursorWidth(1.5f);
                feeTextField.setLines(1);
                feeTextField.setSingleLine(true);
                feeTextField.setOnEditorActionListener((v, actionId, event) -> {
                    if (event != null && (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_SEARCH || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        feeTextField.hideActionMode();
                        AndroidUtilities.hideKeyboard(feeTextField);
                    }
                    return false;
                });
                mainLayout.addView(feeTextField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 400, 20, 0, 20, 15));
                builder.setView(mainLayout);
                builder.setPositiveButton(LocaleController.getString("HtApply", works.heymate.beta.R.string.HtApply), (dialog, which) -> {
                    if (feeTextField.getText().toString().length() > 0)
                        termsInputCell.setRes(ARGUMENTS_TERMS, feeTextField.getText().toString(), 0);
                });
                AlertDialog alertDialog = builder.create();
                showDialog(alertDialog);
            }
        });

        termsInputCell = new HtTermsInputCell(context,this,  LocaleController.getString("HtTermsAndConditions", works.heymate.beta.R.string.HtTermsAndConditions), termsArgs, works.heymate.beta.R.drawable.ht_pplicy, canEdit);
        termsInputCell.setRes(ARGUMENTS_TERMS, Texts.get(Texts.CREATE_OFFER_SERVICE_PROVIDER_TERMS), 0);
        mainLayout.addView(termsInputCell);

        LinearLayout alertLayout = new LinearLayout(context);
        mainLayout.addView(alertLayout);

        actionLayout = new LinearLayout(context);
        actionLayout.setGravity(Gravity.CENTER);
        actionLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.ht_theme));

        LinearLayout promoteLayout = new LinearLayout(context);
        promoteLayout.setGravity(Gravity.CENTER);

        TextView promoteLabel = new TextView(context);
        promoteLabel.setText(LocaleController.getString("HtPromote", works.heymate.beta.R.string.HtPromote));
        promoteLabel.setTextSize(17);
        promoteLabel.setCompoundDrawablePadding(AndroidUtilities.dp(8));
        promoteLabel.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));

        Drawable promoteDrawable = context.getResources().getDrawable(R.drawable.ic_send);
        promoteDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_wallet_whiteText), PorterDuff.Mode.MULTIPLY));
        promoteLabel.setCompoundDrawablesWithIntrinsicBounds(null, null, promoteDrawable, null);
        promoteLayout.addView(promoteLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 12, 12, 12, 12));
        promoteLayout.setEnabled(true);
        promoteLayout.setOnClickListener(v -> {
            titleTextField.setHighlightColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
            priceInputCell.setError(false, 1);
            locationInputCell.setError(false);
            categoryInputCell.setError(false, 0);
            categoryInputCell.setError(false, 1);

            UndoView undoView = new UndoView(context, this);
            undoView.setColors(Theme.getColor(Theme.key_chat_inRedCall), Theme.getColor(Theme.key_dialogTextBlack));
            alertLayout.removeAllViews();
            String errors = "";
            alertLayout.addView(undoView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 15, 15, 15, 15));
            if (titleTextField.getText().toString().isEmpty()) {
                titleTextField.setHighlightColor(Theme.getColor(Theme.key_chat_inRedCall));
                errors += LocaleController.getString("HtTitleEmpty", works.heymate.beta.R.string.HtTitleEmpty);
            }
            if (descriptionTextField.getText().toString().isEmpty()) {
                descriptionTextField.setHighlightColor(Theme.getColor(Theme.key_chat_inRedCall));
                errors += LocaleController.getString("HtDescriptionEmpty", works.heymate.beta.R.string.HtDescriptionEmpty);
            }
            if (priceInputCell.getRes(ARGUMENTS_PRICE) == null) {
                priceInputCell.setError(true, 1);
                errors += LocaleController.getString("HtPriceEmpty", works.heymate.beta.R.string.HtPriceEmpty);
            }
            if (locationInputCell.getLocationInfo() == null) {
                locationInputCell.setError(true);
                errors += LocaleController.getString("HtLocationEmpty", works.heymate.beta.R.string.HtLocationEmpty);
            }
            if (categoryInputCell.getRes(ARGUMENTS_CATEGORY) == null) {
                categoryInputCell.setError(true, 0);
                errors += LocaleController.getString("HtCategoryEmpty", works.heymate.beta.R.string.HtCategoryEmpty);
            }
            if (categoryInputCell.getRes(ARGUMENTS_SUB_CATEGORY) == null) {
                categoryInputCell.setError(true, 1);
                errors += LocaleController.getString("HtSubCategoryEmpty", works.heymate.beta.R.string.HtSubCategoryEmpty);
            }
            if (!errors.isEmpty()) {
                undoView.showWithAction(0, UndoView.ACTION_OFFER_DATA_INCOMPLETE, errors, null, () -> {
                    undoView.setVisibility(View.GONE);
                });
            } else {
                // TODO Wrong promote logic.

                String configText = paymentInputCell.getRes().toString();

                LocationInputItem.LocationInfo locationInfo = locationInputCell.getLocationInfo();

                OfferDto newOffer = new OfferDto();
                newOffer.setTitle(titleTextField.getText().toString());
                newOffer.setDescription(descriptionTextField.getText().toString());
                newOffer.setTerms(termsInputCell.getRes(ARGUMENTS_TERMS));
                newOffer.setConfigText(configText);
                newOffer.setCategory(categoryInputCell.getRes(ARGUMENTS_CATEGORY));
                newOffer.setSubCategory(categoryInputCell.getRes(ARGUMENTS_SUB_CATEGORY));
                newOffer.setExpire(expireDate);
                newOffer.setLocation(locationInfo.address);
                newOffer.setCurrency(priceInputCell.getRes(ARGUMENTS_CURRENCY));
                newOffer.setRateType(priceInputCell.getRes(ARGUMENTS_RATE_TYPE));
                newOffer.setRate(priceInputCell.getRes(ARGUMENTS_PRICE));
                newOffer.setLatitude(locationInfo.latitude);
                newOffer.setLongitude(locationInfo.longitude);
                newOffer.setDateSlots(dateSlots);
                newOffer.setStatus(OfferStatus.ACTIVE);
                newOffer.setUserId(UserConfig.getInstance(currentAccount).clientUserId);
                if(offerUUID == null) {
                    offerUUID = UUID.randomUUID().toString();
                    newOffer.setServerUUID(offerUUID);
                }
                int currentTime = (int) ((new Date()).toInstant().getEpochSecond() / 1000);
                newOffer.setCreatedAt(currentTime);
                newOffer.setEditedAt(currentTime);
                Offer createdOffer = HtAmplify.getInstance(context).createOffer(newOffer, pickedImage, null, null);

                HtSQLite.getInstance().addOffer(createdOffer);

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");

                TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();

                String name;

                if (user.username != null) {
                    name = "@" + user.username;
                }
                else {
                    name = user.first_name;

                    if (!TextUtils.isEmpty(user.last_name)) {
                        name = name + " " + user.last_name;
                    }
                }

                String message = OfferUtils.serializeBeautiful(createdOffer, name, OfferUtils.CATEGORY, OfferUtils.EXPIRY);
                share.putExtra(Intent.EXTRA_TEXT, message);
                getParentActivity().startActivity(Intent.createChooser(share, LocaleController.getString("HtPromoteOffer", works.heymate.beta.R.string.HtPromoteYourOffer)));
                parentLayout.fragmentsStack.remove(parentLayout.fragmentsStack.size() - 2);
                finishFragment();
            }
        });

        LinearLayout saveLayout = new LinearLayout(context);
        saveLayout.setGravity(Gravity.CENTER);

        TextView saveLabel = new TextView(context);
        saveLabel.setText(LocaleController.getString("HtSave", works.heymate.beta.R.string.HtSave));
        saveLabel.setTextSize(17);
        saveLabel.setCompoundDrawablePadding(AndroidUtilities.dp(8));
        saveLabel.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        Drawable saveDrawable = context.getResources().getDrawable(R.drawable.ic_save).mutate();
        saveDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_wallet_whiteText), PorterDuff.Mode.SRC_IN));
        saveLabel.setCompoundDrawablesWithIntrinsicBounds(null, null, saveDrawable, null);
        saveLayout.addView(saveLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 12, 12, 12, 12));
        saveLayout.setEnabled(true);
        saveLayout.setOnClickListener(v -> {
            Wallet wallet = Wallet.get(getParentActivity(), TG2HM.getCurrentPhoneNumber());

            if (!wallet.isCreated()) {
                AlertDialog alert = new AlertDialog.Builder(getParentActivity())
                        .setTitle(Texts.get(Texts.CREATE_OFFER_NO_WALLET))
                        .setMessage(Texts.get(Texts.CREATE_OFFER_NO_WALLET_DESCRIPTION))
                        .setPositiveButton(Texts.get(Texts.CREATE_WALLET), (dialog, which) -> presentFragment(new WalletActivity(saveLayout::performClick)))
                        .setNegativeButton(Texts.get(Texts.CANCEL), (dialog, which) -> dialog.dismiss())
                        .create();
                showDialog(alert);
                return;
            }

            titleTextField.setHighlightColor(context.getResources().getColor(works.heymate.beta.R.color.ht_green));
            priceInputCell.setError(false, 1);
            locationInputCell.setError(false);
            categoryInputCell.setError(false, 0);
            categoryInputCell.setError(false, 1);

            UndoView undoView = new UndoView(context, this);
            undoView.setColors(Theme.getColor(Theme.key_chat_inRedCall), Theme.getColor(Theme.key_dialogTextBlack));
            alertLayout.removeAllViews();
            StringBuilder errors = new StringBuilder();
            alertLayout.addView(undoView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 15, 15, 15, 15));
            if (titleTextField.getText().toString().isEmpty()) {
                titleTextField.setHighlightColor(Theme.getColor(Theme.key_chat_inRedCall));
                errors.append(LocaleController.getString("HtTitleEmpty", works.heymate.beta.R.string.HtTitleEmpty));
            }
            if (descriptionTextField.getText().toString().isEmpty()) {
                descriptionTextField.setHighlightColor(Theme.getColor(Theme.key_chat_inRedCall));
                errors.append(LocaleController.getString("HtDescriptionEmpty", works.heymate.beta.R.string.HtDescriptionEmpty));
            }
            if (priceInputCell.getRes(ARGUMENTS_PRICE) == null) {
                priceInputCell.setError(true, 1);
                errors.append(LocaleController.getString("HtPriceEmpty", works.heymate.beta.R.string.HtPriceEmpty));
            }
            if (locationInputCell.getLocationInfo() == null) {
                locationInputCell.setError(true);
                errors.append(LocaleController.getString("HtLocationEmpty", works.heymate.beta.R.string.HtLocationEmpty));
            }
            if (categoryInputCell.getRes(ARGUMENTS_CATEGORY) == null) {
                categoryInputCell.setError(true, 0);
                errors.append(LocaleController.getString("HtCategoryEmpty", works.heymate.beta.R.string.HtCategoryEmpty));
            }
            if (categoryInputCell.getRes(ARGUMENTS_SUB_CATEGORY) == null) {
                categoryInputCell.setError(true, 1);
                errors.append(LocaleController.getString("HtSubCategoryEmpty", works.heymate.beta.R.string.HtSubCategoryEmpty));
            }
            if (errors.length() > -0) {
                undoView.showWithAction(0, UndoView.ACTION_OFFER_DATA_INCOMPLETE, errors, null, () -> {
                    undoView.setVisibility(View.GONE);
                });
            } else {
                LocationInputItem.LocationInfo locationInfo = locationInputCell.getLocationInfo();

                String configText = paymentInputCell.getRes().toString();
                wallet.signOffer(priceInputCell.getRes(ARGUMENTS_PRICE), configText, (successful, signature, exception) -> {
                    if (!successful) {
                        Toast.makeText(context, Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
                        return;
                    }

                    OfferDto newOffer = new OfferDto();
                    newOffer.setTitle(titleTextField.getText().toString());
                    newOffer.setDescription(descriptionTextField.getText().toString());
                    newOffer.setTerms(termsInputCell.getRes(ARGUMENTS_TERMS));
                    newOffer.setConfigText(configText);
                    newOffer.setCategory(categoryInputCell.getRes(ARGUMENTS_CATEGORY));
                    newOffer.setSubCategory(categoryInputCell.getRes(ARGUMENTS_SUB_CATEGORY));
                    newOffer.setExpire(expireDate);
                    newOffer.setLocation(locationInfo.address);
                    newOffer.setCurrency(priceInputCell.getRes(ARGUMENTS_CURRENCY));
                    newOffer.setRateType(priceInputCell.getRes(ARGUMENTS_RATE_TYPE));
                    newOffer.setRate(priceInputCell.getRes(ARGUMENTS_PRICE));
                    newOffer.setLatitude(locationInfo.latitude);
                    newOffer.setLongitude(locationInfo.longitude);
                    newOffer.setStatus(OfferStatus.ACTIVE);
                    newOffer.setDateSlots(dateSlots);
                    newOffer.setUserId(UserConfig.getInstance(currentAccount).clientUserId);

                    if(offerUUID == null) {
                        offerUUID = UUID.randomUUID().toString();
                        newOffer.setServerUUID(offerUUID);
                    }
                    int currentTime = (int) ((new Date()).toInstant().getEpochSecond() / 1000);
                    newOffer.setCreatedAt(currentTime);
                    newOffer.setEditedAt(currentTime);
                    if (actionType != ActionType.EDIT) {
                        Offer offer = HtAmplify.getInstance(context).createOffer(newOffer, pickedImage, wallet.getAddress(), signature);

                        TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();

                        String name;

                        if (user.username != null) {
                            name = "@" + user.username;
                        }
                        else {
                            name = user.first_name;

                            if (!TextUtils.isEmpty(user.last_name)) {
                                name = name + " " + user.last_name;
                            }
                        }

                        String phrase = OfferUtils.serializeBeautiful(offer, name, OfferUtils.CATEGORY, OfferUtils.EXPIRY);

                        SendMessagesHelper.getInstance(currentAccount).sendMessage(phrase, (long) UserConfig.getInstance(currentAccount).clientUserId, null, null, null, false, null, null, null, false, 0);
                    } else {
                        HtSQLite.getInstance().addOffer(offerUUID, newOffer);
                        newOffer.setServerUUID(offerUUID);
                        HtAmplify.getInstance(context).updateOffer(newOffer);
                    }
                    undoView.setColors(context.getResources().getColor(works.heymate.beta.R.color.ht_green), Theme.getColor(Theme.key_wallet_whiteText));
                    undoView.showWithAction(0, UndoView.ACTION_OFFER_SAVED, errors, null, () -> {
                        undoView.setVisibility(View.GONE);
                    });
                    presentFragment(new OffersActivity(context));
                    parentLayout.fragmentsStack.remove(parentLayout.fragmentsStack.size() - 2);
                    if (actionType == ActionType.EDIT)
                        parentLayout.fragmentsStack.remove(parentLayout.fragmentsStack.size() - 3);
                    finishFragment();
                });
            }
        });

        if (canEdit) {
            actionLayout.addView(saveLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 0.5f));

            View divider = new View(context);
            divider.setBackgroundColor(Theme.getColor(Theme.key_wallet_whiteText));
            actionLayout.addView(divider, LayoutHelper.createLinear(1, 36));
        }
        actionLayout.addView(promoteLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 0.5f));
        mainLayout.addView(actionLayout);
        mainScrollView.addView(mainLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        fragmentMainLayout.addView(mainScrollView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        return fragmentView;
    }

    public void setCategory(String text) {
        categoryInputCell.setRes(ARGUMENTS_CATEGORY, text, 0);
    }

    public void setSubCategory(String text) {
        categoryInputCell.setRes(ARGUMENTS_SUB_CATEGORY, text, 1);
    }

    public void setDateSlots(ArrayList<Long> dates) {
        this.dateSlots = dates;
        scheduleInputCell.setDateSlots(dateSlots);
    }

    public void setFee(String text, int position) {
        priceInputCell.setRes(ARGUMENTS_PRICE, text, 1);
        if (position == 0)
            priceInputCell.setRes(ARGUMENTS_PRICE, text, 1);
        else
            pricesInputCell.get(position - 1).setRes(ARGUMENTS_PRICE, text, 1);
    }

    public void setRateType(String text, int position) {
        if (position == 0)
            priceInputCell.setRes(ARGUMENTS_RATE_TYPE, text, 0);
        else
            pricesInputCell.get(position - 1).setRes(ARGUMENTS_RATE_TYPE, text, 0);
    }

    public void setCurrency(String text, int position) {
        if (position == 0)
            priceInputCell.setRes(ARGUMENTS_CURRENCY, text, 2);
        else
            pricesInputCell.get(position - 1).setRes(ARGUMENTS_CURRENCY, text, 2);

    }

    public void setLocation(String address, double latitude, double longitude) {
        locationInputCell.setLocationInfo(address, latitude, longitude);
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public void setTitle(String title) {
        titleTextField.setText(title);
    }

    public void setOfferUUID(String offerUUID) {
        this.offerUUID = offerUUID;
        cameraImage.setImageBitmap(HtStorage.getInstance().getOfferImage(context, offerUUID));
    }

    public void setDescription(String description) {
        descriptionTextField.setText(description);
    }

    public void setPaymentConfig(String config) {
        try {
            JSONObject json = new JSONObject(config);
            for(int i = 0; i< 7;i++)
                paymentInputCell.setRes(json.get("arg" + i), i);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setTerms(String terms) {
        termsInputCell.setRes(ARGUMENTS_TERMS, terms, 0);
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
        paymentInputCell.setActionType(actionType);
        if (actionType == ActionType.VIEW) {
            addPriceLayout.setVisibility(View.GONE);
            titleTextField.setKeyListener(null);
            titleTextField.setEnabled(false);
            titleTextField.setClickable(false);
            titleTextField.setFocusable(false);

            descriptionTextField.setKeyListener(null);
            descriptionTextField.setEnabled(false);
            descriptionTextField.setClickable(false);
            descriptionTextField.setFocusable(false);

            actionLayout.removeAllViews();
            LinearLayout contactSenderLayout = new LinearLayout(context);
            contactSenderLayout.setBackgroundColor(Theme.getColor(Theme.key_dialogTextBlue));
            contactSenderLayout.setGravity(Gravity.CENTER);

            TextView contactSenderLabel = new TextView(context);
            contactSenderLabel.setText(LocaleController.getString("HtContactSender", works.heymate.beta.R.string.HtContactSender));
            contactSenderLabel.setTextSize(17);
            contactSenderLabel.setTypeface(contactSenderLabel.getTypeface(), Typeface.BOLD);
            contactSenderLabel.setCompoundDrawablePadding(AndroidUtilities.dp(4));
            contactSenderLabel.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));

            Drawable saveDrawable = context.getResources().getDrawable(works.heymate.beta.R.drawable.floating_message);
            saveDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_wallet_whiteText), PorterDuff.Mode.MULTIPLY));
            contactSenderLabel.setCompoundDrawablesWithIntrinsicBounds(saveDrawable, null, null, null);
            contactSenderLayout.addView(contactSenderLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 12, 12, 12, 12));

            actionLayout.addView(contactSenderLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50, 1f));

        }
        switch (actionType) {
            case CREATE: {
                actionBar.setTitle(LocaleController.getString("HtCreateOffer", works.heymate.beta.R.string.HtCreateOffer));
                break;
            }
            case EDIT: {
                actionBar.setTitle(LocaleController.getString("HtEditDraft", works.heymate.beta.R.string.HtEditDraft));
                break;
            }
            case VIEW: {
                actionBar.setTitle(LocaleController.getString("HtViewOffer", works.heymate.beta.R.string.HtViewOffer));
                break;
            }
        }
    }
}

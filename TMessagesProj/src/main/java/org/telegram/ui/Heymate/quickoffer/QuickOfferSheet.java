package org.telegram.ui.Heymate.quickoffer;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadioButton;
import org.telegram.ui.Heymate.widget.Space;

import java.util.ArrayList;
import java.util.List;

import works.heymate.api.APIObject;
import works.heymate.beta.R;

public class QuickOfferSheet extends BottomSheet {

    private static final String OFFER_TYPE_PAYMENT = "payment";
    private static final String OFFER_TYPE_MEETING = "meeting";

    private static final int CREATE = 0;
    private static final int TITLE = 1;
    private static final int DESCRIPTION = 2;
    private static final int TYPE = 3;
    private static final int TYPE_PAYMENT = 4;
    private static final int TYPE_VIDEO_MEETING = 5;
    private static final int INVITE_LINK_TITLE = 6;
    private static final int INVITE_LINK_TOGGLE = 7;
    private static final int INVITE_LINK = 8;
    private static final int PRICE_TITLE = 9;
    private static final int CURRENCY = 10;
    private static final int PRICE = 11;
    private static final int DISCLAIMER = 12;
    private static final int WHITE = 13;
    private static final int GRAY = 14;
    private static final int DIVIDER = 15;

    private RecyclerView mList;
    private ContentAdapter mContentAdapter;

    private List<Integer> mItems = new ArrayList<>();

    private APIObject mOffer;

    private String mOfferType = OFFER_TYPE_MEETING;
    private boolean mHasInviteLink = false;

    public QuickOfferSheet(Context context) {
        super(context, true);

        setDimBehindAlpha(0x4D);
        setDimBehind(true);
        setCanDismissWithSwipe(false);

        mList = new RecyclerView(context);
        mList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        mContentAdapter = new ContentAdapter();
        mList.setAdapter(mContentAdapter);

        FrameLayout content = new FrameLayout(context);

        content.addView(mList, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(240)));

        setCustomView(content);

        mOffer = new APIObject(new JSONObject());
    }

    @Override
    protected void onStart() {
        super.onStart();

        fillItems();
    }

    private void createOffer() {
        // TODO
    }

    private void setOfferType(String offerType) {
        mOfferType = offerType;

        fillItems();
    }

    private void fillItems() {
        mItems.clear();

        mItems.add(CREATE);
        mItems.add(WHITE);
        mItems.add(TITLE);
        mItems.add(DESCRIPTION);
        mItems.add(WHITE);
        mItems.add(GRAY);
        mItems.add(TYPE);
        mItems.add(TYPE_PAYMENT);
        mItems.add(DIVIDER);
        mItems.add(TYPE_VIDEO_MEETING);
        mItems.add(GRAY);

        if (OFFER_TYPE_MEETING.equals(mOfferType)) {
            mItems.add(INVITE_LINK_TITLE);
            mItems.add(INVITE_LINK_TOGGLE);

            if (mHasInviteLink) {
                mItems.add(INVITE_LINK);
                mItems.add(WHITE);
            }

            mItems.add(WHITE);
            mItems.add(GRAY);
        }

        mItems.add(PRICE_TITLE);
        mItems.add(CURRENCY);
        mItems.add(DIVIDER);
        mItems.add(PRICE);
        mItems.add(DISCLAIMER);

        mContentAdapter.notifyDataSetChanged();
    }

    private class ContentAdapter extends RecyclerView.Adapter {

        @Override
        public int getItemViewType(int position) {
            return mItems.get(position);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final Context context = parent.getContext();

            View view;

            switch (viewType) {
                case CREATE: {
                    FrameLayout container = new FrameLayout(context) {
                        @Override
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
                            setMeasuredDimension(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), getMeasuredHeightAndState());
                        }
                    };
                    container.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

                    TextView title = new TextView(context);
                    title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
                    title.setTextSize(16);
                    title.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
                    title.setText("Quick offer");
                    container.addView(title, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT));

                    TextView create = new TextView(context);
                    create.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
                    create.setTextSize(14);
                    create.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
                    create.setText("CREATE");
                    create.setOnClickListener(v -> createOffer());
                    container.addView(create, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT));

                    view = container;
                    break;
                }
                case TITLE:
                case DESCRIPTION: {
                    EditText editText = createEditText(context, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, false, 120);
                    editText.setMaxLines(1);
                    editText.setSingleLine();

                    RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.leftMargin = params.rightMargin = params.topMargin = params.bottomMargin = AndroidUtilities.dp(16);
                    editText.setLayoutParams(params);

                    view = editText;
                    break;
                }
                case TYPE:
                case INVITE_LINK_TITLE:
                case PRICE_TITLE: {
                    TextView title = new TextView(context);
                    title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
                    title.setTextSize(14);
                    title.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
                    view = title;
                    break;
                }
                case TYPE_PAYMENT:
                case TYPE_VIDEO_MEETING: {
                    FrameLayout container = new FrameLayout(context) {
                        @Override
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
                            setMeasuredDimension(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), getMeasuredHeightAndState());
                        }
                    };
                    container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView text = new TextView(context);
                    text.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    text.setTextSize(16);
                    text.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
                    text.setOnClickListener(v -> setOfferType(String.valueOf(text.getTag())));
                    container.addView(text);

                    RadioButton radio = new RadioButton(context);
                    radio.setSize(AndroidUtilities.dp(20));
                    radio.setColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_radioBackgroundChecked));
                    container.addView(radio, LayoutHelper.createFrame(20, 20, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 16, 0));

                    view = container;
                    break;
                }
                case INVITE_LINK_TOGGLE: {
                    NotificationsCheckCell check = new NotificationsCheckCell(context, 16, 56, false);
                    check.setTextAndValueAndCheck("Join via invite link", null, false, false);
                    check.setOnClickListener(v -> {
                        mHasInviteLink = !mHasInviteLink;
                        fillItems();
                    });
                    view = check;
                    break;
                }
                case INVITE_LINK: {
                    EditText editText = createEditText(context, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI, true, 0);
                    editText.setMaxLines(1);
                    editText.setSingleLine(true);
                    editText.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(24), Theme.getColor(Theme.key_graySection)));
                    editText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    editText.setTextSize(14);
                    editText.setGravity(Gravity.LEFT);
                    editText.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
                    editText.setHint("Invite link");

                    RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.leftMargin = params.rightMargin = AndroidUtilities.dp(16);
                    editText.setLayoutParams(params);

                    view = editText;
                    break;
                }
                case CURRENCY:
                case PRICE: {
                    view = new TextSettingsCell(context, 16);
                    break;
                }
                case DISCLAIMER: {
                    TextView disclaimer = new TextView(context);
                    disclaimer.setBackgroundColor(Theme.getColor(Theme.key_graySection));
                    disclaimer.setTextColor(Theme.getColor(Theme.key_graySectionText));
                    disclaimer.setTextSize(12);
                    disclaimer.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16));

                    final String standardOffers = "Standard Offers";
                    SpannableStringBuilder text = new SpannableStringBuilder("If you want to add more option (Category, Schedule, Terms and Conditions and etc...) to your offer use ");
                    final int start = text.length();
                    text.append(standardOffers).append(".");
                    text.setSpan(new MetricAffectingSpan() {

                        @Override
                        public void updateMeasureState(@NonNull TextPaint textPaint) {
                            textPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
                        }

                        @Override
                        public void updateDrawState(TextPaint tp) {
                            tp.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
                        }

                    }, start, start + standardOffers.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    disclaimer.setText(text);
                    view = disclaimer;
                    break;
                }
                case WHITE:
                    view = new Space(context, AndroidUtilities.dp(8), Theme.key_windowBackgroundWhite);
                    break;
                case GRAY:
                    view = new Space(context, AndroidUtilities.dp(8), Theme.key_windowBackgroundGray);
                    break;
                case DIVIDER: {
                    view = new Space(context, AndroidUtilities.dp(1), Theme.key_divider);
                    ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.leftMargin = AndroidUtilities.dp(16);
                    view.setLayoutParams(params);
                    break;
                }
                default:
                    view = new View(context);
            }

            return new RecyclerView.ViewHolder(view) { };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int item = mItems.get(position);

            if (item == TITLE) {
                ((EditText) holder.itemView).setHint("Title");
            }
            else if (item == DESCRIPTION) {
                ((EditText) holder.itemView).setHint("Description");
            }
            else if (item == TYPE) {
                ((TextView) holder.itemView).setText("Type");
            }
            else if (item == INVITE_LINK_TITLE) {
                ((TextView) holder.itemView).setText("Invite Link");
            }
            else if (item == PRICE_TITLE) {
                ((TextView) holder.itemView).setText("Price");
            }
            else if (item == TYPE_PAYMENT) {
                ViewGroup container = (ViewGroup) holder.itemView;
                TextView text = (TextView) container.getChildAt(0);
                RadioButton radio = (RadioButton) container.getChildAt(1);

                text.setTag(OFFER_TYPE_PAYMENT);
                text.setText("Payment");
                radio.setChecked(OFFER_TYPE_PAYMENT.equals(mOfferType), true);
            }
            else if (item == TYPE_VIDEO_MEETING) {
                ViewGroup container = (ViewGroup) holder.itemView;
                TextView text = (TextView) container.getChildAt(0);
                RadioButton radio = (RadioButton) container.getChildAt(1);

                text.setTag(OFFER_TYPE_MEETING);
                text.setText("Video Meeting");
                radio.setChecked(OFFER_TYPE_MEETING.equals(mOfferType), true);
            }
            else if (item == INVITE_LINK_TOGGLE) {
                ((NotificationsCheckCell) holder.itemView).setChecked(mHasInviteLink);
            }
            else if (item == CURRENCY) {
                ((TextSettingsCell) holder.itemView).setTextAndValue("Currency", "USD", false);
                // TODO
            }
            else if (item == PRICE) {
                ((TextSettingsCell) holder.itemView).setTextAndValue("Price", "50", false);
                // TODO
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

    }

    private static EditText createEditText(Context context, int inputType, boolean imeDone, int maxLength) {
        EditTextBoldCursor editText = new EditTextBoldCursor(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
                setMeasuredDimension(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), getMeasuredHeightAndState());
            }
        };
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        editText.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        editText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        editText.setBackground(Theme.createEditTextDrawable(context, false));
        editText.setPadding(0, 0, 0, AndroidUtilities.dp(6));
        editText.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        editText.setInputType(inputType);
        editText.setImeOptions(imeDone ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
        if (maxLength > 0) {
            InputFilter[] inputFilters = new InputFilter[1];
            inputFilters[0] = new InputFilter.LengthFilter(maxLength);
            editText.setFilters(inputFilters);
        }
        editText.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        editText.setCursorSize(AndroidUtilities.dp(20));
        editText.setCursorWidth(1.5f);

        return editText;
    }

}

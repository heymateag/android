package org.telegram.ui.Heymate.offer;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.Offer;
import com.yashoid.sequencelayout.SequenceLayout;
import com.yashoid.sequencelayout.Span;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.Heymate.FileCache;
import org.telegram.ui.Heymate.LoadingUtil;
import org.telegram.ui.Heymate.MeetingType;
import org.telegram.ui.Heymate.ReferralUtils;
import org.telegram.ui.Heymate.TG2HM;
import works.heymate.core.offer.PricingInfo;
import org.telegram.ui.Heymate.payment.PaymentController;
import org.telegram.ui.Heymate.payment.WalletExistence;
import org.telegram.ui.Heymate.widget.OfferImagePlaceHolderDrawable;
import org.telegram.ui.Heymate.widget.RoundedCornersImageView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import works.heymate.beta.R;
import works.heymate.core.Texts;
import works.heymate.core.offer.OfferUtils;
import works.heymate.core.offer.PurchasePlanInfo;

public class OfferDetailsActivity extends BaseFragment implements OfferPricingView.OnPlanChangedListener {

    private static final long ANIMATION_DURATION = 200;

    private static final int BASIC_INFO = 0;
    private static final int CREATION_TIME = 1;
    private static final int EXPIRATION_TIME = 2;
    private static final int PRICING = 3;
    private static final int LOCATION = 4;
    private static final int PAYMENT_TERMS = 5;
    private static final int TERMS_AND_CONDITIONS = 6;
    private static final int REFERRAL = 7;
    private static final int FOOTER = 8;
    private static final int DIVIDER = 9;

    private static final int[] FULL_OFFER = {
            BASIC_INFO, CREATION_TIME, EXPIRATION_TIME, PRICING, DIVIDER, LOCATION, DIVIDER, PAYMENT_TERMS, DIVIDER, TERMS_AND_CONDITIONS, DIVIDER, REFERRAL, FOOTER
    };

    private static final int[] ONLINE_OFFER = {
            BASIC_INFO, CREATION_TIME, EXPIRATION_TIME, PRICING, DIVIDER, PAYMENT_TERMS, DIVIDER, TERMS_AND_CONDITIONS, DIVIDER, REFERRAL, FOOTER
    };

    private static final int[] OFFER_WITHOUT_REFERRAL = {
            BASIC_INFO, CREATION_TIME, EXPIRATION_TIME, PRICING, DIVIDER, LOCATION, DIVIDER, PAYMENT_TERMS, DIVIDER, TERMS_AND_CONDITIONS, FOOTER
    };

    private static final int[] ONLINE_OFFER_WITHOUT_REFERRAL = {
            BASIC_INFO, CREATION_TIME, EXPIRATION_TIME, PRICING, DIVIDER, PAYMENT_TERMS, DIVIDER, TERMS_AND_CONDITIONS, FOOTER
    };

    private final int mActionbarHeight = ActionBar.getCurrentActionBarHeight() + (Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
    private final int mMaxImageHeight = AndroidUtilities.dp(180) + (Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);

    private int mImageHeight = mMaxImageHeight;

    private Span mImageHeightSpan;
    private Span mForwardLeftPushSpan;
    private Span mForwardWidthSpan;
    private Span mForwardHeightSpan;

    private ValueAnimator mAnimator = null;
    private boolean mClosing;

    private View mToolbarHeader;
    private View mToolbar;
    private TextView mTitle;
    private RecyclerView mContentList;
    private RoundedCornersImageView mImage;

    private Offer mOffer = null;
    private OfferUtils.PhraseInfo mPhraseInfo = null;

    private PricingInfo mPricingInfo = null;
    private JSONObject mPaymentTerms = null;

    private int[] mRows = null;

    private String mSelectedPlan = null;

    @Override
    public View createView(Context context) {
        SequenceLayout content = (SequenceLayout) LayoutInflater.from(context).inflate(R.layout.activity_offerdetails, null, false);

        mImageHeightSpan = content.findSequenceById("image").getSpans().get(0);
        Span toolbarTopSpan = content.findSequenceById("toolbar").getSpans().get(0);
        Span toolbarSpan = content.findSequenceById("toolbar").getSpans().get(1);
        mForwardWidthSpan = content.findSequenceById("forward_width").getSpans().get(1);
        mForwardHeightSpan = content.findSequenceById("forward_height").getSpans().get(1);
        mForwardLeftPushSpan = content.findSequenceById("forward_width").getSpans().get(2);

        toolbarTopSpan.size = Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0;
        toolbarSpan.size = ActionBar.getCurrentActionBarHeight();

        mContentList = new RecyclerView(context) {

            @Override
            public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow, int type) {
                if (dy > 0) {
                    if (dy < mImageHeight - mActionbarHeight) {
                        mImageHeight -= dy;
                        consumed[1] += dy;
                    }
                    else {
                        consumed[1] += mImageHeight - mActionbarHeight;
                        mImageHeight = mActionbarHeight;
                    }
                }
                else {
                    View firstView = findFirstView();

                    if (firstView != null && firstView.getTop() - mImageHeight == 0) {
                        if (mImageHeight - dy < mMaxImageHeight) {
                            consumed[1] += dy;
                            mImageHeight -= dy;
                        }
                        else {
                            int reminder = mMaxImageHeight - mImageHeight;
                            consumed[1] -= reminder;
                            mImageHeight = mMaxImageHeight;
                        }
                    }
                }

                onImageHeightChanged();

                return super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
            }

            private View findFirstView() {
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    ViewHolder holder = getChildViewHolder(child);

                    if (holder != null && holder.getAdapterPosition() == 0) {
                        return child;
                    }
                }

                return null;
            }

        };

        mContentList.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mContentList.setPadding(0, mImageHeight, 0, 0);
        mContentList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mContentList.setAdapter(mAdapter);

        ((ViewGroup) content.findViewById(R.id.list)).addView(mContentList, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        mToolbarHeader = content.findViewById(R.id.toolbar_header);
        mToolbarHeader.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        mToolbarHeader.setAlpha(0);

        mToolbar = content.findViewById(R.id.toolbar);
        mToolbar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        mToolbar.setAlpha(0);

        mTitle = content.findViewById(R.id.title);
        mTitle.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        mTitle.setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle));
        mTitle.setTextSize(!AndroidUtilities.isTablet() && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 18 : 20);
        mTitle.setText("Offer Details"); // TODO Texts
        mTitle.setAlpha(0);

        mImage = content.findViewById(R.id.offer_image);
        ImageView forward = content.findViewById(R.id.forward);
        ImageView back = content.findViewById(R.id.back);
        ImageView more = content.findViewById(R.id.more);
        TextView promote = content.findViewById(R.id.promote);
        TextView book = content.findViewById(R.id.book);

        BackDrawable backDrawable = new BackDrawable(false);
        backDrawable.setColor(Theme.getColor(Theme.key_actionBarDefaultIcon));
        back.setImageDrawable(backDrawable);
        back.setOnClickListener(v -> finishFragment());

        more.setImageDrawable(TG2HM.getThemedDrawable(R.drawable.ic_ab_other, Theme.getColor(Theme.key_actionBarDefaultIcon)));

        promote.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        promote.setText("Promote"); // TODO Texts
        promote.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Theme.getColor(Theme.key_windowBackgroundWhite)));
        promote.setOnClickListener(v -> WalletExistence.ensure(() -> promote(true)));

        book.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));
        book.setText("Book Now");
        book.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Theme.getColor(Theme.key_chats_actionBackground)));
        book.setOnClickListener(v -> initPayment());

        forward.setImageDrawable(TG2HM.getThemedDrawable(R.drawable.ic_ab_forward, Theme.getColor(Theme.key_chats_actionIcon)));
        forward.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(40), Theme.getColor(Theme.key_chats_actionBackground)));
        forward.setOnClickListener(v-> WalletExistence.ensure(() -> promote(false)));

        mImage.setCornerRadius(0, 0, AndroidUtilities.dp(12), AndroidUtilities.dp(12));

        fragmentView = content;

        onImageHeightChanged();

        checkPhoto();

        return content;
    }

    public void setOffer(Offer offer, OfferUtils.PhraseInfo phraseInfo) {
        mOffer = offer;
        mPhraseInfo = phraseInfo;

        if (mOffer == null) {
            mPhraseInfo = null;

            mPricingInfo = null;
            mPaymentTerms = null;

            mRows = null;

            mAdapter.notifyDataSetChanged();
            return;
        }

        checkPhoto();

        try {
            mPricingInfo = new PricingInfo(new JSONObject(offer.getPricingInfo()));
            mPaymentTerms = new JSONObject(offer.getTermsConfig());
        } catch (JSONException e) {
            mPricingInfo = null;
            mPaymentTerms = null;
        }

        boolean onlineMeeting = MeetingType.ONLINE_MEETING.equals(mOffer.getMeetingType());
        boolean hasReferralPlan = false;

        try {
            if (mPaymentTerms != null && mPaymentTerms.getDouble(OfferUtils.PROMOTION_RATE) > 0) {
                hasReferralPlan = true;
            }
        } catch (JSONException e) { }

        if (onlineMeeting) {
            mRows = hasReferralPlan ? ONLINE_OFFER : ONLINE_OFFER_WITHOUT_REFERRAL;
        }
        else {
            mRows = hasReferralPlan ? FULL_OFFER : OFFER_WITHOUT_REFERRAL;
        }

        mAdapter.notifyDataSetChanged();
    }

    private void checkPhoto() {
        if (mImage == null || mOffer == null) {
            return;
        }

        if (mOffer.getHasImage() != null && mOffer.getHasImage()) {
            FileCache.get().getImage(mOffer.getId(), AndroidUtilities.displaySize.x, (success, drawable, exception) -> {
                if (drawable != null) {
                    mImage.setImageDrawable(drawable);
                }
                else {
                    mImage.setImageDrawable(getImagePlaceHolder());
                }
            });
        }
        else {
            mImage.setImageDrawable(getImagePlaceHolder());
        }
    }

    private Drawable getImagePlaceHolder() {
        OfferImagePlaceHolderDrawable drawable = new OfferImagePlaceHolderDrawable(true);

        int tagOffset = Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0;
        tagOffset = tagOffset * drawable.getIntrinsicHeight() / mMaxImageHeight;

        drawable.setTagOffset(tagOffset);

        return drawable;
    }

    private void onImageHeightChanged() {
        mContentList.setPadding(0, mImageHeight, 0, 0);
        mImageHeightSpan.size = mImageHeight;

        if (mImageHeight == mActionbarHeight) {
            if (mAnimator != null) {
                if (mClosing) {
                    return;
                }

                mAnimator.cancel();
                mAnimator = null;
            }
            else if (mClosing) {
                return;
            }

            mClosing = true;

            startAnimation(0);
        }
        else {
            if (mAnimator != null) {
                if (!mClosing) {
                    return;
                }

                mAnimator.cancel();
                mAnimator = null;
            }
            else if (!mClosing) {
                return;
            }

            mClosing = false;

            startAnimation(40);
        }

        fragmentView.requestLayout();
    }

    private void startAnimation(float targetValue) {
        mAnimator = new ValueAnimator();
        mAnimator.setFloatValues(mForwardWidthSpan.size, targetValue);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.addUpdateListener(mAnimatorUpdateListener);
        mAnimator.addListener(mAnimatorListener);
        mAnimator.start();
    }

    private final ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = animation -> {
        float size = (float) animation.getAnimatedValue();
        mForwardWidthSpan.size = size;
        mForwardHeightSpan.size = size;
        mForwardLeftPushSpan.size = 20 - mForwardWidthSpan.size /2f;

        float alpha = mClosing ? animation.getAnimatedFraction() : 1 - animation.getAnimatedFraction();
        mToolbarHeader.setAlpha(alpha);
        mToolbar.setAlpha(alpha);
        mTitle.setAlpha(alpha);

        fragmentView.requestLayout();
    };

    private final Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationEnd(Animator animation) {
            mAnimator = null;
        }

        @Override public void onAnimationStart(Animator animation) { }
        @Override public void onAnimationCancel(Animator animation) { }
        @Override public void onAnimationRepeat(Animator animation) { }

    };

    @Override
    public void onPlanChanged(String plan) {
        mSelectedPlan = plan;
    }

    private void initPayment() {
        if (mSelectedPlan == null || mOffer == null || mPricingInfo == null) {
            return;
        }

        PurchasePlanInfo purchasePlanInfo = mPricingInfo.getPurchasePlanInfo(mSelectedPlan);

        PaymentController.get(getParentActivity()).initPayment(mOffer.getId(), purchasePlanInfo.type, mPhraseInfo == null ? null : mPhraseInfo.referralId);
    }

    private void promote(boolean share) {
        if (mOffer == null) {
            return;
        }

        if (mPhraseInfo == null || mOffer.getUserId() != null && mOffer.getUserId().equals(String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId))) {
            doPromote(null, share);
            return;
        }

        LoadingUtil.onLoadingStarted();

        ReferralUtils.getReferralId(mPhraseInfo, (success, referralId, exception) -> {
            LoadingUtil.onLoadingFinished();

            if (getParentActivity() == null) {
                return;
            }

            if (!success) {
                // TODO Organize error messages
                Toast.makeText(getParentActivity(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
                return;
            }

            doPromote(referralId, share);
        });
    }

    private void doPromote(String referralId, boolean share) {
        String message = OfferUtils.serializeBeautiful(mOffer, referralId, mOffer.getUserId(), OfferUtils.CATEGORY, OfferUtils.EXPIRY);

        if (share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            getParentActivity().startActivity(Intent.createChooser(shareIntent, LocaleController.getString("HtPromoteYourOffer", works.heymate.beta.R.string.HtPromoteYourOffer)));
        }
        else {
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            args.putInt("dialogsType", 3);
            args.putInt("messagesCount", 1);
            args.putInt("hasPoll", 0);
            DialogsActivity fragment = new DialogsActivity(args);
            fragment.setDelegate((fragment1, dids, message1, param) -> {
                if (dids.size() > 1 || dids.get(0) == getUserConfig().getClientUserId()) {
                    for (int a = 0; a < dids.size(); a++) {
                        long did = dids.get(a);
                        SendMessagesHelper.getInstance(getCurrentAccount()).sendMessage(message, did, null, null, null, false, null, null, null, true, 0);
                    }
                    fragment1.finishFragment();
                } else {
                    long did = dids.get(0);

                    SendMessagesHelper.getInstance(getCurrentAccount()).sendMessage(message, did, null, null, null, false, null, null, null, true, 0);

                    int lower_part = (int) did;
                    int high_part = (int) (did >> 32);
                    Bundle args1 = new Bundle();
                    // args1.putBoolean("scrollToTopOnResume", scrollToTopOnResume);
                    if (lower_part != 0) {
                        if (lower_part > 0) {
                            args1.putInt("user_id", lower_part);
                        } else {
                            args1.putInt("chat_id", -lower_part);
                        }
                    } else {
                        args1.putInt("enc_id", high_part);
                    }
                    if (lower_part != 0) {
                        if (!getMessagesController().checkCanOpenChat(args1, fragment1)) {
                            return;
                        }
                    }
                    if (presentFragment(new ChatActivity(args1), true)) {
                        if (!AndroidUtilities.isTablet()) {
                            removeSelfFromStack();
                        }
                    } else {
                        fragment1.finishFragment();
                    }
                }
            });

            presentFragment(fragment);
        }
    }

    private final RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        @Override
        public int getItemViewType(int position) {
            return mRows[position];
        }

        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View view;

            switch (viewType) {
                case BASIC_INFO:
                    view = createBasicInfo(parent);
                    break;
                case CREATION_TIME:
                    view = createTime(parent, "Created", R.drawable.ic_schedule);
                    break;
                case EXPIRATION_TIME:
                    view = createTime(parent, "Expiration", R.drawable.ic_expiry);
                    break;
                case PRICING:
                    view = createPricing(parent);
                    break;
                case LOCATION:
                    view = createLocation(parent);
                    break;
                case PAYMENT_TERMS:
                    view = createPaymentTerms(parent);
                    break;
                case TERMS_AND_CONDITIONS:
                    view = createTermsAndConditions(parent);
                    break;
                case REFERRAL:
                    view = createReferral(parent);
                    break;
                case FOOTER:
                    view = new View(parent.getContext());
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
                    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(84)));
                    break;
                case DIVIDER:
                    view = new View(parent.getContext());
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
                    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(12)));
                    break;
                default:
                    throw new IllegalArgumentException("Undefined view type.");
            }

            return new RecyclerView.ViewHolder(view) {};
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case BASIC_INFO:
                    bindBasicInfo(holder.itemView);
                    return;
                case CREATION_TIME:
                    bindCreationTime(holder.itemView);
                    return;
                case EXPIRATION_TIME:
                    bindExpirationTime(holder.itemView);
                    return;
                case PRICING:
                    bindPricing(holder.itemView);
                    return;
                case LOCATION:
                    bindLocation(holder.itemView);
                    return;
                case PAYMENT_TERMS:
                    bindPaymentTerms(holder.itemView);
                    return;
                case TERMS_AND_CONDITIONS:
                    bindTermsAndConditions(holder.itemView);
                    return;
                case REFERRAL:
                    bindReferral(holder.itemView);
                    return;
            }
        }

        @Override
        public int getItemCount() {
            return mRows == null ? 0 : mRows.length;
        }

    };

    private View createBasicInfo(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_basicinfo, parent, false);

        TextView title = view.findViewById(R.id.title);
        TextView category = view.findViewById(R.id.category);
        TextView description = view.findViewById(R.id.description);

        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        category.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        description.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        return view;
    }

    private void bindBasicInfo(View view) {
        TextView title = view.findViewById(R.id.title);
        TextView category = view.findViewById(R.id.category);
        TextView description = view.findViewById(R.id.description);

        title.setText(mOffer.getTitle());
        category.setText(mOffer.getSubCategory());
        description.setText(mOffer.getDescription());
    }

    private View createTime(ViewGroup parent, String titleText, int iconResId) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_time, parent, false);

        ImageView icon = view.findViewById(R.id.icon);
        TextView title = view.findViewById(R.id.title);
        TextView time = view.findViewById(R.id.time);

        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        time.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        icon.setImageDrawable(TG2HM.getThemedDrawable(iconResId, Theme.getColor(Theme.key_windowBackgroundWhiteGrayText)));
        title.setText(titleText);

        return view;
    }

    private void bindCreationTime(View view) {
        TextView time = view.findViewById(R.id.time);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d y", Locale.getDefault());
        time.setText(dateFormat.format(mOffer.getCreatedAt().toDate()));
    }

    private void bindExpirationTime(View view) {
        TextView time = view.findViewById(R.id.time);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d y", Locale.getDefault());
        time.setText(dateFormat.format(mOffer.getExpiry().toDate()));
    }

    private View createPricing(ViewGroup parent) {
        OfferPricingView view = new OfferPricingView(parent.getContext());
        view.setBottomPadding(24);
        view.setOnPlanChangedListener(OfferDetailsActivity.this);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    private void bindPricing(View view) {
        OfferPricingView offerPricingView = (OfferPricingView) view;
        offerPricingView.setPricingInfo(mPricingInfo, mSelectedPlan);
    }

    private View createLocation(ViewGroup parent) {
        View view = new OfferLocationView(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    private void bindLocation(View view) {
        OfferLocationView locationView = (OfferLocationView) view;
        locationView.setAddress(mOffer.getLocationData());

        try {
            double latitude = Double.parseDouble(mOffer.getLatitude());
            double longitude = Double.parseDouble(mOffer.getLongitude());

            locationView.setLocation(latitude, longitude);
        } catch (NumberFormatException e) { }
    }

    private View createPaymentTerms(ViewGroup parent) {
        View view = new OfferPaymentTermsView(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    private void bindPaymentTerms(View view) {
        ((OfferPaymentTermsView) view).setPaymentTerms(mPaymentTerms);
    }

    private View createTermsAndConditions(ViewGroup parent) {
        View view = new OfferTermsView(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    private void bindTermsAndConditions(View view) {
        ((OfferTermsView) view).setTerms(mOffer.getTerms());
    }

    private View createReferral(ViewGroup parent) {
        View view = new OfferReferralView(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    private void bindReferral(View view) {
        try {
            int promotionPercent = mPaymentTerms.getInt(OfferUtils.PROMOTION_RATE);
            ((OfferReferralView) view).setReferralAmount(promotionPercent);
        } catch (JSONException e) { }
    }

    @Override
    protected void clearViews() {
        for (int i = 0; i < mContentList.getChildCount(); i++) {
            if (mContentList.getChildAt(i) instanceof OfferLocationView) {
                ((OfferLocationView) mContentList.getChildAt(i)).onDestroy();
            }
        }

        super.clearViews();
    }

    @Override
    protected void setParentLayout(ActionBarLayout layout) {
        if (parentLayout != layout) {
            parentLayout = layout;
            inBubbleMode = parentLayout != null && parentLayout.isInBubbleMode();
            if (fragmentView != null) {
                ViewGroup parent = (ViewGroup) fragmentView.getParent();
                if (parent != null) {
                    try {
                        onRemoveFromParent();
                        parent.removeViewInLayout(fragmentView);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                if (parentLayout != null && parentLayout.getContext() != fragmentView.getContext()) {
                    fragmentView = null;
                }
            }
            if (actionBar != null) {
                boolean differentParent = parentLayout != null && parentLayout.getContext() != actionBar.getContext();
                if (actionBar.shouldAddToContainer() || differentParent) {
                    ViewGroup parent = (ViewGroup) actionBar.getParent();
                    if (parent != null) {
                        try {
                            parent.removeViewInLayout(actionBar);
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                }
                if (differentParent) {
                    actionBar = null;
                }
            }
        }
    }

}

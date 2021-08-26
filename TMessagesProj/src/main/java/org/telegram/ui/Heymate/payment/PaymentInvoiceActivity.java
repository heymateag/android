package org.telegram.ui.Heymate.payment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.ActivityMonitor;
import org.telegram.ui.Heymate.Constants;
import org.telegram.ui.Heymate.FileCache;
import org.telegram.ui.Heymate.HeymateRouter;
import org.telegram.ui.Heymate.HtAmplify;
import works.heymate.core.offer.PricingInfo;
import org.telegram.ui.Heymate.widget.RoundedCornersImageView;

import works.heymate.beta.R;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.core.offer.PurchasePlanInfo;
import works.heymate.core.offer.PurchasePlanTypes;

public class PaymentInvoiceActivity extends BaseFragment {

    public static final String HOST = "invoice";

    public static Intent getIntent(Context context, String offerId, String purchasedPlanType, Money walletBalance) {
        Bundle args = new Bundle();
        args.putString(Constants.OFFER_ID, offerId);
        args.putString(Constants.PURCHASED_PLAN_TYPE, purchasedPlanType);
        args.putParcelable(Constants.MONEY, walletBalance);

        return HeymateRouter.createIntent(context, HOST, args);
    }

    private RoundedCornersImageView mOfferImage;
    private TextView mOfferTitle;
    private TextView mOfferCategory;
    private TextView mPurchasedPlanType;
    private TextView mPurchasedPlanDescription;
    private ViewGroup mInvoice;
    private TextView mPay;

    private Money mTotalPayment = null;

    public PaymentInvoiceActivity(Bundle args) {
        super(args);
    }

    @Override
    public View createView(Context context) {
        ActionBar actionBar = getActionBar();
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setTitle("Payment Invoice"); // TODO TEXTS
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (getParentActivity() == null) {
                    return;
                }

                if (id == -1) {
                    finishFragment();
                }
            }
        });

        View content = LayoutInflater.from(context).inflate(R.layout.activity_paymentinvoice, null, false);

        mOfferImage = content.findViewById(R.id.offer_image);
        mOfferTitle = content.findViewById(R.id.offer_title);
        mOfferCategory = content.findViewById(R.id.offer_category);
        mPurchasedPlanType = content.findViewById(R.id.purchased_plan_type);
        mPurchasedPlanDescription = content.findViewById(R.id.purchased_plan_details);
        mInvoice = content.findViewById(R.id.invoice);
        mPay = content.findViewById(R.id.pay);

        content.findViewById(R.id.divider).setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        mOfferImage.setCornerRadius(AndroidUtilities.dp(8));
        mOfferTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mOfferCategory.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mPurchasedPlanType.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mPurchasedPlanDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mPay.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        mPay.setBackgroundColor(Theme.getColor(Theme.key_wallet_greenText));

        mPay.setOnClickListener(v -> {
            if (mTotalPayment != null) {
                ActivityMonitor.get().getCurrentActivity().startActivity(PaymentMethodSelectionActivity.getIntent(getParentActivity(), mTotalPayment));
                finishFragment();
            }
        });

        Bundle args = getArguments();
        String offerId = args.getString(Constants.OFFER_ID);

        HtAmplify.getInstance(getParentActivity()).getOffer(offerId, (success, offer, exception) -> {
            if (offer == null) {
                finishFragment();
                return;
            }

            if (offer.getHasImage() != null && offer.getHasImage()) {
                FileCache.get().getImage(offer.getId(), AndroidUtilities.dp(80), (success1, drawable, exception1) -> mOfferImage.setImageDrawable(drawable));
            }
            else {
                mOfferImage.setVisibility(View.GONE);
            }

            mOfferTitle.setText(offer.getTitle());
            mOfferCategory.setText(offer.getCategory());

            PricingInfo pricingInfo;

            try {
                pricingInfo = new PricingInfo(new JSONObject(offer.getPricingInfo()));
            } catch (JSONException e) {
                finishFragment();
                return;
            }

            String purchasedPlanType = args.getString(Constants.PURCHASED_PLAN_TYPE);
            Money walletBalance = args.getParcelable(Constants.MONEY);

            switch (purchasedPlanType) {
                case PurchasePlanTypes.SINGLE:
                    mPurchasedPlanType.setText("Single");
                    mPurchasedPlanDescription.setText(" - 1 session");
                    break;
                case PurchasePlanTypes.BUNDLE:
                    mPurchasedPlanType.setText("Bundle");
                    mPurchasedPlanDescription.setText(" - " + pricingInfo.bundleCount + " sessions");
                    break;
                case PurchasePlanTypes.SUBSCRIPTION:
                    mPurchasedPlanType.setText("Subscription");
                    mPurchasedPlanDescription.setText(" - unlimited sessions " + pricingInfo.subscriptionPeriod);
                    break;
            }

            PurchasePlanInfo planInfo = pricingInfo.getPurchasePlanInfo(purchasedPlanType);
            Money servicePrice = planInfo.price;

            if (PurchasePlanTypes.BUNDLE.equals(purchasedPlanType)) {
                Money realPrice = Money.create(pricingInfo.price * 100, pricingInfo.currency).multiplyBy(pricingInfo.bundleCount);
                Money discount = realPrice.minus(servicePrice);

                addInvoiceRow("Service Price", realPrice);
                addInvoiceRow("Discount", discount);
            }
            else {
                addInvoiceRow("Service Price", servicePrice);
            }

            Money fee = Money.create(PaymentController.GAS_ADJUST_CENTS, pricingInfo.currency);

            addInvoiceRow("Fee", fee);
            addInvoiceRow("Wallet Balance", walletBalance);

            mTotalPayment = servicePrice.plus(fee).minus(walletBalance);
            addInvoiceRow("Total Payment", mTotalPayment);

            mPay.setText("Pay " + mTotalPayment.toString());
        });

        fragmentView = content;

        return content;
    }

    private void addInvoiceRow(String title, Money amount) {
        final Context context = getParentActivity();

        if (context == null) {
            return;
        }

        FrameLayout row = new FrameLayout(context);

        TextView textTitle = new TextView(context);
        textTitle.setTextSize(16);
        textTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textTitle.setText(title);
        row.addView(textTitle, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL));

        TextView textAmount = new TextView(context);
        textAmount.setTextSize(16);
        textAmount.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        textAmount.setText(amount.toString());
        row.addView(textAmount, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL));

        mInvoice.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40));
    }

}

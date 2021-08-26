package org.telegram.ui.Heymate.payment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Heymate.Constants;
import org.telegram.ui.Heymate.HeymateRouter;

import works.heymate.core.Money;

public class PaymentMethodSelectionActivity extends BaseFragment {

    public static final String HOST = "payment";

    public static Intent getIntent(Context context, Money amount) {
        Bundle args = new Bundle();
        args.putParcelable(Constants.MONEY, amount);

        return HeymateRouter.createIntent(context, HOST, args);
    }

    public PaymentMethodSelectionActivity(Bundle args) {
        super(args);
    }

    @Override
    public View createView(Context context) {
        ActionBar actionBar = getActionBar();
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setTitle("Payment Method"); // TODO TEXTS
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

        Money amount = getArguments().getParcelable(Constants.MONEY);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        for (PaymentMethod paymentMethod: PaymentMethod.PAYMENT_METHODS) {
            if (paymentMethod.supportedCurrencies.contains(amount.getCurrency())) {
                View view = paymentMethod.createView(context, amount.getCurrency());

                view.setOnClickListener(v -> {
                    if (paymentMethod.execute(context, amount)) {
                        finishFragment();
                    }
                });

                content.addView(view);
            }
        }

        fragmentView = content;

        return content;
    }

}

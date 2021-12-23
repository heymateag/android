package org.telegram.ui.Heymate;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.log.HMLog;

import works.heymate.celo.CeloContext;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.core.Texts;
import works.heymate.core.wallet.Wallet;

public class EasterActivity extends BaseFragment {

    private Wallet wallet;

    private EditText editAmount;

    @Override
    public View createView(Context context) {
        ActionBar actionBar = getActionBar();
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setTitle("Heymate Status");
        actionBar.createMenu().addItem(1, "Report");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                switch (id) {
                    case -1:
                        finishFragment();
                        return;
                    case 1:
                        HMLog.report();
                        Toast.makeText(getParentActivity(), "Done", Toast.LENGTH_LONG).show();
                        return;
                }
            }
        });

        LinearLayout content = new LinearLayout(context);
        content.setBackgroundColor(0xFFFFFFFF);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(72, 72, 72, 72);

        Spinner currencySpinner = new Spinner(context);
        currencySpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, new String[] {Currency.USD.name(), Currency.EUR.name()}));
        currencySpinner.setSelection(TG2HM.getDefaultCurrency() == Currency.USD ? 0 : 1);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TG2HM.defaultCurrency = position == 0 ? Currency.USD : Currency.EUR;

                editAmount.setText("...");
                refresh();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }

        });
        content.addView(currencySpinner, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        TextView textAmount = new TextView(context);
        textAmount.setText("Amount in cents - 1 cent less than the actual balance for gas safety.");
        content.addView(textAmount, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        editAmount = new EditText(context);
        editAmount.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        content.addView(editAmount, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        wallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());

        TextView textDestination = new TextView(context);
        textDestination.setText("Destination address");
        content.addView(textDestination, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 12, 0, 0));

        EditText editDestination = new EditText(context);
        editDestination.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        editDestination.setHint("Address");
        editDestination.setSingleLine(true);
        editDestination.setMaxLines(1);
        content.addView(editDestination, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        Button buttonTransfer = new Button(context);
        buttonTransfer.setText("Transfer");
        content.addView(buttonTransfer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 12, 0, 0, 0));

        buttonTransfer.setOnClickListener(v -> {
            if (!wallet.isCreated()) {
                Toast.makeText(getParentActivity(), "Wallet not created", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long amount = Long.parseLong(editAmount.getText().toString());
                String destination = editDestination.getText().toString();

                if (destination.length() == 0) {
                    Toast.makeText(context, "Destination not entered.", Toast.LENGTH_SHORT).show();
                    return;
                }

                LoadingUtil.onLoadingStarted();

                wallet.transfer(Money.create(amount, TG2HM.getDefaultCurrency()), destination, (success, error) -> {
                    LoadingUtil.onLoadingFinished();

                    new AlertDialog.Builder(context)
                            .setTitle(success ? "Done" : "Failed")
                            .setMessage(success ? "We are fine!" : error)
                            .setNeutralButton("OK", (dialog, which) -> {
                                dialog.dismiss();
                                if (success) {
                                    finishFragment(true);
                                }
                            })
                            .setCancelable(false)
                            .show();
                });
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Amount is not a number.", Toast.LENGTH_SHORT).show();
            }
        });

        String text = HeymateConfig.PRODUCTION ? "Production" : "Staging";
        text += HeymateConfig.MAIN_NET ? "\nMain net" : "\nTest net (Alfajores)";
        text += "\ndebug: " + HeymateConfig.DEBUG;
        text += "\ndemo: " + HeymateConfig.DEMO;
        text += "\nversion: " + HeymateConfig.INTERNAL_VERSION + "\n\n";
        text += "Your address is:\n" + (wallet.isCreated() ? wallet.getAddress() : "Wallet hasn't been created.");

        TextView address = new TextView(context);
        address.setPadding(0, 0, 0, AndroidUtilities.dp(24));
        address.setTextIsSelectable(true);
        address.setTextSize(12);
        address.setText(text);
        address.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        content.addView(address, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f));

        fragmentView = content;

        return content;
    }

    private void refresh() {
        if (!wallet.isCreated()) {
            wallet.createNew();
            editAmount.setText("0");
        }
        else {
            wallet.getBalance((success, cUSD, cEUR, errorCause) -> {
                Money balance = TG2HM.getDefaultCurrency() == Currency.USD ? cUSD : cEUR;

                if (success) {
                    editAmount.setText("" + (balance.getCents()  - 1));
                }
                else {
                    editAmount.setText("Failed to get balance!");
                }
            });
        }
    }

    @Override
    protected void clearViews() {
        ((FrameLayout) fragmentView).removeAllViews();
        super.clearViews();
    }

}

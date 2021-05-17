package org.telegram.ui.Heymate;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;

import works.heymate.core.wallet.Wallet;

public class EasterActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(72, 72, 72, 72);

        TextView textAmount = new TextView(context);
        textAmount.setText("Amount in cents - 10 cents less than the actual balance for gas safety.");
        content.addView(textAmount, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        EditText editAmount = new EditText(context);
        editAmount.setInputType(EditorInfo.TYPE_CLASS_NUMBER);

        Wallet wallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());

        if (!wallet.isCreated()) {
            wallet.createNew();
            editAmount.setText("0");
        }
        else {
            wallet.getBalance((success, cents, errorCause) -> {
                if (success) {
                    editAmount.setText("" + cents);
                }
                else {
                    editAmount.setText("Failed to get balance!");
                }
            });
        }
        content.addView(editAmount, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

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
            try {
                long amount = Long.parseLong(editAmount.getText().toString());
                String destination = editDestination.getText().toString();

                if (destination.length() == 0) {
                    Toast.makeText(context, "Destination not entered.", Toast.LENGTH_SHORT).show();
                    return;
                }

                LoadingUtil.onLoadingStarted(context);

                wallet.transfer(amount, destination, (success, error) -> {
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

        return content;
    }

    @Override
    protected void clearViews() {
        ((FrameLayout) fragmentView).removeAllViews();
        super.clearViews();
    }

}

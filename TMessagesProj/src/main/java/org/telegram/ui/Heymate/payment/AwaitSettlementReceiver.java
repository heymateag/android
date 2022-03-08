package org.telegram.ui.Heymate.payment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AwaitSettlementReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, AwaitSettlementService.class));
    }

}

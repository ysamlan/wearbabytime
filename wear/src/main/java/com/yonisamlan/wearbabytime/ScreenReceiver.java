package com.yonisamlan.wearbabytime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            context.startActivity(new Intent(context, MainActivity.class).addFlags(
                    Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        }
    }
}

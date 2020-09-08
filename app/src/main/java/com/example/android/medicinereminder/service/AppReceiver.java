package com.example.android.medicinereminder.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            //if the intent is not null
            //get the action
            final String action = intent.getAction();

            if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                //create intent to AppService
                Intent rescheduleIntent = new Intent(context, AppService.class);

                //set the action of it to reschedule
                rescheduleIntent.setAction(AppService.ACTION_RESCHEDULE);

                //start the AppService from the created intent
                context.startService(rescheduleIntent);
            }
        }
    }
}

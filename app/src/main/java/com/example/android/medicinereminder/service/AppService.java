package com.example.android.medicinereminder.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.android.medicinereminder.db.AppDatabase;
import com.example.android.medicinereminder.db.MedicineDao;
import com.example.android.medicinereminder.model.Medicine;
import com.example.android.medicinereminder.ui.MainActivity;
import com.example.android.medicinereminder.util.MedicineAlarmUtil;
import com.example.android.medicinereminder.util.MedicineTime;
import com.example.android.medicinereminder.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class AppService extends IntentService {

    public static final String ACTION_NOTIFICATION = "notification";
    public static final String ACTION_RESCHEDULE = "reschedule";
    public static final String ACTION_RESET_TAKEN_TIMES = "reset_taken_times";

    private static final String ACTION_INCREASE = "increase";
    private static final String ACTION_NOTHING = "nothing";

    public static final String MEDICINE_KEY = "medicine";
    private static final String MEDICINE_ID_KEY = "medicine_id";

    private final String CHANNEL_ID = "Daily Medicine";
    private final String NOTIFICATIONS_GROUP_KEY = "DailyMedicineNotificationsGroupKey";


    public AppService() {
        super("AppService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            //get the action of the intent
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_NOTIFICATION:
                        //if action == notification
                        //get the medicine as string from the intent
                        //then create a new Medicine object using gson library
                        Medicine medicine = new Gson().fromJson(intent.getStringExtra(MEDICINE_KEY), Medicine.class);
                        //push the notification
                        pushNotification(medicine);
                        break;
                    case ACTION_RESCHEDULE:
                        //if action == reschedule
                        //reschedule the alarm based of times from the database
                        rescheduleAlarms();
                        break;
                    case ACTION_INCREASE:
                        //if action == increase
                        //get the id from the intent
                        int id1 = intent.getIntExtra(MEDICINE_ID_KEY, -1);

                        //increase the taken time using the passed medicine id
                        IncreaseMedicineTakenTimes(id1);
                        break;
                    case ACTION_NOTHING:
                        //if action == nothing
                        //get the id from the intent
                        int id2 = intent.getIntExtra(MEDICINE_ID_KEY, -1);

                        //cancel the notification
                        cancelNotification(id2);
                        break;
                    case ACTION_RESET_TAKEN_TIMES:
                        //reset all medicines taken times for a new day
                        resetAllMedicinesTakenTimes();
                        break;
                }
            }
        }
    }

    private void pushNotification(@NonNull Medicine medicine) {
        //get medicine id
        final int medicineId = medicine.getId();

        //region notification intents
        //create intent to the MainActivity
        Intent tapIntent = new Intent(getApplication(), MainActivity.class);

        //add flags to handle the stack properly
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //create the pending intent
        PendingIntent tapPendingIntent = PendingIntent.getActivity(
                this,
                -1,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //create intent to AppService
        Intent actionDoneIntent = new Intent(getApplication(), AppService.class);

        //set the intent action to increase
        actionDoneIntent.setAction(ACTION_INCREASE);

        //add the medicine id
        actionDoneIntent.putExtra(MEDICINE_ID_KEY, medicineId);

        //create the pending intent
        PendingIntent actionDonePendingIntent = PendingIntent.getService(
                this,
                2 * medicineId - 1,
                actionDoneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //create intent to AppService
        Intent actionCancelIntent = new Intent(getApplication(), AppService.class);

        //set the intent action to nothing
        actionCancelIntent.setAction(ACTION_NOTHING);

        //add the medicine id
        actionCancelIntent.putExtra(MEDICINE_ID_KEY, medicineId);

        //create the pending intent
        PendingIntent actionCancelPendingIntent = PendingIntent.getService(
                this,
                2 * medicineId,
                actionCancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //endregion notification intents

        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Don't forget your medicine: " + medicine.getName())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setGroup(NOTIFICATIONS_GROUP_KEY)
                .setVibrate(new long[]{100, 100, 100})
                .setAutoCancel(false)
                .setOngoing(true)
                .setVisibility(VISIBILITY_PUBLIC)
                .setContentIntent(tapPendingIntent)
                .addAction(R.drawable.ic_done_white_24dp, "Took it", actionDonePendingIntent)
                .addAction(R.drawable.ic_cancel_white_24dp, "Cancel", actionCancelPendingIntent);

        Bitmap picture = medicine.getPicture();
        if (picture != null) {
            builder.setLargeIcon(picture);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        //push the notification
        notificationManager.notify(medicineId, builder.build());

        //schedule the next alarm
        scheduleNextAlarm(medicine);
    }

    private void scheduleNextAlarm(@NonNull Medicine medicine) {
        ArrayList<MedicineTime> alarmTimes = medicine.getAlarmTimes();
        int numOfAlarms = alarmTimes.size();
        int i = 0;

        //get instance of MedicineDao
        MedicineDao dao = AppDatabase.getInstance(this).medicineDao();

        //get instance of MedicineAlarmUtil
        MedicineAlarmUtil alarmUtil = MedicineAlarmUtil.getInstance(this.getApplication());

        int dayInMills = 0;

        for (; i < numOfAlarms; ++i) {
            MedicineTime time = alarmTimes.get(i);
            long triggerTime = alarmUtil.createTriggerTime(time.getHourOfDay(), time.getMinute()) + dayInMills;

            if (triggerTime > System.currentTimeMillis()) {
                //schedule the alarm
                alarmUtil.set(triggerTime, medicine);

                //update the medicine with the next alarm
                medicine.setNextAlarm(new Gson().toJson(time));
                dao.updateMedicine(medicine);

                break;
            }

            //if no alarm was set
            if ((i + 1) == numOfAlarms) {
                //add a day to triggerTime
                dayInMills = 1000 * 60 * 60 * 24;

                i = -1; //will increase to 0 before next loop
            }
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void rescheduleAlarms() {
        //get instance of MedicineDao
        MedicineDao dao = AppDatabase.getInstance(this).medicineDao();

        //get instance of MedicineAlarmUtil
        MedicineAlarmUtil alarmUtil = MedicineAlarmUtil.getInstance(getApplication());

        //get all  medicines
        final List<Medicine> medicines = dao.loadAllMedicinesAsList();

        //if the list is not null
        if (medicines != null) {
            for (Medicine medicine : medicines) {
                //cancel any existed alarm
                alarmUtil.cancelAlarm(medicine);

                //schedule the next alarm
                scheduleNextAlarm(medicine);
            }
        }
    }

    private void cancelNotification(int id) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        //cancel the notification with the id
        notificationManager.cancel(id);
    }

    private void IncreaseMedicineTakenTimes(int id) {
        //get instance of MedicineDao
        MedicineDao dao = AppDatabase.getInstance(this).medicineDao();

        //first: cancel the notification
        cancelNotification(id);

        //get the current taken times
        int takenTimes = dao.getTakenTimes(id);

        //get daily taken times
        final int dailyTakeTimes = dao.getDailyTakeTimes(id);

        //if the current taken times < daily taken times
        if (takenTimes < dailyTakeTimes) {
            //increase the current taken times by 1
            takenTimes++;

            //then update it
            dao.updateTakenTimes(id, takenTimes);
        }
    }

    private void resetAllMedicinesTakenTimes() {
        //get instance of MedicineDao
        MedicineDao dao = AppDatabase.getInstance(this).medicineDao();

        //get instance of MedicineAlarmUtil
        MedicineAlarmUtil alarmUtil = MedicineAlarmUtil.getInstance(this.getApplication());


        //get all medicines
        final List<Medicine> medicines = dao.loadAllMedicinesAsList();

        //if the list is not null
        if (medicines != null) {
            for (Medicine medicine : medicines) {
                //reset medicine taken times to 0
                medicine.setTakenTimes(0);

                //update the medicine
                dao.updateMedicine(medicine);
            }
        }

        //reschedule the alarm for the next day
        alarmUtil.scheduleResettingAllMedicinesTakenTimesDaily();
    }
}

package com.example.android.medicinereminder.util;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.example.android.medicinereminder.db.AppDatabase;
import com.example.android.medicinereminder.model.Medicine;
import com.example.android.medicinereminder.service.AppService;
import com.example.android.medicinereminder.ui.MainActivity;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.List;

public class MedicineAlarmUtil {
    private final AlarmManager alarmManager;
    private final Application application;
    private final Gson gson;

    private MedicineAlarmUtil(Application application) throws NullPointerException {
        this.application = application;
        alarmManager = (AlarmManager) this.application.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) throw new NullPointerException("Cannot create AlarmManager.");

        gson = new Gson();
    }

    private static MedicineAlarmUtil ourInstance = null;

    public static MedicineAlarmUtil getInstance(Application application) {
        if (ourInstance == null) {
            ourInstance = new MedicineAlarmUtil(application);
        }
        return ourInstance;
    }

    public void set(@NonNull Long triggerTime, @NonNull Medicine medicine) {
        //create intent to AppService
        Intent intent = new Intent(application, AppService.class);

        //set the action to notification
        intent.setAction(AppService.ACTION_NOTIFICATION);

        //put the medicine in the intent
        intent.putExtra(AppService.MEDICINE_KEY, gson.toJson(medicine));

        //create the pending intent
        PendingIntent pendingIntent = PendingIntent.getService(
                application,
                medicine.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //create intent to the MainActivity
        Intent mainActivityIntent = new Intent(application, MainActivity.class);

        //add flags to handle the stack properly
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //create the pending intent
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(
                application,
                -2,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //create AlarmClockInfo instance
        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(triggerTime, mainActivityPendingIntent);

        //set the alarm
        alarmManager.setAlarmClock(clockInfo, pendingIntent);

        //schedule resetting all medicines taken times every day
        scheduleResettingAllMedicinesTakenTimesDaily();
    }

    public void scheduleResettingAllMedicinesTakenTimesDaily() {
        //create intent to the MainActivity
        Intent mainActivityIntent = new Intent(application, MainActivity.class);

        //add flags to handle the stack properly
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //create the pending intent
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(
                application,
                -2,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );


        //create intent to the AppService
        Intent appServiceIntent = new Intent(application, AppService.class);

        //set the action to reset_taken_times
        appServiceIntent.setAction(AppService.ACTION_RESET_TAKEN_TIMES);

        //create the pending intent
        PendingIntent appServicePendingIntent = PendingIntent.getService(
                application,
                -3,
                appServiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //set the trigger time to be 23:59:59
        long triggerTime = createTriggerTime(23, 59) + (59 * 1000);

        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(triggerTime, mainActivityPendingIntent);

        alarmManager.setAlarmClock(clockInfo, appServicePendingIntent);
    }

    public Long createTriggerTime(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTimeInMillis();
    }

    public void cancelAlarm(Medicine medicine) {
        Intent intent = new Intent(application, AppService.class);
        intent.setAction(AppService.ACTION_NOTIFICATION);
        intent.putExtra(AppService.MEDICINE_KEY, gson.toJson(medicine));

        PendingIntent pendingIntent = PendingIntent.getService(
                application,
                medicine.getId(),
                intent,
                PendingIntent.FLAG_NO_CREATE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }

        //if no medicines left,
        List<Medicine> medicines =
                AppDatabase.getInstance(application).medicineDao().loadAllMedicines().getValue();
        if (medicines == null || medicines.size() == 0) {
            //cancel the scheduled resetting all medicines taken times alarm
            cancelScheduledResettingAllMedicinesTakenTimesDaily();
        }

    }

    private void cancelScheduledResettingAllMedicinesTakenTimesDaily() {
        //create intent to the AppService
        Intent appServiceIntent = new Intent(application, AppService.class);

        //set the action to reset_taken_times
        appServiceIntent.setAction(AppService.ACTION_RESET_TAKEN_TIMES);

        //create the pending intent
        PendingIntent appServicePendingIntent = PendingIntent.getService(
                application,
                -3,
                appServiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (appServicePendingIntent != null) {
            alarmManager.cancel(appServicePendingIntent);
        }
    }
}

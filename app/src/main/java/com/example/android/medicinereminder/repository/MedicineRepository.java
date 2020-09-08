package com.example.android.medicinereminder.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.android.medicinereminder.db.MedicineDao;
import com.example.android.medicinereminder.model.Medicine;
import com.example.android.medicinereminder.util.AppExecutors;
import com.example.android.medicinereminder.util.MedicineAlarmUtil;
import com.example.android.medicinereminder.util.MedicineTime;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MedicineRepository {

    private final MedicineDao medicineDao;
    private final MedicineAlarmUtil alarmUtil;
    private final AppExecutors executor;

    private LiveData<List<Medicine>> medicines;

    private MedicineRepository(@NonNull MedicineDao medicineDao, @NonNull MedicineAlarmUtil alarmUtil) {
        this.medicineDao = medicineDao;
        this.alarmUtil = alarmUtil;

        this.executor = AppExecutors.getInstance();
        medicines = medicineDao.loadAllMedicines();
    }

    private static MedicineRepository ourInstance = null;

    public static MedicineRepository getInstance(MedicineDao medicineDao, MedicineAlarmUtil alarmUtil) {
        if (ourInstance == null) {
            ourInstance = new MedicineRepository(medicineDao, alarmUtil);
        }
        return ourInstance;
    }

    public LiveData<List<Medicine>> loadAllMedicines() {
        return medicines;
    }

    public void insertMedicine(@NonNull Medicine medicine) {
        executor.diskIO().execute(() -> {
            //insert medicine to get the auto generated id
            int id = (int) medicineDao.insertMedicine(medicine);
            medicine.setId(id);

            //schedule the next alarm
            String nextAlarm = scheduleNextAlarm(medicine);

            //set medicine next alarm
            medicine.setNextAlarm(nextAlarm);

            //update the medicine without rescheduling the alarm
            updateMedicineWithNoAlarmRescheduling(medicine);
        });
    }

    private void updateMedicineWithNoAlarmRescheduling(Medicine medicine) {
        executor.diskIO().execute(() -> {
            medicineDao.updateMedicine(medicine);
        });
    }

    public void updateMedicine(@NonNull Medicine medicine) {
        executor.diskIO().execute(() -> {
            medicineDao.updateMedicine(medicine);

            //cancel any scheduled alarm
            cancelAlarm(medicine);

            //schedule the next alarm
            String nextAlarm = scheduleNextAlarm(medicine);

            //set medicine next alarm
            medicine.setNextAlarm(nextAlarm);

            //update the medicine without rescheduling the alarm
            updateMedicineWithNoAlarmRescheduling(medicine);
        });
    }

    public void deleteMedicine(@NonNull Medicine medicine) {
        executor.diskIO().execute(() -> {
            medicineDao.deleteMedicine(medicine);

            cancelAlarm(medicine);
        });
    }

    private String scheduleNextAlarm(@NonNull Medicine medicine) {
        ArrayList<MedicineTime> alarmTimes = medicine.getAlarmTimes();
        int numOfAlarms = alarmTimes.size();
        int i = 0;

        int dayInMills = 0;

        for (; i < numOfAlarms; ++i) {
            MedicineTime time = alarmTimes.get(i);
            long triggerTime = alarmUtil.createTriggerTime(time.getHourOfDay(), time.getMinute()) + dayInMills;

            if (triggerTime > System.currentTimeMillis()) {
                alarmUtil.set(triggerTime, medicine);
                return new Gson().toJson(time);
            }

            //if no alarm was set
            if ((i + 1) == numOfAlarms) {
                //add a day to triggerTime
                dayInMills = 1000 * 60 * 60 * 24;

                i = -1; //will increase to 0 before next loop
            }
        }

        return ""; //never reached
    }

    private void cancelAlarm(Medicine medicine) {
        alarmUtil.cancelAlarm(medicine);
    }
}

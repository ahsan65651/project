package com.example.android.medicinereminder.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.example.android.medicinereminder.db.AppDatabase;
import com.example.android.medicinereminder.db.MedicineDao;
import com.example.android.medicinereminder.model.Medicine;
import com.example.android.medicinereminder.repository.MedicineRepository;
import com.example.android.medicinereminder.util.MedicineAlarmUtil;
import com.example.android.medicinereminder.util.MedicineTime;

import java.util.ArrayList;

public class MedicineActivityViewModel extends AndroidViewModel {
    private MedicineRepository repository;

    private Medicine medicine;
    private Boolean firstTime;

    public MedicineActivityViewModel(@NonNull Application application) {
        super(application);

        MedicineDao dao = AppDatabase.getInstance(application).medicineDao();
        MedicineAlarmUtil alarmUtil = MedicineAlarmUtil.getInstance(application);
        repository = MedicineRepository.getInstance(dao, alarmUtil);

        medicine = new Medicine();
        firstTime = true;
    }

    public void insertMedicine() {
        repository.insertMedicine(medicine);
    }

    public void updateMedicine() {
        //reset medicine taken times to 0 when updating
        medicine.setTakenTimes(0);

        repository.updateMedicine(medicine);
    }

    public void deleteMedicine() {
        repository.deleteMedicine(medicine);
    }

    public Bitmap getPicture() {
        return medicine.getPicture();
    }

    public void setPicture(Bitmap picture) {
        medicine.setPicture(picture);
    }

    public ArrayList<MedicineTime> getTimes() {
        return medicine.getAlarmTimes();
    }

    public void addTime(MedicineTime medicineTime) {
        medicine.addTime(medicineTime);
    }

    public String getMedicineTimesAsString() {
        //create string of the generated times
        StringBuilder timesStringBuilder = new StringBuilder("");
        for (MedicineTime time : medicine.getAlarmTimes()) {
            String hour = String.valueOf(time.getHourOfDay());
            if (hour.length() == 1) hour = "0" + hour;

            String minute = String.valueOf(time.getMinute());
            if (minute.length() == 1) minute = "0" + minute;

            timesStringBuilder.append(hour).append(":").append(minute).append(", ");
        }

        if (timesStringBuilder.length() >= 2) {
            //delete the last ", " in the string
            timesStringBuilder.deleteCharAt(timesStringBuilder.length() - 1);
            timesStringBuilder.deleteCharAt(timesStringBuilder.length() - 1);
        }

        return timesStringBuilder.toString();
    }

    public void resetTimes() {
        medicine.setAlarmTimes(new ArrayList<>());
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setName(String name) {
        medicine.setName(name);
    }

    public void setDailyTakeTimes(int dailyTakeTimes) {
        medicine.setDailyTakeTimes(dailyTakeTimes);
    }

    public void setMedicine(Medicine medicine) {
        if (firstTime) {
            this.medicine = medicine;
            firstTime = false;
        }
    }

    public void generateTimes(Integer dailyTakeTimes) {
        //set the first time to be 12:00 PM
        int hourOfDay = 12;

        for (int i = 0; i < dailyTakeTimes; ++i) {
            //add the time
            addTime(new MedicineTime(hourOfDay, 0));

            //set the next time
            hourOfDay = ((24 / dailyTakeTimes) + hourOfDay) % 24;
        }
    }
}

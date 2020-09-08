package com.example.android.medicinereminder.util;

import android.support.annotation.NonNull;

public class MedicineTime implements Comparable<MedicineTime> {
    private int hourOfDay;
    private int minute;

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public MedicineTime(int hourOfDay, int minute) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;
    }

    @Override
    public int compareTo(@NonNull MedicineTime o) {
        return (this.getHourOfDay() - o.getHourOfDay()) + (this.getMinute() - o.getMinute());
    }
}

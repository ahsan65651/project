package com.example.android.medicinereminder.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.medicinereminder.util.MedicineTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

@Entity
public class Medicine {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    @NonNull
    private String name;

    @ColumnInfo(name = "take_times")
    @NonNull
    private Integer dailyTakeTimes;

    @ColumnInfo(name = "taken_times")
    private Integer takenTimes;

    @ColumnInfo(name = "picture")
    @Nullable
    private Bitmap picture;

    @ColumnInfo(name = "date")
    @NonNull
    private Date date;

    @ColumnInfo(name = "next_alarm")
    @NonNull
    private String nextAlarm;

    @ColumnInfo(name = "times")
    @NonNull
    private ArrayList<MedicineTime> alarmTimes;

    public Medicine(int id, @NonNull String name, @NonNull Integer dailyTakeTimes, Integer takenTimes, @Nullable Bitmap picture, @NonNull Date date, @NonNull String nextAlarm, @NonNull ArrayList<MedicineTime> alarmTimes) {
        this.id = id;
        this.name = name;
        this.dailyTakeTimes = dailyTakeTimes;
        this.takenTimes = takenTimes;
        this.picture = picture;
        this.date = date;
        this.nextAlarm = nextAlarm;
        this.alarmTimes = alarmTimes;
    }

    @Ignore
    public Medicine() {
        this.name = "";
        this.dailyTakeTimes = 1;
        this.alarmTimes = new ArrayList<>();

        this.takenTimes = 0;
        this.date = new Date();
        this.nextAlarm = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public Integer getDailyTakeTimes() {
        return dailyTakeTimes;
    }

    public void setDailyTakeTimes(@NonNull Integer dailyTakeTimes) {
        this.dailyTakeTimes = dailyTakeTimes;
    }

    public Integer getTakenTimes() {
        return takenTimes;
    }

    public void setTakenTimes(Integer takenTimes) {
        this.takenTimes = takenTimes;
    }

    @Nullable
    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(@Nullable Bitmap picture) {
        this.picture = picture;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    public void setDate(@NonNull Date date) {
        this.date = date;
    }

    @NonNull
    public ArrayList<MedicineTime> getAlarmTimes() {
        Collections.sort(alarmTimes);

        return alarmTimes;
    }

    public void setAlarmTimes(@NonNull ArrayList<MedicineTime> alarmTimes) {
        this.alarmTimes = alarmTimes;
    }

    public void addTime(MedicineTime time) {
        alarmTimes.add(time);
    }

    @NonNull
    public String getNextAlarm() {
        return nextAlarm;
    }

    public void setNextAlarm(@NonNull String nextAlarm) {
        this.nextAlarm = nextAlarm;
    }
}

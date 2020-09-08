package com.example.android.medicinereminder.db.converter;

import android.arch.persistence.room.TypeConverter;
import android.support.annotation.NonNull;

import com.example.android.medicinereminder.util.MedicineTime;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MedicineTimeConverter {
    @TypeConverter
    public static String toString(@NonNull ArrayList<MedicineTime> time) {
        return new Gson().toJson(time);
    }

    @TypeConverter
    public static ArrayList<MedicineTime> toArrayListOfMedicineTime(String time) {
        Type collectionType = new TypeToken<ArrayList<MedicineTime>>() {
        }.getType();

        return new Gson().fromJson(time, collectionType);
    }
}

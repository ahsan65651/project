package com.example.android.medicinereminder.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.example.android.medicinereminder.db.converter.BitmapConverter;
import com.example.android.medicinereminder.db.converter.DateConverter;
import com.example.android.medicinereminder.db.converter.MedicineTimeConverter;
import com.example.android.medicinereminder.model.Medicine;

@Database(entities = {Medicine.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class, BitmapConverter.class, MedicineTimeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase ourInstance = null;
    public abstract MedicineDao medicineDao();
    public static AppDatabase getInstance(Context context) {
        if (ourInstance == null) {
            synchronized (AppDatabase.class)
            {
                ourInstance = Room.databaseBuilder(context, AppDatabase.class, "dailymedicine.db")
                        .build();
            }
        }
        return ourInstance;
    }
}

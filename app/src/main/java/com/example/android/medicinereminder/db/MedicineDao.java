package com.example.android.medicinereminder.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.android.medicinereminder.model.Medicine;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface MedicineDao {
    @Query("SELECT * FROM Medicine")
    LiveData<List<Medicine>> loadAllMedicines();

    @Query("SELECT * FROM Medicine")
    List<Medicine> loadAllMedicinesAsList();

    @Insert
    long insertMedicine(Medicine medicine);

    @Update(onConflict = REPLACE)
    void updateMedicine(Medicine medicine);

    @Delete
    void deleteMedicine(Medicine medicine);

    @Query("UPDATE Medicine SET taken_times = :takenTimes WHERE id = :id")
    void updateTakenTimes(int id, int takenTimes);

    @Query("SELECT taken_times FROM Medicine WHERE id = :id")
    int getTakenTimes(int id);

    @Query("SELECT take_times FROM Medicine WHERE id = :id")
    int getDailyTakeTimes(int id);
}

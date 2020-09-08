package com.example.android.medicinereminder.db.converter;

import android.arch.persistence.room.TypeConverter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class BitmapConverter {
    @TypeConverter
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);

            return stream.toByteArray();
        }
        return null;
    }

    @TypeConverter
    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        if (byteArray != null) {
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }
        return null;
    }
}

package com.example.android.medicinereminder.ui;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.medicinereminder.model.Medicine;
import com.example.android.medicinereminder.util.MedicineTime;
import com.example.android.medicinereminder.R;
import com.google.gson.Gson;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MedicinesAdapter extends RecyclerView.Adapter<MedicinesAdapter.ViewHolder> {

    private ViewActions mViewActions;

    private List<Medicine> medicines;

    MedicinesAdapter(ViewActions mViewActions, List<Medicine> medicines) {
        this.mViewActions = mViewActions;
        this.medicines = medicines;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;

        //notify the adapter that the dataset has been changed
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate the medicine_item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.medicine_item, viewGroup, false);

        //return the inflated view
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);

        //set mMedicineNameTextView to the medicine name
        holder.mMedicineNameTextView.setText(medicine.getName());

        //set mMedicineDetailsTextView to show the medicine TakenTimes for today
        String medicineDetails = medicine.getTakenTimes() + " out of " + medicine.getDailyTakeTimes() + " today.";
        holder.mMedicineDetailsTextView.setText(medicineDetails);

        //get medicine next alarm using Gson library
        MedicineTime time = new Gson().fromJson(medicine.getNextAlarm(), MedicineTime.class);

        if (time != null) {
            //get hourOfDay of day and format it
            String hourOfDay = String.valueOf(time.getHourOfDay());
            if (hourOfDay.length() == 1) hourOfDay = "0" + hourOfDay;

            //get minute and format it
            String minute = String.valueOf(time.getMinute());
            if (minute.length() == 1) minute = "0" + minute;

            //set mMedicineNextAlarmTextView to show thw next alarm
            String medicineNextAlarm = "Next alarm at: " + hourOfDay + ":" + minute + ".";
            holder.mMedicineNextAlarmTextView.setText(medicineNextAlarm);
        } else {
            holder.mMedicineNextAlarmTextView.setText("No Alarm Scheduled.");
        }

        //set mMedicineAddedDateTextView to the date the medicine where added in
        String medicineDate = "Added at: " + medicine.getDate().toString();
        holder.mMedicineAddedDateTextView.setText(medicineDate);

        //get medicine picture
        Bitmap picture = medicine.getPicture();
        if (picture != null) {
            //if the picture is not null
            //set mMedicinePictureImageView to the medicine picture
            holder.mMedicinePictureImageView.setImageBitmap(picture);
        } else {
            //make mMedicinePictureImageView empty
            // in case it holding a picture of another medicine
            holder.mMedicinePictureImageView.setImageBitmap(null);
        }

        //set the list item click listener
        holder.itemView.setOnClickListener((v -> mViewActions.onRecyclerItemClickListener(medicine)));
    }

    @Override
    public int getItemCount() {
        //get list size
        if (medicines != null) {
            int count = medicines.size();
            if (count == 0) {
                //if size == 0, make the empty view visible
                return makeEmptyViewVisible();
            }

            //if the size is bigger than 0, make the empty view invisible
            mViewActions.setEmptyViewVisibility(View.INVISIBLE);

            return count;
        }

        //if medicines == null, make the empty view visible
        return makeEmptyViewVisible();
    }

    private int makeEmptyViewVisible() {
        mViewActions.setEmptyViewVisibility(View.VISIBLE);
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.medicine_name)
        TextView mMedicineNameTextView;

        @BindView(R.id.medicines_details)
        TextView mMedicineDetailsTextView;

        @BindView(R.id.medicines_next_alarm)
        TextView mMedicineNextAlarmTextView;

        @BindView(R.id.medicine_picture)
        ImageView mMedicinePictureImageView;

        @BindView(R.id.medicine_added_date)
        TextView mMedicineAddedDateTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface ViewActions {
        /*
         * toggle the empty view visibility
         * @param visibility the visibility desired
         * */
        void setEmptyViewVisibility(int visibility);

        /*
         * the click listener for list items
         * @param medicine the clicked medicine
         * */
        void onRecyclerItemClickListener(Medicine medicine);
    }
}

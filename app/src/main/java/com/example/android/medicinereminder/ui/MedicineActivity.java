package com.example.android.medicinereminder.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.medicinereminder.model.Medicine;
import com.example.android.medicinereminder.util.MedicineTime;
import com.example.android.medicinereminder.viewmodel.MedicineActivityViewModel;
import com.example.android.medicinereminder.R;
import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MedicineActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int CAMERA_REQUEST_CODE = 1;

    @BindView(R.id.medicine_name)
    TextInputEditText mMedicineNameTextInputEditText;

    @BindView(R.id.daily_take_times_spinner)
    Spinner mMedicineDailyTakeTimesSpinner;

    @BindView(R.id.times)
    TextView mTimesTextView;

    @BindView(R.id.notification_reschedule_button)
    Button mNotificationRescheduleButton;

    @BindView(R.id.take_picture)
    Button mTakePictureButton;

    @BindView(R.id.medicine_picture)
    ImageView mMedicinePictureImageView;

    @BindView(R.id.done)
    Button mDoneButton;

    @BindView(R.id.delete)
    Button mDeleteButton;
    private MedicineActivityViewModel mViewModel;
    private Boolean mIsActivityForEdit;
    private Boolean mIsSpinnerClicked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);
        ButterKnife.bind(this);
        mIsActivityForEdit = false;
        mIsSpinnerClicked = false;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        mViewModel = ViewModelProviders.of(this).get(MedicineActivityViewModel.class);
   String key = getString(R.string.medicine_key);

        if (intent.hasExtra(key)) {
            Medicine medicine = new Gson().fromJson(intent.getStringExtra(getString(R.string.medicine_key)), Medicine.class);
        mViewModel.setMedicine(medicine);
            mIsActivityForEdit = true;
            if (actionBar != null) actionBar.setTitle("Edit Medicine");
        } else {
            if (actionBar != null) actionBar.setTitle("Add Medicine");
        }
        setupViews();
    }

    private void setupViews() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.medicine_take_times, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        mMedicineDailyTakeTimesSpinner.setAdapter(adapter);

        //add listener to the spinner
        mMedicineDailyTakeTimesSpinner.setOnItemSelectedListener(this);

        //set the mNotificationRescheduleButton click listener
        mNotificationRescheduleButton.setOnClickListener(v -> rescheduleNotifications());

        //set the mTakePictureButton click listener
        mTakePictureButton.setOnClickListener(v -> {
            //create the camera intent
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                //start the camera with the CAMERA_REQUEST_CODE
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        });

        //set the done button click listener
        mDoneButton.setOnClickListener(v -> save());

        if (mIsActivityForEdit) {
            //load medicine name
            mMedicineNameTextInputEditText.setText(mViewModel.getMedicine().getName());

            //load medicine Daily Take Times to the spinner
            mMedicineDailyTakeTimesSpinner.setSelection(
                    adapter.getPosition(mViewModel.getMedicine().getDailyTakeTimes().toString())
            );

            //check the picture in mViewModel
            Bitmap picture = mViewModel.getPicture();
            if (picture != null) {
                //if medicine picture != null
                //pass it to the image view
                mMedicinePictureImageView.setImageBitmap(picture);
            }

            //make the delete button visible
            mDeleteButton.setVisibility(View.VISIBLE);

            //set the delete button click listener
            mDeleteButton.setOnClickListener(v -> delete());
        } else {
            //check the picture in mViewModel
            Bitmap picture = mViewModel.getPicture();
            if (picture != null) {
                //if medicine picture != null
                //pass it to the image view
                mMedicinePictureImageView.setImageBitmap(picture);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //if the camera intent is back with the picture
            //get it as thumbnail
            Bitmap picture = null;
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    picture = (Bitmap) bundle.get("data");
                }
            }

            if (picture != null) {
                //put the picture to the mMedicinePictureImageView
                //to display it to the user
                mMedicinePictureImageView.setImageBitmap(picture);

                //save the picture to the view model
                mViewModel.setPicture(picture);
            }
        }
    }

    private void rescheduleNotifications() {
        //get medicine times from the view model
        ArrayList<MedicineTime> times = mViewModel.getTimes();

        //reset medicine times in the view model
        mViewModel.resetTimes();

        //ask user to reset times
        for (MedicineTime time : times) {
            //create new MedicineTimePickerDialog
            MedicineTimePickerDialog dialog = new MedicineTimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        mViewModel.addTime(new MedicineTime(hourOfDay, minute));
                        mTimesTextView.setText(mViewModel.getMedicineTimesAsString());
                    }, time
            );

            //show the created MedicineTimePickerDialog to the user
            dialog.show();
        }
    }

    private void delete() {
        confirm(true);
    }

    private void save() {
        Editable editable = mMedicineNameTextInputEditText.getText();
Toast errorToast = Toast.makeText(this, "Medicine Name Required !", Toast.LENGTH_SHORT);

        if (editable != null) {
            String name = editable.toString();

            if (!TextUtils.isEmpty(name)) {
                mViewModel.setName(name);
  mViewModel.setDailyTakeTimes(Integer.valueOf((String) mMedicineDailyTakeTimesSpinner.getSelectedItem()));

                if (mIsActivityForEdit) {
                    mViewModel.updateMedicine();
                } else {
                    mViewModel.insertMedicine();
                }

                 this.finish();
            } else {
                errorToast.show();
            }
        } else {
            errorToast.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // handle the back button click action
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        confirm(false);
    }

    private void confirm(Boolean deleteOperation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("yes", (dialog, which) -> {
            if (deleteOperation) {
                mViewModel.deleteMedicine();
            }
       MedicineActivity.this.finish();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        if (deleteOperation) builder.setMessage("Delete this medicine ?");
        else builder.setMessage("Discard changes ?");
        builder.show();
    }

    //region AdapterView.OnItemSelectedListener

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //avoid resetting times first time user enters the activity
        if (!mIsSpinnerClicked) {
            mIsSpinnerClicked = true;

            if (!mIsActivityForEdit) {
                //generate default times based on user selection of number of daily take times
                mViewModel.generateTimes(Integer.valueOf((String) parent.getSelectedItem()));
            }
        } else {
            //reset medicine times
            mViewModel.resetTimes();

            //generate default times based on user selection of number of daily take times
            mViewModel.generateTimes(Integer.valueOf((String) parent.getSelectedItem()));
        }

        //show the generated times to user
        mTimesTextView.setText(mViewModel.getMedicineTimesAsString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //do nothing
    }

    //endregion AdapterView.OnItemSelectedListener

    private class MedicineTimePickerDialog extends TimePickerDialog
            implements TimePickerDialog.OnCancelListener {
        private final MedicineTime time;

        MedicineTimePickerDialog(Context context, OnTimeSetListener listener, MedicineTime time) {
            super(context, listener, time.getHourOfDay(), time.getMinute(), true);

            this.time = time;

            this.setCancelable(false);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            //add default time
            mViewModel.addTime(time);

            mTimesTextView.setText(mViewModel.getMedicineTimesAsString());
        }
    }
}

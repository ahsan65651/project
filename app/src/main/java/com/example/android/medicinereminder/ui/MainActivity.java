package com.example.android.medicinereminder.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.android.medicinereminder.model.Medicine;
import com.example.android.medicinereminder.viewmodel.MainActivityViewModel;
import com.example.android.medicinereminder.R;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MedicinesAdapter.ViewActions {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_view)
    TextView mEmptyView;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bind the views with ButterKnife library
        ButterKnife.bind(this);

        //add the recycler view adapter
        MedicinesAdapter adapter = new MedicinesAdapter(this, null);
        mRecyclerView.setAdapter(adapter);

        //set the layoutManager of the recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //get the view model instance
        MainActivityViewModel viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        //observe medicines changes
        //update the adapter data
        viewModel.getMedicines().observe(this, adapter::setMedicines);

        //navigate to the @MedicineActivity when click the add floating action button
        // to add new medicine
        mFab.setOnClickListener(v -> {
            //create an intent to MedicineActivity
            Intent medicineIntent = new Intent(getApplicationContext(), MedicineActivity.class);

            //start the activity to add new medicine
            startActivity(medicineIntent);
        });
    }

    //region MedicinesAdapter.ViewActions
    @Override
    public void setEmptyViewVisibility(int visibility) {
        //set the empty view visibility to be equal to visibility
        mEmptyView.setVisibility(visibility);
    }

    @Override
    public void onRecyclerItemClickListener(Medicine medicine) {
        //create an intent to MedicineActivity
        Intent editMedicineIntent = new Intent(this, MedicineActivity.class);

        //put the medicine to the intent as string usin Gson library
        editMedicineIntent.putExtra(getString(R.string.medicine_key), new Gson().toJson(medicine));

        //start the activity to edit the clicked medicine
        startActivity(editMedicineIntent);
    }

    //endregion MedicinesAdapter.ViewActions
}


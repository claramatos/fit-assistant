package com.example.android.fitassistant.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import com.example.android.fitassistant.R;


/**
 * Displays available activity types to start a MapActivity
 *
 * A simple {@link Fragment} subclass.
 */
public class SingleChoiceDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pick_activity)
                .setItems(R.array.activities_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int activtyType) {
                        // after the activity type is selected start MapActivity
                        Intent intent = new Intent(getActivity(), MapActivity.class);
                        intent.putExtra(MapActivity.ACTIVITY_TYPE, activtyType);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SingleChoiceDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}

package com.example.thusitha.wifidirecttestapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.ArrayList;


public class ListSelectionAlert extends DialogFragment {

    private int selection = 0;

    public interface ListSelectionAlertListener {
        void onDialogPositiveClick (ListSelectionAlert selectionAlert);
    }

    private ListSelectionAlertListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ArrayList<String> list = getArguments().getStringArrayList("list");
        String title = getArguments().getString("title");
        assert list != null;
        CharSequence [] csList = list.toArray(new CharSequence[list.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(title)
                .setSingleChoiceItems(csList, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selection = which;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(ListSelectionAlert.this);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = null;
        if (context instanceof Activity){
            activity = (Activity) context;
        }

        mListener = (ListSelectionAlertListener) activity;
    }

    public int getSelection () {
        return selection;
    }
}

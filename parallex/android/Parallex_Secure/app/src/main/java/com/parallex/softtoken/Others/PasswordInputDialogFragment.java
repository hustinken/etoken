/**
 * Copyright (c) 2014 Entrust, Inc. All rights reserved.
 * Use is subject to the terms of the accompanying license agreement.
 * Entrust Confidential.
 */
package com.parallex.softtoken.Others;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.entrust.identityGuard.mobile.sdk.example.R;

public class PasswordInputDialogFragment extends DialogFragment {

    private int title;
    private String message;
    private EditText inputField;

    public interface InputDialogListener {
        void onFinishedDialog(String inputText);
    };

    public PasswordInputDialogFragment() {
    }

    @Override
    public void setArguments(Bundle arguments) {
        title = arguments.getInt("title");
        message = arguments.getString("message");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_text, null);
        inputField = (EditText) view.findViewById(R.id.dialog_inputText);
        inputField.setTransformationMethod(PasswordTransformationMethod.getInstance());

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        InputDialogListener activity = (InputDialogListener) getActivity();

                        activity.onFinishedDialog(inputField.getText().toString().trim());
                        dialog.dismiss();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        inputField.setText("");

                        InputDialogListener activity = (InputDialogListener) getActivity();
                        activity.onFinishedDialog(null);
                        dialog.dismiss();
                        dialog.cancel();
                    }
                }).create();

        return dialog;
    }
}

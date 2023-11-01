package com.parallex.softtoken.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.entrust.identityGuard.mobile.sdk.example.R;

import java.util.Objects;

public class ProgressDialog extends Dialog {
    public ProgressDialog(Activity activity) {
        super(activity);
    }

    TextView progressLabel;
    ProgressBar progressBar;
    Button button, button2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.progress_dialog);
        progressBar = findViewById(R.id.progress_bar);
        progressLabel = findViewById(R.id.loading_label_id);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        Objects.requireNonNull(getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClicked.onClicked(view);
                dismiss();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClicked2.onClicked(view);
            }
        });
    }

    boolean lock = false;
    public void setProgressLabel(String message, Boolean disableBackPress){
        lock = disableBackPress;
        setCanceledOnTouchOutside(!lock);
        progressLabel.setText(message);
        progressBar.setVisibility(View.VISIBLE);
        button.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
    }

    public void setProgressLabel(String message, Boolean disableBackPress, Boolean disableProgress){
        if (disableProgress)
            progressBar.setVisibility(View.GONE);
        lock = disableBackPress;
        setCanceledOnTouchOutside(!lock);
        progressLabel.setText(message);
        button.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
    }

    public void setButtonClicked(String buttonText, ButtonClicked buttonClicked){
        button.setVisibility(View.VISIBLE);
        button.setText(buttonText);
        this.buttonClicked = buttonClicked;
    }

    public void setButtonClicked2(String buttonText, ButtonClicked buttonClicked){
        button2.setVisibility(View.INVISIBLE);
        button2.setText(buttonText);
        this.buttonClicked2 = buttonClicked;
    }

    @Override
    public void onBackPressed() {
        if(!lock)  {
            dismiss();
            super.onBackPressed();
        }
    }

    private ButtonClicked buttonClicked;
    private ButtonClicked buttonClicked2;
    public interface ButtonClicked{
        void onClicked(View v);
    }
}

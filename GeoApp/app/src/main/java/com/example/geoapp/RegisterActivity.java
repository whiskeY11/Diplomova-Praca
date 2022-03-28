package com.example.geoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.geoapp.database.DatabaseCalls;
import com.example.geoapp.misc.Helper;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findViewById(R.id.register_progress).setVisibility(View.INVISIBLE);
    }

    public void onDoRegisterClick(View v) {
        findViewById(R.id.register_progress).setVisibility(View.VISIBLE);
        EditText email = findViewById(R.id.emailRegister);
        EditText password = findViewById(R.id.passwordRegister);
        DatabaseCalls.getInstance().setCallbackHandler(registerHandler);
        DatabaseCalls.getInstance().execRegister(email.getText().toString(), password.getText().toString());
    }

    private void returnToMainActivity() {
        setResult(RESULT_OK);
        finish();
    }

    Handler registerHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -1:
                    Toast.makeText(RegisterActivity.this, "Problém so sieťou.", Toast.LENGTH_LONG).show();
                    break;
                case 0: //REGISTER SUCCESS
                    returnToMainActivity();
                    break;
                case 1: //REGISTER FAILED - USER EXISTS
                    Helper.Companion.getInstance().instantiateInfoDialog(RegisterActivity.this,
                            "Registrácia", "Používateľ so zadaným menom už existuje, vyberte si prosím iné meno.",
                            -1, true);
                    findViewById(R.id.register_progress).setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
}
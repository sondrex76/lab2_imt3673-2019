package com.example.sondre_lab_2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// Activity with list of items
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button transfer = findViewById(R.id.btnOptions);
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Goes to the options menu
                startActivity(new Intent(MainActivity.this, ActivityUserPreferences.class));
            }
        });

    }
}

package com.example.sondre_lab_2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ActivityUserPreferences extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_preferences);

        // If text field is not empty it will fill the text field with the current url
        if (!TextUtils.isEmpty(MainActivity.url)) {

            // Sets the Text
            final EditText URL = findViewById(R.id.txtURL);
            URL.setText(MainActivity.url);
        }

        // If number of elements have been defined it will select the correct value
        if (MainActivity.numDisplayedElements != 0) {
            final Spinner numberElementsSpinner = findViewById(R.id.spinnerNumList);
            numberElementsSpinner.setSelection(((ArrayAdapter)numberElementsSpinner.getAdapter()).getPosition("" + MainActivity.numDisplayedElements));
        }

        // If frequency have been defined it will select the correct value
        if (MainActivity.fetchFreq != 0) {
            final Spinner numberElementsSpinner = findViewById(R.id.spinnerFrequency);
            switch (MainActivity.fetchFreq) {
                case 1: // 10 minutes
                    numberElementsSpinner.setSelection(0);
                    break;
                case 2: // 60 minutes
                    numberElementsSpinner.setSelection(1);
                    break;
                case 3: // 1 a day
                    numberElementsSpinner.setSelection(2);
                    break;
            }
        }

        // On save
        final Button transfer = findViewById(R.id.btnSave);
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // saves data, sends it to parent and goes back to parent
                // Updates data in MainActivity

                // Gets and updates URL value
                final EditText URL = findViewById(R.id.txtURL);
                MainActivity.url = URL.getText().toString();

                // Gets and updatesnumber of elements to be displayed
                final Spinner numDisplayedElements = findViewById(R.id.spinnerNumList);
                MainActivity.numDisplayedElements = Integer.parseInt(numDisplayedElements.getSelectedItem().toString());

                // Gets and updates the frequency of checks
                final Spinner numberElementsSpinner = findViewById(R.id.spinnerFrequency);
                switch (numberElementsSpinner.getSelectedItem().toString()) {
                    case "10 minutes":
                        MainActivity.fetchFreq =  1;
                    break;
                    case "60 minutes":
                        MainActivity.fetchFreq =  2;
                    break;
                    case "Once a day":
                        MainActivity.fetchFreq =  3;
                    break;
                    default:    // In case an invalid result occurs, DEBUG
                        Toast.makeText(ActivityUserPreferences.this,
                                "Error, update rate not returning valid value",
                                Toast.LENGTH_SHORT).show();
                        break;
                }

                MainActivity.saveDataPhysically(getApplicationContext()); // Saves data to disk
                finish(); // Ends this activity and goes back to parent
            }
        });
    }
}

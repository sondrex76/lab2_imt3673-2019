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

        // runs code for start
        startCode();
    }

    // checks if string is empty, used for testing
    protected Boolean emptyURL(String value) {
        return (TextUtils.isEmpty(value));
    }

    // returns an int for testing purposes
    protected int startCode() {
        // If text field is not empty it will fill the text field with the current url
        if (!emptyURL(MainActivity.url)) {
            // Sets the Text
            final EditText URL = findViewById(R.id.txtURL);
            URL.setText(MainActivity.url);
        } else { return -1; }

        // If number of elements have been defined it will select the correct value
        if (MainActivity.numDisplayedElements != 0) {
            final Spinner numberElementsSpinner = findViewById(R.id.spinnerNumList);
            numberElementsSpinner.setSelection(((ArrayAdapter)numberElementsSpinner.getAdapter()).getPosition("" + MainActivity.numDisplayedElements));
        } else { return -2; }

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
        } else { return -3; }

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
                        MainActivity.fetchFreq =  MainActivity.translateFrequency(3);
                        break;
                    case "60 minutes":
                        MainActivity.fetchFreq =  MainActivity.translateFrequency(1);
                        break;
                    case "Once a day":
                        MainActivity.fetchFreq =  MainActivity.translateFrequency(2);
                        break;
                    default:    // In case an invalid result occurs, sets value to lowest(10 min)
                        MainActivity.fetchFreq =  MainActivity.translateFrequency(3);
                        break;
                }

                MainActivity.saveDataPhysically(getApplicationContext()); // Saves data to disk
                finish(); // Ends this activity and goes back to parent
            }
        });
        return 1;
    }
}

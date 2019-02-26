package com.example.sondre_lab_2;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// Activity with list of items
public class MainActivity extends AppCompatActivity {

    // Options values
    public static String url;
    public static int numDisplayedElements, fetchFreq;

    // List of RssFeedModel from feed, one feed per RssFeedModel
    public static List newsList = new ArrayList<RssFeedModel>();

    // Menu elements
    Button debugButton;

    // Recycler view to display elements
    RecyclerView  mRecyclerView;
    // SwipeRefreshLayout to refresh on request
    SwipeRefreshLayout mSwipeLayout;
    // Text views
    TextView mFeedTitleTextView;
    TextView mFeedDescriptionTextView;
    TextView mFeedLinkTextView;

    // Values for parseFeed
    private String mFeedTitle;
    private String mFeedLink;
    private String mFeedDescription;

    // OnCreate, runs at the start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Reads data from file(if it exists)
        GetDataPhysically();

        // Debug button, used to check if things are being fetched correctly
        debugButton = findViewById(R.id.btnDebugg);
        // Recycler view to display elements
        mRecyclerView = findViewById(R.id.recyclerView);
        // SwipeRefreshLayout to refresh on request
        mSwipeLayout = findViewById(R.id.swipeRefreshLayout);
        // Text views
        mFeedTitleTextView = findViewById(R.id.feedTitle);
        mFeedDescriptionTextView = findViewById(R.id.feedDescription);
        mFeedLinkTextView = findViewById(R.id.feedLink);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Listener for when the debug button is clicked to get data from the RSS feed
        debugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchFeedTask().execute("", "" ,"");
            }
        });

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask().execute("", "", "");
            }
        });


        // Detects when the options button is clicked
        final Button transfer = findViewById(R.id.btnOptions);
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Goes to the options menu
                startActivity(new Intent(MainActivity.this, ActivityUserPreferences.class));
            }
        });
    }

    // Writes data to disk(internal)
    static public void saveDataPhysically(Context context) {
        // Writes to file
        String filename = "settingsLab2";
        String fileContents = url + "\n" + numDisplayedElements + "\n" + fetchFreq;
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Loads data from disk(internal)
    public void GetDataPhysically() {
        try {
            FileInputStream inputStream = getApplicationContext().openFileInput("settingsLab2");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String urlLine = bufferedReader.readLine(),
                    numDisplayLine = bufferedReader.readLine(),
                    fetchFreqLine = bufferedReader.readLine();

            // if url have been stored
            if (!urlLine.equals("")) {
                url = urlLine;
            }

            // if numDisplayedElements have been stored
            if (!numDisplayLine.equals("")) {
                numDisplayedElements = Integer.parseInt(numDisplayLine);
            }

            // if fetchFreq have been stored
            if (!fetchFreqLine.equals("")) {
                fetchFreq = Integer.parseInt(fetchFreqLine);
            }
            inputStreamReader.close(); // closes input stream
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Returns the number of seconds that should be waited based on the value sent
    static public int translateFrequensy(int currentFrequency) {
        int returnThis;
        switch (currentFrequency) {
            case 1:
                returnThis =  3600;
                break;
            case 2:
                returnThis =  86400;
                break;
            default:
                returnThis = 600; // defaults value to be 10 minutes, 0 will also return this
                break;
        }
        return returnThis;
    }

    // Class with values of single feed item
    private class RssFeedModel {
        private String title;
        private String link;
        private String description;

        private RssFeedModel(String title, String link, String description) {
            this.title = title;
            this.link = link;
            this.description = description;
        }
    }
    // DEBUG
    int debugValue = 0;
    String testString = "";

    // AsyncTask which shall update newsList
    class FetchFeedTask extends AsyncTask<String, Void, Boolean>{
        // Temporary list, replaces the old one afterwards
        private  List temporaryList = new ArrayList<String>();
        private String tempURL;

        @Override
        protected void onPreExecute() {
            //Setup precondition to execute some task
        }

        // Gets feed and sets all translated elements into temporaryList
        @Override
        protected Boolean doInBackground(String... params) {

            // DEBUG
            tempURL = url;
            try {
                if(!tempURL.startsWith("http://") && !tempURL.startsWith("https://"))
                    tempURL = "http://" + url;

                URL urlValue = new URL(tempURL);
                InputStream inputStream = urlValue.openConnection().getInputStream();
                // Error is located in parseFeed()
                temporaryList = parseFeed(inputStream);  // gets list of items

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onProgressUpdate(Void... value) {
            //Update the progress of current task
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {  // If the attempt were successful
                //Show the result obtained from doInBackground
                newsList.clear();               // Clears the list of items so new ones can be added without problem
                newsList.add(temporaryList);    // Adds all new elements into the list

                Toast.makeText(MainActivity.this,
                        debugValue + ", " + testString,
                        Toast.LENGTH_SHORT).show();
            } else {        // Failed attempt
                Toast.makeText(MainActivity.this,
                    "Enter a valid Rss feed url, DEBUG: " + debugValue, // debugValue is DEBUG
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    // RSS parse class
    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String description = null;
        boolean isItem = false;     // if it is an item
        // Temporary list
        List<RssFeedModel> items = new ArrayList<>();

        int countElements = 0; // Number of elements currently loaded

        try {
            debugValue = 1;
            // Values needed for parsing
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser currentParser = xmlFactoryObject.newPullParser();

            currentParser.setInput(inputStream, null);

            debugValue++;

            int event = currentParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT && countElements++ < numDisplayedElements)  {
                String name = currentParser.getName();
                debugValue++;

                testString = name; // DEBUG

                switch (event){
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.END_TAG:
                        if(name.equals("item")){
                            //temperature = currentParser.getAttributeValue(null,"value");
                        }
                        break;
                }
                event = currentParser.next();
            }

            /*
            xmlPullParser.nextTag(); // ERROR is located here
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();



                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                Log.d("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("link")) {
                    link = result;
                } else if (name.equalsIgnoreCase("description")) {
                    description = result;
                }

                if (title != null && link != null && description != null) {
                    if(isItem) {
                        RssFeedModel item = new RssFeedModel(title, link, description);
                        items.add(item);
                    }
                    else {
                        mFeedTitle = title;
                        mFeedLink = link;
                        mFeedDescription = description;
                    }

                    title = null;
                    link = null;
                    description = null;
                    isItem = false;
                }
            }
*/
            return items;
        } finally {
            inputStream.close();
        }
    }
}

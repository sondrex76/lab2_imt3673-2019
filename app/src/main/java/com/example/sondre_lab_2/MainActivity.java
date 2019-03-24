package com.example.sondre_lab_2;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

// Class with values of single feed item

// Activity with list of items
public class MainActivity extends AppCompatActivity {
    // Options values
    public static String url;
    public static int numDisplayedElements, fetchFreq;

    // List of RssFeedModel from feed, one feed per RssFeedModel, holds the items ot be displayed
    public static List newsList = new ArrayList<RssFeedModel>();
    // List all current items including those which does not fit the regular expression
    private static List currentList = new ArrayList<RssFeedModel>();

    boolean resetTimer = false;

    // Menu elements

    // Recycler view to display elements
    RecyclerView  mRecyclerView;
    // SwipeRefreshLayout to refresh on request
    SwipeRefreshLayout mSwipeLayout;
    // Text views
    TextView mFeedTitleTextView;
    // EditText(search bar
    EditText mSearch;

    // OnCreate, runs at the start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Reads data from file(if it exists)
        getDataPhysically();

        // Recycler view to display elements
        mRecyclerView = findViewById(R.id.recyclerView);

        // SwipeRefreshLayout to refresh on request
        mSwipeLayout = findViewById(R.id.swipeRefreshLayout);
        // Text views, only need one for the title since link and description are in the background
        mFeedTitleTextView = findViewById(R.id.feedTitle);
        // Search bar, for searching through all elements with a regular expression
        mSearch = findViewById(R.id.txtSearch);

        // The RecyclerView, the item with all the items in it in a list
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(this, newsList);
        mRecyclerView.setAdapter(adapter);

        // Sets the layout type to linear
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Listener for when you swipe downwards to refresh
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeLayout.setRefreshing(true);
                getRSSFeed();

                // Stops the refreshing icon
                mSwipeLayout.setRefreshing(false);
            }
        });

        // When the text in the search-bar have been changed
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getRSSFeed();
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

        new FetchFeedTask().execute();
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
    public boolean getDataPhysically() {
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

            return true;
        } catch (Exception e) { // An error occurred, sets all values to default
            e.printStackTrace();
            Log.d("DEBUG", "ERROR");
            return false;
        }
    }

    // Checks if a RssFeedModel element fits the regular expression
    boolean fitsPattern(RssFeedModel elements, String expression) {
        String[] element = {elements.title, elements.link, elements.description};

        //Log.d("PATTERN", "expression");

        for (int i = 0; i < 2; i++) {
            if (element[i].matches(expression)) { return true; }
        }

        return false; // DEBUG
    }

    // parses a string of xml data and adds it to the list of news items, returns int for testing
    int parseXML(String value, String expression) {
        int debugValue = -1; // default value in case an unknown error occurred

        try {
            // Translates string to inputStream to be used in docBuilder
            InputStream in = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(in);

            // Gets all items
            NodeList nList = doc.getElementsByTagName("item");

            debugValue =  nList.getLength(); // TESTING

            // Collects elements until there are no more or max number have been reached
            for(int i =0; i < nList.getLength() && i < numDisplayedElements; i++){
                // Checks if the result is acceptable
                if(nList.item(i).getNodeType() == Node.ELEMENT_NODE){
                    Element elm = (Element)nList.item(i);

                    RssFeedModel tempRSS= new RssFeedModel(
                            elm.getElementsByTagName("title").item(0).getTextContent(),
                            elm.getElementsByTagName("link").item(0).getTextContent(),
                            elm.getElementsByTagName("description").item(0).getTextContent()
                            //((Element)(elm.getElementsByTagName("description").item(0))).getAttribute("img")
                    );

                    currentList.add(tempRSS); // adds the new element
                }
            }
            // Closes the stream
            in.close();

            // Updates currentList, just adds the elements in newList normally, but modifies it otherwise

            newsList.clear();

            // Checks if the expression is empty, if it is not it adds the relevant elements to newsList
            if (expression.equals("")) {
                newsList.addAll(currentList);
            } else { // in case the result have to be gone through with a regular expression
                try {
                    // Checks if pattern is valid by catching an expression if it is not
                    Pattern.compile(expression);

                    // Goes through all items and checks them up against the regular expression
                    for (int i = 0; i < currentList.size(); i++) {
                        if (fitsPattern((RssFeedModel) currentList.get(i), expression)) {
                            newsList.add(currentList.get(i)); // adds the element which fits the criteria
                        }
                    }
                } catch (PatternSyntaxException  err) { // if expression is invalid, reports back and gives empty list
                    Log.e("Expression error", err + "");
                }
            }
            // Catches errors and returns different debugValues for testing
        } catch (ParserConfigurationException err) {
            debugValue = -2;
        } catch (IOException err) {
            debugValue = -3;
        } catch (SAXException err) {
            debugValue = -4;
        }
        // DEBUG
        // Log.d("test", "" + debugValue);

        return debugValue;
    }

    // Value is outside of function to allow elements indie StringRequest to modify it
     int returnValue;

    // Gets RSS feed, no async needed since I am using Volley
    int getRSSFeed() {
        currentList.clear(); // Clears the list of items so new ones can be added without problem
        returnValue = -1;    // sets the default value to -1

        // checks that the url isn't null
        if (url != null) {
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                url = "https://" + url;

            // Makes a RequestQueue
            final RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

            // Gets xml and moves it into the string response inside onResponse
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Parses XML
                            returnValue =  parseXML(response, mSearch.getText().toString());
                            queue.stop();

                            // Runs the code on the main thread even if this is run on an async
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    final RecyclerView recycleViewElement = findViewById(R.id.recyclerView);

                                    MyRecyclerViewAdapter adapter;

                                    // makes a LinearLayoutManager for managing the lines
                                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                    recycleViewElement.setLayoutManager(linearLayoutManager);

                                    adapter = new MyRecyclerViewAdapter(MainActivity.this, newsList);
                                    recycleViewElement.setAdapter(adapter);
                                }
                            });
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Log.e("String Request error", error + "");
                    returnValue = -2;
                    queue.stop();
                }
            });
            queue.add(stringRequest);
        }
        return returnValue;
    }

    // Returns the number of milliseconds that should be waited based on the value sent
    static public int translateFrequency(int currentFrequency) {
        int returnThis;
        switch (currentFrequency) {
            case 1:
                returnThis =  3600000;
                break;
            case 2:
                returnThis =  86400000;
                break;
            default:
                returnThis = 600000; // defaults value to be 10 minutes, 0 will also return this
                break;
        }
        return returnThis;
    }

    // task done in async, placed here to make it testable
    public int asyncTask(int timeGone, int fetchFrequensy) {
        // if the amount of time that have gone by is larger then fetchFreq or feed have been fetched already
        if (resetTimer) { // Resets timer
            timeGone = 0;
            resetTimer = false;
        }
        else if (timeGone >= fetchFrequensy) {
            timeGone = 0;   // Resets timeGone
            getRSSFeed();   // updates the RSS feed
        } else { // Still waiting
            // Log.d("TestAsync " + fetchFreq, timeGone + ""); // DEBUG

            // Sleeps for one minute before the loop can check if it is time to update
            // Checks are so often to ensure if you update manually it doesn't oversleep much
            timeGone += 10000; // minimum time
        }

        // returns value
        return timeGone;
    }


    // AsyncTask, runs every so often based on the config
    class FetchFeedTask extends AsyncTask<Void, Void, Void>{
        // Temporary list, replaces the old one afterwards

        int timeGone;

        @Override
        protected void onPreExecute() {
            //Setup precondition to execute some task, makes feed be run at the start
            timeGone = translateFrequency(2) * 1000;
        }

        // Gets feed and sets all translated elements into temporaryList
        @Override
        protected Void doInBackground(Void... value) {
            while (true) {
                timeGone = asyncTask(timeGone, fetchFreq);
                SystemClock.sleep(10000);
            }
        }

        @Override
        protected void onProgressUpdate(Void... value) {
            //Update the progress of current task
        }
    }
}

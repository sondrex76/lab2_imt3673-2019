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

   /* // Code from guide, didn't work and I should do it myself
    // Values for use in feed
    private List<RssFeedModel> mFeedModelList;
    private String mFeedTitle;
    private String mFeedLink;
    private String mFeedDescription;
*/
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
                // new FetchFeedTask().execute((Void) null);
            }
        });

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // new FetchFeedTask().execute((Void) null);
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
            if (urlLine != "") {
                url = urlLine;
            }

            // if numDisplayedElements have been stored
            if (numDisplayLine != "") {
                numDisplayedElements = Integer.parseInt(numDisplayLine);
            }

            // if fetchFreq have been stored
            if (fetchFreqLine != "") {
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

    /*
    // Class with values of single feed item
    public class RssFeedModel {

        public String title;
        public String link;
        public String description;

        public RssFeedModel(String title, String link, String description) {
            this.title = title;
            this.link = link;
            this.description = description;
        }
    }

    // Class for fetching feed
    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(url))
                return false;

            try {
                if(!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;

                URL urlValue = new URL(url);
                InputStream inputStream = urlValue.openConnection().getInputStream();
                mFeedModelList = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);

            if (success) {
                mFeedTitleTextView.setText("Feed Title: " + mFeedTitle);
                mFeedDescriptionTextView.setText("Feed Description: " + mFeedDescription);
                mFeedLinkTextView.setText("Feed Link: " + mFeedLink);
                // Fill RecyclerView
                mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList));
            } else {
                Toast.makeText(MainActivity.this,
                        "Enter a valid Rss feed url",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // RSS parse class
    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException,
            IOException {
        String title = null;
        String link = null;
        String description = null;
        boolean isItem = false;
        List<RssFeedModel> items = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
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

            return items;
        } finally {
            inputStream.close();
        }
    }

    // Feed list class
    public class RssFeedListAdapter
            extends RecyclerView.Adapter<RssFeedListAdapter.FeedModelViewHolder> {

        private List<RssFeedModel> mRssFeedModels;

        public class FeedModelViewHolder extends RecyclerView.ViewHolder {
            private View rssFeedView;

            public FeedModelViewHolder(View v) {
                super(v);
                rssFeedView = v;
            }
        }

        public RssFeedListAdapter(List<RssFeedModel> rssFeedModels) {
            mRssFeedModels = rssFeedModels;
        }

        @Override
        public FeedModelViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_rss_feed, parent, false);
            FeedModelViewHolder holder = new FeedModelViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(FeedModelViewHolder holder, int position) {
            final RssFeedModel rssFeedModel = mRssFeedModels.get(position);
            ((TextView)holder.rssFeedView.findViewById(R.id.titleText)).setText(rssFeedModel.title);
            ((TextView)holder.rssFeedView.findViewById(R.id.descriptionText))
                    .setText(rssFeedModel.description);
            ((TextView)holder.rssFeedView.findViewById(R.id.linkText)).setText(rssFeedModel.link);
        }

        // Gets number of items
        @Override
        public int getItemCount() {
            return mRssFeedModels.size();
        }
    }*/
}

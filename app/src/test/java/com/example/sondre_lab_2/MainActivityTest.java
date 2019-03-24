package com.example.sondre_lab_2;

import android.app.Activity;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class MainActivityTest {
    @Test
    public void TestgetDataPhysically() { // TODO implement working test for this
        MainActivity m = new MainActivity();

        // assertEquals("Expected false", true, m.getDataPhysically());
    }

    @Test
    public void TestsaveDataPhysically() { // TODO implement working test of this one

    }

    @Test
    public void TestfitsPattern() {
        MainActivity m = new MainActivity();

        RssFeedModel rss = new RssFeedModel("title", "link", "description");

        assertEquals("Expected true", true, m.fitsPattern(rss, ".*"));
        assertEquals("Expected false", false, m.fitsPattern(rss, "a"));
    }

    @Test
    public void TestparseXML() {
        MainActivity m = new MainActivity();
        assertTrue(m.parseXML("aaa", "") < 0);

        // Valid xml
        String rssValue = "<rss version=\"2.0\"><channel><title>xkcd.com</title><link>https://xkcd.com/</link><description>test</description><language>en</language><item><title>test 1</title><link>https://xkcd.com/2120/</link><description>test</description><pubDate>Wed, 06 Mar 2019 05:00:00 -0000</pubDate></item></channel></rss>";

        assertTrue(m.parseXML(rssValue, "") == 1);
    }

    @Test
    public void TestgetRSSFeed() { // TODO figure out how to test a function which will pretty much always work

        MainActivity m = new MainActivity();

        //m.getRSSFeed();

        //m.url = "https://xkcd.com/rss.xml";
        //assertTrue(m.getRSSFeed() >= 0);
        //m.url = "re";
        //assertTrue(m.getRSSFeed() < 0);
        // m.finish();
    }

    @Test
    public void TesttranslateFrequency() {
        MainActivity m = new MainActivity();
        assertEquals("Expected 3600000", 3600000, m.translateFrequency(1));
        assertEquals("Expected 86400000", 86400000, m.translateFrequency(2));
        assertEquals("Expected 600000", 600000, m.translateFrequency(3));
    }

    @Test
    public void TestasyncTask() {
        MainActivity m = new MainActivity();

        // if
        m.resetTimer = true;
        assertEquals("Expected 10 000", 0, m.asyncTask(10000, 10000));

        // else if
        assertEquals("Expected 0", 0, m.asyncTask(10000, 1000));

        // else
        assertEquals("Expected 10 000", 10000, m.asyncTask(0, 10000));
    }
}

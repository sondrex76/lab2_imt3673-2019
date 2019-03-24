package com.example.sondre_lab_2;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.mock;

public class ActivityUserPreferencesTest {
    @Test
    public void TeststartCode() {
        ActivityUserPreferences a = mock(ActivityUserPreferences.class);
        // TODO Figure out how to use mock so I can actually test these stupid things

        //assertEquals("Epected true", true, a.emptyURL(""));
        //assertEquals("Epected false", false, a.emptyURL("aa"));
        a.startCode();
    }
}

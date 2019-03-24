package com.example.sondre_lab_2;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RssFeedModelTest {

    @Test
    public void TestRssFeedModel() {
        RssFeedModel a = new RssFeedModel("a", "b", "c");
        assertEquals("Expected a", "a", a.title);
        assertEquals("Expected b", "b", a.link);
        assertEquals("Expected c", "c", a.description);

        assertNotEquals("Should not be a", "a", a.description);
    }
}

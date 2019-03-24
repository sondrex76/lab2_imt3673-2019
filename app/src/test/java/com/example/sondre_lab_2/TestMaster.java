package com.example.sondre_lab_2;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ActivityContentDisplayTest.class,
        ActivityUserPreferencesTest.class,
        MainActivityTest.class,
        MyRecyclerViewAdapterTest.class,
        RssFeedModelTest.class})
public class TestMaster { }

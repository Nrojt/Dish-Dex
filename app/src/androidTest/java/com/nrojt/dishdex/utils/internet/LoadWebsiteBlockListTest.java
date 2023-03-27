package com.nrojt.dishdex.utils.internet;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class LoadWebsiteBlockListTest {

    private Context context;

    //Instrumentation test allows the test to interact with the application
    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    //Test that the list of ad URLs is not null and is not empty
    @Test
    public void testLoadBlockList() {
        LoadWebsiteBlockList blockList = new LoadWebsiteBlockList(context);
        blockList.loadBlockList();
        ArrayList<String> adUrls = blockList.getAdUrls();
        assertNotNull(adUrls);
        assertFalse(adUrls.isEmpty());
    }
}

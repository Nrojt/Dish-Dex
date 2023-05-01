package com.nrojt.dishdex.backend.viewmodels;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

@RunWith(Parameterized.class)
public class HomePageFragmentViewModelTest {
    // This rule is needed to test LiveData
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    private HomePageFragmentViewModel viewModel;
    private LocalDateTime fixedDateTime;
    private int expectedTimeCategoryId;

    @Before
    public void setUp() {
        viewModel = new HomePageFragmentViewModel();
    }

    // Constructor for the test, we pass in the fixed time and the expected time category id
    // A constructor is needed because you cannot pass parameters into a test method
    // This constructor is called for each test by the Parameterized runner
    public HomePageFragmentViewModelTest(LocalTime fixedTime, int expectedTimeCategoryId) {
        this.fixedDateTime = LocalDateTime.of(2023, 5, 1, fixedTime.getHour(), fixedTime.getMinute());
        this.expectedTimeCategoryId = expectedTimeCategoryId;
    }

    //Using parameters to test multiple values and so I can easily add more values to test
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { LocalTime.parse("05:01"), 1 },
                { LocalTime.parse("08:00"), 1 },
                { LocalTime.parse("09:59"), 1 },
                { LocalTime.parse("10:00"), 2 },
                { LocalTime.parse("12:00"), 2 },
                { LocalTime.parse("13:59"), 2 },
                { LocalTime.parse("14:00"), 3 },
                { LocalTime.parse("18:00"), 3 },
                { LocalTime.parse("19:59"), 3 }
        });
    }

    @Test
    public void testSetTimeCategory() {
        // Making a fixed clock so we can test the time category
        ZoneId zoneId = ZoneId.systemDefault();
        Instant fixedInstant = fixedDateTime.atZone(zoneId).toInstant();
        Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

        // Passing the clock into the viewmodel
        viewModel.setTimeCategoryID(fixedClock);

        assertEquals(expectedTimeCategoryId, viewModel.getTimeCategoryIDLiveData().getValue().intValue());
    }
}

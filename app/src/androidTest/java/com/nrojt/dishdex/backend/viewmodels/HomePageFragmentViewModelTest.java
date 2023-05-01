package com.nrojt.dishdex.backend.viewmodels;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

public class HomePageFragmentViewModelTest {
    // This rule is needed to test LiveData
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    private HomePageFragmentViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new HomePageFragmentViewModel();
    }

    @Test
    public void testSetTimeCategoryIDBreakfast() {
        // Set a fixed date and time to use during testing
        LocalDateTime fixedDateTime = LocalDateTime.of(2023, 5, 1, 5, 1);
        ZoneId zoneId = ZoneId.systemDefault();
        Instant fixedInstant = fixedDateTime.atZone(zoneId).toInstant();
        Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

        // Use the fixed clock to spoof the time in the ViewModel
        viewModel.setTimeCategoryID(fixedClock); // pass the fixed clock as a parameter

        // Assert that the correct time category ID is set for the fixed time
        assertEquals(1, viewModel.getTimeCategoryIDLiveData().getValue().intValue());

        // Set a fixed date and time to use during testing
        fixedDateTime = LocalDateTime.of(2023, 5, 1, 9, 59);
        zoneId = ZoneId.systemDefault();
        fixedInstant = fixedDateTime.atZone(zoneId).toInstant();
        fixedClock = Clock.fixed(fixedInstant, zoneId);

        // Use the fixed clock to spoof the time in the ViewModel
        viewModel.setTimeCategoryID(fixedClock); // pass the fixed clock as a parameter

        // Assert that the correct time category ID is set for the fixed time
        assertEquals(1, viewModel.getTimeCategoryIDLiveData().getValue().intValue());
    }

    @Test
    public void testSetTimeCategoryIDLunch() {
        // Set a fixed date and time to use during testing
        LocalDateTime fixedDateTime = LocalDateTime.of(2023, 5, 1, 10, 0);
        ZoneId zoneId = ZoneId.systemDefault();
        Instant fixedInstant = fixedDateTime.atZone(zoneId).toInstant();
        Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

        // Use the fixed clock to spoof the time in the ViewModel
        viewModel.setTimeCategoryID(fixedClock); // pass the fixed clock as a parameter

        // Assert that the correct time category ID is set for the fixed time
        assertEquals(2, viewModel.getTimeCategoryIDLiveData().getValue().intValue());

        // Set a fixed date and time to use during testing
        fixedDateTime = LocalDateTime.of(2023, 5, 1, 13, 59);
        zoneId = ZoneId.systemDefault();
        fixedInstant = fixedDateTime.atZone(zoneId).toInstant();
        fixedClock = Clock.fixed(fixedInstant, zoneId);

        // Use the fixed clock to spoof the time in the ViewModel
        viewModel.setTimeCategoryID(fixedClock); // pass the fixed clock as a parameter

        // Assert that the correct time category ID is set for the fixed time
        assertEquals(2, viewModel.getTimeCategoryIDLiveData().getValue().intValue());
    }

    @Test
    public void testSetTimeCategoryIDDinner() {
        // Set a fixed date and time to use during testing
        LocalDateTime fixedDateTime = LocalDateTime.of(2023, 5, 1, 14, 0);
        ZoneId zoneId = ZoneId.systemDefault();
        Instant fixedInstant = fixedDateTime.atZone(zoneId).toInstant();
        Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

        // Use the fixed clock to spoof the time in the ViewModel
        viewModel.setTimeCategoryID(fixedClock); // pass the fixed clock as a parameter

        // Assert that the correct time category ID is set for the fixed time
        assertEquals(3, viewModel.getTimeCategoryIDLiveData().getValue().intValue());

        // Set a fixed date and time to use during testing
        fixedDateTime = LocalDateTime.of(2023, 5, 1, 19, 59);
        zoneId = ZoneId.systemDefault();
        fixedInstant = fixedDateTime.atZone(zoneId).toInstant();
        fixedClock = Clock.fixed(fixedInstant, zoneId);

        // Use the fixed clock to spoof the time in the ViewModel
        viewModel.setTimeCategoryID(fixedClock); // pass the fixed clock as a parameter

        // Assert that the correct time category ID is set for the fixed time
        assertEquals(3, viewModel.getTimeCategoryIDLiveData().getValue().intValue());
    }
}

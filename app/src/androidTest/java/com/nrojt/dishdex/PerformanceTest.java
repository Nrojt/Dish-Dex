package com.nrojt.dishdex;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import androidx.test.platform.app.InstrumentationRegistry;

public class PerformanceTest {

    @Test
    public void testSortPerformance() {
        // Generate a large array of integers
        int[] data = new int[100000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (int) (Math.random() * 1000);
        }

        // Record the start time
        long startTime = System.currentTimeMillis();

        // Sort the array
        sort(data);

        // Record the end time
        long endTime = System.currentTimeMillis();

        // Calculate the elapsed time
        long elapsedTime = endTime - startTime;

        // Verify that the elapsed time is less than 1000 milliseconds
        assertTrue(elapsedTime < 1000);
    }

    private void sort(int[] data) {
        // Method to sort the array of integers
    }
}

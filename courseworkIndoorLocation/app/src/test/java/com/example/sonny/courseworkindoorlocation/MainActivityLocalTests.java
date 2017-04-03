package com.example.sonny.courseworkindoorlocation;

/**
 * Created by sstoke04 on 03/04/2017.
 */

import android.location.Location;

import com.indooratlas.android.sdk.IALocation;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class MainActivityLocalTests {

    @Test
    public void checkGetDate()
    {
        Long tsLong = System.currentTimeMillis()/1000;
        DateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy");
        Date currentDate = (new Date(tsLong));
        String mTestTime = formatDate.format(currentDate);
        assert(MainActivity.getDate(tsLong).equals(mTestTime));
    }

    @Test
    public void checkGetDateFalse()
    {
        Long tsLong = System.currentTimeMillis()/1000;
        DateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = (new Date(tsLong));
        String mTestTime = formatDate.format(currentDate);
        assertFalse(MainActivity.getDate(tsLong).equals(mTestTime));
    }

    @Test
    public void checkGetTime()
    {
        Long tsLong = System.currentTimeMillis()/1000;
        DateFormat formatDate = new SimpleDateFormat("HH:mm:ss");
        Date currentTime = (new Date(tsLong));
        String mTestTime = formatDate.format(currentTime);
        assert(MainActivity.getCurrentTime(tsLong).equals(mTestTime));
    }

    @Test
    public void checkGetTimeFalse()
    {
        Long tsLong = System.currentTimeMillis()/1000;
        DateFormat formatDate = new SimpleDateFormat("HH/mm/ss");
        Date currentTime = (new Date(tsLong));
        String mTestTime = formatDate.format(currentTime);
        assertFalse(MainActivity.getCurrentTime(tsLong).equals(mTestTime));
    }


}

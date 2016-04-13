package com.example.lawrence.weather;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

// This is the data/model layer for the app.
public class Weather {
    final String dayOfWeek;
    final String minTemp;
    final String maxTemp;
    final String humidity;
    final String description;
    final String iconURL;

    public Weather(long timeStamp, double minTemp, double maxTemp,
                   double humidity, String description, String iconName) {

        // truncate numbers after decimal point.
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);

        this.dayOfWeek = convertTimeStampToDay(timeStamp);

        // unicode for the "degree" symbol
        this.minTemp = numberFormat.format(minTemp) + "\u00B0F";
        this.maxTemp = numberFormat.format(maxTemp) + "\u00B0F";

        // get humidity percentage, rather than a number.
        this.humidity =
                NumberFormat.getPercentInstance().format(humidity / 100.0);
        this.description = description;
        // url to fetch icon image for the weather condition.
        this.iconURL = "http://openweathermap.org/img/w/" + iconName + ".png";
    }

    // method to get day of week (e.g. Monday, Tuesday, etc.) from timestamp
    private static String convertTimeStampToDay(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp * 1000); // convert millisecs to secs
        TimeZone tz = TimeZone.getDefault(); // get device's time zone

        // adjust time for device's time zone
        calendar.add(Calendar.MILLISECOND,
                tz.getOffset(calendar.getTimeInMillis()));

        // SimpleDateFormat that returns the day's name
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE");
        return dateFormatter.format(calendar.getTime());
    }

}

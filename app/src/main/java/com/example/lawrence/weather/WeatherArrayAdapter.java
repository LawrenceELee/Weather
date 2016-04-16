package com.example.lawrence.weather;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This is also in the controller layer.
// We use an ArrayAdapter to bind data/model from Weather.java to ListView in MainActivity.java
// An optimization to the ArrayAdapter is to use a ViewHolder pattern. This has faster
// performance because we "save"/reduce the amount of findViewById() calls which are VERY expensive.
// The ViewHolder class contains references to the views, if this is being created for the
// first time, we inflate() and findViewById() (both expensive operations), otherwise we
// can save processing by reusing the same ViewHolder object and just reset the data.
public class WeatherArrayAdapter extends ArrayAdapter<Weather> {

    // cache previous downloaded weather icons, so that we don't waste time/bandwidth
    // downloading them again. cache persists until app is terminated.
    private Map<String, Bitmap> iconCache = new HashMap<>();

    // constructor, call superclass
    public WeatherArrayAdapter(Context context, List<Weather> forecast) {
        // forecast is passed from MainActivity to be populated here.
        super(context, -1, forecast);
        // -1 for 2nd arg indicates that we are using a custom layout.
        // so that we can display more than 1 TextView.
        // (by default a ListView displays 1 or 2 TextViews.
    }

    // have to override getView to create custom views for ListView's list_items
    // getView() maps data to a custom ListView item.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // position identifies the ListView's position in the array of ListViews
        // convertView is the ListView itself
        // parent is the layout's parent ViewGroup that we will attach the ListView item in the inflater

        Weather weather = getItem(position);

        // viewholder object holds all the list_item views
        ViewHolder viewHolder;

        // check for reusable ViewHolder from ListView list_item that scrolled offscreen
        if( convertView == null ){

            // no reusable ViewHolder, so create new ViewHolder
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);

            // set properties
            viewHolder.conditionImageView =
                    (ImageView) convertView.findViewById(R.id.conditionImageView);
            viewHolder.dayTextView =
                    (TextView) convertView.findViewById(R.id.dayTextView);
            viewHolder.lowTextView =
                    (TextView) convertView.findViewById(R.id.lowTextView);
            viewHolder.hiTextView =
                    (TextView) convertView.findViewById(R.id.hiTextView);
            viewHolder.humidityTextView =
                    (TextView) convertView.findViewById(R.id.humidityTextView);

            // set tag so that ViewHolder can be reused if needed
            convertView.setTag(viewHolder);

        } else {
            // reuse existing ViewHolder that scrolled off the screen.
            // we get the ref by using getTag()
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // resetting the used existing ViewHolder

        // check cache for icon image
        if( iconCache.containsKey(weather.getIconURL()) ){
            // set image from cache if downloaded before
            viewHolder.conditionImageView.setImageBitmap(iconCache.get(weather.getIconURL()));
        } else {
            // new thread to download icon
            new LoadImageTask(viewHolder.conditionImageView).execute(weather.getIconURL());
        }

        // bind other weather data to view
        Context context = getContext(); // need context to load string resources from strings.xml
        viewHolder.dayTextView.setText(
                context.getString(R.string.day_description, weather.getDayOfWeek(), weather.getDescription())
        );
        viewHolder.lowTextView.setText(
                context.getString(R.string.low_temp, weather.getMinTemp())
        );
        viewHolder.hiTextView.setText(
                context.getString(R.string.high_temp, weather.getMaxTemp())
        );
        viewHolder.humidityTextView.setText(
                context.getString(R.string.humidity, weather.getHumidity())
        );

        // return completed list_item to display
        return convertView;
    }

    // AsyncTask to load weather icon in separate thread
    // AysncTask abstract the details of the creating/executing threads from you.
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap>{
        private ImageView imageView; // displays thumbnail

        // store ImageView on which to set the downloaded Bitmap
        public LoadImageTask(ImageView iv){
            imageView = iv;
        }

        // load image; params[0] is the String URL representing the image
        @Override
        protected Bitmap doInBackground(String... params) {
            // the ... operator allows for a variable number (0 or more) of arguments
            Bitmap icon = null;
            HttpURLConnection connection = null;

            try{
                URL url = new URL(params[0]);

                // open http connection
                connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = null;

                try{
                    inputStream = connection.getInputStream();
                    icon = BitmapFactory.decodeStream(inputStream);
                    iconCache.put(params[0], icon);         // cache icon
                } catch(Exception e){
                    e.printStackTrace();
                }

            } catch (Exception e){
                e.printStackTrace();
            } finally {
                connection.disconnect();    // close HTTP connection
            }

            // return the weather icon
            return icon;
        }
    } // end LoadImageTask inner class

    // use ViewHolder pattern to reuse views as list_items scroll off screen
    // when a ListView is created, a ViewHolder object is associated with that item.
    // if there is a ListView item that's being reused, we simply obtain that item's ViewHolder.
    private static class ViewHolder {
        ImageView conditionImageView;
        TextView dayTextView;
        TextView lowTextView;
        TextView hiTextView;
        TextView humidityTextView;
    } // end ViewHolder inner class
}

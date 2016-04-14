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
public class WeatherArrayAdapter extends ArrayAdapter<Weather> {

    // cache previous downloaded weather icons
    private Map<String, Bitmap> iconCache = new HashMap<>();

    // constructor, call superclass
    public WeatherArrayAdapter(Context context, List<Weather> forecast) {
        super(context, -1, forecast);
    }

    // have to override getView to create custom views for ListView's list_items
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

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
            convertView.setTag(viewHolder);

        } else {
            // reuse existing ViewHolder stored as the list_item's tag
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
    private static class ViewHolder {
        ImageView conditionImageView;
        TextView dayTextView;
        TextView lowTextView;
        TextView hiTextView;
        TextView humidityTextView;
    } // end ViewHolder inner class
}

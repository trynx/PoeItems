package com.nicodo.mypoe;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ListView
        ListView itemsListView = (ListView) findViewById(R.id.lv_items_name);
        List<PoeItem> itemsList = new ArrayList<>();

        PoeArrayAdapter adapter = new PoeArrayAdapter(this, R.layout.listview_item, itemsList);
        // Get the data from the API and save in the item list
        new GetDataTask(itemsList, adapter, getApplicationContext()).execute();

        itemsListView.setAdapter(adapter);
    }

    /**
     * Helper Class Adapter to get a custom ArrayAdapter which accepts PoeItem Class for it's list items
     *
     */
    private class PoeArrayAdapter extends ArrayAdapter<PoeItem> {
        private final int listLayout;
        private boolean didShowAlert = false; // When changing orientation, it will prevent to show it every time

        private PoeArrayAdapter(Context context, int listLayout, List<PoeItem> values) {
            super(context, listLayout, values);
            this.listLayout = listLayout;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){ // Lazy Loading the view - only if really need to create a new one
                convertView = LayoutInflater.from(getContext()).inflate(listLayout, parent, false);
            }

            PoeItem poeItem = getItem(position);

            TextView tvItem = (TextView) convertView.findViewById(R.id.tv_item_name);

            if(poeItem != null){
                tvItem.setText(poeItem.getFullName());
                // Color of text
                tvItem.setBackgroundColor(setTextBackgroundColor(poeItem));
                // Boldness of text
                tvItem.setTypeface(tvItem.getTypeface(), setTextBoldeness(poeItem));
                // Bonus - In case there is a negative level, show it to the user
                levelValidation(tvItem, poeItem);
            }
            return convertView;
        }

        // Depend on the item identified, it will set the boldness
        private int setTextBoldeness(PoeItem item) {
            return item.isIdentified() ? Typeface.BOLD : Typeface.NORMAL;
        }

        // Depend on the item Level, it will set the background color
        private int setTextBackgroundColor(PoeItem item) {
            int lvl = item.getLevel();
            // Depend on the lvl of the item, returns different color
            if (lvl < 0) { // Negative level, do nothing to the background
                return Color.TRANSPARENT;
            }else if(lvl < 10){ // Level under 9 included
                return Color.RED;
            } else if(lvl <= 50){ // Level between 10-50 included
                return Color.YELLOW;
            } else { // Level over 51 included
                return Color.GREEN;
            }
        }

        // Check whether the item have negative or no level -> Let the user know
        private void levelValidation(TextView tv, PoeItem item){
            if(item.getLevel() < 0 && !didShowAlert) {
                tv.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG); // Mark the item
                // Alert the user
                String msg = item.getFullName() + " have a negative or no level";
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).
                                                                        setTitle("Invalid Level").
                                                                        setMessage(msg);
                AlertDialog alert = builder.create();
                alert.show();

                didShowAlert = true; // No need to show again
            }
        }
    }

    /**
     * AsyncTask Class to GET the request from HTTP
     * Once have the data, update the list with the items
     */
    static class GetDataTask extends AsyncTask<Void, Void, Void>{

        private List<PoeItem> itemList;
        // Keep a reference to the adapter to update with the new data later
        private PoeArrayAdapter adapter;

        private GetDataTask(List<PoeItem> itemList, PoeArrayAdapter adapter, Context context) {
            this.itemList = itemList;
            this.adapter = adapter;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String url = "http://nikita.hackeruweb.co.il/hackDroid/items.json";

            JSONObject itemsJson = new JSONObject();
            // Get the data from the HTTP and save in itemsJson
            try {
                HttpRequest req = new HttpRequest(url);
                itemsJson = req.prepare().sendAndReadJSON();
            } catch (JSONException | IOException  e) {
                e.printStackTrace();
            }
            // Only if the JSON have any data, continue on
            if(itemsJson.length() > 0){
                // Test - Show raw json data
                Log.e("JSON", itemsJson.toString());

                try {
                    JSONArray itemsJsonArr = itemsJson.getJSONArray("hits");
                    // Once it's sure that there will be new data in the itemList, clear from old data
                    itemList.clear();
                    // Iterate through the JSON Array with the data to get each JSON Obj with the relevant data
                    for (int i = 0; i < itemsJsonArr.length(); i++){
                        // Base json - get every needed data from this
                        JSONObject tempJson = itemsJsonArr.getJSONObject(i).getJSONObject("_source");
                        // Data
                        String fullName = tempJson.getJSONObject("info").getString("fullName");
                        boolean isIdentified = tempJson.getJSONObject("attributes").getBoolean("identified");

                        // Save the initialization of requirements
                        JSONObject requiJson = tempJson.optJSONObject("requirements");

                        int lvl = -1; // Default invalid
                        if(requiJson != null){ // Check whether there are requirements
                            lvl = requiJson.getInt("Level");
                        } // -> Not: keep -1 lvl, indicate that something is wrong with the item

                        // Test - Show data input
                        Log.e("Name", fullName);
                        Log.e("Identified", Boolean.toString(isIdentified));
                        Log.e("Lvl", Integer.toString(lvl));

                        itemList.add(new PoeItem(fullName, isIdentified, lvl));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Update the new items in the list
            if(itemList.size() > 0) adapter.notifyDataSetChanged();
        }
    }



}


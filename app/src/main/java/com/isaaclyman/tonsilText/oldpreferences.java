package com.isaaclyman.tonsilText;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class oldpreferences extends Activity {

    public static Set<String> mQuotes;

    @Override
    protected void onCreate(Bundle state){
        super.onCreate(state);

        // Open preferences file
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        // Get user-defined quick quotes or use a default
        Set defQuoteSet = new HashSet(Arrays.asList(R.array.defaultQuotes));
        mQuotes = prefs.getStringSet("quotes", defQuoteSet);
    }

    @Override
    protected void onStop(){
        super.onStop();

        // Get editor object for preference changes
        SharedPreferences quotes = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = quotes.edit();
        Set defQuoteSet = new HashSet(Arrays.asList(mQuotes));
        editor.putStringSet("quotes", defQuoteSet);

        // Commit the edits
        editor.commit();
    }

    public static Set<String> getmQuotes() {
        return mQuotes;
    }

}
package com.isaaclyman.tonsilText;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

//
// Main Activity
//
public class MainActivity extends Activity {

    private InputMethodManager imm;
    private EditText editMessage;
    private TextView viewMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Default
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get elements
        editMessage = findViewById(R.id.edit_message);
        viewMessage = findViewById(R.id.view_message);
        imm = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);

        // When the user finishes editing text, submit it
        editMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    unEditText();
                    return true;
                }
                return false;
            }
        });

        // Dirty fix for TextView hint on initial creation
        ViewTreeObserver vto = viewMessage.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                viewMessage.setText(editMessage.getText());

                ViewTreeObserver obs = viewMessage.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });

        // Set default preferences if this is the first run of the app
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // If "Quick Text on startup" pref is set to "true", head on over there
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.edit_button:
                editText();
                return true;
            case R.id.user_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.quick_strings:
                //Intent intent = new Intent(this, QuickStrings.class);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void editText() {
        viewMessage.setVisibility(View.GONE);
        editMessage.setVisibility(View.VISIBLE);
        editMessage.performClick();
        editMessage.requestFocus();
        editMessage.setPressed(true);
        imm.showSoftInput(editMessage, 0);
        editMessage.selectAll();
    }

    public void editText(View view) {
        editText();
    }

    public void unEditText() {
        imm.hideSoftInputFromWindow(editMessage.getWindowToken(), 0);
        viewMessage.setText(editMessage.getText());
        editMessage.setVisibility(View.GONE);
        viewMessage.setVisibility(View.VISIBLE);
    }

}
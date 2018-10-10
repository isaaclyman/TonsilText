package com.isaaclyman.tonsilText;

import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Toast;

import java.util.Locale;

//
// Main Activity
//
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private InputMethodManager imm;
    private EditText editMessage;
    private TextView viewMessage;

    private TextToSpeech mTTS;

    private final int ACT_CHECK_TTS_DATA = 12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Default
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get elements
        editMessage = findViewById(R.id.edit_message);
        viewMessage = findViewById(R.id.view_message);
        FloatingActionButton fabTts = findViewById(R.id.fab_tts);
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

        fabTts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTTS == null) {
                    Intent ttsIntent = new Intent();
                    ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                    startActivityForResult(ttsIntent, ACT_CHECK_TTS_DATA);
                } else {
                    if (!editMessage.getText().toString().trim().isEmpty()) {
                        speakText(editMessage.getText().toString().trim());
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ACT_CHECK_TTS_DATA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // Data exists, so we instantiate the TTS engine
                mTTS = new TextToSpeech(this, this);
            } else {
                // Data is missing, so we start the TTS installation process
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        Drawable drawable = menu.findItem(R.id.user_settings).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, android.R.color.white));
        menu.findItem(R.id.user_settings).setIcon(drawable);

        drawable = menu.findItem(R.id.quick_strings).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, android.R.color.white));
        menu.findItem(R.id.quick_strings).setIcon(drawable);

        drawable = menu.findItem(R.id.edit_button).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, android.R.color.white));
        menu.findItem(R.id.edit_button).setIcon(drawable);

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

    private void speakText(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if (mTTS != null) {
                int result = mTTS.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "TTS language is not supported", Toast.LENGTH_LONG).show();
                } else {
                    if (!editMessage.getText().toString().trim().isEmpty()) {
                        speakText(editMessage.getText().toString().trim());
                    }
                }
            }
        } else {
            Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }
}
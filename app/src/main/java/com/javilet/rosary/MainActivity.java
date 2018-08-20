package com.javilet.rosary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();
// Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();

        SharedPreferences sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        String locale = sharedPreferences.getBoolean("traditional", true) ? "zh-rTW" : "zh";
        conf.setLocale(new Locale(locale.toLowerCase())); // API 17+ only.
// Use conf.locale = new Locale(...) if targeting lower versions
        res.updateConfiguration(conf, dm);
        setContentView(R.layout.activity_main);

        ConstraintLayout joyfulLayout = findViewById(R.id.joyful_layout);
        ConstraintLayout sorrowfulLayout = findViewById(R.id.sorrowful_layout);
        ConstraintLayout gloriousLayout = findViewById(R.id.glorious_layout);
        ConstraintLayout luminousLayout = findViewById(R.id.luminous_layout);

        joyfulLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PlayerActivity.class);
                intent.putExtra("mystery", "joyful");
                startActivity(intent);
            }
        });

        sorrowfulLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PlayerActivity.class);
                intent.putExtra("mystery", "sorrowful");
                startActivity(intent);
            }
        });

        gloriousLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PlayerActivity.class);
                intent.putExtra("mystery", "glorious");
                startActivity(intent);
            }
        });

        luminousLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PlayerActivity.class);
                intent.putExtra("mystery", "luminous");
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_switch_chinese:
                SharedPreferences settings = getSharedPreferences("preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("traditional", !settings.getBoolean("traditional", true));
                editor.apply();
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

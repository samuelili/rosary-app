package com.javilet.samuel.rosary;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}

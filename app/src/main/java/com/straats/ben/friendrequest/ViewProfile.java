package com.straats.ben.friendrequest;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class ViewProfile extends AppCompatActivity {

    TextView profileText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        fab.setVisibility(View.INVISIBLE);

        profileText = findViewById(R.id.profileText);

        fillProfile();
    }

    private void fillProfile() {
        String text = "Instagram: @user\n" +
                "Twitter: @Usseerr\n" +
                "Youtube: Sick Gamer 22\n" +
                "Linkedin: @sadfff";
        profileText.setText(text);
    }
}

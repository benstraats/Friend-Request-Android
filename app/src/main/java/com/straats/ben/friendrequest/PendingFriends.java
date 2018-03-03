package com.straats.ben.friendrequest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class PendingFriends extends AppCompatActivity {

    private ListView mainListView;

    private ArrayList<String> users = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_friends);

        mainListView = findViewById(R.id.PendingList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, users);
        mainListView.setAdapter(adapter);
        populateList();

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Accepted user " + position, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateList() {
        for (int i=0; i<50; i++) {
            users.add("Pending Friend Request " + i);
        }
        adapter.notifyDataSetChanged();
    }
}

package com.example.map;

import android.content.Intent;
import android.os.Bundle;
import android.service.controls.actions.FloatAction;
import android.widget.Button;
import android.widget.Toast;

import com.example.map.model.Place;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    RecyclerView recyclerView;
    FloatingActionButton btnAdd;
    List<Place> places = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        places.add(new Place("Hanoi", "Vietnam", 0));
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(new PlaceApdater(places));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivityForResult(intent, 1);
        });
        recyclerView = findViewById(R.id.recyclerView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String name = data.getStringExtra("name");
            String address = data.getStringExtra("address");
            double distance = data.getDoubleExtra("distance", 0);
            Place place = new Place(name, address, distance);
            Toast.makeText(this, place.getName(), Toast.LENGTH_SHORT).show();
            places.add(place);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

}
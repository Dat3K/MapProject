package com.example.map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.map.model.Place;

import java.util.ArrayList;
import java.util.List;

public class PlaceApdater extends RecyclerView.Adapter<PlaceApdater.ViewHolder>{
    List<Place> places;

    public PlaceApdater(List<Place> places) {
        this.places = places;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.place_item_layout, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place place = places.get(position);
        holder.tvName.setText(place.getName());
        holder.tvAddress.setText(place.getAddress());
        holder.tvDistance.setText(place.getDistance() + " km");
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvDistance;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }
    }
}

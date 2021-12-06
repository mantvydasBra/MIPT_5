package com.example.mipt_5;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class WeatherRVAdapter extends RecyclerView.Adapter<WeatherRVAdapter.ViewHolder> {

    private Context context;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;

    public WeatherRVAdapter(Context context, ArrayList<WeatherRVModel> weatherRVModelArrayList) {
        this.context = context;
        this.weatherRVModelArrayList = weatherRVModelArrayList;
    }

    // Populating recycler view with little cards (horizontally)
    @NonNull
    @Override
    public WeatherRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item, parent, false);
        Log.d("[ INFLATER ]", "Inflated view with little cards");
        return new ViewHolder(view);
    }

    // Populating little cards with data
    @Override
    public void onBindViewHolder(@NonNull WeatherRVAdapter.ViewHolder holder, int position) {

        WeatherRVModel model = weatherRVModelArrayList.get(position);
        holder.tvTemperature.setText(String.format("%s%s", model.getTemperature(), context.getString(R.string.Celsius)));
        Picasso.get().load("http:".concat(model.getIcon())).into(holder.ivCondition);
        holder.tvWind.setText(String.format("%s%s", model.getWindSpeed(), context.getString(R.string.kilometersPerHour)));
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try {
            Date t = input.parse(model.getTime());
            holder.tvTime.setText(output.format(Objects.requireNonNull(t)));
            Log.d("[ onBindViewHolder ]", "Date, wind, temperature set and images loaded");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return weatherRVModelArrayList.size();
    }

    // Getting little card's variables
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvWind;
        private final TextView tvTemperature;
        private final TextView tvTime;
        private final ImageView ivCondition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWind = itemView.findViewById(R.id.tvWindSpeed);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivCondition = itemView.findViewById(R.id.ivCondition);

        }
    }
}

package com.example.acer.popularmoviesapp.Model;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.acer.popularmoviesapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by acer on 5/18/2017.
 */

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.MovieViewHolder> {
    private ArrayList<Movie> movies;
    private int rowLayout;
    private Context context;

    public DataAdapter(List<Movie> movies, int row_layout, Context applicationContext) {
        this.movies = (ArrayList<Movie>) movies;
        this.rowLayout = row_layout;
        this.context = applicationContext;
    }


    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView;
        CardView moviesLayout;



        public MovieViewHolder(View v) {
            super(v);
            moviesLayout = (CardView) v.findViewById(R.id.movies_layout);
            imgView = (ImageView) v.findViewById(R.id.img_movie);


        }
    }



    @Override
    public DataAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Picasso.with(context).load("https://image.tmdb.org/t/p/w300_and_h450_bestv2" + movies.get(position).getBackdropPath()).resize(200, 250).into(holder.imgView);
    }


    @Override
    public int getItemCount() {
        return movies.size();
    }
}

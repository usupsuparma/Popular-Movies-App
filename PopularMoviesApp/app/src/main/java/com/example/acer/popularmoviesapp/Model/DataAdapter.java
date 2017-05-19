package com.example.acer.popularmoviesapp.Model;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.acer.popularmoviesapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by acer on 5/18/2017.
 */

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.MovieViewHolder> {
    private List<Movie> movies;
    private int rowLayout;
    private Context context;

    public DataAdapter(List<Movie> movies, int rowLayout, Context context) {
        this.movies = movies;
        this.rowLayout = rowLayout;
        this.context = context;
    }


    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView;
        CardView moviesLayout;
        TextView movieTitle;
        TextView releaseFilm;
        TextView durasi;
        TextView ranting;
        TextView tandaMark;
        TextView deskripsi;
        VideoView thailer;



        public MovieViewHolder(View v) {
            super(v);
            imgView = (ImageView) v.findViewById(R.id.img_movie);
            moviesLayout = (CardView) v.findViewById(R.id.movies_layout);
//            movieTitle = (TextView) v.findViewById(R.id.title);
//            releaseFilm = (TextView) v.findViewById(R.id.release);
//            durasi = (TextView) v.findViewById(R.id.durasi);
//            ranting = (TextView) v.findViewById(R.id.bintang);
//            tandaMark = (TextView) v.findViewById(R.id.tanda);
//            deskripsi = (TextView) v.findViewById(R.id.deskripsi);
//            thailer = (VideoView) v.findViewById(R.id.thailer);


        }
    }



    @Override
    public DataAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent,false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
//        holder.movieTitle.setText(movies.get(position).getTitle());
//        holder.releaseFilm.setText(movies.get(position).getReleaseDate());
//        holder.durasi.setText(movies.get(position).getVoteCount().toString());
//        holder.ranting.setText(movies.get(position).getVoteAverage().toString());
//        holder.deskripsi.setText(movies.get(position).getOverview());
//        holder.thailer.setVideoPath(movies.get(position).getVideo().toString());
        Picasso.with(context).load("https://image.tmdb.org/t/p/w300_and_h450_bestv2" + movies.get(position).getBackdropPath()).resize(200, 250).into(holder.imgView);
    }


    @Override
    public int getItemCount() {
        return movies.size();
    }
}

package com.example.acer.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.acer.popularmoviesapp.Model.DataAdapter;
import com.example.acer.popularmoviesapp.Model.Movie;
import com.example.acer.popularmoviesapp.Model.MovieResponse;
import com.example.acer.popularmoviesapp.Rest.ApiClient;
import com.example.acer.popularmoviesapp.Rest.ApiInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public final static String EXTRA_MESSAGE_TITLE = "title";
    public final static String EXTRA_MESSAGE_RELESE = "relesea";

    // api key
    private final static String API_KEY = "4057dc6093c3083b684e9221c6e847fe";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (API_KEY.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please obtain your API KEY first from themoviedb.org", Toast.LENGTH_LONG).show();
            return;
        }
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.card_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(),2));



        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<MovieResponse> call = apiService.getTopRatedMovies(API_KEY);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse>call, Response<MovieResponse> response) {
                final List<Movie> movies = response.body().getResults();
                Log.d(TAG, "Number of movies received: " + movies.size());
                Toast.makeText(MainActivity.this, "Number of movies received: " + movies.size(), Toast.LENGTH_LONG).show();
                recyclerView.setAdapter(new DataAdapter(movies, R.layout.row_layout, getApplicationContext()));

                // perintah klik recycleview
                recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {

                        public boolean onSingleTapUp(MotionEvent e){
                            return true;
                        }
                    });
                    @Override
                    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                        View child = rv.findChildViewUnder(e.getX(), e.getY());
                        if (child != null && gestureDetector.onTouchEvent(e)){
                            int position = rv.getChildAdapterPosition(child);
                            Toast.makeText(getApplicationContext(), "Id : " + movies.get(position).getId() + " selected", Toast.LENGTH_SHORT).show();

                            Intent i = new Intent(MainActivity.this, detail.class);
                            //judul
                            i.putExtra("title", movies.get(position).getTitle());

                            // tahun
                            i.putExtra("date", movies.get(position).getReleaseDate());

                            //durasi
                            i.putExtra("durasi",movies.get(position).getPopularity().toString());

                            //ranting
                            i.putExtra("vote", movies.get(position).getVoteAverage().toString());

                            // tanda
                            i.putExtra("tanda",movies.get(position).getVoteCount().toString());

                            // deskripsi
                            i.putExtra("overview", movies.get(position).getOverview());
                            i.putExtra("bg", movies.get(position).getPosterPath());
                            MainActivity.this.startActivity(i);

                        }
                        return false;
                    }

                    @Override
                    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

                    }

                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

                    }
                });
            }

            @Override
            public void onFailure(Call<MovieResponse>call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
            }
        });
    }
}

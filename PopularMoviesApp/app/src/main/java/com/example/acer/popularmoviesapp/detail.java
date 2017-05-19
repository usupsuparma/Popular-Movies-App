package com.example.acer.popularmoviesapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static com.example.acer.popularmoviesapp.MainActivity.EXTRA_MESSAGE_RELESE;
import static com.example.acer.popularmoviesapp.MainActivity.EXTRA_MESSAGE_TITLE;

public class detail extends AppCompatActivity {
    private TextView mTitle, mWaktuRelease,mDurasi,mRanting,mTanda,mDeskripsi;
    private ImageView mBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        // judul
        mTitle = (TextView) findViewById(R.id.tampil_title);
        mTitle.setText(getIntent().getStringExtra("title"));

        // tahun
        mWaktuRelease = (TextView) findViewById(R.id.tampil_tahun);
        mWaktuRelease.setText(getIntent().getStringExtra("date"));

        // durasi
        mDurasi = (TextView) findViewById(R.id.tampil_durasi);
        mDurasi.setText(getIntent().getStringExtra("durasi"));

        // bintang
        mRanting = (TextView) findViewById(R.id.tampil_ranting);
        mRanting.setText(getIntent().getStringExtra("vote"));

        // tanda
        mTanda = (TextView) findViewById(R.id.tampil_tanda);
        mTanda.setText(getIntent().getStringExtra("tanda"));

        // gambar
        mBg = (ImageView) findViewById(R.id.bg);
        Picasso.with(this)
                .load("https://image.tmdb.org/t/p/w300_and_h450_bestv2" + getIntent().getStringExtra("bg"))
                .resize(200, 300)
                .into(mBg);

        // deskripsi
        mDeskripsi = (TextView) findViewById(R.id.tampil_deskripsi);
        mDeskripsi.setText(getIntent().getStringExtra("overview"));

    }
}

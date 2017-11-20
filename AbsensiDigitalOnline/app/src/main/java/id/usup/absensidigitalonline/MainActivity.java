package id.usup.absensidigitalonline;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.usup.absensidigitalonline.api.ApiRequest;
import id.usup.absensidigitalonline.api.ApiServer;
import id.usup.absensidigitalonline.model.Value;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";
    private static final int DEVICE_ID_REQUEST = 101;
    private static final int PERM_COARSE_LOCATION = 110;
    GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;



    @BindView(R.id.textClock_main)
    TextView mClock;
    @BindView(R.id.textView_main_lantitude_cek)
    TextView mLantitude;
    @BindView(R.id.textView_main_longitude)
    TextView mLongitude;
    @BindView(R.id.textView_main_no_hp)
    TextView mNoHandphone;
    @BindView(R.id.textView_main_imei)
    TextView mImei;
    private ProgressDialog progress;

    @OnClick(R.id.button_main_in)
    void sendDataAbsensiIn() {
        Log.d(TAG,"Proses Absen");
        //create progress dialog
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage(getString(R.string.progress_dialog_hint));
        progress.show();

        final String  nip = "1";
        Date currentTime = Calendar.getInstance().getTime();
        Log.d(TAG,"currentTime: "+currentTime);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm:ss a");
        date.setTimeZone(TimeZone.getTimeZone("GMT+7:00"));

        final String jam = date.format(currentLocalTime);
        final String lantitude = mLantitude.getText().toString();
        final String longitude = mLongitude.getText().toString();
        final String noHandphone = mNoHandphone.getText().toString();
        final String imei = mImei.getText().toString();

        ApiRequest api = ApiServer.getApiServer().create(ApiRequest.class);
        Call<Value> call = api.sendDataAbsensi(nip,jam,lantitude,longitude,noHandphone,imei);
        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                final String value = response.body().getValue();
                Log.d(TAG, "value: " + value);
                final String message = response.body().getMessage();
                Log.d(TAG, "message: " + message);

                if (value.equals("1")) {
                    Log.d(TAG, "value dapat 1");
                    Toast.makeText(MainActivity.this, getString(R.string.hint_succes_absent), Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }  else {
                    Toast.makeText(MainActivity.this, getString(R.string.hint_failed_absent), Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                progress.dismiss();
                Toast.makeText(MainActivity.this, getString(R.string.error_message_network), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "error jaringan " + call);

            }
        });


    }

    @OnClick(R.id.button_main_out)
    void sendDataAbsensiOut() {


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getDeviceId();
        setupGoogleAPI();


    }


    public void getDeviceId() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_PHONE_STATE},
                    DEVICE_ID_REQUEST);
        } else {

            assert telephonyManager != null;
            @SuppressLint("HardwareIds") final String deviceId = telephonyManager.getDeviceId();
            @SuppressLint("HardwareIds") final String noPhone = telephonyManager.getLine1Number();
            mImei.setText(deviceId);
            mImei.setKeyListener(null);
            mNoHandphone.setText(noPhone);
            mNoHandphone.setKeyListener(null);
            Log.i(TAG, "Your imei: " + deviceId + " your phone number: " + noPhone);
        }
    }

    private void setupGoogleAPI() {
        // initialize Google API Client
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // get last location ketika berhasil connect
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERM_COARSE_LOCATION);
            return;
        }else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                //Toast.makeText(this," Connected to Google Location API", Toast.LENGTH_LONG).show();
                getLocation();
            }
        }


    }

    private void getLocation() {
        //TODO: lakukan pengambilan data location: mFusedLocationClient.getLastLocation()...
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERM_COARSE_LOCATION);
            // The callback method gets the result of the request.

        }else {

            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            Log.d(TAG,"Your location: Lantitude: "+mLastLocation.getLatitude()+" Longitude: "+mLastLocation.getLongitude());
            if (mLastLocation != null){
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();

                mLantitude.setText(String.valueOf(latitude));
                mLongitude.setText(String.valueOf(longitude));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        getDeviceId();
        setupGoogleAPI();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlertDialog.Builder backComfirm = new AlertDialog.Builder(MainActivity.this);
        backComfirm.setIcon(android.R.drawable.ic_dialog_alert);
        backComfirm.setTitle("Notice");
        backComfirm.setMessage("Do you want Exit? ");
        backComfirm.setCancelable(false);
        backComfirm.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.finish();
            }
        });
        backComfirm.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog check = backComfirm.create();
        check.show();

    }

}


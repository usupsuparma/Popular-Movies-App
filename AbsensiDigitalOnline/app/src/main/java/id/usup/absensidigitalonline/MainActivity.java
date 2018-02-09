package id.usup.absensidigitalonline;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationServices;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.sql.SQLDataException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.usup.absensidigitalonline.controller.Absensi;
import id.usup.absensidigitalonline.fragment.AboutFragment;
import id.usup.absensidigitalonline.fragment.HelpFragment;
import id.usup.absensidigitalonline.model.api.ApiRequest;
import id.usup.absensidigitalonline.model.api.ApiServer;
import id.usup.absensidigitalonline.controller.Value;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";
    private static final int DEVICE_ID_REQUEST = 101;
    private static final int DEVICE_ID_REQUEST_PHONEST = 102;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 112;
    private int i;
    private View mLayout;
    //private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;

    private Location mLastLocation;
    private String mNip;
    private String mAbsenId = "";


    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;


    @BindView(R.id.button_main_in)
    Button mButtonIn;
    @BindView(R.id.button_main_out)
    Button mButtonOut;
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
    @BindView(R.id.textView_jabatan)
    TextView mJabatan;
    @BindView(R.id.textView_nama)
    TextView mNama;
    private ProgressDialog progress;
    private long mFirshTime;

    @OnClick(R.id.button_main_refresh_gps)
    void buton(Button buttona) {
        displayLocation();
        getDeviceId();
    }


    @OnClick(R.id.button_main_in)
    void sendDataAbsensiIn() {
        Log.d(TAG, "Proses Absen");
        //create progress dialog
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage(getString(R.string.progress_dialog_hint));
        progress.show();

        final String nip = mNip;
        Date currentTime = Calendar.getInstance().getTime();
        Log.d(TAG, "currentTime: " + currentTime);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
        Date currentLocalTime = cal.getTime();
        @SuppressLint("SimpleDateFormat") DateFormat date = new SimpleDateFormat("HH:mm:ss a");
        date.setTimeZone(TimeZone.getTimeZone("GMT+7:00"));

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String tanggal = simpleDateFormat.format(cal.getTime());
        final String jm = date.format(currentLocalTime);
        final long compareDiffrence;
        compareDiffrence = currentLocalTime.getTime();

        ApiRequest api = ApiServer.getApiServer().create(ApiRequest.class);
        Call<Value> call = api.sendDataAbsensi(nip, tanggal, jm);
        call.enqueue(new Callback<Value>() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onResponse(@NonNull Call<Value> call, @NonNull Response<Value> response) {
                try {
                    final String value = response.body().getValue();
                    Log.d(TAG, "value: " + value);
                    final String message = response.body().getMessage();
                    Log.d(TAG, "message: " + message);

                    if (value.equals("1")) {
                        Log.d(TAG, "value dapat 1");
                        progress.dismiss();
                        FancyToast.makeText(getApplicationContext(),getString(R.string.hint_succes_absent),FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
                        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                        final SharedPreferences.Editor edit = sharedPreferences.edit();
                        edit.putString("nip", nip);
                        edit.putString("tanggal", tanggal);
                        edit.putString("jm", jm);
                        edit.putLong("compareDiffrece", compareDiffrence);
                        edit.commit();
                        getAbsenId();
                        mButtonIn.setEnabled(false);
                        mButtonOut.setEnabled(true);

                    } else {
                        FancyToast.makeText(getApplicationContext(),getString(R.string.hint_failed_absent),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                        progress.dismiss();
                    }
                }catch (Exception e){
                    progress.dismiss();
                    FancyToast.makeText(getApplicationContext(),getString(R.string.error_message_network),FancyToast.LENGTH_LONG,FancyToast.CONFUSING,true).show();
                    Log.d(TAG, "error jaringan " + call);
                }

            }

            @Override
            public void onFailure(@NonNull Call<Value> call, Throwable t) {
                progress.dismiss();
                //Toast.makeText(getApplicationContext(), getString(R.string.error_message_network), Toast.LENGTH_SHORT).show();
                FancyToast.makeText(getApplicationContext(),getString(R.string.error_message_network),FancyToast.LENGTH_LONG,FancyToast.CONFUSING,true).show();
                Log.d(TAG, "error jaringan " + call);

            }
        });


    }

    @OnClick(R.id.button_main_out)
    void sendDataAbsensiOut() {

        Log.d(TAG, "Proses Absen OUT");
        //create progress dialog
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage(getString(R.string.progress_dialog_hint));
        progress.show();


        Date currentTime = Calendar.getInstance().getTime();
        Log.d(TAG, "currentTime: " + currentTime);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
        Date currentLocalTime = cal.getTime();
        @SuppressLint("SimpleDateFormat") DateFormat date = new SimpleDateFormat("HH:mm:ss a");
        date.setTimeZone(TimeZone.getTimeZone("GMT+7:00"));

        final String absenId = mAbsenId;
        final String jam_keluar = date.format(currentLocalTime);
        final long endTime = currentLocalTime.getTime();
        final long timeFinal = endTime - mFirshTime;
        int hours = (int) (timeFinal / (1000 * 60 * 60));
        int minutes = (int) (timeFinal / (1000 * 60));
        int secons = (int) timeFinal / 1000;
        if (secons >= 60) {
            secons = secons % 60;
            // minutes = minutes + 1;
        }
        if (minutes >= 60) {
            minutes = minutes % 60;
            // hours = hours + 1;
        }
        final String selisih = hours + ":" + minutes + ":" + secons;
        Log.d(TAG, "jam masuk: " + mFirshTime + " jam keluar: " + endTime + " hasil: " + selisih + "detik");
        Log.d(TAG, "Result from difference time: " + timeFinal);


        ApiRequest api = ApiServer.getApiServer().create(ApiRequest.class);
        Call<Value> call = api.absenOut(absenId, jam_keluar, selisih);
        Log.d(TAG, "nilai absen id: " + absenId + " jam keluar: " + jam_keluar + " selisih: " + selisih);

        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                try {
                    final String value = response.body().getValue();
                    Log.d(TAG, "value: " + value);
                    final String message = response.body().getMessage();
                    Log.d(TAG, "message: " + message);

                    if (value.equals("1")) {
                        Log.d(TAG, "value dapat 1");
                        FancyToast.makeText(getApplicationContext(),getString(R.string.hint_succes_absent_out),FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,false).show();

                        getDiffrenceTime();
                    } else {
                        FancyToast.makeText(getApplicationContext(),getString(R.string.error_message_network),FancyToast.LENGTH_LONG,FancyToast.CONFUSING,false).show();
                        progress.dismiss();
                    }
                }catch (Exception e){
                    progress.dismiss();
                    Toast.makeText(MainActivity.this, getString(R.string.error_message_network), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "error jaringan " + call);
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getPermisions();
        getDeviceId();
        getValidasi();
        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }


    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }


    public void getDeviceId() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    DEVICE_ID_REQUEST_PHONEST);


        } else {

            assert telephonyManager != null;
            final String deviceId = telephonyManager.getDeviceId();
            String noPhone = "00000";
            try {
                noPhone = telephonyManager.getLine1Number();
                Log.d(TAG,"no handphone: "+ noPhone);
                if (noPhone.equals("")){
                    noPhone= telephonyManager.getSimSerialNumber();
                    mNoHandphone.setText(noPhone);
                    Log.d(TAG,"serial sim: "+mNoHandphone.toString());
                }
            }catch (NullPointerException ex){
                System.out.print("eror ambil data no telepon");

            }


            mImei.setText(deviceId);
            mImei.setKeyListener(null);
            mNoHandphone.setText(noPhone);
            mNoHandphone.setKeyListener(null);
            Log.i(TAG, "Your imei: " + deviceId + " your phone number: " + noPhone);
        }

    }




    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    DEVICE_ID_REQUEST);
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    DEVICE_ID_REQUEST);

            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        //mLastLocation = LocationServices.FusedLocationApi.getLocationAvailability();


        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            mLongitude.setText(Double.toString(longitude));
            mLongitude.setKeyListener(null);
            mLantitude.setText(Double.toString(latitude));
            mLantitude.setKeyListener(null);

            /*
            real lokasi sekolah
             */
//            final double latA = -6.866806;
//            final double lonA = 108.245717;
//            final double latB = -6.867154;
//            final double lonB = 108.245825;
//            final double latC = -6.867126;
//            final double lonC = 108.246237;

            /*
            uji coba lokasi rumah
             */
//            final double latA = -6.866806;
//            final double lonA = 108.245717;
//            final double latB = -6.867154;
//            final double lonB = 108.245825;
//            final double latC = -6.867126;
//            final double lonC = 108.246237;


            /*
            uji coba kampus
             */
            final double latA = -6.831206;
            final double lonA = 108.215513;
            final double latC = -6.832320;
            final double lonC = 108.215785;

            if ((latitude <= latA && longitude >= lonA) && (latitude >= latC && longitude <= lonC)) {
               // Toast.makeText(getApplicationContext(), getString(R.string.hint_location_user), Toast.LENGTH_LONG).show();
                FancyToast.makeText(this,getString(R.string.hint_location_user),FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
                final Button mbutton_in = findViewById(R.id.button_main_in);
                final Button mButton_out = findViewById(R.id.button_main_out);
                mbutton_in.setEnabled(true);
                mButton_out.setEnabled(false);

            } else {
               // Toast.makeText(getApplicationContext(), getString(R.string.hint_location_user_out), Toast.LENGTH_LONG).show();
                FancyToast.makeText(this,getString(R.string.hint_location_user_out),FancyToast.LENGTH_LONG,FancyToast.WARNING,false).show();

            }

        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.hint_error_gps), Toast.LENGTH_LONG).show();


        }
    }

    private void getValidasi() {
        mButtonIn.setEnabled(false);
        mButtonOut.setEnabled(false);
        Log.d(TAG, "Proses Absen");
        //create progress dialog
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage(getString(R.string.progress_dialog_sever_hint));
        progress.show();
        final String noHandphoneValidasi = mNoHandphone.getText().toString();
        final String imeiValidasi = mImei.getText().toString();
        Log.d(TAG, "nilai nohp: " + noHandphoneValidasi + " nilai imei: " + imeiValidasi);
        ApiRequest apiRequest = ApiServer.getApiServer().create(ApiRequest.class);
        Call<Value> call = apiRequest.Login(imeiValidasi, noHandphoneValidasi);
        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                try {
                    if (response.isSuccessful()) {
                        final String value = response.body().getValue();
                        Log.d(TAG, "value: " + value);
                        final String message = response.body().getMessage();
                        Log.d(TAG, "message: " + message);
                        if (value.equals("1")) {
                            final String nip = response.body().getNip();
                            Log.d(TAG, "nilai nip: " + nip);
                            Log.d(TAG, "value dapat 1");
                            //Toast.makeText(getApplicationContext(), getString(R.string.hint_succes_validasi), Toast.LENGTH_SHORT).show();
                            FancyToast.makeText(getApplicationContext(),getString(R.string.hint_succes_validasi), FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
                            progress.dismiss();

                            final String nama = response.body().getNama();
                            final String alamat = response.body().getAlamat();
                            final String jk = response.body().getJk();
                            final String jabatan = response.body().getJabatan();
                            final String email = response.body().getEmail();
                            mNip = nip;
                            mNama.setText(nama);
                            mJabatan.setText(jabatan);
                            Log.d(TAG, "jabatan: " + jabatan);
//                        mButtonIn.setEnabled(true);
//                        mButtonOut.setEnabled(true);


                        }
                        else {
                            //Toast.makeText(getApplicationContext(), getString(R.string.hint_failed_validasi), Toast.LENGTH_SHORT).show();
                            FancyToast.makeText(getApplicationContext(),getString(R.string.hint_failed_validasi),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                            progress.dismiss();

                        }
                    }
                }catch (Exception e){
                    progress.dismiss();
                    Toast.makeText(getApplicationContext(),"No Handphone atau imei kosong!",Toast.LENGTH_LONG).show();

                }

            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                progress.dismiss();
                Toast.makeText(MainActivity.this, getString(R.string.error_message_network) + " on failure", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "error jaringan validasi " + call);

            }
        });

    }

    private void getAbsenId() {
        final SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        final String nip = sharedPreferences.getString("nip", "");
        final String tanggal = sharedPreferences.getString("tanggal", "");
        final String jm = sharedPreferences.getString("jm", "time");
        final long firshTime = sharedPreferences.getLong("compareDiffrece", mFirshTime);
        Log.d(TAG, "Checking value jam masuk: " + firshTime);

        ApiRequest api = ApiServer.getApiServer().create(ApiRequest.class);
        Call<Value> call = api.selectAbsenId(nip, tanggal, jm);
        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                final String value = response.body().getValue();
                Log.d(TAG, "value: " + value);
                final String message = response.body().getMessage();
                Log.d(TAG, "message: " + message);

                if (value.equals("1")) {
                    Log.d(TAG, "value dapat 1");
                    Log.d(TAG, "Succes get absen ID");
                    final String absenId = response.body().getmAbsenId();
                    Log.d(TAG, "nilai absen Id: " + absenId);
                    mAbsenId = absenId;
                    mFirshTime = firshTime;

                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Failed get Absen Id");


                }
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                Toast.makeText(MainActivity.this, getString(R.string.error_message_network), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "error jaringan " + call);

            }
        });


    }

    private void getPermisions() {
         /* Check and Request permission */
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    101);
        }
    }

    private void getDiffrenceTime(){
        Log.d(TAG,"pada void get diffrence time");
        String absenId= mAbsenId;
        Log.d(TAG,"nilai absenId: "+absenId);
        ApiRequest api = ApiServer.getApiServer().create(ApiRequest.class);
        Call<Absensi> call= api.updateDiffrenceTime(absenId);
        Log.d(TAG,"sukses call");
        call.enqueue(new Callback<Absensi>() {
            @Override
            public void onResponse(Call<Absensi> call, Response<Absensi> response) {
                final String value = response.body().getValue();
                final String message = response.body().getMessage();
                final String selish = response.body().getSelisih();
                try {
                    if (value.equals("1")){
                        progress.dismiss();
                        FancyToast.makeText(getApplicationContext(),getString(R.string.hint_time_work)+" "+selish,FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
                        mButtonOut.setEnabled(false);
                    }else {
                        progress.dismiss();
                        FancyToast.makeText(getApplicationContext(),getString(R.string.error_network),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();

                    }
                }catch (Exception e){
                    progress.dismiss();
                    Toast.makeText(getApplicationContext(),"Kesalahan Server",Toast.LENGTH_LONG).show();
                }


            }

            @Override
            public void onFailure(Call<Absensi> call, Throwable t) {
                final String mCall = t.getMessage();

                progress.dismiss();
                Log.d(TAG,"gagal update "+mCall);

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // get last location ketika berhasil connect


    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

    }

    @Override
    protected void onResume() {
        super.onResume();
        //getDeviceId();
        i = 0;
        // checkPlayServices();
        //displayLocation();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        i++;
        if (i == 1) {
            Toast.makeText(MainActivity.this, "Press back once more to Log Out.",
                    Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    i=0;
                }

                //duration 3 secon

            },3000);
        } else if (i > 1) {

            super.onBackPressed();
            finish();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) {
            startActivity(new Intent(getApplicationContext(), HelpActivity.class));
        } else if (id == R.id.action_about) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}


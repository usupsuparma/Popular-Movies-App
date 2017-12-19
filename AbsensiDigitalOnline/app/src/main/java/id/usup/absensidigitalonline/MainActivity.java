package id.usup.absensidigitalonline;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.usup.absensidigitalonline.model.api.ApiRequest;
import id.usup.absensidigitalonline.model.api.ApiServer;
import id.usup.absensidigitalonline.controller.Value;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";
    private static final int DEVICE_ID_REQUEST = 101;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private int i ;

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
        final long compareDiffrence = currentLocalTime.getTime();

        ApiRequest api = ApiServer.getApiServer().create(ApiRequest.class);
        Call<Value> call = api.sendDataAbsensi(nip, tanggal, jm);
        call.enqueue(new Callback<Value>() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onResponse(@NonNull Call<Value> call, Response<Value> response) {
                final String value = response.body().getValue();
                Log.d(TAG, "value: " + value);
                final String message = response.body().getMessage();
                Log.d(TAG, "message: " + message);

                if (value.equals("1")) {
                    Log.d(TAG, "value dapat 1");
                    Toast.makeText(MainActivity.this, getString(R.string.hint_succes_absent), Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                    final SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("nip", nip);
                    edit.putString("tanggal", tanggal);
                    edit.putString("jm", jm);
                    edit.putLong("compareDiffrece", compareDiffrence);
                    edit.commit();
                    getAbsenId();
                    mButtonIn.setEnabled(false);

                } else {
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

            secons = secons - 60;
            minutes = minutes + 1;
        }
        if (minutes >= 60) {
            minutes = minutes - 60;
            hours = hours + 1;
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
                final String value = response.body().getValue();
                Log.d(TAG, "value: " + value);
                final String message = response.body().getMessage();
                Log.d(TAG, "message: " + message);

                if (value.equals("1")) {
                    Log.d(TAG, "value dapat 1");
                    Toast.makeText(MainActivity.this, getString(R.string.hint_succes_absent), Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                } else {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_NUMBERS},
                    DEVICE_ID_REQUEST);
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_PHONE_STATE},
                    DEVICE_ID_REQUEST);

        } else {

            assert telephonyManager != null;
            @SuppressLint("HardwareIds") final String deviceId = telephonyManager.getDeviceId();
            @SuppressLint("HardwareIds") final String noPhone = telephonyManager.getLine1Number();
            final String no= telephonyManager.getSimSerialNumber();

            Log.d(TAG,"no handphone: "+no);
            mImei.setText(deviceId);
            mImei.setKeyListener(null);
            mNoHandphone.setText(noPhone);
            mNoHandphone.setKeyListener(null);
            Log.i(TAG, "Your imei: " + deviceId + " your phone number: " + noPhone);
        }
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
        getDeviceId();
        i=0;
//        checkPlayServices();
//        displayLocation();

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
        } else if(i>1) {

            super.onBackPressed();
            finish();

        }

    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // TODO: Consider calling
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
            final double latA = -6.866806;
            final double lonA = 108.245717;
            final double latB = -6.867154;
            final double lonB = 108.245825;
            final double latC = -6.867126;
            final double lonC = 108.246237;

            /*
            uji coba lokasi rumah
             */
//            final double latA = -6.866806;
//            final double lonA = 108.245717;
//            final double latB = -6.867154;
//            final double lonB = 108.245825;
//            final double latC = -6.867126;
//            final double lonC = 108.246237;

            if ((latitude <= latA && longitude >= lonA) && (latitude >= latC && longitude <= latC)){
                Toast.makeText(getApplicationContext(), getString(R.string.hint_location_user),Toast.LENGTH_LONG).show();
                final Button mbutton_in = findViewById(R.id.button_main_in);
                final Button mButton_out = findViewById(R.id.button_main_out);
                mbutton_in.setEnabled(true);
                mButton_out.setEnabled(true);

            }else {
                Toast.makeText(getApplicationContext(),getString(R.string.hint_location_user_out),Toast.LENGTH_LONG).show();
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
        Log.d(TAG, "nailai nohp: " + noHandphoneValidasi + " nilai imei: " + imeiValidasi);
        ApiRequest apiRequest = ApiServer.getApiServer().create(ApiRequest.class);
        Call<Value> call = apiRequest.Login(imeiValidasi, noHandphoneValidasi);
        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                if (response.isSuccessful()) {
                    final String value = response.body().getValue();
                    Log.d(TAG, "value: " + value);
                    final String message = response.body().getMessage();
                    Log.d(TAG, "message: " + message);
                    if (value.equals("1")) {
                        final String nip = response.body().getNip();
                        Log.d(TAG, "nilai nip: " + nip);
                        Log.d(TAG, "value dapat 1");
                        Toast.makeText(getApplicationContext(), getString(R.string.hint_succes_validasi), Toast.LENGTH_SHORT).show();
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


                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.hint_failed_validasi), Toast.LENGTH_SHORT).show();
                        progress.dismiss();

                    }
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
                    Log.d(TAG,"Succes get absen ID");
                    final String absenId = response.body().getmAbsenId();
                    Log.d(TAG, "nilai absen Id: " + absenId);
                    mAbsenId = absenId;
                    mFirshTime = firshTime;

                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"Failed get Absen Id");


                }
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                Toast.makeText(MainActivity.this, getString(R.string.error_message_network), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "error jaringan " + call);

            }
        });


    }


}


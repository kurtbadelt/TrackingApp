package tech.e32.lendme_rastreo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, ConnectionCallbacks, LocationListener {
    private static final String TAG = "SigInActivity";
    private static final int RC_SIGN_IN = 9001;
    private String placasUnidad = "";
    private String nombreConductor = "";
    private boolean terminosYCondiciones = false;
    private ToggleButton botonEncendido;
    private EditText textPlacas = null;
    private EditText textConductor = null;
    private CheckBox checkTnC = null;
    private com.google.android.gms.common.SignInButton botonLogIn = null;
    private TextView notif = null;
    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient mGoogleApiClient_Location;
    private LocationRequest mLocationRequest;
    private boolean writeToDB=false;

    /**
     * Método para obtener el timestamp
     *
     * @return yyyy-MM-dd HH:mm:ss formate date as string
     */
    public static String getCurrentTimeStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.runtime_permissions();



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();


        this.textPlacas = (EditText) findViewById(R.id.TFPlacas);
        this.textConductor = (EditText) findViewById(R.id.TFConductor);
        this.checkTnC = (CheckBox) findViewById(R.id.checkBoxTnC);
        this.botonEncendido = (ToggleButton) findViewById(R.id.botonEncendido);
        this.botonLogIn = (com.google.android.gms.common.SignInButton) findViewById(R.id.sign_in_button);
        this.notif = (TextView) findViewById(R.id.tvNotificacion);

        //Desactivar elementos hasta tener al usuario autenticado
        this.botonEncendido.setEnabled(false);
        this.textPlacas.setEnabled(false);
        this.textConductor.setEnabled(false);
        this.checkTnC.setEnabled(false);


        //Logear al usuario usando Google
        this.botonLogIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                textPlacas.setEnabled(true);
                textConductor.setEnabled(true);
                checkTnC.setEnabled(true);
                signIn();

            }

        });


        //habilitar botòn de encendido cuan el usuario acepta los términos y condiciones
        this.checkTnC.setOnClickListener(new View.OnClickListener() {
                                             public void onClick(View view) {
                                                 botonEncendido.setEnabled(true);
                                             }
                                         }
        );

        //Encender el servicio.
        this.botonEncendido.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                execute(view);

            }

        });

        //Location




        mGoogleApiClient_Location = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    @Override
    protected void onStart() {

            super.onStart();
            mGoogleApiClient_Location.connect();


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient_Location.isConnected()) {
            mGoogleApiClient_Location.disconnect();
        }
    }

    /**
     *
     */
    private void signIn() {
        Intent singInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(singInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Result returned from launching the intent from GoogleSingInApi.getIntent
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

    }

    public void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            this.textConductor.setText(acct.getDisplayName());
        } else {

        }
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Fallo la conexion");
    }

    /**
     * método a utilizar cuando se enciende el servicio
     * Activar el servicio de localizaciòn y guardar a la base de datos.+
     *
     * @param
     */
    public void execute(View v) {

        if (this.botonEncendido.isChecked()) {
            encenderServicio();


        } else if (!this.botonEncendido.isChecked()) {
            apagarServicio();


        }


    }

    public void encenderServicio() {

        this.writeToDB=true;
        this.placasUnidad = textPlacas.getText().toString();
        this.nombreConductor = textConductor.getText().toString();
        this.terminosYCondiciones = checkTnC.isChecked();
        this.notif.setText("Servicio Activo");
        Log.w("*encenderServicio ", "" + this.placasUnidad + "," + this.nombreConductor + "," + this.terminosYCondiciones + " TS: " + getCurrentTimeStamp() + " " + this.botonEncendido.isChecked());
    }

    public void apagarServicio() {
        this.writeToDB=false;
        this.textConductor.setText("");
        this.textPlacas.setText("");
        this.checkTnC.setChecked(false);
        this.textConductor.setEnabled(false);
        this.textPlacas.setEnabled(false);
        this.botonEncendido.setEnabled(false);
        this.checkTnC.setEnabled(false);
        this.notif.setText("Desconectado de Google " + "Servicio Inactivo");


    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {


                    }
                });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        try {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient_Location, mLocationRequest, this);



        } catch (SecurityException ex) {
            this.notif.setText("La aplicación requiere de permisos para obtener tu ubicación");

            Log.w("**Error de seguridad ", ex.toString()+"**");

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient_Location.connect();

    }

    @Override
    public void onLocationChanged(Location location) {

        Log.w("Location Changed", "" + this.placasUnidad + "," + this.nombreConductor + "," + this.terminosYCondiciones + " TS: " + getCurrentTimeStamp() + " " + this.botonEncendido.isChecked() + " Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
        if(writeToDB){
            Log.w("on loc changed","write to db enabled");
            this.notif.setText(this.notif.getText()+" Lati: "+location.getLatitude()+" Long: "+location.getLongitude());
        }

    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            } else {
                runtime_permissions();
            }
        }
    }


}

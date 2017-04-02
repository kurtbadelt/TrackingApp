package tech.e32.lendme_rastreo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private boolean writeToDB=false;
    private BroadcastReceiver broadcastReceiver;
    private String email = null;
    private GoogleApiClient mGoogleApiClient_Location;  //Mover a Servicio
    private LocationRequest mLocationRequest; //Mover a servicio
    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    notif.append("\n" +intent.getExtras().get("coordinates"));
                    Log.w("onResume_",intent.getExtras().get("coordinates").toString());


                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
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
        //Mover a servicio
        mGoogleApiClient_Location = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    @Override
    protected void onStart() {

            super.onStart();
            mGoogleApiClient_Location.connect(); //mover a servicio


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient_Location.isConnected()) { //mover a servicio
            mGoogleApiClient_Location.disconnect();
        }
    }


    /**
     * Maneja el resultado de la actividad de logeo a la api de google
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Result returned from launching the intent from GoogleSingInApi.getIntent
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

    }

    /**
     * Manejador de la respuesta de google ante el login
     * @param result
     */
    public void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            this.textConductor.setText(acct.getDisplayName());
            this.email = acct.getEmail();
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

        Intent intento = new Intent(getApplicationContext(),Servicio_Rastreo.class);
        startService(intento);

        this.writeToDB=true;
        this.placasUnidad = textPlacas.getText().toString();
        this.nombreConductor = textConductor.getText().toString();
        this.terminosYCondiciones = checkTnC.isChecked();
        this.notif.setText("Servicio Activo");
        Log.w("*encenderServicio ", "" + this.placasUnidad + "," + this.nombreConductor + "," + this.terminosYCondiciones + " TS: " + getCurrentTimeStamp() + " " + this.botonEncendido.isChecked());
    }

    public void apagarServicio() {

        signOut();
        Intent intento = new Intent(getApplicationContext(),Servicio_Rastreo.class);
        stopService(intento);

        this.writeToDB=false;
        this.textConductor.setText("");
        this.textPlacas.setText("");
        this.checkTnC.setChecked(false);
        this.textConductor.setEnabled(false);
        this.textPlacas.setEnabled(false);
        this.botonEncendido.setEnabled(false);
        this.checkTnC.setEnabled(false);
        this.notif.setText("Desconectado de Google, " + "Servicio Inactivo");
        Log.w("*Apagar Servicio","---");


    }

    /**
     *Login con servicio de google
     */
    private void signIn() {
        Intent singInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(singInIntent, RC_SIGN_IN);
    }

    /**
     * Logout al usuario de google
     */
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {


                    }
                });

    }

    //Mover a servicio
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

    //Mover a Servicio
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient_Location.connect();

    }

    //Mover a servicio
    @Override
    public void onLocationChanged(Location location) {

        Log.w("Location Changed", "" + this.placasUnidad + "," + this.nombreConductor + "," + this.terminosYCondiciones + " TS: " + getCurrentTimeStamp() + " " + this.botonEncendido.isChecked() + " Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
        if(writeToDB){
            Log.w("on loc changed","write to db enabled");
            this.notif.setText(this.notif.getText()+" Lati: "+location.getLatitude()+" Long: "+location.getLongitude());

            //Escribir a base de datos:
            UbicacionConductor ubc = new UbicacionConductor();
            ubc.crearConductor(this.email,this.nombreConductor,this.placasUnidad,getCurrentTimeStamp(),"Aceptado",""+location.getLatitude(),""+location.getLongitude());
            guardarUbicacion(ubc);
        }

    }

    //Escribir a la base de datos
    private void guardarUbicacion(UbicacionConductor ubc) {


        DatabaseReference  mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Ubicaciones").child(ubc.getConductor()+"-"+ubc.getTimeStamp()).setValue(ubc);

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




}

package tech.e32.lendme_rastreo;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by kurtbadelt on 4/2/17.
 */

public class Servicio_Rastreo extends Service implements GoogleApiClient.OnConnectionFailedListener, ConnectionCallbacks, LocationListener{
    private GoogleApiClient mGoogleApiClient_Location;
    private LocationRequest mLocationRequest;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LocationServ";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mGoogleApiClient_Location = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();



        }

            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("location_update");
                i.putExtra("coordinates",location.getLongitude()+" "+location.getLatitude());
                sendBroadcast(i);
                Log.w("*OnLocationChanged","--");
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
            Log.w("**Error de seguridad ", ex.toString()+"**");

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "ConSusp");
        mGoogleApiClient_Location.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Fallo la conexion");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient_Location.isConnected()) {
            mGoogleApiClient_Location.disconnect();




        }
    }
}




package com.example.mausam;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class MyLocation extends Service implements LocationListener
{
    private final Context mContext;

    /* Flag for whether GPS is enabled. */
    boolean isGPSEnabled = false;

    /* Flag for whether getting location via operator is enabled. */
    boolean isNetworkEnabled = false;

    /* Flag for whether the current location can be obtained. */
    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;

    /* Minimum distance change for updates to occur (in metres). */
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    /* Minimum time to have elapsed between updates (in milliseconds). */
    private static final long MIN_TIME_BW_UPDATES = 60 * 1000;

    protected LocationManager locationManager;

    /* Constructor. */
    public MyLocation(Context context)
    {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation()
    {
        try
        {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            /* Check whether GPS is enabled. */
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            /* Check whether location can be obtained via the operator. */
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled)
            {
                /* No way to get location information. */
            }
            else
            {
                this.canGetLocation = true;

                /* First try to get the current location from the operator. */
                if (isNetworkEnabled)
                {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                                           MIN_TIME_BW_UPDATES,
                                                           MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                                           this);
                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null)
                        {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                /* If we don't yet have any location information, then try to get it using GPS. */
                if (location == null)
                {
                    if (isGPSEnabled)
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                                               MIN_TIME_BW_UPDATES,
                                                               MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                                               this);
                        if (locationManager != null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null)
                            {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.v("Exception", e.toString());
            e.printStackTrace();
        }

        return location;
    }

    public double getLatitude()
    {
        if (location != null)
        {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    public double getLongitude()
    {
        if (location != null)
        {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    public boolean canGetLocation()
    {
        return this.canGetLocation;
    }

    public void showSettingsAlert()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("GPS settings");

        alertDialog.setMessage("GPS is not enabled. Do you want to go to the settings menu?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        /* Do nothing. */
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        /* Do nothing. */
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        /* Do nothing. */
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        /* Do nothing. */
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }
}


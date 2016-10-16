package com.syp4.noctua.googleplayservices;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.utilidades.ClienteREST;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GCMRegistrar
{
    private static final String TAG                  = "GooglePlayServicesUtils";
    private static final String PROJECT_NUMBER       = "393209209344";
    private static final String PROPERTY_APP_VERSION = "gcm_app_version";
    private static final String PROPERTY_REG_ID      = "gcm_reg_id";

    public static boolean checkPlayServices(Context context)
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            return false;
        }
        return true;
    }

    /**
     * Obtiene el ID del GCM almacenado en el móvil
     * @param context Contexto actual
     * @return ID del GCM
     */
    public static String getIdGCM(Context context) {
        final SharedPreferences prefs = GCMRegistrar.getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.equals("")) {
            return "";
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion)
        {
            return "";
        }
        return registrationId;
    }

    private static SharedPreferences getGCMPreferences(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static int getAppVersion(Context context) {
        try
        {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static void checkManifest(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        String permissionName = packageName + ".permission.C2D_MESSAGE";

        // check permission
        try {
            packageManager.getPermissionInfo(permissionName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException(
                    "Application does not define permission " + permissionName);
        }
        // check receivers
        PackageInfo receiversInfo;
        try {
            receiversInfo = packageManager.getPackageInfo(
                    packageName, PackageManager.GET_RECEIVERS);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException(
                    "Could not get receivers for package " + packageName);
        }
        ActivityInfo[] receivers = receiversInfo.receivers;
        if (receivers == null || receivers.length == 0) {
            throw new IllegalStateException("No receiver for package " +
                    packageName);
        }
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "number of receivers for " + packageName + ": " +
                    receivers.length);
        }
        Set<String> allowedReceivers = new HashSet<String>();
        for (ActivityInfo receiver : receivers) {
            if (GCMConstants.PERMISSION_GCM_INTENTS.equals(
                    receiver.permission)) {
                allowedReceivers.add(receiver.name);
            }
        }
        if (allowedReceivers.isEmpty()) {
            throw new IllegalStateException("No receiver allowed to receive " +
                    GCMConstants.PERMISSION_GCM_INTENTS);
        }
        checkReceiver(context, allowedReceivers,
                GCMConstants.INTENT_FROM_GCM_REGISTRATION_CALLBACK);
        checkReceiver(context, allowedReceivers,
                GCMConstants.INTENT_FROM_GCM_MESSAGE);
    }

    private static void checkReceiver(Context context,
                                      Set<String> allowedReceivers, String action) {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        Intent intent = new Intent(action);
        intent.setPackage(packageName);
        List<ResolveInfo> receivers = pm.queryBroadcastReceivers(intent,
                PackageManager.GET_INTENT_FILTERS);
        if (receivers.isEmpty()) {
            throw new IllegalStateException("No receivers for action " +
                    action);
        }
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Found " + receivers.size() + " receivers for action " +
                    action);
        }
        // make sure receivers match
        for (ResolveInfo receiver : receivers) {
            String name = receiver.activityInfo.name;
            if (! allowedReceivers.contains(name)) {
                throw new IllegalStateException("Receiver " + name +
                        " is not set with permission " +
                        GCMConstants.PERMISSION_GCM_INTENTS);
            }
        }
    }

    /**
     * Registra el valor del GCM en la base de datos y el móvil
     * @param context Contexto actual
     */
    public static void registrarGCM(final Context context)
    {
        // Si no existe el ID del GCM lo generamos
        if (getIdGCM(context).equals(""))
        {
            new AsyncTask<Void, Void, String>() {
                private GoogleCloudMessaging gcm;

                @Override
                protected String doInBackground(Void... params) {
                    try {
                        if (gcm == null) {
                            gcm = GoogleCloudMessaging.getInstance(context);
                        }
                        String regid = gcm.register(PROJECT_NUMBER);

                        sendRegistrationIdToBackend(context, regid);
                    }
                    catch (IOException ex) { }
                    return "";
                }
            }.execute(null, null, null);
        }
        else
        {
            sendRegistrationIdToBackend(context);
        }
    }

    private static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private static void sendRegistrationIdToBackend(final Context context, final String regid) {
        final ClienteREST client = new ClienteREST(context);
        JSONObject json = new JSONObject();
        try {
            json.put("token", Helpers.getTokenAcceso(context));
            json.put("gcm", regid);
        }
        catch (Exception ex) { }

        client.post(context, Helpers.URLApi("registrargcm"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                // Si se ha guardado correctamente en la base de datos, lo guardamos en el equipo
                GCMRegistrar.storeRegistrationId(context, regid);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
            }
        });
    }

    private static void sendRegistrationIdToBackend(final Context context) {
        final String regid = getIdGCM(context);
        final ClienteREST client = new ClienteREST(context);
        JSONObject json = new JSONObject();
        try {
            json.put("token", Helpers.getTokenAcceso(context));
            json.put("gcm", regid);
        }
        catch (Exception ex) { }

        client.post(context, Helpers.URLApi("registrargcm"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                // Si se ha guardado correctamente en la base de datos, lo guardamos en el equipo
                GCMRegistrar.storeRegistrationId(context, regid);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
            }
        });
    }
}
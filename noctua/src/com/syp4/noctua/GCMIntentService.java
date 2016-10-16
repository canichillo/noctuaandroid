package com.syp4.noctua;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Calendar;

public class GCMIntentService extends IntentService
{
    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);
        Bundle extras = intent.getExtras();

        if (!extras.isEmpty())
        {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
            {
                Log.e("TAG", "Send error: " + extras.toString());
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
            {
                Log.e("TAG", "Deleted messages on server: " + extras.toString());
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                mostrarNotification(Calendar.getInstance().get(Calendar.YEAR) + Calendar.getInstance().get(Calendar.MONTH) + Calendar.getInstance().get(Calendar.DATE) +
                                    Calendar.getInstance().get(Calendar.HOUR) + Calendar.getInstance().get(Calendar.MINUTE) + Calendar.getInstance().get(Calendar.SECOND),
                                    extras.getString("titulo"), extras.getString("mensaje"), extras.getString("ventana"), extras.getString("codigo"));
            }
        }

        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void mostrarNotification(int id, String title, String msg, String ventana, String codigo)
    {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(msg)
                .setLights(Color.argb(255, 128, 128, 128), 20, 20)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);

        // Abrimos la ventana principal        
        Intent notIntent = new Intent(this, Principal.class);
        // Configuramos los datos de la ventana que abrirá o cargará
        notIntent.putExtra("ventana", ventana);
        notIntent.putExtra("codigo", codigo);
        notIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(contIntent);
        mNotificationManager.notify(id, mBuilder.build());
    }
}
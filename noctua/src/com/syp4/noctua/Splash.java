package com.syp4.noctua;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import org.apache.http.Header;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.syp4.noctua.utilidades.ClienteREST;

public class Splash extends FragmentActivity {
    final Handler handle = new Handler();

    protected void miHilo()
    {
        Thread t = new Thread()
        {
            public void run()
            {
                handle.post(proceso);
            }
        };
        t.start();
    }

    final Runnable proceso = new Runnable() {
        @Override
        public void run() {
            // Datos JSON a enviar al POST
            JSONObject json = JSONToken();

            // Cliente REST
            ClienteREST cliente = new ClienteREST(Splash.this);

            cliente.post(Splash.this, Helpers.URLApi("accesotoken"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    super.onSuccess(statusCode, headers, responseBody);

                    try {
                        // Leemos la respuesta
                        JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                        // Si está correcto
                        if (respuesta.isNull("Error"))
                        {
                            // Abrimos la actividad principal
                            Intent intent = new Intent(Splash.this, Principal.class);
                            intent.putExtra("ventana", "Ofertas");
                            startActivity(intent);

                            // Finalizamos la tarea
                            finish();
                        }
                        // Si se ha producido un error
                        else
                        {
                            // Abrimos la actividad principal
                            Intent intent = new Intent(Splash.this, Principal.class);
                            intent.putExtra("ventana", "Login");
                            startActivity(intent);

                            // Finalizamos la actividad
                            finish();
                        }
                    }
                    catch (Exception ex) { }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);

                    // Mostramos el mensaje de error
                    Helpers.MostrarError(Splash.this, "No hay conexión con el servidor de Noctua");
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Si hay conexion a Internet llamamos a la tarea asincrona para conectar con el servicio. Si no, mostramos
        // un mensaje.
        if (!estaConectado()) {
            Helpers.MostrarError(this, "No hay conexión a Internet. Compruebe si la conexión Wifi o la tarifa de datos están conectados.");
        }
        else
        {
            // Si hemos establecido el token de acceso
            if (Helpers.getTokenAcceso(this).equals(""))
            {
                // Abrimos la actividad principal
                Intent intent = new Intent(Splash.this, Principal.class);
                intent.putExtra("ventana", Helpers.getValor(this, "ayuda").isEmpty() || Helpers.getValor(this, "ayuda").equals("") ? "Ayuda" : "Login");
                startActivity(intent);

                // Finalizamos la actividad
                finish();
            }
            else
            {
                // Establecemos el hilo
                miHilo();
            }
        }
    }

    /**
     * Crea el objeto JSON para la petición POST
     * @return Objeto JSON
     */
    private JSONObject JSONToken()
    {
        try {
            // Objeto JSON
            JSONObject json = new JSONObject();

            // Establecemos el valor del token
            json.put("token", Helpers.getTokenAcceso(this));

            // Indicamos con que sistema operativo hemos entrado
            json.put("so", "A");

            // Indicamos con que dispositivo hemos entrado
            json.put("dispositivo", Build.BRAND + " " + Build.DEVICE);

            // Devolvemos el objeto JSON
            return json;
        }
        catch (Exception ex) { return null; }
    }

    /**
     * Metodo que comprueba si hay conexion a Internet.
     *
     * @return true si hay conexion, false si no hay conexion a Internet.
     */
    public boolean estaConectado() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
}

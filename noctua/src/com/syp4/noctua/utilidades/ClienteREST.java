package com.syp4.noctua.utilidades;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.MySSLSocketFactory;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.R;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.InputStream;
import java.security.KeyStore;

public class ClienteREST extends AsyncHttpClient
{
    public ClienteREST(Context contexto)
    {
        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            InputStream in = contexto.getResources().openRawResource(R.raw.server);
            try {
                trustStore.load(in, Helpers.claveSSL.toCharArray());
            } finally {
                in.close();
            }

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            setSSLSocketFactory(sf);
        } catch (Exception e) {
            Log.e("LOG_TAG", "SSL exception");
        }
    }
}

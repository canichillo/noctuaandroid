package com.syp4.noctua;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;

import com.example.touch.TouchImageView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.syp4.noctua.modelos.ImagenGaleria;
import com.syp4.noctua.ui.NoctuaTextView;
import com.syp4.noctua.utilidades.ClienteREST;
import com.syp4.noctua.utilidades.OnSwipeTouchListener;
import com.syp4.noctua.utilidades.SecureImageDownloader;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

public class ImagenesEmpresa extends FragmentActivity
{
    // ID de la empresa
    int idEmpresa = 0;

    // Descripciones
    ArrayList<ImagenGaleria> galeria = new ArrayList<ImagenGaleria>();

    // Scroll de las imágenes
    int scrollImagenes = -1;

    // Nuestra petición de imágenes
    ClienteREST cliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).
                imageDownloader(new SecureImageDownloader(this, 250, 250)).
                build();
        ImageLoader.getInstance().init(config);

        setContentView(R.layout.imagenesempresa);

        // Si tenemos datos
        if (savedInstanceState != null)
        {
            galeria        = savedInstanceState.getParcelableArrayList("galeria");
            scrollImagenes = savedInstanceState.getInt("posicion");
        }
        else
        {
            if (scrollImagenes == -1) scrollImagenes = Integer.parseInt(Helpers.getValor(this, "scroll"));
        }

        // Obtenemos la empresa
        idEmpresa = Integer.parseInt(Helpers.getValor(this, "empresa"));

        if (galeria.size() > 0)
        {
            // Mostramos la imagen
            ((TouchImageView) findViewById(R.id.imagengrandeempresa)).setImageBitmap(Helpers.LeerImagen(this, "temp", galeria.get(scrollImagenes).ARCHIVO + ".jpg"));

            // Establecemos el título
            ((NoctuaTextView) findViewById(R.id.txtTitulo)).setText("Imágenes (" + (scrollImagenes + 1) + " de " + galeria.size() + ")");
        }

        // Establecemos los eventos de si movemos el zoom en la imagen
        ((TouchImageView) findViewById(R.id.imagengrandeempresa)).setCustomTouchListener(new OnSwipeTouchListener(this)
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                super.onTouch(v, event);

                // Si hemos superado el 100% del zoom, ocultamos la descripcion
                if (((TouchImageView)v).getCurrentZoom() > 1.1F)
                {
                    findViewById(R.id.txtDescripcion).setVisibility(View.INVISIBLE);
                }
                else
                {
                    findViewById(R.id.txtDescripcion).setVisibility(View.VISIBLE);
                }

                return false;
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();

                if (((TouchImageView) findViewById(R.id.imagengrandeempresa)).getCurrentZoom() <= 1.1F)
                {
                    // Si quedan elementos
                    if (scrollImagenes < galeria.size() - 1) {
                        // Aumentamos el scroll
                        scrollImagenes++;

                        // Mostramos la imagen
                        ((TouchImageView) findViewById(R.id.imagengrandeempresa)).setImageBitmap(Helpers.LeerImagen(ImagenesEmpresa.this, "temp", galeria.get(scrollImagenes).ARCHIVO + ".jpg"));

                        // Establecemos el título
                        ((NoctuaTextView) findViewById(R.id.txtTitulo)).setText("Imágenes (" + (scrollImagenes + 1) + " de " + galeria.size() + ")");

                        // Establecemos la descripción
                        ((NoctuaTextView) findViewById(R.id.txtDescripcion)).setText(galeria.get(scrollImagenes).DESCRIPCION);
                    }
                }
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();

                if (((TouchImageView) findViewById(R.id.imagengrandeempresa)).getCurrentZoom() <= 1.1F)
                {
                    // Si quedan elementos
                    if (scrollImagenes > 0) {
                        // Disminuimos el scroll
                        scrollImagenes--;

                        // Mostramos la imagen
                        ((TouchImageView) findViewById(R.id.imagengrandeempresa)).setImageBitmap(Helpers.LeerImagen(ImagenesEmpresa.this, "temp", galeria.get(scrollImagenes).ARCHIVO + ".jpg"));

                        // Establecemos el título
                        ((NoctuaTextView) findViewById(R.id.txtTitulo)).setText("Imágenes (" + (scrollImagenes + 1) + " de " + galeria.size() + ")");

                        // Establecemos la descripción
                        ((NoctuaTextView) findViewById(R.id.txtDescripcion)).setText(galeria.get(scrollImagenes).DESCRIPCION);
                    }
                }
            }
        });

        // Inicializamos la librería HTTP
        cliente = new ClienteREST(this);
    }

    /**
     * Lee las imágenes de la empresa
     */
    private void LeerImagenesEmpresa()
    {
        // Si ya hemos leido la galería de la empresa
        if (galeria != null && galeria.size() != 0) return;

        // Datos de la petición
        JSONObject json = new JSONObject();

        try {
            json.put("empresa", idEmpresa);
        }
        catch (Exception ex) { }

        // Realizamos la petición para obtener la información de la empresa
        cliente.post(this, Helpers.URLApi("imagenesempresa"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try {
                    // Obtenemos los datos de las imágenes
                    galeria = ImagenGaleria.ConsumirLista(Helpers.ResponseBodyToJSONArray(responseBody));

                    ((NoctuaTextView) findViewById(R.id.txtTitulo)).setText("Imágenes (" + (scrollImagenes + 1) + " de " + galeria.size() + ")");

                    // Para cada una de las imágenes
                    for (int indice = 0; indice < galeria.size(); indice++)
                    {
                        // Índice de la imagen
                        final int indiceImagen = indice;

                        // Obtenemos la imagen
                        ImageLoader.getInstance().loadImage(Helpers.URLImagenes("ImagenesEmpresa/" + galeria.get(indice).ARCHIVO + ".jpg"), new SimpleImageLoadingListener(){
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                super.onLoadingComplete(imageUri, view, loadedImage);

                                // Guardamos la imagen
                                Helpers.GuardarImagen(ImagenesEmpresa.this, loadedImage, "temp", galeria.get(indiceImagen).ARCHIVO + ".jpg");

                                // Si es el primer indice
                                if (indiceImagen == scrollImagenes) {
                                    ((NoctuaTextView) findViewById(R.id.txtDescripcion)).setText(galeria.get(indiceImagen).DESCRIPCION);
                                    ((TouchImageView) findViewById(R.id.imagengrandeempresa)).setImageBitmap(loadedImage);
                                }
                            }
                        });
                    }
                }
                catch (Exception ex) { }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Mostramos el mensaje de error
                Helpers.MostrarError(ImagenesEmpresa.this, "No se ha podido obtener las imágenes de la empresa");
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Guardamos los datos de la galería
        outState.putParcelableArrayList("galeria", galeria);
        // Guardamos el elemento mostrado
        outState.putInt("posicion", scrollImagenes);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Leemos las imágenes
        LeerImagenesEmpresa();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        // Si tenemos imágenes cargadas
        if (galeria.size() != 0) {
            // Para cada una de las imágenes
            for (int indice = 0; indice < galeria.size(); indice++) {
                // Eliminamos la imagen almacenada
                Helpers.EliminarImagen(this, "temp", galeria.get(indice).ARCHIVO + ".jpg");
            }
        }

        super.onDestroy();
        cliente.cancelRequests(this, true);
    }
}

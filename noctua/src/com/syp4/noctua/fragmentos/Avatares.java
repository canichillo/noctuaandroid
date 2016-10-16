package com.syp4.noctua.fragmentos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.R;
import com.syp4.noctua.holders.AvatarViewHolder;
import com.syp4.noctua.modelos.Avatar;
import com.syp4.noctua.utilidades.AdaptadorDinamico;
import com.syp4.noctua.utilidades.ClienteREST;
import com.syp4.noctua.utilidades.SecureImageDownloader;

import org.apache.http.Header;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Avatares extends Fragment
{
    // Creamos el cliente HTTP
    ClienteREST cliente;

    // Opciones de la visualización de imágenes
    DisplayImageOptions options;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecemos las opciones
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading)
                .showImageForEmptyUri(R.drawable.loading)
                .showImageOnFail(R.drawable.loading)
                .cacheInMemory(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity())
                .imageDownloader(new SecureImageDownloader(getActivity(), 250, 250))
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        // Inicializamos la configuración
        ImageLoader.getInstance().init(config);

        // Mantenemos la instancia
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Cargamos la vista
        View view = inflater.inflate(R.layout.avatares, null);

        // Establecemos el título de la ficha
        Helpers.SetTitulo(getActivity(), getString(R.string.avatares));

        // Cambiamos la imagen del botón de la cabecera, a la de atrás
        Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.back, Helpers.ATRAS);

        // Desactivamos los 2 menús
        Helpers.ChangeMenuMode(getActivity(), Helpers.NINGUNO);

        // Mostramos la sombra
        Helpers.EstadoSombra(getActivity(), true);

        // Inicializamos el cliente HTTP
        cliente = new ClienteREST(getActivity());

        // Devolvemos la vista
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Leemos los datos de los avatares
        LeerAvatares();
    }

    /**
     * Lee los datos de los avatares y los muestra por pantalla
     */
    private void LeerAvatares()
    {
        // Realizamos la solicitud de datos
        cliente.get(getActivity(), Helpers.URLApi("imagenesavatar"), null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                // Obtenemos la lista de avatares
                final ArrayList<Avatar> avatares = Avatar.ConsumirLista(Helpers.ResponseBodyToJSONArray(responseBody));

                // Creamos el adaptador dinámico
                AdaptadorDinamico<AvatarViewHolder, Avatar> adaptador = new AdaptadorDinamico<AvatarViewHolder, Avatar>(getActivity(), R.layout.avatares_item, avatares) {
                    @Override
                    public AvatarViewHolder onViewHolder(View view) {
                        AvatarViewHolder holder = new AvatarViewHolder();
                        holder.imagen           = (ImageView) view.findViewById(R.id.image);

                        return holder;
                    }

                    @Override
                    public void onEntrada(AvatarViewHolder holder, Avatar avatar, View view, int posicion) {
                        ImageLoader.getInstance().displayImage(Helpers.URLImagenes("Avatares/" + avatar.ID + ".png"),
                                holder.imagen, options, new SimpleImageLoadingListener()
                                {
                                    @Override
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                        super.onLoadingComplete(imageUri, view, loadedImage);
                                    }
                                });

                        // Establecemos el TAG de la imagen
                        holder.imagen.setTag(avatar.ID);

                        // Evento del click sobre la imagen
                        holder.imagen.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // ID de la imagen
                                final String idTag = (String) view.getTag();

                                // Mostramos un mensaje de si queremos seleccionar o no el avatar
                                Helpers.MostrarConfirmacion(getActivity(), getString(R.string.confirmacion), getString(R.string.confseleccionaavatar), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // El cliente hacia el servidor
                                        ClienteREST client = new ClienteREST(getActivity());

                                        // Realizamos la obtención de la imagen
                                        client.get(Helpers.URLImagenes("Avatares/" + idTag + ".png"), new BinaryHttpResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, byte[] binaryData) {
                                                super.onSuccess(statusCode, binaryData);

                                                try {
                                                    // Procesamos los bytes de la imagen recibida
                                                    Bitmap imagen = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);

                                                    // Obtenemos la ruta donde guardaremos la imagen
                                                    String szRuta = Helpers.ImagenFotoPerfil(getActivity());

                                                    // Si existe el archivo lo eliminamos
                                                    if (new File(szRuta).exists())
                                                        new File(szRuta).delete();

                                                    try {
                                                        // Guardamos la imagen
                                                        FileOutputStream fOut = new FileOutputStream(szRuta);
                                                        imagen.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                                                        fOut.flush();
                                                        fOut.close();

                                                        // Volvemos hacia atrás
                                                        Helpers.BackFragment(getActivity());
                                                    }
                                                    catch (IOException ioe) { }
                                                }
                                                catch (Exception ex) { }
                                            }

                                            @Override
                                            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                                                super.onFailure(statusCode, headers, binaryData, error);
                                            }
                                        });
                                    }
                                }, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                });
                            }
                        });
                    }
                };

                // Obtenemos la lista
                GridView lista = (GridView) getActivity().findViewById(R.id.listaavatares);
                lista.setAdapter(adaptador);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cliente.cancelRequests(getActivity(), true);
    }
}
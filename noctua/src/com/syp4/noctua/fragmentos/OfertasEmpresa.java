package com.syp4.noctua.fragmentos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.Principal;
import com.syp4.noctua.R;
import com.syp4.noctua.holders.OfertaViewHolder;
import com.syp4.noctua.modelos.Oferta;
import com.syp4.noctua.ui.NoctuaButton;
import com.syp4.noctua.ui.NoctuaTextView;
import com.syp4.noctua.utilidades.AdaptadorDinamico;
import com.syp4.noctua.utilidades.ClienteREST;
import com.syp4.noctua.utilidades.GPSTracker;
import com.syp4.noctua.utilidades.SecureImageDownloader;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;


public class OfertasEmpresa extends Fragment
{
    // Nuestro gestor de posición
    GPSTracker gps;

    // Nuestra posición
    private LatLng miPosicion;

    // Nuestro listado de ofertas
    ArrayList<Oferta> ofertas;

    // Opciones de la visualización de imágenes
    DisplayImageOptions options;

    // Nuestro adaptador dinámico
    AdaptadorDinamico<OfertaViewHolder, Oferta> adaptador;

    // Creamos el cliente JSON
    ClienteREST cliente;

    // Empresa
    int idEmpresa;

    public static OfertasEmpresa newInstance(int id)
    {
        OfertasEmpresa frag = new OfertasEmpresa();
        frag.idEmpresa      = id;
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecemos las opciones
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.imagenes)
                .showImageForEmptyUri(R.drawable.imagenes)
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

        // Mantenemos los datos
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Cargamos la vista
        View view = inflater.inflate(R.layout.ofertasempresa, null);

        // Establecemos el título de la ventana
        Helpers.SetTitulo(getActivity(), getString(R.string.ofertas));

        // Mostramos el menú
        Helpers.EstadoCabecera(getActivity(), true);

        // Establecemos el color
        Helpers.ColorCabecera(getActivity(), Helpers.getIntFromColor(63, 157, 217));

        // Establecemos el icono de la parte izquierda
        Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.back, Helpers.ATRAS);
        // Establecemos el icono de la parte derecha
        Helpers.IconoDerechaTitulo(getActivity(), 0);

        // Ocultamos la sombra
        Helpers.EstadoSombra(getActivity(), true);

        // Desactivamos el menú
        Helpers.ChangeMenuMode(getActivity(), Helpers.NINGUNO);

        // Inicializamos el cliente HTTP
        cliente = new ClienteREST(getActivity());

        // Devolvemos la vista
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Refrescamos el menú
        ((Principal) getActivity()).RefrescarMenu();

        // Creamos el gestor de posiciones
        gps = new GPSTracker(getActivity());
        gps.setOnLocationChange(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                // Leemos el punto actual
                miPosicion = new LatLng(location.getLatitude(), location.getLongitude());
            }
        });

        // Si no tenemos activo el GPS
        if (!gps.canGetLocation()) {
            gps.showSettingsAlert(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        // Establecemos el punto inicial como nuestra localización
        miPosicion = new LatLng(gps.getLatitude(), gps.getLongitude());

        // Leemos los datos de las ofertas
        LeerOfertas(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Lee las ofertas de la base de datos
     */
    private void LeerOfertas(boolean refrescar)
    {
        // Si ya hemos leido las ofertas
        if (ofertas != null && !refrescar)
        {
            // Configuramos las ofertas
            ConfigurarOfertas();

            // Salimos de la función
            return;
        }

        // Creamos el objeto JSON de la petición
        JSONObject json = new JSONObject();

        try {
            // Establecemos la latitud y longitud
            json.put("latitud", miPosicion.latitude);
            json.put("longitud", miPosicion.longitude);

            // Establecemos la empresa
            json.put("empresa", idEmpresa);
        }
        catch (Exception ex) { }

        // Realizamos la petición
        cliente.post(getActivity(), Helpers.URLApi("ofertasempresa"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try {
                    // Convertimos la respuesta en el array de bytes
                    ofertas = Oferta.ConsumirLista(Helpers.ResponseBodyToJSONArray(responseBody));

                    // Muestra las ofertas
                    ConfigurarOfertas();
                } catch (Exception ex) {
                    try {
                        // Mostramos el error producido
                        Helpers.MostrarError(getActivity(), Helpers.ResponseBodyToJSON(responseBody).getString("Error"));
                    } catch (Exception ex2) {
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Mostramos el error producido
                Helpers.MostrarError(getActivity(), "No se ha podido conseguir los datos de las ofertas");
            }
        });
    }

    /**
     * Muestra las ofertas y las configura
     */
    private void ConfigurarOfertas()
    {
        // Creamos el adaptador de las ofertas
        adaptador = new AdaptadorDinamico<OfertaViewHolder, Oferta>(getActivity(), R.layout.cupon_empresa, ofertas) {
            @Override
            public OfertaViewHolder onViewHolder(View view) {
                OfertaViewHolder holder = new OfertaViewHolder();
                holder.dia              = (NoctuaTextView) view.findViewById(R.id.empresa_dia);
                holder.mes              = (NoctuaTextView) view.findViewById(R.id.empresa_mes);
                holder.nombre           = (NoctuaTextView) view.findViewById(R.id.empresa_nombre);
                holder.empresa          = (NoctuaTextView) view.findViewById(R.id.empresa_empresa);
                holder.hora             = (NoctuaTextView) view.findViewById(R.id.empresa_hora);
                holder.kilometros       = (NoctuaTextView) view.findViewById(R.id.empresa_kilometros);
                holder.imagenoferta     = (ImageView) view.findViewById(R.id.empresa_imagenoferta);
                holder.imagenempresa    = (ImageView) view.findViewById(R.id.empresa_imagenempresa2);
                holder.transparente     = (LinearLayout) view.findViewById(R.id.empresa_lltransparente);

                return holder;
            }

            @Override
            public void onEntrada(OfertaViewHolder holder, Oferta oferta, View view, int posicion)
            {
                // Establecemos la fecha de inicio
                holder.dia.setText("" + oferta.INICIO.get(Calendar.DATE));

                // Establecemos el mes
                holder.mes.setText(Helpers.meses[oferta.INICIO.get(Calendar.MONTH)]);

                // Establecemos la descripción de la oferta (NOMBRE)
                holder.nombre.setText(oferta.NOMBRE);

                // Establecemos el nombre de la empresa
                holder.empresa.setText(oferta.EMPRESA);

                // Establecemos la hora de la oferta (INICIO)
                holder.hora.setText(String.format("%02d:%02d", oferta.INICIO.get(Calendar.HOUR_OF_DAY), oferta.INICIO.get(Calendar.MINUTE)));

                // Establecemos los kilómetros
                if (oferta.KILOMETROS > 1) holder.kilometros.setText(Math.round(oferta.KILOMETROS) + " kms.");
                else holder.kilometros.setText(Math.round(oferta.KILOMETROS * 1000) + " mts.");

                // Si tenemos una imagen para cargar de la oferta
                if (!oferta.IMAGEN.equals("") && !oferta.IMAGEN.equals("null")) {
                    // Cargamos la imagen de la oferta
                    ImageLoader.getInstance().displayImage(Helpers.URLImagenes("Ofertas/" + oferta.IMAGEN + ".jpg"),
                            holder.imagenoferta, options, new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    super.onLoadingComplete(imageUri, view, loadedImage);
                                }
                            }
                    );

                    // Indicamos el tipo de escala
                    holder.imagenoferta.setScaleType(ImageView.ScaleType.FIT_XY);
                } 
                else 
                {
                    // Establecemos la imagen por defecto
                   holder.imagenoferta.setImageResource( R.drawable.logobackground);

                    // Indicamos el tipo de escala
                    holder.imagenoferta.setScaleType(ImageView.ScaleType.CENTER);
                }

                // Si tenemos una imagen para cargar
                if (!oferta.LOGO.equals("") && !oferta.LOGO.equals("null")) 
                {
                    // Cargamos la imagen de la oferta
                    ImageLoader.getInstance().displayImage(Helpers.URLImagenes("Empresas/" + oferta.LOGO + "small.jpg"),
                            holder.imagenempresa, options, new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    super.onLoadingComplete(imageUri, view, loadedImage);

                                    // Establecemos la imagen
                                    ((ImageView) view).setImageBitmap(Helpers.MascaraImagen(getResources(), loadedImage, R.drawable.mascaracompleta));
                                }
                            }
                    );

                    // Indicamos el tipo de escala
                    holder.imagenempresa.setScaleType(ImageView.ScaleType.FIT_XY);
                } 
                else 
                {
                    // Establecemos la imagen por defecto
                    holder.imagenempresa.setImageBitmap(Helpers.MascaraImagen(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.logo), R.drawable.mascaracompleta));

                    // Indicamos el tipo de escala
                    holder.imagenempresa.setScaleType(ImageView.ScaleType.FIT_XY);
                }

                // Establecemos el tag de la oferta
                holder.transparente.setTag(oferta.ID);

                // Establecemos los eventos de selección de la oferta
                holder.transparente.setOnClickListener(clickOferta);
            }
        };

        // Establecemos el adaptador
        ((ListView) getActivity().findViewById(R.id.listaofertasempresa)).setAdapter(adaptador);
    }

    // Evento de selección de la oferta
    View.OnClickListener clickOferta = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Cargamos el fragmento de los datos de la oferta
            Helpers.LoadFragment(getActivity(), DatosOferta.newInstance(Integer.parseInt(view.getTag().toString())), "");
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        cliente.cancelRequests(getActivity(), true);
    }
}

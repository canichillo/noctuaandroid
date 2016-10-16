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
import android.util.TypedValue;
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


public class Ofertas extends Fragment
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

    // Indicamos que pestaña tenemos activada
    int tabOferta = 1;

    // Tipo filtrado
    String tipo = "";

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
        View view = inflater.inflate(R.layout.ofertas, null);

        // Establecemos el título de la ventana
        Helpers.SetTitulo(getActivity(), getString(R.string.ofertas));

        // Mostramos el menú
        Helpers.EstadoCabecera(getActivity(), true);

        // Establecemos el color
        Helpers.ColorCabecera(getActivity(), Helpers.getIntFromColor(63, 157, 217));

        // Establecemos el icono de la parte izquierda
        Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.menu, Helpers.MENU);
        // Establecemos el icono de la parte derecha
        Helpers.IconoDerechaTitulo(getActivity(), 0);

        // Ocultamos la sombra
        Helpers.EstadoSombra(getActivity(), false);

        // Activamos la selección del menú a toda la ventana
        Helpers.ChangeMenuMode(getActivity(), Helpers.IZQUIERDA);

        // Ignoramos el panel de las opciones
        Helpers.getResideMenu(getActivity()).addIgnoredView(view.findViewById(R.id.llOpciones));
        Helpers.getResideMenu(getActivity()).addIgnoredView(view.findViewById(R.id.horizontalScrollView));
        Helpers.getResideMenu(getActivity()).addIgnoredView(view.findViewById(R.id.llCategorias));

        // Inicializamos el cliente HTTP
        cliente = new ClienteREST(getActivity());

        view.findViewById(R.id.favoritos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Estamos en la pestaña de SEGUIDAS
                tabOferta = 1;

                // Mostramos las activas
                MostrarTiposOfertas();
            }
        });

        view.findViewById(R.id.activas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Estamos en la pestaña de ACTIVAS
                tabOferta = 2;

                // Mostramos las activas
                MostrarTiposOfertas();
            }
        });

        view.findViewById(R.id.todas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Estamos en la pestaña de TODAS
                tabOferta = 3;

                // Mostramos las activas
                MostrarTiposOfertas();
            }
        });

        view.findViewById(R.id.cercami).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Estamos en la pestaña de CERCA DE MI
                tabOferta = 4;

                // Mostramos las activas
                MostrarTiposOfertas();
            }
        });

        // Eventos de los botones de abajo
        view.findViewById(R.id.llBotonMapa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helpers.LoadFragment(getActivity(), MapaOfertas.newInstance("Normales", true), "MapaOfertas");
            }
        });

        view.findViewById(R.id.llBotonNoctuas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helpers.LoadFragment(getActivity(), MisCupones.newInstance(true), "MisCupones");
            }
        });

        view.findViewById(R.id.llBotonBares).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Desactivamos los demás botones
                getActivity().findViewById(R.id.pubs).setBackgroundResource(R.drawable.btnpubs);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombrepubs)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");
                getActivity().findViewById(R.id.bares).setBackgroundResource(R.drawable.btnbares);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombrebares)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");
                getActivity().findViewById(R.id.eventos).setBackgroundResource(R.drawable.btneventos);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombreeventos)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");
                getActivity().findViewById(R.id.comidas).setBackgroundResource(R.drawable.btncomida);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombrecomida)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");

                if (!tipo.equals("B")) {
                    // Indicamos que hemos seleccionado el bar
                    getActivity().findViewById(R.id.bares).setBackgroundResource(R.drawable.btnbaressel);
                    ((NoctuaTextView) getActivity().findViewById(R.id.nombrebares)).SetCustomFont("Fonts/HelveticaNeueMedium.ttf");

                    // Establecemos el tipo
                    tipo = "B";
                }
                else tipo = "";

                // Realizamos el filtrado de ofertas
                MostrarTiposOfertas();
            }
        });

        view.findViewById(R.id.llBotonPubs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Desactivamos los demás botones
                getActivity().findViewById(R.id.pubs).setBackgroundResource(R.drawable.btnpubs);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombrepubs)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");
                getActivity().findViewById(R.id.bares).setBackgroundResource(R.drawable.btnbares);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombrebares)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");
                getActivity().findViewById(R.id.eventos).setBackgroundResource(R.drawable.btneventos);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombreeventos)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");
                getActivity().findViewById(R.id.comidas).setBackgroundResource(R.drawable.btncomida);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombrecomida)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");

                if (!tipo.equals("P")) {
                    // Indicamos que hemos seleccionado el pub
                    getActivity().findViewById(R.id.pubs).setBackgroundResource(R.drawable.btnpubssel);
                    ((NoctuaTextView) getActivity().findViewById(R.id.nombrepubs)).SetCustomFont("Fonts/HelveticaNeueMedium.ttf");

                    // Establecemos el tipo
                    tipo = "P";
                }
                else tipo = "";

                // Realizamos el filtrado de ofertas
                MostrarTiposOfertas();
            }
        });

        view.findViewById(R.id.llBotonRestaurantes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Desactivamos los demás botones
                getActivity().findViewById(R.id.pubs).setBackgroundResource(R.drawable.btnpubs);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombrepubs)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");
                getActivity().findViewById(R.id.bares).setBackgroundResource(R.drawable.btnbares);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombrebares)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");
                getActivity().findViewById(R.id.eventos).setBackgroundResource(R.drawable.btneventos);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombreeventos)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");
                getActivity().findViewById(R.id.comidas).setBackgroundResource(R.drawable.btncomida);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombrecomida)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");

                if (!tipo.equals("R")) {
                    // Indicamos que hemos seleccionado el restaurante
                    getActivity().findViewById(R.id.comidas).setBackgroundResource(R.drawable.btncomidasel);
                    ((NoctuaTextView) getActivity().findViewById(R.id.nombrecomida)).SetCustomFont("Fonts/HelveticaNeueMedium.ttf");

                    // Establecemos el tipo
                    tipo = "R";
                }
                else tipo = "";

                // Realizamos el filtrado de ofertas
                MostrarTiposOfertas();
            }
        });

        view.findViewById(R.id.llBotonEventos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Desactivamos los demás botones
                getActivity().findViewById(R.id.pubs).setBackgroundResource(R.drawable.btnpubs);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombrepubs)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");
                getActivity().findViewById(R.id.bares).setBackgroundResource(R.drawable.btnbares);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombrebares)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");
                getActivity().findViewById(R.id.eventos).setBackgroundResource(R.drawable.btneventos);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombreeventos)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");
                getActivity().findViewById(R.id.comidas).setBackgroundResource(R.drawable.btncomida);
                ((NoctuaTextView) getActivity().findViewById(R.id.nombrecomida)).SetCustomFont("Fonts/HelveticaNeueLight.ttf");

                if (!tipo.equals("E")) {
                    // Indicamos que hemos seleccionado el evento
                    getActivity().findViewById(R.id.eventos).setBackgroundResource(R.drawable.btneventossel);
                    ((NoctuaTextView) getActivity().findViewById(R.id.nombreeventos)).SetCustomFont("Fonts/HelveticaNeueMedium.ttf");

                    // Establecemos el tipo
                    tipo = "E";
                }
                else tipo = "";

                // Realizamos el filtrado de ofertas
                MostrarTiposOfertas();
            }
        });

        // Devolvemos la vista
        return view;
    }

    /**
     * Refresca y muestra las pestañas
     */
    private void MostrarTiposOfertas()
    {
        // Según en que pestaña de ofertas estemos haremos una cosa u otra
        switch (tabOferta)
        {
            case 1:
                    // Marcamos el botón de favoritos
                    EstadoBoton(getActivity().findViewById(R.id.favoritos), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonblancoizquierda, Color.rgb(63, 157, 217));

                    // Desmarcamos los demás botones
                    EstadoBoton(getActivity().findViewById(R.id.activas), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonazulbordeblanco, Color.rgb(255, 255, 255));
                    EstadoBoton(getActivity().findViewById(R.id.todas), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonazulbordeblanco, Color.rgb(255, 255, 255));
                    EstadoBoton(getActivity().findViewById(R.id.cercami), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonazulderecha, Color.rgb(255, 255, 255));

                    // Mostramos las seguidas
                    MostrarSeguidas();
                   break;
            case 2:
                    // Marcamos el botón de activas
                    EstadoBoton(getActivity().findViewById(R.id.activas), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonblanco, Color.rgb(63, 157, 217));

                    // Desmarcamos los demás botones
                    EstadoBoton(getActivity().findViewById(R.id.favoritos), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonazulizquierda, Color.rgb(255, 255, 255));
                    EstadoBoton(getActivity().findViewById(R.id.todas), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonazulbordeblanco, Color.rgb(255, 255, 255));
                    EstadoBoton(getActivity().findViewById(R.id.cercami), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonazulderecha, Color.rgb(255, 255, 255));

                    // Mostramos las activas
                    MostrarActivas();
                    break;
            case 3:
                    // Marcamos el botón de todas
                    EstadoBoton(getActivity().findViewById(R.id.todas), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonblanco, Color.rgb(63, 157, 217));

                    // Desmarcamos los demás botones
                    EstadoBoton(getActivity().findViewById(R.id.favoritos), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonazulizquierda, Color.rgb(255, 255, 255));
                    EstadoBoton(getActivity().findViewById(R.id.activas), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonazulbordeblanco, Color.rgb(255, 255, 255));
                    EstadoBoton(getActivity().findViewById(R.id.cercami), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonazulderecha, Color.rgb(255, 255, 255));

                    // Mostramos todas las ofertas
                    MostrarTodas();
                    break;
            case 4:
                    // Marcamos el botón de cerca de mi
                    EstadoBoton(getActivity().findViewById(R.id.cercami), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonblancoderecha, Color.rgb(63, 157, 217));

                    // Desmarcamos los demás botones
                    EstadoBoton(getActivity().findViewById(R.id.favoritos), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonazulizquierda, Color.rgb(255, 255, 255));
                    EstadoBoton(getActivity().findViewById(R.id.todas), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonazulbordeblanco, Color.rgb(255, 255, 255));
                    EstadoBoton(getActivity().findViewById(R.id.activas), "Fonts/HelveticaNeueLight.ttf",
                                R.drawable.botonazulbordeblanco, Color.rgb(255, 255, 255));

                    // Mostramos las que están cercas de mí
                    MostrarCercaMi();
                    break;
        }
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

            // Mostramos los tipos de ofertas ya seleccionadas
            MostrarTiposOfertas();

            // Salimos de la función
            return;
        }

        // Creamos el objeto JSON de la petición
        JSONObject json = new JSONObject();

        try {
            // Establecemos la latitud y longitud
            json.put("latitud", miPosicion.latitude);
            json.put("longitud", miPosicion.longitude);

            // Establecemos el token
            json.put("token", Helpers.getTokenAcceso(getActivity()));

            // Establecemos la ciudad
            json.put("ciudad", Helpers.CIUDAD);
        }
        catch (Exception ex) { }

        // Realizamos la petición
        cliente.post(getActivity(), Helpers.URLApi("ofertas"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try {
                    Log.e("tag", new String(responseBody));
                    // Convertimos la respuesta en el array de bytes
                    ofertas = Oferta.ConsumirLista(Helpers.ResponseBodyToJSONArray(responseBody));

                    // Muestra las ofertas
                    ConfigurarOfertas();

                    // Según en que pestaña de ofertas estemos haremos una cosa u otra
                    MostrarTiposOfertas();
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
        adaptador = new AdaptadorDinamico<OfertaViewHolder, Oferta>(getActivity(), R.layout.cupon_item, ofertas) {
            @Override
            public OfertaViewHolder onViewHolder(View view) {
                OfertaViewHolder holder = new OfertaViewHolder();
                holder.dia              = (NoctuaTextView) view.findViewById(R.id.dia);
                holder.mes              = (NoctuaTextView) view.findViewById(R.id.mes);
                holder.nombre           = (NoctuaTextView) view.findViewById(R.id.nombre);
                holder.empresa          = (NoctuaTextView) view.findViewById(R.id.empresa);
                holder.hora             = (NoctuaTextView) view.findViewById(R.id.hora);
                holder.favorito         = (ImageView) view.findViewById(R.id.favorito);
                holder.kilometros       = (NoctuaTextView) view.findViewById(R.id.kilometros);
                holder.imagenoferta     = (ImageView) view.findViewById(R.id.imagenoferta);
                holder.imagenempresa    = (ImageView) view.findViewById(R.id.imagenempresa);
                holder.transparente     = (LinearLayout) view.findViewById(R.id.lltransparente);

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

                // Si no es seguida la empresa
                if (oferta.FAVORITO.equals("N")) holder.favorito.setVisibility(View.INVISIBLE);
                else holder.favorito.setVisibility(View.VISIBLE);

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
        ((ListView) getActivity().findViewById(R.id.lista)).setAdapter(adaptador);

        // Creamos la cabecera y el pie
        if (((ListView) getActivity().findViewById(R.id.lista)).getHeaderViewsCount() == 0) {
            ((ListView) getActivity().findViewById(R.id.lista)).addFooterView(Helpers.getSpacer(getActivity(), 76), null, false);
            ((ListView) getActivity().findViewById(R.id.lista)).addHeaderView(Helpers.getSpacer(getActivity(), 0), null, false);
        }
    }

    /**
     * Establece el estado de un botón
     * @param vista Botón
     * @param fuente Fuente del botón
     * @param background Background
     * @param color Color de la fuente
     */
    private void EstadoBoton(View vista, String fuente, int background, int color)
    {
        // Establecemos el background
        vista.setBackgroundResource(background);

        // Establecemos la fuente
        ((NoctuaButton) vista).SetCustomFont(fuente);

        // Establecemos el color de la fuente
        ((NoctuaButton) vista).setTextColor(color);
    }

    /**
     * Muestra todas las seguidas
     */
    private void MostrarSeguidas() {
        // Si no hay ofertas
        if (ofertas != null) {
            // Array de las seguidas
            ArrayList<Oferta> seguidas = new ArrayList<Oferta>();

            // Para cada una de las ofertas
            for (int i = 0; i < ofertas.size(); i++)
            {
                // Si es seguida
                if (ofertas.get(i).FAVORITO.equals("S"))
                {
                    // Si no tenemos filtro
                    if (tipo.equals("")) seguidas.add(ofertas.get(i));
                    else if (ofertas.get(i).TIPO.equals(tipo)) seguidas.add(ofertas.get(i));
                }
            }

            // Ordenamos las ofertas por la fecha
            Collections.sort(seguidas, new Comparator() {
                public int compare(Object o1, Object o2) {
                    Oferta p1 = (Oferta) o1;
                    Oferta p2 = (Oferta) o2;
                    return p1.INICIO.compareTo(p2.INICIO);
                }
            });

            // Actualizamos el adaptador
            adaptador.ActualizarElementos(seguidas);

            // Refrescamos los datos
            adaptador.notifyDataSetChanged();

            // Establecemos el estado del texto si está vacío
            MostrarTextoVacio("No estás siguiendo ninguna empresa");
        }
    }

    /**
     * Muestra todas las ofertas
     */
    private void MostrarTodas() {
        // Si no hay ofertas
        if (ofertas != null) {
            // Array de las activas
            ArrayList<Oferta> todas = new ArrayList<Oferta>();

            // Para cada una de las ofertas
            for (int i = 0; i < ofertas.size(); i++)
            {
                // Si no tenemos filtro
                if (tipo.equals("")) todas.add(ofertas.get(i));
                else if (ofertas.get(i).TIPO.equals(tipo)) todas.add(ofertas.get(i));
            }

            // Ordenamos las ofertas por la fecha
            Collections.sort(todas, new Comparator() {
                public int compare(Object o1, Object o2) {
                    Oferta p1 = (Oferta) o1;
                    Oferta p2 = (Oferta) o2;
                    return p1.INICIO.compareTo(p2.INICIO);
                }
            });

            // Actualizamos el adaptador
            adaptador.ActualizarElementos(todas);

            // Refrescamos los datos
            adaptador.notifyDataSetChanged();

            // Establecemos el estado del texto si está vacío
            MostrarTextoVacio("No hay ofertas disponibles");
        }
    }

    /**
     * Muestra todas las activas
     */
    private void MostrarActivas() {
        // Si no hay ofertas
        if (ofertas != null) {
            // Array de las activas
            ArrayList<Oferta> activas = new ArrayList<Oferta>();

            // Para cada una de las ofertas
            for (int i = 0; i < ofertas.size(); i++)
            {
                // Hora actual (INICIO)
                Calendar inicio = Calendar.getInstance();
                inicio.add(Calendar.HOUR_OF_DAY, 1);

                // Si están activas
                if (ofertas.get(i).INICIO.getTimeInMillis() <= inicio.getTimeInMillis() &&
                    ofertas.get(i).FIN.getTimeInMillis() >= Calendar.getInstance().getTimeInMillis())
                {
                    // Si no tenemos filtro
                    if (tipo.equals("")) activas.add(ofertas.get(i));
                    else if (ofertas.get(i).TIPO.equals(tipo)) activas.add(ofertas.get(i));
                }
            }

            // Actualizamos el adaptador
            adaptador.ActualizarElementos(activas);

            // Refrescamos los datos
            adaptador.notifyDataSetChanged();

            // Establecemos el estado del texto si está vacío
            MostrarTextoVacio("No hay ofertas activas");
        }
    }

    /**
     * Muestra las ofertas que están cercanas a uno
     */
    private void MostrarCercaMi()
    {
        // Si no hay ofertas
        if (ofertas != null) {
            // Array de las cercanas
            ArrayList<Oferta> cercanas = new ArrayList<Oferta>();

            // Para cada una de las ofertas
            for (int i = 0; i < ofertas.size(); i++)
            {
                // Si no tenemos filtro
                if (tipo.equals("")) cercanas.add(ofertas.get(i));
                else if (ofertas.get(i).TIPO.equals(tipo)) cercanas.add(ofertas.get(i));
            }

            // Ordenamos las ofertas por la fecha
            Collections.sort(cercanas, new Comparator() {
                public int compare(Object o1, Object o2) {
                    Oferta p1 = (Oferta) o1;
                    Oferta p2 = (Oferta) o2;
                    return Double.compare(p1.KILOMETROS, p2.KILOMETROS);
                }
            });

            // Actualizamos el adaptador
            adaptador.ActualizarElementos(cercanas);

            // Refrescamos los datos
            adaptador.notifyDataSetChanged();

            // Establecemos el estado del texto si está vacío
            MostrarTextoVacio("No hay ofertas cercanas a ti");
        }
    }

    /**
     * Muestra el texto vacío si no hay datos en la lista de ofertas
     * @param textovacio Texto a mostrar si la lista actual está vacía
     */
    private void MostrarTextoVacio(String textovacio)
    {
        // Si hay datos
        if (adaptador.getCount() != 0)
        {
            // Mostramos la lista
            getActivity().findViewById(R.id.lista).setVisibility(View.VISIBLE);

            // Ocultamos el texto vacío
            getActivity().findViewById(R.id.textovacio).setVisibility(View.GONE);
        }
        else
        {
            // Ocultamos la lista
            getActivity().findViewById(R.id.lista).setVisibility(View.GONE);

            // Mostramos el texto vacío
            getActivity().findViewById(R.id.textovacio).setVisibility(View.VISIBLE);

            // Establecemos el texto
            ((NoctuaTextView) getActivity().findViewById(R.id.textovacio)).setText(textovacio);
        }
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

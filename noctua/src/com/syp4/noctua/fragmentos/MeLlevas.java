package com.syp4.noctua.fragmentos;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.Segment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.R;
import com.syp4.noctua.holders.DireccionViewHolder;
import com.syp4.noctua.modelos.Direcciones;
import com.syp4.noctua.ui.NoctuaButton;
import com.syp4.noctua.ui.NoctuaTextView;
import com.syp4.noctua.utilidades.AdaptadorDinamico;
import com.syp4.noctua.utilidades.GPSTracker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MeLlevas extends Fragment implements RoutingListener
{
    // Nuestro mapa de google
    GoogleMap mapa;

    // Inicio de la ruta
    LatLng inicio;

    // Fin de la ruta
    LatLng fin;

    // Nuestro gestor de posición
    GPSTracker gps;

    // La vista creada
    private static View view;

    // Nombre de la empresa
    String empresa;

    // Nuestro adaptador dinámico
    AdaptadorDinamico<DireccionViewHolder, Direcciones> adaptador;

    /**
     * Constructor por defecto
     * @param fin Fin de la ruta
     */
    public static MeLlevas newInstance(LatLng fin, String empresa)
    {
        // Creamos el fragmento
        MeLlevas frag = new MeLlevas();

        frag.fin     = fin;
        frag.empresa = empresa;

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Elimina los problemas con los mapas (si volvemos hacia atrás)
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null){
                parent.removeView(view);
            }
        }

        // Cargamos la vista
        try {
            view = inflater.inflate(R.layout.mellevas, null);
        }
        catch (Exception ex) { }

        // Escondemos la sombra
        Helpers.EstadoSombra(getActivity(), false);

        // Obtenemos el mapa
        SupportMapFragment fm = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapamellevas);
        mapa = fm.getMap();

        // Establecemos el título de la ventana
        Helpers.SetTitulo(getActivity(), "¿Me llevas?");

        // Establecemos el icono de la parte izquierda
        Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.back, Helpers.ATRAS);

        // Establecemos el icono de la parte derecha
        Helpers.IconoDerechaTitulo(getActivity(), 0);

        // Desactivamos el menú
        Helpers.ChangeMenuMode(getActivity(), Helpers.NINGUNO);

        // Establecemos los eventos de los botones
        view.findViewById(R.id.ruta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cambiamos el estado del botón de la RUTA
                EstadoBoton(view, "Fonts/HelveticaNeueLight.ttf", R.drawable.botonblancoizquierda, Color.rgb(63, 157, 217));

                // Cambiamos el estado del otro botón
                EstadoBoton(getActivity().findViewById(R.id.indicaciones), "Fonts/HelveticaNeueLight.ttf", R.drawable.botonazulderecha, Color.rgb(255, 255, 255));

                // Mostramos el mapa
                getActivity().findViewById(R.id.mapamellevas).setVisibility(View.VISIBLE);

                // Ocultamos las indicaciones
                getActivity().findViewById(R.id.listaindicaciones).setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.indicaciones).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cambiamos el estado del botón de la INDICACIONES
                EstadoBoton(view, "Fonts/HelveticaNeueLight.ttf", R.drawable.botonblancoderecha, Color.rgb(63, 157, 217));

                // Cambiamos el estado del otro botón
                EstadoBoton(getActivity().findViewById(R.id.ruta), "Fonts/HelveticaNeueLight.ttf", R.drawable.botonazulizquierda, Color.rgb(255, 255, 255));

                // Mostramos el mapa
                getActivity().findViewById(R.id.mapamellevas).setVisibility(View.GONE);

                // Ocultamos las indicaciones
                getActivity().findViewById(R.id.listaindicaciones).setVisibility(View.VISIBLE);
            }
        });

        // Limpiamos los datos anteriores
        mapa.clear();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            // Por defecto, mostramos la ruta
            getActivity().findViewById(R.id.ruta).performClick();

            // Creamos el gestor de posiciones
            gps = new GPSTracker(getActivity());
            gps.setOnLocationChange(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    // Leemos el punto actual
                    inicio = new LatLng(location.getLatitude(), location.getLongitude());

                    // Leemos de nuevo la ruta
                    LeerRuta();
                }
            });

            // Si no tenemos activo el GPS
            if (!gps.canGetLocation()) {
                gps.showSettingsAlert(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        getActivity().startActivity(intent);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }

            // Establecemos el punto inicial como nuestra localización
            inicio = new LatLng(gps.getLatitude(), gps.getLongitude());

            // Actualizamos la posición de la cámara
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(inicio, 10);
            mapa.moveCamera(cameraUpdate);
        }
        catch (Exception ex) { }
    }

    /**
     * Lee la ruta
     */
    private void LeerRuta()
    {
        // Ruta a realizar
        Routing routing = new Routing(Routing.TravelMode.DRIVING, "es");
        routing.registerListener(this);
        routing.execute(inicio, fin);

        // Nuestros segmentos
        List<Segment> segmentos = null;

        try {
            // Leemos los datos de la ruta
            Route ruta = routing.get();
            ruta.setCountry("ES");

            // Obtenemos los segmentos
            segmentos = ruta.getSegments();
        }
        catch (Exception ex) { }

        // Lista de direcciones
        ArrayList<Direcciones> direcciones = new ArrayList<Direcciones>();

        // Si tenemos segmentos
        if (segmentos != null) {
            // Para cada uno de los segmentos
            for (int indice = 0; indice < segmentos.size(); indice++)
                direcciones.add(new Direcciones(indice + 1, segmentos.get(indice).getInstruction(), segmentos.get(indice).getLength()));
        }

        // Creamos el adaptador
        adaptador = new AdaptadorDinamico<DireccionViewHolder, Direcciones>(getActivity(), R.layout.direccion_item, direcciones) {
            @Override
            public DireccionViewHolder onViewHolder(View view) {
                DireccionViewHolder holder = new DireccionViewHolder();

                holder.orden     = (NoctuaTextView) view.findViewById(R.id.orden);
                holder.direccion = (NoctuaTextView) view.findViewById(R.id.direccion);

                return holder;
            }

            @Override
            public void onEntrada(DireccionViewHolder holder, Direcciones direccion, View view, int position) {
                // Establecemos el orden
                holder.orden.setText(direccion.orden + ".");

                // Establecemos la dirección
                holder.direccion.setText(direccion.direccion);
            }
        };

        // Obtenemos la lista de direcciones para establecerle el adaptador
        ((ListView) view.findViewById(R.id.listaindicaciones)).setAdapter(adaptador);

        // Establecemos los límites del mapa
        LatLngBounds limitesMapa = new LatLngBounds(inicio, fin);

        // Ajustamos los límites del mapa
        limitesMapa = Helpers.CalcularLimitesMapa(inicio, limitesMapa);
        limitesMapa = Helpers.CalcularLimitesMapa(fin, limitesMapa);

        // Actualizamos la posición de la cámara
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(limitesMapa, 70);
        mapa.animateCamera(cameraUpdate);
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingFailure() {
    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions) {
        PolylineOptions polyoptions = new PolylineOptions();
        polyoptions.color(Color.argb(130, 255, 0, 0));
        polyoptions.width(10);
        polyoptions.addAll(mPolyOptions.getPoints());
        mapa.addPolyline(polyoptions);

        // Marca de inicio
        MarkerOptions options = new MarkerOptions();
        options.position(inicio);
        try
        {
            options.icon(Helpers.ImagenMarcadorMapa(getActivity(), BitmapFactory.decodeFile(Helpers.ImagenFotoPerfil(getActivity())), true));
        }
        catch (Exception ex) { }
        options.title("tú");
        mapa.addMarker(options);

        // Marca de fin
        options = new MarkerOptions();
        options.position(fin);
        options.icon(Helpers.ImagenMarcadorMapa(getActivity(), BitmapFactory.decodeResource(getResources(), R.drawable.logo), true));
        options.title(empresa);
        mapa.addMarker(options);
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
}

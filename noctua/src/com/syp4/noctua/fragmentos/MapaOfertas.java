package com.syp4.noctua.fragmentos;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.R;
import com.syp4.noctua.utilidades.ClienteREST;
import com.syp4.noctua.utilidades.GPSTracker;
import com.syp4.noctua.utilidades.SecureImageDownloader;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.WeakHashMap;

public class MapaOfertas extends Fragment
{
    // Nuestro gestor de posición
    GPSTracker gps;

    // Tipo de mapa a cargar
    String TipoMapa;

    // Si debemos de mostrar el botón de atrás
    boolean atras;

    // Visualización de las ofertas en el mapa
    private GoogleMap map;

    // Nuestra posición actual
    LatLng actual;

    // Limites del mapa
    LatLngBounds limitesMapa;

    // Creamos el cliente para la petición HTTP
    ClienteREST cliente;

    // Listado de puntos y empresas
    HashMap<String, Integer> puntosEmpresas = new HashMap<String, Integer>();

    // La vista creada
    private static View view;

    /**
     * Constructor por defecto
     * @param tipo Tipo del mapa a cargar
     */
    public static MapaOfertas newInstance(String tipo, boolean atras)
    {
        // Creamos el fragmento
        MapaOfertas frag = new MapaOfertas();

        frag.TipoMapa = tipo;
        frag.atras    = atras;

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity()).imageDownloader(new SecureImageDownloader(getActivity(), 250, 250)).build();
        ImageLoader.getInstance().init(config);

        // Mantenemos la instancia
        setRetainInstance(true);
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
            view = inflater.inflate(R.layout.mapaofertas, null);
        }
        catch (Exception ex) { }

        // Establecemos el título de la ventana
        Helpers.SetTitulo(getActivity(), "Mapa");

        // Si debemos de mostrar el botón de atrás
        if (atras)
        {
            // Establecemos el icono de la parte derecha
            Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.back, Helpers.ATRAS);

            // Desactivamos el menú
            Helpers.ChangeMenuMode(getActivity(), Helpers.NINGUNO);
        }
        else
        {
            // Establecemos el icono de la parte izquierda
            Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.menu, Helpers.MENU);

            // Sólo mostramos el menú de la izquierda
            Helpers.ChangeMenuMode(getActivity(), Helpers.IZQUIERDA);
        }

        // Establecemos el icono de la parte derecha
        Helpers.IconoDerechaTitulo(getActivity(), 0);

        // Mostramos la sombra
        Helpers.EstadoSombra(getActivity(), true);

        // Devolvemos la vista
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Inicializamos el cliente HTTP
        cliente = new ClienteREST(getActivity());

        // Creamos el gestor de posiciones
        gps = new GPSTracker(getActivity());
        gps.setOnLocationChange(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                // Leemos el punto actual
                actual = new LatLng(location.getLatitude(), location.getLongitude());
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
        actual = new LatLng(gps.getLatitude(), gps.getLongitude());

        // Establecemos los límites del mapa
        limitesMapa = new LatLngBounds(new LatLng(actual.latitude - 0.01, actual.longitude - 0.01),
                                       new LatLng(actual.latitude + 0.01, actual.longitude + 0.01));

        // Iniciamos el mapa
        iniciarMapa();

        // Leemos los puntos de las ofertas
        LeerPuntosOfertas();
    }

    /**
     * Inicia el mapa
     */
    private void iniciarMapa()
    {
        if (map == null)
        {
            SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapaofertas);
            if (mapFragment != null) {
                map = mapFragment.getMap();
                if (map != null) {
                    UiSettings uiSettings = map.getUiSettings();
                    uiSettings.setAllGesturesEnabled(false);
                    uiSettings.setScrollGesturesEnabled(true);
                    uiSettings.setZoomGesturesEnabled(true);
                }
            }

            // Evento de la pulsación sobre el marcador
            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    try
                    {
                        // Datos JSON
                        JSONObject json = new JSONObject();

                        // Establecemos los datos de la empresa
                        json.put("empresa", puntosEmpresas.get(marker.getId()));

                        // Empresa
                        final int empresa = puntosEmpresas.get(marker.getId());

                        // Realizamos la petición para obtener los puntos de las ofertas
                        cliente.post(getActivity(), Helpers.URLApi("numeroofertasempresa"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                super.onSuccess(statusCode, headers, responseBody);

                                try
                                {
                                    // Obtenemos los datos
                                    JSONArray array = Helpers.ResponseBodyToJSONArray(responseBody);

                                    // Si sólo hay una oferta
                                    if (array.length() <= 1)
                                    {
                                        Helpers.LoadFragment(getActivity(), DatosOferta.newInstance(array.getJSONObject(0).optInt("oferta")), "DatosOferta" + array.getJSONObject(0).optInt("oferta"));
                                    }
                                    else Helpers.LoadFragment(getActivity(), OfertasEmpresa.newInstance(empresa), "OfertasEmpresa" + empresa);
                                }
                                catch (Exception ex)
                                {
                                    // Mostramos el error producido
                                    Helpers.MostrarError(getActivity(), "No se ha podido obtener las ofertas de la empresa");
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                super.onFailure(statusCode, headers, responseBody, error);

                                // Mostramos el error producido
                                Helpers.MostrarError(getActivity(), "No se ha podido obtener las ofertas de la empresa");
                            }
                        });
                    }
                    catch (Exception ex) { }
                }
            });
        }
    }

    /**
     * Lee los puntos de las ofertas
     */
    private void LeerPuntosOfertas()
    {
        // Limpiamos todos los puntos anteriores
        map.clear();

        try {
            // Creamos el marcador actual de la posición del usuario
            map.addMarker(Helpers.CrearMarcadorMapa(getActivity(), actual, "Tú", BitmapFactory.decodeFile(Helpers.ImagenFotoPerfil(getActivity())), true, true));
        }
        catch (Exception ex) { }

        // Datos JSON
        JSONObject json = new JSONObject();

        try {
            // Establecemos la ciudad que queremos obtener las ofertas
            json.put("ciudad", Helpers.CIUDAD);

            // Si queremos sólo los cupones
            if (TipoMapa.equals("Cupones")) json.put("token", Helpers.getTokenAcceso(getActivity()));

            // Si queremos las invitaciones
            if (TipoMapa.equals("Invitaciones")) json.put("token", Helpers.getTokenAcceso(getActivity()));
        }
        catch (Exception ex) { }

        // URL de la petición
        String url = "";

        // Si es normal
        if (TipoMapa.equals("Normales"))     url = Helpers.URLApi("ubicacionofertas");
        // Si son los cupones
        if (TipoMapa.equals("Cupones"))      url = Helpers.URLApi("ubicacioncupones");
        // Si son las invitaciones
        if (TipoMapa.equals("Invitaciones")) url = Helpers.URLApi("ubicacioninvitaciones");

        // Realizamos la petición para obtener los puntos de las ofertas
        cliente.post(getActivity(), url, Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) 
            {
                super.onSuccess(statusCode, headers, responseBody);

                // Obtenemos la lista de datos
                try
                {
                    // Convertimos la respuesta en el array de bytes
                    JSONArray arrayPuntos = Helpers.ResponseBodyToJSONArray(responseBody);

                    // Para cada uno de los puntos
                    for (int indice = 0; indice < arrayPuntos.length(); indice++) {
                        // Obtenemos el objeto a procesar
                        final JSONObject json = arrayPuntos.getJSONObject(indice);

                        // Obtenemos y refrescamos los límites del mapa
                        limitesMapa = Helpers.CalcularLimitesMapa(new LatLng(json.optDouble("latitud"), json.optDouble("longitud")), limitesMapa);

                        // Ubicación del pub o empresa
                        LatLng punto = new LatLng(json.optDouble("latitud"), json.optDouble("longitud"));

                        // Imagen
                        int imagenmarcador = 0;

                        // Si tiene imagen
                        if (json.optString("tipo").equals("B")) imagenmarcador = R.drawable.markerbeer;
                        if (json.optString("tipo").equals("P")) imagenmarcador = R.drawable.markerdancing;
                        if (json.optString("tipo").equals("R")) imagenmarcador = R.drawable.markerfood;
                        if (json.optString("tipo").equals("E")) imagenmarcador = R.drawable.markerdj;

                        // Añadimos el marcador
                        Marker marker = map.addMarker(Helpers.CrearMarcadorMapa(getActivity(), punto, json.optString("nombre"),
                                                                                BitmapFactory.decodeResource(getResources(), imagenmarcador), false, false));
                        puntosEmpresas.put(marker.getId(), json.optInt("empresa"));
                    }

                    // Actualizamos la posición de la cámara
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(limitesMapa, 100);
                    map.animateCamera(cameraUpdate);
                }
                catch (Exception ex) { ex.printStackTrace(); }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Mostramos el error producido
                Helpers.MostrarError(getActivity(), "No se ha podido conseguir la ubicación de las ofertas");
            }
        });
    }

    @Override
    public void onDestroy() {
        SupportMapFragment f = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapaofertas);
        if (f.isResumed()){
            getFragmentManager().beginTransaction().remove(f).commit();
        }

        super.onDestroy();
        cliente.cancelRequests(getActivity(), true);
    }
}

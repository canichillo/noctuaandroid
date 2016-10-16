package com.syp4.noctua.fragmentos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.ImagenesEmpresa;
import com.syp4.noctua.R;
import com.syp4.noctua.modelos.Empresa;
import com.syp4.noctua.ui.NoctuaButton;
import com.syp4.noctua.ui.NoctuaTextView;
import com.syp4.noctua.utilidades.ClienteREST;
import com.syp4.noctua.utilidades.SecureImageDownloader;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DatosEmpresa extends Fragment
{
    // ID de la empresa
    int idEmpresa;

    // Petición HTTP
    private ClienteREST cliente;

    // Ubicación de la empresa
    private LatLng posicionEmpresa;

    // La vista creada
    private static View view;

    // Datos de la empresa cargada
    Empresa datosEmpresa;

    // Nuestro mapa de google
    GoogleMap mapa;

    // Nuestra lista de imágenes
    ArrayList<String> galeria = new ArrayList<String>();

    // Posición de la galería
    int posicionGaleria = 0;

    public static DatosEmpresa newInstance(int id)
    {
        // Creamos el fragmento
        DatosEmpresa frag = new DatosEmpresa();

        frag.idEmpresa = id;

        // Mantenemos los datos
        frag.setRetainInstance(true);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Elimina los problemas con los mapas (si volvemos hacia atrás)
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null){
                parent.removeView(view);
            }
        }

        // Cargamos la vista
        try {
            view = inflater.inflate(R.layout.empresa, null);
        }
        catch (Exception ex) { }

        // Establecemos el título de la ventana
        Helpers.SetTitulo(getActivity(), "Empresa");

        // Establecemos el icono de la parte izquierda
        Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.back, Helpers.ATRAS);

        // Establecemos el icono de la parte derecha
        Helpers.IconoDerechaTitulo(getActivity(), 0);

        // Mostramos la sombra
        Helpers.EstadoSombra(getActivity(), true);

        // Desactivamos el menú
        Helpers.ChangeMenuMode(getActivity(), Helpers.NINGUNO);

        // Inicializamos el cliente asíncrono
        cliente = new ClienteREST(getActivity());

        // Establecemos el evento de mostrar los datos de los seguidores
		view.findViewById(R.id.empresa_btoseguidores).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Si hemos cargado la empresa
				if (idEmpresa != 0)
				{
					// Mostramos los datos de los seguidores
					Helpers.LoadFragment(getActivity(), Seguidores.newInstance(idEmpresa), "Seguidores" + idEmpresa);
				}
			}
            });

        // Establecemos el evento del botón de me llevas
        view.findViewById(R.id.empresa_mellevas).setOnClickListener(eventoMeLlevas);
        view.findViewById(R.id.empresa_direccionempresa).setOnClickListener(eventoMeLlevas);
        view.findViewById(R.id.empresa_poblacionempresa).setOnClickListener(eventoMeLlevas);
        view.findViewById(R.id.empresa_imgcoche).setOnClickListener(eventoMeLlevas);

        // Establecemos los eventos de adelante y atrás en las imágenes de la galería
        view.findViewById(R.id.empresa_atrasgaleria).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Si podemos ir hacia atrás
                if (galeria.size() != 0 && posicionGaleria > 0)
                {
                    // Decrementamos la posición
                    posicionGaleria--;

                    // Establecemos la imagen
                    ((ImageView) getActivity().findViewById(R.id.empresa_imagengaleria)).setImageBitmap(Helpers.LeerImagen(getActivity(), "temp", galeria.get(posicionGaleria) + "small.jpg"));
                }
            }
        });

        // Establecemos los eventos de adelante y atrás en las imágenes de la galería
        view.findViewById(R.id.empresa_adelantegaleria).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Si podemos ir hacia atrás
                if (galeria.size() != 0 && posicionGaleria < galeria.size() - 1)
                {
                    // Incrementamos la posición
                    posicionGaleria++;

                    // Establecemos la imagen
                    ((ImageView) getActivity().findViewById(R.id.empresa_imagengaleria)).setImageBitmap(Helpers.LeerImagen(getActivity(), "temp", galeria.get(posicionGaleria) + "small.jpg"));
                }
            }
        });

        // Si pulsamos sobre una imagen
        view.findViewById(R.id.empresa_imagengaleria).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Establecemos el valor de la empresa
                Helpers.setValor(getActivity(), "empresa", idEmpresa + "");

                // Establecemos el valor
                Helpers.setValor(getActivity(), "scroll", posicionGaleria + "");

                // Mostramos la galería de imágenes en grande
                Intent imagenesEmpresa = new Intent(getActivity(), ImagenesEmpresa.class);
                startActivity(imagenesEmpresa);
            }
        });

        // Establecemos el evento del seguimiento de la empresa
        view.findViewById(R.id.empresa_seguirempresa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Texto de la confirmación
                String textoConfirmacion = datosEmpresa.FAVORITO.equals("S") ? "¿Desea eliminar esta empresa de sus favoritas?" : "¿Desea agregar esta empresa a sus favoritas?";

                // URL de la petición
                final String urlPeticion = datosEmpresa.FAVORITO.equals("S") ? "noseguirempresa" : "seguirempresa";

                // Mostramos un mensaje de confirmación
                Helpers.MostrarConfirmacion(getActivity(), getString(R.string.confirmacion), textoConfirmacion, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Datos JSON
                        JSONObject json = new JSONObject();

                        try {
                            // Establecemos la ciudad actual
                            json.put("token", Helpers.getTokenAcceso(getActivity()));
                            json.put("empresa", idEmpresa);
                        } catch (Exception ex) { }

                        // Realizamos la petición para obtener adquirir la oferta
                        cliente.post(getActivity(), Helpers.URLApi(urlPeticion), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                super.onSuccess(statusCode, headers, responseBody);

                                try {
                                    // Obtenemos la respuesta
                                    JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                                    // Si está correcto
                                    if (respuesta.isNull("Error")) {
                                        // Cambiamos el estado
                                        datosEmpresa.FAVORITO = datosEmpresa.FAVORITO.equals("S") ? "N" : "S";

                                        // Mostramos el mensaje de que es correcto
                                        Helpers.MostrarMensaje(getActivity(), "Información", datosEmpresa.FAVORITO.equals("S") ? "Has agregado esta empresa a tus favoritas." : "Has eliminado esta empresa de tus favoritas.");

                                        // Mostramos el estado del seguimiento
                                        EstadoSeguimiento();
                                    } else
                                        Helpers.MostrarError(getActivity(), respuesta.getString("Error"));
                                } catch (Exception ex) {
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                super.onFailure(statusCode, headers, responseBody, error);

                                // Mostramos el error
                                Helpers.MostrarError(getActivity(), "No se ha podido realizar la petición. Inténtelo de nuevo.");
                            }
                        });
                    }
                }, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view) {
                    }
                });
            }
        });

        // Obtenemos el mapa
        SupportMapFragment fm = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapaempresa);
        mapa = fm.getMap();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity()).
            imageDownloader(new SecureImageDownloader(getActivity(), 250, 250)).build();
        ImageLoader.getInstance().init(config);

        // Devolvemos la vista
        return view;
    }

    View.OnClickListener eventoMeLlevas = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Mostramos la ventana de me llevas
            Helpers.LoadFragment(getActivity(), MeLlevas.newInstance(posicionEmpresa, datosEmpresa.NOMBRE), "MeLlevas" + idEmpresa);
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // Leemos los datos de la empresa
        LeerDatosEmpresa();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Lee los datos de la empresa
     */
    private void LeerDatosEmpresa()
    {
        // Si ya hemos leido los datos de la empresa
        if (datosEmpresa != null)
        {
            // Mostramos los datos
            MostrarDatos();

            // Leemos las imágenes de la empresa
            LeerImagenesEmpresa();

            // Salimos de la función
            return;
        }

        // Datos de la petición
        JSONObject json = new JSONObject();

        try {
            json.put("empresa", idEmpresa);
            json.put("token", Helpers.getTokenAcceso(getActivity()));
        }
        catch (Exception ex) { }

        // Realizamos la petición para obtener la información de la empresa
        cliente.post(getActivity(), Helpers.URLApi("datosempresa"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try
                {
                    // Obtenemos los datos de la empresa
                    datosEmpresa = Empresa.ConsumirObjeto(Helpers.ResponseBodyToJSON(responseBody));

                    // Ubicación de la empresa
                    posicionEmpresa = new LatLng(datosEmpresa.LATITUD, datosEmpresa.LONGITUD);

                    // Mostramos los datos
                    MostrarDatos();

                    // Mostramos el estado del seguimiento
                    EstadoSeguimiento();

                    // Leemos las imágenes de la empresa
                    LeerImagenesEmpresa();
                }
                catch (Exception ex) { ex.printStackTrace(); }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
            }
        });
    }

    /**
     * Mostramos los datos
     */
    private void MostrarDatos()
    {
        // Mostramos el botón de seguimiento de la empresa
        getActivity().findViewById(R.id.empresa_seguirempresa).setVisibility(View.VISIBLE);

        // Establecemos el nombre de la empresa
        ((NoctuaTextView) getActivity().findViewById(R.id.empresa_nombreempresa)).setText(datosEmpresa.NOMBRE);

        // Establecemos la dirección
        ((NoctuaTextView) getActivity().findViewById(R.id.empresa_direccionempresa)).setText(datosEmpresa.DIRECCION);

        // Establecemos la población
        ((NoctuaTextView) getActivity().findViewById(R.id.empresa_poblacionempresa)).setText(datosEmpresa.POBLACION);

        // Establecemos el twitter
        ((NoctuaTextView) getActivity().findViewById(R.id.empresa_twitterempresa)).setText(datosEmpresa.TWITTER);

        // Establecemos el email
        ((NoctuaTextView) getActivity().findViewById(R.id.empresa_mailempresa)).setText(datosEmpresa.EMAIL);

        // Establecemos los teléfonos
        ((NoctuaTextView) getActivity().findViewById(R.id.empresa_telefonoempresa)).setText(datosEmpresa.TELEFONO);

        // Establecemos la web
        ((NoctuaTextView) getActivity().findViewById(R.id.empresa_webempresa)).setText(datosEmpresa.WEB);

        // Establecemos el facebook
        ((NoctuaTextView) getActivity().findViewById(R.id.empresa_facebookempresa)).setText(datosEmpresa.FACEBOOK);

        // Establecemos la descripción
        ((NoctuaTextView) getActivity().findViewById(R.id.empresa_descripcionempresa)).setText(datosEmpresa.DESCRIPCION);

        // Establecemos el texto de los seguidores
        ((NoctuaButton) getActivity().findViewById(R.id.empresa_btoseguidores)).setText(datosEmpresa.SEGUIDORES + " seguidores");

        // Mostramos el botón
        getActivity().findViewById(R.id.empresa_btoseguidores).setVisibility(View.VISIBLE);

        // Si tenemos imagen
        if (!datosEmpresa.LOGO.equals("") && !datosEmpresa.LOGO.equals("null"))
        {
            // Si tenemos la imagen almacenada anteriormente
            if (Helpers.ExisteImagen(getActivity(), "temp", datosEmpresa.LOGO + ".jpg"))
            {
                // Establecemos la imagen
                ((ImageView) getActivity().findViewById(R.id.empresa_imagenempresa)).setImageBitmap(Helpers.MascaraImagen(getResources(),
                                                                                                    Helpers.LeerImagen(getActivity(), "temp", datosEmpresa.LOGO + ".jpg"),
                                                                                                    R.drawable.mascaracompleta));

                // Establecemos la ubicación de la empresa
                MostrarUbicacion();
            }
            else
            {
                // Cargamos la imagen
                ImageLoader.getInstance().loadImage(Helpers.URLImagenes("Empresas/" + datosEmpresa.LOGO + ".jpg"), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);

                        // Guardamos la imagen
                        Helpers.GuardarImagen(getActivity(), loadedImage, "temp", datosEmpresa.LOGO + ".jpg");

                        // Establecemos la imagen
                        ((ImageView) getActivity().findViewById(R.id.empresa_imagenempresa)).setImageBitmap(Helpers.MascaraImagen(getResources(), loadedImage, R.drawable.mascaracompleta));

                        // Establecemos la ubicación de la empresa
                        MostrarUbicacion();
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason
                            failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);

                        // Establecemos la imagen
                        ((ImageView) getActivity().findViewById(R.id.empresa_imagenempresa)).setImageBitmap(Helpers.MascaraImagen(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.logo), R.drawable.mascaracompleta));

                        // Establecemos la ubicación de la empresa
                        MostrarUbicacion();
                    }
                });
            }
        }
        else
        {
            // Establecemos la imagen
            ((ImageView) getActivity().findViewById(R.id.empresa_imagenempresa)).setImageBitmap(Helpers.MascaraImagen(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.logo), R.drawable.mascaracompleta));

            // Establecemos la ubicación de la empresa
            MostrarUbicacion();
        }
    }

    /**
     * Muestra la ubicación
     */
    private void MostrarUbicacion()
    {
        if (!datosEmpresa.LOGO.equals("") && !datosEmpresa.LOGO.equals("null")) {
            // Mostramos la ubicación de la empresa
            mapa.addMarker(Helpers.CrearMarcadorMapa(getActivity(), posicionEmpresa, "", Helpers.LeerImagen(getActivity(), "temp", datosEmpresa.LOGO + ".jpg"), true, true));
        }
        else mapa.addMarker(Helpers.CrearMarcadorMapa(getActivity(), posicionEmpresa, "", BitmapFactory.decodeResource(getResources(), R.drawable.logo), true, true));

        // Centramos el mapa
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(posicionEmpresa, 15);
        mapa.moveCamera(cameraUpdate);
    }

    /**
     * Lee las imágenes de la empresa
     */
    private void LeerImagenesEmpresa()
    {
        // Si ya hemos cargado las imágenes de la empresa
        if (galeria.size() != 0)
        {
            // Si hemos cargado los datos
            getActivity().findViewById(R.id.empresa_layoutdatos).setVisibility(View.VISIBLE);

            // Ocultamos el texto de carga
            getActivity().findViewById(R.id.empresa_cargando).setVisibility(View.GONE);

            // Mostramos la galería
            MostrarGaleria();

            // Salimos de la función
            return;
        }

        // Datos de la petición
        JSONObject json = new JSONObject();

        try {
            json.put("empresa", idEmpresa);
        }
        catch (Exception ex) { }

        // Realizamos la petición para obtener la información de la empresa
        cliente.post(getActivity(), Helpers.URLApi("imagenesempresasimple"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try {
                    // Si hemos cargado los datos
                    getActivity().findViewById(R.id.empresa_layoutdatos).setVisibility(View.VISIBLE);

                    // Ocultamos el texto de carga
                    getActivity().findViewById(R.id.empresa_cargando).setVisibility(View.GONE);

                    // Obtenemos los datos de las imágenes
                    JSONArray respuesta = Helpers.ResponseBodyToJSONArray(responseBody);

                    // Para cada una de las imágenes
                    for (int indice = 0; indice < respuesta.length(); indice++) {
                        galeria.add(respuesta.getJSONArray(indice).get(0).toString());
                    }

                    // Mostramos la galería
                    MostrarGaleria();
                }
                catch (Exception ex) { ex.printStackTrace(); }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Mostramos el mensaje de error
                Helpers.MostrarError(getActivity(), "No se ha podido obtener las imágenes de la empresa");
            }
        });
    }

    /**
     * Muestra las imágenes de la empresa
     */
    public void MostrarGaleria()
    {
        // Para cada una de las imágenes
        for (int indice = 0; indice < galeria.size(); indice++) {
            // Índice actual
            final int indiceImagen = indice;

            // Si es el primer indice
            if (indiceImagen == posicionGaleria) {
                try {
                    ((ImageView) getActivity().findViewById(R.id.empresa_imagengaleria)).setImageBitmap(Helpers.LeerImagen(getActivity(), "temp", galeria.get(indice) + "small.jpg"));
                }
                catch (Exception ex) { }
            }

            // Obtenemos la imagen
            ImageLoader.getInstance().loadImage(Helpers.URLImagenes("ImagenesEmpresa/" + galeria.get(indice) + "small.jpg"), new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);

                    // Guardamos la imagen
                    Helpers.GuardarImagen(getActivity(), loadedImage, "temp", galeria.get(indiceImagen) + "small.jpg");

                    // Si es el primer indice
                    if (indiceImagen == posicionGaleria) {
                        ((ImageView) getActivity().findViewById(R.id.empresa_imagengaleria)).setImageBitmap(loadedImage);
                    }
                }
            });
        }

        // Si hay datos
        if (galeria != null && galeria.size() != 0) {
            // Mostramos el layout con las imágenes
            getActivity().findViewById(R.id.llImagenesEmpresa).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy()
    {
        SupportMapFragment f = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapaempresa);
        if (f.isResumed()){
            getFragmentManager().beginTransaction().remove(f).commit();
        }

        super.onDestroy();
        cliente.cancelRequests(getActivity(), true);
    }

    @Override
    public void onDetach() {
        // Si tenemos imágenes cargadas
        if (galeria != null && galeria.size() != 0) {
            // Para cada una de las imágenes
            for (int indice = 0; indice < galeria.size(); indice++) {
                // Eliminamos la imagen almacenada
                Helpers.EliminarImagen(getActivity(), "temp", galeria.get(indice) + "small.jpg");
            }
        }

        super.onDetach();
    }

    /**
     * Mostramos el estado del seguimiento
     */
    private void EstadoSeguimiento()
    {
        // Si estamos siguiendo la empresa
        if (datosEmpresa.FAVORITO.equals("S"))
        {
            // Cambiamos el texto
            ((NoctuaButton) getActivity().findViewById(R.id.empresa_seguirempresa)).setText("Eliminar favorito");

            // Cambiamos el icono
            ((NoctuaButton) getActivity().findViewById(R.id.empresa_seguirempresa)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.estrella, 0, 0, 0);
        }
        else
        {
            // Cambiamos el texto
            ((NoctuaButton) getActivity().findViewById(R.id.empresa_seguirempresa)).setText("Agregar favorito");

            // Cambiamos el icono
            ((NoctuaButton) getActivity().findViewById(R.id.empresa_seguirempresa)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.estrellavacia, 0, 0, 0);
        }
    }
}

package com.syp4.noctua.fragmentos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.R;
import com.syp4.noctua.modelos.Oferta;
import com.syp4.noctua.ui.NoctuaButton;
import com.syp4.noctua.ui.NoctuaTextView;
import com.syp4.noctua.utilidades.ClienteREST;
import com.syp4.noctua.utilidades.SecureImageDownloader;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.Calendar;

public class DatosOferta extends Fragment
{
    // Datos de la oferta a cargar
    int idOferta;

    // Nuestra oferta cargada
    private Oferta oferta;

    // ID de la empresa
    int idEmpresa;

    // Estado de la adquisición
    boolean adquirida = false;

    // Ubicación de la oferta
    private LatLng ubicacionEmpresa;

    // Número de las descargas
    int numeroDescargas = 0;

    // Creamos el cliente para la petición HTTP
    ClienteREST cliente;

    public static DatosOferta newInstance(int id)
    {
        // Creamos el fragmento
        DatosOferta frag = new DatosOferta();

        // Establecemos el ID de la oferta a cargar
        frag.idOferta = id;

        // Mantenemos la instancia
        frag.setRetainInstance(true);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Cargamos la vista
        View view = inflater.inflate(R.layout.oferta, null);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity()).imageDownloader(new SecureImageDownloader(getActivity(), 250, 250)).build();
        ImageLoader.getInstance().init(config);

        // Establecemos el título de la ficha
        Helpers.SetTitulo(getActivity(), getString(R.string.oferta));

        // Cambiamos la imagen del botón de la cabecera, a la de atrás
        Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.back, Helpers.ATRAS);

        // Quitamos el icono de la parte derecha
        Helpers.IconoDerechaTitulo(getActivity(), 0);

        // Desactivamos el menú
        Helpers.ChangeMenuMode(getActivity(), Helpers.NINGUNO);

        // Mostramos la sombra
        Helpers.EstadoSombra(getActivity(), true);

        // Establecemos el evento sobre el botón de adquisición
        view.findViewById(R.id.btoadquisicion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Texto de la confirmación
                String textoConfirmacion = adquirida ? "¿Desea eliminar este Noctúa?" : "¿Desea descargar este Noctúa?";

                // URL de la petición
                final String urlPeticion = adquirida ? "noadquiriroferta" : "adquiriroferta";

                // Mostramos un mensaje de confirmación
                Helpers.MostrarConfirmacion(getActivity(), getString(R.string.confirmacion), textoConfirmacion, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Datos JSON
                        JSONObject json = new JSONObject();

                        try {
                            // Establecemos la ciudad actual
                            json.put("token", Helpers.getTokenAcceso(getActivity()));
                            json.put("oferta", idOferta);
                        } catch (Exception ex) {
                        }

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
                                        adquirida = !adquirida;

                                        // Mostramos el mensaje de que es correcto
                                        Helpers.MostrarMensaje(getActivity(), "Información", adquirida ? "Has descargado el Noctúa." : "Has eliminado el Noctúa.");

                                        // Cambiamos el estado del botón de adquisición
                                        CambiarEstadoAdquisicion();
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
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
        });

        // Establecemos el evento de mostrar los datos de la empresa
        view.findViewById(R.id.oferta_imagenempresa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				// Si hemos cargado la empresa
				if (idEmpresa != 0)
				{
            	    // Mostramos los datos de la empresa
                    Helpers.LoadFragment(getActivity(), DatosEmpresa.newInstance(idEmpresa), "Empresa" + idEmpresa);
				}
            }
        });

        // Establecemos el evento del botón de me llevas
        view.findViewById(R.id.oferta_mellevas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Mostramos la ventana de me llevas
                Helpers.LoadFragment(getActivity(), MeLlevas.newInstance(ubicacionEmpresa, oferta.NOMBRE), "MeLlevas" + idEmpresa);
            }
        });

        // Inicializamos el cliente HTTP
        cliente = new ClienteREST(getActivity());

        // Devolvemos la vista
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Si ya hemos cargado datos
        LeerDatosOferta();
    }

    /**
     * Lee los datos de la oferta
     */
    private void LeerDatosOferta()
    {
        // Si ya tenemos datos cargados
        if (oferta != null)
        {
            // Si hemos cargado los datos
            getActivity().findViewById(R.id.oferta_layoutdatos).setVisibility(View.VISIBLE);

            // Ocultamos el texto de carga
            getActivity().findViewById(R.id.oferta_cargando).setVisibility(View.GONE);

            // Refrescamos los datos
            RefrescarDatos();

            // Salimos de la función
            return;
        }

        // Datos JSON
        JSONObject json = new JSONObject();

        try {
            // Establecemos la ciudad actual
            json.put("ciudad", Helpers.CIUDAD);
            json.put("token", Helpers.getTokenAcceso(getActivity()));
            json.put("id", idOferta);
        }
        catch (Exception ex) { }

        // Realizamos la petición para obtener los datos de la oferta
        cliente.post(getActivity(), Helpers.URLApi("datosoferta"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try
                {
                    // Procesamos la respuesta
                    final JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                    // Si no hay ningún mensaje de error
                    if (respuesta.isNull("Error"))
                    {
                        // Si hemos cargado los datos
                        getActivity().findViewById(R.id.oferta_layoutdatos).setVisibility(View.VISIBLE);

                        // Ocultamos el texto de carga
                        getActivity().findViewById(R.id.oferta_cargando).setVisibility(View.GONE);

                        // Obtenemos los datos de la oferta
                        oferta = Oferta.ConsumirObjeto(respuesta);

                        // Comprobamos si podemos eliminar la oferta o no
                        oferta.ELIMINAR = respuesta.optInt("usados") == 0;

                        // ID de la empresa
                        idEmpresa = respuesta.optInt("idempresa");

                        // Ubicación de la empresa
                        ubicacionEmpresa = new LatLng(respuesta.optDouble("latitud"), respuesta.optDouble("longitud"));

                        // Refrescamos los datos
                        RefrescarDatos();

                        // Parámetros para la obtención de los seguidores
                        JSONObject json = new JSONObject();

                        try {
                            json.put("oferta", idOferta);
                        }
                        catch (Exception ex) {}

                        // Establecemos la consulta para obtener los seguidores
                        cliente.post(getActivity(), Helpers.URLApi("numerodescargasoferta"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                super.onSuccess(statusCode, headers, responseBody);

                                try {
                                    // Obtenemos la respuesta
                                    JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                                    // Guardamos el número de descargas
                                    numeroDescargas = respuesta.optInt("descargas");

                                    // Establecemos el texto de los seguidores
                                    ((NoctuaTextView) getActivity().findViewById(R.id.oferta_textoadquisicion)).setText(numeroDescargas + " descargas");
                                }
                                catch (Exception ex) { }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                super.onFailure(statusCode, headers, responseBody, error);

                                // Mostramos el error producido
                                Helpers.MostrarError(getActivity(), "No se ha podido obtener el número de descargas de la oferta");
                            }
                        });
                    }
                    else
                    {
                        // Mostramos el error producido
                        Helpers.MostrarError(getActivity(), respuesta.getString("Error"));
                    }
                } catch (Exception ex) { }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Mostramos el error producido
                Helpers.MostrarError(getActivity(), "No se ha podido obtener los datos de la oferta");
            }
        });
    }

    private void RefrescarDatos()
    {
        // Establecemos los valores de la oferta
        ((NoctuaTextView) getActivity().findViewById(R.id.oferta_nombreoferta)).setText(oferta.NOMBRE);
        ((NoctuaTextView) getActivity().findViewById(R.id.oferta_nombreempresa)).setText(oferta.EMPRESA);

        // Inicio
        Calendar inicio = Calendar.getInstance();
        inicio.add(Calendar.HOUR_OF_DAY, 1);

        // Si no está activa ocultamos el menú
        if (!(oferta.INICIO.getTimeInMillis() <= inicio.getTimeInMillis() && oferta.FIN.getTimeInMillis() >= Calendar.getInstance().getTimeInMillis()))
            getActivity().findViewById(R.id.btoadquisicion).setVisibility(View.GONE);

        // Descripción de la oferta
        ((NoctuaTextView) getActivity().findViewById(R.id.descripcionoferta)).setText(oferta.DESCRIPCION);

        // Establecemos el día de la oferta
        ((NoctuaTextView) getActivity().findViewById(R.id.oferta_diaoferta)).setText(oferta.INICIO.get(Calendar.DATE) + " de " + Helpers.meseslargo[oferta.INICIO.get(Calendar.MONTH)]);

        // Creamos las horas
        String horasOferta = String.format("%d:%02d %s", oferta.INICIO.get(Calendar.HOUR_OF_DAY),
                                                         oferta.INICIO.get(Calendar.MINUTE),
                                                         oferta.INICIO.get(Calendar.HOUR_OF_DAY) > 12 ? "PM" : "AM");

        // Fin de la hora
        horasOferta += " - ";
        horasOferta += String.format("%d:%02d %s", oferta.FIN.get(Calendar.HOUR_OF_DAY),
                                                   oferta.FIN.get(Calendar.MINUTE),
                                                   oferta.FIN.get(Calendar.HOUR_OF_DAY) > 12 ? "PM" : "AM");

        // Establecemos las horas
        ((NoctuaTextView) getActivity().findViewById(R.id.horas)).setText(horasOferta);

        // Si no debemos mostrar el botón de eliminar (Ha sido usada la oferta)
        if (oferta.ELIMINAR)
        {
            // Mostramos el botón de adquisición de la oferta
            getActivity().findViewById(R.id.btoadquisicion).setVisibility(View.VISIBLE);

            // Establecemos las opciones
            if (oferta.ADQUIRIDA.equals("S"))
            {
                // La hemos adquirido
                adquirida = true;

                // Cambiamos el estado del botón de adquisición
                CambiarEstadoAdquisicion();
            }
            else
            {
                // No la hemos adquirido
                adquirida = false;

                // Cambiamos el estado del botón de adquisición
                CambiarEstadoAdquisicion();
            }
        }
        else getActivity().findViewById(R.id.btoadquisicion).setVisibility(View.GONE);

        try {
            // Si tenemos una imagen para cargar
            if (!oferta.LOGO.equals("") && !oferta.LOGO.equals("null")) {
                // Si tenemos la imagen almacenada anteriormente
                if (Helpers.ExisteImagen(getActivity(), "temp", oferta.LOGO + ".jpg"))
                {
                    // Establecemos la imagen
                    ((ImageView) getActivity().findViewById(R.id.oferta_imagenempresa)).setImageBitmap(Helpers.MascaraImagen(getResources(),
                                                                                                                             Helpers.LeerImagen(getActivity(), "temp", oferta.LOGO + ".jpg"),
                                                                                                                             R.drawable.mascaracompleta));
                }
                else {
                    // Cargamos la imagen de la oferta
                    ImageLoader.getInstance().loadImage(Helpers.URLImagenes("Empresas/" + oferta.LOGO + ".jpg"), new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    super.onLoadingComplete(imageUri, view, loadedImage);

                                    // Establecemos la imagen
                                    ((ImageView) getActivity().findViewById(R.id.oferta_imagenempresa)).setImageBitmap(Helpers.MascaraImagen(getResources(), loadedImage, R.drawable.mascaracompleta));

                                    // Guardamos la imagen temporal
                                    Helpers.GuardarImagen(getActivity(), loadedImage, "temp", oferta.LOGO + ".jpg");
                                }

                                @Override
                                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                    super.onLoadingFailed(imageUri, view, failReason);

                                    // Establecemos la imagen
                                    ((ImageView) getActivity().findViewById(R.id.oferta_imagenempresa)).setImageBitmap(Helpers.MascaraImagen(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.logo), R.drawable.mascaracompleta));

                                    // Guardamos la imagen temporal
                                    Helpers.GuardarImagen(getActivity(), BitmapFactory.decodeResource(getResources(), R.drawable.logobackground), "temp", oferta.IMAGEN + ".jpg");
                                }
                            }
                    );
                }
            }
            else
            {
                // Establecemos la imagen
                ((ImageView) getActivity().findViewById(R.id.oferta_imagenempresa)).setImageBitmap(Helpers.MascaraImagen(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.logo), R.drawable.mascaracompleta));
            }
        }
        catch (Exception ex) { }

        try {
            // Si tenemos una imagen para cargar
            if (!oferta.IMAGEN.equals("") && !oferta.IMAGEN.equals("null")) {
                // Si tenemos la imagen almacenada anteriormente
                if (Helpers.ExisteImagen(getActivity(), "temp", oferta.IMAGEN + ".jpg")) {
                    // Establecemos la imagen
                    ((ImageView) getActivity().findViewById(R.id.oferta_imagenoferta)).setImageBitmap(Helpers.LeerImagen(getActivity(), "temp", oferta.IMAGEN + ".jpg"));
                }
                else {
                    // Cargamos la imagen de la oferta
                    ImageLoader.getInstance().loadImage(Helpers.URLImagenes("Ofertas/" + oferta.IMAGEN + ".jpg"), new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    super.onLoadingComplete(imageUri, view, loadedImage);

                                    try {
                                        // Establecemos la imagen
                                        ((ImageView) getActivity().findViewById(R.id.oferta_imagenoferta)).setImageBitmap(loadedImage);

                                        // Guardamos la imagen temporal
                                        Helpers.GuardarImagen(getActivity(), loadedImage, "temp", oferta.IMAGEN + ".jpg");
                                    }
                                    catch (Exception ex) { }
                                }

                                @Override
                                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                    super.onLoadingFailed(imageUri, view, failReason);

                                    try {
                                        // Establecemos la imagen por defecto
                                        ((ImageView) getActivity().findViewById(R.id.oferta_imagenoferta)).setImageResource(R.drawable.logobackground);

                                        // Guardamos la imagen temporal
                                        Helpers.GuardarImagen(getActivity(), BitmapFactory.decodeResource(getResources(), R.drawable.logobackground), "temp", oferta.IMAGEN + ".jpg");
                                    }
                                    catch (Exception ex) { }
                                }
                            }
                    );
                }
            }
            else
            {
                // Establecemos la imagen por defecto
                ((ImageView) getActivity().findViewById(R.id.oferta_imagenoferta)).setImageResource(R.drawable.logobackground);
            }
        }
        catch (Exception ex) { }

        // Establecemos el texto de los seguidores
        ((NoctuaTextView) getActivity().findViewById(R.id.oferta_textoadquisicion)).setText(numeroDescargas + " descargas");
    }

    /**
     * Cambia el estado del botón de adquisición
     */
    private void CambiarEstadoAdquisicion()
    {
        // Si está adquirida la oferta
        if (adquirida)
        {
            // Cambiamos el texto del botón
            ((NoctuaButton) getActivity().findViewById(R.id.btoadquisicion)).setText(" Eliminar Noctúa");

            // Cambiamos el icono
            ((NoctuaButton) getActivity().findViewById(R.id.btoadquisicion)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.eliminar, 0, 0, 0);
        }
        // Si no está adquirida
        else
        {
            // Cambiamos el texto del botón
            ((NoctuaButton) getActivity().findViewById(R.id.btoadquisicion)).setText(" Descargar Noctúa");

            // Cambiamos el icono
            ((NoctuaButton) getActivity().findViewById(R.id.btoadquisicion)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.descargar, 0, 0, 0);
        }
    }

    @Override
    public void onDetach() {
        if (oferta != null && oferta.IMAGEN != null) {
            // Eliminamos la imagen de la oferta
            Helpers.EliminarImagen(getActivity(), "temp", oferta.IMAGEN + ".jpg");

            // Eliminamos la imagen de la empresa
            Helpers.EliminarImagen(getActivity(), "temp", oferta.LOGO + "small.jpg");
        }

        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cliente.cancelRequests(getActivity(), true);
    }
}
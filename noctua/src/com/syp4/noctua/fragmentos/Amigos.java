package com.syp4.noctua.fragmentos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.R;
import com.syp4.noctua.holders.AmigoViewHolder;
import com.syp4.noctua.modelos.Amigo;
import com.syp4.noctua.ui.NoctuaTextView;
import com.syp4.noctua.utilidades.AdaptadorDinamico;
import com.syp4.noctua.utilidades.ClienteREST;
import com.syp4.noctua.utilidades.SecureImageDownloader;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

public class Amigos extends Fragment
{
    // La vista creada
    private static View view;

    // Nuestro adaptador dinámico
    AdaptadorDinamico<AmigoViewHolder, Amigo> adaptador;

    // Creamos el cliente para la petición HTTP
    ClienteREST cliente;

    // Opciones de la visualización de imágenes
    DisplayImageOptions options;

    // Lista de amigos
    ArrayList<Amigo> amigos;

    public static Amigos newInstance()
    {
        // Creamos el fragmento
        Amigos frag = new Amigos();

        // Mantenemos la instancia
        frag.setRetainInstance(true);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Cargamos la vista
        view = inflater.inflate(R.layout.amigos, null);

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

        ImageLoader.getInstance().init(config);

        // Establecemos el título de la ficha
        Helpers.SetTitulo(getActivity(), getString(R.string.amigos));

        // Cambiamos la imagen del botón de la cabecera, a la de atrás
        Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.menu, Helpers.MENU);

        // Quitamos el icono de la parte derecha
        Helpers.IconoDerechaTitulo(getActivity(), 0);

        // Activamos el menú
        Helpers.ChangeMenuMode(getActivity(), Helpers.IZQUIERDA);

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

        // Si ya hemos cargado datos
        LeerAmigos();
    }

    /**
     * Lee los datos de los amigos
     */
    private void LeerAmigos()
    {
        if (amigos != null)
        {
            // Configuramos la lista
            ConfigurarLista();

            // Salimos de la función
            return;
        }

        // Datos JSON
        JSONObject json = new JSONObject();

        try {
            // Establecemos la ciudad actual
            json.put("token", Helpers.getTokenAcceso(getActivity()));
        }
        catch (Exception ex) { }

        // Realizamos la petición para obtener los datos de los seguidores
        cliente.post(getActivity(), Helpers.URLApi("listaamigos"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try
                {
                    // Convertimos la respuesta en el array de bytes
                    amigos = Amigo.ConsumirLista(Helpers.ResponseBodyToJSONArray(responseBody));

                    // Configura la lista de amigos
                    ConfigurarLista();
                }
                catch (Exception ex)
                {
                    try
                    {
                        // Mostramos el error producido
                        Helpers.MostrarError(getActivity(), Helpers.ResponseBodyToJSON(responseBody).getString("Error"));
                    }
                    catch (Exception ex2) { }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Mostramos el error producido
                Helpers.MostrarError(getActivity(), "No se ha podido obtener los datos de tus amigos");
            }
        });
    }

    /**
     * Configura la lista de amigos
     */
    private void ConfigurarLista()
    {
        // Creamos el adaptador de los seguidores
        adaptador = new AdaptadorDinamico<AmigoViewHolder, Amigo>(getActivity(), R.layout.amigos_item, amigos) {
            @Override
            public AmigoViewHolder onViewHolder(View view)
            {
                AmigoViewHolder holder  = new AmigoViewHolder();
                holder.imagen           = (ImageView) view.findViewById(R.id.imagenamigo);
                holder.nombre           = (NoctuaTextView) view.findViewById(R.id.nombreamigo);
                holder.sistemaoperativo = (ImageView) view.findViewById(R.id.imagensoamigo);
                holder.dispositivo      = (NoctuaTextView) view.findViewById(R.id.dispositivoamigo);
                holder.estado           = (NoctuaTextView) view.findViewById(R.id.amigo_estado);
                holder.chat             = (ImageView) view.findViewById(R.id.amigo_chat);
                holder.amistad          = (ImageView) view.findViewById(R.id.amigo_botonestado);
                holder.ofertas          = (ImageView) view.findViewById(R.id.amigo_verofertas);

                // Establecemos el evento del chat
                view.findViewById(R.id.amigo_chat).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Creamos el chat
                        JSONObject jsonChat = new JSONObject();

                        try
                        {
                            // Establecemos la ciudad actual
                            jsonChat.put("token", Helpers.getTokenAcceso(getActivity()));
                            jsonChat.put("destinatario", Integer.parseInt(view.getTag().toString()));
                        }
                        catch (Exception ex) { }

                        // Realizamos la petición para obtener los datos de los seguidores
                        cliente.post(getActivity(), Helpers.URLApi("nuevochat"), Helpers.ToStringEntity(jsonChat), "application/json", new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                super.onSuccess(statusCode, headers, responseBody);

                                // Obtenemos la respuesta
                                JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                                // Si no hay error
                                if (respuesta.isNull("Error")) {
                                    // Accedemos a la ventana del chat
                                    Helpers.LoadFragment(getActivity(), RoomChat.newInstance(respuesta.optInt("id")), "Chat" + respuesta.optInt("id"));
                                } else
                                    Helpers.MostrarError(getActivity(), respuesta.optString("Error"));
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                super.onFailure(statusCode, headers, responseBody, error);
                            }
                        });
                    }
                });

                // Establecemos el evento del estado
                view.findViewById(R.id.amigo_botonestado).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Obtenemos el estado de la amistad
                        Amigo amigo = amigos.get(Integer.parseInt(view.getTag().toString()));

                        // Si somos amigos, debemos eliminar
                        if (amigo.ESTADO.equals("A")) eliminarAmistad(Integer.parseInt(view.getTag().toString()), amigo.AMISTAD);

                        // Si debes aceptar la amistad
                        if (amigo.ESTADO.equals("p")) aceptarAmistad(Integer.parseInt(view.getTag().toString()), amigo.AMISTAD);
                    }
                });

                // Establecemos el evento de las ofertas
                view.findViewById(R.id.amigo_verofertas).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Obtenemos el estado de la amistad
                        Amigo amigo = amigos.get(Integer.parseInt(view.getTag().toString()));

                        // Mostramos la ventana con las ofertas del amigo
                        Helpers.LoadFragment(getActivity(), OfertasAmigo.newInstance(amigo.USUARIO, amigo.NOMBRE), "OfertasAmigo" + amigo.USUARIO);
                    }
                });

                return holder;
            }

            @Override
            public void onEntrada(AmigoViewHolder holder, Amigo amigo, View view, int posicion)
            {
                // Establecemos el nombre
                holder.nombre.setText(amigo.NOMBRE);

                // Establecemos el sistema operativo
                holder.sistemaoperativo.setImageBitmap(BitmapFactory.decodeResource(getResources(), amigo.SO.equals("A") ? R.drawable.android : R.drawable.macos));

                // Establecemos el dispositivo
                holder.dispositivo.setText(amigo.DISPOSITIVO);

                // Establecemos el tag
                holder.amistad.setTag(posicion);
                holder.chat.setTag(amigo.USUARIO);
                holder.ofertas.setTag(posicion);

                // Si es amigo
                if (amigo.ESTADO.equals("A"))
                {
                    // Mostramos el icono
                    holder.amistad.setVisibility(View.VISIBLE);
                    holder.ofertas.setVisibility(View.VISIBLE);

                    // Cambiamos el icono
                    holder.amistad.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.deleteuser));

                    // Mostramos el texto
                    holder.estado.setText("Amigo");
                }

                // Si está pendiente (por su parte)
                if (amigo.ESTADO.equals("E"))
                {
                    // Ocultamos el icono
                    holder.amistad.setVisibility(View.GONE);
                    holder.ofertas.setVisibility(View.GONE);

                    // Mostramos el texto
                    holder.estado.setText("Pendiente de aceptar amistad");
                }

                // Si está pendiente (por tu parte)
                if (amigo.ESTADO.equals("T"))
                {
                    // Mostramos el icono
                    holder.amistad.setVisibility(View.VISIBLE);

                    // Ocultamos el icono
                    holder.ofertas.setVisibility(View.GONE);

                    // Cambiamos el icono
                    holder.amistad.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ok));

                    // Mostramos el texto
                    holder.estado.setText("Debes aceptar amistad");
                }

                // Si tenemos una imagen para cargar
                if (!amigo.IMAGEN.equals("") && !amigo.IMAGEN.equals("null"))
                {
                    // Cargamos la imagen del seguidor
                    ImageLoader.getInstance().displayImage(Helpers.URLImagenes("Usuarios/" + amigo.IMAGEN + ".jpg"), holder.imagen, options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);

                            // Establecemos la imagen
                            ((ImageView) view).setImageBitmap(Helpers.MascaraImagen(getResources(), loadedImage, R.drawable.mascaracompleta));
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            super.onLoadingFailed(imageUri, view, failReason);

                            // Establecemos la imagen
                            ((ImageView) view).setImageBitmap(Helpers.MascaraImagen(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.user), R.drawable.mascaracompleta));
                        }
                    });
                }
                else
                {
                    // Establecemos la imagen
                    holder.imagen.setImageBitmap(Helpers.MascaraImagen(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.user), R.drawable.mascaracompleta));
                }
            }
        };

        // Establecemos el adaptador
        ((ListView) getActivity().findViewById(R.id.listaamigos)).setAdapter(adaptador);

        if (((ListView) getActivity().findViewById(R.id.listaamigos)).getFooterViewsCount() == 0)
            ((ListView) getActivity().findViewById(R.id.listaamigos)).addHeaderView(Helpers.getSpacer(getActivity(), 3), null, false);
    }

    /**
     * Elimina una amistad
     * @param posicion  Posición
     * @param id ID de la amistad a borrar
     */
    private void eliminarAmistad(final int posicion, final int id)
    {
        // Mostramos el mensaje de confirmación
        Helpers.MostrarConfirmacion(getActivity(), "Confirmación", "¿Desea eliminar esta amistad?", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Petición para eliminar
                JSONObject json = new JSONObject();

                try
                {
                    // Establecemos los datos
                    json.put("token", Helpers.getTokenAcceso(getActivity()));
                    json.put("amistad", id);
                }
                catch (Exception ex) { }

                // Realizamos la petición para obtener eliminar la amistad
                cliente.post(getActivity(), Helpers.URLApi("eliminaramistad"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        super.onSuccess(statusCode, headers, responseBody);

                        // Obtenemos la respuesta
                        JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                        // Si no hay error
                        if (respuesta.isNull("Error"))
                        {
                            // Eliminamos el elemento
                            amigos.remove(posicion);

                            // Actualizamos los datos
                            adaptador.notifyDataSetChanged();
                        }
                        else Helpers.MostrarError(getActivity(), respuesta.optString("Error"));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);
                    }
                });
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    /**
     * Acepta una amistad
     * @param posicion  Posición
     * @param id ID de la amistad a aceptar
     */
    private void aceptarAmistad(final int posicion, final int id)
    {
        // Petición para eliminar
        JSONObject json = new JSONObject();

        try
        {
            // Establecemos los datos
            json.put("token", Helpers.getTokenAcceso(getActivity()));
            json.put("amistad", id);
        }
        catch (Exception ex) { }

        // Realizamos la petición para aceptar la amistad
        cliente.post(getActivity(), Helpers.URLApi("aceptaramistad"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                // Obtenemos la respuesta
                JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                // Si no hay error
                if (respuesta.isNull("Error"))
                {
                    // Indicamos que somos amigos
                    amigos.get(posicion).ESTADO = "A";

                    // Actualizamos los datos
                    adaptador.notifyDataSetChanged();
                }
                else Helpers.MostrarError(getActivity(), respuesta.optString("Error"));
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
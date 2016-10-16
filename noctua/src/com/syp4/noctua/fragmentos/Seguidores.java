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
import com.syp4.noctua.holders.SeguidoresViewHolder;
import com.syp4.noctua.modelos.Seguidor;
import com.syp4.noctua.ui.NoctuaTextView;
import com.syp4.noctua.utilidades.AdaptadorDinamico;
import com.syp4.noctua.utilidades.ClienteREST;
import com.syp4.noctua.utilidades.SecureImageDownloader;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

public class Seguidores extends Fragment
{
    // Datos de la oferta a cargar
    int idEmpresa;

    // La vista creada
    private static View view;

    // Nuestra lista de seguidores
    ArrayList<Seguidor> seguidores;

    // Nuestro adaptador
    AdaptadorDinamico<SeguidoresViewHolder, Seguidor> adaptador;

    // Creamos el cliente para la petición HTTP
    ClienteREST cliente;

    // Opciones de la visualización de imágenes
    DisplayImageOptions options;

    public static Seguidores newInstance(int id)
    {
        // Creamos el fragmento
        Seguidores frag = new Seguidores();

        // Establecemos el ID de la empresa a cargar los seguidores
        frag.idEmpresa = id;

        // Mantenemos la instancia
        frag.setRetainInstance(true);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Cargamos la vista
        view = inflater.inflate(R.layout.seguidores, null);

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
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();

        ImageLoader.getInstance().init(config);

        // Establecemos el título de la ficha
        Helpers.SetTitulo(getActivity(), getString(R.string.seguidores));

        // Cambiamos la imagen del botón de la cabecera, a la de atrás
        Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.back, Helpers.ATRAS);

        // Quitamos el icono de la parte derecha
        Helpers.IconoDerechaTitulo(getActivity(), 0);

        // Desactivamos el menú
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

        // Si ya hemos cargado datos
        LeerSeguidores();
    }

    /**
     * Lee los datos de los seguidores
     */
    private void LeerSeguidores()
    {
        // Si ya hay datos
        if (seguidores != null)
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
            json.put("ciudad", Helpers.CIUDAD);
            json.put("empresa", idEmpresa);
            json.put("token", Helpers.getTokenAcceso(getActivity()));
        }
        catch (Exception ex) { }

        // Realizamos la petición para obtener los datos de los seguidores
        cliente.post(getActivity(), Helpers.URLApi("seguidoresempresa"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try
                {
					// Convertimos la respuesta en el array de bytes
					seguidores = Seguidor.ConsumirLista(Helpers.ResponseBodyToJSONArray(responseBody));

                    // Configuramos la lista de seguidores
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
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) 
            {
                super.onFailure(statusCode, headers, responseBody, error);

                // Mostramos el error producido
                Helpers.MostrarError(getActivity(), "No se ha podido obtener los datos de los seguidores de la empresa");
            }
        });
    }

    /**
     * Configuramos la lista de datos de seguidores
     */
    private void ConfigurarLista()
    {
        // Creamos el adaptador de los seguidores
        adaptador = new AdaptadorDinamico<SeguidoresViewHolder, Seguidor>(getActivity(), R.layout.seguidores_item, seguidores) {
            @Override
            public SeguidoresViewHolder onViewHolder(View view)
            {
                SeguidoresViewHolder holder = new SeguidoresViewHolder();
                holder.imagen      = (ImageView) view.findViewById(R.id.imagenseguidor);
                holder.nombre      = (NoctuaTextView) view.findViewById(R.id.nombreseguidor);
                holder.dispositivo = (NoctuaTextView) view.findViewById(R.id.dispositivoseguidor);
                holder.so          = (ImageView) view.findViewById(R.id.imagensoseguidor);
                holder.estadoamigo = (NoctuaTextView) view.findViewById(R.id.amigoseguidor);
                holder.chat        = (ImageView) view.findViewById(R.id.seguidor_chat);
                holder.amistad     = (ImageView) view.findViewById(R.id.seguidor_amigo);

                // Establecemos el evento del chat
                view.findViewById(R.id.seguidor_chat).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Creamos el chat
                        JSONObject jsonChat = new JSONObject();

                        try {
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
                                if (respuesta.isNull("Error"))
                                {
                                    // Accedemos a la ventana del chat
                                    Helpers.LoadFragment(getActivity(), RoomChat.newInstance(respuesta.optInt("id")), "Chat" + respuesta.optInt("id"));
                                }
                                else Helpers.MostrarError(getActivity(), respuesta.optString("Error"));
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                super.onFailure(statusCode, headers, responseBody, error);
                            }
                        });
                    }
                });

                // Establecemos el evento de la amistad
                view.findViewById(R.id.seguidor_amigo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int amigoseleccionado = Integer.parseInt(view.getTag().toString());

                        // Creamos el chat
                        JSONObject jsonChat = new JSONObject();

                        try {
                            // Establecemos la ciudad actual
                            jsonChat.put("token", Helpers.getTokenAcceso(getActivity()));
                            jsonChat.put("solicitado", seguidores.get(amigoseleccionado).ID);
                        } catch (Exception ex) {
                        }

                        // Realizamos la petición para obtener los datos de los seguidores
                        cliente.post(getActivity(), Helpers.URLApi("solicitudamistad"), Helpers.ToStringEntity(jsonChat), "application/json", new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                super.onSuccess(statusCode, headers, responseBody);

                                // Obtenemos la respuesta
                                JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                                // Si no hay error
                                if (respuesta.isNull("Error"))
                                {
                                    // Actualizamos el estado
                                    seguidores.get(amigoseleccionado).AMIGO = "T";

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
                });

                return holder;
            }

            @Override
            public void onEntrada(SeguidoresViewHolder holder, Seguidor seguidor, View view, int posicion)
            {
                // Establecemos el nombre
                holder.nombre.setText(seguidor.NOMBRE);

                // Establecemos el sistema operativo
                holder.so.setImageBitmap(BitmapFactory.decodeResource(getResources(), seguidor.SO.equals("A") ? R.drawable.android : R.drawable.macos));

                // Establecemos el dispositivo
                holder.dispositivo.setText(seguidor.DISPOSITIVO);

                // Establecemos el tag
                holder.amistad.setTag(posicion);
                holder.chat.setTag(seguidor.ID);

                // Establecemos el tipo de la amistad
                if (seguidor.AMIGO.equals(""))
                {
                    holder.estadoamigo.setVisibility(View.GONE);

                    // Mostramos el botón de la amistad
                    holder.amistad.setVisibility(View.VISIBLE);
                }
                else
                {
                    // Ocultamos el botón de la amistad
                    holder.amistad.setVisibility(View.GONE);

                    // Mostramos el estado del amigo
                    holder.estadoamigo.setVisibility(View.VISIBLE);

                    // Si es amigo
                    if (seguidor.AMIGO.equals("A"))
                        holder.estadoamigo.setText("Amigo");
                    // Si está pendiente (por su parte)
                    if (seguidor.AMIGO.equals("E"))
                        holder.estadoamigo.setText("Pendiente de aceptar amistad");
                    // Si está pendiente (por tu parte)
                    if (seguidor.AMIGO.equals("T"))
                        holder.estadoamigo.setText("Debes aceptar amistad");
                }

                // Si tenemos una imagen para cargar
                if (!seguidor.IMAGEN.equals("") && !seguidor.IMAGEN.equals("null"))
                {
                    // Cargamos la imagen del seguidor
                    ImageLoader.getInstance().displayImage(Helpers.URLImagenes("Usuarios/" + seguidor.IMAGEN + ".jpg"), holder.imagen, options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
                        {
                            super.onLoadingComplete(imageUri, view, loadedImage);

                            // Establecemos la imagen
                            ((ImageView) view).setImageBitmap(Helpers.MascaraImagen(getResources(), loadedImage, R.drawable.mascaracompleta));
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason)
                        {
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
        ((ListView) getActivity().findViewById(R.id.listaseguidores)).setAdapter(adaptador);
        if (((ListView) getActivity().findViewById(R.id.listaseguidores)).getFooterViewsCount() == 0)
            ((ListView) getActivity().findViewById(R.id.listaseguidores)).addHeaderView(Helpers.getSpacer(getActivity(), 3), null, false);
    }

    @Override
    public void onDestroy() 
    {
        super.onDestroy();
        cliente.cancelRequests(getActivity(), true);
    }
}
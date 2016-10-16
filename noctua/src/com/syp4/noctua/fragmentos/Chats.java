package com.syp4.noctua.fragmentos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpResponseHandler;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.Principal;
import com.syp4.noctua.R;
import com.syp4.noctua.holders.ChatViewHolder;
import com.syp4.noctua.modelos.Chat;
import com.syp4.noctua.ui.NoctuaTextView;
import com.syp4.noctua.utilidades.AdaptadorDinamico;
import com.syp4.noctua.utilidades.ClienteREST;
import com.syp4.noctua.utilidades.SecureImageDownloader;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class Chats extends Fragment
{
    // La vista creada
    private static View view;

    // Nuestro adaptador dinámico
    AdaptadorDinamico<ChatViewHolder, Chat> adaptador;

    // Creamos el cliente para la petición HTTP
    ClienteREST cliente;

    // Opciones de la visualización de imágenes
    DisplayImageOptions options;

    public static Chats newInstance()
    {
        // Creamos el fragmento
        Chats frag = new Chats();

        // Mantenemos la instancia
        frag.setRetainInstance(true);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Cargamos la vista
        view = inflater.inflate(R.layout.chats, null);

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

        ImageLoader.getInstance().init(config);

        // Establecemos el título de la ficha
        Helpers.SetTitulo(getActivity(), "Chats");

        // Cambiamos la imagen del botón de la cabecera, a la de atrás
        Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.menu, Helpers.MENU);

        // Quitamos el icono de la parte derecha
        Helpers.IconoDerechaTitulo(getActivity(), 0);

        // Desactivamos el menú
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
        LeerChats();
    }

    /**
     * Lee los datos de los chats
     */
    private void LeerChats()
    {
        // Datos JSON
        JSONObject json = new JSONObject();

        try
        {
            // Establecemos la ciudad actual
            json.put("token", Helpers.getTokenAcceso(getActivity()));

            // Establecemos los datos de la última vez que recogimos los chats
            json.put("fecha", Helpers.getValor(getActivity(), "fechachats", "2014-01-01 00:00:00"));
        }
        catch (Exception ex) { }

        // Realizamos la petición para obtener los datos de los seguidores
        cliente.post(getActivity(), Helpers.URLApi("listachats"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try
                {
					// Convertimos la respuesta en el array de bytes
                    JSONArray array = Helpers.ResponseBodyToJSONArray(responseBody);

                    // Para cada uno de los nuevos chats
                    for (int indice = 0; indice < array.length(); indice++)
                    {
                        // Comprobamos si existe el chat
                        if (!((Principal) getActivity()).getDb().ExisteChatBD( array.getJSONObject(indice).optInt("destinatario"))) {
                            // Insertamos el elemento en la base de datos de chats
                            ((Principal) getActivity()).getDb().crearChat(new Chat(array.getJSONObject(indice).optInt("id"),
                                    array.getJSONObject(indice).optString("nombre"),
                                    array.getJSONObject(indice).optString("imagen"),
                                    array.getJSONObject(indice).optString("so"),
                                    array.getJSONObject(indice).optString("dispositivo"),
                                    Helpers.StrToCalendar("2014-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss"),
                                    array.getJSONObject(indice).optInt("destinatario")));
                        }
                    }

                    // No hay chats disponibles
                    if (array.length() == 0) return;

                    // Como hemos recogido datos, guardamos la fecha de la última recogida
                    Helpers.setValor(getActivity(), "fechachats", Helpers.CalendarToStr(Calendar.getInstance()));

                    // Obtenemos la lista
                    ArrayList<Chat> chats = ((Principal) getActivity()).getDb().ChatsBD();

                    // Actualizamos los datos del chat
                    adaptador.ActualizarElementos(chats);
                    adaptador.notifyDataSetChanged();
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
                Helpers.MostrarError(getActivity(), "No se ha podido obtener los datos de los chats");
            }
        });

        // Obtenemos la lista
        ArrayList<Chat> chats = ((Principal) getActivity()).getDb().ChatsBD();

        // Creamos el adaptador de los seguidores
        adaptador = new AdaptadorDinamico<ChatViewHolder, Chat>(getActivity(), R.layout.chats_item, chats) {
            @Override
            public ChatViewHolder onViewHolder(View view)
            {
                ChatViewHolder holder = new ChatViewHolder();
                holder.imagen      = (ImageView) view.findViewById(R.id.chat_imagen);
                holder.nombre      = (NoctuaTextView) view.findViewById(R.id.chat_nombre);
                holder.dispositivo = (NoctuaTextView) view.findViewById(R.id.chat_dispositivo);
                holder.so          = (ImageView) view.findViewById(R.id.chat_imagenso);
                return holder;
            }

            @Override
            public void onEntrada(ChatViewHolder holder, final Chat chat, View view, int posicion)
            {
                // Establecemos el nombre
                holder.nombre.setText(chat.NOMBRE);

                // Establecemos el sistema operativo
                holder.so.setImageBitmap(BitmapFactory.decodeResource(getResources(), chat.SO.equals("A") ? R.drawable.android : R.drawable.macos));

                // Establecemos el dispositivo
                holder.dispositivo.setText(chat.DISPOSITIVO);

                // Si tenemos una imagen para cargar
                if (!chat.IMAGEN.equals("") && !chat.IMAGEN.equals("null"))
                {
                    // Cargamos la imagen del seguidor
                    ImageLoader.getInstance().displayImage(Helpers.URLImagenes("Usuarios/" + chat.IMAGEN + ".jpg"), holder.imagen, options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
                        {
                            super.onLoadingComplete(imageUri, view, loadedImage);

                            // Establecemos la imagen
                            ((ImageView) view).setImageBitmap(Helpers.MascaraImagen(getResources(), loadedImage, R.drawable.mascaracompleta));

                            // Guardamos la imagen
                            Helpers.GuardarImagen(getActivity(), loadedImage, "chats/images", chat.IMAGEN + ".jpg");
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
        ((ListView) getActivity().findViewById(R.id.listachats)).setAdapter(adaptador);

        // Establecemos el evento de seleccionar el chat
        ((ListView) getActivity().findViewById(R.id.listachats)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Cargamos el chat
                Helpers.LoadFragment(getActivity(), RoomChat.newInstance(((Chat) adapterView.getItemAtPosition(i)).ID), "Chat" + ((Chat) adapterView.getItemAtPosition(i)).ID);
            }
        });

        // Establecemos el evento de eliminación del chat
        ((ListView) getActivity().findViewById(R.id.listachats)).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Chat chat = (Chat) adapterView.getItemAtPosition(i);

                // Realizamos la pregunta
                Helpers.MostrarConfirmacion(getActivity(), "Confirmación", "¿Desea eliminar el chat?", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Eliminamos el chat
                        ((Principal) getActivity()).getDb().eliminarChat(chat.ID);

                        // Eliminamos la imagen
                        Helpers.EliminarImagen(getActivity(), "chats/images", chat.IMAGEN + ".jpg");

                        // Eliminamos los datos de la web
                        JSONObject json = new JSONObject();

                        try
                        {
                            // Establecemos el token del usuario
                            json.put("token", Helpers.getTokenAcceso(getActivity()));

                            // Establecemos el ID del chat
                            json.put("chat", chat.ID);
                        }
                        catch (Exception ex) { }

                        // Realizamos la petición para obtener los datos de los seguidores
                        cliente.post(getActivity(), Helpers.URLApi("eliminarchat"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                super.onSuccess(statusCode, headers, responseBody);

                                // Obtenemos la respuesta
                                JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                                // Si hay error
                                if (!respuesta.isNull("Error")) {
                                    // Mostramos el error producido
                                    Helpers.MostrarError(getActivity(), respuesta.optString("Error"));
                                }
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

                return true;
            }
        });
    }

    @Override
    public void onDestroy() 
    {
        super.onDestroy();
        cliente.cancelRequests(getActivity(), true);
    }
}
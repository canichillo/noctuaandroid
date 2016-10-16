package com.syp4.noctua.fragmentos;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.Principal;
import com.syp4.noctua.R;
import com.syp4.noctua.holders.LineaChatViewHolder;
import com.syp4.noctua.modelos.Chat;
import com.syp4.noctua.modelos.LineaChat;
import com.syp4.noctua.ui.NoctuaText;
import com.syp4.noctua.ui.NoctuaTextView;
import com.syp4.noctua.utilidades.AdaptadorDinamico;
import com.syp4.noctua.utilidades.ClienteREST;
import com.syp4.noctua.utilidades.SecureImageDownloader;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class RoomChat extends Fragment
{
    // ID del chat
    int id;

    // Datos del chat actual
    Chat datosChat;

    // Si ya hemos leido la fecha
    boolean leidoFecha = false;

    // Nuestro adaptador dinámico
    AdaptadorDinamico<LineaChatViewHolder, LineaChat> adaptador;

    // Realizamos el proceso de peticion
    ClienteREST cliente;

    // Temporizador de los chats
    Timer t;

    public static RoomChat newInstance(int id)
    {
        // Creamos el fragmento
        RoomChat frag = new RoomChat();

        // Establecemos el ID del chat
        frag.id = id;

        // Mantenemos la instancia
        frag.setRetainInstance(true);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
		// Cargamos la vista
        View view = inflater.inflate(R.layout.chat, null);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity()).imageDownloader(new SecureImageDownloader(getActivity(), 250, 250)).build();
        ImageLoader.getInstance().init(config);

        // Establecemos el título de la ficha
        Helpers.SetTitulo(getActivity(), "Chat");

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

        // Inicializamos el temporizador
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Volvemos a leer los mensajes
                        LeerChat();
                    }
                });
            }
        }, 0, 20000);  // Cada 20 segundos

        ((NoctuaText) view.findViewById(R.id.chat_textomensaje)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                NoctuaText tv = ((NoctuaText) getActivity().findViewById(R.id.chat_textomensaje));
                int height_in_pixels = tv.getLineCount() * tv.getLineHeight();
                height_in_pixels += tv.getPaddingTop() + tv.getPaddingBottom();
                if (height_in_pixels < Helpers.ConvertDpToPixels(getResources(), 87)) tv.setHeight(height_in_pixels);
                else tv.setHeight(Helpers.ConvertDpToPixels(getResources(), 87));
                tv.invalidate();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Establecemos el evento de enviar mensaje al chat
        view.findViewById(R.id.chat_botonenviar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtenemos el texto del mensaje
                final String texto = ((NoctuaText) getActivity().findViewById(R.id.chat_textomensaje)).getText().toString();

                // Si hemos establecido algún texto
                if (!texto.trim().isEmpty())
                {
                    // Petición para crear un nuevo mensaje
                    JSONObject json = new JSONObject();

                    try
                    {
                        // Establecemos el tipo
                        json.put("tipo", "T");

                        // Establecemos el token
                        json.put("token", Helpers.getTokenAcceso(getActivity()));

                        // Establecemos el chat
                        json.put("chat", id);

                        // Establecemos el texto
                        json.put("texto", texto.trim());
                    }
                    catch (Exception ex) { }

                    // Realizamos la petición
                    cliente.post(getActivity(), Helpers.URLApi("nuevomensajechat"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() 
                    {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            super.onSuccess(statusCode, headers, responseBody);

                            try
                            {
                                // Obtenemos la respuesta
                                JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                                // Si no ha habido ningún error
                                if (respuesta.isNull("Error"))
                                {
                                    // Guardamos el mensaje
                                    ((Principal) getActivity()).getDb().crearMensajeChat(id, texto.trim(), "P", "T");

                                    // Limpiamos el texto del mensaje
                                    ((NoctuaText) getActivity().findViewById(R.id.chat_textomensaje)).setText("");
                                    getActivity().findViewById(R.id.chat_textomensaje).clearFocus();
                                    Helpers.hideSoftKeyboard(getActivity());

                                    // Obtenemos la lista
                                    ArrayList<LineaChat> lineas = ((Principal) getActivity()).getDb().LineasChatBD(id);

                                    // Refrescamos el adaptador
                                    adaptador.ActualizarElementos(lineas);

                                    // Indicamos que debemos actualizar
                                    adaptador.notifyDataSetChanged();

                                    // Nuestra lista
                                    ListView lista = ((ListView) getActivity().findViewById(R.id.listachat));

                                    // Nos posicionamos al final
                                    lista.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                                    lista.smoothScrollToPosition(adaptador.getCount());
                                    lista.setStackFromBottom(true);
                                }
                                else
                                {
                                    // Mostramos el error producido
                                    Helpers.MostrarError(getActivity(), respuesta.optString("Error"));
                                }
                            }
                            catch (Exception ex) { }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            super.onFailure(statusCode, headers, responseBody, error);

                            // Mostramos el error producido
                            Helpers.MostrarError(getActivity(), "No se ha podido enviar el mensaje inténtelo de nuevo");
                        }
                    });
                }
            }
        });

        // Devolvemos la vista
        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        // Si no hemos leido los datos del chat
        if (datosChat == null)
        {
            // Obtenemos los datos del chat
            datosChat = ((Principal) getActivity()).getDb().ChatBD(id);

            // Si no tenemos datos
            if (datosChat.ID == 0)
            {
                // Solicitamos los nuevos datos del chat
                JSONObject json = new JSONObject();

                try
                {
                    // Establecemos el token
                    json.put("token", Helpers.getTokenAcceso(getActivity()));

                    // Establecemos el chat
                    json.put("chat", id);
                }
                catch (Exception ex) { }

                // Realizamos la petición
                cliente.post(getActivity(), Helpers.URLApi("datoschat"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        super.onSuccess(statusCode, headers, responseBody);

                        // Si hemos recibido bien los datos
                        try
                        {
                            // Obtenemos la respuesta
                            JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                            // Si hay algún error
                            if (respuesta.isNull("Error"))
                            {
                                // Creamos los datos del chat
                                datosChat = new Chat(respuesta.optInt("id"), respuesta.optString("nombre"), respuesta.optString("imagen"),
                                        respuesta.optString("so"), respuesta.optString("dispositivo"), Helpers.StrToCalendar("2014-01-01", "yyyy-MM-dd"),
                                        respuesta.optInt("destinatario"));

                                // Guardamos los datos en la base de datos
                                ((Principal) getActivity()).getDb().crearChat(datosChat);

                                // Establecemos el título de la ficha
                                Helpers.SetTitulo(getActivity(), datosChat.NOMBRE, 18);

                                // Leemos la imagen
                                ImageLoader.getInstance().loadImage(Helpers.URLImagenes("Usuarios/" + datosChat.IMAGEN + ".jpg"), new SimpleImageLoadingListener(){
                                    @Override
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                        super.onLoadingComplete(imageUri, view, loadedImage);

                                        // Guardamos la imagen
                                        Helpers.GuardarImagen(getActivity(), loadedImage, "chats/images", datosChat.IMAGEN + ".jpg");

                                        // Establecemos el icono
                                        Helpers.IconoDerechaTitulo(getActivity(), Helpers.MascaraImagen(getResources(), Helpers.LeerImagen(getActivity(), "chats/images", datosChat.IMAGEN + ".jpg"), R.drawable.mascaracompleta));
                                    }
                                });

                                // Leemos de nuevo el chat
                                LeerChat();
                            }
                            else
                            {
                                // Mostramos el error producido
                                Helpers.MostrarError(getActivity(), respuesta.optString("Error"));
                            }
                        }
                        catch (Exception ex) { }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);
                    }
                });

                // Salimos de la función
                return;
            }
        }

        // Establecemos el título de la ficha
        Helpers.SetTitulo(getActivity(), datosChat.NOMBRE, 18);

        // Si existe la imagen
        if (Helpers.ExisteImagen(getActivity(), "chats/images", datosChat.IMAGEN + ".jpg")) {
            // Establecemos la imagen de la derecha
            Helpers.IconoDerechaTitulo(getActivity(), Helpers.MascaraImagen(getResources(), Helpers.LeerImagen(getActivity(), "chats/images", datosChat.IMAGEN + ".jpg"), R.drawable.mascaracompleta));
        }
    }

    /**
     * Lee los datos del chat
      */
    private void LeerChat()
    {
        // Si no tenemos datos
        if (datosChat.ID == 0) return;

        // Solicitamos los nuevos datos del chat
        final JSONObject json = new JSONObject();

        try
        {
            // Establecemos la fecha
            json.put("fecha", Helpers.CalendarToStr(datosChat.FECHA));

            // Establecemos el token
            json.put("token", Helpers.getTokenAcceso(getActivity()));

            // Establecemos el usuario que debemos obtener los mensajes
            json.put("destinatario", datosChat.DESTINATARIO);

            // Establecemos el chat
            json.put("chat", id);
        }
        catch (Exception ex) { }

        // Realizamos la petición
        cliente.post(getActivity(), Helpers.URLApi("mensajeschat"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() 
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try
                {
                    // Obtenemos el array de mensajes
                    JSONArray mensajes = Helpers.ResponseBodyToJSONArray(responseBody);

                    // Si hemos obtenido mensajes
                    if (mensajes.length() > 0 && !leidoFecha) {
                        // Insertamos el elemento en la base de datos de chats
                        ((Principal) getActivity()).getDb().crearMensajeChat(id, Helpers.CalendarToStr(Calendar.getInstance()), "", "F");

                        // Indicamos que hemos leido la fecha
                        leidoFecha = true;
                    }

                    // Para cada uno de los mensajes
                    for (int indice = 0; indice < mensajes.length(); indice++)
                    {
                        // Insertamos el elemento en la base de datos de chats
                        ((Principal) getActivity()).getDb().crearMensajeChat(id, mensajes.getJSONObject(indice).optString("texto"), "A", "T");
                    }

                    // Solicitamos los nuevos datos del chat
                    JSONObject jsonEliminar = new JSONObject();

                    try
                    {
                        // Establecemos el token
                        jsonEliminar.put("token", Helpers.getTokenAcceso(getActivity()));

                        // Establecemos el chat
                        jsonEliminar.put("chat", id);

                        // Establecemos la fecha
                        jsonEliminar.put("fecha", Helpers.CalendarToStr(datosChat.FECHA));

                        // Establecemos el usuario
                        jsonEliminar.put("usuario", datosChat.DESTINATARIO);
                    }
                    catch (Exception ex) { }

                    // Actualizamos la fecha de recogida de líneas del chat
                    ((Principal) getActivity()).getDb().actualizarChat(id, Calendar.getInstance());
                    datosChat.FECHA = Calendar.getInstance();

                    // Eliminamos los mensajes recogidos
                    cliente.post(getActivity(), Helpers.URLApi("eliminarmensajeschat"), Helpers.ToStringEntity(jsonEliminar), "application/json", new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            super.onSuccess(statusCode, headers, responseBody);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            super.onFailure(statusCode, headers, responseBody, error);

                            // Mostramos el error producido
                            Helpers.MostrarError(getActivity(), "No se ha podido eliminar los mensajes recibidos del servidor");
                        }
                    });
                }
                catch (Exception ex) { }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
            }
        });

        // Obtenemos la lista
        ArrayList<LineaChat> lineas = ((Principal) getActivity()).getDb().LineasChatBD(id);

        // Si no hemos creado el adaptador dinámico anteriormente
        if (adaptador == null) 
        {
            // Creamos el adaptador del chat
            adaptador = new AdaptadorDinamico<LineaChatViewHolder, LineaChat>(getActivity(), R.layout.lineachat, lineas) 
            {
                @Override
                public LineaChatViewHolder onViewHolder(View view) {
                    LineaChatViewHolder holder = new LineaChatViewHolder();
                    holder.mensaje             = (NoctuaTextView) view.findViewById(R.id.lineachat_texto);
                    holder.layoutfechahora     = (LinearLayout) view.findViewById(R.id.layout_fechamensaje);
                    holder.fecha               = (NoctuaTextView) view.findViewById(R.id.lineachat_dia);
                    holder.hora                = (NoctuaTextView) view.findViewById(R.id.lineachat_hora);
                    return holder;
                }

                @Override
                public void onEntrada(LineaChatViewHolder holder, LineaChat entrada, View view, int posicion) 
                {
                    // Escondemos el texto del mensaje
                    holder.mensaje.setVisibility(View.GONE);

                    // Escondemos el texto de la hora
                    holder.layoutfechahora.setVisibility(View.GONE);

                    // Si es de tipo texto
                    if (entrada.TIPO.equals("T")) 
                    {
                        // Mostramos el mensaje
                        holder.mensaje.setVisibility(View.VISIBLE);

                        // Establecemos el mensaje
                        holder.mensaje.setText(entrada.TEXTO);

                        // Según el origen
                        if (entrada.ORIGEN.equals("P")) 
                        {
                            // Establecemos el background
                            holder.mensaje.setBackgroundResource(R.drawable.chatpropio);

                            // Establecemos el color del texto
                            holder.mensaje.setTextColor(Color.rgb(91, 100, 109));

                            // Mostramos el mensaje a la izquierda
                            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.mensaje.getLayoutParams();
                            params.gravity     = Gravity.LEFT;
                            params.rightMargin = 80;
                            params.leftMargin  = 5;
                            holder.mensaje.setLayoutParams(params);
                        }
                        if (entrada.ORIGEN.equals("A")) 
                        {
                            // Establecemos el background
                            holder.mensaje.setBackgroundResource(R.drawable.chatotro);

                            // Establecemos el color del texto
                            holder.mensaje.setTextColor(Color.WHITE);

                            // Mostramos el mensaje a la derecha
                            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.mensaje.getLayoutParams();
                            params.gravity     = Gravity.RIGHT;
                            params.leftMargin  = 80;
                            params.rightMargin = 5;
                            holder.mensaje.setLayoutParams(params);
                        }
                    }

                    // Si es de tipo fecha
                    if (entrada.TIPO.equals("F")) 
                    {
                        // Eliminamos el background
                        holder.mensaje.setBackgroundColor(Color.TRANSPARENT);

                        // Escondemos el texto de la hora
                        holder.layoutfechahora.setVisibility(View.VISIBLE);

                        // Fecha actual
                        Calendar fecha = Helpers.StrToCalendar(entrada.TEXTO, "yyyy-MM-dd HH:mm:ss");

                        // Etablecemos la hora
                        holder.hora.setText(String.format("%02d:%02d %s", fecha.get(Calendar.HOUR_OF_DAY),
                                fecha.get(Calendar.MINUTE),
                                fecha.get(Calendar.HOUR_OF_DAY) > 12 ? "PM" : "AM"));

                        // Días entre las fechas
                        int dias = (int) Helpers.daysBetween(fecha, Calendar.getInstance());

                        // Establecemos la fecha
                        switch (dias) 
                        {
                            case 0:
                                holder.fecha.setText("Hoy,");
                                break;
                            case 1:
                                holder.fecha.setText("Ayer,");
                                break;
                            default:
                                // Si es en el mismo año
                                if (fecha.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR))
                                    holder.fecha.setText(String.format("%s %02d, %s,", Helpers.dias[fecha.get(Calendar.DAY_OF_WEEK) - 1],
                                            fecha.get(Calendar.DATE),
                                            Helpers.meseslargo[fecha.get(Calendar.MONTH)]));
                                else
                                    holder.fecha.setText(String.format("%s %02d, %s %d,", Helpers.dias[fecha.get(Calendar.DAY_OF_WEEK) - 1],
                                            fecha.get(Calendar.DATE),
                                            Helpers.meseslargo[fecha.get(Calendar.MONTH)],
                                            fecha.get(Calendar.YEAR)));
                                break;
                        }
                    }
                }
            };

            // Nuestra lista
            ListView lista = ((ListView) getActivity().findViewById(R.id.listachat));

            // Establecemos el adaptador
            lista.setAdapter(adaptador);

            // Establecemos la cabecera
            lista.addHeaderView(Helpers.getSpacer(getActivity(), 1), null, false);

            // Establecemos el pie de página
            lista.addFooterView(Helpers.getSpacer(getActivity(), 40), null, false);

            // Nos posicionamos al final
            lista.smoothScrollToPosition(adaptador.getCount());
            lista.setStackFromBottom(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        t.cancel();
        cliente.cancelRequests(getActivity(), true);
    }
}
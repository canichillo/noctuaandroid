package com.syp4.noctua.fragmentos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.R;
import com.syp4.noctua.holders.NumerosViewHolder;
import com.syp4.noctua.modelos.Numero;
import com.syp4.noctua.modelos.Oferta;
import com.syp4.noctua.ui.NoctuaButton;
import com.syp4.noctua.ui.NoctuaTextView;
import com.syp4.noctua.utilidades.AdaptadorDinamico;
import com.syp4.noctua.utilidades.ClienteREST;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class DesactivacionOferta extends Fragment
{
    // ID de la oferta
    int idOferta = 0;

    // Contraseña de la oferta
    int pwdOferta = 0;

    // Petición HTTP
    ClienteREST cliente;

    // Indica cuantos elementos hemos seleccionado para eliminar
    int numElementos = 0;

    // Indica cuantos elementos ya hemos usado
    int numUsados = 0;

    // Cuantos servicios tiene la oferta
    int numServicios = 0;

    // Nuestro adaptador dinámico
    AdaptadorDinamico<NumerosViewHolder, Numero> adaptadorDinamico;

    // Datos de la oferta
    Oferta datosOferta;

    // Nuestra lista
    private ArrayList<Numero> lista;

    // Digitos de la clave
    String digitosPassword = "";

    // ID de la empresa
    int idEmpresa;

    /**
     * Constructor
     * @param oferta ID de la oferta cargada
     */
    public static DesactivacionOferta newInstance(int oferta)
    {
        // Creamos el fragmento
        DesactivacionOferta frag = new DesactivacionOferta();

        frag.idOferta = oferta;

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Cargamos la vista
        View view = inflater.inflate(R.layout.desactivacionoferta, null);

        // Establecemos los eventos para los botones
        view.findViewById(R.id.button1).setOnClickListener(eventoBoton);
        view.findViewById(R.id.button2).setOnClickListener(eventoBoton);
        view.findViewById(R.id.button3).setOnClickListener(eventoBoton);
        view.findViewById(R.id.button4).setOnClickListener(eventoBoton);
        view.findViewById(R.id.button5).setOnClickListener(eventoBoton);

        // Evento del borrado de números
        view.findViewById(R.id.button_erase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (digitosPassword.length() > 1)
                    // Eliminamos el primer digito
                    digitosPassword = digitosPassword.substring(0, digitosPassword.length() - 1);
                else digitosPassword = "";

                // Volvemos a mostrar los digitos
                MostrarDigitosPassword();
            }
        });

        // Establecemos el evento de mostrar los datos de la empresa
        view.findViewById(R.id.desactivacion_imagenempresa).setOnClickListener(new View.OnClickListener() {
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

        // Inicializamos el cliente HTTP
        cliente = new ClienteREST(getActivity());

        // Establecemos el icono de la parte izquierda
        Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.back, Helpers.ATRAS);

        // Establecemos el icono de la parte derecha
        Helpers.IconoDerechaTitulo(getActivity(), 0);

        // Activamos la selección del menú a la derecha
        Helpers.ChangeMenuMode(getActivity(), Helpers.NINGUNO);

        // Establecemos el título
        Helpers.SetTitulo(getActivity(), "Noctua");

        // Mostramos la sombra
        Helpers.EstadoSombra(getActivity(), true);

        // Devolvemos la vista
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Lee los datos de desactivación
        LeerDatosDesactivacion();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cliente.cancelRequests(getActivity(), true);
    }

    @Override
    public void onDetach() {
        if (datosOferta != null && datosOferta.IMAGEN != null) {
            // Eliminamos la imagen de la oferta
            Helpers.EliminarImagen(getActivity(), "temp", datosOferta.IMAGEN + ".jpg");
        }

        super.onDetach();
    }

    /**
     * Lee los datos para la desactivación de la oferta
     */
    private void LeerDatosDesactivacion()
    {
        // Si ya hemos cargado datos anteriormente
        if (lista != null && lista.size() != 0)
        {
            // Inicializamos la lista
            InicializarLista();

            // Mostramos los datos
            MostrarDatos();

            // Salimos de la función
            return;
        }

        // Creamos el objeto JSON de la petición
        JSONObject json = new JSONObject();

        try
        {
            // Establecemos el token
            json.put("token", Helpers.getTokenAcceso(getActivity()));

            // Establecemos la oferta
            json.put("oferta", idOferta);
        }
        catch (Exception ex) { }

        // Inicializamos la lsita
        lista = new ArrayList<Numero>();

        // Realizamos la petición de lectura de los datos
        cliente.post(getActivity(), Helpers.URLApi("datosdesactivacionoferta"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try
                {
                    // Obtenemos la respuesta
                    JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                    // Si no hay error
                    if (respuesta.isNull("Error"))
                    {
                        // ID de la empresa
                        idEmpresa = respuesta.optInt("idempresa");

                        // Si tenemos cantidad
                        if (respuesta.optInt("cantidad") != 0)
                        {
                            // Número de servicios
                            numServicios = respuesta.optInt("cantidad");

                            // Usados
                            numUsados = respuesta.optInt("usados");

                            // Password de la desactivación
                            pwdOferta = respuesta.optInt("password");

                            // Si hay números disponibles
                            if (numServicios - numUsados > 0)
                            {
                                // Creamos la lista de números
                                lista = new ArrayList<Numero>();
                                for (int i = 1; i <= numServicios - numUsados; i++)
                                {
                                    Numero numero = new Numero();
                                    numero.NUMERO = String.valueOf(i);
                                    numero.ESTADO = "N";
                                    lista.add(numero);
                                }

                                // Inicializamos la lista
                                InicializarLista();
                            }

                            // Si hay un número disponible
                            if (numServicios == 1)
                            {
                                // Ocultamos el grid
                                getActivity().findViewById(R.id.gridEliminacion).setVisibility(View.GONE);
                                getActivity().findViewById(R.id.desactivacion_seleccionados1).setVisibility(View.GONE);
                                getActivity().findViewById(R.id.desactivacion_seleccionados2).setVisibility(View.GONE);
                                getActivity().findViewById(R.id.desactivacion_seleccionadosnum).setVisibility(View.GONE);

                                // Agrandamos el texto de disponibles
                                ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_textodisponibles)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                                ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_textodisponibles)).setText("1 servicio disponible");

                                // Por defecto, es un 1 elemento
                                numElementos = 1;
                            }
                        }

                        // Obtenemos los datos de la oferta
                        datosOferta = Oferta.ConsumirObjeto(Helpers.ResponseBodyToJSON(responseBody));

                        // Mostramos los datos
                        MostrarDatos();
                    }
                    else
                    {
                        // Mostramos el error producido
                        Helpers.MostrarError(getActivity(), respuesta.optString("Error"));
                    }
                } catch (Exception ex) {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Mostramos el mensaje de error
                Helpers.MostrarError(getActivity(), "No se ha podido obtener los datos de la desactivación de la oferta");
            }
        });
    }

    /**
     * Inicializamos la lista
     */
    private void InicializarLista()
    {
        // Inicializamos el adaptador dinámico
        adaptadorDinamico = new AdaptadorDinamico<NumerosViewHolder, Numero>(getActivity(), R.layout.eliminacionoferta, lista) {
            @Override
            public NumerosViewHolder onViewHolder(View view) {
                NumerosViewHolder holder = new NumerosViewHolder();

                holder.numero = (NoctuaTextView) view.findViewById(R.id.numeroelemento);

                return holder;
            }

            @Override
            public void onEntrada(NumerosViewHolder holder, Numero entrada, View view, int posicion) {
                // Mostramos el número
                holder.numero.setText(entrada.NUMERO);

                // Mostramos si está seleccionado
                if (entrada.ESTADO.equals("S"))
                {
                    holder.numero.setBackgroundResource(R.drawable.btnbackground);
                    holder.numero.setTextColor(Color.rgb(255, 255, 255));
                }

                // Si ni está seleccionado ni usado
                if (entrada.ESTADO.equals("N"))
                {
                    holder.numero.setBackgroundColor(Color.TRANSPARENT);
                    holder.numero.setTextColor(Color.rgb(63, 157, 217));
                }
            }
        };

        // Establecemos el adaptador a la lista
        ((GridView) getActivity().findViewById(R.id.gridEliminacion)).setAdapter(adaptadorDinamico);

        // Centramos los servicios
        CentrarServicios();

        // Establecemos el evento de la selección
        ((GridView) getActivity().findViewById(R.id.gridEliminacion)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Marcamos todos los elementos anteriores
                for (int indice = 0; indice < adapterView.getCount(); indice++) {
                    // Si no hay sido usado
                    ((Numero) adapterView.getItemAtPosition(indice)).ESTADO = indice <= i ? "S" : "N";
                }

                // Cambiamos el número de elementos seleccionados
                numElementos = i + 1;

                // Si hemos seleccionado algún elemento
                if (numElementos == 0) {
                    // Indicamos cuantos hemos seleccionado
                    ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_seleccionados1)).setText("No ha seleccionado nada");
                    ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_seleccionadosnum)).setText("");
                    ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_seleccionados2)).setText("");
                } else {
                    // Indicamos cuantos hemos seleccionado
                    ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_seleccionados1)).setText("Ha seleccionado");
                    ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_seleccionadosnum)).setText("" + numElementos);
                    ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_seleccionados2)).setText("servicios");
                }

                // Actualizamos los datos
                adaptadorDinamico.notifyDataSetChanged();
            }
        });
    }

    /**
     * Mostramos los datos
     */
    private void MostrarDatos()
    {
        // Si hemos cargado los datos
        getActivity().findViewById(R.id.desactivacion_layoutdatos).setVisibility(View.VISIBLE);

        // Ocultamos el texto de carga
        getActivity().findViewById(R.id.desactivacion_cargando).setVisibility(View.GONE);

        // Si tenemos una imagen para cargar
        if (!datosOferta.LOGO.equals("") && !datosOferta.LOGO.equals("null")) {
            // Si tenemos la imagen almacenada anteriormente
            if (Helpers.ExisteImagen(getActivity(), "temp", datosOferta.IMAGEN + ".jpg"))
            {
                // Establecemos la imagen
                ((ImageView) getActivity().findViewById(R.id.desactivacion_imagenempresa)).setImageBitmap(Helpers.LeerImagen(getActivity(), "temp", datosOferta.IMAGEN + ".jpg"));
            }
            else {
                // Cargamos la imagen de la oferta
                ImageLoader.getInstance().loadImage(Helpers.URLImagenes("Empresas/" + datosOferta.LOGO + ".jpg"), new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                super.onLoadingComplete(imageUri, view, loadedImage);

                                // Establecemos la imagen
                                ((ImageView) getActivity().findViewById(R.id.desactivacion_imagenempresa)).setImageBitmap(Helpers.MascaraImagen(getResources(), loadedImage, R.drawable.mascaracompleta));
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                super.onLoadingFailed(imageUri, view, failReason);

                                // Establecemos la imagen
                                ((ImageView) getActivity().findViewById(R.id.desactivacion_imagenempresa)).setImageBitmap(Helpers.MascaraImagen(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.logo), R.drawable.mascaracompleta));
                            }
                        }
                );
            }
        }
        else
        {
            // Establecemos la imagen
            ((ImageView) getActivity().findViewById(R.id.desactivacion_imagenempresa)).setImageBitmap(Helpers.MascaraImagen(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.logo), R.drawable.mascaracompleta));
        }

        // Establecemos el nombre de la oferta
        ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_nombreoferta)).setText(datosOferta.NOMBRE);

        // Establecemos el día de la oferta
        ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_diaoferta)).setText(datosOferta.INICIO.get(Calendar.DATE) + " de " + Helpers.meseslargo[datosOferta.INICIO.get(Calendar.MONTH)]);

        // Creamos las horas
        String horasOferta = String.format("%d:%02d %s", datosOferta.INICIO.get(Calendar.HOUR_OF_DAY),
                datosOferta.INICIO.get(Calendar.MINUTE),
                datosOferta.INICIO.get(Calendar.HOUR_OF_DAY) > 12 ? "PM" : "AM");

        // Fin de la hora
        horasOferta += " - ";
        horasOferta += String.format("%d:%02d %s", datosOferta.FIN.get(Calendar.HOUR_OF_DAY),
                                                   datosOferta.FIN.get(Calendar.MINUTE),
                                                   datosOferta.FIN.get(Calendar.HOUR_OF_DAY) > 12 ? "PM" : "AM");

        // Establecemos las horas
        ((NoctuaTextView) getActivity().findViewById(R.id.horas)).setText(horasOferta);

        // Si no es infinita
        if (numServicios != 0)
        {
            // Si hay elementos disponibles
            if (numServicios != numUsados)
            {
                // Si sólo hay 1 elemento
                if (numServicios > 1)
                {
                    // Mostramos la desactivacion
                    getActivity().findViewById(R.id.gridEliminacion).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.desactivacion_seleccionados1).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.desactivacion_seleccionadosnum).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.desactivacion_seleccionados2).setVisibility(View.VISIBLE);

                    // Mostramos los demás textos
                    getActivity().findViewById(R.id.top_message).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.top_message).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.pincode).setVisibility(View.VISIBLE);

                    // Mostramos el teclado numérico
                    getActivity().findViewById(R.id.tableLayout1).setVisibility(View.VISIBLE);

                    if (numUsados == 0)
                    {
                        // Cambiamos el texto
                        ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_textodisponibles)).setText("No has usado ningún servicio");
                    }
                    else {
                        // Cambiamos el texto
                        ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_textodisponibles)).setText("Has usado " + numUsados + " servicios");
                    }
                }
            }
            else
            {
                // Ocultamos la desactivacion
                getActivity().findViewById(R.id.gridEliminacion).setVisibility(View.GONE);
                getActivity().findViewById(R.id.desactivacion_seleccionados1).setVisibility(View.GONE);
                getActivity().findViewById(R.id.desactivacion_seleccionadosnum).setVisibility(View.GONE);
                getActivity().findViewById(R.id.desactivacion_seleccionados2).setVisibility(View.GONE);

                // Cambiamos el texto
                ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_textodisponibles)).setText("Has gastado todos tus servicios.");
                ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_textodisponibles)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);

                // Ocultamos los demás textos
                getActivity().findViewById(R.id.top_message).setVisibility(View.GONE);
                getActivity().findViewById(R.id.top_message).setVisibility(View.GONE);
                getActivity().findViewById(R.id.pincode).setVisibility(View.GONE);

                // Ocultamos el teclado numérico
                getActivity().findViewById(R.id.tableLayout1).setVisibility(View.GONE);
            }
        }
        // Es infinita
        else
        {
            // Ocultamos la desactivacion
            getActivity().findViewById(R.id.gridEliminacion).setVisibility(View.GONE);
            getActivity().findViewById(R.id.desactivacion_seleccionados1).setVisibility(View.GONE);
            getActivity().findViewById(R.id.desactivacion_seleccionadosnum).setVisibility(View.GONE);
            getActivity().findViewById(R.id.desactivacion_seleccionados2).setVisibility(View.GONE);

            // Cambiamos el texto
            ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_textodisponibles)).setText("La oferta es ilimitada mientras esté activa.");
        }
    }

    /**
     * Realiza el proceso de desactivar la oferta
     */
    private void DesactivarOferta()
    {
        // Creamos el objeto JSON de la petición
        JSONObject json = new JSONObject();

        try {
            // Establecemos el token
            json.put("token", Helpers.getTokenAcceso(getActivity()));

            // Establecemos la oferta
            json.put("oferta", idOferta);

            // Establecemos la cantidad
            json.put("cantidad", numUsados + numElementos);

            // Establecemos el estado
            json.put("estado", numUsados + numElementos == numServicios ? "F" : "I");
        }
        catch (Exception ex) { }

        // Realizamos la petición de lectura de los datos
        cliente.post(getActivity(), Helpers.URLApi("desactivaroferta"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try {
                    // Obtenemos la respuesta
                    JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                    // Si no hay error
                    if (!respuesta.isNull("Error")) Helpers.MostrarError(getActivity(), respuesta.optString("Error"));
                    else
                    {
                        // Ahora eliminamos los elementos seleccionados
                        for (int i = numElementos - 1; i >= 0; i--)
                            adaptadorDinamico.EliminarElemento(adaptadorDinamico.getItem(i));

                        // Actualizamos los valores del interior
                        for (int i = 0; i < adaptadorDinamico.getCount(); i++)
                            adaptadorDinamico.getItem(i).NUMERO = String.valueOf(i + 1);

                        // Refrescamos los datos
                        adaptadorDinamico.notifyDataSetChanged();

                        // Actualizamos los elementos usados
                        numUsados += numElementos;

                        // Eliminamos el código de desactivación
                        digitosPassword = "";

                        // Centramos los servicios
                        CentrarServicios();

                        // Mostramos los digitos
                        MostrarDigitosPassword();

                        // Reseteamos el número de elementos seleccionados
                        numElementos = 0;

                        // Indicamos cuantos hemos seleccionado
                        ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_seleccionados1)).setText("No ha seleccionado nada");
                        ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_seleccionadosnum)).setText("");
                        ((NoctuaTextView) getActivity().findViewById(R.id.desactivacion_seleccionados2)).setText("");

                        // Mostramos los datos
                        MostrarDatos();
                    }
                }
                catch (Exception ex) { }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Mostramos el error producido
                Helpers.MostrarError(getActivity(), "No se ha podido desactivar la oferta");
            }
        });
    }

    /**
     * Muestra los digitos por pantalla
     */
    private void MostrarDigitosPassword()
    {
        // Mostramos los digitos
        ((NoctuaTextView) getActivity().findViewById(R.id.pincode)).setText(String.format("%02d", digitosPassword.equals("") ? 0 : Integer.parseInt(digitosPassword)));
    }

    /**
     * Centra los servicios en la pantalla
     */
    private void CentrarServicios()
    {
        // Obtenemos el tamaño de la pantalla
        Point medidas = Helpers.MedidasPantalla(getActivity(), true);

        // Comprobamos cuantos elementos debemos mostrar en los servicios
        int medidasServicios = Helpers.ConvertDpToPixels(getResources(), ((numServicios - numUsados) * 60) + ((numServicios - numUsados - 1) * 5));

        // Si las medidas de los servicios es inferior a la anchura de la pantalla
        if (medidasServicios < medidas.x)
        {
            // Posición de desplazamiento a la izquierda
            int offsetLeft = (medidas.x - medidasServicios) / 2;

            // Establecemos los parámetros
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getActivity().findViewById(R.id.espaciogrid).getLayoutParams();
            params.width = offsetLeft;
            getActivity().findViewById(R.id.espaciogrid).setLayoutParams(params);
        }
    }

    /**
     * Evento que gestiona la pulsación de un botón para el password
     */
    private View.OnClickListener eventoBoton = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            // Si ya hemos introducido los números
            if (digitosPassword.length() >= 2) return;

            // Debemos haber seleccionado algún elemento
            if (numElementos <= 0)
            {
                // Mostramos el mensaje
                Helpers.MostrarError(getActivity(), "No has seleccionado ningún servicio para usar");

                return;
            }

            // Cogemos el texto del botón
            String textoBoton = ((NoctuaButton) view).getText().toString();

            // Calculamos el nuevo número
            digitosPassword += textoBoton;

            // Mostramos los digitos
            MostrarDigitosPassword();

            // Si hemos rellenado correctamente los digitos del password
            if (digitosPassword.length() == 2 && numElementos > 0)
            {
                // Si no está todavía activa
                if (datosOferta.INICIO != null && datosOferta.FIN != null)
                {
                    // Hora actual
                    Calendar horaActual = Calendar.getInstance();

                    // Si no está activa la oferta
                    if (horaActual.getTimeInMillis() < datosOferta.INICIO.getTimeInMillis() ||
                        horaActual.getTimeInMillis() > datosOferta.FIN.getTimeInMillis())
                    {
                        // Mostramos el mensaje
                        Helpers.MostrarError(getActivity(), "La oferta no está activa, no podemos procesar su petición");

                        // Salimos de la función
                        return;
                    }

                    // Si coincide la contraseña
                    if (pwdOferta == Integer.parseInt(digitosPassword))
                        // Desactivamos la oferta
                        DesactivarOferta();
                }
            }
        }
    };
}

package com.syp4.noctua.fragmentos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.syp4.noctua.Helpers;
import com.syp4.noctua.googleplayservices.GCMRegistrar;
import com.syp4.noctua.modelos.Usuarios;
import com.syp4.noctua.R;
import com.syp4.noctua.ui.NoctuaText;

import com.facebook.*;
import com.facebook.model.GraphUser;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.syp4.noctua.utilidades.ClienteREST;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class Login extends Fragment {
    // Nombre del usuario
    NoctuaText m_tvUsuario;
    // Contraseña del usuario
    NoctuaText m_tvContraseña;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();

    // Diálogo de Espera
    DialogFragment m_Espera;

    // Creamos el cliente JSON
    ClienteREST cliente;

    // Nuestra vista
    View view = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mantenemos la instancia
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Cargamos la vista
        view = inflater.inflate(R.layout.login, null);

        // Escondemos la cabecera
        Helpers.EstadoCabecera(getActivity(), false);

        // Obtenemos el texto del nombre de usuario
        m_tvUsuario = (NoctuaText) view.findViewById(R.id.txtUsuario);
        m_tvUsuario.ConfiguracionTexto(this.getString(R.string.user_upper), Helpers.getIntFromColor(74, 84, 94), Helpers.getIntFromColor(0, 0, 0));
        m_tvUsuario.Refrescar ();

        // Obtenemos el texto de la contraseña
        m_tvContraseña = (NoctuaText) view.findViewById(R.id.txtContrasenya);
        m_tvContraseña.ConfiguracionTexto(this.getString(R.string.password_upper), Helpers.getIntFromColor(74, 84, 94), Helpers.getIntFromColor(0, 0, 0));
        m_tvContraseña.Refrescar();

        // Establecemos el evento de registrar
        view.findViewById(R.id.registrar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cargamos el fragmento "Registrar"
                Helpers.LoadFragment(getActivity(), new Registrar(), "Registrar");
            }
        });

        // Establecemos el evento del botón ACCEDER
        view.findViewById(R.id.acceder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccederClick();
            }
        });

        // Establecemos el evento del botón FACEBOOK
        view.findViewById(R.id.facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FacebookClick();
            }
        });

        // Desactivamos la selección del menú a ninguna
        Helpers.ChangeMenuMode(getActivity(), Helpers.NINGUNO);

        // Mostramos la sombra
        Helpers.EstadoSombra(getActivity(), true);

        // Indicamos que el contenido es Login
        Helpers.SetContentFragment(getActivity(), this);

        // Establecemos el logo
        Helpers.MascaraImagen(getResources(), (ImageView) view.findViewById(R.id.imagenlogo),
                              BitmapFactory.decodeResource(getResources(), R.drawable.logo), R.drawable.mascaracompleta);

        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(getActivity(), null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(getActivity());
            }
            Session.setActiveSession(session);
        }

        // Ocultamos el teclado si pulsamos afuera de los campos de texto
        view.findViewById(R.id.scrollview).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                getActivity().findViewById(R.id.txtUsuario).clearFocus();
                getActivity().findViewById(R.id.txtContrasenya).clearFocus();
                return false;
            }
        });

        // Inicializamos el cliente
        cliente = new ClienteREST(getActivity());

        // Devolvemos la vista
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();

        try
        {
            Session.getActiveSession().removeCallback(statusCallback);
        }
        catch (Exception ex) {}
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
    }

    /*
     * Gestiona el evento del FACEBOOK
     */
    private void FacebookClick()
    {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
            Session.openActiveSession(getActivity(), this, true, statusCallback);
        }
    }

    /*
     * Crea un nuevo usuario en la base de datos del servidor
     * @param nuevo Los datos del nuevo usuario
     */
    private void NuevoUsuario(String IDFacebook, Usuarios nuevo)
    {
        final String szIDFacebook = IDFacebook;

        // Guardamos los datos del nombre del usuario
        Helpers.setNombre(getActivity(), nuevo.NOMBRE);

        // Datos de la solicitud
        cliente.post(getActivity(), Helpers.URLApi("nuevousuario"), Usuarios.ToStringEntity(nuevo), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                try
                {
                    // Obtenemos el objeto JSON
                    JSONObject objeto = Helpers.ResponseBodyToJSON(responseBody);

                    // Si está OK
                    if (objeto.isNull("Error"))
                    {
                        // Guardamos el token del usuario
                        Helpers.setTokenAcceso(getActivity(), objeto.getJSONObject("usuario").getString("token"));

                        // Guardamos el nombre del usuario
                        Helpers.setNombre(getActivity(), objeto.getJSONObject("usuario").getString("nombre"));

                        // Subimos la foto del usuario
                        GetPictureFacebook(szIDFacebook, objeto.getJSONObject("usuario").getString("imagen"));
                    }
                    else
                    {
                        // Mostramos la ventana de error
                        Helpers.MostrarError(getActivity(), objeto.getString("Error"));
                    }
                } catch (JSONException ex) {
                    // Mostramos la ventana de error
                    Helpers.MostrarError(getActivity(), getString(R.string.notcreateuser));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Mostramos la ventana de error
                Helpers.MostrarError(getActivity(), getString(R.string.notcreateuser));
            }
        });
    }

    /*
     * Obtiene la imagen de facebook
     */
    private void GetPictureFacebook(String faceBookId, final String nombre)
    {
        try
        {
            String url = "http://graph.facebook.com/" + faceBookId + "/picture?type=large";

            // Realizamos la obtención de la imagen
            cliente.get(url, new BinaryHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, byte[] binaryData)
                {
                    try {
                        // Procesamos los bytes de la imagen recibida
                        Bitmap imagen = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);

                        // Obtenemos la ruta donde guardaremos la imagen
                        String szRuta = Helpers.ImagenFotoPerfil(getActivity());

                        // Si existe el archivo lo eliminamos
                        if (new File(szRuta).exists()) new File(szRuta).delete();

                        try {
                            // Guardamos la imagen
                            FileOutputStream fOut = new FileOutputStream(szRuta);
                            imagen.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                            fOut.flush();
                            fOut.close();

                            // Ahora subimos la imagen
                            SubirFotoUsuario(Helpers.BitmapToByte(imagen), nombre);
                        }
                        catch (IOException ioe)
                        {
                            Log.e(getString(R.string.error), ioe.getMessage());
                        }
                    } catch (Exception ex) {
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                }
            });
        }
        catch (Exception ex)
        {
        }
    }

    /*
     * Sube la foto para un usuario
     * @param bDatos Datos de la foto
     */
    private void SubirFotoUsuario(byte[] bDatos, String nombre)
    {
        // Si no hay datos, salimos
        if (bDatos == null)	return ;

        // Establecemos los parametros
        RequestParams params = new RequestParams();
        params.put("archivo", new ByteArrayInputStream(bDatos), "imagen.jpg");
        params.put("directorio", "Usuarios");
        params.put("nombre", nombre);

        // Realizamos la petición
        cliente.post(getActivity(), Helpers.URLApi("subirimagen"), params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Obtenemos el objeto JSON
                JSONObject objeto = Helpers.ResponseBodyToJSON(responseBody);

                // Si está OK
                if (objeto.isNull("Error"))
                {
                    // Si tenemos el Google Play Services
                    if (GCMRegistrar.checkPlayServices(getActivity()))
                        // Registramos el ID del Google Cloud Messaging
                        GCMRegistrar.registrarGCM(getActivity());

                    // Abrimos la ventana de los ofertas
                    Helpers.LoadFragment(getActivity(), new Ofertas(), "Ofertas");
                }
                else
                {
                    try {
                        // Mostramos la ventana de error
                        Helpers.MostrarError(getActivity(), objeto.getString("Error"));
                    }
                    catch (Exception ex) { }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Mostramos la ventana de error
                Helpers.MostrarError(getActivity(), getString(R.string.failuploadphoto));
            }
        });
    }

    /*
     * Evento que gestion la pulsación del botón "Acceder"
     */
    private void AccederClick()
    {
        try
        {
            // Comprobamos si estan todos los parametros
            if (m_tvUsuario.EsDefecto() || m_tvContraseña.EsDefecto())
            {
                Helpers.MostrarInformacion(getActivity(), this.getString(R.string.warning), this.getString(R.string.notenoughparameters), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                return;
            }

            // Mostramos el panel de progreso
            m_Espera = Helpers.MostrarMensaje(getActivity(), this.getString(R.string.processing), this.getString(R.string.waitamoment));

            // Comprobamos el acceso
            ComprobarAcceso(m_tvUsuario.getText().toString(), m_tvContraseña.getText().toString());
        }
        catch (Exception ex){ m_Espera.dismiss(); }
    }

    /*
     * Comprueba los datos de acceso del usuario
     * @param usuario Nombre del usuario
     * @param password Contraseña del usuario
     */
    private void ComprobarAcceso(String usuario, String password)
    {
        // Creamos el objeto JSON para la petición POST
        final JSONObject json = new JSONObject();

        try {
            // Establecemos los valores del json
            json.put("usuario", usuario);
            json.put("password", password);
            json.put("so", "A");
            json.put("dispositivo", Build.BRAND + " " + Build.DEVICE);
        }
        catch (Exception ex) {}

        // Datos de la solicitud
        cliente.post(getActivity(), Helpers.URLApi("usuariopassword"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                // Convertimos la respuesta en un objeto json
                JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);
                Log.e("", respuesta.toString());
                // Comprobamos si es correcto
                if (respuesta.isNull("Error")) {
                    try {
                        // Guardamos el token del usuario
                        Helpers.setTokenAcceso(getActivity(), respuesta.getJSONObject("usuario").getString("token"));

                        // Guardamos el nombre del usuario
                        Helpers.setNombre(getActivity(), respuesta.getJSONObject("usuario").getString("nombre"));

                        // Obtenemos la imagen del usuario
                        ObtenerImagenUsuario(respuesta.getJSONObject("usuario").getString("imagen"));
                    }
                    catch (Exception ex) { }
                }
                else
                {
                    // Cerramos la ventana de espera
                    m_Espera.dismiss();

                    // Mostramos la ventana de que el usuario está dado de baja
                    if (respuesta.optString("Error").equals("El usuario está dado de baja")) {
                        // Mostramos la confirmación de dar de baja
                        Helpers.MostrarConfirmacion(getActivity(), getString(R.string.confirmacion), "El usuario está dado de baja. ¿Desea volver a darlo de alta?", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Datos de la solicitud
                                cliente.post(getActivity(), Helpers.URLApi("altausuariopassword"), Helpers.ToStringEntity(json), "application/json", new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        super.onSuccess(statusCode, headers, responseBody);

                                        // Respuesta
                                        JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                                        try {
                                            // Guardamos el token del usuario
                                            Helpers.setTokenAcceso(getActivity(), respuesta.getJSONObject("usuario").getString("token"));

                                            // Guardamos el nombre del usuario
                                            Helpers.setNombre(getActivity(), respuesta.getJSONObject("usuario").getString("nombre"));

                                            // Obtenemos la imagen del usuario
                                            ObtenerImagenUsuario(respuesta.getJSONObject("usuario").getString("imagen"));
                                        }
                                        catch (Exception ex) { }
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        super.onFailure(statusCode, headers, responseBody, error);

                                        // Mostramos el mensaje de error
                                        Helpers.MostrarError(getActivity(), "No se ha podido volver a activar el usuario en Noctua");
                                    }
                                });
                            }
                        }, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });
                    }
                    else
                    {
                        Helpers.MostrarInformacion(getActivity(), getString(R.string.error), getString(R.string.userdoesntexist), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                m_Espera.dismiss();
                Helpers.MostrarError(getActivity(), getString(R.string.errorestablishserver));
            }
        });
    }

    /*
     * Obtiene la imagen del usuario
     */
    private void ObtenerImagenUsuario(String nombre)
    {
        // Realizamos la obtención de la imagen
        ImageLoader.getInstance().loadImage(Helpers.URLImagenes("Usuarios/" + nombre + ".jpg"), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);

                try {
                    // Procesamos los bytes de la imagen recibida
                    Bitmap imagen = loadedImage;

                    try {
                        // Guardamos la imagen
                        FileOutputStream fOut = new FileOutputStream(Helpers.ImagenFotoPerfil(getActivity()));
                        imagen.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                    } catch (IOException ioe) {
                    }
                }
                catch (Exception ex) { }

                // Limpiamos la opción de espera
                m_Espera.dismiss();

                // Si tenemos el Google Play Services
                if (GCMRegistrar.checkPlayServices(getActivity()))
                    // Registramos el ID del Google Cloud Messaging
                    GCMRegistrar.registrarGCM(getActivity());

                // Cargamos los ofertas
                Helpers.LoadFragment(getActivity(), new Ofertas(), "Ofertas");
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);

                // Limpiamos la opción de espera
                m_Espera.dismiss();

                // Si tenemos el Google Play Services
                if (GCMRegistrar.checkPlayServices(getActivity()))
                    // Registramos el ID del Google Cloud Messaging
                    GCMRegistrar.registrarGCM(getActivity());

                // Cargamos los ofertas
                Helpers.LoadFragment(getActivity(), new Ofertas(), "Ofertas");
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    /*
    * Clase para gestionar el callback del estado del facebook
    */
    private class SessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception)
        {
            // Si está abierto la sesión
            if (session.isOpened()) {
                // Establecemos los permisos de Facebook
                final Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(getActivity(), Arrays.asList("user_location", "user_birthday", "user_likes", "email"));
                session.requestNewReadPermissions(newPermissionsRequest);

                // Realizamos la petición de obtención de los datos del usuario
                Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, final Response response) {
                        try {
                            // Leemos el género
                            final String genero = user.getInnerJSONObject().getString("gender").equals("male") ? "H" : "M";

                            // Fecha de nacimiento
                            final Calendar nacimiento = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

                            try {
                                // Realizamos la conversión
                                nacimiento.setTime(sdf.parse(user.getBirthday()));
                            } catch (ParseException ex) {

                            }

                            // Usuario de facebook
                            final GraphUser userFacebook = user;

                            // Petición JSON
                            JSONObject objeto = new JSONObject();
                            objeto.put("email", user.getInnerJSONObject().getString("email"));
                            objeto.put("facebook", user.getId());
                            objeto.put("usuario", user.getInnerJSONObject().getString("email"));
                            objeto.put("so", "A");
                            objeto.put("dispositivo", Build.BRAND + " " + Build.DEVICE);

                            // Ejecutamos la petición
                            cliente.post(getActivity(), Helpers.URLApi("usuariofacebook"), Helpers.ToStringEntity(objeto), "application/json", new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    super.onSuccess(statusCode, headers, responseBody);

                                    // Respuesta
                                    JSONObject respuesta = Helpers.ResponseBodyToJSON(responseBody);

                                    // Comprobamos si está correcto
                                    if (respuesta.isNull("Error"))
                                    {
                                        try {
                                            // Guardamos el token del usuario
                                            Helpers.setTokenAcceso(getActivity(), respuesta.getJSONObject("usuario").getString("token"));

                                            // Guardamos el nombre del usuario
                                            Helpers.setNombre(getActivity(), respuesta.getJSONObject("usuario").getString("nombre"));

                                            // Subimos la foto del usuario
                                            GetPictureFacebook(userFacebook.getId(), respuesta.getJSONObject("usuario").getString("imagen"));
                                        }
                                        catch (Exception ex) { }
                                    }
                                    else
                                    {
                                        try {
                                            // No debe estar dado de baja
                                            if (!respuesta.optString("Error").equals("El usuario está dado de baja")) {
                                                // Llamamos al REST de guardar
                                                NuevoUsuario(userFacebook.getId(), new Usuarios(userFacebook.getFirstName() + " " + userFacebook.getLastName(), userFacebook.getInnerJSONObject().getString("email"), userFacebook.getInnerJSONObject().getString("email"), userFacebook.getInnerJSONObject().getString("email"), nacimiento, genero, userFacebook.getId(), "A", Build.BRAND + " " + Build.DEVICE));
                                            }
                                            else
                                            {
                                                // Mostramos la confirmación de dar de baja
                                                Helpers.MostrarConfirmacion(getActivity(), getString(R.string.confirmacion), "El usuario está dado de baja. ¿Desea volver a darlo de alta?", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        // Petición JSON
                                                        JSONObject objeto = new JSONObject();

                                                        try {
                                                            // Establecemos los parámetros
                                                            objeto.put("email", userFacebook.getInnerJSONObject().getString("email"));
                                                            objeto.put("facebook", userFacebook.getId());
                                                            objeto.put("usuario", userFacebook.getInnerJSONObject().getString("email"));
                                                            objeto.put("so", "A");
                                                            objeto.put("dispositivo", Build.BRAND + " " + Build.DEVICE);
                                                        }
                                                        catch (Exception ex) { }

                                                        // Ejecutamos la petición
                                                        cliente.post(getActivity(), Helpers.URLApi("usuarioaltafacebook"), Helpers.ToStringEntity(objeto), "application/json", new AsyncHttpResponseHandler() {
                                                            @Override
                                                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                                super.onSuccess(statusCode, headers, responseBody);

                                                                // Respuesta
                                                                JSONObject json = Helpers.ResponseBodyToJSON(responseBody);

                                                                try {
                                                                    // Guardamos el token del usuario
                                                                    Helpers.setTokenAcceso(getActivity(), json.getJSONObject("usuario").getString("token"));

                                                                    // Guardamos el nombre del usuario
                                                                    Helpers.setNombre(getActivity(), json.getJSONObject("usuario").getString("nombre"));

                                                                    // Subimos la foto del usuario
                                                                    GetPictureFacebook(userFacebook.getId(), json.getJSONObject("usuario").getString("imagen"));
                                                                }
                                                                catch (Exception ex) { }
                                                            }

                                                            @Override
                                                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                                super.onFailure(statusCode, headers, responseBody, error);

                                                                // Mostramos el mensaje de error
                                                                Helpers.MostrarError(getActivity(), "No se ha podido volver a activar el usuario en Noctua");
                                                            }
                                                        });
                                                    }
                                                }, new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {

                                                    }
                                                });
                                            }
                                        }
                                        catch (Exception ex) { }
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    super.onFailure(statusCode, headers, responseBody, error);
                                }
                            });
                        } catch (JSONException ex) {
                        }
                    }
                });

                // Ejecutamos la petición
                Request.executeBatchAsync(request);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cliente.cancelRequests(getActivity(), true);
    }
}

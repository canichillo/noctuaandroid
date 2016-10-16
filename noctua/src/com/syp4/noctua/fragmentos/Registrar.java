package com.syp4.noctua.fragmentos;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.DatePicker;

import com.syp4.noctua.dialogos.DatePickerFragment;
import android.app.DatePickerDialog.OnDateSetListener;

import android.support.v4.app.FragmentManager;

import java.io.*;
import java.util.Calendar;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.content.Intent;

import com.syp4.noctua.Helpers;
import com.syp4.noctua.googleplayservices.GCMRegistrar;
import com.syp4.noctua.modelos.Usuarios;
import com.syp4.noctua.ui.NoctuaButton;
import com.syp4.noctua.R;
import com.syp4.noctua.ui.NoctuaText;
import com.syp4.noctua.ui.NoctuaCheckbox;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.syp4.noctua.utilidades.ClienteREST;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class Registrar extends Fragment
{
    // Variable que gestiona la imagen del perfil
    ImageView m_Imagen;
    // Imagen del perfil
    Bitmap m_bmpImagen;
    // Constante para la selección de la foto
    private final int SELECT_PHOTO = 1;
    // Constante para la captura de foto (cámara)
    private final int TAKE_PHOTO = 2;
    // Nombre del usuario
    NoctuaText m_tvNombre;
    // Nombre del usuario
    NoctuaText m_tvUsuario;
    // Email del usuario
    NoctuaText m_tvEmail;
    // Contraseña del usuario
    NoctuaText m_tvContraseña;
    // Nacimiento del usuario
    NoctuaButton m_btNacimiento;
    // Terminos de uso
    NoctuaCheckbox m_cbTerminos;
    // Fecha seleccionada
    Calendar m_calFecha;
    // Nuestro dialogo de espera
    DialogFragment m_Espera;
    // Género del usuario
    NoctuaButton m_tgGenero;

    // Si se ha guardado correctamente el perfíl
    boolean mGuardadoPerfil = false;

    // Ruta de la imagen
    String nombreImagen = "";

    // Realizamos el proceso de peticion
    ClienteREST cliente;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mantenemos la instancia
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Cargamos la vista
        View view = inflater.inflate(R.layout.registrar, null);

        // Mostramos el título
        Helpers.EstadoCabecera(getActivity(), true);

        // Establecemos el título de la ficha
        Helpers.SetTitulo(getActivity(), getActivity().getString(R.string.register));

        // Cambiamos la imagen del botón de la cabecera, a la de atrás
        Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.back, Helpers.ATRAS);

        // El color de la cabecera será transparente
        Helpers.ColorCabecera(getActivity(), Color.TRANSPARENT);

        // El color de la sombra de la cabecera será blanco
        Helpers.ColorSombra(getActivity(), Color.WHITE);

        // Obtenemos la imagen del perfil
        m_Imagen = (ImageView) view.findViewById(R.id.imgFoto);

        // Establecemos el evento del "click"
        m_Imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llamamos al diálogo de la imagen
                DialogoImagen();
            }
        });

        // Obtenemos el texto del nombre de usuario
        m_tvUsuario = (NoctuaText) view.findViewById(R.id.txtUsuario);
        m_tvUsuario.ConfiguracionTexto(this.getString(R.string.user), Helpers.getIntFromColor(193, 193, 193), Color.WHITE);
        m_tvUsuario.Refrescar ();

        // Obtenemos el texto del correo electronico
        m_tvEmail = (NoctuaText) view.findViewById(R.id.txtEmail);
        m_tvEmail.ConfiguracionTexto(this.getString(R.string.mail), Helpers.getIntFromColor(193, 193, 193), Color.WHITE);
        m_tvEmail.Refrescar ();

        // Obtenemos el texto de la contraseña
        m_tvContraseña = (NoctuaText) view.findViewById(R.id.txtContrasenya);
        m_tvContraseña.TextoDefecto(this.getString(R.string.password));
        m_tvContraseña.ConfiguracionTexto(this.getString(R.string.password), Helpers.getIntFromColor(193, 193, 193), Color.WHITE);
        m_tvContraseña.Refrescar ();

        // Obtenemos el texto del nombre
        m_tvNombre = (NoctuaText) view.findViewById(R.id.txtNombre);
        m_tvNombre.ConfiguracionTexto(this.getString(R.string.name), Helpers.getIntFromColor(193, 193, 193), Color.WHITE);
        m_tvNombre.Refrescar ();

        // Obtenemos el texto del nacimiento
        m_btNacimiento = (NoctuaButton) view.findViewById(R.id.btoNacimiento);

        // Configuramos el "checkbox" de los terminos
        m_cbTerminos = (NoctuaCheckbox) view.findViewById(R.id.chkTerminos);
        m_cbTerminos.ConfiguracionCheckbox(R.drawable.checked, R.drawable.unchecked);

        // Configuramos el togglebutton
        m_tgGenero = (NoctuaButton) view.findViewById(R.id.btoGenero);

        // Inicializamos la fecha
        m_calFecha = Calendar.getInstance();

        // Establecemos el evento de registrar
        NoctuaButton boton = (NoctuaButton) view.findViewById(R.id.btoRegistrar);

        try
        {
            // Si tenemos imagen
            if (new File(Helpers.ImagenFotoPerfil(getActivity())).exists())
            {
                // La establecemos
                m_bmpImagen = BitmapFactory.decodeFile(Helpers.ImagenFotoPerfil(getActivity()));
                Helpers.MascaraImagen(getResources(), m_Imagen, m_bmpImagen, R.drawable.mascara);

                // Quitamos el background
                m_Imagen.setBackgroundResource(R.drawable.avatar);
            }
        }
        catch (Exception ex)
        {
            Helpers.MostrarError(getActivity(), this.getString(R.string.error));
        }

        // Desactivamos la selección del menú a ninguna
        Helpers.ChangeMenuMode(getActivity(), Helpers.NINGUNO);

        // Mostramos la sombra
        Helpers.EstadoSombra(getActivity(), true);

        // Indicamos que el contenido es Login
        Helpers.SetContentFragment(getActivity(), this);

        // Establecemos el evento de los términos de uso
        view.findViewById(R.id.btoTerminos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cargamos el fragmento "Terminos"
                Helpers.LoadFragment(getActivity(), Terminos.newInstance(false), "Terminos");
            }
        });

        // Establecemos el evento de selección de la foto de perfíl
        view.findViewById(R.id.imgFoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Mostramos el diálogo de selección de la imagen
                DialogoImagen();
            }
        });

        // Establecemos el evento de cambiar de género
        view.findViewById(R.id.btoGenero).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GeneroClick();
            }
        });

        // Establecemos el evento de "REGISTRAR"
        view.findViewById(R.id.btoRegistrar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegistrarClick();
            }
        });

        // Establecemos el evento de "NACIMIENTO"
        view.findViewById(R.id.btoNacimiento).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Seleccionamos la fecha
                showDatePicker();
            }
        });

        // Inicializamos el HTTP
        cliente = new ClienteREST(getActivity());

        // Devolvemos la vista
        return view;
    }

    /*
     * Muestra el diálogo de selección de imágenes
     */
    private void DialogoImagen()
    {
        Helpers.MostrarSolicitudImagenAvatar(getActivity(), this.getString(R.string.selection), this.getString(R.string.whereselectionphoto), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cargamos el fragmento de los avatares
                Helpers.LoadFragment(getActivity(), new Avatares(), "Avatares");
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    // Indicamos que queremos seleccionar una foto de la galería
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

                    // Todas las imágenes
                    photoPickerIntent.setType("image/*");

                    // Establecemos los eventos
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                }
                catch (Exception ex) {}
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    // Indicamos que queremos de la camara
                    Intent camara = new Intent("android.media.action.IMAGE_CAPTURE");

                    //start camera intent
                    startActivityForResult(camara, TAKE_PHOTO);
                }
                catch (Exception ex) {}
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Según el código de petición
        switch(requestCode)
        {
            // SELECCIÓN DE FOTO
            case SELECT_PHOTO:
                if(resultCode == getActivity().RESULT_OK && data != null && data.getData() != null)
                {
                    try
                    {
                        // Ruta de la imagen seleccionada
                        final Uri imageUri            = data.getData();
                        final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);

                        // Obtenemos la imagen
                        m_bmpImagen = BitmapFactory.decodeStream(imageStream);

                        // Redimensionamos la imagen
                        m_bmpImagen = Helpers.ResizedBitmap(m_bmpImagen, 300, true);
                    }
                    catch (Exception ex) { return; }

                    try
                    {
                        // Guardamos la imagen
                        FileOutputStream fOut = new FileOutputStream(Helpers.ImagenFotoPerfil(getActivity()));
                        m_bmpImagen.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                    }
                    catch (IOException ioe)
                    {
                    }

                    // Establecemos la imagen
                    Helpers.MascaraImagen(getResources(), m_Imagen, m_bmpImagen, R.drawable.mascara);
                    m_Imagen.setBackgroundResource(R.drawable.avatar);
                }
                break;
            // CAPTURA DE FOTO
            case TAKE_PHOTO:
                if(resultCode == getActivity().RESULT_OK)
                {
                    // Obtenemos la imagen
                    m_bmpImagen = (Bitmap) data.getExtras().get("data");

                    // Redimensionamos la imagen
                    m_bmpImagen = Helpers.ResizedBitmap(m_bmpImagen, 300, true);

                    try
                    {
                        // Guardamos la imagen
                        FileOutputStream fOut = new FileOutputStream(Helpers.ImagenFotoPerfil(getActivity()));
                        m_bmpImagen.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                    }
                    catch (IOException ioe)
                    {
                    }

                    // Establecemos la imagen
                    Helpers.MascaraImagen(getResources(), m_Imagen, m_bmpImagen, R.drawable.mascara);
                    m_Imagen.setBackgroundResource(R.drawable.avatar);
                }
                break;
        }
    }

    /*
     * Selección de la fecha
     */
    private void showDatePicker()
    {
        DatePickerFragment date = new DatePickerFragment();
        /**
         * Establecemos la fecha para el diálogo
         */
        Bundle args = new Bundle();
        args.putInt("year", m_calFecha.get(Calendar.YEAR));
        args.putInt("month", m_calFecha.get(Calendar.MONTH));
        args.putInt("day", m_calFecha.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        /**
         * Establecemos el callback para capturar la fecha
         */
        FragmentManager manager = getActivity().getSupportFragmentManager();
        date.setCallBack(ondate);
        date.show(manager, getString(R.string.birthday));
    }

    /*
     * Crea un nuevo usuario en la base de datos del servidor
     * @param nuevo Los datos del nuevo usuario
     */
    private void NuevoUsuario(Usuarios nuevo)
    {
        // Si hemos guardado el perfíl, y nos ha dado error en la imagen
        // sólo reintentaremos guardar la foto
        if (!mGuardadoPerfil)
        {
            // Datos de la solicitud
            cliente.post(getActivity(), Helpers.URLApi("nuevousuario"), Usuarios.ToStringEntity(nuevo), "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    super.onSuccess(statusCode, headers, responseBody);

                    try {
                        // Obtenemos el objeto JSON
                        JSONObject objeto = Helpers.ResponseBodyToJSON(responseBody);

                        // Si está OK
                        if (objeto.isNull("Error"))
                        {
                            // Guardamos el token del usuario
                            Helpers.setTokenAcceso(getActivity(), objeto.getJSONObject("usuario").getString("token"));

                            // Guardamos el nombre del usuario
                            Helpers.setNombre(getActivity(), objeto.getJSONObject("usuario").getString("nombre"));

                            // Hemos guardado bien el perfíl
                            mGuardadoPerfil = true;

                            // Subimos la foto del usuario
                            if (m_bmpImagen != null)
                                SubirFotoUsuario(Helpers.BitmapToByte(m_bmpImagen), objeto.getJSONObject("usuario").getString("imagen"));
                        }
                        else
                        {
                            // Cerramos el cuadro de diálogo
                            m_Espera.dismiss();

                            // Mostramos la ventana de error
                            Helpers.MostrarError(getActivity(), objeto.getString("Error"));
                        }
                    } catch (JSONException ex) {
                        m_Espera.dismiss();

                        // Mostramos la ventana de error
                        Helpers.MostrarError(getActivity(), getString(R.string.notcreateuser));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);

                    // Cerramos el cuadro de diálogo
                    m_Espera.dismiss();

                    // Mostramos la ventana de error
                    Helpers.MostrarError(getActivity(), getString(R.string.notcreateuser));
                }
            });
        }
        else
        {
            // Subimos la foto del usuario
            if (m_bmpImagen != null)
                SubirFotoUsuario(Helpers.BitmapToByte(m_bmpImagen), nombreImagen);
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

        // Guardamos el nombre de la imagen
        nombreImagen = nombre;

        // Establecemos los parametros
        RequestParams params = new RequestParams();
        params.put("archivo", new ByteArrayInputStream(bDatos), "imagen.jpg");
        params.put("directorio", "Usuarios");
        params.put("nombre", nombre);

        // Realizamos la petición
        cliente.post(getActivity(), Helpers.URLApi("subirimagen"), params, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                // Eliminamos la barra de progreso
                m_Espera.dismiss();

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
                    // Eliminamos la barra de progreso
                    m_Espera.dismiss();

                    try {
                        // Mostramos la ventana de error
                        Helpers.MostrarError(getActivity(), objeto.getString("Error"));
                    }
                    catch (Exception ex){}
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Eliminamos la barra de progreso
                m_Espera.dismiss();

                // Mostramos la ventana de error
                Helpers.MostrarError(getActivity(), getString(R.string.failuploadphoto));
            }
        });
    }

    /*
     * Callback que recogerá la fecha seleccionada
     */
    OnDateSetListener ondate = new OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // Establecemos la fecha seleccionada
            m_calFecha.set(year, monthOfYear, dayOfMonth);

            // Establecemos el texto seleccionado
            m_btNacimiento.setText(String.valueOf(dayOfMonth) + "/" + String.valueOf(monthOfYear) + "/" + String.valueOf(year));
        }
    };

    /*
     * Evento que gestiona la pulsación del botón "Género"
     */
    private void GeneroClick()
    {
        // Si estaba seleccionado "Hombre"
        if (m_tgGenero.getText().toString().equals(this.getString(R.string.male)))
        {
            // Cambiamos el texto a "Mujer"
            m_tgGenero.setText(this.getString(R.string.female));

            // Cambiamos el background
            m_tgGenero.setBackgroundResource(R.drawable.bckfemale);

            // Establecemos el valor de la izquierda género
            m_tgGenero.setCompoundDrawablesWithIntrinsicBounds(R.drawable.female, 0, 0, 0);
        }
        // Si está seleccionado "Mujer"
        else
        {
            // Cambiamos el texto a "Hombre"
            m_tgGenero.setText(this.getString(R.string.male));

            // Cambiamos el background
            m_tgGenero.setBackgroundResource(R.drawable.bckmale);

            // Establecemos el valor de la izquierda género
            m_tgGenero.setCompoundDrawablesWithIntrinsicBounds(R.drawable.male, 0, 0, 0);
        }
    }

    /*
     * Evento que gestiona la pulsación del botón "Registrar"
     */
     private void RegistrarClick()
     {
        try
        {
            // Comprobamos si estan todos los parametros
            if (m_tvNombre.EsDefecto() || m_tvUsuario.EsDefecto() || m_tvContraseña.EsDefecto() || m_tvEmail.EsDefecto() || m_btNacimiento.getText().toString().trim().equals(this.getString(R.string.birth)))
            {
                Helpers.MostrarInformacion(getActivity(), this.getString(R.string.warning), this.getString(R.string.notenoughparameters), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                return;
            }

            // Comprobamos que esta marcado lo de acepto los terminos
            if (!m_cbTerminos.isChecked())
            {
                Helpers.MostrarInformacion(getActivity(), this.getString(R.string.warning), getString(R.string.mustacceptterms), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                return;
            }

            // Si no hay foto, le indicamos que la establezca
            if (m_bmpImagen == null)
            {
                // Muestra el mensaje de advertencia
                Helpers.MostrarInformacion(getActivity(), this.getString(R.string.warning), getString(R.string.photonotestablished), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                // Salimos
                return;
            }

            // Mostramos el panel de progreso
            m_Espera = Helpers.MostrarMensaje(getActivity(), this.getString(R.string.processing), this.getString(R.string.waitamoment));

            // Leemos el género
            String genero = "";
            if (m_tgGenero.getText().toString().equals(this.getString(R.string.male)))
                genero = "H";
            else genero = "M";

            // Llamamos al REST de guardar
            NuevoUsuario(new Usuarios(m_tvNombre.getText().toString(), m_tvUsuario.getText().toString(), m_tvContraseña.getText().toString(), m_tvEmail.getText().toString(), m_calFecha, genero, "", "A", Build.BRAND + " " + Build.DEVICE));
        }
        catch (Exception ex){ }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cliente.cancelRequests(getActivity(), true);
    }
}

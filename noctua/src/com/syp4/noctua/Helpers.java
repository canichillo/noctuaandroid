package com.syp4.noctua;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.*;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.content.res.Resources;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.special.ResideMenu.ResideMenu;
import com.syp4.noctua.dialogos.DialogoConfirmacion;
import com.syp4.noctua.dialogos.DialogoError;
import com.syp4.noctua.dialogos.DialogoInformacion;
import com.syp4.noctua.dialogos.DialogoMensaje;
import com.syp4.noctua.dialogos.DialogoSolicitudImagenAvatar;
import com.syp4.noctua.ui.NoctuaTextView;

import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

public final class Helpers
{
    // Acciones del menú
    public final static int MENU  = 1;
    public final static int ATRAS = 2;

    // Ciudad del NOCTUA
    public final static int CIUDAD = 2;

    // Modo del menú
    public final static int IZQUIERDA = 0;
    public final static int DERECHA   = 1;
    public final static int AMBOS     = 2;
    public final static int NINGUNO   = 3;

    // Nuestra clave de acceso al SSL
    public final static String claveSSL = "ZmUqxYWue9mzKV7FWBqr";

    // Meses
    public final static String[] meses = { "ene", "feb", "mar", "abr", "may", "jun",
                                           "jul", "ago", "sep", "oct", "nov", "dic" };

    public final static String[] meseslargo = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                                                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };

    // Dias de la semana
    public  final static String[] dias = { "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado" };

    /**
     * Obtiene la KeyHash de la aplicación necesaria para Facebook, etc...
     * @param activity Actividad principal
     */
    public static void KeyHashApp(FragmentActivity activity)
    {
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(
                    "com.syp4.noctua",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    /**
     * Convierte un color RGB a entero
     * @param Red Valor del color rojo
     * @param Green Valor del color verde
     * @param Blue Valor del color azul
     * @return Color en valor entero
     */
    public static int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF0000;
        Green = (Green << 8) & 0x0000FF00;
        Blue = Blue & 0x000000FF;

        return 0xFF000000 | Red | Green | Blue;
    }

    /**
     * Metodo que obtiene el token de acceso
     *
     * @param contexto Contexto de la actividad
     * @return Token de acceso
     */
    public static String getTokenAcceso(Context contexto) {
        SharedPreferences preferencias = contexto.getSharedPreferences("noctuaAccess", Context.MODE_PRIVATE);
        return preferencias.getString("token", "");
    }

    /**
     * Guarda el token de acceso en las preferencias de la aplicación
     *
     * @param contexto Contexto de la actividad
     * @param token    Token de acceso a guardar
     */
    public static void setTokenAcceso(Context contexto, String token) {
        SharedPreferences preferencias = contexto.getSharedPreferences("noctuaAccess", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString("token", token);
        editor.commit();
    }

    /**
     * Metodo que obtiene el nombre
     *
     * @param contexto Contexto de la actividad
     * @return Nombre del usuario
     */
    public static String getNombre(Context contexto) {
        SharedPreferences preferencias = contexto.getSharedPreferences("noctuaAccess", Context.MODE_PRIVATE);
        return preferencias.getString("nombre", "");
    }

    /**
     * Guarda el nombre en las preferencias de la aplicación
     *
     * @param contexto Contexto de la actividad
     * @param nombre   Nombre del usuario
     */
    public static void setNombre(Context contexto, String nombre) {
        SharedPreferences preferencias = contexto.getSharedPreferences("noctuaAccess", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString("nombre", nombre);
        editor.commit();
    }

    /**
     * Metodo que obtiene una valor de las preferencias
     *
     * @param contexto Contexto de la actividad
     * @return Valor de la clave establecida
     */
    public static String getValor(Context contexto, String clave) {
        SharedPreferences preferencias = contexto.getSharedPreferences("noctuaAccess", Context.MODE_PRIVATE);
        return preferencias.getString(clave, "");
    }

    /**
     * Metodo que obtiene una valor de las preferencias
     *
     * @param contexto Contexto de la actividad
     * @return Valor de la clave establecida
     */
    public static String getValor(Context contexto, String clave, String defecto) {
        SharedPreferences preferencias = contexto.getSharedPreferences("noctuaAccess", Context.MODE_PRIVATE);
        return preferencias.getString(clave, defecto);
    }

    /**
     * Guarda el nombre en las preferencias de la aplicación
     *
     * @param contexto Contexto de la actividad
     */
    public static void setValor(Context contexto, String clave, String valor) {
        SharedPreferences preferencias = contexto.getSharedPreferences("noctuaAccess", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString(clave, valor);
        editor.commit();
    }

    /*
     * Genera un bitmap con los bordes redondeados
     * @param bitmap Imagen original
     * @param pixels Cantidad de pixels redondeados
     * @return Imagen con los bordes redondeados
     */
    public static Bitmap RoundedRectBitmap(Bitmap bitmap, int pixels)
    {
        int color;
        Paint paint;
        Rect rect;
        RectF rectF;
        Bitmap result;
        Canvas canvas;
        float roundPx;

        result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(result);

        color   = Color.rgb(23, 23, 23);
        paint   = new Paint();
        rect    = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        rectF   = new RectF(rect);
        roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return result;
    }

    /*
     * Redimensiona una imagen
     * @param bitmap Imagen original
     * @param newWidth Ancho nuevo de la imagen
     * @return Imagen redimensionada
     */
    public static Bitmap ResizedBitmap(Bitmap bm, int newWidth, boolean keep)
    {
        int width  = bm.getWidth();
        int height = bm.getHeight();

        float aspect      = (float)width / height;
        float scaleWidth  = newWidth;
        float scaleHeight = keep ? scaleWidth : scaleWidth / aspect;

        // Crea una matriz de manipulacion
        Matrix matrix = new Matrix();

        // Redimensionamos el bitmap
        matrix.postScale(scaleWidth / width, scaleHeight / height);

        // Recreamos el bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);

        bm.recycle();

        return resizedBitmap;
    }

    /*
     * Establece la máscara a  una imagen
     * @param res Archivo de recursos
     * @param imageView Componente de la imagen
     * @param bmpOriginal Imagen original
     * @param nMascara ID de la máscara
     */
    public static void MascaraImagen(Resources res, ImageView imageView, Bitmap bmpOriginal, int nMascara)
    {
        Canvas canvas    = new Canvas();
        Bitmap mainImage = bmpOriginal;
        Bitmap maskImage = BitmapFactory.decodeResource (res, nMascara);
        maskImage        = Bitmap.createScaledBitmap(maskImage, mainImage.getWidth(), mainImage.getHeight(), true);
        Bitmap result    = Bitmap.createBitmap(mainImage.getWidth(), mainImage.getHeight(), Bitmap.Config.ARGB_8888);

        canvas.setBitmap(result);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);

        canvas.drawBitmap(mainImage, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(maskImage, 0, 0, paint);
        paint.setXfermode(null);

        imageView.setImageBitmap(result);
        imageView.setScaleType (ImageView.ScaleType.CENTER_INSIDE);
    }

    /*
     * Establece la máscara a una imagen
     * @param res Archivo de recursos
     * @param bmpOriginal Imagen original
     * @param nMascara ID de la máscara
     */
    public static Bitmap MascaraImagen(Resources res, Bitmap bmpOriginal, int nMascara)
    {
        Canvas canvas    = new Canvas();
        Bitmap mainImage = bmpOriginal;
        Bitmap maskImage = BitmapFactory.decodeResource (res, nMascara);
        maskImage        = Bitmap.createScaledBitmap(maskImage, mainImage.getWidth(), mainImage.getHeight(), true);
        Bitmap result    = Bitmap.createBitmap(mainImage.getWidth(), mainImage.getHeight(), Bitmap.Config.ARGB_8888);

        canvas.setBitmap(result);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);

        canvas.drawBitmap(mainImage, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(maskImage, 0, 0, paint);
        paint.setXfermode(null);

        return result;
    }

    /*
     * Convierte un Bitmap a su representacion en un array de bytes
     * @param bitmap Imagen original
     * @return Representacion en un array de bytes de la imagen
     */
    public static byte[] BitmapToByte(Bitmap bitmap)
    {
        byte[] bitmapData;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        bitmapData = bos.toByteArray();

        return bitmapData;
    }

    /*
     * Establece el título de la cabecera
     * @param vista Vista actual
     * @param szTitulo Título nuevo de la cabecera
     */
    public static void SetTitulo(Activity vista, String szTitulo)
    {
        // Obtenemos el campo del titulo
        NoctuaTextView Titulo = (NoctuaTextView) vista.findViewById(R.id.txtTitulo);

        // Establecemos el texto del titulo
        Titulo.setText(szTitulo);

        // Establecemos la fuente por defecto
        Titulo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
    }

    /*
     * Establece el título de la cabecera
     * @param vista Vista actual
     * @param szTitulo Título nuevo de la cabecera
     * @param size Tamaño de la fuente
     */
    public static void SetTitulo(Activity vista, String szTitulo, int size)
    {
        // Obtenemos el campo del titulo
        NoctuaTextView Titulo = (NoctuaTextView) vista.findViewById(R.id.txtTitulo);

        // Establecemos el texto del titulo
        Titulo.setText(szTitulo);

        // Establecemos la fuente por defecto
        Titulo.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    /**
     * Muestra u oculta la sombra de la cabecera
     * @param actividad Actividad principal
     * @param mostrar Si debemos mostrar o no la sombra
     */
    public static void EstadoSombra(FragmentActivity actividad, boolean mostrar)
    {
        if (mostrar)
            actividad.findViewById(R.id.llsombra).setVisibility(View.VISIBLE);
        else actividad.findViewById(R.id.llsombra).setVisibility(View.GONE);
    }

    /**
     * Establece el icono de la izquierda de la cabecera
     *
     * @param vista Vista actual
     * @param icono Icono
     */
    public static void IconoIzquierdaTitulo(Activity vista, int icono, int tag)
    {
        // Accedemos al icono
        ImageView imagen = (ImageView) vista.findViewById(R.id.imgtituloi);

        // Si no hemos establecido el icono, ocultamos el icono
        if (icono == 0) imagen.setVisibility(View.INVISIBLE);
            // Establecemos el icono
        else
        {
            // Mostramos la imagen
            imagen.setVisibility(View.VISIBLE);

            // Obtenemos los parámetros
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) vista.findViewById(R.id.imgtituloi).getLayoutParams();

            // Si es el icono de atrás
            if (icono == R.drawable.back)
                params.leftMargin = 10;
            else params.leftMargin = 20;

            // Reconfiguramos la posición
            vista.findViewById(R.id.imgtituloi).setLayoutParams(params);

            // Establecemos la imagen
            imagen.setImageResource(icono);

            // Establecemos el tag
            imagen.setTag(tag);
        }
    }

    /**
     * Establece el icono de la derecha de la cabecera
     *
     * @param vista Vista actual
     * @param icono Icono
     */
    public static void IconoDerechaTitulo(Activity vista, int icono)
    {
        // Accedemos al icono
        ImageView imagen = (ImageView) vista.findViewById(R.id.imgtitulod);

        // Si no hemos establecido el icono, ocultamos el icono
        if (icono == 0)
        {
            // Ocultamos la imagen
            imagen.setVisibility(View.INVISIBLE);
        }
        else
        {
            // Mostramos la imagen
            imagen.setVisibility(View.VISIBLE);

            // Establecemos el icono o imagen
            imagen.setImageResource(icono);
        }
    }

    /**
     * Establece el color de la cabecera
     * @param vista Vista principal
     * @param color Color de la cabecera
     */
    public static void ColorCabecera(Activity vista, int color)
    {
        ((Principal) vista).ColorCabecera(color);
    }

    /**
     * Establece el color de la sombra
     * @param vista Vista principal
     * @param color Color de la sombra
     */
    public static void ColorSombra(Activity vista, int color)
    {
        ((Principal) vista).ColorSombra(color);
    }

    /**
     * Establece el icono de la derecha de la cabecera
     *
     * @param vista Vista actual
     * @param icono Icono
     */
    public static void IconoDerechaTitulo(Activity vista, Bitmap icono)
    {
        // Accedemos al icono
        ImageView imagen = (ImageView) vista.findViewById(R.id.imgtitulod);

        // Si no hemos establecido el icono, ocultamos el icono
        if (icono == null)
        {
            // Ocultamos la imagen
            imagen.setVisibility(View.INVISIBLE);
        }
        else
        {
            // Mostramos la imagen
            imagen.setVisibility(View.VISIBLE);

            // Establecemos el icono o imagen
            imagen.setImageBitmap(icono);
        }
    }

    /**
     * Muestra el diálogo de confirmación
     *
     * @param actividad Actividad
     * @param titulo  Título del mensaje
     * @param mensaje Contenido del mensaje
     * @param yes     Evento cuando pulsamos sobre el botón sí
     * @param no      Evento cuando pulsamos sobre el botón no
     */
    public static void MostrarConfirmacion(FragmentActivity actividad, String titulo, String mensaje,
                                           final View.OnClickListener yes, final View.OnClickListener no)
    {
        try {
            // Creamos el diálogo
            DialogFragment dialogo = new DialogoConfirmacion().newInstance(titulo, mensaje, yes, no);
            dialogo.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialogo);
            dialogo.show(actividad.getSupportFragmentManager(), "DialogoConfirmacion");
            dialogo.setCancelable(false);
        }
        catch (Exception ex) { }
    }

    /**
     * Muestra el diálogo de información
     *
     * @param actividad Actividad
     * @param titulo  Título del mensaje
     * @param mensaje Contenido del mensaje
     * @param aceptar Evento cuando pulsamos sobre el botón aceptar
     */
    public static DialogFragment MostrarInformacion(FragmentActivity actividad, String titulo, String mensaje,
                                          final View.OnClickListener aceptar)
    {
        try {
            // Creamos el diálogo
            DialogFragment dialogo = new DialogoInformacion().newInstance(titulo, mensaje, aceptar);
            dialogo.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialogo);
            dialogo.show(actividad.getSupportFragmentManager(), "DialogoInformacion");
            dialogo.setCancelable(false);
            return dialogo;
        }
        catch (Exception ex) { return null; }
    }

    /**
     * Muestra el diálogo de error
     *
     * @param actividad Actividad
     * @param mensaje Contenido del mensaje
     */
    public static DialogFragment MostrarError(FragmentActivity actividad, String mensaje)
    {
        try {
            // Creamos el diálogo
            DialogFragment dialogo = new DialogoError().newInstance("Error", mensaje);
            dialogo.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialogo);
            dialogo.show(actividad.getSupportFragmentManager(), "DialogoError");
            dialogo.setCancelable(false);
            return dialogo;
        }
        catch (Exception ex) { return null; }
    }

    /**
     * Mostramos el mensaje de espera
     *
     * @param actividad Actividad
     * @param titulo  Título del mensaje
     * @param mensaje Contenido del mensaje
     * @return Diálogo creado
     */
    public static DialogFragment MostrarMensaje(FragmentActivity actividad, String titulo, String mensaje)
    {
        try {
            // Creamos el diálogo
            DialogFragment dialogo = new DialogoMensaje().newInstance(titulo, mensaje);
            dialogo.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialogo);
            dialogo.show(actividad.getSupportFragmentManager(), "DialogoMensaje");
            dialogo.setCancelable(false);

            // Devolvemos el diálogo
            return dialogo;
        }
        catch (Exception ex) { return null; }
    }

    /**
     * Muestra el diálogo de solicitud de imagen y avatar
     */
    public static DialogFragment MostrarSolicitudImagenAvatar(FragmentActivity actividad, String titulo, String mensaje,
                                                              final View.OnClickListener avatar, final View.OnClickListener seleccion, final View.OnClickListener tomarfoto)
    {
        try {
            // Creamos el diálogo
            DialogFragment dialogo = new DialogoSolicitudImagenAvatar().newInstance(titulo, mensaje, avatar, seleccion, tomarfoto);
            dialogo.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialogo);
            dialogo.show(actividad.getSupportFragmentManager(), "DialogoSolicitudImagenAvatar");

            // Devolvemos el diálogo
            return dialogo;
        }
        catch (Exception ex) { return null; }
    }

    /**
     * Crea la URL completa para un servicio web o API del WAP
     *
     * @param api API del WAP
     * @return URL completa del servicio web
     */
    public static String URLApi(String api)
    {
        return "https://ec2-54-72-54-47.eu-west-1.compute.amazonaws.com/api/" + api;
    }

    public static String URLImagenes(String imagen)
    {
        return "https://ec2-54-72-54-47.eu-west-1.compute.amazonaws.com/Imagenes/" + imagen;
    }

    /*
     * Convierte una fecha en formato "string" a "calendar"
     */
    public static Calendar StrToCalendar(String fecha, String formato)
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(formato, Locale.US);
            Calendar date        = Calendar.getInstance();
            date.setTime(sdf.parse(fecha));

            return date;
        }
        catch (ParseException ex)
        {
            return Calendar.getInstance();
        }
    }

    /*
     * Convierte una fecha en formato "date" a "string"
     */
    public static String CalendarToStr(Calendar fec)
    {
        try
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return df.format(fec.getTime());
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    /*
     * Convierte los pixels a Dp
     * @param res Recursos
     * @param pixelValue Valor de los pixels
     * @returns Dp segun el valor de los pixels
     */
    public static int ConvertPixelsToDp(Resources res, float pixelValue)
    {
        float Densidad = res.getDisplayMetrics().density;
        int dp = (int) ((pixelValue) / Densidad);
        return dp;
    }

    /*
     * Convierte Dp a pixels
     * @param Dps dps
     * @returns Valor de pixels de los Dp
     */
    public static int ConvertDpToPixels(Resources res, float dps)
    {
        float scale = res.getDisplayMetrics().density;
        int pixels  = (int) (dps * scale + 0.5f);

        return pixels;
    }

    /**
     * Calcula las medidas de la pantalla
     * @param actividad Actividad sobre la que calcularemos el tamaño de la misma
     * @param real Si queremos calcular el tamaño real (sin barras de notificación)
     * @return Medidas de la pantalla
     */
    public static Point MedidasPantalla(Activity actividad, boolean real)
    {
        Point medidas = new Point();

        WindowManager w        = actividad.getWindowManager();
        Display d              = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        // since SDK_INT = 1;
        medidas.x = metrics.widthPixels;
        medidas.y = metrics.heightPixels;

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
        {
            try
            {
                medidas.x = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
                medidas.y = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
            }
            catch (Exception ignored) { }
        }

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17)
        {
            try
            {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
                medidas.x = realSize.x;
                medidas.y = realSize.y;
            }
            catch (Exception ignored) { }
        }

        // Si queremos las verdaderas (sin contar barras de notificaciones, etc...)
        if (real)
        {
            Rect rect  = new Rect();
            Window win = actividad.getWindow();

            win.getDecorView().getWindowVisibleDisplayFrame(rect);

            // Get the height occupied by the decoration contents
            int contentViewTop = win.findViewById(Window.ID_ANDROID_CONTENT).getTop();

            // Hacemos los cálculos
            if (Build.VERSION.SDK_INT >= 15)
               medidas.y -= contentViewTop + 300;
            else medidas.y -= contentViewTop;
        }

        return medidas;
    }

    /**
     * Realiza el proceso de volver hacia atrás en los fragmentos (PULSAR BOTÓN HACIA ATRÁS)
     * @param activity Actividad principal
     */
    public static void BackFragment(FragmentActivity activity)
    {
        try
        {
            if (activity.getSupportFragmentManager().getBackStackEntryCount() == 0)
            {
                activity.finish();
            }
            else
            {
                activity.getSupportFragmentManager().popBackStack();
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        catch (Exception ex){ }
    }

    /**
     * Establece el valor del fragmento del contenido
     * @param activity Actividad principal
     * @param fragment Fragmento del contenido
     */
    public static void SetContentFragment(FragmentActivity activity, Fragment fragment)
    {
        ((Principal) activity).setmContent(fragment);
    }

    /**
     * Carga un fragmento en la vista principal
     * @param activity Actividad principal
     * @param fragmento Fragmento o ventana a cargar
     */
    public static void LoadFragment(FragmentActivity activity, Fragment fragmento, String tag)
    {
        // Accedemos a la actividad principal
        Principal principal = (Principal) activity;

        // Cargamos el fragmento
        principal.cambiarFragmento(fragmento, tag);
    }

    /**
     * Cambia el modo del menú lateral
     * @param activity Actividad principal
     * @param mode Modo del menú
     */
    public static void ChangeMenuMode(FragmentActivity activity, int mode)
    {
        // Accedemos a la actividad principal
        Principal principal = (Principal) activity;

        // Establecemos el modo del menú
        principal.CambiarModoMenu(mode);
    }

    /**
     * Convierte un objeto a StringEntity
     *
     * @return StringEntity del objeto
     */
    public static StringEntity ToStringEntity(JSONObject json)
    {
        try
        {
            StringEntity entidad = new StringEntity(json.toString(), HTTP.UTF_8);
            return entidad;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * Convierte una respuesta (array de bytes) a un objeto JSON
     * @param response Respuesta en array de bytes
     * @return Objeto JSON
     */
    public static JSONObject ResponseBodyToJSON(byte[] response)
    {
        try
        {
            // Realizamos la conversión
            return new JSONObject(new String(response, "UTF-8"));
        }
        catch (Exception ex) { return new JSONObject(); }
    }

    /**
     * Convierte una respuesta (array de bytes) a un array JSON
     * @param response Respuesta en array de bytes
     * @return Array JSON
     */
    public static JSONArray ResponseBodyToJSONArray(byte[] response)
    {
        try
        {
            // Realizamos la conversión
            return new JSONArray(new String(response, "UTF-8"));
        }
        catch (Exception ex) { return new JSONArray(); }
    }

    /**
     * Comprueba si está montado la unidad externa SD-CARD
     * @return Si tiene o no unidad externa SD-CARD
     */
    public static boolean isExternalStorageMounted()
    {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Obtiene la rotación del móvil
     * @param context Contexto actual
     * @return Angulo de rotación del móvil
     */
    public static int OrientacionDispositivo(Context context)
    {
        WindowManager windowManager =  (WindowManager) context.getSystemService(context.WINDOW_SERVICE);

        Configuration config = context.getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT))
            return Configuration.ORIENTATION_LANDSCAPE;
        else
            return Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * Obtiene los grados de orientación del móvil
     * @param context Contexto actual
     * @return Grados de la orientación
     */
    public static int GradosDispositivo(Context context)
    {
        WindowManager windowManager =  (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        Configuration config = context.getResources().getConfiguration();
        return windowManager.getDefaultDisplay().getRotation();
    }

    /**
     * Crea el icono para un marcador del mapa
     * @param activity Actividad principal
     * @param bmp Imagen del marcador
     * @param mascara Usar máscara
     * @return Icono creado
     */
    public static BitmapDescriptor ImagenMarcadorMapa(FragmentActivity activity, Bitmap bmp, boolean mascara)
    {
        // Opciones de la imagen
        BitmapFactory.Options options = new BitmapFactory.Options();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            options.inMutable = true;
        }

        // Cargamos el marcador o pin por defecto
        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.markermap, options);
        // Ajustamos el tamaño
        bitmap = Helpers.ResizedBitmap(bitmap, 100, true);
        if (bitmap.isMutable() == false) bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        // Creamos el lienzo
        Canvas canvas = new Canvas(bitmap);

        // Ajustamos el icono pasado y le aplicamos la máscara
        bmp = mascara ? Helpers.ResizedBitmap(bmp, 75, true) : Helpers.ResizedBitmap(bmp, 68, true);
        if (mascara) bmp = Helpers.MascaraImagen(activity.getResources(), bmp, R.drawable.mascaracompleta);

        // Dibujamos la imagen pasada como icono
        if (mascara) canvas.drawBitmap(bmp, 7, 5, null);
        else canvas.drawBitmap(bmp, 11, 5, null);

        // Devolvemos la imagen del icono para el marcador del mapa
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Crea las opciones para un marcardor del Google Map
     * @return Opciones del marcador
     */
    public static MarkerOptions CrearMarcadorMapa(FragmentActivity activity, LatLng posicion,
                                                  String titulo, Bitmap bmp, boolean snippet, boolean mascara)
    {
        // Opciones para el marcador
        MarkerOptions options = new MarkerOptions();

        // Establecemos los valores del marcador
        options.title(titulo);
        options.icon(ImagenMarcadorMapa(activity, bmp, mascara));
        options.position(posicion);
        if (snippet && !options.getTitle().equals("Tú")) options.snippet(titulo);
        if (!options.getTitle().equals("Tú")) options.snippet("Acceder a las ofertas");

        // Devolvemos las opciones del marcador
        return options;
    }

    /**
     * Crea las opciones para un marcardor del Google Map
     * @return Opciones del marcador
     */
    public static MarkerOptions CrearMarcadorMapa(FragmentActivity activity, LatLng posicion,
                                                  String titulo, String snippet, Bitmap bmp, boolean mascara)
    {
        // Opciones para el marcador
        MarkerOptions options = new MarkerOptions();

        // Establecemos los valores del marcador
        options.title(titulo);
        options.icon(ImagenMarcadorMapa(activity, bmp, mascara));
        options.position(posicion);
        options.snippet(snippet);

        // Devolvemos las opciones del marcador
        return options;
    }

    /**
     * Recalcula de nuevo los límites del mapa
     * @param punto Punto del mapa a comprobar
     * @param limitesMapa Limites del mapa
     */
    public static LatLngBounds CalcularLimitesMapa(LatLng punto, LatLngBounds limitesMapa)
    {
        // Límites de salida
        LatLngBounds limites = new LatLngBounds(limitesMapa.southwest, limitesMapa.northeast);

        // Si es superior al punto NORESTE
        if (punto.latitude > limites.northeast.latitude)
        {
            // Actualizamos el tope superior
            limites = new LatLngBounds(limites.southwest, new LatLng(punto.latitude, limites.northeast.longitude));
        }

        // Si es superior al punto NORESTE
        if (punto.longitude > limites.northeast.longitude)
        {
            // Actualizamos el tope superior
            limites = new LatLngBounds(limites.southwest, new LatLng(limites.northeast.latitude, punto.longitude));
        }

        // Si es inferior al punto SUROESTE
        if (punto.latitude < limites.southwest.latitude)
        {
            // Actualizamos el tope inferior
            limites = new LatLngBounds(new LatLng(punto.latitude, limites.southwest.longitude), limites.northeast);
        }

        // Si es inferior al punto SUROESTE
        if (punto.longitude < limites.southwest.longitude)
        {
            // Actualizamos el tope inferior
            limites = new LatLngBounds(new LatLng(limites.southwest.latitude, punto.longitude), limites.northeast);
        }

        return limites;
    }

    /**
     * Guarda una imagen en un directorio temporal para su posterior uso
     * @throws IOException
     */
    public static void GuardarImagen(FragmentActivity actividad, Bitmap imagen, String directorio, String archivo)
    {
        // Ruta completa
        String ruta = "";

        // Si tenemos la SD activada
        if (Helpers.isExternalStorageMounted())
        {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.syp4.noctua/" + directorio;
            File sgDir  = new File(path);
            if (!sgDir.exists()) sgDir.mkdirs();

            ruta = path + "/" + archivo;
        }
        else
        {
            String path = actividad.getFilesDir() + "/noctua/" + directorio;
            File sgDir  = new File(path);
            if (!sgDir.exists()) sgDir.mkdirs();

            ruta = path + "/" + archivo;
        }

        try {
            FileOutputStream fOut = new FileOutputStream(ruta);
            imagen.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        }
        catch (Exception ex) { }
    }

    /**
     * Carga una imagen de un archivo temporal
     * @param actividad Actividad principal
     * @param archivo Nombre del archivo de la imagen
     */
    public static Bitmap LeerImagen(FragmentActivity actividad, String directorio, String archivo)
    {
        // Ruta completa
        String ruta = "";

        // Si tenemos la SD activada
        if (Helpers.isExternalStorageMounted())
        {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.syp4.noctua/" + directorio;
            File sgDir  = new File(path);
            if (!sgDir.exists()) sgDir.mkdirs();

            ruta = path + "/" + archivo;
        }
        else
        {
            String path = actividad.getFilesDir() + "/noctua/" + directorio;
            File sgDir  = new File(path);
            if (!sgDir.exists()) sgDir.mkdirs();

            ruta = path + "/" + archivo;
        }

        return BitmapFactory.decodeFile(ruta);
    }

    /**
     * Elimina una imagen temporal
     * @param actividad Actividad principal
     * @param archivo Archivo temporal a eliminar
     */
    public static void EliminarImagen(FragmentActivity actividad, String directorio, String archivo)
    {
        // Ruta completa
        String ruta = "";

        // Si tenemos la SD activada
        if (Helpers.isExternalStorageMounted())
        {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.syp4.noctua/" + directorio;
            File sgDir  = new File(path);
            if (!sgDir.exists()) sgDir.mkdirs();

            ruta = path + "/" + archivo;
        }
        else
        {
            String path = actividad.getFilesDir() + "/noctua/" + directorio;
            File sgDir  = new File(path);
            if (!sgDir.exists()) sgDir.mkdirs();

            ruta = path + "/" + archivo;
        }

        try {
            // Si existe el archivo
            if (new File(ruta).exists())
                new File(ruta).delete();
        }
        catch (Exception ex) { }
    }

    /**
     * Comprueba si existe una imagen temporal
     * @param actividad Actividad principal
     * @param archivo Nombre del archivo de la imagen
     * @return Si existe o no la imagen
     */
    public static boolean ExisteImagen(FragmentActivity actividad, String directorio, String archivo)
    {
        // Ruta completa
        String ruta = "";

        // Si tenemos la SD activada
        if (Helpers.isExternalStorageMounted())
        {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.syp4.noctua/" + directorio;
            File sgDir  = new File(path);
            if (!sgDir.exists()) sgDir.mkdirs();

            ruta = path + "/" + archivo;
        }
        else
        {
            String path = actividad.getFilesDir() + "/noctua/" + directorio;
            File sgDir  = new File(path);
            if (!sgDir.exists()) sgDir.mkdirs();

            ruta = path + "/" + archivo;
        }

        try {
            // Si existe el archivo
            return new File(ruta).exists();
        }
        catch (Exception ex) { return false; }
    }

    /**
     * Rota una imagen una cantidad de grados
     * @param src Imagen a rotar
     * @param degree Cantidad de grados a rotar
     * @return Imagen rotada
     */
    public static Bitmap rotateImage(Bitmap src, float degree)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    /**
     * Rota un NoctuaTextView
     * @param texto Texto a rotar
     * @param grados Cantidad de grados a rotar
     */
    public static void RotateTextView(NoctuaTextView texto, float grados)
    {
        if (Build.VERSION.SDK_INT < 11)
        {
            RotateAnimation animation = new RotateAnimation(0, grados);
            animation.setDuration(10);
            animation.setFillAfter(true);
            texto.startAnimation(animation);
        }
        else
        {
            texto.setRotation(grados);
        }
    }

    /**
     * Obtiene la ruta de la imagen a guardar
     * @return Ruta donde almacenaremos la imagen
     * @throws IOException
     */
    public static String ImagenFotoPerfil(FragmentActivity actividad)
    {
        // Si tenemos la SD activada
        if (Helpers.isExternalStorageMounted())
        {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.syp4.noctua/images";
            File sgDir  = new File(path);
            if (!sgDir.exists()) sgDir.mkdirs();

            return path + File.separator + "fotoNoctua.jpg";
        }
        else
        {
            String path = actividad.getFilesDir() + "/noctua/images";
            File sgDir  = new File(path);
            if (!sgDir.exists()) sgDir.mkdirs();

            return path + File.separator + "fotoNoctua.jpg";
        }
    }

    /**
     * Obtiene el menú de la aplicación
     * @param actividad Actividad principal
     * @return Menú de la aplicación
     */
    public static ResideMenu getResideMenu(FragmentActivity actividad)
    {
        return ((Principal) actividad).getResideMenu();
    }

    /**
     * Para las cabeceras y pies de las listas
     * @param actividad
     * @return
     */
    public static LinearLayout getSpacer(FragmentActivity actividad, int size)
    {
        LinearLayout    spacer = new LinearLayout(actividad);
        spacer.setOrientation(LinearLayout.HORIZONTAL);
        spacer.setPadding(0, ConvertDpToPixels(actividad.getResources(), size), 0, 0);
        return spacer;
    }

    /**
     * Cambia el estado de visualizado de la cabecera
     * @param actividad Actividad principal
     * @param mostrar Si debemos mostrar o no la cabecera
     */
    public static void EstadoCabecera(FragmentActivity actividad, boolean mostrar)
    {
        ((Principal) actividad).EstadoCabecera(mostrar);
    }

    /**
     * Obtiene la diferencia en dias de dos fechas
     * @param startDate Fecha de comienzo
     * @param endDate Fecha de fin
     * @return Número de días
     */
    public static long daysBetween(Calendar startDate, Calendar endDate)
    {
        long end = endDate.getTimeInMillis();
        long start = startDate.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
    }

    /**
     * Esconde el teclado
     * @param activity Actividad principal
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}

package com.syp4.noctua.dialogos;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.syp4.noctua.R;
import com.syp4.noctua.ui.NoctuaTextView;

public class DialogoSolicitudImagen extends DialogFragment
{
    // Variables
    String titulo  = "";
    String mensaje = "";
    View.OnClickListener camara  = null;
    View.OnClickListener galeria = null;

    /**
     * Constructor por defecto
     * @param titulo Título del mensaje
     * @param mensaje Contenido del mensaje
     */
    public static DialogoSolicitudImagen newInstance(String titulo, String mensaje, View.OnClickListener galeria, View.OnClickListener camara)
    {
        // Configuramos el diálogo
        DialogoSolicitudImagen frag = new DialogoSolicitudImagen();

        // Establecemos los textos
        frag.titulo  = titulo;
        frag.mensaje = mensaje;

        // Establecemos los eventos
        frag.camara  = camara;
        frag.galeria = galeria;

        // Devolvemos la instancia creada
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Establecemos el layout
        View view = inflater.inflate(R.layout.solicitudimagen, null);

        // Establecemos el título
        ((NoctuaTextView) view.findViewById(R.id.titulo)).setText(titulo);

        // Establecemos el contenido del mensaje
        ((NoctuaTextView) view.findViewById(R.id.mensaje)).setText(mensaje);

        // Evento cuando pulsamos sobre el botón CAMARA
        view.findViewById(R.id.camara).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                camara.onClick(v);
                dismiss();
            }
        });

        // Evento cuando pulsamos sobre el botón SELECCIONAR
        view.findViewById(R.id.seleccionarfoto).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                galeria.onClick(v);
                dismiss();
            }
        });

        // Devolvemos la vista creada
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Obtenemos el tamaño que queremos del diálogo
        DisplayMetrics metrics = getActivity().getBaseContext().getResources().getDisplayMetrics();
        int screenWidth        = (int) (metrics.widthPixels * 0.80);

        Window window = getDialog().getWindow();
        window.setLayout(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
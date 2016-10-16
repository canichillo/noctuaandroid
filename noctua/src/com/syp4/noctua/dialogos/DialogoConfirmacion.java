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

public class DialogoConfirmacion extends DialogFragment
{
    // Variables
    String titulo  = "";
    String mensaje = "";
    View.OnClickListener aceptar  = null;
    View.OnClickListener cancelar = null;

    /**
     * Constructor por defecto
     * @param titulo Título del mensaje
     * @param mensaje Contenido del mensaje
     */
    public static DialogoConfirmacion newInstance(String titulo, String mensaje, View.OnClickListener aceptar, View.OnClickListener cancelar)
    {
        // Configuramos el diálogo
        DialogoConfirmacion frag = new DialogoConfirmacion();

        // Establecemos los textos
        frag.titulo  = titulo;
        frag.mensaje = mensaje;

        // Establecemos los eventos
        frag.aceptar  = aceptar;
        frag.cancelar = cancelar;

        // Mantenemos la instancia
        frag.setRetainInstance(true);

        // Devolvemos la instancia creada
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Establecemos el layout
        View view = inflater.inflate(R.layout.confirmacion, null);

        // Establecemos el título
        ((NoctuaTextView) view.findViewById(R.id.titulo)).setText(titulo);

        // Establecemos el contenido del mensaje
        ((NoctuaTextView) view.findViewById(R.id.mensaje)).setText(mensaje);

        // Evento cuando pulsamos sobre el aceptar
        Button buttonDialogYes = (Button) view.findViewById(R.id.aceptar);
        buttonDialogYes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                aceptar.onClick(v);
                dismiss();
            }
        });

        // Evento cuando pulsamos sobre el botón cancelar
        Button buttonDialogNo = (Button) view.findViewById(R.id.cancelar);
        buttonDialogNo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancelar.onClick(v);
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
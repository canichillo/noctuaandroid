package com.syp4.noctua.dialogos;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.syp4.noctua.R;
import com.syp4.noctua.ui.NoctuaTextView;

public class DialogoError extends DialogFragment
{
    // Variables del diálogo
    String titulo;
    String mensaje;

    /**
     * Constructor por defecto
     * @param titulo Título del mensaje
     * @param mensaje Contenido del mensaje
     */
    public static DialogoError newInstance(String titulo, String mensaje)
    {
        // Configuramos el diálogo
        DialogoError frag = new DialogoError();
        frag.titulo       = titulo;
        frag.mensaje      = mensaje;

        // Mantenemos la instancia
        frag.setRetainInstance(true);

        // Devolvemos la instancia creada
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Establecemos el layout
        View view = inflater.inflate(R.layout.error, null);

        // Establecemos el título
        ((NoctuaTextView) view.findViewById(R.id.titulo)).setText(titulo);

        // Establecemos el contenido del mensaje
        ((NoctuaTextView) view.findViewById(R.id.mensaje)).setText(mensaje);

        // Establecemos el evento
        view.findViewById(R.id.aceptar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
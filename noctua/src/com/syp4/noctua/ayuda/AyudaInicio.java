package com.syp4.noctua.ayuda;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.syp4.noctua.Helpers;
import com.syp4.noctua.R;

public class AyudaInicio extends Fragment
{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mantenemos la instancia
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Cargamos la vista
        View view = inflater.inflate(R.layout.ayudainicio, null);

        // Recortamos la imagen del logo
        ((ImageView) view.findViewById(R.id.ayuda1_logo)).setImageBitmap(Helpers.MascaraImagen(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.logo), R.drawable.mascaracompleta));

        // Devolvemos la vista
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
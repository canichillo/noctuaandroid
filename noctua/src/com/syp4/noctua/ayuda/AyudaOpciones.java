package com.syp4.noctua.ayuda;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.syp4.noctua.R;

public class AyudaOpciones extends Fragment
{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mantenemos la instancia
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Cargamos la vista
        View view = inflater.inflate(R.layout.ayudaopciones, null);

        // Devolvemos la vista
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
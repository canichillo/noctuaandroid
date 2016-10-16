package com.syp4.noctua.ayuda;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.syp4.noctua.Helpers;
import com.syp4.noctua.R;
import com.syp4.noctua.fragmentos.Login;

public class AyudaComenzar extends Fragment
{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mantenemos la instancia
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Cargamos la vista
        View view = inflater.inflate(R.layout.ayudacomenzar, null);

        // Establecemos el evento de comenzar la aplicación
        view.findViewById(R.id.comenzarapp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Indicamos que hemos visto la ayuda
                Helpers.setValor(getActivity(), "ayuda", "S");

                // Eliminamos la ayuda
                getActivity().getSupportFragmentManager().beginTransaction().remove(getActivity().getSupportFragmentManager().findFragmentById(R.id.content)).commit();

                // Cargamos el login
                Helpers.LoadFragment(getActivity(), new Login(), "Login");
            }
        });

        // Devolvemos la vista
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
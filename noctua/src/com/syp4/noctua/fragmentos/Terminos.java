package com.syp4.noctua.fragmentos;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.syp4.noctua.Helpers;
import com.syp4.noctua.R;

public class Terminos extends Fragment
{
    // Indica si debemos mostrar el botón del menú
    boolean menu;

    /**
     * Constructor por defecto
     * @param menu
     */
    public static Terminos newInstance(boolean menu)
    {
        Terminos frag = new Terminos();
        frag.menu = menu;
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mantenemos la instancia
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Cargamos la vista
        View view = inflater.inflate(R.layout.terminos, null);

        // Establecemos el título de la ficha
        Helpers.SetTitulo(getActivity(), getString(R.string.termsuse));

        // Si debemos mostrar el menú
        if (menu)
        {
            // Establecemos el icono de la izquierda
            Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.menu, Helpers.MENU);
        }
        // No debemos mostrar el menú
        else
        {
            // Establecemos el icono de la izquierda
            Helpers.IconoIzquierdaTitulo(getActivity(), R.drawable.back, Helpers.ATRAS);
        }

        // Escondemos el icono de la derecha
        Helpers.IconoDerechaTitulo(getActivity(), 0);

        // Mostramos la sombra
        Helpers.EstadoSombra(getActivity(), true);

        // Devolvemos la vista
        return view;
    }
}
package com.syp4.noctua.fragmentos;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.syp4.noctua.Helpers;
import com.syp4.noctua.R;
import com.syp4.noctua.ayuda.AyudaComenzar;
import com.syp4.noctua.ayuda.AyudaFacebook;
import com.syp4.noctua.ayuda.AyudaFavoritos;
import com.syp4.noctua.ayuda.AyudaInicio;
import com.syp4.noctua.ayuda.AyudaMenuIzquierdo;
import com.syp4.noctua.ayuda.AyudaOpciones;
import com.syp4.noctua.ayuda.AyudaRegistrar;
import com.syp4.noctua.ayuda.AyudaRegistrarBien;
import com.syp4.noctua.ayuda.AyudaTickets;
import com.syp4.noctua.utilidades.AdaptadorFragmentos;

public class Ayuda extends Fragment
{
    // Nuestra página
    ViewPager pager = null;
    AdaptadorFragmentos adaptadorFragmentos;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mantenemos la instancia
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Cargamos la vista
        View view = inflater.inflate(R.layout.ayuda, null);

        // Escondemos la cabecera
        Helpers.EstadoCabecera(getActivity(), false);

        // Escondemos la sombra
        Helpers.EstadoSombra(getActivity(), false);

        // Desactivamos la selección del menú a ninguna
        Helpers.ChangeMenuMode(getActivity(), Helpers.NINGUNO);

        // Nuestro paginado
        pager = (ViewPager) view.findViewById(R.id.pagerayuda);

        // Nuestro adaptador de fragmentos
        adaptadorFragmentos = new AdaptadorFragmentos(getActivity().getSupportFragmentManager());
        adaptadorFragmentos.addFragment(new AyudaInicio());
        adaptadorFragmentos.addFragment(new AyudaRegistrar());
        adaptadorFragmentos.addFragment(new AyudaRegistrarBien());
        adaptadorFragmentos.addFragment(new AyudaFacebook());
        adaptadorFragmentos.addFragment(new AyudaMenuIzquierdo());
        adaptadorFragmentos.addFragment(new AyudaOpciones());
        adaptadorFragmentos.addFragment(new AyudaFavoritos());
        adaptadorFragmentos.addFragment(new AyudaTickets());
        adaptadorFragmentos.addFragment(new AyudaComenzar());
        pager.setAdapter(adaptadorFragmentos);

        // Establecemos el evento para gestionar cuando hemos cambiado de fragmento de la ayuda
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // Limpiamos todos los botones
                ((ImageView) getActivity().findViewById(R.id.imgpag1)).setImageResource(R.drawable.viewpagerunselected);
                ((ImageView) getActivity().findViewById(R.id.imgpag2)).setImageResource(R.drawable.viewpagerunselected);
                ((ImageView) getActivity().findViewById(R.id.imgpag3)).setImageResource(R.drawable.viewpagerunselected);
                ((ImageView) getActivity().findViewById(R.id.imgpag4)).setImageResource(R.drawable.viewpagerunselected);
                ((ImageView) getActivity().findViewById(R.id.imgpag5)).setImageResource(R.drawable.viewpagerunselected);
                ((ImageView) getActivity().findViewById(R.id.imgpag6)).setImageResource(R.drawable.viewpagerunselected);
                ((ImageView) getActivity().findViewById(R.id.imgpag7)).setImageResource(R.drawable.viewpagerunselected);
                ((ImageView) getActivity().findViewById(R.id.imgpag8)).setImageResource(R.drawable.viewpagerunselected);
                ((ImageView) getActivity().findViewById(R.id.imgpag9)).setImageResource(R.drawable.viewpagerunselected);

                // Según la página seleccionada
                switch (position)
                {
                    case 0: ((ImageView) getActivity().findViewById(R.id.imgpag1)).setImageResource(R.drawable.viewpagerselected); break;
                    case 1: ((ImageView) getActivity().findViewById(R.id.imgpag2)).setImageResource(R.drawable.viewpagerselected); break;
                    case 2: ((ImageView) getActivity().findViewById(R.id.imgpag3)).setImageResource(R.drawable.viewpagerselected); break;
                    case 3: ((ImageView) getActivity().findViewById(R.id.imgpag4)).setImageResource(R.drawable.viewpagerselected); break;
                    case 4: ((ImageView) getActivity().findViewById(R.id.imgpag5)).setImageResource(R.drawable.viewpagerselected); break;
                    case 5: ((ImageView) getActivity().findViewById(R.id.imgpag6)).setImageResource(R.drawable.viewpagerselected); break;
                    case 6: ((ImageView) getActivity().findViewById(R.id.imgpag7)).setImageResource(R.drawable.viewpagerselected); break;
                    case 7: ((ImageView) getActivity().findViewById(R.id.imgpag8)).setImageResource(R.drawable.viewpagerselected); break;
                    case 8: ((ImageView) getActivity().findViewById(R.id.imgpag9)).setImageResource(R.drawable.viewpagerselected); break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

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
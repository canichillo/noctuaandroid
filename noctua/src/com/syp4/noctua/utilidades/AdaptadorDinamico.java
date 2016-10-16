package com.syp4.noctua.utilidades;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public abstract class AdaptadorDinamico<T, U> extends BaseAdapter {
    // Lista de elementos
    ArrayList<U> elementos;
    // Nuestro layout o diseño
    int layout;
    // Contexto
    Context contexto;

    /**
     * Constructor
     *
     * @param context  Contexto
     * @param lay      Diseño
     * @param elements Elementos
     */
    public AdaptadorDinamico(Context context, int lay, ArrayList<U> elements) {
        // Establecemos los valores
        contexto  = context;
        elementos = elements;
        layout    = lay;
    }

    /**
     * Actualiza los elementos del adaptador
     * @param elements Nuevos elementos
     */
    public void ActualizarElementos(ArrayList<U> elements)
    {
        elementos = elements;
    }

    /**
     * Elimina un elemento de la lista
     * @param elemento Elemento a eliminar
     * @return Si ha sido eliminado o no
     */
    public Boolean EliminarElemento(U elemento)
    {
        return elementos.remove(elemento);
    }

    /**
     * Devuelve la posición del elemento especificado
     * @param elemento Elemento a encontrar
     * @return Posición del elemento
     */
    public int PosicionElemento(Object elemento)
    {
        return elementos.indexOf(elemento);
    }

    /**
     * Gestiona la visualización de cada elemento
     *
     * @param i         Posición actual
     * @param view      Vista
     * @param viewGroup Grupo
     * @return Vista con los datos establecidos
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        T holder = null;

        if (view == null)
        {
            LayoutInflater vi = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view              = vi.inflate(layout, null);
            holder            = onViewHolder(view);
            view.setTag(holder);
        }
        else
        {
            holder = (T) view.getTag();
        }
        onEntrada(holder, elementos.get(i), view, i);
        return view;
    }

    @Override
    public int getCount() {
        return elementos.size();
    }

    @Override
    public U getItem(int posicion) {
        return elementos.get(posicion);
    }

    @Override
    public long getItemId(int posicion) {
        return posicion;
    }

    public abstract T onViewHolder(View view);

    /**
     * Devuelve cada una de las entradas con cada una de las vistas a la que debe de ser asociada
     *
     * @param holder Nuestro gestor de contenido
     * @param entrada La entrada que será la asociada a la view. La entrada es del tipo del paquete/handler
     * @param view    View particular que contendrá los datos del paquete/handler
     * @param posicion La posición del elemento en la lista
     */
    public abstract void onEntrada(T holder, U entrada, View view, int posicion);
}
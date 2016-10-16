package com.syp4.noctua.modelos;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Empresa {
    public int    ID;
    public String NOMBRE;
    public String DIRECCION;
    public Double DISTANCIA;
    public String LOGO;
    public Bitmap IMAGEN;
    public Double LATITUD;
    public Double LONGITUD;
    public String DESCRIPCION;
    public String FAVORITO;
    public String POBLACION;
    public String TWITTER;
    public String FACEBOOK;
    public String EMAIL;
    public String TELEFONO;
    public String WEB;
    public int    SEGUIDORES;

    public Empresa(int id, String nombre, String direccion, Double distancia, String logo,
                   Bitmap imagen, double latitud, double longitud, String descripcion, String favorito,
                   String poblacion, String twitter, String facebook, String email, String telefono, String web, int seguidores)
    {
        ID          = id;
        NOMBRE      = nombre;
        DIRECCION   = direccion;
        DISTANCIA   = distancia;
        LOGO        = logo;
        IMAGEN      = imagen;
        LATITUD     = latitud;
        LONGITUD    = longitud;
        DESCRIPCION = descripcion;
        FAVORITO    = favorito;
        POBLACION   = poblacion;
        TWITTER     = twitter;
        FACEBOOK    = facebook;
        EMAIL       = email;
        TELEFONO    = telefono;
        WEB         = web;
        SEGUIDORES  = seguidores;
    }

    public static Empresa ConsumirObjeto(JSONObject jo) {
        Empresa object = null;
        if (jo != null) {
            object = new Empresa(jo.optInt("id"), jo.optString("nombre"), jo.optString("direccion"),
                                 jo.optDouble("distancia"), jo.optString("logo"), null,
                                 jo.optDouble("latitud"), jo.optDouble("longitud"), jo.optString("descripcion"), jo.optString("favorito"),
                                 jo.optString("poblacion"), jo.optString("twitter"), jo.optString("facebook"),
                                 jo.optString("email"), jo.optString("telefonos"), jo.optString("web"), jo.optInt("seguidores"));
        }
        return object;
    }

    public static ArrayList<Empresa> ConsumirLista(JSONArray ja) {
        ArrayList<Empresa> objects = new ArrayList<Empresa>();
        if (ja != null) {
            for (int i = 0; i < ja.length(); i++)
                objects.add(Empresa.ConsumirObjeto(ja.optJSONObject(i)));
        }
        return objects;
    }
}

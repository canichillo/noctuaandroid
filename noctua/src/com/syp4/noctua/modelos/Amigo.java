package com.syp4.noctua.modelos;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Amigo
{
    public String IMAGEN;
    public String NOMBRE;
    public String ESTADO;
    public String SO;
    public String DISPOSITIVO;
    public int    AMISTAD;
    public int    USUARIO;

    public Amigo()
    {
    }

    public static Amigo ConsumirObjeto(JSONObject jo) {
        Amigo object = null;
        if (jo != null) {
            object = new Amigo();
            object.IMAGEN      = jo.optString("imagen");
            object.NOMBRE      = jo.optString("nombre");
            object.ESTADO      = jo.optString("estado");
            object.SO          = jo.optString("so");
            object.DISPOSITIVO = jo.optString("dispositivo");
            object.AMISTAD     = jo.optInt("amistad");
            object.USUARIO     = jo.optInt("usuario");
        }
        return object;
    }

    public static ArrayList<Amigo> ConsumirLista(JSONArray ja) {
        ArrayList<Amigo> objects = new ArrayList<Amigo>();
        if (ja != null) {
            for (int i = 0; i < ja.length(); i++)
                objects.add(Amigo.ConsumirObjeto(ja.optJSONObject(i)));
        }
        return objects;
    }
}

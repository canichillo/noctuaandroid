package com.syp4.noctua.modelos;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Seguidor
{
    public int    ID;
    public String IMAGEN;
    public String NOMBRE;
    public String SO;
    public String DISPOSITIVO;
    public String AMIGO;

    public Seguidor(int id, String imagen, String nombre,
                    String so, String dispositivo, String amigo)
    {
        ID           = id;
        IMAGEN       = imagen;
        NOMBRE       = nombre;
        SO           = so;
        DISPOSITIVO  = dispositivo;
        AMIGO        = amigo;
    }

    public static Seguidor ConsumirObjeto(JSONObject jo) {
        Seguidor object = null;
        if (jo != null) {
            object = new Seguidor(jo.optInt("id"), jo.optString("imagen"), jo.optString("nombre"),
                                  jo.optString("so"), jo.optString("dispositivo"), jo.optString("amigo"));
        }
        return object;
    }

    public static ArrayList<Seguidor> ConsumirLista(JSONArray ja) {
        ArrayList<Seguidor> objects = new ArrayList<Seguidor>();
        if (ja != null) {
            for (int i = 0; i < ja.length(); i++)
                objects.add(Seguidor.ConsumirObjeto(ja.optJSONObject(i)));
        }
        return objects;
    }
}

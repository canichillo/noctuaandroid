package com.syp4.noctua.modelos;

import com.syp4.noctua.Helpers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class Oferta
{
    public int      ID;
    public String   NOMBRE;
    public String   EMPRESA;
    public String   IMAGEN;
    public String   LOGO;
    public double   KILOMETROS;
    public Calendar INICIO;
    public Calendar FIN;
    public String   FAVORITO;
    public String   TIPOOFERTA;
    public String   TIPO;
    public String   DESCRIPCION;
    public String   ADQUIRIDA;
    public String   ESTADO;
    public boolean  ELIMINAR;

    public Oferta()
    {

    }

    public static Oferta ConsumirObjeto(JSONObject jo)
    {
        Oferta object = null;
        if (jo != null)
        {
            object             = new Oferta();
            object.ID          = jo.optInt("id");
            object.EMPRESA     = jo.optString("empresa").toUpperCase();
            object.IMAGEN      = jo.optString("imagen");
            object.LOGO        = jo.optString("logo");
            object.INICIO      = Helpers.StrToCalendar(jo.optString("inicio").replace("T", " "), "yyyy-MM-dd HH:mm:ss");
            object.FIN         = Helpers.StrToCalendar(jo.optString("fin").replace("T", " "), "yyyy-MM-dd HH:mm:ss");
            object.KILOMETROS  = jo.optDouble("distancia");
            object.NOMBRE      = jo.optString("nombre");
            object.FAVORITO    = jo.optString("favorito");
            object.TIPOOFERTA  = jo.optString("tipooferta");
            object.TIPO        = jo.optString("tipo");
            object.DESCRIPCION = jo.optString("descripcion");
            object.ESTADO      = jo.optString("estado");
            object.ADQUIRIDA   = jo.optString("adquirida");
            object.ELIMINAR    = true;
        }
        return object;
    }

    public static ArrayList<Oferta> ConsumirLista(JSONArray ja)
    {
        ArrayList<Oferta> objects = new ArrayList<Oferta>();
        for (int i = 0; i < ja.length(); i++)
            objects.add(Oferta.ConsumirObjeto(ja.optJSONObject(i)));
        return objects;
    }
}
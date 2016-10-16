package com.syp4.noctua.modelos;

import android.util.Log;

import java.util.Calendar;

import com.syp4.noctua.Helpers;

import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

public class Usuarios
{
    public String   NOMBRE;
    public String   USUARIO;
    public String   PASSWORD;
    public String   EMAIL;
    public Calendar NACIMIENTO;
    public String   GENERO;
    public String   FACEBOOK;
    public String   SO;
    public String   DISPOSITIVO;

    public Usuarios(String nombre, String usuario, String password,
                    String email, Calendar nacimiento, String genero,
                    String facebook, String so, String dispositivo)
    {
        NOMBRE      = nombre;
        USUARIO     = usuario;
        PASSWORD    = password;
        EMAIL       = email;
        NACIMIENTO  = nacimiento;
        GENERO      = genero;
        FACEBOOK    = facebook;
        SO          = so;
        DISPOSITIVO = dispositivo;
    }

    public static StringEntity ToStringEntity(Usuarios usuario)
    {
        // Formato JSON
        JSONObject json = new JSONObject();

        try
        {
            // Establecemos los valores
            json.put("nombre", usuario.NOMBRE);
            json.put("usuario", usuario.USUARIO);
            json.put("password", usuario.PASSWORD);
            json.put("email", usuario.EMAIL);
            json.put("genero", usuario.GENERO);
            json.put("nacimiento", Helpers.CalendarToStr(usuario.NACIMIENTO));
            json.put("facebook", usuario.FACEBOOK);
            json.put("so", usuario.SO);
            json.put("dispositivo", usuario.DISPOSITIVO);
        }
        catch (JSONException ex)
        {

        }

        try
        {
            // Salida
            StringEntity entidad = new StringEntity(json.toString(), HTTP.UTF_8);
            return entidad;
        }
        catch (Exception ex)
        {
           return null;
        }
    }
}
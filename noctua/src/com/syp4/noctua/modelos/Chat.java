package com.syp4.noctua.modelos;

import java.util.Calendar;

public class Chat
{
    public int      ID;
    public String   NOMBRE;
    public String   IMAGEN;
    public String   SO;
    public String   DISPOSITIVO;
    public Calendar FECHA;
    public int      DESTINATARIO;

    public Chat()
    {

    }

    public Chat(int id, String nombre, String imagen, String so, String dispositivo, Calendar fecha, int destinatario)
    {
        ID           = id;
        NOMBRE       = nombre;
        IMAGEN       = imagen;
        SO           = so;
        DISPOSITIVO  = dispositivo;
        FECHA        = fecha;
        DESTINATARIO = destinatario;
    }
}

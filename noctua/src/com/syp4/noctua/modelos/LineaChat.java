package com.syp4.noctua.modelos;

public class LineaChat
{
    public int    ID;
    public String TEXTO;
    public String ORIGEN;
    public String TIPO;

    public LineaChat(int id, String texto, String origen, String tipo)
    {
        ID     = id;
        TEXTO  = texto;
        ORIGEN = origen;
        TIPO   = tipo;
    }
}

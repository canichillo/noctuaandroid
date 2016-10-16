package com.syp4.noctua.modelos;

public class Direcciones
{
    public int    orden;
    public String direccion;
    public int    distancia;

    /**
     * Constructor por defecto
     * @param orden Orden
     * @param direccion Dirección
     * @param distancia Distancia
     */
    public Direcciones(int orden, String direccion, int distancia)
    {
        this.orden     = orden;
        this.direccion = direccion;
        this.distancia = distancia;
    }
}
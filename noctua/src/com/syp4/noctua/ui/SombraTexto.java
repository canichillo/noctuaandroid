package com.syp4.noctua.ui;

import android.graphics.Color;

public class SombraTexto
{
    public int m_nShadowColor;
    public int m_nShadowDx;
    public int m_nShadowDy;
    public int m_nShadowRadius;

    public SombraTexto()
    {
        m_nShadowColor  = Color.TRANSPARENT;
        m_nShadowDx     = 0;
        m_nShadowDy     = 0;
        m_nShadowRadius = 0;
    }

    public SombraTexto(int color, int dx, int dy, int radius)
    {
        m_nShadowColor  = color;
        m_nShadowDx     = dx;
        m_nShadowDy     = dy;
        m_nShadowRadius = radius;
    }
}

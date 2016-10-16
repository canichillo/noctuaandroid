package com.syp4.noctua.ui;

import android.content.*;
import android.content.res.*;
import android.graphics.Typeface;
import android.util.*;
import android.widget.*;

import com.syp4.noctua.R;

public class NoctuaButton extends Button {
    public NoctuaButton(Context context)
    {
        super(context);
    }

    public NoctuaButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // Establecemos la fuente
        EstablecerFuente(context, attrs);
    }

    public NoctuaButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        // Establecemos la fuente
        EstablecerFuente(context, attrs);
    }

    private void EstablecerFuente(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomFonts);
        String customFont = a.getString(R.styleable.CustomFonts_customFont);
        SetCustomFont(customFont);
        a.recycle();
    }

    public void SetCustomFont(String asset)
    {
        Typeface tf;
        try
        {
            tf = Typeface.createFromAsset(getContext().getAssets(), asset);
        }
        catch (Exception e)
        {
            return;
        }

        if (null == tf) return;

        setTypeface(tf);
    }

    /// <summary>
    /// Establece el icono que tendra el boton
    /// </summary>
    /// <param name="nIcono">Icono</param>
    public void ConfigurarIcono(int nIcono)
    {
        // Establecemos el icono
        setCompoundDrawablesWithIntrinsicBounds (nIcono, 0, 0, 0);
        setCompoundDrawablePadding(20);
    }
}

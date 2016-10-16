package com.syp4.noctua.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.syp4.noctua.R;

public class NoctuaCheckbox extends CheckBox {
    public int m_nMarcado    = 0;
    public int m_nDesmarcado = 0;

    public NoctuaCheckbox(Context context)
    {
        super(context);
    }

    public NoctuaCheckbox(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // Establecemos la fuente
        EstablecerFuente(context, attrs);

        // Establecemos el evento
        setOnCheckedChangeListener(eventoCheckedChange);
    }

    public NoctuaCheckbox(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        // Establecemos la fuente
        EstablecerFuente(context, attrs);

        // Establecemos el evento
        setOnCheckedChangeListener(eventoCheckedChange);
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
            Log.e("NoctuaButton", "Could not get Typeface: " + asset + " Error: " + e);
            return;
        }

        if (null == tf) return;

        setTypeface(tf);
    }

    /*
     * Configura el estado del checkbox
     */
    public void ConfiguracionCheckbox(int nMarcado, int nDesmarcado)
    {
        // Establecemos los valores
        m_nMarcado    = nMarcado;
        m_nDesmarcado = nDesmarcado;
    }

    /*
     * Refresca el estado del checkbox
     */
    public void Refrescar()
    {
        // Si hemos marcado el checkbox
        if (isChecked())
        {
            // Si hemos establecido la imagen de marcado
            if (m_nMarcado != 0)
                setButtonDrawable(m_nMarcado);
        }
        // No esta marcado
        else
        {
            // Si hemos establecido la imagen de desmarcado
            if (m_nDesmarcado != 0)
                setButtonDrawable(m_nDesmarcado);
        }
    }

    /*
     * Gestiona el evento del cambio de estado del Checkbox (marcado o no)
     */
    private OnCheckedChangeListener eventoCheckedChange = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // Si hemos marcado el checkbox
            if (isChecked)
            {
                // Si hemos establecido la imagen de marcado
                if (m_nMarcado != 0)
                    setButtonDrawable (m_nMarcado);
            }
            // No esta marcado
            else
            {
                // Si hemos establecido la imagen de desmarcado
                if (m_nDesmarcado != 0)
                    setButtonDrawable (m_nDesmarcado);
            }
        }
    };
}

package com.syp4.noctua.ui;

import android.content.*;
import android.content.res.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.*;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import android.graphics.Canvas;

import com.syp4.noctua.R;

public class NoctuaText extends EditText
{
        private int    m_nBckNormal     = 0;
        private int    m_nBckFocus      = 0;
        private String m_szTextoDefecto = "";
        private int    m_ColorTextoDef  = Color.TRANSPARENT;
        private int    m_ColorTextoNor  = Color.TRANSPARENT;
        private int    m_nIconoNormal   = 0;
        private int    m_nIconoFoco     = 0;
        private SombraTexto m_SombraD   = new SombraTexto();
        private SombraTexto m_SombraN   = new SombraTexto();

        public NoctuaText(Context context)
        {
            super(context);
        }

        public NoctuaText(Context context, AttributeSet attrs)
        {
            super(context, attrs);

            // Configuramos la fuente
            EstablecerFuente(context, attrs);
        }

        public NoctuaText(Context context, AttributeSet attrs, int defStyle)
        {
            super(context, attrs, defStyle);

            // Configuramos la fuente
            EstablecerFuente(context, attrs);
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);
        }

        private void EstablecerFuente(Context context, AttributeSet attrs)
        {
            try
            {
                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomFonts);
                String customFont = a.getString(R.styleable.CustomFonts_customFont);
                SetCustomFont(customFont);
                a.recycle();

                // Establecemos el evento del foco
                setOnFocusChangeListener(mTextFocus);
                setOnKeyListener(mKeyListener);
            }
            catch (Exception ex)
            {
                Log.e("Error NoctuaText", ex.getMessage());
            }
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
                Log.e("NoctuaText", "Could not get Typeface: " + asset + " Error: " + e);
                return;
            }

            if (null == tf) return;

            setTypeface(tf);
        }

        /// <summary>
        /// Establece el background del campo de texto
        /// </summary>
        /// <param name="nNormal">Background normal</param>
        /// <param name="nFocus">Background con el foco</param>
        public void ConfiguracionBackground(int nNormal, int nFocus)
        {
            // Establecemos los valores de los background
            m_nBckNormal = nNormal;
            m_nBckFocus  = nFocus;
        }

        /// <summary>
        /// Establecemos el texto por defecto
        /// </summary>
        /// <param name="szDefecto">Texto por defecto</param>
        /// <param name="defecto">Color del texto por defecto</param>
        /// <param name="normal">Color del texto normal</param>
        public void ConfiguracionTexto(String szDefecto, int defecto, int normal)
        {
            // Establecemos el texto por defecto
            m_szTextoDefecto = szDefecto;
            this.setText(m_szTextoDefecto);

            // Establecemos los valores por defecto
            m_ColorTextoDef = defecto;
            m_ColorTextoNor = normal;

            // Establecemos el color del texto por defecto, si esta establecido
            if (defecto != Color.TRANSPARENT)
                setTextColor(m_ColorTextoDef);
        }

        /**
         * Establece el texto por defecto
         * @param szDefecto Texto por defecto
         */
        public void TextoDefecto(String szDefecto)
        {
            m_szTextoDefecto = szDefecto;
        }

        /// <summary>
        /// Establece los iconos del campo de texto
        /// </summary>
        /// <param name="nNormal">Icono normal</param>
        /// <param name="nFocus">Icono con el foco</param>
        public void ConfiguracionIcono(int nNormal, int nFocus)
        {
            // Establecemos los valores de los iconos
            m_nIconoNormal = nNormal;
            m_nIconoFoco   = nFocus;
        }

        /**
         * Configuración de la sombra
         * @param defecto Sombra cuando es la sombra por defecto
         * @param normal Sombra cuando tiene la sombra nomrla
         */
        public void ConfiguracionSombra(SombraTexto defecto, SombraTexto normal)
        {
            m_SombraD = defecto;
            m_SombraN = normal;
        }

        /// <summary>
        /// Refresca los datos y configuraciones del campo de texto
        /// </summary>
        public void Refrescar()
        {
            // Establecemos el background del foco (solo si existe)
            if (m_nBckNormal != 0)
                setBackgroundResource(m_nBckNormal);

            // Si esta vacio
            if (getText().toString().trim().isEmpty() && !m_szTextoDefecto.isEmpty())
            {
                // Establecemos el texto por defecto
                setText(m_szTextoDefecto);

                // Establecemos el color del texto por defecto, solo si lo hemos establecido
                if (m_ColorTextoDef != Color.TRANSPARENT)
                    setTextColor(m_ColorTextoDef);
            }

            if (m_nIconoNormal != 0)
            {
                // Establecemos el icono
                setCompoundDrawablesWithIntrinsicBounds(0, 0, m_nIconoNormal, 0);
            }

            // Mostramos la sombra
            MostrarSombra();
        }

        /// <summary>
        /// Comprueba si tiene el valor por defecto
        /// </summary>
        /// <returns><c>true</c>, if defecto was esed, <c>false</c> otherwise.</returns>
        public Boolean EsDefecto()
        {
            return (m_szTextoDefecto.isEmpty()) || (getText().toString().trim().equals(m_szTextoDefecto));
        }

        /**
         * Muestra la sombra del texto
         */
        private void MostrarSombra()
        {
            // Si tiene el valor por defecto
            if (EsDefecto())
            {
                // Si tenemos establecida la sombra "defecto"
                if (m_SombraD.m_nShadowColor != Color.TRANSPARENT)
                {
                    setShadowLayer(m_SombraD.m_nShadowRadius, m_SombraD.m_nShadowDx,
                                   m_SombraD.m_nShadowDy, m_SombraD.m_nShadowColor);
                }
                else setShadowLayer(0, 0, 0, Color.TRANSPARENT);
            }
            else
            {
                // Si tenemos establecida la sombra "normal"
                if (m_SombraN.m_nShadowColor != Color.TRANSPARENT)
                {
                    setShadowLayer(m_SombraN.m_nShadowRadius, m_SombraN.m_nShadowDx,
                                   m_SombraN.m_nShadowDy, m_SombraN.m_nShadowColor);
                }
                else setShadowLayer(0, 0, 0, Color.TRANSPARENT);
            }
        }

        /// <summary>
        /// Evento que gestiona cuando hemos hecho seleccion en un campo de texto
        /// </summary>
        /// <param name="sender">Emisor</param>
        /// <param name="args">Argumentos</param>
        private OnFocusChangeListener mTextFocus = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                // Si estamos con el foco
                if (b)
                {
                    // Establecemos el background del foco (solo si existe)
                    if (m_nBckFocus != 0)
                        setBackgroundResource (m_nBckFocus);

                    // Si el texto es igual al que tiene por defecto
                    if (getText().toString().trim().equals(m_szTextoDefecto) && !m_szTextoDefecto.isEmpty())
                    {
                        // Limpiamos
                        setText("");

                        // Establecemos el color del texto normal, solo si lo hemos establecido
                        if (m_ColorTextoNor != Color.TRANSPARENT)
                            setTextColor (m_ColorTextoNor);
                    }
                }
                // No tenemos el foco
                else
                {
                    // Establecemos el background del foco (solo si existe)
                    if (m_nBckNormal != 0)
                        setBackgroundResource(m_nBckNormal);

                    // Si esta vacio
                    if (getText().toString().trim().equals("") && !m_szTextoDefecto.isEmpty())
                    {
                        // Establecemos el texto por defecto
                        setText(m_szTextoDefecto);

                        // Establecemos el color del texto por defecto, solo si lo hemos establecido
                        if (m_ColorTextoDef != Color.TRANSPARENT)
                            setTextColor (m_ColorTextoDef);
                    }
                }

                // Establecemos el icono
                IconoTextField (b);

                // Mostramos la sombra
                MostrarSombra();
            }
        };

        /// <summary>
        /// Evento que gestiona cuando se ha realizado un cambio en el campo de texto
        /// </summary>
        /// <param name="sender">Emisor</param>
        /// <param name="args">Argumentos</param>
        private OnKeyListener mKeyListener = new OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                // Si el texto es el por defecto
                if (getText().toString().trim().equals(m_szTextoDefecto))
                {
                    // Establecemos el color del texto por defecto, solo si lo hemos establecido
                    if (m_ColorTextoDef != Color.TRANSPARENT)
                        setTextColor (m_ColorTextoDef);

                    // Salimos
                    return false;
                }

                // Establecemos el color del texto normal, solo si lo hemos establecido
                if (m_ColorTextoNor != Color.TRANSPARENT)
                    setTextColor (m_ColorTextoNor);

                // Mostramos la sombra
                MostrarSombra();

                return false;
            }
        };

        /// <summary>
        /// Establece el icono del campo de texto
        /// </summary>
        /// <param name="bFocus">Si tiene o no el foco</param>
        private void IconoTextField(Boolean bFocus)
        {
            // Si hemos establecido la imagen con el foco
            if (bFocus && m_nIconoFoco != 0)
            {
                // Establecemos el icono
                setCompoundDrawablesWithIntrinsicBounds(0, 0, m_nIconoFoco, 0);
            }

            // Si hemos establecido la imagen normal
            if (!bFocus && m_nIconoNormal != 0)
            {
                // Establecemos el icono
                setCompoundDrawablesWithIntrinsicBounds(0, 0, m_nIconoNormal, 0);
            }
        }
}

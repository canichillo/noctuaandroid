package com.syp4.noctua;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;
import com.syp4.noctua.fragmentos.Amigos;
import com.syp4.noctua.fragmentos.Ayuda;
import com.syp4.noctua.fragmentos.Chats;
import com.syp4.noctua.fragmentos.DatosOferta;
import com.syp4.noctua.fragmentos.Invitaciones;
import com.syp4.noctua.fragmentos.Login;
import com.syp4.noctua.fragmentos.MapaOfertas;
import com.syp4.noctua.fragmentos.MisCupones;
import com.syp4.noctua.fragmentos.Ofertas;
import com.syp4.noctua.fragmentos.RoomChat;

import java.io.File;

public class Principal extends FragmentActivity
{
    // El contenido o fragmento cargado
    private Fragment mContent;

    // Etiqueta de la ventana
    private String tag = "";

    // Nuestro menú
    private ResideMenu resideMenu;

    // Nuestro usuario
    private ResideMenuItem usuarioItem;

    // La base de datos del módulo WAP
    private DatabaseHelper db;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // Configuramos el menú
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.stars);
        resideMenu.attachToActivity(this);
        resideMenu.clearDirections();

        // Ventana a abrir
        String ventana = getIntent().getStringExtra("ventana");

        // Cargamos el contenido
        if (savedInstanceState != null)
        {
            try
            {
                // Recargamos el contenido
                mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
            }
            catch (Exception ex) { }
        }

        // Si es la primera vez
        if (mContent == null)
        {
            // Si debemos abrir la ventana de login
            if (ventana.equals("Login")) 
            {
                // Creamos el contenido del login
                mContent = new Login();

                // Es el login
                tag = "Login";
            }

            // Si debemos abrir la ventana de ofertas
            if (ventana.equals("Ofertas")) 
            {
                // Creamos el contenido de los ofertas
                mContent = new Ofertas();

                // Son las ofertas
                tag = "Ofertas";
            }

            // Si es la ventana de Ayuda
            if  (ventana.equals("Ayuda"))
            {
                // Creamos el contenido de la ayuda
                mContent = new Ayuda();

                // Es la ayuda
                tag = "Ayuda";
            }

            // Si es un chat
            if (ventana.equals("Chat"))
            {
                // Código de la ventana a abrir (Código interno)
                int codigoVentana = Integer.parseInt(getIntent().getStringExtra("codigo"));
                mContent = RoomChat.newInstance(codigoVentana);

                // ID
                tag = "Chat" + codigoVentana;
            }

            // Si es una invitación
            if (ventana.equals("Invitacion"))
            {
                // Código de la ventana a abrir (Código interno)
                int codigoVentana = Integer.parseInt(getIntent().getStringExtra("codigo"));
                mContent = DatosOferta.newInstance(codigoVentana);

                // ID
                tag = "Invitacion" + codigoVentana;
            }

            // Si es una amistad
            if (ventana.equals("Amistad"))
            {
                mContent = Amigos.newInstance();;

                // ID
                tag = "Amigos";
            }
        }

        // Establecemos el contenido
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, mContent, tag)
                .commitAllowingStateLoss();

        // Establecemos los items del menú
        try {
            if (new File(Helpers.ImagenFotoPerfil(this)).exists()) {
                usuarioItem = new ResideMenuItem(this, Helpers.MascaraImagen(getResources(), BitmapFactory.decodeFile(Helpers.ImagenFotoPerfil(this)), R.drawable.mascaracompleta),
                        Helpers.getNombre(this), 999, R.layout.menulperfil);
                resideMenu.addMenuItem(usuarioItem, ResideMenu.DIRECTION_LEFT);
            }
            else
            {
                usuarioItem = new ResideMenuItem(this, R.drawable.loading, Helpers.getNombre(this), 999, R.layout.menulitem);
                resideMenu.addMenuItem(usuarioItem, ResideMenu.DIRECTION_LEFT);
            }
        }
        catch (Exception ex) {
            usuarioItem = new ResideMenuItem(this, R.drawable.loading, Helpers.getNombre(this), 999, R.layout.menulitem);
            resideMenu.addMenuItem(usuarioItem, ResideMenu.DIRECTION_LEFT);
        }

        resideMenu.addMenuItem(new ResideMenuItem(this, R.drawable.tickets, getString(R.string.ofertas), 1, R.layout.menulitem), ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(new ResideMenuItem(this, R.drawable.mimapa, getString(R.string.mapa), 2, R.layout.menulitem), ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(new ResideMenuItem(this, R.drawable.miscupones, getString(R.string.miscupones), 3, R.layout.menulitem), ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(new ResideMenuItem(this, R.drawable.misinvitaciones, "Invitaciones", 4, R.layout.menulitem), ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(new ResideMenuItem(this, R.drawable.chats, "Chats", 5, R.layout.menulitem), ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(new ResideMenuItem(this, R.drawable.amigos, "Amigos", 6, R.layout.menulitem), ResideMenu.DIRECTION_LEFT);

        // Establecemos el evento del menú
        resideMenu.setOnItemClickListener(new ResideMenu.OnItemClickListener() {
            @Override
            public void OnClick(View view) {
                MostrarFragmento(((ResideMenuItem) view).getID());
            }
        });

        // Establecemos el evento del botón del menú
        findViewById(R.id.imgtituloi).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Si no tiene valor el TAG salimos
                if (v.findViewById(R.id.imgtituloi).getTag() == null) return;

                // Si estamos en el menú
                if ((Integer)v.findViewById(R.id.imgtituloi).getTag() == Helpers.MENU)
                {
                    // Si el menú está mostrado, lo ocultamos
                    if (resideMenu.isOpened())
                        resideMenu.closeMenu();
                    // No está mostrado, lo mostramos
                    else resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                }

                // Si estamos en el botón hacia atrás
                if ((Integer)v.findViewById(R.id.imgtituloi).getTag() == Helpers.ATRAS)
                {
                    onBackPressed();
                }
            }
        });

        // Quitamos el icono de la derecha
        Helpers.IconoDerechaTitulo(this, 0);

        // Crea o abre la base de datos
        db = new DatabaseHelper(this);
    }

    /**
     * Cambia la vista según el elemento seleccionado
     * @param fragment
     * @param tag Etiqueta de la ventana
     */
    public void cambiarFragmento(Fragment fragment, String tag)
    {
        // Si no hay fragmento
        if (fragment == null) return;

        // Primeramente comprobamos si ya existe el fragmento
        getSupportFragmentManager().popBackStack(tag, getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);

        mContent = fragment;
        FragmentTransaction transaccion = getSupportFragmentManager().beginTransaction();
        transaccion.replace(R.id.content, fragment, tag);
        transaccion.addToBackStack(tag);
        transaccion.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaccion.commit();
        resideMenu.closeMenu();
    }

    /**
     * Accede a la base de datos
     * @return Base de datos
     */
    public DatabaseHelper getDb()
    {
        return db;
    }

    @Override
    public void onBackPressed()
    {
        try {
            if ((Integer) findViewById(R.id.imgtituloi).getTag() == Helpers.ATRAS) {
                Helpers.BackFragment(Principal.this);
            } else return;
        }
        catch (Exception ex) { return; }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.onInterceptTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    /**
     * Cambiamos el tipo del mostrado del menú
     */
    public void CambiarModoMenu(int modo)
    {
        // Limpiamos todas las direcciones
        resideMenu.clearDirections();

        if (modo == Helpers.IZQUIERDA) resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
        if (modo == Helpers.DERECHA) resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT);
        if (modo == Helpers.NINGUNO)
        {
            resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT);
            resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
        }
    }

    /**
     * Establece el valor del fragmento actual
     * @param fragment Fragmento actual
     */
    public void setmContent(Fragment fragment)
    {
        mContent = fragment;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        try {
            getSupportFragmentManager().putFragment(outState, "mContent", mContent);
            super.onSaveInstanceState(outState);
        }
        catch (Exception ex) { }
    }

    /**
     * Muestra el fragmento que queremos mostrar
     * @param id ID del fragmento
     */
    private void MostrarFragmento(int id)
    {
        // Fragmento a cargar
        Fragment fragment = null;
        // Etiqueta del fragmento
        String tag = "";

        switch (id)
        {
            case 1:   fragment = new Ofertas(); tag = "Ofertas"; break;
            case 2:   fragment = MapaOfertas.newInstance("Normales", false); tag = "MapaOfertas"; break;
            case 3:   fragment = MisCupones.newInstance(false); tag = "MisCupones"; break;
            case 4:   fragment = new Invitaciones(); tag = "Invitaciones"; break;
            case 5:   fragment = Chats.newInstance(); tag = "Chats"; break;
            case 6:   fragment = Amigos.newInstance(); tag = "Amigos"; break;
        }

        // Si hemos cargado un fragmento
        if (fragment == null) return;

        // Cargamos el fragmento especificado
        Helpers.LoadFragment(this, fragment, tag);
    }

    /**
     * Refresca el menú con los datos del nombre y imagen del usuario
     */
    public void RefrescarMenu()
    {
        try {
            // Establecemos la ruta
            String szRuta = Helpers.ImagenFotoPerfil(this);

            // Si tenemos imagen
            if (new File(szRuta).exists())
            {
                // La establecemos
                Bitmap m_bmpImagen = BitmapFactory.decodeFile(szRuta);
                m_bmpImagen        = Helpers.ResizedBitmap(m_bmpImagen, 100, true);
                usuarioItem.setIcon(Helpers.MascaraImagen(getResources(), m_bmpImagen, R.drawable.mascaracompleta));
            }
            else
            {
                // La establecemos
                Bitmap m_bmpImagen = BitmapFactory.decodeResource(getResources(), R.drawable.user);
                m_bmpImagen        = Helpers.ResizedBitmap(m_bmpImagen, 100, true);
                usuarioItem.setIcon(Helpers.MascaraImagen(getResources(), m_bmpImagen, R.drawable.mascaracompleta));
            }
        }
        catch (Exception ex) { }

        // Establecemos el nombre del usuario (perfíl)
        usuarioItem.setTitle(Helpers.getNombre(this));
    }

    /**
     * Muestra la cabecera
     * @param mostrar Si debemos o no mostrar la cabecera
     */
    public void EstadoCabecera(boolean mostrar)
    {
        // Si debemos mostrar la cabecera
        if (mostrar)
        {
            // Mostramos la cabecera
            findViewById(R.id.llHeader).setVisibility(View.VISIBLE);

            // Establecemos el margen superior del contenido
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) findViewById(R.id.content).getLayoutParams();
            params.topMargin = Helpers.ConvertDpToPixels(getResources(), 40);
            findViewById(R.id.content).setLayoutParams(params);
        }
        else
        {
            // Escondemos la cabecera
            findViewById(R.id.llHeader).setVisibility(View.GONE);

            // Establecemos el margen superior del contenido
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) findViewById(R.id.content).getLayoutParams();
            params.topMargin = 0;
            findViewById(R.id.content).setLayoutParams(params);
        }
    }

    /**
     * Establece el color de la cabecera
     * @param color Color de la cabecera
     */
    public void ColorCabecera(int color)
    {
        // Establecemos el color de la cabecera
        findViewById(R.id.llCabecera).setBackgroundColor(color);

        // Si es transparente
        if (color == Color.TRANSPARENT)
        {
            // Establecemos el margen superior del contenido
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) findViewById(R.id.content).getLayoutParams();
            params.topMargin = 0;
            findViewById(R.id.content).setLayoutParams(params);
        }
        else
        {
            // Establecemos el margen superior del contenido
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) findViewById(R.id.content).getLayoutParams();
            params.topMargin = 40;
            findViewById(R.id.content).setLayoutParams(params);
        }
    }

    /**
     * Establece el color de la sombra
     * @param color Color de la sombra
     */
    public void ColorSombra(int color)
    {
        findViewById(R.id.llsombra).setBackgroundColor(color);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Devuelve el menú de la aplicación
     * @return Menú de la aplicación
     */
    public ResideMenu getResideMenu()
    {
        return resideMenu;
    }
}

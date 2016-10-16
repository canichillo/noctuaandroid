package com.syp4.noctua;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.syp4.noctua.modelos.Chat;
import com.syp4.noctua.modelos.LineaChat;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class DatabaseHelper extends SQLiteOpenHelper
{
    // Versión de la base de datos
    private static final int DATABASE_VERSION = 1;

    // Nombre de la base de datos
    private static final String DATABASE_NAME = "NoctuaDataBase.db";

    // Creación de las tablas
    private static final String CREATE_TABLE_CHATS   = "CREATE TABLE CHATS (ID INTEGER PRIMARY KEY, NOMBRE VARCHAR(75), IMAGEN VARCHAR(100), SO VARCHAR(1), DISPOSITIVO VARCHAR(75), FECHA DATETIME, DESTINATARIO INTEGER)";
    private static final String CREATE_TABLE_CHATUNO = "CREATE TABLE CHATUNO (ID INTEGER PRIMARY KEY, CHAT INTEGER, MENSAJE TEXT, PROPIO CHAR(1), TIPO CHAR(1))";

    public DatabaseHelper(Context context) {
        super(context, Helpers.isExternalStorageMounted() ? Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.syp4.noctua/chats/" + DATABASE_NAME : DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // Creamos las tablas requeridas
        db.execSQL(CREATE_TABLE_CHATS);
        db.execSQL(CREATE_TABLE_CHATUNO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    /**
     * Crea un chat en la base de datos
     * @param chat Datos del chat
     */
    public void crearChat(Chat chat)
    {
        // Comprobamos si ya existe el chat
        if (ChatBD(chat.ID).ID != 0) return;

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("ID", chat.ID);
        values.put("NOMBRE", chat.NOMBRE);
        values.put("IMAGEN", chat.IMAGEN);
        values.put("SO", chat.SO);
        values.put("DISPOSITIVO", chat.DISPOSITIVO);
        values.put("FECHA", Helpers.CalendarToStr(chat.FECHA));

        // Insertamos la fila
        db.insert("CHATS", null, values);
        db.close();
    }

    /**
     * Devuelve el máximo ID de una tabla
     * @param tabla Tabla
     * @param id Campo ID
     * @return Máximo para el ID de la tabla
     */
    private int MaximoTabla(String tabla, String id)
    {
        try
        {
            // Creamos la consulta SQL
            String selectQuery = "SELECT MAX(" + id + ") + 1 FROM " + tabla;

            // Realizamos la petición a la base de datos
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c          = db.rawQuery(selectQuery, null);

            // Nos movemos a la primera posición
            if (c.moveToFirst())
            {
                return c.getInt(0);
            }
            else return 1;
        }
        catch (Exception ex) { return 1; }
    }

    /**
     * Actualiza un chat
     * @param id ID del chat
     * @param fecha Última fecha de actualización
     */
    public void actualizarChat(int id, Calendar fecha)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("FECHA", Helpers.CalendarToStr(fecha));

        // Actualizamos la fila
        db.update("CHATS", values, "ID = " + id, null);
        db.close();
    }

    /**
     * Elimina un chat
     * @param id ID del chat a eliminar
     */
    public void eliminarChat(int id)
    {
        // Leemos los datos del chat antes de borrarlo
        Chat chat = ChatBD(id);

        // Eliminamos la imagen
        try {
            // Si existe el archivo
            if (new File(chat.IMAGEN).exists())
                new File(chat.IMAGEN).delete();
        }
        catch (Exception ex) { }

        SQLiteDatabase db = this.getWritableDatabase();

        // Eliminamos el chat
        db.delete("CHATS", "ID = " + id, null);

        // Eliminamos el chat
        db.delete("CHATUNO", "CHAT = " + id, null);

        // Cerramos la conexión
        db.close();
    }

    /**
     * Crea un nuevo mensaje de chat
     * @param chat ID del chat
     * @param texto Texto del mensaje
     * @param propio Si el mensaje es propio
     */
    public void crearMensajeChat(int chat, String texto, String propio, String tipo)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("ID", MaximoTabla("CHATUNO", "ID"));
        values.put("CHAT", chat);
        values.put("MENSAJE", texto);
        values.put("PROPIO", propio);
        values.put("TIPO", tipo);

        // Insertamos la fila
        db.insert("CHATUNO", null, values);
        db.close();
    }

    /**
     * Devuelve las líneas del chat
     * @return Líneas del chat
     */
    public ArrayList<LineaChat> LineasChatBD(int id)
    {
        try
        {
            // Líneas del chat
            ArrayList<LineaChat> lineas = new ArrayList<LineaChat>();

            // Creamos la consulta SQL
            String selectQuery = "SELECT ID, MENSAJE, PROPIO, TIPO FROM CHATUNO WHERE CHAT = " + id + " ORDER BY ID";

            // Realizamos la petición a la base de datos
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c          = db.rawQuery(selectQuery, null);

            // Si hay datos
            if (c.moveToFirst())
            {
                // Mientras haya datos
                do {
                    lineas.add(new LineaChat(c.getInt(0), c.getString(1), c.getString(2), c.getString(3)));
                }while (c.moveToNext());
            }

            // Cerramos el cursor
            c.close();

            // Cerramos la base de datos
            db.close();

            // Devolvemos esas líneas
            return lineas;
        }
        catch (Exception ex) { return new ArrayList<LineaChat>(); }
    }

    /**
     * Devuelve los datos de un chat
     * @return Datos del chat
     */
    public Chat ChatBD(int id)
    {
        try
        {
            // Datos del chat
            Chat chat = new Chat();

            // Creamos la consulta SQL
            String selectQuery = "SELECT * FROM CHATS WHERE ID = " + id;

            // Realizamos la petición a la base de datos
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c          = db.rawQuery(selectQuery, null);

            // Si hay datos
            if (c.moveToFirst())
            {
                // Mientras haya datos
                do {
                    chat.ID          = c.getInt(0);
                    chat.NOMBRE      = c.getString(1);
                    chat.IMAGEN      = c.getString(2);
                    chat.SO          = c.getString(3);
                    chat.DISPOSITIVO = c.getString(4);
                    chat.FECHA       = Helpers.StrToCalendar(c.getString(5), "yyyy-MM-dd HH:mm:ss");
                }while (c.moveToNext());
            }

            // Cerramos el cursor
            c.close();

            // Cerramos la base de datos
            db.close();

            // Devolvemos los datos del chat
            return chat;
        }
        catch (Exception ex) { return new Chat(); }
    }

    /**
     * Devuelve los datos de un chat
     * @return Datos del chat
     */
    public boolean ExisteChatBD(int destinatario)
    {
        try
        {
            // Creamos la consulta SQL
            String selectQuery = "SELECT * FROM CHATS WHERE DESTINATARIO = " + destinatario;

            // Realizamos la petición a la base de datos
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c          = db.rawQuery(selectQuery, null);

            // Si hay datos
            if (c.moveToFirst())
            {
                // Cerramos el cursor
                c.close();

                // Cerramos la base de datos
                db.close();

                // Existe el chat
                return true;
            }

            // Cerramos el cursor
            c.close();

            // Cerramos la base de datos
            db.close();

            // No existe el chat
            return false;
        }
        catch (Exception ex) { return false; }
    }

    /**
     * Devuelve la lista de chats
     * @return Listado de chats
     */
    public ArrayList<Chat> ChatsBD()
    {
        try
        {
            // Listado de chats
            ArrayList<Chat> chats = new ArrayList<Chat>();

            // Creamos la consulta SQL
            String selectQuery = "SELECT * FROM CHATS";

            // Realizamos la petición a la base de datos
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c          = db.rawQuery(selectQuery, null);

            // Si hay datos
            if (c.moveToFirst())
            {
                // Mientras haya datos
                do {
                    chats.add(new Chat(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), Helpers.StrToCalendar(c.getString(5), "yyyy-MM-dd HH:mm:ss"), c.getInt(6)));
                }while (c.moveToNext());
            }

            // Cerramos el cursor
            c.close();

            // Cerramos la base de datos
            db.close();

            // Devolvemos esa lista de chats
            return chats;
        }
        catch (Exception ex) { return new ArrayList<Chat>(); }
    }

    /**
     * Cierra la base de datos
     */
    public void closeDB()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
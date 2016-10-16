package com.syp4.noctua.modelos;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ImagenGaleria implements Parcelable
{
    public String DESCRIPCION;
    public String ARCHIVO;

    public static final Parcelable.Creator<ImagenGaleria> CREATOR = new Parcelable.Creator<ImagenGaleria>() {

        @Override
        public ImagenGaleria createFromParcel(Parcel source) {
            return new ImagenGaleria(source);
        }

        @Override
        public ImagenGaleria[] newArray(int size) {
            return new ImagenGaleria[size];
        }

    };

    public static ImagenGaleria ConsumirObjeto(JSONObject jo)
    {
        ImagenGaleria object = null;
        if (jo != null)
        {
            object             = new ImagenGaleria();
            object.ARCHIVO     = jo.optString("archivo");
            object.DESCRIPCION = jo.optString("descripcion");
        }
        return object;
    }

    public static ArrayList<ImagenGaleria> ConsumirLista(JSONArray ja)
    {
        ArrayList<ImagenGaleria> objects = new ArrayList<ImagenGaleria>();
        for (int i = 0; i < ja.length(); i++)
            objects.add(ImagenGaleria.ConsumirObjeto(ja.optJSONObject(i)));
        return objects;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(DESCRIPCION);
        parcel.writeString(ARCHIVO);
    }

    public ImagenGaleria()
    {

    }

    private ImagenGaleria(Parcel in) {
        DESCRIPCION = in.readString();
        ARCHIVO     = in.readString();
    }
}

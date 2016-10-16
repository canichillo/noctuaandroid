package com.syp4.noctua.modelos;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Avatar {
    public String ID;

    public Avatar(String id)
    {
        ID = id;
    }

    public static Avatar ConsumirObjeto(JSONObject jo) {
        Avatar object = null;
        if (jo != null) {
            object = new Avatar(jo.optString("id"));
        }
        return object;
    }

    public static ArrayList<Avatar> ConsumirLista(JSONArray ja) {
        ArrayList<Avatar> objects = new ArrayList<Avatar>();
        if (ja != null) {
            for (int i = 0; i < ja.length(); i++)
                objects.add(Avatar.ConsumirObjeto(ja.optJSONObject(i)));
        }
        return objects;
    }
}

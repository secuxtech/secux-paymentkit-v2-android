package com.secuxtech.paymentkit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


class JsonUtils {

    public static Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> retMap = new HashMap<String, Object>();

        try{
            if(json != JSONObject.NULL) {
                retMap = toObjMap(json);
            }
        }catch (Exception e){

        }

        return retMap;
    }

    public static Map<String, Integer> toIntMap(JSONObject jsonobj) throws JSONException{
        Map<String, Integer> map = new HashMap<String, Integer>();
        Iterator<String> keys = jsonobj.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Integer value = Integer.valueOf(jsonobj.get(key).toString());

            map.put(key, value);
        }
        return map;
    }

    public static Map<String, Object> toObjMap(JSONObject jsonobj)  throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keys = jsonobj.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = jsonobj.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toObjMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }
            else if (value instanceof JSONObject) {
                value = toObjMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}

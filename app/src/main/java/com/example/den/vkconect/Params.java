package com.example.den.vkconect;

import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by den on 08.02.2018.
 */

public class Params {
    TreeMap<String, String> args = new TreeMap<String, String>();
    String method_name;

    public Params(String method_name) {
        this.method_name = method_name;
    }

    public void put(String param_name, String param_value) {
        if (param_value == null || param_value.length() == 0)
            return;
        args.put(param_name, param_value);
    }

    public void put(String param_name, Long param_value) {
        if (param_value == null)
            return;
        args.put(param_name, Long.toString(param_value));
    }

    public void put(String param_name, Integer param_value) {
        if (param_value == null)
            return;
        args.put(param_name, Integer.toString(param_value));
    }

    public void putDouble(String param_name, double param_value) {
        args.put(param_name, Double.toString(param_value));
    }

    public String getParamsString() {
        String params = "";
        for (Map.Entry<String, String> entry : args.entrySet()) {
            if (params.length() != 0)
                params += "&";
            params += (entry.getKey() + "=" + URLEncoder.encode(entry.getValue()));
        }
        return params;
    }

}
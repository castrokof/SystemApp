package com.example.systemapp.data.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

// El backend manda algunos campos booleanos como 0/1 (JSON NUMBER) en vez de true/false
// (JSON BOOLEAN) — el TypeAdapter<Boolean> por defecto de Gson es estricto y lanza
// IllegalStateException ante un NUMBER, tumbando el parseo de todo el array de medidoresout.
public class LenientBooleanAdapter extends TypeAdapter<Boolean> {

    @Override
    public void write(JsonWriter out, Boolean value) throws IOException {
        out.value(value);
    }

    @Override
    public Boolean read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        switch (token) {
            case NULL:
                in.nextNull();
                return null;
            case NUMBER:
                return in.nextInt() != 0;
            case STRING:
                String s = in.nextString();
                return "1".equals(s) || "true".equalsIgnoreCase(s);
            default:
                return in.nextBoolean();
        }
    }
}

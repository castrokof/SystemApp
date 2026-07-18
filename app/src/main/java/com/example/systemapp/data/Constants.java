package com.example.systemapp.data;

import com.example.systemapp.MainActivity;
import com.example.systemapp.BuildConfig;
import com.example.systemapp.MainActivity;

public class Constants {


    static public final String RESPONSE_CODE_STATUS_ERROR = "ERROR";
    static public final String RESPONSE_CODE_STATUS_OK = "OK";

    static public final String VALIDACION1 = "LA=LANT";
    static public final String VALIDACION2 = "LA<LANT";
    static public final String VALIDACION3 = "CA>165CP";
    static public final String VALIDACION4 = "CA<35CP";
    static public final String VALIDACION5 = "CA<50CP";

    static public final String IP_DEF = BuildConfig.IP_DEF;
    static public final String BASE_API = BuildConfig.BASE_API;

    static private MainActivity context;
    static private String ip;

    public Constants(MainActivity mainActivity, String ip) {
        this.context = mainActivity;
        this.ip = ip;
    }

    public static String BASE_URL = "https://"+IP_DEF+BASE_API;
}

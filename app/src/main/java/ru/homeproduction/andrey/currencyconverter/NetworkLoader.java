package ru.homeproduction.andrey.currencyconverter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkLoader {

    private static final String TAG = "DEBUG_TAG";
    private static final int READ_BUFFER_TIMEOUT = 15000;
    private static final int CONNECT_TIMEOUT = 15000;
    private static final String GET = "GET";

    public static InputStream getInputStreamFromUrl(String urlString) throws IOException {

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(READ_BUFFER_TIMEOUT);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setRequestMethod(GET);
        conn.setDoInput(true);
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }
}

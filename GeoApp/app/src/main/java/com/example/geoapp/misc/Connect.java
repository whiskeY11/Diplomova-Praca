package com.example.geoapp.misc;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Connect {
    public final static String REQUEST_RETURNED_ERROR = "Neúspešné";
    public final static String REQUEST_UNABLE_TO_EXEC = "Chyba";

    private final int DEFAULT_CONNECTION_TIMEOUT = 8000;
    private final int DEFAULT_READ_TIMEOUT = 8000;

    private String oauthToken = "";
    private String errorMessage = "";

    public String getToken() {
        return oauthToken;
    }

    public void setToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    public String getErrorMessage() { return errorMessage; }

    public String makeRequest(
            String link, String metoda, String f, String[] parametre,
            String[] params, boolean useOAuth, int[] timeouts)
    {
        HttpURLConnection conn = null;
        URL url;

        try {
            url = new URL(link + f);

            //nastavenie HttpURLConnection, skade sa budú dostávať dáta z Lumenu
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(timeouts[0]);
            conn.setReadTimeout(timeouts[1]);
            conn.setRequestMethod(metoda);
            //povoluje posielanie aj prijímanie
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            if (useOAuth) conn.setRequestProperty("Authorization", "Bearer " + getToken());

            if (params != null) {
                JSONObject data = new JSONObject();
                for (int i = 0; i < params.length; i++) {
                    String nazov = parametre[i];
                    data.put(nazov, params[i]);
                }

                //otvorí cestu pre posielanie dat
                OutputStream os = conn.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                bw.write(data.toString());
                bw.flush();
                bw.close();
                os.close();
            }

            conn.connect();
            int response_code = conn.getResponseCode();

            //v pripade erroru vypise chybu
            if(conn.getErrorStream() != null ) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String response = "";
                String responseRow;
                while ((responseRow = br.readLine()) != null) {
                    response += responseRow;
                }
                System.out.println(response);
                errorMessage = response;
            }

            System.out.println(response_code + " " + HttpURLConnection.HTTP_OK);

            if (response_code == HttpURLConnection.HTTP_OK) {    //ak je pripojenie uspesne
                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return (result.toString());    //posunie data do onPostExecute
            } else {
                return REQUEST_RETURNED_ERROR;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return REQUEST_UNABLE_TO_EXEC;
        } finally {
            if(conn != null)
                conn.disconnect();
        }
    }

    public String makeRequest(String link, String metoda, String f, String[] parametre, String[] params) {
        return makeRequest(link, metoda, f, parametre, params, false, new int[]{DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT});
    }

    public String makeRequest(String link, String metoda, String f, String[] parametre, String[] params, boolean useOAuth) {
        return makeRequest(link, metoda, f, parametre, params, useOAuth, new int[]{DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT});
    }
}

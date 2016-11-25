package com.peng.t00055798.teacherupdates.ReadFromServer;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Micheal Peng on 10/5/2016.
 */
public class URLClientManager {

    public static String getdatafromthisurl(String link) {

        BufferedReader bufreader = null;
        HttpURLConnection urlcon;
        URL url;
        StringBuilder SB;
        String oneLine;

        try {
            url = new URL(link);
            urlcon = (HttpURLConnection) url.openConnection();
            SB = new StringBuilder();
            bufreader = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));

            int i = 0;
            while ((oneLine = bufreader.readLine()) != null) {
                i++;
                SB.append(oneLine + "\n");
                Log.d("Line", i + "-" + oneLine);
            }
            return SB.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (bufreader != null) {

                try {
                    bufreader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

    }
}

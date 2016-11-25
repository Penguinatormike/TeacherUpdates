
package com.peng.t00055798.teacherupdates.ReadFromServer;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.peng.t00055798.teacherupdates.LocalCourseDatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by helmiligi on 1/30/2016.
 */
public class readCourse extends AsyncTask<String, String, String>  {
    String strFileContents = null;
    Context ctx;
    int id;
    String course,teacher,password,teacher_name;
    LocalCourseDatabaseAdapter myDb;

    public readCourse(Context context) {
        ctx = context;
    }
    @Override
    protected String doInBackground(String... params) {

        HttpURLConnection conn = null;
        String URL_LINK = "http://pengmichael.com/tu/course/course_JSON.php";
        try {
            //constants
            URL url = new URL(URL_LINK);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /*milliseconds*/);
            conn.setConnectTimeout(1000 /* milliseconds */);  // Temp. Fix for Issue 2. Reduce connection timeout
            conn.setUseCaches(false); // Temp. Fix for Issue 2. Clear Cache - Exception in Android L #79 - https://github.com/square/okio/issues/79
            conn.setRequestMethod("GET");
            conn.setAllowUserInteraction(false);
            //open
            conn.connect();

            int status = conn.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder("");
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();
                    Log.d("JSON", "JSON = " + sb.toString());
                    String testnull = sb.toString();
                    if (testnull.equals("null")){
                        Log.d("JSON","** ==========  N O     D A T  A    F O U N D   ======== **");
                    } else {
                        decodeJSON(sb.toString());
                    }
                    break;

            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }
    public void decodeJSON(String objs) {
        myDb = new LocalCourseDatabaseAdapter(ctx);
        myDb.open();
        try {
            Log.d("JSON", "** ========== R E A D I N G   J A S O N   A R R A Y ======== **");
            JSONArray jsonObjects = new JSONArray(objs);

            for (int i = 0; i < jsonObjects.length(); i++) {
                JSONObject oneObject = jsonObjects.getJSONObject(i);
                id = oneObject.getInt("id");
                course = oneObject.getString("course_name");
                teacher = oneObject.getString("teacher_id");
                password = oneObject.getString("password");
                teacher_name = oneObject.getString("first") + " " +
                        oneObject.getString("last");

                if (!myDb.contains(id)) {
                    // there are rows that contain the same id, so skip
                    Log.d("am I skipping? = ", course);
                    myDb.insertRow(id, course, teacher, password, teacher_name, 0);
                }else{
                    Log.d("skip adding = ", course);
                }
                Log.d("JSON", "** ID = " + String.valueOf(id));
                Log.d("JSON", "** course = " + course);
                Log.d("JSON", "** teacher = " + teacher);
                Log.d("JSON", "** teacher_name = " + teacher_name);
               // Log.d("JSON", "** pass = " + password);
                Log.d("JSON", "** ================== **");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(String... values) {

        super.onProgressUpdate(values);
        String msg = values[0];
    }
}
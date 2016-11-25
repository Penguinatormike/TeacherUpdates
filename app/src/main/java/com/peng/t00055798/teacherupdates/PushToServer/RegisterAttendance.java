package com.peng.t00055798.teacherupdates.PushToServer;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.peng.t00055798.teacherupdates.Activity.MainActivity;
import com.peng.t00055798.teacherupdates.LocalCourseDatabaseAdapter;
import com.peng.t00055798.teacherupdates.R;
import com.peng.t00055798.teacherupdates.RegisteredCourse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Micheal Peng on 10/19/2016.
 */
public class RegisterAttendance extends AsyncTask<String, String, String> {
    String strFileContents = null;
    Context ctx;
    ArrayList<RegisteredCourse> registeredCourses;
    String att;

    public RegisterAttendance(Context context){
        ctx = context;
        //registeredCourses = _registeredCourse;

    }

    @Override
    protected String doInBackground(String... params) {
        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;

        String URL_LINK = "http://pengmichael.com/tu/attendance/register_attendance.php";
        try {
            //constants

            java.util.Date dt = new java.util.Date();

            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String currentTime = sdf.format(dt);

            URL url = new URL(URL_LINK);
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("Token", params[0]);
            jsonObject.put("CRN", params[1]);
            jsonObject.put("address", params[2]);
            jsonObject.put("latitude", params[3]);
            jsonObject.put("longitude", params[4]);
            jsonObject.put("day_code", params[5]);
            jsonObject.put("date", currentTime);
            String course = params[6];

            String message = jsonObject.toString();
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /*milliseconds*/);
            conn.setConnectTimeout(1000 /* milliseconds */);  // Temp. Fix for Issue 2. Reduce connection timeout
            conn.setUseCaches(false); // Temp. Fix for Issue 2. Clear Cache - Exception in Android L #79 - https://github.com/square/okio/issues/79
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(message.getBytes().length);
            //make some HTTP header nicety
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            //open
            conn.connect();
            //setup send
            os = new BufferedOutputStream(conn.getOutputStream());
            os.write(message.getBytes());
            //clean up
            os.flush();
            //do somehting with response
            is = conn.getInputStream();
            //String contentAsString = readIt(is,len);
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream() );
            byte[] contents = new byte[1024];

            int bytesRead=0;

            while( (bytesRead = in.read(contents)) != -1){
                strFileContents = new String(contents, 0, bytesRead);
            }
            att = "\n" +"Attendance for "+ course + " at " + currentTime + " has been recorded" +"\n"+ readFromFile(ctx);


            Log.d("DEBUG", "Input Stream = " + strFileContents);
            Log.d("DEBUG", "Input param2 = " + params[2]);
            Log.d("DEBUG", "file write  = " + att);


            if(!strFileContents.matches("You have the wrong code, please try again")) {
                writeToFile(att, ctx);
                Log.d("DEBUG", "ere");
            }
            Log.d("DEBUG", "file read  = " + readFromFile(ctx));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            //clean up
            try {
                if ((os != null) && (is != null)) {
                    os.close();
                    is.close();
                }

                if (os == null) {

                    Log.d("DEBUG", " os NULL POINTERS");
                }

                if (is == null) {
                    Log.d("DEBUG", "is NULL POINTERS");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {                       // Temp. Fix for Issue 2. Surround with Try/Catch to through an exception when URL-connection can't be closed
                conn.disconnect();
                if (strFileContents != null) {
                    //publishProgress("Data Sent to Server");
                    Log.d("DEBUG", "Data Sent to Server");
                }
            } catch (Exception e) {
                e.printStackTrace();
                //publishProgress("Server connection problem. Please Try again!");
                Log.d("DEBUG", "HttpURLConnection didn't work as expected");
            }


            return "";
        }
    }
    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString + "\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(!strFileContents.matches("You have the wrong code, please try again")) {
            TextView txtView = (TextView) ((Activity) ctx).findViewById(R.id.rtv);
            txtView.setText(att);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }
}

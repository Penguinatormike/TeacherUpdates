package com.peng.t00055798.teacherupdates.PushToServer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.peng.t00055798.teacherupdates.Activity.MainActivity;
import com.peng.t00055798.teacherupdates.LocalCourseDatabaseAdapter;
import com.peng.t00055798.teacherupdates.R;
import com.peng.t00055798.teacherupdates.RegisteredCourse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Michael Peng on 10/7/2016.
 */

public class RegisterClass extends AsyncTask<String, String, String> {
    String strFileContents = null;
    Context ctx;
    LocalCourseDatabaseAdapter myDb;
    ArrayList<RegisteredCourse> registeredCourses;
    ListView lv;
    private ArrayAdapter<String> arrayAdapter;
    String[] osArray;
    List<String> courseList;
    List<Integer> CRN;
    List<String> teacherName;

    public RegisterClass(ArrayList<RegisteredCourse> _registeredCourse, Context context){
        ctx = context;
        registeredCourses = new ArrayList<RegisteredCourse>();
        lv = (ListView)((Activity) ctx).findViewById(R.id.navList);
        myDb = new LocalCourseDatabaseAdapter(ctx);
        myDb.open();

    }
    public boolean contains(ArrayList<RegisteredCourse> registeredCourses, RegisteredCourse new_course){
        for(int i = 0; i < registeredCourses.size(); i++){
            if(registeredCourses.get(i).getCrn() == new_course.getCrn()){
                return true;
            }
        }
        return false;

    }
    public void fillClass(){
        Cursor c = myDb.queryLastEvents();
        while(c.moveToNext()){
            int course_id = c.getInt(c.getColumnIndex(myDb.KEY_ROWID));
            String course = c.getString(c.getColumnIndexOrThrow(myDb.KEY_COURSE));
            String password = c.getString(c.getColumnIndexOrThrow(myDb.KEY_PASSWORD));
            String t_id = c.getString(c.getColumnIndexOrThrow(myDb.KEY_TEACHER));
            String teacher_name = c.getString(c.getColumnIndexOrThrow(myDb.KEY_TEACHER_NAME));
            int registered = c.getInt(c.getColumnIndexOrThrow(myDb.KEY_REGISTERED));

            courseList.add(course);
            CRN.add(course_id);
            teacherName.add(teacher_name);



            //add to hashset
            if(registered == 1) {
                Log.d("course re= ", registered+"");
                Log.d("course re= ", ""+course);
                Log.d("course re = ", ""+course_id);
                RegisteredCourse new_course = new RegisteredCourse(course_id, course, teacher_name);
                Log.d("boolean adding regis=", registeredCourses.contains(new_course)+"");

                if(!contains(registeredCourses, new_course))//latest new course will be added
                {
                    registeredCourses.add(new_course);
                }


            }



        }
        //addDrawerItems(registeredCourses);
        addDrawerItems();
        //registeredCourses.clear();
    }
    public void addDrawerItems() {
        //read_courses(); // from the server.. should only do once in awhile
        //fillClass();
        //you need to fix this where the registered courses already has
        RegisteredCourse[] RegisteredArray = registeredCourses.toArray(new RegisteredCourse[registeredCourses.size()]);
        osArray = new String[RegisteredArray.length+1];

        for(int i = 0; i < osArray.length-1; i++){
            osArray[i] = RegisteredArray[i].getCourse();
            Log.d("osArray in register = ", i + " " + osArray[i]);
        }
        osArray[osArray.length-1] = "Refresh";
        arrayAdapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, osArray);
        arrayAdapter.notifyDataSetChanged();
        lv.setAdapter(arrayAdapter);

    }
    @Override
    protected String doInBackground(String... params) {
        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;


        String URL_LINK = "http://pengmichael.com/tu/course/register_course.php";
        try {
            //constants


            URL url = new URL(URL_LINK);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Token", params[0]);
            jsonObject.put("CRN", params[1]);
            jsonObject.put("password",  params[2]);
            jsonObject.put("course_name",  params[3]);
            jsonObject.put("teacher_name",  params[4]);



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
            Log.d("DEBUG", "Input Stream = " + strFileContents);
            Log.d("DEBUG", "Input param2 = " + params[2]);
            //if response back from server is a success then change the registered table where
            //column registered from 0 to 1
            if(strFileContents.matches("You are now enrolled!")){
                //Update the registered id to 1
                Log.d("updating row = ", params[1]);

                myDb.updateRow(Long.parseLong(params[1]), 1);
//                RegisteredCourse new_course = new RegisteredCourse(Integer.parseInt(params[1]),params[3],params[4]);
//                registeredCourses.add(new_course);
//                Log.d("new courses", registeredCourses.toString());
                //registeredCourses.add();
                Log.d("hererere", params[1]);
                fillClass();
                //mActivity.addDrawerItems();
            }


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

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
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

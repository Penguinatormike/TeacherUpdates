package com.peng.t00055798.teacherupdates.Activity;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.peng.t00055798.teacherupdates.CourseAdapter;
import com.peng.t00055798.teacherupdates.Fragments.RegisteredFragment;
import com.peng.t00055798.teacherupdates.LocalCourseDatabaseAdapter;
import com.peng.t00055798.teacherupdates.Manifest;
import com.peng.t00055798.teacherupdates.PushToServer.RegisterAttendance;
import com.peng.t00055798.teacherupdates.R;
import com.peng.t00055798.teacherupdates.ReadFromServer.readCourse;
import com.peng.t00055798.teacherupdates.RegisteredCourse;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener, RegisteredFragment.OnFragmentInteractionListener {

    ProgressBar downloadprogress;
    String token;
    boolean thread_running = true;
    String contents ="COMP 2160";
    //for detecting first time launch
    SharedPreferences prefs = null;

    //database stuff
    LocalCourseDatabaseAdapter myDb;
    public static final String KEY_ROWID = "id";
    public static final String KEY_COURSE = "course_name";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_TEACHER = "teacher_id";
    int course_id;//verifying courses

    //displaying stuff nicely
    private RecyclerView mRecyclerView;
    private RecyclerView listRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter listAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.LayoutManager listLayoutManager;
    TextView et1;
    Button bt1;
    RegisteredCourse current_course;

    Context ctx;
    ArrayList<RegisteredCourse> registeredCourses;

    List<String> courseList;
    List<Integer> CRN;
    List<String> teacherName;
    DrawerLayout mDrawerLayout;

    String osArray[];
    CardView mDrawerList;
    ListView lv;
    private ArrayAdapter<String> arrayAdapter;

    String lat;
    int lastposition;
    String address;
    protected double latitude,longitude;
    protected boolean gps_enabled,network_enabled;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    TextView rtV;
    EditText code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;

        rtV = (TextView)findViewById(R.id.rtv);

        //open a connection to the database
        openDB();

        //used for keeping track of registered courses by the user, loads it up every launch
        courseList = new ArrayList();
        teacherName = new ArrayList();
        CRN = new ArrayList<>();

        //registeredCourses is the thing for keeping track of users course on create
        registeredCourses = new ArrayList<RegisteredCourse>();

        //navigation
        et1 = (TextView)findViewById(R.id.textView6);
        code= (EditText)findViewById(R.id.editText);
        lv = (ListView)findViewById(R.id.navList);
        bt1 = (Button)findViewById(R.id.recordButton);
        bt1.setEnabled(false);//ensure user does not send right away
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainActivity.this, "Time for an upgrade"+ position, Toast.LENGTH_SHORT).show();
                clearAdapter();
                lastposition = position;
                showClassAttendance(position,registeredCourses);
                //fillClass();
                //addDrawerItems();

            }
        });

        //course view
        mDrawerList = (CardView)findViewById(R.id.card_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //check to see if its first time opening app
        prefs = getSharedPreferences("com.peng.t00055798.teacherupdates", MODE_PRIVATE);

        //setup recycler view to display stuff on right menu
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new CourseAdapter(registeredCourses, mRecyclerView, ctx);
        mRecyclerView.setAdapter(mAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //pre loaded code by android studio
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //download bar for loading courses on startup
        downloadprogress = (ProgressBar) findViewById(R.id.progressBar);
        downloadprogress.setVisibility(View.INVISIBLE);

        //verify the user, every student has a token
        token =  prefs.getString("token", "DEFAULT");

        if(!Netisready()){
            Toast.makeText(MainActivity.this, "You need to be connected to the internet", Toast.LENGTH_LONG).show();
        }
        //verify that they actually did the check before they get here
        if(token.matches("null")){
            Log.d("Something went wrong", "Reinstall app");
        }else{
            //read courses to db on initial launch
            read_courses(); // fro
            fillClass();//fill the arrays

            //populate newest information from push notifications
            courseList.clear();
            CRN.clear();
            teacherName.clear();

            //check if there was a course already
            course_id = prefs.getInt("course_id",0);
            if (registeredCourses.size() == 0) {
                et1.setText("You don't have any courses, add a course by clicking on options, on the top right" + "\n");
            } else {
                //initialize first course that was picked to be the default when courses load

               showInterface(0,registeredCourses);
               current_course = registeredCourses.get(0);
            }


        }


    }

    // user's contacts
    public void getPermissionToReadUserLocation() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }

            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }else{

        }
    }
    public void getPermissionToWriteUserStorage() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }

            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }else{

        }
    }
    public void getPermissionToReadUserStorage() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }

            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }else{

        }
    }


    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == 1) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void RecordAttendance(View v){
        //if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            //Toast.makeText(MainActivity.this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
            //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
           // getCompleteAddressString(latitude,longitude);
            //return;
       // }
        RegisterAttendance reg = new RegisterAttendance(this);
        reg.execute(token, Integer.toString(current_course.getCrn()), address, Double.toString(latitude), Double.toString(longitude),code.getText().toString(), current_course.getCourse());
        bt1.setEnabled(false);
        if(!Netisready()){
            Toast.makeText(MainActivity.this, "You need to be connected to the internet", Toast.LENGTH_LONG).show();
        }
        Timer buttonTimer = new Timer();
        buttonTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        bt1.setEnabled(true);
                    }
                });
            }
        }, 5000);



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
    private void clearFile(Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write("");
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("My Current ", "" + strReturnedAddress.toString());
            } else {
                Log.w("My Current ", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current ", "Canont get Address!");
        }
        return strAdd;
    }
    public void showInterface(int position, ArrayList<RegisteredCourse> registeredCourses){
        //show button and text view


        String builder = "<b>Sign in for</b>: " + registeredCourses.get(position).getCourse() + "<br>";
        builder += "<b>CRN:</b> " + registeredCourses.get(position).getCrn() + "<br>";
        builder += "<b>Teacher</b>: " + registeredCourses.get(position).getTeacher()+ "<br>";

        //debugging purposes, remove later
        if(address != null) {
            //builder += "Your Location: " + address;
        }

        et1.setText(Html.fromHtml(builder));
        et1.setVisibility(View.VISIBLE);
        code.setVisibility(View.VISIBLE);
        rtV.setVisibility(View.VISIBLE);
        bt1.setVisibility(View.VISIBLE);
        showFragment();


    }
    public void clearAdapter() {
        mAdapter = new CourseAdapter(registeredCourses, mRecyclerView, ctx);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
    }


    private void showClassAttendance(int position, ArrayList<RegisteredCourse> registeredCourses) {
        if(position+1 > registeredCourses.size()){
            fillClass();//refresh was clicked
        }else{
            showInterface(position,registeredCourses);
            current_course = registeredCourses.get(position);
            //send this to the server
        }
    }

    public void addDrawerItems() {
        //read_courses(); // from the server.. should only do once in awhile
        //fillClass();
        //you need to fix this where the registered courses already has
        RegisteredCourse[] RegisteredArray = registeredCourses.toArray(new RegisteredCourse[registeredCourses.size()]);
        osArray = new String[RegisteredArray.length+1];

        for(int i = 0; i < osArray.length-1; i++){
            osArray[i] = RegisteredArray[i].getCourse();
            Log.d("osArray = ", i + " " + osArray[i]);
        }
        osArray[osArray.length-1] = "Refresh";
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        arrayAdapter.notifyDataSetChanged();
        lv.setAdapter(arrayAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            Intent myIntent = new Intent(MainActivity.this, FirstLaunchActivity.class);
            MainActivity.this.startActivity(myIntent);
        }
        //setContentView(R.layout.activity_main);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
        RegisteredFragment newFragment = new RegisteredFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    public void hideFragment(){

        View frag = findViewById(R.id.fragment);
        frag.setVisibility(View.GONE);//Or View.INVISBLE
    }
    public void showFragment(){

        View frag = findViewById(R.id.fragment);
        frag.setVisibility(View.VISIBLE);//Or View.INVISBLE
    }
    private class Connect extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadprogress.setVisibility(View.VISIBLE);
            //font.setText("Connecting...");
            rtV.setText(readFromFile(ctx));
            readCourse r = new readCourse(ctx);
            // Here, thisActivity is the current activity
            getPermissionToReadUserLocation();
            getPermissionToReadUserStorage();
            getPermissionToWriteUserStorage();
            r.execute();
        }

        @Override
        protected String doInBackground(String... links) {

            if (Netisready()){

                // publishProgress("Loading..");
                //contents = URLClientManager.getdatafromthisurl(links[0]);
                //decodeJSON(contents);


            }else {
                publishProgress("Not connected to internet");

            }

            return contents;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            downloadprogress.setVisibility(View.INVISIBLE);
            if ( ContextCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
                    //Toast.makeText(MainActivity.this, "Got Location, Ensure location is turned on.", Toast.LENGTH_LONG).show();
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, MainActivity.this);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, MainActivity.this);
                    //getCompleteAddressString(latitude,longitude);
                    return;
             }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //font.setText("Connecting...");
            Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_SHORT).show();
        }

    }

    protected Boolean Netisready(){
        ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo localnetwork = conManager.getActiveNetworkInfo();
        if (localnetwork != null && localnetwork.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
    private void openDB() {
        myDb = new LocalCourseDatabaseAdapter(this);
        myDb.open();
    }
    private void closeDB() {
        myDb.close();
    }

    public void read_courses(){

        Connect getPage = new Connect();
        getPage.execute();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
                Log.d("course = ", registered+"");
                Log.d("course = ", ""+course);
                Log.d("course = ", ""+course_id);
                RegisteredCourse new_course = new RegisteredCourse(course_id, course, teacher_name);
                Log.d("boolean adding =", registeredCourses.contains(new_course)+"");

                if(!contains(registeredCourses, new_course))//latest new course will be added
                {
                    registeredCourses.add(new_course);
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("course_id", new_course.getCrn());
                editor.commit();// commit is important here.

                et1.setText("");
            }



        }
        //addDrawerItems(registeredCourses);
        addDrawerItems();
        //registeredCourses.clear();
    }



    public boolean contains(ArrayList<RegisteredCourse> registeredCourses, RegisteredCourse new_course){
        for(int i = 0; i < registeredCourses.size(); i++){
            if(registeredCourses.get(i).getCrn() == new_course.getCrn()){
                return true;
            }
        }
        return false;

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //need to use pop up box or something here
        if (id == R.id.add_course) {
            //DialogFragment newFragment = new RegisterCourseDialogFragment();
            //newFragment.show(getFragmentManager(), token);
            rtV.setVisibility(View.INVISIBLE);
            code.setVisibility(View.INVISIBLE);
            bt1.setVisibility(View.INVISIBLE);
            et1.setVisibility(View.INVISIBLE);
            hideFragment();
            FragmentManager fm = getFragmentManager();

            read_courses(); // from the server.. should only do once in awhile
            //I am so sorry, for some reason this fixes the double
            fillClass();//fill the arrays
            courseList.clear();
            CRN.clear();
            teacherName.clear();

            fillClass();//fill the arrays
            //addDrawerItems();

            Integer[] id_array = CRN.toArray(new Integer[0]);
            String[] course_array = courseList.toArray(new String[0]);
            String[] teacher_array = teacherName.toArray(new String[0]);
            //Log.d("array = ", ""+array.length);

            mAdapter = new CourseAdapter(course_array, id_array, teacher_array, mRecyclerView, ctx);
            mRecyclerView.setAdapter(mAdapter);

            courseList.clear();
            CRN.clear();
            teacherName.clear();

            return true;
        }else if(id == R.id.clear_registry){
            clearFile(this);
            rtV.setText(readFromFile(ctx));
        }/*else if(id == R.id.change_name){

        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


/*
        //need to generate courses here
        if (id == R.id.nav_camera) {
            try {
                Cursor cursor = myDb.queryLastEvents();
                String data ="";
                int registered = 0;
                while (cursor.moveToNext()) {
                    registered = cursor.getInt(cursor.getColumnIndex(myDb.KEY_REGISTERED));
                    if(registered == 1) {

                        Log.d("registered? = ", registered + "");
                    }
                }
                Log.d("here","");
                //Log.d("registered? = ", data);

            }catch(Exception e){
                Log.e("exception triggered", e.toString());
            }
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        bt1.setEnabled(true);
        Log.d("location change :", "Your Location: " + latitude + " " + longitude);
        if(address == null) {

            address = getCompleteAddressString(latitude, longitude);
            //et1.append("Your Location: " + latitude + " " + longitude);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }


}


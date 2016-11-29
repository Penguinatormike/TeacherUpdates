package com.peng.t00055798.teacherupdates.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.peng.t00055798.teacherupdates.PushToServer.SendPersonalInfo;
import com.peng.t00055798.teacherupdates.R;

/*
    On first launch check if user has internet. If not prompt them that they need it.

    If they have internet get information from them and send it to the server


 */
public class FirstLaunchActivity extends AppCompatActivity {


    EditText firstnameET,lastnameET,studentidET;
    SharedPreferences prefs = null;
    boolean thread_running = true;
    String Token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_launch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //subscribe to get an account
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        Token =   FirebaseInstanceId.getInstance().getToken();
        Log.d("debug token", " " + Token);
        Thread t = new Thread(new Runnable(){
            public void run(){
                while(thread_running){
                    Token = FirebaseInstanceId.getInstance().getToken();
                    if(Token != null){
                        Log.d("Device Token is   ", Token);

                        thread_running = false;
                    }else{
                        Log.d("token not loaded", "-");
                    }
                    try{
                        Thread.sleep(1000);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });t.start();
        //check network connection
        if(!isNetworkConnected()){
            Context context = getApplicationContext();
            CharSequence text = "You must be connected to the internet first!!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        firstnameET = (EditText)findViewById(R.id.firstname);
        lastnameET = (EditText)findViewById(R.id.lastname);
        studentidET = (EditText)findViewById(R.id.studentid);

        studentidET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sign_up();
                    handled = true;
                }
                return handled;
            }
        });

        prefs = getSharedPreferences("com.peng.t00055798.teacherupdates", MODE_PRIVATE);

    }
    public void sign_up_botton(View v){
        sign_up();

    }
    public void sign_up(){

        if(firstnameET.getText().toString().matches("") ||
                lastnameET.getText().toString().matches("") ||
                studentidET.getText().toString().matches("")){
            Context context = getApplicationContext();
            CharSequence text = "All fields must be filled out!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        }
        else if(!isNetworkConnected()){
            Context context = getApplicationContext();
            CharSequence text = "You must be connected to the internet first!!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        else {
                //subscribe to get an account
            FirebaseMessaging.getInstance().subscribeToTopic("test");
            Token =   FirebaseInstanceId.getInstance().getToken();

            Thread t = new Thread(new Runnable(){
                public void run(){
                    while(thread_running){
                        Token = FirebaseInstanceId.getInstance().getToken();
                        if(Token != null){
                            Log.d("Device Token is   ", Token);

                            thread_running = false;
                        }else{
                            Log.d("token not loaded", "-");
                        }
                        try{
                            Thread.sleep(1000);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            });t.start();

                //send code to server

                SendPersonalInfo sendOBJ = new SendPersonalInfo();
                sendOBJ.execute(firstnameET.getText().toString(), lastnameET.getText().toString(),
                        studentidET.getText().toString(),Token );

                CharSequence text = "Sign up success!";
                int duration = Toast.LENGTH_SHORT;
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();


                //make sure app doesn't show this activity again
                prefs.edit().putBoolean("firstrun", false).commit();

                //this will be used to access token to verify user
                //prefs.edit().putString("token",Token).apply();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("token", Token);
                editor.commit();// commit is important here.

                Log.d("token in first =", prefs.getString("token", "DEFAULT"));
                //send them back to main page
                Intent myIntent = new Intent(FirstLaunchActivity.this, MainActivity.class);
                FirstLaunchActivity.this.startActivity(myIntent);
                finish();
        }



    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}

package com.peng.t00055798.teacherupdates.PushToServer;


import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 * Created by t00055798 on 9/9/2016.
 *
 * This class is for updating the client tokens to recieve the messages sent by server
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService{

    @Override
    public void onTokenRefresh(){
        String token = FirebaseInstanceId.getInstance().getToken();
        registerToken(token);
    }

    private void registerToken(String token){
        Log.d("token", token);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Token", token)
                //.add("CRN", CRN)
                .build();
        //registers users
        Request request = new Request.Builder()
                .url("http://pengmichael.com/tu/sign_in/register.php")
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}

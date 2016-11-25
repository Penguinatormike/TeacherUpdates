package com.peng.t00055798.teacherupdates.ReadFromServer;

/**
 * Created by t00055798 on 9/9/2016.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;
import com.peng.t00055798.teacherupdates.Activity.MainActivity;
import com.peng.t00055798.teacherupdates.R;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String message = remoteMessage.getData().get("message");
        Log.d("message= " , message);
        if(message.matches("Incorrect password, please try again")
                || message.matches("You are now enrolled!")
                || message.matches(("You have recorded your attendance for the day"))
                || message.matches(("You have the wrong code, please try again")))
        {
            showVerifyClass(message);
        }else{
            showNotification(message);
        }
    }

    private void showVerifyClass(String message){
        Log.d("verify class service =", message);

        ShowToastInClass st = new ShowToastInClass();
        st.execute(message);


    }


    private void showNotification(String message){
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("Teacher Update!")
                .setContentText(message)
                .setSmallIcon(R.drawable.class_room_icon)
                .setContentIntent(pendingIntent);

        sound(builder);

        NotificationManager manager
                = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0, builder.build());




    }
    private void sound(NotificationCompat.Builder builder){

        //Vibration
        builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        //LED
        builder.setLights(Color.RED, 3000, 3000);

        //Tone
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);
    }

    private class ShowToastInClass extends AsyncTask<String, String, String> {

        String message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            Context context = getApplicationContext();
            CharSequence text = message;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        @Override
        protected String doInBackground(String... params) {
            message = params[0];
            return null;
        }
    }


}

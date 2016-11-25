package com.peng.t00055798.teacherupdates.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.peng.t00055798.teacherupdates.Activity.MainActivity;
import com.peng.t00055798.teacherupdates.PushToServer.RegisterClass;
import com.peng.t00055798.teacherupdates.R;
import com.peng.t00055798.teacherupdates.RegisteredCourse;

import java.util.ArrayList;


/**
 * Created by t00055798 on 9/13/2016.
 *
 * This class is a dialog builder for registering for a course
 */
public class RegisterCourseDialogFragment extends DialogFragment {
    SharedPreferences prefs = null;
    String token;
    Context context;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction



        prefs = getActivity().getSharedPreferences("com.peng.t00055798.teacherupdates", getActivity().MODE_PRIVATE);
        final String course_name = getArguments().getString("course_name");
        final String teacher_name = getArguments().getString("teacher_name");
        final ArrayList<RegisteredCourse> registeredCourses  = getArguments().getParcelableArrayList("list");


        LayoutInflater inflater = getActivity().getLayoutInflater();
        //final EditText editText = (EditText) view.findViewById(R.id.course);

        final int CRN = getArguments().getInt("CRN");
        token = prefs.getString("token", "");
        Log.d("token = ", token);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText editText = new EditText(getActivity());
        builder.setView(editText); // uncomment this line
        builder.setTitle("Enter password for " + course_name + "(CRN: "+CRN+")");
        builder.setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String password = String.valueOf(editText.getText());
                        Log.d("edittext = ",password);
                        //Log.d("here = ","");

                        RegisterClass r = new RegisterClass(registeredCourses, getActivity());
                        r.execute(token, CRN+"", password,teacher_name, course_name);


                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
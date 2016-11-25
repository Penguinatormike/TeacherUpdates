package com.peng.t00055798.teacherupdates;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peng.t00055798.teacherupdates.Fragments.RegisterCourseDialogFragment;

import java.util.ArrayList;

/**
 * Created by Micheal Peng on 10/10/2016.
 */
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private String[] course;
    private Integer[] CRN;
    private String[] teacher;
    RecyclerView recyclerView;
    Context context;
    ArrayList<RegisteredCourse> registeredCourses;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView courseTextView;
        public TextView idTextView;
        public TextView teacherTextView;
        View itemView;

        public ViewHolder(View v) {
            super(v);
            courseTextView = (TextView) v.findViewById(R.id.textView);
            idTextView = (TextView) v.findViewById(R.id.textView2);
            teacherTextView = (TextView) v.findViewById(R.id.textView3);
            itemView = v;
        }
    }

    //for the side bar
    public CourseAdapter(ArrayList<RegisteredCourse> _registeredCourses,  RecyclerView _recyclerView, Context _ctx){
        recyclerView = _recyclerView;
        context = _ctx;
        registeredCourses = _registeredCourses;
    }

    //for the menu
    // Provide a suitable constructor (depends on the kind of dataset)
    public CourseAdapter(String[]_course, Integer[] _CRN, String[] _teacher, RecyclerView _recyclerView, Context ctx) {
        course = _course;
        CRN = _CRN;
        teacher = _teacher;
        recyclerView = _recyclerView;
        context = ctx;


    }

    // Create new views (invoked by the layout manager)
    @Override
    public CourseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {


        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view, parent, false);
        //v.setOnClickListener(new MyOnClickListener());
        // create a new view

        // set the view's size, margins, paddings and layout parameters
        //...
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.courseTextView.setText(course[position]);
        holder.idTextView.setText("CRN: "+(CRN[position]));
        holder.teacherTextView.setText(teacher[position]);
        final int itemPosition = holder.getAdapterPosition();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Put your code here


                //int itemPosition = recyclerView.indexOfChild(v);
                int code = CRN[itemPosition];
                String teacher_name = teacher[itemPosition];
                String course_name = course[itemPosition];

                final Activity activity = (Activity) context;

                // Return the fragment manager
                DialogFragment newFragment = new RegisterCourseDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("CRN", code);
                bundle.putString("teacher", teacher_name);
                bundle.putString("course_name", course_name);
                bundle.putParcelableArrayList("courses", registeredCourses);
                newFragment.setArguments(bundle);
                newFragment.show(activity.getFragmentManager(), "missiles");

                Log.e("Clicked and Position is",String.valueOf(itemPosition));
            }
        });


    }

    public void clearData() {
        recyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
    }
    class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }

//    class SideBaronClickListener implements View.OnClickListener {
//        @Override
//        public void onClick(View v) {
//            int itemPosition = recyclerView.indexOfChild(v);
//            int code = CRN[itemPosition];
//            String teacher_name = teacher[itemPosition];
//            String course_name = course[itemPosition];
//
//            final Activity activity = (Activity) context;
//
//            // Return the fragment manager
//            DialogFragment newFragment = new RegisterCourseDialogFragment();
//            Bundle bundle = new Bundle();
//            bundle.putInt("CRN", code);
//            bundle.putString("teacher", teacher_name);
//            bundle.putString("course_name", course_name);
//            newFragment.setArguments(bundle);
//            newFragment.show(activity.getFragmentManager(), "missiles");
//
//            Log.e("Clicked and Position is",String.valueOf(itemPosition));
//        }
//    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (course == null)
            return 0;
        else
            return  course.length;
    }

}

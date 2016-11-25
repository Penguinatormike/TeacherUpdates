package com.peng.t00055798.teacherupdates;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Micheal Peng on 10/12/2016.
 */
public class RegisteredCourse implements Parcelable{
    private int crn;
    private String course;
    private String teacher;

    public RegisteredCourse(int _crn, String _course, String _teacher){

        crn = _crn;
        course = _course;
        teacher = _teacher;
    }

    public int getCrn(){
        return crn;
    }
    public String getCourse(){
        return course;
    }
    public String getTeacher(){
        return teacher;
    }

    protected RegisteredCourse(Parcel in) {
        crn = in.readInt();
        course = in.readString();
        teacher = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(crn);
        dest.writeString(course);
        dest.writeString(teacher);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RegisteredCourse> CREATOR = new Parcelable.Creator<RegisteredCourse>() {
        @Override
        public RegisteredCourse createFromParcel(Parcel in) {
            return new RegisteredCourse(in);
        }

        @Override
        public RegisteredCourse[] newArray(int size) {
            return new RegisteredCourse[size];
        }
    };
}

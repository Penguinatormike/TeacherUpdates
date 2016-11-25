package com.peng.t00055798.teacherupdates;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Micheal Peng on 10/6/2016.
 */
public class LocalCourseDatabaseAdapter {
    /////////////////////////////////////////////////////////////////////
    //	Constants & Data
    /////////////////////////////////////////////////////////////////////
    // For logging:
    private static final String TAG = "DBAdapter";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;
    /*
     * CHANGE 1:
     */
    // TODO: Setup your fields here:
    public static final String KEY_COURSE = "course_name";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_TEACHER = "teacher_id";
    public static final String KEY_TEACHER_NAME = "teacher_name";
    public static final String KEY_REGISTERED = "registered";


    // TODO: Setup your field numbers here (0 = KEY_ROWID, 1=...)
    public static final int COL_COURSE = 1;
    public static final int COL_DESC = 2;
    public static final int COL_TEACHER = 3;

    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_COURSE, KEY_TEACHER, KEY_PASSWORD,KEY_TEACHER_NAME};

    // DB info: it's name, and the table we are using (just one).
    public static final String DATABASE_NAME = "teacher_update";
    public static final String DATABASE_TABLE = "course";

    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 3;


    private static final String DATABASE_CREATE_SQL =
            "create table " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " integer primary key autoincrement, "

			/*
			 * CHANGE 2:
			 */
                    // TODO: Place your fields here!
                    // + KEY_{...} + " {type} not null"
                    //	- Key is the column name you created above.
                    //	- {type} is one of: text, integer, real, blob
                    //		(http://www.sqlite.org/datatype3.html)
                    //  - "not null" means it is a required field (must be given a value).
                    // NOTE: All must be comma separated (end of line!) Last one must have NO comma!!
                    + KEY_COURSE + " string not null, "
                    + KEY_TEACHER + " int not null,"
                    + KEY_PASSWORD + " string not null,"
                    + KEY_TEACHER_NAME + " string,"
                    + KEY_REGISTERED + " int"

                    // Rest  of creation:
                    + ");";

    // Context of application who uses us.
    private final Context context;

    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public LocalCourseDatabaseAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public LocalCourseDatabaseAdapter open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }

    // Add a new set of values to the database.
    public long insertRow(int id, String course, String teacher,String password,String teacher_name, int registered) {
		/*
		 * CHANGE 3:
		 */
        Log.d("database = ", course);
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_COURSE, course);
        initialValues.put(KEY_TEACHER, teacher);
        initialValues.put(KEY_PASSWORD, password);
        initialValues.put(KEY_TEACHER_NAME, teacher_name);
        initialValues.put(KEY_REGISTERED, registered);


        // Insert it into the database.
        return db.insertWithOnConflict(DATABASE_TABLE, null, initialValues,SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Return all data in the database.
    public Cursor getAllRows() {
        String where = null;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }
    public Cursor queryLastEvents() {
        return db.query(DATABASE_TABLE, null, null, null, null, null, KEY_ROWID+" DESC");
    }

    // Get a specific row (by rowId)
    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        Log.d("db =", c.toString());
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }
    public Boolean contains(long rowId){
        String query = "SELECT COUNT(*) FROM "+DATABASE_TABLE + " WHERE " + KEY_ROWID + " = " + rowId;
        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();
        Log.d("contains", rowId+"");
        if (cursor.getInt(0) > 0){
           return true;
        }
        return false;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, int registered) {
        String where = KEY_ROWID + " = " + rowId;
        Log.d("registered db = ", registered+"");
		/*
		 * CHANGE 4:
		 */
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:
        ContentValues newValues = new ContentValues();
        //newValues.put(KEY_COURSE, course);
        //newValues.put(KEY_TEACHER, teacher);
        //newValues.put(KEY_PASSWORD, password);
        //newValues.put(KEY_TEACHER_NAME, teacher_name);
        newValues.put(KEY_REGISTERED, registered);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }



    /////////////////////////////////////////////////////////////////////
    //	Private Helper Classes:
    /////////////////////////////////////////////////////////////////////

    /**
     * Private class which handles database creation and upgrading.
     * Used to handle low-level database access.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

            // Recreate new database:
            onCreate(_db);
        }
    }
}
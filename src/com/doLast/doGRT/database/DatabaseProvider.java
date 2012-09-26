/**
 * The usage of database is referenced from 
 * http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/ 
 * */

package com.doLast.doGRT.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class DatabaseProvider extends ContentProvider {
	private static final String TAG = "DatabaseProvider";
		
    private static String DB_PATH = "/data/data/com.doLast.doGRT/databases/"; 
    private static String DB_NAME = "GRT_GTFS.sqlite";
    private static final int DB_VERSION = 1;
    private static final String BASE_PATH = "";
    
    // Database
    private DatabaseHelper mOpenHelper;
    
    // For UriMatcher
    private static final int BUS_STOP = 100;
    private static final int BUS_STOP_ID = 110;
    private static final int CALENDAR = 200;
    private static final int ROUTE = 300;
    private static final int ROUTE_ID = 310;
    private static final int TRIP = 400;
    private static final int STOP_TIME = 500;
     
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
    	sUriMatcher.addURI(DatabaseSchema.AUTHORITY, DatabaseSchema.StopsColumns.TABLE_NAME, BUS_STOP);
    	sUriMatcher.addURI(DatabaseSchema.AUTHORITY, DatabaseSchema.CalendarColumns.TABLE_NAME, CALENDAR);
    	sUriMatcher.addURI(DatabaseSchema.AUTHORITY, DatabaseSchema.RoutesColumns.TABLE_NAME, ROUTE);
    	sUriMatcher.addURI(DatabaseSchema.AUTHORITY, DatabaseSchema.RoutesColumns.TABLE_NAME + "/#", ROUTE);
    	sUriMatcher.addURI(DatabaseSchema.AUTHORITY, DatabaseSchema.TripsColumns.TABLE_NAME, TRIP);
    	sUriMatcher.addURI(DatabaseSchema.AUTHORITY, DatabaseSchema.StopTimesColumns.TABLE_NAME, STOP_TIME);
    }
    
    /** This class helps create and update the database
     * 
     * @author Andreas
     *
     */
	public class DatabaseHelper extends SQLiteOpenHelper {
	    //The Android's default system path of your application database.
		
	    private SQLiteDatabase myDataBase; 
	    private final Context myContext;
		    
		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			this.myContext = context;
		}
	
		@Override
		public void onCreate(SQLiteDatabase db) {			
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		    if (newVersion > oldVersion)
		        Log.v("Database Upgrade", "Database version higher than old.");
		    myContext.deleteDatabase(DB_NAME);
		}
		
	    @Override
		public synchronized void close() {
	    	if(myDataBase != null)
	    		myDataBase.close();
	    	super.close();
		}
		
	    /**
	     * Check if the database already exist to avoid re-copying the file each time you open the application.
	     * return true if it exists, false if it doesn't
	     */
	    private boolean checkDataBase(){
	    	SQLiteDatabase checkDB = null;
	 
/*	    	try{
	    		String myPath = DB_PATH + DB_NAME;
	    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	    		// Another way of checking whether the database exist

	    	}catch(SQLiteException e){
	    		// database does't exist yet.
	    		System.out.println(e.getMessage());
	    	}
	    	
	    	if(checkDB != null){
	    		checkDB.close();
	    	}
	    	
	    	return checkDB != null ? true : false;*/
	    	
	    	// Another way of checking if the database exists
    		File db_file = new File(DB_PATH + DB_NAME);
    		return db_file.exists();
	    }
	    
	    /**
	     * Copies your database from your local assets-folder to the just created empty database in the
	     * system folder, from where it can be accessed and handled.
	     * This is done by transferring bytestream.
	     * */
	    private void copyDataBase() throws IOException {
	    	// Open your local db as the input stream
	    	InputStream myInput = myContext.getAssets().open(DB_NAME);
	 
	    	// Path to the just created empty db
	    	String outFileName = DB_PATH + DB_NAME;
	 
	    	// Open the empty db as the output stream
	    	OutputStream myOutput = new FileOutputStream(outFileName);
	 
	    	// transfer bytes from the inputfile to the outputfile
	    	byte[] buffer = new byte[1024];
	    	int length;
	    	while ((length = myInput.read(buffer))>0){
	    		myOutput.write(buffer, 0, length);
	    	}
	 
	    	// Close the streams
	    	myOutput.flush();
	    	myOutput.close();
	    	myInput.close();
	    }
	    
		/**
	     * Creates a empty database on the system and rewrites it with your own database.
	     */
	    public void createDataBase() throws IOException {
	    	boolean dbExist = checkDataBase();
	        if (dbExist) {
	            Log.v("DB Exists", "db exists");
	            // By calling this method here onUpgrade will be called on a
	            // writeable database, but only if the version number has been
	            // bumped
	            this.getWritableDatabase();
	        } 
	
	        
	        if (!dbExist) {
	    		/** 
	    		 * Database doesn't exist
	    		 * By calling this method and empty database will be created into the default system path
	    		 * of your application so we are gonna be able to overwrite that database with our database.
	    		 */
	        	this.getReadableDatabase();
	 
	        	try {
	    			copyDataBase();
	    		} catch (IOException e) {
	        		throw new Error("Error copying database"); 
	        	}
	    	}
	    }
	   
	    public void openDataBase() throws SQLException {
	    	// Open the database
	        String myPath = DB_PATH + DB_NAME;
	    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	    }
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// Nothing to do here, this database is only readable
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// Nothing to do here, this database is only readable
		return null;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		try {
			mOpenHelper.createDataBase();
		} catch (IOException e) {
			throw new Error("Unable to create database");
		}
		
		try {
			mOpenHelper.openDataBase();
		} catch (SQLException e) {
			throw e;
		}
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
	    // Uisng SQLiteQueryBuilder instead of query() method
	    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

	    // Select a table
	    String table = null;
	    switch(sUriMatcher.match(uri)) {
	    case BUS_STOP:
	    	table = DatabaseSchema.StopsColumns.TABLE_NAME;
	    	break;
	    case CALENDAR:
	    	table = DatabaseSchema.CalendarColumns.TABLE_NAME;
	    	break;
	    case ROUTE:
	    	table = DatabaseSchema.RoutesColumns.TABLE_NAME;
	    	break;
	    case ROUTE_ID:
	        queryBuilder.appendWhere(DatabaseSchema.RoutesColumns._ID + "="
	                + uri.getLastPathSegment());
	    	break;
	    case TRIP:
	    	table = DatabaseSchema.TripsColumns.TABLE_NAME;
	    	break;
	    case STOP_TIME:
	    	table = DatabaseSchema.StopTimesColumns.TABLE_NAME;
	    	break;
	    default:
	    	throw new IllegalArgumentException("Unknown URI " + uri);
	    }
	    queryBuilder.setTables(table);
	    
	    
        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DatabaseSchema.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }
        
        // Opens the database object in "read" mode, since no writes need to be done.
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        
        /*
         * Performs the query. If no problems occur trying to read the database, then a Cursor
         * object is returned; otherwise, the cursor variable contains null. If no records were
         * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
         */
        Cursor c = queryBuilder.query(
            db,            // The database to query
            projection,    // The columns to return from the query
            selection,     // The columns for the where clause
            selectionArgs, // The values for the where clause
            null,          // don't group the rows
            null,          // don't filter by row groups
            "'"+orderBy+"'"        // The sort order
        );
	    
        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// Nothing to do, does not allow update
		return 0;
	}
}
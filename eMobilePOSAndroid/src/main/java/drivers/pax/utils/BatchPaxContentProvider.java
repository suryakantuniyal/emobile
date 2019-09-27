package drivers.pax.utils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.android.emobilepos.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class BatchPaxContentProvider extends ContentProvider {
    SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

        static final String PROVIDER_NAME = "drivers.pax.utils.BatchPaxContentProvider";
        static final String URL = "content://" + PROVIDER_NAME + "/batchPax";
        static final String URL_DELETE_BY_ID = URL + "/delete";

        public static final Uri CONTENT_URI = Uri.parse(URL);

        public static final String _ID = "_id";
        public static final String RESULT = "result";
        public static final String RESULT_DATE = "result_date";

        private static HashMap<String, String> RESULTS_PROJECTION_MAP;

        static final int RESULTS = 1;
        static final int RESULT_ID = 2;
        static final int DELETE_BY_ID = 3;


    static final UriMatcher uriMatcher;
        static{
            uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            uriMatcher.addURI(PROVIDER_NAME, "results", RESULTS);
            uriMatcher.addURI(PROVIDER_NAME, "results/#", RESULT_ID);
            uriMatcher.addURI(PROVIDER_NAME, "delete/#", DELETE_BY_ID);
        }

        /**
         * Database specific constant declarations
         */

        private SQLiteDatabase db;
        static final String DATABASE_NAME = "ResultsDB";
        static final String RESULTS_TABLE_NAME = "results";
        static final int DATABASE_VERSION = 1;
        static final String CREATE_DB_TABLE =
                " CREATE TABLE " + RESULTS_TABLE_NAME +
                        " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        " result TEXT NOT NULL, " +
                        " result_date TEXT NOT NULL);";

        /**
         * Helper class that actually creates and manages
         * the provider's underlying data repository.
         */

        private static class DatabaseHelper extends SQLiteOpenHelper {
            DatabaseHelper(Context context){
                super(context, DATABASE_NAME, null, DATABASE_VERSION);
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL(CREATE_DB_TABLE);
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE IF EXISTS " +  RESULTS_TABLE_NAME);
                onCreate(db);
            }
        }

        @Override
        public boolean onCreate() {
            Context context = getContext();
            DatabaseHelper dbHelper = new DatabaseHelper(context);

            /**
             * Create a write able database which will trigger its
             * creation if it doesn't already exist.
             */

            db = dbHelper.getWritableDatabase();
            return (db == null)? false:true;
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            try{
                values.put(BatchPaxContentProvider.RESULT_DATE, fmt.format(Calendar.getInstance().getTime()));
                /**
                 * Add a new student record
                 */
                long rowID = db.insert(RESULTS_TABLE_NAME, "", values);
                /**
                 * If record is added successfully
                 */
                if (rowID > 0) {
                    Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);
                    return _uri;
                }
            }catch (Exception x){
                x.printStackTrace();
            }
            throw new SQLException("Failed to add a record into " + uri);
        }

    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection,String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(RESULTS_TABLE_NAME);
        switch (uriMatcher.match(uri)) {
            case RESULTS:
                qb.setProjectionMap(RESULTS_PROJECTION_MAP);
                break;
            case RESULT_ID:
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                break;
        }

        if (sortOrder == null || sortOrder == ""){
            /**
             * By default sort on student names
             */
            sortOrder = RESULT;
        }

        Cursor c = qb.query(db,	projection,	selection,
                selectionArgs,null, null, sortOrder);
        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            int count = 0;
            db.delete(RESULTS_TABLE_NAME,_ID +  " = ?",selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }

        @Override
        public int update(Uri uri, ContentValues values,
                          String selection, String[] selectionArgs) {
            int count = 0;
            switch (uriMatcher.match(uri)) {
                case RESULTS:
                    count = db.update(RESULTS_TABLE_NAME, values, selection, selectionArgs);
                    break;

                case RESULT_ID:
                    count = db.update(RESULTS_TABLE_NAME, values,
                            _ID + " = " + uri.getPathSegments().get(1) +
                                    (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri );
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }

        @Override
        public String getType(Uri uri) {
            switch (uriMatcher.match(uri)){
                /**
                 * Get all student records
                 */
                case RESULTS:
                    return "vnd.android.cursor.dir/vnd.example.students";
                /**
                 * Get a particular student
                 */
                case RESULT_ID:
                    return "vnd.android.cursor.item/vnd.example.students";
                default:
                    throw new IllegalArgumentException("Unsupported URI: " + uri);
            }
        }


}

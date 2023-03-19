package com.nrojt.utils.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseConnection extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mydatabase.db";
    private static final String DATABASE_PATH = "/data/data/com.example.myapp/databases/";

    private static final int DATABASE_VERSION = 1;

    public DatabaseConnection(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void copyDatabaseFromAssets(Context context) throws IOException {
        // Open the pre-existing database file from the app's assets directory
        InputStream inputStream = context.getAssets().open(DATABASE_NAME);

        // Create the output directory if it doesn't exist
        File databasePath = new File(DATABASE_PATH);
        if (!databasePath.exists()) {
            databasePath.mkdirs();
        }

        // Create the output file and copy the contents of the input stream to it
        OutputStream outputStream = new FileOutputStream(DATABASE_PATH + DATABASE_NAME);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create the initial schema of the database
        sqLiteDatabase.execSQL("CREATE TABLE mytable (id INTEGER PRIMARY KEY, name TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle upgrades from one version of the database to the next
        if (oldVersion < 2) {
            // Upgrade from version 1 to version 2
            db.execSQL("ALTER TABLE mytable ADD COLUMN age INTEGER");
        }
        if (oldVersion < 3) {
            // Upgrade from version 2 to version 3
            db.execSQL("CREATE TABLE myothertable (id INTEGER PRIMARY KEY, description TEXT)");
        }
    }
}

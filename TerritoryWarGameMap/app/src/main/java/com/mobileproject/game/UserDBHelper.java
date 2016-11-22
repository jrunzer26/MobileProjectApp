package com.mobileproject.game;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by 100520993 on 11/13/2016.
 */

public class UserDBHelper extends SQLiteOpenHelper {

    private static final String NAME = "UserDB";
    private static final int DATABASE_VERSION = 1;

    /**
     * Creates the database helper.
     * @param context - the current context
     */
    public UserDBHelper(Context context) {
        super(context, NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the database
     * @param db the database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table Users (          " +
                "username text primary key,        " +
                "gold int DEFAULT 0,              " +
                "food int DEFAULT 0,              " +
                "tiles int DEFAULT 0,             " +
                "tilesTaken int DEFAULT 0,        " +
                "goldObtained int DEFAULT 0,      " +
                "foodObtained int DEFAULT 0,      " +
                "totalGoldObtained int DEFAULT 0, " +
                "totalFoodObtained int DEFAULT 0, " +
                "totalSoldiers int DEFAULT 0,     " +
                "soldiersAvailable int DEFAULT 0) "
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Saves a user to the database.
     * @param user the user to save
     */
    public void saveUser(User user) {
        System.out.println("saving user");
        System.out.println("username: " + user.getUsername());
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("gold", user.getGold());
        values.put("food", user.getFood());
        values.put("tiles", user.getTiles());
        values.put("tilesTaken", user.getTilesTaken());
        values.put("goldObtained", user.getGoldObtained());
        values.put("foodObtained", user.getGoldObtained());
        values.put("totalGoldObtained", user.getGoldObtained());
        values.put("totalFoodObtained", user.getTotalFoodObtained());
        values.put("totalSoldiers", user.getTotalSoldiers());
        values.put("soldiersAvailable", user.getSoldiersAvailable());
        System.out.println("saving user: " + user.getUsername());
        String[] username = {user.getUsername()};
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Users","username = ?", username);
        db.insert("Users", null, values);
    }

    public void showUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            String[] select = new String[0];
            Cursor cursor = db.rawQuery("Select * from users", select);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                System.out.println("username: " + cursor.getInt(cursor.getColumnIndex("username")));
                System.out.println(cursor.getInt(cursor.getColumnIndex("gold")));
                System.out.println(cursor.getInt(cursor.getColumnIndex("food")));
                cursor.moveToNext();
            }
        }
    }


    /**
     * Gets the user's resources the last time they played.
     * @param username
     * @return a user with the resources
     */
    public User getUser(String username) {
        System.out.println("get user: " + username);
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            String[] columns = new String[]{
                    "gold",
                    "food",
                    "tiles",
                    "tilesTaken",
                    "goldObtained",
                    "foodObtained",
                    "totalGoldObtained",
                    "totalFoodObtained",
                    "totalSoldiers",
                    "soldiersAvailable"
            };
            String where = "username = ?";
            String[] whereArgs = new String[]{username};
            String groupBy = "";
            String groupArgs = "";
            String orderBy = "";


            Cursor cursor = db.query("Users", columns, where, whereArgs,
                    groupBy, groupArgs, orderBy);


            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                User user = new User(username,
                        cursor.getInt(cursor.getColumnIndex("gold")),
                        cursor.getInt(cursor.getColumnIndex("food")),
                        cursor.getInt(cursor.getColumnIndex("tiles")),
                        cursor.getInt(cursor.getColumnIndex("tilesTaken")),
                        cursor.getInt(cursor.getColumnIndex("goldObtained")),
                        cursor.getInt(cursor.getColumnIndex("foodObtained")),
                        cursor.getInt(cursor.getColumnIndex("totalGoldObtained")),
                        cursor.getInt(cursor.getColumnIndex("totalFoodObtained")),
                        cursor.getInt(cursor.getColumnIndex("totalSoldiers")),
                        cursor.getInt(cursor.getColumnIndex("soldiersAvailable")));
                System.out.println("after get: username: " + user.getUsername());
                return user;
            }
        }
        return null;
    }
}

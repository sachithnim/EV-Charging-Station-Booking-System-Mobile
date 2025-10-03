package com.example.ev_mobile.db;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ev_mobile.Models.EVOwner;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "evcharging.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_EV_OWNERS = "ev_owners";
    private static final String COL_NIC = "nic";
    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PHONE = "phone";
    private static final String COL_ADDRESS = "address";
    private static final String COL_PASSWORD = "password";
    private static final String COL_IS_ACTIVE = "is_active";

    public SQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_EV_OWNERS + " ("
                + COL_NIC + " TEXT PRIMARY KEY, "
                + COL_NAME + " TEXT, "
                + COL_EMAIL + " TEXT, "
                + COL_PHONE + " TEXT, "
                + COL_ADDRESS + " TEXT, "
                + COL_PASSWORD + " TEXT, "
                + COL_IS_ACTIVE + " INTEGER DEFAULT 1)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EV_OWNERS);
        onCreate(db);
    }

    // Insert EV Owner (local create)
    public void insertEVOwner(EVOwner owner) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NIC, owner.getNic());
        values.put(COL_NAME, owner.getName());
        values.put(COL_EMAIL, owner.getEmail());
        values.put(COL_PHONE, owner.getPhone());
        values.put(COL_ADDRESS, owner.getAddress());
        values.put(COL_PASSWORD, owner.getPassword());
        values.put(COL_IS_ACTIVE, owner.isActive() ? 1 : 0);
        db.insert(TABLE_EV_OWNERS, null, values);
        db.close();
    }

    // Update EV Owner
    public void updateEVOwner(EVOwner owner) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, owner.getName());
        values.put(COL_EMAIL, owner.getEmail());
        values.put(COL_PHONE, owner.getPhone());
        values.put(COL_ADDRESS, owner.getAddress());
        values.put(COL_PASSWORD, owner.getPassword());
        values.put(COL_IS_ACTIVE, owner.isActive() ? 1 : 0);
        db.update(TABLE_EV_OWNERS, values, COL_NIC + " = ?", new String[]{owner.getNic()});
        db.close();
    }

    // Deactivate (set is_active = 0)
    public void deactivateEVOwner(String nic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_ACTIVE, 0);
        db.update(TABLE_EV_OWNERS, values, COL_NIC + " = ?", new String[]{nic});
        db.close();
    }

    // Get EV Owner by NIC
    public EVOwner getEVOwner(String nic) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EV_OWNERS, null, COL_NIC + " = ?", new String[]{nic}, null, null, null);
        if (cursor.moveToFirst()) {
            EVOwner owner = new EVOwner();
            owner.setNic(cursor.getString(cursor.getColumnIndexOrThrow(COL_NIC)));
            owner.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
            owner.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)));
            owner.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)));
            owner.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS)));
            owner.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)));
            owner.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ACTIVE)) == 1);
            cursor.close();
            db.close();
            return owner;
        }
        cursor.close();
        db.close();
        return null;
    }
}

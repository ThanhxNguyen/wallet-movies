package com.nguyen.paul.thanh.walletmovie.database.interfaces;

/**
 * Created by THANH on 10/01/2017.
 */

public interface GenresTableConst {

    public static final String TABLE_NAME = "genres";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";

    public static final String SQL_CREATE_GENRES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                    COLUMN_NAME + " TEXT)";

    public static final String SQL_DELETE_GENRES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}

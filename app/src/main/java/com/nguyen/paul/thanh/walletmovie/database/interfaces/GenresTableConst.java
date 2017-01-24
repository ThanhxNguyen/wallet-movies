package com.nguyen.paul.thanh.walletmovie.database.interfaces;

/**
 * Holds constants for genres table
 */

public interface GenresTableConst {

    static final String TABLE_NAME = "genres";
    static final String COLUMN_ID = "id";
    static final String COLUMN_NAME = "name";

    static final String SQL_CREATE_GENRES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                    COLUMN_NAME + " TEXT)";

    static final String SQL_DELETE_GENRES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}

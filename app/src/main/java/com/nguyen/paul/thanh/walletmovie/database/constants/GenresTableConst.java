package com.nguyen.paul.thanh.walletmovie.database.constants;

/**
 * Holds constants for genres table
 */

public class GenresTableConst {

    private GenresTableConst() {}

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

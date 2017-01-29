package com.nguyen.paul.thanh.walletmovie.database.constants;

/**
 * Holds constants for genres table
 */

public class GenresTableConst {

    //to avoid class instantiation
    private GenresTableConst() {}

    public static final String GENRES_TABLE_NAME = "genres";
    public static final String GENRES_COLUMN_ID = "id";
    public static final String GENRES_COLUMN_NAME = "name";

    public static final String SQL_CREATE_GENRES =
            "CREATE TABLE " + GENRES_TABLE_NAME + " (" +
                    GENRES_COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                    GENRES_COLUMN_NAME + " TEXT)";

    public static final String SQL_DELETE_GENRES =
            "DROP TABLE IF EXISTS " + GENRES_TABLE_NAME;
}

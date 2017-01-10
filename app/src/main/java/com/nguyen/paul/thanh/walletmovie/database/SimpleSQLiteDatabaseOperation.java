package com.nguyen.paul.thanh.walletmovie.database;

import com.nguyen.paul.thanh.walletmovie.database.interfaces.DatabaseOperation;

/**
 * The purpose of this class is to provide a convenient way to implement DatabaseOperation.
 * Extend this class and override methods when needed. Therefore, no need to implement all
 * methods when only need some certain methods.
 */

public class SimpleSQLiteDatabaseOperation implements DatabaseOperation {
    @Override
    public void find(int id) {

    }

    @Override
    public void findAll() {

    }

    @Override
    public void insert() {

    }

    @Override
    public void update(int id) {

    }

    @Override
    public void delete(int id) {

    }
}

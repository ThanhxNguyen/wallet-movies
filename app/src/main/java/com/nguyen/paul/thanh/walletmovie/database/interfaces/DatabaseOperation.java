package com.nguyen.paul.thanh.walletmovie.database.interfaces;

/**
 * This interface defines methods for database operations.
 * The Purpose is to provide a uniform way for database operations.
 */

public interface DatabaseOperation {
    public void find(int id);
    public void findAll();
    public void insert();
    public void update(int id);
    public void delete(int id);
}

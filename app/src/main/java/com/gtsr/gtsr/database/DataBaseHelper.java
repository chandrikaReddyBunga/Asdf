package com.gtsr.gtsr.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DataBaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "gtsr.db";

    private static final int DATABASE_VERSION = 2;

    ConnectionSource objConnectionSource;

    private Dao<TestFactors,Integer> testFactorsesDao=null;
    private Dao<UrineresultsModel, Integer> urineresultsModelIntegerDao = null;

    public DataBaseHelper(Context contex) {
        super(contex, DATABASE_NAME, null, DATABASE_VERSION);
        getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        Log.e("DBStatusonCreate", "OnCreate" + connectionSource);

        try {
            TableUtils.createTable(connectionSource,TestFactors.class);
            TableUtils.createTable(connectionSource,UrineresultsModel.class);
            testFactorsesDao = DaoManager.createDao(connectionSource,TestFactors.class);
            urineresultsModelIntegerDao = DaoManager.createDao(connectionSource, UrineresultsModel.class);
            objConnectionSource = connectionSource;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Log.e("DBStatus", "OnUpgrade" + connectionSource);
        try {
            TableUtils.dropTable(connectionSource, UrineresultsModel.class, true);
            TableUtils.dropTable(connectionSource, TestFactors.class, true);

            onCreate(database, connectionSource);
            objConnectionSource = connectionSource;
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public Dao<UrineresultsModel, Integer> getUrineresultsDao() {
        if (urineresultsModelIntegerDao == null) {
            try {
                urineresultsModelIntegerDao = getDao(UrineresultsModel.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return urineresultsModelIntegerDao;
    }
    public Dao<TestFactors, Integer> getTestFactorsesDao() {
        if (testFactorsesDao == null) {
            try {
                testFactorsesDao = getDao(TestFactors.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return testFactorsesDao;
    }

}

package com.example.nurhazim.i_recall.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.nurhazim.i_recall.data.CardsContract.CardEntry;
import com.example.nurhazim.i_recall.data.CardsContract.DeckEntry;

/**
 * Created by NurHazim on 13-Oct-14.
 */
public class CardsDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "cards.db";

    public CardsDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_DECK_TABLE = "CREATE TABLE " + DeckEntry.TABLE_NAME + " (" +
                DeckEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                DeckEntry.COLUMN_DECK_NAME + " TEXT NOT NULL );";

        final String SQL_CREATE_CARD_TABLE = "CREATE TABLE " + CardEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                CardEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                CardEntry.COLUMN_DECK_KEY + " INTEGER NOT NULL, " +

                CardEntry.COLUMN_TERM + " TEXT NOT NULL, " +
                CardEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + CardEntry.COLUMN_DECK_KEY + ") REFERENCES " +
                DeckEntry.TABLE_NAME + " (" + DeckEntry._ID + "));";

        db.execSQL(SQL_CREATE_DECK_TABLE);
        db.execSQL(SQL_CREATE_CARD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DeckEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CardEntry.TABLE_NAME);
        onCreate(db);
    }
}
package com.example.nurhazim.i_recall;

import android.content.Context;
import android.database.Cursor;

import com.example.nurhazim.i_recall.data.CardsContract;

/**
 * Created by NurHazim on 17-Oct-14.
 */
public class Utility {

    public static long getDeckId(Context context, String deckName){
        Cursor cursor = context.getContentResolver().query(
                CardsContract.DeckEntry.buildDeckWithName(deckName),
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        return cursor.getLong(cursor.getColumnIndex(CardsContract.DeckEntry._ID));
    }

    public static String getDeckName(Context context, long ID){
        Cursor cursor = context.getContentResolver().query(
                CardsContract.DeckEntry.buildDeckWithId(ID),
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(CardsContract.DeckEntry.COLUMN_DECK_NAME));
    }

    public static boolean hasNewLine(CharSequence charSequence){
        String newline = System.getProperty("line.separator");
        String string = String.valueOf(charSequence);
        return string.contains(newline);
    }
}

package com.example.nurhazim.i_recall;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.nurhazim.i_recall.data.CardsContract;

import java.util.Vector;

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

    public static void DeleteCards(Context context, Vector<Long> id){
        int affected = 0;
        for(Long cardId : id){
            affected += context.getContentResolver().delete(
                    CardsContract.CardEntry.CONTENT_URI,
                    CardsContract.CardEntry._ID + " = ?",
                    new String[]{String.valueOf(cardId)}
            );
        }
        Log.v(context.getClass().getSimpleName(), affected + " rows deleted");
    }

    public static void DeleteDeck(Context context, Vector<Long> id){
        int affected = 0;
        for(Long deckId : id){
            Cursor cursor = context.getContentResolver().query(
                    CardsContract.CardEntry.buildCardWithDeckID(deckId),
                    null,
                    null,
                    null,
                    null
            );

            Vector<Long> cardsToDelete = new Vector<Long>();
            if(cursor.moveToFirst()){
                do{
                    cardsToDelete.add(cursor.getLong(cursor.getColumnIndex(CardsContract.CardEntry._ID)));
                }while(cursor.moveToNext());
            }
            DeleteCards(context, cardsToDelete);

            affected += context.getContentResolver().delete(
                    CardsContract.DeckEntry.CONTENT_URI,
                    CardsContract.DeckEntry._ID + " = ?",
                    new String[]{String.valueOf(deckId)}
            );
        }
        Log.v(context.getClass().getSimpleName(), affected + " deck(s) deleted");
    }
}

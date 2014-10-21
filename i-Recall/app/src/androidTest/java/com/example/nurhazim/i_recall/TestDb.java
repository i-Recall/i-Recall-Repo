package com.example.nurhazim.i_recall;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.example.nurhazim.i_recall.data.CardsContract.CardEntry;
import com.example.nurhazim.i_recall.data.CardsContract.DeckEntry;
import com.example.nurhazim.i_recall.data.CardsDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by NurHazim on 13-Oct-14.
 */
public class TestDb extends AndroidTestCase {

    public void testCreateDb() throws Throwable{
        mContext.deleteDatabase(CardsDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new CardsDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {
        CardsDbHelper dbHelper = new CardsDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = createAnimalDeckValues();

        long locationRowId = db.insert(DeckEntry.TABLE_NAME, null, testValues);

        assertTrue(locationRowId != -1);

        Cursor cursor = db.query(
                DeckEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        validateCursor(cursor, testValues);

        ContentValues testCardValues = createCardValues(locationRowId);

        long cardRowId = db.insert(CardEntry.TABLE_NAME, null, testCardValues);
        assertTrue(cardRowId != -1);

        Cursor cardCursor = db.query(
                CardEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        validateCursor(cardCursor, testCardValues);

        dbHelper.close();
    }

    static ContentValues createAnimalDeckValues(){
        ContentValues testValues = new ContentValues();

        testValues.put(DeckEntry.COLUMN_DECK_NAME, "Animals");

        return testValues;
    }

    static ContentValues createCardValues(long locationRowId){
        ContentValues cardValues = new ContentValues();

        cardValues.put(CardEntry.COLUMN_DECK_KEY, locationRowId);
        cardValues.put(CardEntry.COLUMN_TERM, "cat");
        cardValues.put(CardEntry.COLUMN_DESCRIPTION, "A pretentious little thing.");

        return cardValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}

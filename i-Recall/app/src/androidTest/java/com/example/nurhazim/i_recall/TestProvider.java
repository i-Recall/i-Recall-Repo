package com.example.nurhazim.i_recall;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.nurhazim.i_recall.data.CardsContract.CardEntry;
import com.example.nurhazim.i_recall.data.CardsContract.DeckEntry;
import com.example.nurhazim.i_recall.data.CardsDbHelper;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Created by NurHazim on 13-Oct-14.
 */
public class TestProvider extends AndroidTestCase {

    private static final String LOG_TAG = TestProvider.class.getSimpleName();

    private static final String[] deckNames = new String[]{
            "Animals",
            "Towns",
            "Plants",
            "Races",
            "Cars"
    };

    private static final String[] animalCardTerms = new String[]{
            "Cat",
            "Dog"
    };

    private static final String[] animalCardDescriptions = new String[]{
            "A pretentious little thing.",
            "A man's best friend."
    };

    private static final String[] townCardTerms = new String[]{
            "Petaling Jaya"
    };

    private static final String[] townCardDescriptions = new String[]{
            "The center of Selangor"
    };

    public void deleteAllRecords(){
        mContext.getContentResolver().delete(
                CardEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                DeckEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                CardEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                DeckEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void testInsertReadProvider() {

        deleteAllRecords();

        ContentValues deckAnimalValues = createAnimalDeckValues();
        ContentValues deckTownValues = createTownDeckValues();
        ContentValues deckPlantValues = createPlantDeckValues();
        ContentValues deckRaceValues = createRaceDeckValues();
        ContentValues deckCarValues = createCarDeckValues();

        Uri deckAnimalInsertUri = mContext.getContentResolver().insert(DeckEntry.CONTENT_URI, deckAnimalValues);
        Uri deckTownInsertUri = mContext.getContentResolver().insert(DeckEntry.CONTENT_URI, deckTownValues);
        Uri deckPlantInsertUri = mContext.getContentResolver().insert(DeckEntry.CONTENT_URI, deckPlantValues);
        Uri deckRaceInsertUri = mContext.getContentResolver().insert(DeckEntry.CONTENT_URI, deckRaceValues);
        Uri deckCarInsertUri = mContext.getContentResolver().insert(DeckEntry.CONTENT_URI, deckCarValues);

        long deckAnimalRowId = ContentUris.parseId(deckAnimalInsertUri);
        long deckTownRowId = ContentUris.parseId(deckTownInsertUri);
        long deckPlantRowId = ContentUris.parseId(deckPlantInsertUri);
        long deckRaceRowId = ContentUris.parseId(deckRaceInsertUri);
        long deckCarRowId = ContentUris.parseId(deckCarInsertUri);

        assertTrue(deckAnimalRowId != -1);
        assertTrue(deckTownRowId != -1);
        assertTrue(deckPlantRowId != -1);
        assertTrue(deckRaceRowId != -1);
        assertTrue(deckCarRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                DeckEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertTrue(cursor.moveToFirst());
        assertTrue(cursor.getCount() == deckNames.length);

        for(int i = 0; i < cursor.getCount(); ++i){
            assertTrue(cursor.getString(cursor.getColumnIndex(DeckEntry.COLUMN_DECK_NAME)).equals(deckNames[i]));
            cursor.moveToNext();
        }

        ContentValues testAnimalCard1Values = createAnimalCard1Values(deckAnimalRowId);
        ContentValues testAnimalCard2Values = createAnimalCard2Values(deckAnimalRowId);
        ContentValues testTownCardValues = createTownCardValues(deckTownRowId);

        Uri animal1InsertUri = mContext.getContentResolver().insert(CardEntry.CONTENT_URI, testAnimalCard1Values);
        Uri animal2InsertUri = mContext.getContentResolver().insert(CardEntry.CONTENT_URI, testAnimalCard2Values);
        Uri townInsertUri = mContext.getContentResolver().insert(CardEntry.CONTENT_URI, testTownCardValues);

        long animal1CardRowId = ContentUris.parseId(animal1InsertUri);
        long animal2CardRowId = ContentUris.parseId(animal2InsertUri);
        long townCardRowId = ContentUris.parseId(townInsertUri);

        assertTrue(animal1CardRowId != -1);
        assertTrue(animal2CardRowId != -1);
        assertTrue(townCardRowId != -1);

        cursor = mContext.getContentResolver().query(
                DeckEntry.buildDeckWithName(deckNames[0]),
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();
        long deckID = cursor.getLong(cursor.getColumnIndex(CardEntry._ID));

        Cursor cardCursor = mContext.getContentResolver().query(
                CardEntry.buildCardWithDeckID(deckID),
                null,
                null,
                null,
                null
        );

        assertTrue(cardCursor.moveToFirst());

        for(int i = 0; i < cardCursor.getCount(); i++){
            assertTrue(cardCursor.getString(cardCursor.getColumnIndex(CardEntry.COLUMN_TERM)).equals(animalCardTerms[i]));
            assertTrue(cardCursor.getString(cardCursor.getColumnIndex(CardEntry.COLUMN_DESCRIPTION)).equals(animalCardDescriptions[i]));
            cardCursor.moveToNext();
        }

        cardCursor.close();
        cursor.close();
    }

    public void testGetType(){
        String type = mContext.getContentResolver().getType(CardEntry.CONTENT_URI);
        assertEquals(CardEntry.CONTENT_TYPE, type);

        String testName = deckNames[2];
        type = mContext.getContentResolver().getType(DeckEntry.buildDeckWithName(testName));
        assertEquals(DeckEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(DeckEntry.CONTENT_URI);
        assertEquals(DeckEntry.CONTENT_TYPE, type);

        long id = 2;
        type = mContext.getContentResolver().getType(CardEntry.buildCardWithDeckID(id));
        assertEquals(CardEntry.CONTENT_TYPE, type);
    }

    public void testUpdateDeck(){
        deleteAllRecords();
        ContentValues testValues = createAnimalDeckValues();

        Uri deckUri = mContext.getContentResolver().insert(DeckEntry.CONTENT_URI, testValues);
        long deckRowId = ContentUris.parseId(deckUri);

        assertTrue(deckRowId != -1);
        ContentValues updatedValues = new ContentValues(testValues);
        updatedValues.put(DeckEntry._ID, deckRowId);
        updatedValues.put(DeckEntry.COLUMN_DECK_NAME, "Potatoes");

        int count = mContext.getContentResolver().update(
                DeckEntry.CONTENT_URI, updatedValues,
                DeckEntry._ID + " = ?",
                new String[]{Long.toString(deckRowId)}
        );

        assertEquals(count, 1);

        Cursor cursor = mContext.getContentResolver().query(
                DeckEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        validateCursor(cursor, updatedValues);
    }

    public void testDeleteRecordsAtEnd(){
        deleteAllRecords();
    }

    static ContentValues createAnimalDeckValues(){
        ContentValues testValues = new ContentValues();

        testValues.put(DeckEntry.COLUMN_DECK_NAME, deckNames[0]);

        return testValues;
    }

    static ContentValues createTownDeckValues(){
        ContentValues testValues = new ContentValues();

        testValues.put(DeckEntry.COLUMN_DECK_NAME, deckNames[1]);

        return testValues;
    }

    static ContentValues createPlantDeckValues(){
        ContentValues testValues = new ContentValues();

        testValues.put(DeckEntry.COLUMN_DECK_NAME, deckNames[2]);

        return testValues;
    }

    static ContentValues createRaceDeckValues(){
        ContentValues testValues = new ContentValues();

        testValues.put(DeckEntry.COLUMN_DECK_NAME, deckNames[3]);

        return testValues;
    }

    static ContentValues createCarDeckValues(){
        ContentValues testValues = new ContentValues();

        testValues.put(DeckEntry.COLUMN_DECK_NAME, deckNames[4]);

        return testValues;
    }


    static ContentValues createAnimalCard1Values(long locationRowId){
        ContentValues cardValues = new ContentValues();

        cardValues.put(CardEntry.COLUMN_DECK_KEY, locationRowId);
        cardValues.put(CardEntry.COLUMN_TERM, animalCardTerms[0]);
        cardValues.put(CardEntry.COLUMN_DESCRIPTION, animalCardDescriptions[0]);

        return cardValues;
    }

    static ContentValues createAnimalCard2Values(long locationRowId){
        ContentValues cardValues = new ContentValues();

        cardValues.put(CardEntry.COLUMN_DECK_KEY, locationRowId);
        cardValues.put(CardEntry.COLUMN_TERM, animalCardTerms[1]);
        cardValues.put(CardEntry.COLUMN_DESCRIPTION, animalCardDescriptions[1]);

        return cardValues;
    }

    static ContentValues createTownCardValues(long locationRowId){
        ContentValues cardValues = new ContentValues();

        cardValues.put(CardEntry.COLUMN_DECK_KEY, locationRowId);
        cardValues.put(CardEntry.COLUMN_TERM, townCardTerms[0]);
        cardValues.put(CardEntry.COLUMN_DESCRIPTION, townCardDescriptions[0]);

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

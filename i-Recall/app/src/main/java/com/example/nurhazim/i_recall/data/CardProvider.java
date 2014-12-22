package com.example.nurhazim.i_recall.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by NurHazim on 13-Oct-14.
 */
public class CardProvider extends ContentProvider {
    private static final String LOG_TAG = ContentProvider.class.getSimpleName();

    private static final int DECK = 0;
    private static final int DECK_WITH_NAME = 1;
    private static final int DECK_WITH_ID = 2;
    private static final int CARD = 100;
    private static final int CARD_WITH_DECK_ID = 101;
    private static final int USER_PERFORMANCE = 1000;
    private static final int USER_PERFORMANCE_WITH_DECK_ID_AND_STUDY_METHOD = 1001;
    private static final int PLAYER = 2000;
    private static final int PLAYER_WITH_ID = 2001;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private CardsDbHelper mOpenHelper;
    
//    this can be used for reference if ever need to
//    private static final SQLiteQueryBuilder sQueryBuilder;
//
//    static {
//        sQueryBuilder = new SQLiteQueryBuilder();
//        sQueryBuilder.setTables(
//                CardsContract.CardEntry.TABLE_NAME + " INNER JOIN " +
//                        CardsContract.DeckEntry.TABLE_NAME +
//                        " ON " + CardsContract.CardEntry.TABLE_NAME +
//                        "." + CardsContract.CardEntry.COLUMN_DECK_KEY +
//                        " = " + CardsContract.DeckEntry.TABLE_NAME +
//                        "." + CardsContract.DeckEntry._ID);
//    }

    private static final String sDeckWithNameSelection =
            CardsContract.DeckEntry.TABLE_NAME +
                    "." + CardsContract.DeckEntry.COLUMN_DECK_NAME + " = ? ";
    private static final String sDeckWithID =
            CardsContract.DeckEntry.TABLE_NAME +
                    "." + CardsContract.DeckEntry._ID + " = ?";

    private static final String sCardWithID =
            CardsContract.CardEntry.TABLE_NAME +
                    "." + CardsContract.CardEntry.COLUMN_DECK_KEY + " = ? ";

    private static final String sPlayerWithID =
            CardsContract.PlayerEntry.TABLE_NAME +
                    "." + CardsContract.PlayerEntry._ID + " = ?";

    private static final String sPerformanceWithDeckIdAndStudyMethod =
            CardsContract.UserPerformanceEntry.TABLE_NAME +
                    "." + CardsContract.UserPerformanceEntry.COLUMN_DECK_KEY + " = ? AND " +
            CardsContract.UserPerformanceEntry.TABLE_NAME +
                    "." + CardsContract.UserPerformanceEntry.COLUMN_STUDY_METHOD + " = ?";

    private Cursor getDeckWithName(Uri uri, String[] projection, String sortOder){
        String selection = sDeckWithNameSelection;
        String[] selectionArgs = new String[]{CardsContract.DeckEntry.getNameFromUri(uri)};
        Log.v(LOG_TAG, "getting decks with name: " + CardsContract.DeckEntry.getNameFromUri(uri));

        return mOpenHelper.getReadableDatabase().query(
                CardsContract.DeckEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOder);
    }

    private Cursor getDeckWithID(Uri uri, String[] projection, String sortOrder){
        String[] selectionArgs = new String[]{CardsContract.DeckEntry.getIdFromUri(uri)};

        return mOpenHelper.getReadableDatabase().query(
                CardsContract.DeckEntry.TABLE_NAME,
                projection,
                sDeckWithID,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getCardsWithID(Uri uri, String[] projection, String sortOrder){
        Log.v(LOG_TAG, "getting cards with ID: " + CardsContract.CardEntry.getDeckIdFromUri(uri));
        String deckID = CardsContract.CardEntry.getDeckIdFromUri(uri);

        String selection = sCardWithID;
        String[] selectionArgs = new String[]{deckID};

        return mOpenHelper.getReadableDatabase().query(
                CardsContract.CardEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getPlayerWithId(Uri uri, String[] projection, String sortOrder){
        String playerId = CardsContract.PlayerEntry.getPlayerIdFromUri(uri);

        String selection = sPlayerWithID;
        String[] selectionArgs = new String[]{playerId};

        return mOpenHelper.getReadableDatabase().query(
                CardsContract.PlayerEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getPerformanceWithDeckIdAndStudyMethod(Uri uri, String[] projection, String sortOrder){
        String DeckId = CardsContract.UserPerformanceEntry.getDeckIdFromUri(uri);
        String studyMethod = CardsContract.UserPerformanceEntry.getStudyMethodFromUri(uri);

        String selection = sPerformanceWithDeckIdAndStudyMethod;
        String[] selectionArgs = new String[]{DeckId, studyMethod};

        return mOpenHelper.getReadableDatabase().query(
                CardsContract.UserPerformanceEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    public static UriMatcher buildUriMatcher(){
        final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CardsContract.CONTENT_AUTHORITY;

        sURIMatcher.addURI(authority, CardsContract.PATH_DECK, DECK);
        sURIMatcher.addURI(authority, CardsContract.PATH_DECK + "/#", DECK_WITH_ID);
        sURIMatcher.addURI(authority, CardsContract.PATH_DECK + "/*", DECK_WITH_NAME);

        sURIMatcher.addURI(authority, CardsContract.PATH_CARD, CARD);
        sURIMatcher.addURI(authority, CardsContract.PATH_CARD + "/#", CARD_WITH_DECK_ID);

        sURIMatcher.addURI(authority, CardsContract.PATH_PLAYER, PLAYER);
        sURIMatcher.addURI(authority, CardsContract.PATH_PLAYER + "/#", PLAYER_WITH_ID);

        sURIMatcher.addURI(authority, CardsContract.PATH_USER_PERFORMANCE, USER_PERFORMANCE);
        sURIMatcher.addURI(authority, CardsContract.PATH_USER_PERFORMANCE + "/#/#", USER_PERFORMANCE_WITH_DECK_ID_AND_STUDY_METHOD);

        return sURIMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new CardsDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch(sUriMatcher.match(uri)){
            case DECK_WITH_NAME:
                retCursor = getDeckWithName(uri, projection, sortOrder);
                break;
            case DECK_WITH_ID:
                retCursor = getDeckWithID(uri, projection, sortOrder);
                break;
            case DECK:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CardsContract.DeckEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CARD_WITH_DECK_ID:
                retCursor = getCardsWithID(uri, projection, sortOrder);
                break;
            case CARD:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CardsContract.CardEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case USER_PERFORMANCE_WITH_DECK_ID_AND_STUDY_METHOD:
                retCursor = getPerformanceWithDeckIdAndStudyMethod(uri, projection, sortOrder);
                break;
            case PLAYER_WITH_ID:
                retCursor = getPlayerWithId(uri, projection, sortOrder);
                break;
            case PLAYER:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CardsContract.PlayerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch(match){
            case DECK_WITH_NAME:
                return CardsContract.DeckEntry.CONTENT_ITEM_TYPE;
            case DECK_WITH_ID:
                return CardsContract.DeckEntry.CONTENT_ITEM_TYPE;
            case DECK:
                return CardsContract.DeckEntry.CONTENT_TYPE;
            case CARD_WITH_DECK_ID:
                return CardsContract.CardEntry.CONTENT_TYPE;
            case CARD:
                return CardsContract.CardEntry.CONTENT_TYPE;
            case PLAYER_WITH_ID:
                return CardsContract.PlayerEntry.CONTENT_ITEM_TYPE;
            case PLAYER:
                return CardsContract.PlayerEntry.CONTENT_TYPE;
            case USER_PERFORMANCE:
                return CardsContract.UserPerformanceEntry.CONTENT_ITEM_TYPE;
            case USER_PERFORMANCE_WITH_DECK_ID_AND_STUDY_METHOD:
                return CardsContract.UserPerformanceEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        long _id;

        switch (match){
            case DECK:
                _id = db.insert(CardsContract.DeckEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnUri = CardsContract.DeckEntry.buildDeckUri(_id);
                }
                else{
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            case CARD:
                _id = db.insert(CardsContract.CardEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnUri = CardsContract.CardEntry.buildCardUri(_id);
                }
                else{
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            case PLAYER:
                _id = db.insert(CardsContract.PlayerEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnUri = CardsContract.PlayerEntry.buildPlayerUri(_id);
                }
                else{
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            case USER_PERFORMANCE:
                _id = db.insert(CardsContract.UserPerformanceEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnUri = CardsContract.UserPerformanceEntry.buildPerformanceUri(_id);
                }
                else{
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int affectedRows;

        switch (match) {
            case DECK:
                affectedRows = db.delete(CardsContract.DeckEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CARD:
                affectedRows = db.delete(CardsContract.CardEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (selection == null || affectedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return affectedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int affectedRows;

        switch (match) {
            case DECK:
                affectedRows = db.update(CardsContract.DeckEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CARD:
                affectedRows = db.update(CardsContract.CardEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (affectedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return affectedRows;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CARD:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(CardsContract.CardEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}

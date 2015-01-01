package com.example.nurhazim.i_recall.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by NurHazim on 13-Oct-14.
 */
public class CardsContract {
    public static final String CONTENT_AUTHORITY = "com.example.nurhazim.i_recall";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_DECK = "deck";
    public static final String PATH_CARD = "card";
    public static final String PATH_USER_PERFORMANCE = "user_performance";
    public static final String PATH_PLAYER = "player";

    public static final class DeckEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DECK).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_DECK;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_DECK;

        public static final String TABLE_NAME = "deck";

        public static final String COLUMN_DECK_NAME = "deck_name";

        public static Uri buildDeckUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildDeckWithName(String deckName){
            return CONTENT_URI.buildUpon().appendPath(deckName).build();
        }

        public static Uri buildDeckWithId(long id){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }

        // adrian: newly added
        //public static Uri buildDeckWithCardTermSearchString(String searchString){
        //    return CONTENT_URI.buildUpon().appendPath("search_term").appendPath(searchString).build();
        //}

        public static String getNameFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }
        // adrian: newly added
        /**
         * Get the search string from Uri, Example, will get Business from the Uri content://com.example.nurhazim.i_recall/deck/search_term/Customer
         * @param uri The Uri passed in from
         * @return The search string
         */
        public static String getSearchStringFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static String getIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }

    public static final class CardEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CARD).build();

        // adrian: newly added
        public static Uri buildDeckWithCardTermSearchString(String searchString){
            return CONTENT_URI.buildUpon().appendPath("search_term").appendPath(searchString).build();
        }

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CARD;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CARD;

        public static final String TABLE_NAME = "card";

        public static final String COLUMN_DECK_KEY = "deck_key";

        public static final String COLUMN_TERM = "term";
        public static final String COLUMN_DESCRIPTION = "description";

        public static Uri buildCardUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildCardWithDeckID(long id){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }

        public static String getDeckIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }

    public static final class PlayerEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAYER).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PLAYER;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PLAYER;

        public static final String TABLE_NAME = "player";

        public static final String COLUMN_PLAYER_NAME = "name";

        public static Uri buildPlayerUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPlayerWithId(long id){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }

        public static String getPlayerIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }

    public static final class UserPerformanceEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER_PERFORMANCE).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_USER_PERFORMANCE;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_USER_PERFORMANCE;

        public static final String TABLE_NAME = "user_performance";

        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_STUDY_METHOD = "study_method";

        public static final String COLUMN_DECK_KEY = "deck_key";

        public static Uri buildPerformanceWithDeckIdAndStudyMethod(long id, int study_method){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).appendPath(String.valueOf(study_method)).build();
        }

        public static Uri buildPerformanceUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getPerformanceIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String getDeckIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }
        public static String getStudyMethodFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }
    }
}

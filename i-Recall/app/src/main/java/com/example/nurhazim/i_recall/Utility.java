package com.example.nurhazim.i_recall;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.nurhazim.i_recall.data.CardsContract;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.StringTokenizer;
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

    /**
     * // Adrian: newly added
     * Helper method for import function
     * @param context App context
     * @param currentDeckName The current deck name
     * @param currentCards The cards of this deck
     * @param totalRows Current total rows
     * @return The current total rows after this run
     */
    private static long insertADeckWithCards(Context context, String currentDeckName, Vector<String[]> currentCards, long totalRows)
    {
        long currentTotalRows = totalRows;

        long currentDeckId = 0;
        // check for existing deck name first\
        try
        {
            currentDeckId = getDeckId(context, currentDeckName);
        }
        catch (Exception ex)
        {

        }

        if (currentDeckId == 0) // totally new deck, we do insert deck here
        {
            ContentValues newDeck = new ContentValues();
            newDeck.put(CardsContract.DeckEntry.COLUMN_DECK_NAME, currentDeckName);
            Uri insertUri = context.getContentResolver().insert(CardsContract.DeckEntry.CONTENT_URI, newDeck);
            currentDeckId = ContentUris.parseId(insertUri);
        }

        if (currentDeckId > 0)
        {
            // we do insert/update cards here
            for (int i=0; i<currentCards.size(); i++)
            {
                String values[] = currentCards.elementAt(i);

                ContentValues newCard = new ContentValues();
                newCard.put(CardsContract.CardEntry.COLUMN_TERM, values[0]);
                newCard.put(CardsContract.CardEntry.COLUMN_DESCRIPTION, values[1]);
                newCard.put(CardsContract.CardEntry.COLUMN_DECK_KEY, currentDeckId);

                // check for existing card with same deck id and same card term
                // if yes, we do update existing card
                // if no, we do insert card

                String whereString = CardsContract.CardEntry.COLUMN_DECK_KEY + " = " + currentDeckId + " AND " + CardsContract.CardEntry.COLUMN_TERM + " = '" + values[0] + "'";
                Cursor cardCursor = context.getContentResolver().query(
                        CardsContract.CardEntry.CONTENT_URI,
                        new String[]{CardsContract.CardEntry._ID,CardsContract.CardEntry.COLUMN_TERM},
                        whereString,
                        null,
                        null);

                boolean needInsert = false;
                if (cardCursor != null)
                {
                    if (cardCursor.getCount() > 0)
                    {
                        cardCursor.moveToFirst();
                        long currentCardId = cardCursor.getLong(0);

                        context.getContentResolver().update(
                                CardsContract.CardEntry.CONTENT_URI,
                                newCard,
                                CardsContract.CardEntry._ID + " = ?",
                                new String[]{String.valueOf(currentCardId)}
                        );
                        currentTotalRows++;
                    }
                    else
                    {
                        needInsert = true;
                    }
                    cardCursor.close();;
                }
                else
                {
                    needInsert = true;
                }

                if (needInsert)
                {
                    context.getContentResolver().insert(
                            CardsContract.CardEntry.CONTENT_URI,
                            newCard
                    );
                    currentTotalRows++;
                }
            }
        }
        return currentTotalRows;
    }

    /**
     * // Adrian: newly added
     * Import the .txt file data into database
     * @param fullPathOfFile The .txt file to be import
     * @return Number of rows of card imported
     */
    public static long ImportDecks(Context context, String fullPathOfFile) {
        long totalRows = 0;
        try
        {
                /*
                    Example data
                    Business:
                    Customer|A person possibly interested in buying your product
                    Manager|A person who manages activities within an organization

                    Game:
                    Ping Pong|Table tennis
                    Badminton|A game play by either 2 or 4 people
                */
            File txtFile = new File(fullPathOfFile);
            BufferedReader br = new BufferedReader(new FileReader(txtFile));
            String singleLine = "";
            String currentDeckName = "";
            long currentDeckId = 0 ;
            Vector<String[]> currentCards = new Vector<String[]>();

            StringTokenizer deckToknizer = null;
            while ((singleLine = br.readLine()) != null)
            {
                String trimmed = singleLine.trim();
                if ((!(trimmed.contains("|"))) && (trimmed.endsWith(":"))) // found a Deck
                {
                    deckToknizer = new StringTokenizer(trimmed,":");
                    currentDeckName = deckToknizer.nextToken();
                    currentDeckName = currentDeckName.trim();
                }
                else if (trimmed.length() == 0)
                {
                    // this is assume to be end of a single deck
                    // so we do the insert of a single deck with it's card here

                    totalRows = insertADeckWithCards(context, currentDeckName, currentCards, totalRows);

                    currentDeckId = 0;
                    currentDeckName = "";
                    currentCards = new Vector();
                }
                else
                {
                    if (currentDeckName.length() > 0)
                    {
                        // we can start to look for cards here
                        if (trimmed.length() > 0)
                        {
                            StringTokenizer cardTokenizer = new StringTokenizer(trimmed,"|");
                            String term = cardTokenizer.nextToken();
                            String description = cardTokenizer.nextToken();

                            String values[] = new String[2];
                            values[0] = term.trim();
                            values[1] = description.trim();
                            currentCards.add(values);
                        }
                    }
                }

            }
            br.close();

            if (currentCards.size() > 0)
            {
                totalRows = insertADeckWithCards(context, currentDeckName, currentCards, totalRows);
            }
        }
        catch (Exception ex)
        {
            Log.v(context.getClass().getSimpleName(), "Import ex " + ex.getMessage());
        }
        return totalRows;
    }

    /**
     * // Adrian: newly added
     * Export the selected decks with it's cards to .txt file
     * @param context The app context
     * @param id The list of deck's id
     * @param fileName The fullpath file name of the .txt file
     */
    public static void ExportDeck(Context context, Vector<Long> id, String fileName) {
        int deckCounter = 0;

        try {

            BufferedWriter br = new BufferedWriter(new FileWriter(fileName));

            for (Long deckId : id) {
                String deckName = getDeckName(context, deckId);

                Cursor cursor = context.getContentResolver().query(
                        CardsContract.CardEntry.buildCardWithDeckID(deckId),
                        new String[]{CardsContract.CardEntry._ID, CardsContract.CardEntry.COLUMN_TERM, CardsContract.CardEntry.COLUMN_DESCRIPTION},
                        null,
                        null,
                        null
                );

                if (deckCounter > 0) {
                    br.newLine();
                }
                br.write(deckName + ":");
                br.newLine();
                Vector<Long> cardsToExport = new Vector<Long>();
                if (cursor.moveToFirst()) {
                    do {
                        //cardsToExport.add(cursor.getLong(cursor.getColumnIndex(CardsContract.CardEntry._ID)));
                        String term = cursor.getString(cursor.getColumnIndex(CardsContract.CardEntry.COLUMN_TERM));
                        String description = cursor.getString(cursor.getColumnIndex(CardsContract.CardEntry.COLUMN_DESCRIPTION));
                        br.write(term + "|" + description);
                        br.newLine();
                    } while (cursor.moveToNext());
                }
                cursor.close();
                deckCounter++;
            }
            br.flush();
            br.close();
        } catch (Exception ex) {

        }

        Log.v(context.getClass().getSimpleName(), deckCounter + " deck(s) exported");
    }
}

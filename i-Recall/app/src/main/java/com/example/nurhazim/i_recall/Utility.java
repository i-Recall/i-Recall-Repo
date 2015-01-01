package com.example.nurhazim.i_recall;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.nurhazim.i_recall.data.CardProvider;
import com.example.nurhazim.i_recall.data.CardsContract;
import com.example.nurhazim.i_recall.data.CardsDbHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.Vector;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

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

    public static String GetDate(Context context){
        Calendar currentDate = Calendar.getInstance();
        int day = currentDate.get(Calendar.DAY_OF_MONTH);
        int month = currentDate.get(Calendar.MONTH);
        int year = currentDate.get(Calendar.YEAR);

        String[] months = context.getResources().getStringArray(R.array.months);
        return String.valueOf(day) + ", " + months[month] + " " + String.valueOf(year);
    }

    public static List<String> GetArrayListOfDecks(Context context){
        Cursor deckCursor = context.getContentResolver().query(
                CardsContract.DeckEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        List<String> allDecks = new ArrayList<String>(deckCursor.getCount());

        if(deckCursor.moveToFirst()) {
            do {
                allDecks.add(deckCursor.getString(deckCursor.getColumnIndex(CardsContract.DeckEntry.COLUMN_DECK_NAME)));
            } while (deckCursor.moveToNext());
        }
        else{
            allDecks.add("No decks to study with");
        }
        return allDecks;
    }

    public static List<String> GetArrayListOfStudyMethods(){
        List<String> allStudyMethod = new ArrayList<String>();

        //make sure this arrangement as the one in StudyActivity
        allStudyMethod.add("Flashcards");
        allStudyMethod.add("True / False");
        allStudyMethod.add("Game");

        return allStudyMethod;
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

    /**
     * // Adrian: newly added
     * Export the selected decks with it's cards to .xls file
     * @param context The app context
     * @param id The list of deck's id
     * @param fileName The fullpath file name of the .txt file
     */
    public static void ExportDeckToXls(Context context, Vector<Long> id, String fileName){
        int deckCounter = 0;

        try
        {
            // create a new xls workbook file here
            WritableWorkbook workbook = Workbook.createWorkbook(new File(fileName));

            for(Long deckId : id){
                String deckName = getDeckName(context,deckId);
                //create a sheet for each deck
                WritableSheet sheet = workbook.createSheet(deckName, deckCounter);

                Cursor cursor = context.getContentResolver().query(
                        CardsContract.CardEntry.buildCardWithDeckID(deckId),
                        new String[]{CardsContract.CardEntry._ID, CardsContract.CardEntry.COLUMN_TERM,CardsContract.CardEntry.COLUMN_DESCRIPTION},
                        null,
                        null,
                        null
                );

                Vector<Long> cardsToExport = new Vector<Long>();
                int rowCounter = 0;
                if(cursor.moveToFirst()){
                    do{
                        //cardsToExport.add(cursor.getLong(cursor.getColumnIndex(CardsContract.CardEntry._ID)));
                        String term = cursor.getString(cursor.getColumnIndex(CardsContract.CardEntry.COLUMN_TERM));
                        String description = cursor.getString(cursor.getColumnIndex(CardsContract.CardEntry.COLUMN_DESCRIPTION));

                        // create cell labels
                        Label termLabel = new Label(0, rowCounter, term);
                        Label descriptionLabel = new Label(1, rowCounter, description);
                        // add cell labels to cells
                        sheet.addCell(termLabel);
                        sheet.addCell(descriptionLabel);
                        rowCounter++;
                    }while(cursor.moveToNext());
                }
                cursor.close();
                deckCounter++;
            }
            workbook.write();
            workbook.close();
        }
        catch (Exception ex)
        {

        }

        Log.v(context.getClass().getSimpleName(), deckCounter + " deck(s) exported to xls");
    }

    // Adrian: newly added
    public static String getNowDateTimeString()
    {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(cal.getTime());
    }

    // Adrian: newly added
    public static String getNowDateTimeStringNoSymbols()
    {
        String result = getNowDateTimeString();
        result = result.replace("-", "");
        result = result.replace(" ", "");
        result = result.replace(":", "");

        return result;
    }

    /**
     * // Adrian: newly added
     * Do the real backup of database here
     * @param context Context of the app
     * @param fullPathOfDirectory The directory to save the backup file
     * @return The full backup file string
     */
    public static String BackupDatabase(Context context, String fullPathOfDirectory) {
        try
        {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite())
            {
                // our current database file path
                String currentDBPath = "//data//com.example.nurhazim.i_recall//databases//" + CardsDbHelper.DATABASE_NAME;
                //String backupDBPath = CardsDbHelper.DATABASE_NAME + "_" + getNowDateTimeStringNoSymbols() + ".sql";
                String backupDBPath = "cards_" + getNowDateTimeStringNoSymbols() + ".db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(fullPathOfDirectory, backupDBPath);

                // copy the existing database file path to backup path
                if (currentDB.exists())
                {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    return backupDB.getAbsolutePath();
                }
            }

        }
        catch (Exception ex)
        {

        }
        return "";
    }

    /**
     * // Adrian: newly added
     * Do the real database restore here
     * @param context The context of the app
     * @param fullPathOfFile The full path of the restore file
     * @return 0 for failure, 1 for success
     */
    public static long RestoreDatabase(Context context, String fullPathOfFile) {
        long result = 0;
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                // path of exiting database file
                String currentDBPath = "//data//com.example.nurhazim.i_recall//databases//" + CardsDbHelper.DATABASE_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(fullPathOfFile);

                // copy the restore file into existing database file, overwrite existing database file
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    // after the database copy is done, we need to reset the database to load the newly overwritten database
                    ContentResolver resolver = context.getContentResolver();
                    ContentProviderClient client = resolver.acquireContentProviderClient("myAuthority");
                    CardProvider provider = (CardProvider) client.getLocalContentProvider();
                    provider.resetDatabase();
                    client.release();

                    result = 1;
                }
            }
        } catch (Exception ex) {

        }
        return result;
    }
}

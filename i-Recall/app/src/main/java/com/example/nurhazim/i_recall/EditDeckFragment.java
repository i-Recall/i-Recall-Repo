package com.example.nurhazim.i_recall;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.nurhazim.i_recall.data.CardsContract;

import java.util.Vector;

/**
 * Created by NurHazim on 17-Oct-14.
 */
public class EditDeckFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = EditDeckFragment.class.getSimpleName();

    private String mCurrentDeck;
    private CardsAdapter mCardAdapter;
    private boolean mTitleEdited = false;
    private boolean mCardEdited = false;

    private static final int CARD_LOADER = 0;

    private static final String[] CARD_COLUMNS = {
            CardsContract.CardEntry.TABLE_NAME + "." + CardsContract.CardEntry._ID,
            CardsContract.CardEntry.COLUMN_TERM,
            CardsContract.CardEntry.COLUMN_DESCRIPTION,
            CardsContract.CardEntry.COLUMN_DECK_KEY
    };

    public static final int COL_CARD_ID = 0;
    public static final int COL_CARD_TERM = 1;
    public static final int COL_CARD_DESCRIPTION = 2;
    public static final int COL_DECK_ID = 3;

    public EditDeckFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mCardAdapter = new CardsAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_edit_deck, container, false);
        Intent intent = getActivity().getIntent();
        if(intent != null && intent.hasExtra(SingleDeckFragment.DECK_NAME_KEY)){
            mCurrentDeck = intent.getStringExtra(SingleDeckFragment.DECK_NAME_KEY);
            populateDeckAndCards(rootView);
        }
        EditText newDescription = (EditText) rootView.findViewById(R.id.new_card_description);
        newDescription.addTextChangedListener(new SimpleTextWatcher(newDescription));

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        long DeckId = Utility.getDeckId(getActivity(), mCurrentDeck);
        return new CursorLoader(
                getActivity(),
                CardsContract.CardEntry.buildCardWithDeckID(DeckId),
                CARD_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCardAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCardAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CARD_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void populateDeckAndCards(View view){
        EditText deckNameText = (EditText) view.findViewById(R.id.edit_deck_name_text);
        deckNameText.setText(mCurrentDeck);
        deckNameText.addTextChangedListener(new SimpleTextWatcher(deckNameText));

        ListView editCardsList = (ListView) view.findViewById(R.id.edit_deck_list_view);
        editCardsList.setAdapter(mCardAdapter);
    }

    public boolean allowBackPressed(){
        if(mTitleEdited || mCardEdited){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_save_message)
                    .setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UpdateDeckName();
                            UpdateCards();
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();
                        }
                    });
            AlertDialog saveDialog = builder.create();
            saveDialog.show();
            return false;
        }
        else{
            return true;
        }
    }

    private void UpdateDeckName(){
        EditText editDeckName = (EditText) getActivity().findViewById(R.id.edit_deck_name_text);

        if(mTitleEdited){
            Log.v(LOG_TAG, "Updating Deck Name");
            ContentValues updatedDeck = new ContentValues();
            updatedDeck.put(CardsContract.DeckEntry.COLUMN_DECK_NAME, editDeckName.getText().toString());

            getActivity().getContentResolver().update(
                    CardsContract.DeckEntry.CONTENT_URI,
                    updatedDeck,
                    CardsContract.DeckEntry._ID + " = ?",
                    new String[]{String.valueOf(Utility.getDeckId(getActivity(), mCurrentDeck))}
            );
            mCurrentDeck = editDeckName.getText().toString();
        }
    }

    private void DeleteCards(Vector<Long> id){
        for(Long cardId : id){
            getActivity().getContentResolver().delete(
                    CardsContract.CardEntry.CONTENT_URI,
                    CardsContract.CardEntry._ID + " = ?",
                    new String[]{String.valueOf(cardId)}
            );
        }
    }

    private void addNewCard(){
        EditText newTerm = (EditText) getActivity().findViewById(R.id.new_card_term);
        EditText newDescription = (EditText) getActivity().findViewById(R.id.new_card_description);

        ContentValues newCard = new ContentValues();
        newCard.put(CardsContract.CardEntry.COLUMN_TERM, newTerm.getText().toString().trim());
        newCard.put(CardsContract.CardEntry.COLUMN_DESCRIPTION, newDescription.getText().toString().trim());
        newCard.put(CardsContract.CardEntry.COLUMN_DECK_KEY, Utility.getDeckId(getActivity(), mCurrentDeck));

        getActivity().getContentResolver().insert(
                CardsContract.CardEntry.CONTENT_URI,
                newCard
        );

        newTerm.setText("");
        newDescription.setText("");
    }

    private void UpdateCards(){
        if(mCardEdited) {
            Log.v(LOG_TAG, "Updating Cards");
            mCardAdapter.getCursor().moveToFirst();
            for (int i = 0; i < mCardAdapter.getCount(); i++) {
                ListView listView = (ListView) getActivity().findViewById(R.id.edit_deck_list_view);
                LinearLayout listViewItem = (LinearLayout) listView.getChildAt(i);
                EditText editTerm = (EditText) listViewItem.findViewById(R.id.edit_card_term);
                EditText editDescription = (EditText) listViewItem.findViewById(R.id.edit_card_description);

                long cardId = mCardAdapter.getCursor().getLong(mCardAdapter.getCursor().getColumnIndex(CardsContract.CardEntry._ID));

                ContentValues updatedCard = new ContentValues();
                updatedCard.put(CardsContract.CardEntry.COLUMN_TERM, editTerm.getText().toString());
                updatedCard.put(CardsContract.CardEntry.COLUMN_DESCRIPTION, editDescription.getText().toString());

                getActivity().getContentResolver().update(
                        CardsContract.CardEntry.CONTENT_URI,
                        updatedCard,
                        CardsContract.CardEntry._ID + " = ?",
                        new String[]{String.valueOf(cardId)}
                );
                mCardAdapter.getCursor().moveToNext();
            }
        }
    }

    private class CardsAdapter extends CursorAdapter {
        EditText mEditTerm, mEditDescription;

        public CardsAdapter(Context context, Cursor cursor, int flags){
            super(context, cursor, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_edit_deck, parent, false);

            mEditTerm = (EditText) view.findViewById(R.id.edit_card_term);
            mEditDescription = (EditText) view.findViewById(R.id.edit_card_description);

            mEditTerm.setText(cursor.getString(EditDeckFragment.COL_CARD_TERM));
            mEditDescription.setText(cursor.getString(EditDeckFragment.COL_CARD_DESCRIPTION));

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            mEditTerm.addTextChangedListener(new SimpleTextWatcher(mEditTerm));
            mEditDescription.addTextChangedListener(new SimpleTextWatcher(mEditDescription));

            return view;
        }

        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }
    }

    private class SimpleTextWatcher implements TextWatcher{
        private View view;
        private SimpleTextWatcher(View view){
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            switch(view.getId()){
                case R.id.edit_deck_name_text:
                    mTitleEdited = false;
                    break;
                case R.id.edit_card_term:
                case R.id.edit_card_description:
                    mCardEdited = false;
                    break;
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            switch(view.getId()){
                case R.id.edit_deck_name_text:
                    mTitleEdited = true;
                    break;
                case R.id.edit_card_term:
                case R.id.edit_card_description:
                    mCardEdited = true;
                    break;
                case R.id.new_card_description:
                    if(Utility.hasNewLine(s)){
                        addNewCard();
                    }
                    break;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}

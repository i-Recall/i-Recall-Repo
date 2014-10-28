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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.nurhazim.i_recall.data.CardsContract;

/**
 * Created by NurHazim on 17-Oct-14.
 */
public class EditDeckFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String mCurrentDeck;
    private CardsAdapter mCardAdapter;
    private boolean mEdited = false;

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
        if(mEdited){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_save_message)
                    .setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UpdateData();
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

    private void UpdateData(){
        EditText editDeckName = (EditText) getActivity().findViewById(R.id.edit_deck_name_text);

        if(!editDeckName.getText().toString().equals(mCurrentDeck)){
            ContentValues updatedDeck = new ContentValues();
            updatedDeck.put(CardsContract.DeckEntry.COLUMN_DECK_NAME, editDeckName.getText().toString());

            getActivity().getContentResolver().update(
                    CardsContract.DeckEntry.CONTENT_URI,
                    updatedDeck,
                    CardsContract.DeckEntry._ID + " = ?",
                    new String[]{String.valueOf(Utility.getDeckId(getActivity(), mCurrentDeck))}
            );
            mCurrentDeck = editDeckName.getText().toString();
            //change the SingleDeckFragment Toolbar title to new edited deck name
        }

//            mCardAdapter.mCursor = getActivity().getContentResolver().query(
//                    CardsContract.CardEntry.buildCardWithDeckID(Utility.getDeckId(getActivity(), mCurrentDeck)),
//                    CARD_COLUMNS,
//                    null,
//                    null,
//                    null
//            );
        mCardAdapter.getCursor().moveToFirst();
        for(int i = 0; i < mCardAdapter.getCount(); i++){
            LinearLayout listViewItem = (LinearLayout) mCardAdapter.getItem(i);
            EditText editTerm = (EditText) listViewItem.findViewById(R.id.edit_card_term);
            EditText editDescription = (EditText) listViewItem.findViewById(R.id.edit_card_description);

            ContentValues updatedCard = new ContentValues();
            updatedCard.put(CardsContract.CardEntry.COLUMN_TERM, editTerm.getText().toString());
            updatedCard.put(CardsContract.CardEntry.COLUMN_DESCRIPTION, editDescription.getText().toString());

            long cardId = mCardAdapter.getCursor().getLong(mCardAdapter.getCursor().getColumnIndex(CardsContract.CardEntry._ID));

            getActivity().getContentResolver().update(
                    CardsContract.CardEntry.CONTENT_URI,
                    updatedCard,
                    CardsContract.CardEntry._ID + " = ?",
                    new String[]{String.valueOf(cardId)}
            );
            mCardAdapter.getCursor().moveToNext();
        }
    }

    private class CardsAdapter extends CursorAdapter {
        EditText mEditTerm, mEditDescription;
        Cursor mCursor;
        Context mContext;

        public CardsAdapter(Context context, Cursor cursor, int flags){
            super(context, cursor, flags);
            mContext = context;
            mCursor = cursor;
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
                case R.id.edit_card_term:
                case R.id.edit_card_description:
                    mEdited = false;
                    break;
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            switch(view.getId()){
                case R.id.edit_deck_name_text:
                case R.id.edit_card_term:
                case R.id.edit_card_description:
                    mEdited = true;
                    break;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}

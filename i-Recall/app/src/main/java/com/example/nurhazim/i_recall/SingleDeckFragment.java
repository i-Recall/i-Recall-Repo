package com.example.nurhazim.i_recall;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.nurhazim.i_recall.data.CardsContract;

import java.util.Vector;

/**
 * Created by NurHazim on 16-Oct-14.
 */
public class SingleDeckFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = SingleDeckFragment.class.getSimpleName();

    private static final int CARD_LOADER = 0;

    public static final String DECK_NAME_KEY = "deck_name";
    private String mCurrentDeckName;
    private long mCurrentDeckId;
    private SimpleCursorAdapter mCardAdapter;

    public SingleDeckFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        long deckId;
        if(mCurrentDeckId == -1){
            deckId = Utility.getDeckId(getActivity(), mCurrentDeckName);
        }
        else{
            deckId = mCurrentDeckId;
        }

        return new CursorLoader(
                getActivity(),
                CardsContract.CardEntry.buildCardWithDeckID(deckId),
                null,
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CARD_LOADER, null, this);
        mCurrentDeckName = Utility.getDeckName(getActivity(), mCurrentDeckId);
        getActivity().setTitle(mCurrentDeckName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_edit_deck:
                Intent intent = new Intent(getActivity(), EditDeckActivity.class);
                intent.putExtra(DECK_NAME_KEY, mCurrentDeckName);
                startActivity(intent);
                return true;
            case R.id.action_delete:
                Vector<Long> toDelete = new Vector<Long>();
                toDelete.add(mCurrentDeckId);
                Utility.DeleteDeck(getActivity(), toDelete);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_single_deck, container, false);
        final ListView cardsListView = (ListView) rootView.findViewById(R.id.cards_listview);

        Intent intent = getActivity().getIntent();
        if(intent != null && intent.hasExtra(DECK_NAME_KEY)){
            mCurrentDeckName = intent.getStringExtra(DECK_NAME_KEY);
            mCurrentDeckId = Utility.getDeckId(getActivity(), mCurrentDeckName);
            Log.v(LOG_TAG, "Current deck is " + mCurrentDeckName);
            getActivity().setTitle(mCurrentDeckName);
            Cursor cursor = getCards();
            if(cursor != null){
                mCardAdapter = new SimpleCursorAdapter(
                        getActivity(),
                        R.layout.list_item_card,
                        cursor,
                        new String[]{
                                CardsContract.CardEntry.COLUMN_TERM,
                                CardsContract.CardEntry.COLUMN_DESCRIPTION
                        },
                        new int[]{
                                R.id.card_term_text,
                                R.id.card_description_text
                        },
                        CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
                );
                cardsListView.setAdapter(mCardAdapter);
            }
            cardsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            cardsListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    final int checkCount = cardsListView.getCheckedItemCount();
                    mode.setTitle(checkCount + " Selected");
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.delete_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.action_delete:
                            SparseBooleanArray checkedItems = cardsListView.getCheckedItemPositions();
                            if(checkedItems != null){
                                Vector<Long> toDelete = new Vector<Long>();
                                for(int i = 0; i < checkedItems.size(); i++){
                                    int itemIndex = checkedItems.keyAt(i);
                                    mCardAdapter.getCursor().moveToPosition(itemIndex);
                                    toDelete.add(mCardAdapter.getCursor().getLong(mCardAdapter.getCursor().getColumnIndex(CardsContract.CardEntry._ID)));
                                }
                                Utility.DeleteCards(getActivity(), toDelete);
                            }
                            mode.finish();
                            return true;
                        default:
                            return false;
                    }
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {

                }
            });

            Button btnFlashcard = (Button) rootView.findViewById(R.id.button_flashcards);
            Button btnTruefalse = (Button) rootView.findViewById(R.id.button_truefalse);

            btnFlashcard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), StudyActivity.class);
                    intent.putExtra(StudyActivity.MODE_KEY, String.valueOf(StudyActivity.MODE_FLASHCARDS));
                    intent.putExtra(DECK_NAME_KEY, mCurrentDeckName);
                    startActivity(intent);
                }
            });

            btnTruefalse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), StudyActivity.class);
                    intent.putExtra(StudyActivity.MODE_KEY, String.valueOf(StudyActivity.MODE_TRUE_FALSE));
                    intent.putExtra(DECK_NAME_KEY, mCurrentDeckName);
                    startActivity(intent);
                }
            });
        }

        return rootView;
    }

    private Cursor getCards(){
        long rowId;
        Cursor deckCursor = getActivity().getContentResolver().query(
                CardsContract.DeckEntry.buildDeckWithName(mCurrentDeckName),
                null,
                null,
                null,
                null
        );
        if(deckCursor.moveToFirst()){
            rowId = deckCursor.getLong(deckCursor.getColumnIndex(CardsContract.DeckEntry._ID));
            return getActivity().getContentResolver().query(
                    CardsContract.CardEntry.buildCardWithDeckID(rowId),
                    null,
                    null,
                    null,
                    null
            );
        }
        else{
            return null;
        }
    }
}

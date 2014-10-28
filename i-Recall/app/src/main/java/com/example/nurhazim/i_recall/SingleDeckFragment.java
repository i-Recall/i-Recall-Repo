package com.example.nurhazim.i_recall;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.nurhazim.i_recall.data.CardsContract;

/**
 * Created by NurHazim on 16-Oct-14.
 */
public class SingleDeckFragment extends Fragment {
    private static final String LOG_TAG = SingleDeckFragment.class.getSimpleName();

    public static final String DECK_NAME_KEY = "deck_name";
    private String mCurrentDeckName;
    private long mCurrentDeckId;
    private SimpleCursorAdapter mCardAdapter;

    public SingleDeckFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentDeckName = Utility.getDeckName(getActivity(), mCurrentDeckId);
        getActivity().setTitle(mCurrentDeckName);
        Cursor cursor = getActivity().getContentResolver().query(
                CardsContract.CardEntry.buildCardWithDeckID(mCurrentDeckId),
                null,
                null,
                null,
                null
        );
        mCardAdapter.swapCursor(cursor);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_edit_deck){
            Intent intent = new Intent(getActivity(), EditDeckActivity.class);
            intent.putExtra(DECK_NAME_KEY, mCurrentDeckName);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_single_deck, container, false);
        ListView cardsListView = (ListView) rootView.findViewById(R.id.cards_listview);

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
            Cursor cardCursor = getActivity().getContentResolver().query(
                    CardsContract.CardEntry.buildCardWithDeckID(rowId),
                    null,
                    null,
                    null,
                    null
            );
            return cardCursor;
        }
        else{
            return null;
        }
    }
}

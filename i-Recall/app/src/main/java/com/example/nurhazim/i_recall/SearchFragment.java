package com.example.nurhazim.i_recall;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.nurhazim.i_recall.data.CardsContract;

/**
 * This is the search fragment that implements the searching of all card term in database
 */
public class SearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int SEARCH_LOADER = 0;
    SimpleCursorAdapter mDeckAdapter; // the data adapter

    public SearchFragment() {
        // Required empty public constructor
    }

    // part of LoaderCallbacks methods to Override to perform the implementation of LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String passedInSearchText = null;
        // retrieve the arguments passed in via Bundle if available
        if (args != null)
        {
            // get the search string here
            passedInSearchText = args.getString("searchText");
        }
        String wherePartInQuery = null;
        if (passedInSearchText != null)
        {
            // here we generate the WHERE part of sql query (excluding the WHERE keyword) that search for the card term
            wherePartInQuery = CardsContract.CardEntry.COLUMN_TERM + " LIKE '%" + passedInSearchText + "%'";
        }
        // call the new CursorLoader to load data
        return new CursorLoader(
                getActivity(),
                CardsContract.CardEntry.buildDeckWithCardTermSearchString(passedInSearchText),
                null,
                wherePartInQuery,
                null,
                null
        );
    }

    // part of LoaderCallbacks methods to Override to perform the implementation of LoaderCallbacks
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mDeckAdapter.swapCursor(data);
    }

    // part of LoaderCallbacks methods to Override to perform the implementation of LoaderCallbacks
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDeckAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // set the menu
        inflater.inflate(R.menu.search_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(R.string.dialog_hint_search_string);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        // this is the search result ListView
        final ListView cardListView = (ListView) rootView.findViewById(R.id.result_cards_listview);

        // initialize the data adapter
        mDeckAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_card,
                null,
                new String[]{
                        CardsContract.CardEntry.COLUMN_TERM
                },
                new int[]{
                        R.id.card_term_text
                },
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        cardListView.setAdapter(mDeckAdapter);

        // set the action to be done when user click on one of the search result item
        cardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) // user click on one of the item
            {
                // get the data
                SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();
                Cursor cursor = adapter.getCursor();
                cursor.moveToPosition(position);

                // get the id
                long cardId = cursor.getLong(0);
                long deckId = cursor.getLong(2);
                // create and launch SingleDeckActivity
                Intent intent = new Intent(getActivity(), SingleDeckActivity.class);
                intent.putExtra(SingleDeckFragment.DECK_NAME_KEY, Utility.getDeckName(SearchFragment.this.getActivity(),deckId));
                startActivity(intent);

            }
        });

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_search_deck){ // user click on the search menu icon
            // create and show the pop up search dialog fragment
            SearchCardFragment dialog = new SearchCardFragment();
            dialog.setFragment(this);
            dialog.show(getFragmentManager(), "Search Card");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(SEARCH_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // this is where we initiate the search operation
    public void doSearch(String searchText)
    {
        // create new Bundle so that we can pass in the search string parameter
        Bundle values = new Bundle();
        // set the search string parameter
        values.putString("searchText", searchText);
        // restart the Loader to do searching
        // this will eventually call onLoaderReset(), onCreateLoader() and onLoadFinished() depends on the status
        getLoaderManager().restartLoader(SEARCH_LOADER,values, this);
    }


    // this is the search pop up DialogFragment that shows up when user click on the search image on top right
    public static class SearchCardFragment extends DialogFragment {
        private static final String LOG_TAG = SearchCardFragment.class.getSimpleName();
        Fragment fragment = null;

        public SearchCardFragment()
        {
        }

        // keep the calling parent here, which is SearchFragment
        public void setFragment(Fragment fragment)
        {
            this.fragment = fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_search_card, null))
                    .setTitle("Search Card")
                    .setPositiveButton(R.string.dialog_button_search, new DialogInterface.OnClickListener() { // set the "Search" button
                        @Override
                        public void onClick(DialogInterface dialog, int which) { // handles user click on "Search" button
                            Dialog f = (Dialog) dialog;
                            // get the search string
                            EditText searchEditText = (EditText) f.findViewById(R.id.dialog_new_deck_search_text);
                            Log.v(LOG_TAG, "search card " + searchEditText.getText().toString());
                            // call the search method here
                            ((SearchFragment)fragment).doSearch(searchEditText.getText().toString());
                        }
                    })
                    .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() { // handles user click on "Cancel" button
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SearchCardFragment.this.getDialog().cancel();
                        }
                    });
            Dialog dialogNewDeck = builder.create();

            //to make keyboard appear
            dialogNewDeck.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            );
            return dialogNewDeck;
        }
    }

}

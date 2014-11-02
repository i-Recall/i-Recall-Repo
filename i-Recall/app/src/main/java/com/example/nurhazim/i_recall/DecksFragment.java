package com.example.nurhazim.i_recall;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
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
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.nurhazim.i_recall.data.CardsContract;

import java.util.Vector;

/**
 * Created by NurHazim on 15-Oct-14.
 */
public class DecksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    SimpleCursorAdapter mDeckAdapter;
    private static final int DECK_LOADER = 0;

    private static final String LOG_TAG = DecksFragment.class.getSimpleName();

    public DecksFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                CardsContract.DeckEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mDeckAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDeckAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.deck_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DECK_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_new_deck){
            NewDeckFragment dialog = new NewDeckFragment();
            dialog.show(getFragmentManager(), "New Deck");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(R.string.title_decks);

        Cursor cursor = getActivity().getContentResolver().query(
                CardsContract.DeckEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        Log.v(LOG_TAG, "cursor count is " + cursor.getCount());

        mDeckAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_deck,
                cursor,
                new String[]{
                        CardsContract.DeckEntry.COLUMN_DECK_NAME
                },
                new int[]{
                        R.id.deck_name_text
                },
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        View rootView = inflater.inflate(R.layout.fragment_decks, container, false);

        final ListView deckListView = (ListView) rootView.findViewById(R.id.decks_listview);
        deckListView.setAdapter(mDeckAdapter);

        deckListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        deckListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final int checkCount = deckListView.getCheckedItemCount();
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
                        SparseBooleanArray checkedItems = deckListView.getCheckedItemPositions();
                        if(checkedItems != null){
                            Vector<Long> toDelete = new Vector<Long>();
                            for(int i = 0; i < checkedItems.size(); i++){
                                int itemIndex = checkedItems.keyAt(i);
                                mDeckAdapter.getCursor().moveToPosition(itemIndex);
                                toDelete.add(mDeckAdapter.getCursor().getLong(mDeckAdapter.getCursor().getColumnIndex(CardsContract.DeckEntry._ID)));
                            }
                            Utility.DeleteDeck(getActivity(), toDelete);
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

        deckListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();
                Cursor cursor = adapter.getCursor();
                cursor.moveToPosition(position);

                Intent intent = new Intent(getActivity(), SingleDeckActivity.class);
                intent.putExtra(SingleDeckFragment.DECK_NAME_KEY, cursor.getString(cursor.getColumnIndex(CardsContract.DeckEntry.COLUMN_DECK_NAME)));
                startActivity(intent);
            }
        });

        return rootView;
    }

    public static class NewDeckFragment extends DialogFragment{
        private static final String LOG_TAG = NewDeckFragment.class.getSimpleName();
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_new_deck, null))
                    .setTitle("New Deck")
                    .setPositiveButton(R.string.dialog_button_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Dialog f = (Dialog) dialog;
                            EditText deckName = (EditText) f.findViewById(R.id.dialog_new_deck_name_text);

                            ContentValues newDeck = new ContentValues();

                            newDeck.put(CardsContract.DeckEntry.COLUMN_DECK_NAME, deckName.getText().toString());
                            Uri insertUri = getActivity().getContentResolver().insert(CardsContract.DeckEntry.CONTENT_URI, newDeck);
                            Log.v(LOG_TAG, "new deck " + deckName.getText().toString() + " inserted at " + ContentUris.parseId(insertUri));
                        }
                    })
                    .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NewDeckFragment.this.getDialog().cancel();
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

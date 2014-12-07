package com.example.nurhazim.i_recall;

import android.app.Activity;
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
import android.widget.Toast;

import com.example.nurhazim.i_recall.data.CardsContract;

import java.util.Vector;


public class ImportExportFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    SimpleCursorAdapter mDeckAdapter;
    private static final int DECK_LOADER = 0;
    public static final int PICK_IMPORT_TEXT_FILE = 1;
    public static final int PICK_EXPORT_TEXT_FILE = 2;
    Vector<Long> toExport;

    private static final String LOG_TAG = DecksFragment.class.getSimpleName();

    public ImportExportFragment() {
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
        inflater.inflate(R.menu.import_deck_menu, menu);
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
        if(id == R.id.action_import_deck){
            doImport();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doImport()
    {
        Intent intent = new Intent(this.getActivity(), FileDialog.class);
        intent.putExtra(FileDialog.START_PATH, "/sdcard");

        //can user select directories or not
        intent.putExtra(FileDialog.CAN_SELECT_DIR, false);

        startActivityForResult(intent, PICK_IMPORT_TEXT_FILE);
    }

    private void doExport()
    {
        Intent intent = new Intent(this.getActivity(), FileDialog.class);
        intent.putExtra(FileDialog.START_PATH, "/sdcard");

        //can user select directories or not
        intent.putExtra(FileDialog.CAN_SELECT_DIR, false);

        intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_CREATE);

        startActivityForResult(intent, PICK_EXPORT_TEXT_FILE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch (requestCode)
        {
            case (PICK_IMPORT_TEXT_FILE):
                if (resultCode == Activity.RESULT_OK) {
                    try {

                        String filePath = intent.getStringExtra(FileDialog.RESULT_PATH);
                        Toast.makeText(this.getActivity(), "Import file path " + filePath, Toast.LENGTH_SHORT).show();
                        Utility.ImportDecks(this.getActivity(),filePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this.getActivity(), e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case (PICK_EXPORT_TEXT_FILE):
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        String filePath = intent.getStringExtra(FileDialog.RESULT_PATH);
                        Toast.makeText(this.getActivity(), "Export file path " + filePath, Toast.LENGTH_SHORT).show();
                        Utility.ExportDeck(getActivity(), toExport,filePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this.getActivity(), e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(R.string.title_import_export_decks);

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

        View rootView = inflater.inflate(R.layout.fragment_import_export, container, false);

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
                inflater.inflate(R.menu.export_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.action_export_deck:
                        SparseBooleanArray checkedItems = deckListView.getCheckedItemPositions();
                        if(checkedItems != null){
                            toExport = new Vector<Long>();
                            for(int i = 0; i < checkedItems.size(); i++){
                                int itemIndex = checkedItems.keyAt(i);
                                mDeckAdapter.getCursor().moveToPosition(itemIndex);
                                toExport.add(mDeckAdapter.getCursor().getLong(mDeckAdapter.getCursor().getColumnIndex(CardsContract.DeckEntry._ID)));
                            }
                            doExport();
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

}
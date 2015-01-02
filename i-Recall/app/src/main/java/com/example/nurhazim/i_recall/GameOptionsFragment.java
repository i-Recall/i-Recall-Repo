package com.example.nurhazim.i_recall;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.nurhazim.i_recall.data.CardsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NurHazim on 18-Nov-14.
 */
public class GameOptionsFragment extends Fragment {
    private final String STRING_USER = "User";
    private static final String STRING_NEW_PLAYER = "New Player..";

    static List<String> mPlayers = new ArrayList<String>();
    static ArrayAdapter<String> mSpinnerPlayerAdapter;

    @Override
    public void onDestroy() {
        mPlayers.clear();
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Cursor playerCursor = getActivity().getContentResolver().query(
                CardsContract.PlayerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        mPlayers.add(STRING_USER);
        if(playerCursor.moveToFirst()){
            while(playerCursor.moveToNext()){
                mPlayers.add(playerCursor.getString(playerCursor.getColumnIndex(CardsContract.PlayerEntry.COLUMN_PLAYER_NAME)));
            }
        }
        mPlayers.add(STRING_NEW_PLAYER);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.game_options_fragment, container, false);

        final Spinner spinnerPlayer1 = (Spinner) rootView.findViewById(R.id.spinner_player1);
        final Spinner spinnerPlayer2 = (Spinner) rootView.findViewById(R.id.spinner_player2);
        final Spinner spinnerDeck = (Spinner) rootView.findViewById(R.id.spinner_deck);

        List<String> decks = Utility.GetArrayListOfDecks(getActivity());

        mSpinnerPlayerAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                mPlayers
        );

        final ArrayAdapter<String> spinnerDecksAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                decks
        );

        mSpinnerPlayerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDecksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerPlayer1.setAdapter(mSpinnerPlayerAdapter);
        spinnerPlayer2.setAdapter(mSpinnerPlayerAdapter);
        spinnerPlayer2.setSelection(1);
        spinnerPlayer1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == mPlayers.size()-1){
                    NewPlayerDialogFragment dialog = new NewPlayerDialogFragment();
                    dialog.show(getFragmentManager(), "New Player");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerDeck.setAdapter(spinnerDecksAdapter);

        Button buttonStart = (Button) rootView.findViewById(R.id.button_start_game);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), StudyActivity.class);
                intent.putExtra(StudyActivity.MODE_KEY, String.valueOf(StudyActivity.MODE_GAME));
                intent.putExtra(SingleDeckFragment.DECK_NAME_KEY, spinnerDeck.getSelectedItem().toString());
                if(spinnerPlayer1.getSelectedItem().toString().equals(STRING_USER) && spinnerPlayer2.getSelectedItem().toString().equals(STRING_USER)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setMessage(getResources().getString(R.string.dialog_message_error_two_users))
                            .setTitle(R.string.dialog_error)
                            .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create().show();
                }
                else if(spinnerPlayer1.getSelectedItem().toString().equals(STRING_USER)){
                    intent.putExtra(StudyGameFragment.USER_KEY, 1);
                    startActivity(intent);
                }
                else if(spinnerPlayer2.getSelectedItem().toString().equals(STRING_USER)){
                    intent.putExtra(StudyGameFragment.USER_KEY, 2);
                    startActivity(intent);
                }
                else{
                    startActivity(intent);
                }
            }
        });

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_game_option, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_online_game:
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class NewPlayerDialogFragment extends DialogFragment {
        private static final String LOG_TAG = NewPlayerDialogFragment.class.getSimpleName();
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_new_player, null))
                    .setTitle("New Player")
                    .setPositiveButton(R.string.dialog_button_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Dialog f = (Dialog) dialog;
                            EditText playerName = (EditText) f.findViewById(R.id.dialog_new_player_text);

                            ContentValues newPlayer = new ContentValues();

                            newPlayer.put(CardsContract.PlayerEntry.COLUMN_PLAYER_NAME, playerName.getText().toString());
                            Uri insertUri = getActivity().getContentResolver().insert(CardsContract.PlayerEntry.CONTENT_URI, newPlayer);
                            Log.v(LOG_TAG, "new player " + playerName.getText().toString() + " inserted at " + ContentUris.parseId(insertUri));
                            mPlayers.remove(mPlayers.size() - 1);
                            mPlayers.add(playerName.getText().toString());
                            mPlayers.add(STRING_NEW_PLAYER);
                            mSpinnerPlayerAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NewPlayerDialogFragment.this.getDialog().cancel();
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

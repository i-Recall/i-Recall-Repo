package com.example.nurhazim.i_recall;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.nurhazim.i_recall.data.CardsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NurHazim on 18-Nov-14.
 */
public class GameOptionsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.game_options_fragment, container, false);

        final Spinner spinnerPlayer1 = (Spinner) rootView.findViewById(R.id.spinner_player1);
        final Spinner spinnerPlayer2 = (Spinner) rootView.findViewById(R.id.spinner_player2);
        final Spinner spinnerDeck = (Spinner) rootView.findViewById(R.id.spinner_deck);

        List<String> players = new ArrayList<String>();

        players.add("Jack");
        players.add("Jason");
        players.add("Jim");
        players.add("John");
        players.add("Jeep");

        Cursor cursor = getActivity().getContentResolver().query(
                CardsContract.DeckEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        List<String> decks = new ArrayList<String>();

        do{
            decks.add(cursor.getString(cursor.getColumnIndex(CardsContract.DeckEntry.COLUMN_DECK_NAME)));
        }while(cursor.moveToNext());

        ArrayAdapter<String> spinnerPlayerAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                players
        );

        final ArrayAdapter<String> spinnerDecksAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                decks
        );

        spinnerPlayerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDecksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerPlayer1.setAdapter(spinnerPlayerAdapter);
        spinnerPlayer2.setAdapter(spinnerPlayerAdapter);
        spinnerDeck.setAdapter(spinnerDecksAdapter);

        Button buttonStart = (Button) rootView.findViewById(R.id.button_start_game);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), StudyActivity.class);
                intent.putExtra(StudyActivity.MODE_KEY, String.valueOf(StudyActivity.MODE_GAME));
                intent.putExtra(SingleDeckFragment.DECK_NAME_KEY, spinnerDeck.getSelectedItem().toString());
                startActivity(intent);
            }
        });

        return rootView;
    }
}

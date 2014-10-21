package com.example.nurhazim.i_recall;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.nurhazim.i_recall.data.CardsContract;

/**
 * Created by NurHazim on 17-Oct-14.
 */
public class EditDeckFragment extends Fragment {

    private String mCurrentDeck;
    private SimpleCursorAdapter mCardAdapter;
    private boolean mEdited = false;

    public EditDeckFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_deck, container, false);
        Intent intent = getActivity().getIntent();
        if(intent != null && intent.hasExtra(SingleDeckFragment.DECK_NAME_KEY)){
            mCurrentDeck = intent.getStringExtra(SingleDeckFragment.DECK_NAME_KEY);
            populateDeckAndCards(rootView);
        }
        return rootView;
    }

    private void populateDeckAndCards(View view){
        EditText deckNameText = (EditText) view.findViewById(R.id.edit_deck_name_text);
        deckNameText.setText(mCurrentDeck);
        deckNameText.addTextChangedListener(textWatcher);

        long deckRowId = Utility.getDeckId(getActivity(), mCurrentDeck);

        Cursor cardCursor = getActivity().getContentResolver().query(
                CardsContract.CardEntry.buildCardWithDeckID(deckRowId),
                null,
                null,
                null,
                null
        );

        mCardAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_edit_deck,
                cardCursor,
                new String[]{
                        CardsContract.CardEntry.COLUMN_TERM,
                        CardsContract.CardEntry.COLUMN_DESCRIPTION
                },
                new int[]{
                        R.id.edit_card_term,
                        R.id.edit_card_description
                },
                0
        );

        ListView editCardsList = (ListView) view.findViewById(R.id.edit_deck_list_view);
        editCardsList.setAdapter(mCardAdapter);

        View listItem = getActivity().getLayoutInflater().inflate(R.layout.list_item_edit_deck, null);
        EditText editCardTerm = (EditText) listItem.findViewById(R.id.edit_card_term);
        EditText editCardDescription = (EditText) listItem.findViewById(R.id.edit_card_description);

        editCardTerm.addTextChangedListener(textWatcher);
        editCardDescription.addTextChangedListener(textWatcher);
    }

    //one TextWatcher for one EditText. Sigh.
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mEdited = false;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mEdited = true;
        }

        @Override
        public void afterTextChanged(Editable s) {
            mEdited = true;
        }
    };

    public boolean allowBackPressed(){
        if(mEdited){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_save_message)
                    .setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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


}

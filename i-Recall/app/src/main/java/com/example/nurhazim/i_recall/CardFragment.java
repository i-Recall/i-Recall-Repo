package com.example.nurhazim.i_recall;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

/**
 * Created by NurHazim on 05-Nov-14.
 */
public class CardFragment extends Fragment {
    private boolean showingFront = true;
    private String textTerm = "This is a term";
    private String textDescription = "This is a description";

    TextView cardText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_study_flashcards, container, false
        );

        Bundle bundle = getArguments();
        if(bundle != null){
            textTerm = bundle.getString(StudyFlashcardFragment.TERM_KEY);
            textDescription = bundle.getString(StudyFlashcardFragment.DESCRIPTION_KEY);
        }

        final CardView flashcard = (CardView) rootView.findViewById(R.id.flashcard);
        cardText = (TextView) flashcard.findViewById(R.id.flashcard_text);
        cardText.setText(textTerm);
        flashcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator flipOut = ObjectAnimator.ofFloat(flashcard, "rotationY", 0.0f, 90f);
                flipOut.setDuration(900);
                flipOut.setInterpolator(new AccelerateInterpolator());

                final ObjectAnimator flipIn = ObjectAnimator.ofFloat(flashcard, "rotationY", 270f, 360f);
                flipIn.setDuration(900);
                flipIn.setInterpolator(new AccelerateInterpolator());

                flipOut.start();
                flipOut.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ToggleText();
                        flipIn.start();
                    }
                });
            }
        });

        return rootView;
    }

    private void ToggleText(){
        if(showingFront){
            cardText.setText(textDescription);
            showingFront = false;
        }
        else{
            cardText.setText(textTerm);
            showingFront = true;
        }
    }
}

package com.example.nurhazim.i_recall;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by NurHazim on 05-Nov-14.
 */
public class CardFragment extends Fragment {
    private boolean showingFront = true;
    private String textTerm = "This is a term";
    private String textDescription = "This is a description";

    private boolean evaluated = false;

    TextView cardText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_study_flashcards, container, false
        );

        Bundle bundle = getArguments();
        if(bundle != null){
            textTerm = bundle.getString(StudyActivity.TERM_KEY);
            textDescription = bundle.getString(StudyActivity.DESCRIPTION_KEY);
        }

        final Button btnYes = (Button) rootView.findViewById(R.id.button_correct);
        final Button btnNo = (Button) rootView.findViewById(R.id.button_wrong);
        final TextView selfEval = (TextView) rootView.findViewById(R.id.self_evaluation_text);

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnYes.setVisibility(View.INVISIBLE);
                btnNo.setVisibility(View.INVISIBLE);
                selfEval.setVisibility(View.INVISIBLE);
                evaluated = true;
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnYes.setVisibility(View.INVISIBLE);
                btnNo.setVisibility(View.INVISIBLE);
                selfEval.setVisibility(View.INVISIBLE);
                evaluated = true;
            }
        });

        final CardView flashcard = (CardView) rootView.findViewById(R.id.cardview_flashcard);

        cardText = (TextView) flashcard.findViewById(R.id.cardview_text);

        cardText.setText(textTerm);

        flashcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ObjectAnimator ascend = ObjectAnimator.ofFloat(flashcard, "cardElevation", 5f, 50f);
                ascend.setDuration(720);
                final ObjectAnimator descend = ObjectAnimator.ofFloat(flashcard, "cardElevation", 50f, 5f);
                descend.setDuration(720);

                Animation flashcardFlip = AnimationUtils.loadAnimation(getActivity(), R.anim.flashcard_flip);
                flashcard.startAnimation(flashcardFlip);
                ascend.start();
                ascend.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        descend.start();
                        ToggleText();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                if (!evaluated) {
                    btnYes.setVisibility(View.VISIBLE);
                    btnNo.setVisibility(View.VISIBLE);
                    selfEval.setVisibility(View.VISIBLE);
                }
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

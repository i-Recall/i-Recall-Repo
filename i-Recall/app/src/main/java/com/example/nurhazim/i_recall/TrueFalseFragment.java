package com.example.nurhazim.i_recall;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by NurHazim on 18-Nov-14.
 */
public class TrueFalseFragment extends Fragment {

    public TrueFalseFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.true_false_pager, container, false);

        return rootView;
    }
}

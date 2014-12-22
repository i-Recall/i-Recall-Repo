package com.example.nurhazim.i_recall;

import android.app.Fragment;
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

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by NurHazim on 01-Dec-14.
 */
public class UserPerformanceFragment extends Fragment {
    LineChartView mLineChart;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_performance, container, false);

        final Spinner spinnerDeck = (Spinner) rootView.findViewById(R.id.spinner_deck);
        final Spinner spinnerStudyMethod = (Spinner) rootView.findViewById(R.id.spinner_study_method);
        Button buttonRefresh = (Button) rootView.findViewById(R.id.button_refresh);
        mLineChart = (LineChartView) rootView.findViewById(R.id.chart_user_performance);

        List<String> decks = Utility.GetArrayListOfDecks(getActivity());
        List<String> studyMethods = Utility.GetArrayListOfStudyMethods();

        ArrayAdapter<String> spinnerDecksAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                decks
        );
        ArrayAdapter<String> spinnerStudyMethodAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                studyMethods
        );

        spinnerDecksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudyMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerDeck.setAdapter(spinnerDecksAdapter);
        spinnerStudyMethod.setAdapter(spinnerStudyMethodAdapter);

        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopulateChart(spinnerDeck.getSelectedItem().toString(), MatchStudyMethod(spinnerStudyMethod.getSelectedItem().toString()));
            }
        });

        return rootView;
    }

    private int MatchStudyMethod(String studyMethod){
        List<String> studyMethods = Utility.GetArrayListOfStudyMethods();
        for(int i = 0; i < studyMethods.size(); ++i){
            if(studyMethod.equals(studyMethods.get(i))){
                return i;
            }
        }
        return -1;
    }

    private void PopulateChart(String deckName, int studyMethod){
        long deckId = Utility.getDeckId(getActivity(), deckName);

        Cursor performanceCursor = getActivity().getContentResolver().query(
                CardsContract.UserPerformanceEntry.buildPerformanceWithDeckIdAndStudyMethod(deckId, studyMethod),
                null,
                null,
                null,
                null
        );
        if(performanceCursor.moveToFirst()) {
            List<PointValue> values = new ArrayList<PointValue>(performanceCursor.getCount());
            List<AxisValue> axisXValues = new ArrayList<AxisValue>(performanceCursor.getCount());

            int counter = 0;
            do {
                values.add(new PointValue(counter, performanceCursor.getInt(performanceCursor.getColumnIndex(CardsContract.UserPerformanceEntry.COLUMN_DURATION))));
                axisXValues.add(new AxisValue(counter, performanceCursor.getString(performanceCursor.getColumnIndex(CardsContract.UserPerformanceEntry.COLUMN_DATE)).toCharArray()));
                ++counter;
            } while (performanceCursor.moveToNext());

            Axis axisY = new Axis();
            axisY.setName("Time Taken to Complete Deck(s)");
            Axis axisX = new Axis(axisXValues);
            axisX.setName("Session");

            Line line = new Line(values).setColor(getResources().getColor(R.color.color_primary_dark)).setCubic(true);
            line.setHasLabels(true);
            List<Line> lines = new ArrayList<Line>(1);
            lines.add(line);

            LineChartData data = new LineChartData(lines);

            data.setAxisYLeft(axisY);
            data.setAxisXBottom(axisX);
            mLineChart.setLineChartData(data);
            mLineChart.setVisibility(View.VISIBLE);
        }
        else mLineChart.setVisibility(View.INVISIBLE);
    }
}

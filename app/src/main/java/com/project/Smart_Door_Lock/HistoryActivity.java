package com.project.Smart_Door_Lock;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class HistoryActivity extends AppCompatActivity {


    // 막대그래프의 가로축

    private String[] mMonth = new String[] {

            "Jan", "Feb" , "Mar", "Apr", "May", "Jun",

            "Jul", "Aug" , "Sep", "Oct", "Nov", "Dec"

    };

    private GraphicalView mChartView;


    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);

        drawChart();

    }


    private void drawChart(){

        int[] x = { 1,2,3,4,5,6,7,8 };

        int[] income = { 10,20,30,40,50,60,70,80};


        // Creating an  XYSeries for visit

        XYSeries visitSeries = new XYSeries("Visitors");


        // Adding data to Visit Series

        for(int i=0;i<x.length;i++){

            visitSeries.add(x[i], income[i]);
        }


        // Creating a dataset to hold each series

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();


        // Adding Visit Series to the dataset

        dataset.addSeries(visitSeries);



        // Creating XYSeriesRenderer to customize incomeSeries

        XYSeriesRenderer visitRenderer = new XYSeriesRenderer();

        visitRenderer.setColor(Color.GREEN);

        visitRenderer.setPointStyle(PointStyle.CIRCLE);

        visitRenderer.setFillPoints(true);

        visitRenderer.setLineWidth(2);

        visitRenderer.setDisplayChartValues(true);



        // Creating a XYMultipleSeriesRenderer to customize the whole chart

        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();

        multiRenderer.setXLabels(0);

        multiRenderer.setChartTitle("Visit Chart");

        multiRenderer.setXTitle("Year 2018");

        multiRenderer.setYTitle("Number of visitors");

        multiRenderer.setZoomButtonsVisible(true);

        for(int i=0;i<x.length;i++){

            multiRenderer.addXTextLabel(i+1, mMonth[i]);

        }

        multiRenderer.setLabelsTextSize(40);
        multiRenderer.setAxisTitleTextSize(30);

        // Adding incomeRenderer and expenseRenderer to multipleRenderer

        // Note: The order of adding dataseries to dataset and renderers to multipleRenderer

        // should be same

        multiRenderer.addSeriesRenderer(visitRenderer);


        // Creating an intent to plot line chart using dataset and multipleRenderer

        // Intent intent = ChartFactory.getLineChartIntent(getBaseContext(), dataset, multiRenderer);



        // Start Activity

        //startActivity(intent);


        if (mChartView == null) {

            LinearLayout layout = (LinearLayout) findViewById(R.id.chart_line);

            mChartView = ChartFactory.getLineChartView(this, dataset, multiRenderer);

            multiRenderer.setClickEnabled(true);

            multiRenderer.setSelectableBuffer(10);

            layout.addView(mChartView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,

                    LinearLayout.LayoutParams.FILL_PARENT));

        } else {

            mChartView.repaint();

        }

    }

}

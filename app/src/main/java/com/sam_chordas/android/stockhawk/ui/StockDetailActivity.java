package com.sam_chordas.android.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.StockGraphIntentService;
import com.sam_chordas.android.stockhawk.service.StockGraphTaskService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ravisravankumar on 08/04/16.
 */
public class StockDetailActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    Intent mServiceIntent;
    boolean isConnected;
    private LineChart mChart;
    String stockSymbol="";
    JSONArray resultQuoteArray;

    String TAG = StockDetailActivity.class.getSimpleName();
    //float[] openData,closeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        stockSymbol = getIntent().getStringExtra(getString(R.string.extra_symbol));
        getSupportActionBar().setTitle(stockSymbol);
        Log.v(TAG, "stock Symbol is " + stockSymbol);

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        mServiceIntent = new Intent(StockDetailActivity.this, StockGraphIntentService.class);
        mServiceIntent.putExtra(getString(R.string.extra_symbol), stockSymbol);

        if (isConnected) {
            startService(mServiceIntent);
        } else {
            Toast.makeText(StockDetailActivity.this, getString(R.string.check_network_connection), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(StockGraphTaskService.GRAPH_RESULT_EVENT));
        super.onResume();
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            try {
                String getResponse = intent.getStringExtra(getString(R.string.extra_getresponse));
                Log.d("receiver", "Got message: " + getResponse);
                JSONObject mainJsonResponse = new JSONObject(getResponse);
                JSONObject queryJsonObject = mainJsonResponse.getJSONObject("query");
                int count = queryJsonObject.getInt("count");
                JSONObject resultsJsonObject = queryJsonObject.getJSONObject("results");
                resultQuoteArray = resultsJsonObject.getJSONArray("quote");
                //prepareGraphData();
                renderGraph();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void renderGraph(){
        mChart = new LineChart(this);
        addContentView(mChart, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mChart.setDrawGridBackground(true);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDescription(stockSymbol);
        mChart.setDrawBorders(true);

        mChart.getAxisLeft().setDrawAxisLine(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getAxisRight().setDrawAxisLine(false);
        mChart.getAxisRight().setDrawGridLines(false);
        mChart.getXAxis().setDrawAxisLine(false);
        mChart.getXAxis().setDrawGridLines(false);
        // enable touch gestures
        mChart.setTouchEnabled(true);
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        Legend l = mChart.getLegend();

        l.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        mChart.setData(getData());
        mChart.setOnChartValueSelectedListener(this);

        mChart.setContentDescription(getString(R.string.a11y_graph, stockSymbol));
        mChart.setHighlightPerDragEnabled(false);
    }

    private LineData getData() {

        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> yValsOpen = new ArrayList<Entry>();
        ArrayList<Entry> yValsClose = new ArrayList<>();
        ArrayList<Entry> yValsHigh = new ArrayList<>();
        ArrayList<Entry> yValsLow = new ArrayList<>();
        try {
            for (int i = 0; i < resultQuoteArray.length(); i++) {
                JSONObject object = resultQuoteArray.getJSONObject(i);
                xVals.add(resultQuoteArray.getJSONObject(i).getString("Date"));
                String open = object.getString("Open");
                String close = object.getString("Close");
                String high = object.getString("High");
                String low = object.getString("Low");
                float openVal = Float.valueOf(open);
                float closeVal = Float.valueOf(close);
                float highVal = Float.valueOf(high);
                float lowVal = Float.valueOf(low);
                yValsOpen.add(new Entry(openVal, i));
                yValsClose.add(new Entry(closeVal,i));
                yValsHigh.add(new Entry(highVal,i));
                yValsLow.add(new Entry(lowVal,i));
                Log.d(TAG, object.toString());
            }
        } catch (JSONException e) {

        }

        // create a dataset and give it a type
        LineDataSet openSet = new LineDataSet(yValsOpen, getString(R.string.lable_open));
        LineDataSet closeSet = new LineDataSet(yValsClose, getString(R.string.lable_close));
        LineDataSet highSet = new LineDataSet(yValsHigh, getString(R.string.lable_high));
        LineDataSet lowSet = new LineDataSet(yValsLow, getString(R.string.lable_low));
        styleDataLine(openSet, Color.GREEN);
        styleDataLine(closeSet, Color.RED);
        styleDataLine(highSet, Color.BLUE);
        styleDataLine(lowSet, Color.BLACK);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(openSet); // add the datasets
        dataSets.add(closeSet);
        dataSets.add(highSet);
        dataSets.add(lowSet);
        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        return data;
    }

    private void styleDataLine(LineDataSet set2, int color) {
        set2.setLineWidth(1.75f);
        set2.setCircleRadius(5f);
        set2.setColor(color);
        set2.setCircleColorHole(color);
        set2.setHighLightColor(color);
        set2.setDrawValues(false);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Toast.makeText(StockDetailActivity.this, ""+e.getVal(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }
}

package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ravisravankumar on 09/04/16.
 */
public class StockGraphTaskService extends GcmTaskService {

    private Context mContext;
    private String TAG = StockGraphTaskService.class.getSimpleName();
    private OkHttpClient client = new OkHttpClient();
    public static final String GRAPH_RESULT_EVENT = "com.sam_chordas.android.stockhawk.service.graphdata";

    public StockGraphTaskService() {
    }

    public StockGraphTaskService(Context context) {
        mContext = context;
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        if(mContext == null){
            mContext = this;
        }
        String stockSymbol = taskParams.getExtras().getString(mContext.getString(R.string.extra_symbol));
        Log.v(TAG,"stock Input "+stockSymbol);
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String endDate = sdf.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            Date result = cal.getTime();
            String startDate = sdf.format(result);
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where " +
                    "symbol = \"" + stockSymbol + "\" and startDate = \""+startDate+"\" and endDate = \""+endDate+"\""
                    , "UTF-8"));
            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&format=json&callback=");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String urlString;
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;
        if(urlStringBuilder!=null){
            urlString = urlStringBuilder.toString();
            try {
                getResponse = fetchData(urlString);
                Intent intent = new Intent(GRAPH_RESULT_EVENT);
                // You can also include some extra data.
                intent.putExtra(mContext.getString(R.string.extra_getresponse), getResponse);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                result = GcmNetworkManager.RESULT_SUCCESS;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}

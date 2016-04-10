package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;

/**
 * Created by ravisravankumar on 09/04/16.
 */
public class StockGraphIntentService extends IntentService {

    public StockGraphIntentService() {
        super(StockGraphIntentService.class.getName());
    }

    public StockGraphIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(StockGraphIntentService.class.getSimpleName(), "Stock Graph Intent Service");
        StockGraphTaskService stockGraphTaskService = new StockGraphTaskService(this);
        Bundle args = new Bundle();
        args.putString(getString(R.string.extra_symbol), intent.getStringExtra(getString(R.string.extra_symbol)));
        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        stockGraphTaskService.onRunTask(new TaskParams(getString(R.string.task_data), args));
    }
}

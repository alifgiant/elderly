package com.buahbatu.elderlywatch.adapter;

import android.graphics.RectF;
import com.robinhood.spark.SparkAdapter;
import java.util.ArrayList;
import java.util.List;

public class ElderlyAdapter extends SparkAdapter{
    private List<Float> dataHolder;

    public ElderlyAdapter() {
        dataHolder = new ArrayList<>();
    }

    public void addDataToChart(float y){
        dataHolder.add(y);
        if (dataHolder.size() > 100){
            dataHolder.remove(0);
        }
    }

    @Override
    public int getCount() {
        return dataHolder.size();
    }

    @Override
    public Object getItem(int index) {
        return null;
    }

    @Override
    public float getY(int index) {
        return dataHolder.get(index);
    }

    @Override
    public RectF getDataBounds() {
        final int count = getCount();

        float minY = 0;
        float maxY = 100;
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            final float x = getX(i);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);

            final float y = getY(i);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        // set values on the return object
        return new RectF(minX, minY, maxX, maxY);
    }
}

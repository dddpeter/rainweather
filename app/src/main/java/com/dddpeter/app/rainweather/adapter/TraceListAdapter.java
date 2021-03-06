package com.dddpeter.app.rainweather.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dddpeter.app.rainweather.pojo.Trace;

import java.util.ArrayList;
import java.util.List;

public class TraceListAdapter extends ArrayAdapter<Trace> {
    private static final int TYPE_TOP = 0x0000;
    private static final int TYPE_NORMAL = 0x0001;
    private int resourceId;
    private int tvAcceptTime;
    private int tvAcceptStation;
    private int tvTopLine;
    private int tvDot;
    private int timellineDotFirst;
    private int timellineDotNormal;
    private SharedPreferences preferences;
    private Context context;
    private List<Trace> traceList = new ArrayList<>(1);

    public TraceListAdapter(Context context, int resource,
                            int tvAcceptTime,
                            int tvAcceptStation,
                            int tvTopLine,
                            int tvDot,
                            int timellineDotFirst,
                            int timellineDotNormal,
                            List<Trace> traceList) {
        super(context, resource, traceList);
        this.resourceId = resource;
        this.context = context;
        this.tvAcceptStation = tvAcceptStation;
        this.tvAcceptTime = tvAcceptTime;
        this.tvTopLine = tvTopLine;
        this.tvDot = tvDot;
        this.timellineDotFirst = timellineDotFirst;
        this.timellineDotNormal = timellineDotNormal;
        this.traceList = traceList;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final Trace trace = getItem(position);
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(resourceId, parent, false);
            holder.tvAcceptTime = (TextView) convertView.findViewById(tvAcceptTime);
            holder.tvAcceptStation = (TextView) convertView.findViewById(tvAcceptStation);
            holder.tvTopLine = (TextView) convertView.findViewById(tvTopLine);
            holder.tvDot = (TextView) convertView.findViewById(tvDot);
            convertView.setTag(holder);
        }

        if (getItemViewType(position) == TYPE_TOP) {
            // ??????????????????????????????
            holder.tvTopLine.setVisibility(View.INVISIBLE);
            // ??????????????????
            holder.tvAcceptTime.setTextColor(0xff336699);
            holder.tvAcceptStation.setTextColor(0xff555555);
            holder.tvDot.setBackgroundResource(timellineDotFirst);
        } else if (getItemViewType(position) == TYPE_NORMAL) {
            holder.tvTopLine.setVisibility(View.VISIBLE);
            holder.tvAcceptTime.setTextColor(0xff999999);
            holder.tvAcceptStation.setTextColor(0xff999999);
            holder.tvDot.setBackgroundResource(timellineDotNormal);
        }

        holder.tvAcceptTime.setText(trace.getAcceptTime());
        holder.tvAcceptStation.setText(trace.getAcceptStation());
        return convertView;
    }


    @Override
    public int getItemViewType(int id) {
        if (id == 0) {
            return TYPE_TOP;
        }
        return TYPE_NORMAL;
    }

    static class ViewHolder {
        public TextView tvAcceptTime, tvAcceptStation;
        public TextView tvTopLine, tvDot;
    }
}
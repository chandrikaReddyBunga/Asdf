package com.gtsr.gtsr;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gtsr.gtsr.database.TestFactors;
import com.gtsr.gtsr.database.UrineResultsDataController;
import com.gtsr.gtsr.database.UrineresultsModel;
import com.gtsr.gtsr.testModule.ResultPageViewController;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ResultRecyclerAdapter extends RecyclerView.Adapter<ResultRecyclerAdapter.ResultHolder> {
    Context context;
    int itemPosition =-1;
    ArrayList<TestFactors> testFactorsArrayList;

    public ResultRecyclerAdapter(Context context, ArrayList<TestFactors> testFactorsArrayList1) {
        this.context = context;
        this.testFactorsArrayList=testFactorsArrayList1;
    }

    @NonNull
    @Override
    public ResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View resultView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_result_item, parent,
                false);
        return new ResultHolder(resultView);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultHolder holder, int position) {
        UrineresultsModel objTestFactors =UrineResultsDataController.getInstance().allUrineResults.get(position);
         holder.txtDate.setText(convertTimestampTodate(objTestFactors.getTestedTime()));
        if (itemPosition==position){
            holder.imgRound.setImageResource(R.drawable.check);
            UrineResultsDataController.getInstance().currenturineresultsModel=UrineResultsDataController.getInstance().allUrineResults.get(itemPosition);
            context.startActivity(new Intent(context, ResultPageViewController.class));
        }else{
            holder.imgRound.setImageResource(R.drawable.ellipse);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(itemPosition != position){
                   itemPosition=position;
                   notifyDataSetChanged();
               }else{
                   itemPosition=-1;
                   notifyDataSetChanged();
               }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (UrineResultsDataController.getInstance().allUrineResults.size() > 0) {
            return UrineResultsDataController.getInstance().allUrineResults.size();
        } else {
            return 0;
        }
    }
    public class ResultHolder extends RecyclerView.ViewHolder {
        ImageView imgRound;
        TextView txtDate;
        public ResultHolder(@NonNull View itemView) {
            super(itemView);
            imgRound = itemView.findViewById(R.id.img_check);
            txtDate=itemView.findViewById(R.id.txtdate);
;
        }
    }
    public String convertTimestampTodate(String stringData) {
        long yourmilliseconds = Long.parseLong(stringData);
        SimpleDateFormat weekFormatter = new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH);
        Date resultdate = new Date(yourmilliseconds * 1000);
        String weekString = weekFormatter.format(resultdate);
        return weekString;
    }
}

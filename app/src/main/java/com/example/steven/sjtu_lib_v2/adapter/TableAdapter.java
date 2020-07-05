package com.example.steven.sjtu_lib_v2.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.activity.SingleDetailActivity;
import com.example.steven.sjtu_lib_v2.activity.SingleDetailActivity.InternalHandler;

import org.jsoup.nodes.Element;

import java.util.List;



/**
 * Created by steven on 2016/2/15.
 * Changed by Keenster on 2020/7/3
 */
public class TableAdapter extends ArrayAdapter<Element> {
    Button borrowBtn;
    Button addToCart;
    TextView location_table;
    TextView subscribing_table;
    TextView single_status_table;
    TextView return_data_table;
    InternalHandler mmHandler = new InternalHandler();

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Element element=getItem(position);
        if (convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.detail_table,null);
        }
        location_table= (TextView) convertView.findViewById(R.id.location_table);
        subscribing_table= (TextView) convertView.findViewById(R.id.subscring_table);
        single_status_table= (TextView) convertView.findViewById(R.id.single_status_table);
        return_data_table= (TextView) convertView.findViewById(R.id.return_date_table);
        borrowBtn = (Button) convertView.findViewById(R.id.borrowbtn);
        addToCart = (Button) convertView.findViewById(R.id.addtocart);

        location_table.setText(element.getElementsByAttributeValue("title", "显示馆藏地详细信息").text());
        subscribing_table.setText(element.getElementsByClass("EXLLocationTableColumn1").text());
        single_status_table.setText(element.getElementsByClass("EXLLocationTableColumn2").text());
        return_data_table.setText(element.getElementsByClass("EXLLocationTableColumn3").text());
        borrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message bookmsg = new Message();
                bookmsg.what = SingleDetailActivity.REPORT_MSG;
                Bundle bundle = new Bundle();
                bundle.putString("location",location_table.getText().toString());
                bundle.putString("bookid",subscribing_table.getText().toString());
                bookmsg.setData(bundle);
                SingleDetailActivity.mHandler.sendMessage(bookmsg);
            }
        });
        if (! return_data_table.getText().toString().equals("在架上")) {
            TableRow shadow1= (TableRow) convertView.findViewById(R.id.shadow1);
            TableRow shadow2= (TableRow) convertView.findViewById(R.id.shadow2);
            TableRow deep1= (TableRow) convertView.findViewById(R.id.deep1);
            TableRow deep2= (TableRow) convertView.findViewById(R.id.deep2);

            shadow1.setBackgroundColor(android.graphics.Color.parseColor("#fdeee7"));
            shadow2.setBackgroundColor(android.graphics.Color.parseColor("#fdeee7"));
            deep1.setBackgroundColor(android.graphics.Color.parseColor("#fdddd0"));
            deep2.setBackgroundColor(android.graphics.Color.parseColor("#fdddd0"));
        }
        return convertView;
    }

    public TableAdapter(Context context, int resource, List<Element> objects) {
        super(context, resource, objects);
    }


}

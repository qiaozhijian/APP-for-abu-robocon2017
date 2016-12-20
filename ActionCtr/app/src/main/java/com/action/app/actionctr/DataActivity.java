package com.action.app.actionctr;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.action.app.actionctr.sqlite.Manage;

import java.util.ArrayList;
import java.util.List;

public class DataActivity extends BasicActivity implements AdapterView.OnItemSelectedListener,View.OnClickListener{

    private Spinner spinner;
    private Manage manage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        (findViewById(R.id.save_data)).setOnClickListener(this);
        (findViewById(R.id.cancel_data)).setOnClickListener(this);

        spinner=(Spinner)findViewById(R.id.option_select);
        List<String> optionSelect=new ArrayList<String>();

        optionSelect.add("column1");
        optionSelect.add("column2");
        optionSelect.add("column3");
        optionSelect.add("column4");
        optionSelect.add("column5");
        optionSelect.add("column6");
        optionSelect.add("column7");

        ArrayAdapter<String> optionSelectAdapter=new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,optionSelect);
        optionSelectAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(optionSelectAdapter);
        spinner.setOnItemSelectedListener(this);
        manage=new Manage(this);

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.save_data:
                break;
            case R.id.cancel_data:
                Intent intent=new Intent(DataActivity.this,BeginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> adapter, View v,int arg1,long arg2){
        ArrayList<Manage.dataSt> data;
        data=manage.selectAll(adapter.getSelectedItem().toString());

        dataAdapter data_adapter=new dataAdapter(DataActivity.this,R.layout.item_listview_activity_data,data);
        ListView listView=(ListView)findViewById(R.id.list_data_display);
        listView.setAdapter(data_adapter);


        Log.d("dataDisplay","display");
    }
    public void onNothingSelected(AdapterView<?> adapter){

    }

    private class dataAdapter extends ArrayAdapter<Manage.dataSt>{

        private int resourceId;

        public dataAdapter(Context context, int textViewResourceId, List<Manage.dataSt> objects) {
            super(context, textViewResourceId, objects);
            resourceId=textViewResourceId;
        }
        @Override
        public View getView(int position, View contenView, ViewGroup parent){
            Manage.dataSt data=getItem(position);
            View view= LayoutInflater.from(getContext()).inflate(resourceId,null);
            TextView[] text=new TextView[7];

            text[0]=(TextView)view.findViewById(R.id.text1_data_item_listview);
            text[1]=(TextView)view.findViewById(R.id.text2_data_item_listview);
            text[2]=(TextView)view.findViewById(R.id.text3_data_item_listview);
            text[3]=(TextView)view.findViewById(R.id.text4_data_item_listview);
            text[4]=(TextView)view.findViewById(R.id.text5_data_item_listview);
            text[5]=(TextView)view.findViewById(R.id.text6_data_item_listview);
            text[6]=(TextView)view.findViewById(R.id.text7_data_item_listview);

            text[0].setText(String.valueOf(data.roll));
            text[1].setText(String.valueOf(data.pitch));
            text[2].setText(String.valueOf(data.yaw));
            text[3].setText(String.valueOf(data.speed1));
            text[4].setText(String.valueOf(data.speed2));
            text[5].setText(data.direction);
            text[6].setText(data.date);

            return view;
        }
    }
}

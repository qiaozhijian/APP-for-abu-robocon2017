package com.action.app.actionctr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.action.app.actionctr.sqlite.Manage;
import  com.action.app.actionctr.excel.Excel;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DataActivity extends BasicActivity implements AdapterView.OnItemSelectedListener,View.OnClickListener{

    private Spinner spinner;
    private Spinner state_spinner;
    private Spinner gun_spinner;
    private Spinner region_spinner;
    private Spinner onTheWay_spinner;
    private Manage manage;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        (findViewById(R.id.save_data)).setOnClickListener(this);
        (findViewById(R.id.cancel_data)).setOnClickListener(this);

        spinner=(Spinner)findViewById(R.id.option_select);//柱子的选择
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
        manage.close();
        super.onDestroy();
    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.save_data: //保存到excel

                AlertDialog.Builder dialog= new AlertDialog.Builder(DataActivity.this);
                dialog.setTitle("Notice");
                dialog.setMessage("Are you sure to save as excel?");
                dialog.setCancelable(false);
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String date=dateFormat.format(new Date(System.currentTimeMillis()));
                        Excel data_excel = new Excel(date+".xls");
                        data_excel.storeExcel(manage,spinner.getSelectedItem().toString(),gun_spinner.getSelectedItem().toString(),state_spinner.getSelectedItem().toString());  //  讲数据库中的内容导出到excel

                    }
                });
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();

                break;
            case R.id.cancel_data:
                Intent intent=new Intent(DataActivity.this,BeginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
    boolean database_ok =false;
    float[] param2set =  new  float[5];
    String[] state = new String[4];
    @Override//实现两个spiner联动，需要嵌套
    public void onItemSelected(AdapterView<?> adapter, View v,int arg1,long arg2){
        switch(adapter.getId())
        {
            case R.id.region_select:
                database_ok = true;
                break;
            case R.id.option_select:
                onTheWay_spinner = (Spinner)findViewById(R.id.option_onTheWay);//位置的选择
                List<String> onTheWaySelect=new ArrayList<String>();
                onTheWaySelect.add("中间");
                onTheWaySelect.add("途中：左");
                onTheWaySelect.add("途中：右");
                ArrayAdapter<String> onTheWaySelectAdapter=new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,onTheWaySelect);
                onTheWaySelectAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                onTheWay_spinner.setAdapter(onTheWaySelectAdapter);
                onTheWay_spinner.setOnItemSelectedListener(this);
                break;
            case R.id.state_select:
                region_spinner = (Spinner)findViewById(R.id.region_select);//区域的选择
                if(spinner.getSelectedItem().toString().equals("column7") && gun_spinner.getSelectedItem().toString().equals("上") && state_spinner.getSelectedItem().toString().equals("打盘")) {
                    List<String> regionoptionSelect=new ArrayList<String>();
                    regionoptionSelect.add("0.0");
                    regionoptionSelect.add("1.0");
                    regionoptionSelect.add("2.0");
                    regionoptionSelect.add("3.0");
                    regionoptionSelect.add("4.0");
                    regionoptionSelect.add("5.0");
                    regionoptionSelect.add("6.0");
                    regionoptionSelect.add("7.0");
                    ArrayAdapter<String> regionoptionSelectAdapter=new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,regionoptionSelect);
                    regionoptionSelectAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    region_spinner.setAdapter(regionoptionSelectAdapter);
                    region_spinner.setOnItemSelectedListener(this);
                }else{
                    database_ok = true;
                }
                break;
            case R.id.gun_select:
                state_spinner=(Spinner)findViewById(R.id.state_select);//枪的选择
                List<String> stateoptionSelect=new ArrayList<String>();

                stateoptionSelect.add("扔");
                stateoptionSelect.add("打球");
                stateoptionSelect.add("打盘");
                ArrayAdapter<String> stateoptionSelectAdapter=new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,stateoptionSelect);
                stateoptionSelectAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                state_spinner.setAdapter(stateoptionSelectAdapter);
                state_spinner.setOnItemSelectedListener(this);

                //gun_spinner.getSelectedItem().toString();
                break;
            case R.id.option_onTheWay:
                gun_spinner=(Spinner)findViewById(R.id.gun_select);//枪的选择
                List<String> gunoptionSelect=new ArrayList<String>();
                gunoptionSelect.add("左");
                gunoptionSelect.add("上");
                gunoptionSelect.add("右");
                ArrayAdapter<String> gunoptionSelectAdapter=new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,gunoptionSelect);
                gunoptionSelectAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                gun_spinner.setAdapter(gunoptionSelectAdapter);
                gun_spinner.setOnItemSelectedListener(this);

                break;
            default:
                break;
        }
       if(database_ok)
       {
           String add="";
           if(onTheWay_spinner!=null){
               if(onTheWay_spinner.getSelectedItem().toString().equals("途中：左"))
               {
                   add="onTheWay_";
               }
               else if(onTheWay_spinner.getSelectedItem().toString().equals("途中：右"))
               {
                   add="onTheWay2_";
               }
           }

           ArrayList<Manage.dataSt> data;
           if(spinner.getSelectedItem().toString().equals("column7") && gun_spinner.getSelectedItem().toString().equals("上") && state_spinner.getSelectedItem().toString().equals("打盘")){
               data=manage.selectColumn(add+spinner.getSelectedItem().toString(),gun_spinner.getSelectedItem().toString(),state_spinner.getSelectedItem().toString(),region_spinner.getSelectedItem().toString());
           }else{
               data=manage.selectAll(add+(spinner.getSelectedItem().toString()),gun_spinner.getSelectedItem().toString(),state_spinner.getSelectedItem().toString());
           }

           dataAdapter data_adapter=new dataAdapter(DataActivity.this,R.layout.item_listview_activity_data,data);
           ListView listView=(ListView)findViewById(R.id.list_data_display);
           listView.setAdapter(data_adapter);
           state[0] = spinner.getSelectedItem().toString();
           state[1] = gun_spinner.getSelectedItem().toString();
           state[2] =state_spinner.getSelectedItem().toString();
           state[3] =onTheWay_spinner.getSelectedItem().toString();
           Log.d("dataDisplay","display");
           listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
               @Override
               public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                   String add="";
                   if(state[3].equals("途中：左"))
                   {
                       add="onTheWay_";
                   }
                   if(state[3].equals("途中：右"))
                   {
                       add="onTheWay2_";
                   }


                   Manage.dataSt data = manage.selectOne(add+state[0],state[1],state[2],position+1);
                   param2set[0] = data.roll;
                   param2set[1] = data.pitch;
                   param2set[2] = data.yaw;
                   param2set[3] = data.speed1;
                   param2set[4] = data.speed2;
                   //我们需要的内容，跳转页面或显示详细信息
                   Log.d("onItemClick","go to next activity");
                   AlertDialog.Builder dialog= new AlertDialog.Builder(DataActivity.this);
                   dialog.setTitle("注意");
                   dialog.setMessage("您是否确认需要将改组参数导出？");
                   dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                           Intent intent;
                           intent=new Intent(DataActivity.this,ParamChangeActivity.class);
                           intent.putExtra("param2set",param2set);
                           intent.putExtra("state2set",state);
                           startActivity(intent);
                           finish();
                       }
                   });
                   dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                       }
                   });
                   dialog.show();

               }
           });
       }
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
            TextView[] text=new TextView[8];

            text[0]=(TextView)view.findViewById(R.id.text1_data_item_listview);
            text[1]=(TextView)view.findViewById(R.id.text2_data_item_listview);
            text[2]=(TextView)view.findViewById(R.id.text3_data_item_listview);
            text[3]=(TextView)view.findViewById(R.id.text4_data_item_listview);
            text[4]=(TextView)view.findViewById(R.id.text5_data_item_listview);
            text[5]=(TextView)view.findViewById(R.id.text6_data_item_listview);
            text[6]=(TextView)view.findViewById(R.id.text7_data_item_listview);
            text[7]=(TextView)view.findViewById(R.id.text8_data_item_listview);

            text[0].setText(String.valueOf(data.roll));
            text[1].setText(String.valueOf(data.pitch));
            text[2].setText(String.valueOf(data.yaw));
            text[3].setText(String.valueOf(data.speed1));
            text[4].setText(String.valueOf(data.speed2));
            text[5].setText(data.direction);
            text[6].setText(data.date);
            text[7].setText(data.note);



            return view;
        }
    }
}

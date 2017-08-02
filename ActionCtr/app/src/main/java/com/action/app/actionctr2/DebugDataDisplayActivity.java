package com.action.app.actionctr2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.action.app.actionctr2.excel.Excel;
import com.action.app.actionctr2.wifi.wifiDataProcess;

import java.util.ArrayList;
import java.util.List;

public class DebugDataDisplayActivity extends BasicActivity implements View.OnClickListener{

    private wifiDataProcess dataProcess;
    private debugDataAdapter adapter;
    private ArrayList<String> debugData;

    private boolean freshFlag=true;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_data_display);

        findViewById(R.id.activity_debugdata_cancel).setOnClickListener(this);
        findViewById(R.id.activity_debug_data_fresh).setOnClickListener(this);
        findViewById(R.id.activity_debug_data_save).setOnClickListener(this);
        findViewById(R.id.activity_debug_data_clear).setOnClickListener(this);

        final ListView listView=(ListView)findViewById(R.id.activity_debugdata_listview);

        dataProcess=new wifiDataProcess(this);

        handler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                debugData.clear();
                debugData.addAll(dataProcess.getWifiStringDataList());
                adapter.notifyDataSetChanged();
                listView.setSelection(debugData.size()-1);
            }
        };
        final Runnable runnableDataUpdate=new Runnable() {
            @Override
            public void run() {
                if(freshFlag){
                    Message message=new Message();
                    message.what=0;
                    handler.sendMessage(message);
                }
                handler.postDelayed(this,300);
            }
        };

        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                if(dataProcess.getBinder()!=null) {
                    debugData=new ArrayList<>();
                    debugData.addAll(dataProcess.getWifiStringDataList());
                    adapter=new debugDataAdapter(DebugDataDisplayActivity.this,R.layout.item_listview_activity_debugdata,debugData);
                    listView.setAdapter(adapter);
                    handler.postDelayed(runnableDataUpdate,200);
                }
                else {
                    handler.postDelayed(this,500);
                }
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onPause(){
        freshFlag=false;
        super.onPause();
    }
    @Override
    public void onStop(){
        freshFlag=false;
        super.onStop();
    }
    @Override
    public void onDestroy(){
        freshFlag=false;
        dataProcess.unbind();
        super.onDestroy();
    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.activity_debugdata_cancel:
                Intent intent=new Intent(DebugDataDisplayActivity.this,BeginActivity.class);
                freshFlag=false;
                startActivity(intent);
                finish();
                break;
            case R.id.activity_debug_data_clear:
                dataProcess.getWifiStringDataList().clear();
                Message message=new Message();
                message.what=0;
                handler.sendMessage(message);
                break;
            case R.id.activity_debug_data_fresh:
                freshFlag=!freshFlag;
                break;
            case R.id.activity_debug_data_save://保存到excel
                if(!freshFlag) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(DebugDataDisplayActivity.this);
                    final EditText commentText=new EditText(DebugDataDisplayActivity.this);
                    dialog.setTitle("注意");
                    dialog.setMessage("确定保存数据到excel?请输入文件名：");
                    dialog.setView(commentText);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                         //   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                         //   String date = dateFormat.format(new Date(System.currentTimeMillis()));
                            String filename = commentText.getText().toString();
                            Excel data_excel = new Excel(filename+ ".xls");
                            data_excel.storeExcel(debugData);  //  将wifi收到的数据保存到excel

                        }
                    });
                    dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.show();
                }else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(DebugDataDisplayActivity.this);
                    dialog.setTitle("警告");
                    dialog.setMessage("请先暂停更新");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.show();
                }
                break;
        }
    }

    public class debugDataAdapter extends ArrayAdapter<String>{
        private int resourceId;

        public debugDataAdapter(Context context, int textViewResourceId, List<String> objects){
            super(context, textViewResourceId, objects);
            resourceId=textViewResourceId;
        }
        @Override
        public View getView(int position, View contenView, ViewGroup parent){
            String text=getItem(position);
            View view= LayoutInflater.from(getContext()).inflate(resourceId,null);
            ((TextView)view.findViewById(R.id.text_debugdata_item_listview)).setText(text);
            return view;
        }
    }


}

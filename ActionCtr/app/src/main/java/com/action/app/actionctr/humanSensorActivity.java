package com.action.app.actionctr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.action.app.actionctr.ble.bleDataProcess;

import java.util.ArrayList;
import java.util.List;

public class humanSensorActivity extends BasicActivity implements View.OnClickListener{


    private ArrayList<Button> buttonsBallList=new ArrayList<>();
    private ArrayList<Button> buttonsFrisbeeList=new ArrayList<>();
    private ArrayList<Button> buttonsColumnList=new ArrayList<>();
    private ArrayList<Button> buttonsDefendList=new ArrayList<>();

    private bleDataProcess bleDataManage;


    private ArrayList<Button> getAllButton(ViewGroup group)
    {
        ArrayList<Button> arrayList=new ArrayList<>();
        for (int i=0;i<group.getChildCount();i++) {
            View v=group.getChildAt(i);
            if(v instanceof Button){
                arrayList.add((Button) v);
            }
            else if(v instanceof ViewGroup)
            {
                arrayList.addAll(getAllButton((ViewGroup) v));
            }
        }
        return  arrayList;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_human_sensor);
        bleDataManage=new bleDataProcess(this);
        for(int i=0;i<7;i++){
            buttonsColumnList.add(null);
            buttonsBallList.add(null);
            buttonsFrisbeeList.add(null);
            if(i<6)
                buttonsDefendList.add(null);
        }

        RelativeLayout layout=(RelativeLayout)findViewById(R.id.activity_human_sensor);
        ArrayList<Button> list=getAllButton(layout);
        for (Button b:list) {
            b.setOnClickListener(this);
            if(String.valueOf(b.getText().subSequence(0,2)).equals("柱子")) {
                int index=Integer.parseInt(String.valueOf(b.getText().subSequence(2,3)))-1;
                buttonsColumnList.set(index,b);
            }
            else if(String.valueOf(b.getText().subSequence(1,2)).equals("球")) {
                int index=Integer.parseInt(String.valueOf(b.getText().subSequence(0,1)))-1;
                buttonsBallList.set(index,b);
            }
            else if(String.valueOf(b.getText().subSequence(1,2)).equals("盘")) {
                int index=Integer.parseInt(String.valueOf(b.getText().subSequence(0,1)))-1;
                buttonsFrisbeeList.set(index,b);
            }
            else if(String.valueOf(b.getText().subSequence(0,2)).equals("防守")) {
                int index=Integer.parseInt(String.valueOf(b.getText().subSequence(2,3)));
                buttonsDefendList.set(index,b);
            }
        }
    }
    @Override
    public void onClick(final View v) {
        for (int i=0;i<buttonsBallList.size();i++) {
            Button b=buttonsBallList.get(i);
            if(b.getText().equals(((Button)v).getText())){
                Log.d("humanSensor","send"+String.valueOf(10+i));
                bleDataManage.sendCmd((byte)(10+i));
            }
        }
        for (int i=0;i<buttonsFrisbeeList.size();i++) {
            Button b=buttonsFrisbeeList.get(i);
            if(b.getText().equals(((Button)v).getText())){
                Log.d("humanSensor","send"+String.valueOf(20+i));
                bleDataManage.sendCmd((byte)(20+i));
            }
        }
        for(int i=0;i<buttonsColumnList.size();i++){
            Button b=buttonsColumnList.get(i);
            if(b.getText().equals(((Button)v).getText())){
                Intent intent;
                intent=new Intent(this,ParamChangeActivity.class);
                intent.putExtra("button_id",i+1);
                startActivity(intent);
                finish();
            }
        }
        for(int i=0;i<buttonsDefendList.size();i++){
            Button b=buttonsDefendList.get(i);
            if(b.getText().equals(((Button)v).getText())){
                Log.d("humanSensor","send"+String.valueOf(40+i));
                bleDataManage.sendCmd((byte)(40+i));
            }
        }
        switch (v.getId()) {
            case R.id.human_sensor_reset:
            case R.id.human_sensor_left_gun_none:
            case R.id.human_sensor_right_gun_none:
                AlertDialog.Builder dialog= new AlertDialog.Builder(humanSensorActivity.this);
                dialog.setTitle("注意");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(v.getId()==R.id.human_sensor_reset)
                            bleDataManage.sendCmd((byte)30);
                        else if(v.getId()==R.id.human_sensor_left_gun_none){
                            Button button=(Button) findViewById(R.id.human_sensor_left_gun_none);
                            byte cmd=50;
                            if(button.getText().equals("左枪有弹"))
                            {
                                button.setText("左枪无弹");
                                cmd++;
                            }
                            else
                                button.setText("左枪有弹");
                            bleDataManage.sendCmd(cmd);
                            Log.d("humanSensor","send "+String.valueOf(cmd));
                        }
                        else if(v.getId()==R.id.human_sensor_right_gun_none){
                            Button button=(Button) findViewById(R.id.human_sensor_right_gun_none);
                            byte cmd=52;
                            if(button.getText().equals("右枪有弹"))
                            {
                                button.setText("右枪无弹");
                                cmd++;
                            }
                            else
                                button.setText("右枪有弹");
                            bleDataManage.sendCmd(cmd);
                            Log.d("humanSensor","send "+String.valueOf(cmd));
                        }
                    }
                });
                dialog.show();
                break;
            case R.id.human_sensor_defend:
                findViewById(R.id.human_sensor_defend_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.human_sensor_normal_layout).setVisibility(View.INVISIBLE);
                break;
            case R.id.human_sensor_defend_cancel:
                findViewById(R.id.human_sensor_defend_layout).setVisibility(View.INVISIBLE);
                findViewById(R.id.human_sensor_normal_layout).setVisibility(View.VISIBLE);
                break;
            case R.id.human_sensor_cancel:
                Intent intent= new Intent(this,BeginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bleDataManage.unbind();
    }

}

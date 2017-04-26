package com.action.app.actionctr;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.action.app.actionctr.ble.bleDataProcess;

import java.util.ArrayList;
import java.util.List;

public class humanSensorActivity extends BasicActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener {


    private ArrayList<Button> buttonsBallList=new ArrayList<>();
    private ArrayList<Button> buttonsFrisbeeList=new ArrayList<>();
    private ArrayList<Button> buttonsColumnList=new ArrayList<>();

    private bleDataProcess bleDataManage;


    private byte state_ball=0;
    private byte state_frisbee=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_human_sensor);

        bleDataManage=new bleDataProcess(this);

        ((ToggleButton)findViewById(R.id.human_sensor_top_gun_mode)).setOnCheckedChangeListener(this);
        


        buttonsBallList.add((Button) findViewById(R.id.human_sensor_ball_is1));
        buttonsFrisbeeList.add((Button)findViewById(R.id.human_sensor_frisbee_is1));

        buttonsBallList.add((Button)findViewById(R.id.human_sensor_ball_is2));
        buttonsFrisbeeList.add((Button)findViewById(R.id.human_sensor_frisbee_is2));

        buttonsBallList.add((Button)findViewById(R.id.human_sensor_ball_is3));
        buttonsFrisbeeList.add((Button)findViewById(R.id.human_sensor_frisbee_is3));

        buttonsBallList.add((Button)findViewById(R.id.human_sensor_ball_is4));
        buttonsFrisbeeList.add((Button)findViewById(R.id.human_sensor_frisbee_is4));

        buttonsBallList.add((Button)findViewById(R.id.human_sensor_ball_is5));
        buttonsFrisbeeList.add((Button)findViewById(R.id.human_sensor_frisbee_is5));

        buttonsBallList.add((Button)findViewById(R.id.human_sensor_ball_is6));
        buttonsFrisbeeList.add((Button)findViewById(R.id.human_sensor_frisbee_is6));

        buttonsBallList.add((Button)findViewById(R.id.human_sensor_ball_is7));
        buttonsFrisbeeList.add((Button)findViewById(R.id.human_sensor_frisbee_is7));

        buttonsColumnList.add((Button)findViewById(R.id.human_sensor_column1));
        buttonsColumnList.add((Button)findViewById(R.id.human_sensor_column2));
        buttonsColumnList.add((Button)findViewById(R.id.human_sensor_column3));
        buttonsColumnList.add((Button)findViewById(R.id.human_sensor_column4));
        buttonsColumnList.add((Button)findViewById(R.id.human_sensor_column5));
        buttonsColumnList.add((Button)findViewById(R.id.human_sensor_column6));
        buttonsColumnList.add((Button)findViewById(R.id.human_sensor_column7));

        for(Button obj:buttonsColumnList){
            obj.setOnClickListener(this);
        }

        for (Button obj:buttonsFrisbeeList) {
            obj.setOnClickListener(this);
        }
        for (Button obj:buttonsBallList) {
            obj.setOnClickListener(this);
        }

        (findViewById(R.id.human_sensor_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(humanSensorActivity.this,BeginActivity.class);
                startActivity(intent);

                SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
                editor.putInt("state_ball",state_ball);
                editor.putInt("state_frisbee",state_frisbee);
                editor.commit();

                finish();
            }
        });
    }
    @Override
    public void onClick(View v) {
        for (int i=0;i<buttonsBallList.size();i++) {
            Button b=buttonsBallList.get(i);
            if(b.getId()==v.getId()){
                //Log.d("humanSensor","send"+String.valueOf(10+i));
                bleDataManage.sendCmd((byte)(10+i));
            }
        }
        for (int i=0;i<buttonsFrisbeeList.size();i++) {
            Button b=buttonsFrisbeeList.get(i);
            if(b.getId()==v.getId()){
                //Log.d("humanSensor","send"+String.valueOf(20+i));
                bleDataManage.sendCmd((byte)(20+i));
            }
        }
        for(int i=0;i<buttonsColumnList.size();i++){
            Button b=buttonsColumnList.get(i);
            if(b.getId()==v.getId()){
                Intent intent;
                intent=new Intent(this,ParamChangeActivity.class);
                intent.putExtra("button_id",i+1);
                startActivity(intent);
                finish();
            }
        }
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
        if(isChecked)
            bleDataManage.sendCmd((byte)30);
        else
            bleDataManage.sendCmd((byte)31);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        bleDataManage.unbind();
    }

}

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

public class humanSensorActivity extends BasicActivity implements CompoundButton.OnCheckedChangeListener {


    private ArrayList<ToggleButton> toggleButtonsBallList=new ArrayList<>();
    private ArrayList<ToggleButton> toggleButtonsFrisbeeList=new ArrayList<>();

    private bleDataProcess bleDataManage;


    private byte state_ball=0;
    private byte state_frisbee=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_human_sensor);

        bleDataManage=new bleDataProcess(this);

        toggleButtonsBallList.add((ToggleButton)findViewById(R.id.human_sensor_ball_is1));
        toggleButtonsFrisbeeList.add((ToggleButton)findViewById(R.id.human_sensor_frisbee_is1));

        toggleButtonsBallList.add((ToggleButton)findViewById(R.id.human_sensor_ball_is2));
        toggleButtonsFrisbeeList.add((ToggleButton)findViewById(R.id.human_sensor_frisbee_is2));

        toggleButtonsBallList.add((ToggleButton)findViewById(R.id.human_sensor_ball_is3));
        toggleButtonsFrisbeeList.add((ToggleButton)findViewById(R.id.human_sensor_frisbee_is3));

        toggleButtonsBallList.add((ToggleButton)findViewById(R.id.human_sensor_ball_is4));
        toggleButtonsFrisbeeList.add((ToggleButton)findViewById(R.id.human_sensor_frisbee_is4));

        toggleButtonsBallList.add((ToggleButton)findViewById(R.id.human_sensor_ball_is5));
        toggleButtonsFrisbeeList.add((ToggleButton)findViewById(R.id.human_sensor_frisbee_is5));

        toggleButtonsBallList.add((ToggleButton)findViewById(R.id.human_sensor_ball_is6));
        toggleButtonsFrisbeeList.add((ToggleButton)findViewById(R.id.human_sensor_frisbee_is6));

        toggleButtonsBallList.add((ToggleButton)findViewById(R.id.human_sensor_ball_is7));
        toggleButtonsFrisbeeList.add((ToggleButton)findViewById(R.id.human_sensor_frisbee_is7));

        SharedPreferences dataSt=getSharedPreferences("data",MODE_PRIVATE);

        state_ball=(byte) dataSt.getInt("state_ball",0);
        state_frisbee=(byte) dataSt.getInt("state_frisbee",0);

        for(int i=0;i<toggleButtonsBallList.size();i++)
        {
            int temp1=1;
            int temp2=2;
            for(int j=0;j<i;j++)
            {
                temp1*=2;
                temp2*=2;
            }
            toggleButtonsBallList.get(i).setChecked((((state_ball%temp2)/temp1)==1));
            toggleButtonsFrisbeeList.get(i).setChecked((((state_frisbee%temp2)/temp1)==1));

            toggleButtonsBallList.get(i).setOnCheckedChangeListener(this);
            toggleButtonsFrisbeeList.get(i).setOnCheckedChangeListener(this);
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        state_ball=0;
        state_frisbee=0;

        ColorDrawable colorDrawable= (ColorDrawable) buttonView.getBackground();
        if(colorDrawable.getColor()==Color.parseColor("#969696")) {
            buttonView.setBackgroundColor(Color.parseColor("#6495ED"));
        }
        else {
            buttonView.setBackgroundColor(Color.parseColor("#969696"));
        }


        for(int i=toggleButtonsBallList.size()-1;i>=0;i--) {
            state_ball=(byte) (state_ball*2);
            state_frisbee=(byte) (state_frisbee*2);
            ToggleButton toggleButton=toggleButtonsBallList.get(i);
            if(toggleButton.isChecked()){
                state_ball+=(byte) 1;
            }

            toggleButton=toggleButtonsFrisbeeList.get(i);
            if(toggleButton.isChecked()){
                state_frisbee+=(byte) 1;
            }
            bleDataManage.sendState(state_ball,state_frisbee);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        bleDataManage.unbind();
    }

}

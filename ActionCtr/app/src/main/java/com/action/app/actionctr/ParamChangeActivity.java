package com.action.app.actionctr;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.action.app.actionctr.ble.bleDataProcess;
import com.action.app.actionctr.sqlite.Manage;

import java.util.ArrayList;

/**
 * Created by 56390 on 2016/12/8.
 */

public class ParamChangeActivity extends BasicActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener {

    private Manage sqlManage;
    private bleDataProcess bleDataManage;

    private EditText editTextRoll;
    private EditText editTextPitch;
    private EditText editTextYaw;
    private EditText editTextSpeed1;
    private EditText editTextSpeed2;


    private SeekBar seekBar_pitch;
    private SeekBar seekBar_roll;
    private SeekBar seekBar_yaw;
    private SeekBar seekBar_speed1;
    private SeekBar seekBar_speed2;

    private ProgressDialog progressDialog;


    private float progressToFloat(SeekBar seekBar,int val){
        switch (seekBar.getId()){
            case R.id.progress_pitch:
                return (val/360.0f*36-6);
            case R.id.progress_roll:
                return (val/450.0f*45-0);
            case R.id.progress_yaw:
                return (val/900.0f*90-45);
            case R.id.progress_speed1:
                return (val/350.0f*350-0);
            case R.id.progress_speed2:
                return (val/350.0f*350-0);
            default:
                Log.e("paramChange","err progressToFloat");
                return 0.0f;
        }
    }
    private int floatToProgress(SeekBar seekBar,float val){
        switch (seekBar.getId()){
            case R.id.progress_pitch:
                if(val<-6)
                    val=-6;
                if(val>30)
                    val=30;
                return (int)((val+6)/36*360);
            case R.id.progress_roll:
                if(val<-0)
                    val=-0;
                if(val>45)
                    val=45;
                return (int)((val+0)/45*450);
            case R.id.progress_yaw:
                if(val<-45)
                    val=-45;
                if(val>45)
                    val=45;
                return (int)((val+45)/90*900);
            case R.id.progress_speed1:
                if(val<-0)
                    val=0;
                if(val>350)
                    val=350;
                return (int)((val+0)/350*350);
            case R.id.progress_speed2:
                if(val<-0)
                    val=0;
                if(val>350)
                    val=350;
                return (int)((val+0)/350*350);
            default:
                Log.e("paramChange","err floatToProgress");
                return 0;
        }
    }

    private int buttonId;
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_param_change);
        sqlManage=new Manage(this);
        bleDataManage=new bleDataProcess(this);

        findViewById(R.id.button_param_cancel).setOnClickListener(this);
        findViewById(R.id.button_param_save).setOnClickListener(this);
        findViewById(R.id.button_param_change).setOnClickListener(this);

        findViewById(R.id.roll_decrease).setOnClickListener(this);
        findViewById(R.id.roll_increase).setOnClickListener(this);
        findViewById(R.id.pitch_decrease).setOnClickListener(this);
        findViewById(R.id.pitch_increase).setOnClickListener(this);
        findViewById(R.id.yaw_decrease).setOnClickListener(this);
        findViewById(R.id.yaw_increase).setOnClickListener(this);
        findViewById(R.id.speed1_decrease).setOnClickListener(this);
        findViewById(R.id.speed1_increase).setOnClickListener(this);
        findViewById(R.id.speed2_decrease).setOnClickListener(this);
        findViewById(R.id.speed2_increase).setOnClickListener(this);

        seekBar_pitch=((SeekBar)findViewById(R.id.progress_pitch));
        seekBar_roll=((SeekBar)findViewById(R.id.progress_roll));
        seekBar_yaw=((SeekBar)findViewById(R.id.progress_yaw));
        seekBar_speed1=((SeekBar)findViewById(R.id.progress_speed1));
        seekBar_speed2=((SeekBar)findViewById(R.id.progress_speed2));

        seekBar_pitch.setMax(360);
        seekBar_roll.setMax(450);
        seekBar_yaw.setMax(900);
        seekBar_speed1.setMax(350);
        seekBar_speed2.setMax(350);


        seekBar_pitch.setOnSeekBarChangeListener(this);
        seekBar_roll.setOnSeekBarChangeListener(this);
        seekBar_yaw.setOnSeekBarChangeListener(this);
        seekBar_speed1.setOnSeekBarChangeListener(this);
        seekBar_speed2.setOnSeekBarChangeListener(this);

        Intent intent=getIntent();
        buttonId=intent.getIntExtra("button_id",0);

        Log.d("paraChange","buttonId: "+String.valueOf(buttonId));
        ((TextView)findViewById(R.id.column_num)).setText("column: "+String.valueOf(buttonId));

        editTextRoll=(EditText)findViewById(R.id.edit_roll);
        editTextPitch=(EditText)findViewById(R.id.edit_pitch);
        editTextYaw=(EditText)findViewById(R.id.edit_yaw);
        editTextSpeed1=(EditText)findViewById(R.id.edit_speed1);
        editTextSpeed2=(EditText)findViewById(R.id.edit_speed2);

        if(!sqlManage.Select(buttonId)){
            sqlManage.roll=0.0f;
            sqlManage.pitch=0.0f;
            sqlManage.yaw=0.0f;
            sqlManage.speed1=0;
            sqlManage.speed2=0;
        }
        (editTextRoll).setText(String.valueOf(sqlManage.roll));
        (editTextPitch).setText(String.valueOf(sqlManage.pitch));
        (editTextYaw).setText(String.valueOf(sqlManage.yaw));
        (editTextSpeed1).setText(String.valueOf(sqlManage.speed1));
        (editTextSpeed2).setText(String.valueOf(sqlManage.speed2));

        seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,sqlManage.pitch));
        seekBar_roll.setProgress(floatToProgress(seekBar_roll,sqlManage.roll));
        seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,sqlManage.yaw));
        seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,sqlManage.speed1));
        seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,sqlManage.speed2));

        sqlManage.ward=intent.getStringExtra("gesture_ward");
        Log.d("paraChange","ward: "+sqlManage.ward);
        ((TextView)findViewById(R.id.column_ward)).setText("ward: "+sqlManage.ward);
    }
    @Override
    public void onClick(View v)
    {
        float valueF;
        int   valueI;
        EditText editText=null;
        SeekBar  seekBar=null;
        switch (v.getId())
        {
            case R.id.button_param_save:
                AlertDialog.Builder dialog= new AlertDialog.Builder(ParamChangeActivity.this);
                dialog.setTitle("Notice");
                dialog.setMessage("Are you sure to change Param?");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        sqlManage.roll=Float.parseFloat(editTextRoll.getText().toString());
                        sqlManage.pitch=Float.parseFloat(editTextPitch.getText().toString());
                        sqlManage.yaw=Float.parseFloat(editTextYaw.getText().toString());

                        sqlManage.speed1=Integer.parseInt(editTextSpeed1.getText().toString());
                        sqlManage.speed2=Integer.parseInt(editTextSpeed2.getText().toString());

                        sqlManage.Insert(buttonId);
                        Toast.makeText(ParamChangeActivity.this, "save ok", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.setCancelable(false);
                dialog.show();

                break;
            case R.id.button_param_change:
                sqlManage.roll=Float.parseFloat(editTextRoll.getText().toString());
                sqlManage.pitch=Float.parseFloat(editTextPitch.getText().toString());
                sqlManage.yaw=Float.parseFloat(editTextYaw.getText().toString());

                sqlManage.speed1=Integer.parseInt(editTextSpeed1.getText().toString());
                sqlManage.speed2=Integer.parseInt(editTextSpeed2.getText().toString());

                Log.d("data change","roll: "+String.valueOf(sqlManage.roll));
                Log.d("data change","pitch: "+String.valueOf(sqlManage.pitch));
                Log.d("data change","yaw: "+String.valueOf(sqlManage.yaw));
                Log.d("data change","speed1: "+String.valueOf(sqlManage.speed1));
                Log.d("data change","speed2: "+String.valueOf(sqlManage.speed2));

                progressDialog=new ProgressDialog(ParamChangeActivity.this);
                progressDialog.setTitle("data sending,please wait......");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                final Handler handler=new Handler();
                Runnable runnable=new Runnable() {
                    private byte id=0;
                    @Override
                    public void run() {
                        if(bleDataManage.checkSendOk()){
                            switch (id) {
                                case 0:
                                    bleDataManage.sendParam((byte) (id+buttonId*5-5),sqlManage.roll);
                                    break;
                                case 1:
                                    bleDataManage.sendParam((byte) (id+buttonId*5-5),sqlManage.pitch);
                                    break;
                                case 2:
                                    bleDataManage.sendParam((byte) (id+buttonId*5-5),sqlManage.yaw);
                                    break;
                                case 3:
                                    bleDataManage.sendParam((byte) (id+buttonId*5-5),sqlManage.speed1);
                                    break;
                                case 4:
                                    bleDataManage.sendParam((byte) (id+buttonId*5-5),sqlManage.speed2);
                                    break;
                                case 5:
                                    progressDialog.cancel();
                                    break;
                                default:
                                    Log.e("change button","onclick run err run err!!!!!");
                                    break;
                            }
                            if(id!=5){
                                handler.postDelayed(this,50);
                            }
                            id++;
                        }
                        else {
                            handler.postDelayed(this,50);
                        }
                    }
                };
                handler.postDelayed(runnable,50);
                break;
            case R.id.button_param_cancel:
                Intent intent=new Intent(this,BeginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.pitch_increase:
                editText=(EditText)findViewById(R.id.edit_pitch);
                seekBar=(SeekBar)findViewById(R.id.progress_pitch);
                break;
            case R.id.roll_increase:
                editText=(EditText)findViewById(R.id.edit_roll);
                seekBar=(SeekBar)findViewById(R.id.progress_roll);
                break;
            case R.id.yaw_increase:
                editText=(EditText)findViewById(R.id.edit_yaw);
                seekBar=(SeekBar)findViewById(R.id.progress_yaw);
                break;
            case R.id.pitch_decrease:
                editText=(EditText)findViewById(R.id.edit_pitch);
                seekBar=(SeekBar)findViewById(R.id.progress_pitch);
                break;
            case R.id.roll_decrease:
                editText=(EditText)findViewById(R.id.edit_roll);
                seekBar=(SeekBar)findViewById(R.id.progress_roll);
                break;
            case R.id.yaw_decrease:
                editText=(EditText)findViewById(R.id.edit_yaw);
                seekBar=(SeekBar)findViewById(R.id.progress_yaw);
                break;
            case R.id.speed1_increase:
                editText=(EditText)findViewById(R.id.edit_speed1);
                seekBar=(SeekBar)findViewById(R.id.progress_speed1);
                break;
            case R.id.speed2_increase:
                editText=(EditText)findViewById(R.id.edit_speed2);
                seekBar=(SeekBar)findViewById(R.id.progress_speed2);
                break;
            case R.id.speed1_decrease:
                editText=(EditText)findViewById(R.id.edit_speed1);
                seekBar=(SeekBar)findViewById(R.id.progress_speed1);
                break;
            case R.id.speed2_decrease:
                editText=(EditText)findViewById(R.id.edit_speed2);
                seekBar=(SeekBar)findViewById(R.id.progress_speed2);
                break;
            default:
                Log.e("button","no case for button click");
                finish();
                break;
        }
        if(editText!=null&&seekBar!=null){
            switch (v.getId())
            {
                case R.id.pitch_increase:
                case R.id.roll_increase:
                case R.id.yaw_increase:
                    valueF=Float.parseFloat(editText.getText().toString());
                    valueF+=0.5f;
                    seekBar.setProgress(floatToProgress(seekBar,valueF));
                    break;
                case R.id.pitch_decrease:
                case R.id.roll_decrease:
                case R.id.yaw_decrease:
                    valueF=Float.parseFloat(editText.getText().toString());
                    valueF-=0.5f;
                    seekBar.setProgress(floatToProgress(seekBar,valueF));
                    break;
                case R.id.speed1_increase:
                case R.id.speed2_increase:
                    valueI=Integer.parseInt(editText.getText().toString());
                    valueI+=1;
                    seekBar.setProgress(floatToProgress(seekBar,valueI));
                    break;
                case R.id.speed1_decrease:
                case R.id.speed2_decrease:
                    valueI=Integer.parseInt(editText.getText().toString());
                    valueI-=1;
                    seekBar.setProgress(floatToProgress(seekBar,valueI));
                    break;
                default:
                    Log.e("button","no case for button click");
                    finish();
                    break;
            }
        }

    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.progress_pitch:
                editTextPitch.setText(String.valueOf(progressToFloat(seekBar,progress)));
                break;
            case R.id.progress_roll:
                editTextRoll.setText(String.valueOf(progressToFloat(seekBar,progress)));
                break;
            case R.id.progress_yaw:
                editTextYaw.setText(String.valueOf(progressToFloat(seekBar,progress)));
                break;
            case R.id.progress_speed1:
                editTextSpeed1.setText(String.valueOf((int)progressToFloat(seekBar,progress)));
                break;
            case R.id.progress_speed2:
                editTextSpeed2.setText(String.valueOf((int)progressToFloat(seekBar,progress)));
                break;
        }
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        sqlManage.close();
        bleDataManage.unbind();
    }
}

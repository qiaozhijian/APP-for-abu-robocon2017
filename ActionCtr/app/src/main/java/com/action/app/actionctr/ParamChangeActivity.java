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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.action.app.actionctr.ble.bleDataProcess;
import com.action.app.actionctr.sqlite.Manage;

/**
 * Created by 56390 on 2016/12/8.
 */

public class ParamChangeActivity extends BasicActivity implements View.OnClickListener {

    private Manage sqlManage;
    private bleDataProcess bleDataManage;

    private EditText editTextRoll;
    private EditText editTextPitch;
    private EditText editTextYaw;
    private EditText editTextSpeed1;
    private EditText editTextSpeed2;

    private ProgressDialog progressDialog;

    private int buttonId;
    private String buttonWard;
    @Override
    protected void onCreate(Bundle s)
    {
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

        Intent intent=getIntent();
        buttonId=intent.getIntExtra("button_id",0);
        sqlManage.ward=intent.getStringExtra("gesture_ward");
        ((TextView)findViewById(R.id.column_num)).setText("column: "+String.valueOf(buttonId));
        ((TextView)findViewById(R.id.column_ward)).setText("ward: "+sqlManage.ward);

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
    }
    @Override
    public void onClick(View v)
    {
        float valueF;
        int   valueI;
        EditText editText=null;
        switch (v.getId())
        {
            case R.id.button_param_save:
                AlertDialog.Builder dialog= new AlertDialog.Builder(ParamChangeActivity.this);
                dialog.setTitle("Notice");
                dialog.setMessage("Are you sure to change Param?");
                dialog.setCancelable(false);
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
                progressDialog.setCancelable(true);
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
                                    bleDataManage.sendParam(id,sqlManage.roll);
                                    break;
                                case 1:
                                    bleDataManage.sendParam(id,sqlManage.pitch);
                                    break;
                                case 2:
                                    bleDataManage.sendParam(id,sqlManage.yaw);
                                    break;
                                case 3:
                                    bleDataManage.sendParam(id,sqlManage.speed1);
                                    break;
                                case 4:
                                    bleDataManage.sendParam(id,sqlManage.speed2);
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
                break;
            case R.id.roll_increase:
                editText=(EditText)findViewById(R.id.edit_roll);
                break;
            case R.id.yaw_increase:
                editText=(EditText)findViewById(R.id.edit_yaw);
                break;
            case R.id.pitch_decrease:
                editText=(EditText)findViewById(R.id.edit_pitch);
                break;
            case R.id.roll_decrease:
                editText=(EditText)findViewById(R.id.edit_roll);
                break;
            case R.id.yaw_decrease:
                editText=(EditText)findViewById(R.id.edit_yaw);
                break;
            case R.id.speed1_increase:
                editText=(EditText)findViewById(R.id.edit_speed1);
                break;
            case R.id.speed2_increase:
                editText=(EditText)findViewById(R.id.edit_speed2);
                break;
            case R.id.speed1_decrease:
                editText=(EditText)findViewById(R.id.edit_speed1);
                break;
            case R.id.speed2_decrease:
                editText=(EditText)findViewById(R.id.edit_speed2);
                break;
            default:
                Log.e("button","no case for button click");
                finish();
                break;
        }
        if(editText!=null){
            switch (v.getId())
            {
                case R.id.pitch_increase:
                case R.id.roll_increase:
                case R.id.yaw_increase:
                    valueF=Float.parseFloat(editText.getText().toString());
                    valueF+=0.5f;
                    editText.setText(String.valueOf(valueF));
                    break;
                case R.id.pitch_decrease:
                case R.id.roll_decrease:
                case R.id.yaw_decrease:
                    valueF=Float.parseFloat(editText.getText().toString());
                    valueF-=0.5f;
                    editText.setText(String.valueOf(valueF));
                    break;
                case R.id.speed1_increase:
                case R.id.speed2_increase:
                    valueI=Integer.parseInt(editText.getText().toString());
                    valueI+=1;
                    editText.setText(String.valueOf(valueI));
                    break;
                case R.id.speed1_decrease:
                case R.id.speed2_decrease:
                    valueI=Integer.parseInt(editText.getText().toString());
                    valueI-=1;
                    editText.setText(String.valueOf(valueI));
                    break;
                default:
                    Log.e("button","no case for button click");
                    finish();
                    break;
            }
        }

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        bleDataManage.unbind();
    }
}

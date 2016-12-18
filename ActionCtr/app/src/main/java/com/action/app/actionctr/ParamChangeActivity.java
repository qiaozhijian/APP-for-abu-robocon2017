package com.action.app.actionctr;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.action.app.actionctr.sqlite.Manage;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.common.StringUtil;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;

/**
 * Created by 56390 on 2016/12/8.
 */

public class ParamChangeActivity extends BasicActivity implements View.OnClickListener {

    private Manage sqlManage;

    private EditText editTextRoll;
    private EditText editTextPitch;
    private EditText editTextYaw;
    private EditText editTextSpeed1;
    private EditText editTextSpeed2;

    private int buttonId;
    private String buttonWard;
    @Override
    protected void onCreate(Bundle s)
    {
        super.onCreate(s);
        setContentView(R.layout.activity_param_change);

        sqlManage=new Manage(this);


        findViewById(R.id.button_param_cancel).setOnClickListener(this);
        findViewById(R.id.button_param_save).setOnClickListener(this);
        findViewById(R.id.button_param_change).setOnClickListener(this);

        Intent intent=getIntent();
        buttonId=intent.getIntExtra("button_id",0);
        buttonWard=intent.getStringExtra("gesture_ward");
        ((TextView)findViewById(R.id.column_num)).setText("column: "+String.valueOf(buttonId));
        ((TextView)findViewById(R.id.column_ward)).setText("ward: "+buttonWard);

        editTextRoll=(EditText)findViewById(R.id.edit_roll);
        editTextPitch=(EditText)findViewById(R.id.edit_pitch);
        editTextYaw=(EditText)findViewById(R.id.edit_yaw);
        editTextSpeed1=(EditText)findViewById(R.id.edit_speed1);
        editTextSpeed2=(EditText)findViewById(R.id.edit_speed2);

        if(sqlManage.Select(buttonId)){
            (editTextRoll).setText(String.valueOf(sqlManage.roll));
            (editTextPitch).setText(String.valueOf(sqlManage.pitch));
            (editTextYaw).setText(String.valueOf(sqlManage.yaw));
            (editTextSpeed1).setText(String.valueOf(sqlManage.speed1));
            (editTextSpeed2).setText(String.valueOf(sqlManage.speed2));
        }
        else{
            editTextRoll.setText(String.valueOf(0.0f));
            editTextPitch.setText(String.valueOf(0.0f));
            editTextYaw.setText(String.valueOf(0.0f));
            editTextSpeed1.setText(String.valueOf(0));
            editTextSpeed2.setText(String.valueOf(0));
        }
    }
    @Override
    public void onClick(View v)
    {
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
                break;
            case R.id.button_param_cancel:
                Intent intent=new Intent(this,BeginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}

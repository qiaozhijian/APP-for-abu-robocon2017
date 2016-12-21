package com.action.app.actionctr;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.action.app.actionctr.sqlite.Manage;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.common.StringUtil;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.*;
import java.io.File;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import jxl.write.WriteException;
import jxl.write.biff.*;

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
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getExcelDir() {
        // SD卡指定文件夹
      //  String sdcardPath = Environment.getExternalStorageDirectory().toString();

        String sdcardPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        File dir = new File(sdcardPath/* + File.separator + "textexcel"*/);

        if (dir.exists()) {
            return dir.toString();

        } else {
            dir.mkdirs();
            Log.e("BAG", "保存路径不存在,");
            return dir.toString();
        }
    }


    private void craetExcel() {
        try {
            // 打开文件
            WritableWorkbook book = Workbook.createWorkbook(new File((/*"/testexcel/test.xls"*/getExcelDir() + File.separator + "1.xls")));
            // 生成名为“第一张工作表”的工作表，参数0表示这是第一页
            WritableSheet sheet = book.createSheet("第一张工作表", 0);
            // 在Label对象的构造子中指名单元格位置是第一列第一行(0,0)
            // 以及单元格内容为baby
            Label label = new Label(0, 0, "baby");
            // 将定义好的单元格添加到工作表中

            sheet.addCell(label);
            // 生成一个保存数字的单元格，必须使用Number的完整包路径，否则有语法歧义。
            //单元格位置是第二列，第一行，值为123
            jxl.write.Number number = new jxl.write.Number(1, 0, 123);
            sheet.addCell(number);
            //写入数据并关闭
            book.write();
            book.close();

        } catch (WriteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle s)
    {
        super.onCreate(s);
        setContentView(R.layout.activity_param_change);
        sqlManage=new Manage(this,2);

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
        float valueF;
        int   valueI;

        String path ;
        EditText editText=null;
        switch (v.getId())
        {
            case R.id.button_param_save:
           //     isExternalStorageWritable();
          //      isExternalStorageReadable();
         //       path = getExcelDir();
                craetExcel();////////////////////////////////////////////////////////////////
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
}

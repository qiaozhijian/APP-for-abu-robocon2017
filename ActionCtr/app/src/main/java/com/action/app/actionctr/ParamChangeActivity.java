package com.action.app.actionctr;

import android.app.Activity;
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
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.action.app.actionctr.ble.bleDataProcess;
import com.action.app.actionctr.sqlite.Manage;
import com.action.app.actionctr.MenuRightFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by 56390 on 2016/12/8.
 */

public class ParamChangeActivity extends BasicActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener,View.OnTouchListener {

    private final int districtNum=7;

    private int maxYaw=50;
    private int minYaw=-50;
    private float stepYaw=0.5f;

    private int maxRoll=45;
    private int minRoll=0;
    private float stepRoll=0.5f;
    private int   gain_roll=10;


    private int maxPitch=40;
    private int minPitch=-10;
    private float stepPitch=0.5f;


    private int maxSpeed=350;
    private int minSpeed=0;
    private float stepSpeed=1.0f;





    private DrawerLayout mDrawerLayout;

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
            case R.id.progress_yaw:
                return (val+minYaw*10)/10.0f;
            case R.id.progress_pitch:
                return (val+minPitch*10)/10.0f;
            case R.id.progress_roll:
                return (val+minRoll*gain_roll)/((float)gain_roll);
            case R.id.progress_speed1:
            case R.id.progress_speed2:
                return (val+minSpeed);
            default:
                Log.e("paramChange","err progressToFloat");
                return 0.0f;
        }
    }
    private int floatToProgress(SeekBar seekBar,float val){
        switch (seekBar.getId()){
            case R.id.progress_yaw:
                if(val<minYaw)
                    val=minYaw;
                if(val>maxYaw)
                    val=maxYaw;
                return Math.round((val-minYaw)*10);
            case R.id.progress_pitch:
                if(val<minPitch)
                    val=minPitch;
                if(val>maxPitch)
                    val=maxPitch;
                return Math.round((val-minPitch)*10);
            case R.id.progress_roll:
                if(val<minRoll)
                    val=minRoll;
                if(val>maxRoll)
                    val=maxRoll;
                return Math.round((val-minRoll)*gain_roll);
            case R.id.progress_speed1:
            case R.id.progress_speed2:
                if(val<minSpeed)
                    val=minSpeed;
                if(val>maxSpeed)
                    val=maxSpeed;
                return Math.round((val-minSpeed));
            default:
                Log.e("paramChange","err floatToProgress");
                return 0;
        }
    }
    private void setRange(int maxX,int maxY,int maxZ,int maxS,
                          int minX,int minY,int minZ,int minS,
                          float stepX,float stepY,float stepZ,float stepS){
        maxYaw=maxZ;
        minYaw=minZ;
        stepYaw=stepZ;

        maxRoll=maxY;
        minRoll=minY;
        stepRoll=stepY;

        maxPitch=maxX;
        minPitch=minX;
        stepPitch=stepX;

        maxSpeed=maxS;
        minSpeed=minS;
        stepSpeed=stepS;

        seekBar_yaw.setMax((maxYaw-minYaw)*10);
        seekBar_pitch.setMax((maxPitch-minPitch)*10);
        seekBar_roll.setMax((maxRoll-minRoll)*gain_roll);
        seekBar_speed1.setMax((maxSpeed-minSpeed));
        seekBar_speed2.setMax((maxSpeed-minSpeed));
    }
    private int buttonId;
    float[] param2set =new float[5];
    String[] init_state = new String[3];
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_param_change);
        sqlManage=new Manage(this);
        bleDataManage=new bleDataProcess(this);


        findViewById(R.id.button_param_shot).setOnClickListener(this);//射
        findViewById(R.id.button_param_shot).setOnTouchListener(this);

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

        findViewById(R.id.gun_left).setOnClickListener(this);
        findViewById(R.id.gun_right).setOnClickListener(this);
        findViewById(R.id.gun_up).setOnClickListener(this);
        findViewById(R.id.button_param_shotball).setOnClickListener(this);
        findViewById(R.id.button_param_shotfrisbee).setOnClickListener(this);
        findViewById(R.id.button_param_fly).setOnClickListener(this);


        seekBar_pitch=((SeekBar)findViewById(R.id.progress_pitch));
        seekBar_roll=((SeekBar)findViewById(R.id.progress_roll));
        seekBar_yaw=((SeekBar)findViewById(R.id.progress_yaw));
        seekBar_speed1=((SeekBar)findViewById(R.id.progress_speed1));
        seekBar_speed2=((SeekBar)findViewById(R.id.progress_speed2));

        setRange( maxPitch, maxRoll, maxYaw, maxSpeed,
                  minPitch, minRoll, minYaw, minSpeed,
                  stepPitch, stepRoll, stepYaw, stepSpeed);

        seekBar_pitch.setOnSeekBarChangeListener(this);
        seekBar_roll.setOnSeekBarChangeListener(this);
        seekBar_yaw.setOnSeekBarChangeListener(this);
        seekBar_speed1.setOnSeekBarChangeListener(this);
        seekBar_speed2.setOnSeekBarChangeListener(this);
        //接收其他活动消息
        Intent intent=getIntent();
        buttonId=intent.getIntExtra("button_id",0);
        if(buttonId!=0)
        {
            Log.d("paraChange","buttonId: "+String.valueOf(buttonId));
            ((TextView)findViewById(R.id.column_num)).setText("column: "+String.valueOf(buttonId));
            if(!sqlManage.Select(buttonId,"左","扔")){
                sqlManage.roll=0.0f;
                sqlManage.pitch=0.0f;
                sqlManage.yaw=0.0f;
                sqlManage.speed1=0;
                sqlManage.speed2=0;
            }
        }


        editTextRoll=(EditText)findViewById(R.id.edit_roll);
        editTextPitch=(EditText)findViewById(R.id.edit_pitch);
        editTextYaw=(EditText)findViewById(R.id.edit_yaw);
        editTextSpeed1=(EditText)findViewById(R.id.edit_speed1);
        editTextSpeed2=(EditText)findViewById(R.id.edit_speed2);


        (editTextRoll).setText(String.valueOf(sqlManage.roll));
        (editTextPitch).setText(String.valueOf(sqlManage.pitch));
        (editTextYaw).setText(String.valueOf(sqlManage.yaw));
        (editTextSpeed1).setText(String.valueOf(sqlManage.speed1));
        (editTextSpeed2).setText(String.valueOf(sqlManage.speed2));

        param2set = intent.getFloatArrayExtra("param2set");
        init_state = intent.getStringArrayExtra("state2set");
        if(param2set!=null)
        {

            seekBar_roll.setProgress(floatToProgress(seekBar_roll,param2set[0]));
            seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,param2set[1]));
            seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,param2set[2]));
            seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,param2set[3]));
            seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,param2set[4]));
        }
        else
        {
            seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,sqlManage.pitch));
            seekBar_roll.setProgress(floatToProgress(seekBar_roll,sqlManage.roll));
            seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,sqlManage.yaw));
            seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,sqlManage.speed1));
            seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,sqlManage.speed2));
        }
        if(init_state!= null)
        {
            int id = 0;
            ((TextView)findViewById(R.id.column_num)).setText(init_state[0]);
            ((TextView)findViewById(R.id.gun_num)).setText(init_state[1]);
            ((TextView)findViewById(R.id.state)).setText(init_state[2]);
            switch(init_state[0])
            {
                case "column7":id++;
                case "column6":id++;
                case "column5":id++;
                case "column4":id++;
                case "column3":id++;
                case "column2":id++;
                case "column1":id++;
                    buttonId = id;
                    break;
            }

        }


        setDrawerLeftEdgeSize(this, mDrawerLayout, 1.0f);

        initView();
        initEvents();
    }
     /**
     2  * 抽屉滑动范围控制
     3  * @param activity
     4  * @param drawerLayout
     5  * @param displayWidthPercentage 占全屏的份额0~1
     6  */
            private void setDrawerLeftEdgeSize(Activity activity, DrawerLayout drawerLayout, float displayWidthPercentage) {
            if (activity == null || drawerLayout == null)
                   return;
            try {
                   // find ViewDragHelper and set it accessible
                    Field leftDraggerField = drawerLayout.getClass().getDeclaredField("mLeftDragger");
                     leftDraggerField.setAccessible(true);
                     ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);
                    // find edgesize and set is accessible
                     Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
                  edgeSizeField.setAccessible(true);
                 int edgeSize = edgeSizeField.getInt(leftDragger);
                   // set new edgesize
                   // Point displaySize = new Point();
                    DisplayMetrics dm = new DisplayMetrics();
                   activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
                     edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) (dm.widthPixels * displayWidthPercentage)));
                } catch (NoSuchFieldException e) {
                    Log.e("NoSuchFieldException", e.getMessage().toString());
                } catch (IllegalArgumentException e) {
                Log.e("IllegalArgument", e.getMessage().toString());

                } catch (IllegalAccessException e) {
                  Log.e("IllegalAccessException", e.getMessage().toString());
             }
      }
   //右滑菜单监听
    private void initView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_param_change);
        //    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
    }

    private void initEvents() {
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                Log.d("MyGesture","SSSlide");
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                Log.d("MyGesture","OOOpen");



            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(v.getId())
        {
            case R.id.button_param_shot:
                if(event.getAction() == MotionEvent.ACTION_UP){  //发射键按键松开事件
                    //bleDataManage.sendCmd(2);
                }
                break;
            default:
                break;
        }
        return false;
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
            case R.id.button_param_shot:
                byte id=0;
                switch(String.valueOf(((TextView)findViewById(R.id.gun_num)).getText())){
                    case "左":
                        id = 1;
                        break;
                    case "右":
                        id = 2;
                        break;
                    case "上":
                        id = 3;
                        break;
                }

                bleDataManage.sendCmd(id);
                break;
            case R.id.button_param_save:
                AlertDialog.Builder dialog= new AlertDialog.Builder(ParamChangeActivity.this);
                final EditText commentText=new EditText(ParamChangeActivity.this);
                dialog.setTitle("注意");
                dialog.setMessage("您是否确认需要将改组参数保存到数据库?下面请输入注释");
                dialog.setView(commentText);
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        sqlManage.roll=Float.parseFloat(editTextRoll.getText().toString());
                        sqlManage.pitch=Float.parseFloat(editTextPitch.getText().toString());
                        sqlManage.yaw=Float.parseFloat(editTextYaw.getText().toString());
                        sqlManage.speed1=Integer.parseInt(editTextSpeed1.getText().toString());
                        sqlManage.speed2=Integer.parseInt(editTextSpeed2.getText().toString());

                        sqlManage.comment=commentText.getText().toString();
                        sqlManage.Insert(buttonId, String.valueOf(((TextView)findViewById(R.id.gun_num)).getText()),String.valueOf(((TextView)findViewById(R.id.state)).getText()));
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
                    private byte id2=0;
                    @Override
                    public void run() {
                        switch(String.valueOf(((TextView)findViewById(R.id.state)).getText())){
                            case "打球":
                                id2=0;
                                break;
                            case "打盘":
                                id2=1;
                                break;
                            case "扔":
                                id2=2;
                                break;
                        }
                        id2=(byte)(id2*3);
                        switch(String.valueOf(((TextView)findViewById(R.id.gun_num)).getText())){
                            case "左":
                                id2+=0;
                                break;
                            case "右":
                                id2+=1;
                                break;
                            case "上":
                                id2+=2;
                                break;
                        }
                        if(bleDataManage.checkSendOk()){
                            switch (id) {
                                case 0:
                                    bleDataManage.sendParam((byte) (id+buttonId*5-5),id2,sqlManage.roll);
                                    break;
                                case 1:
                                    bleDataManage.sendParam((byte) (id+buttonId*5-5),id2,sqlManage.pitch);
                                    break;
                                case 2:
                                    bleDataManage.sendParam((byte) (id+buttonId*5-5),id2,sqlManage.yaw);
                                    break;
                                case 3:
                                    bleDataManage.sendParam((byte) (id+buttonId*5-5),id2,sqlManage.speed1);
                                    break;
                                case 4:
                                    bleDataManage.sendParam((byte) (id+buttonId*5-5),id2,sqlManage.speed2);
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
            case R.id.gun_left:
                ((TextView)findViewById(R.id.gun_num)).setText("左");;
                if(param2set!=null && init_state[1] .equals("左" ) && init_state[2].equals(String.valueOf(((TextView)findViewById(R.id.state)).getText())) )
                {
                    seekBar_roll.setProgress(floatToProgress(seekBar_roll,param2set[0]));
                    seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,param2set[1]));
                    seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,param2set[2]));
                    seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,param2set[3]));
                    seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,param2set[4]));
                }
                else
                {
                    if(!sqlManage.Select(buttonId,"左",String.valueOf(((TextView)findViewById(R.id.state)).getText()))){
                        sqlManage.roll=0.0f;
                        sqlManage.pitch=0.0f;
                        sqlManage.yaw=0.0f;
                        sqlManage.speed1=0;
                        sqlManage.speed2=0;
                    }
                    seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,sqlManage.pitch));
                    seekBar_roll.setProgress(floatToProgress(seekBar_roll,sqlManage.roll));
                    seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,sqlManage.yaw));
                    seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,sqlManage.speed1));
                    seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,sqlManage.speed2));
                }

                break;
            case R.id.gun_up:
                ((TextView)findViewById(R.id.gun_num)).setText("上");
                if(param2set!=null && init_state[1].equals("上") && init_state[2].equals(String.valueOf(((TextView)findViewById(R.id.state)).getText())))
                {
                    seekBar_roll.setProgress(floatToProgress(seekBar_roll,param2set[0]));
                    seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,param2set[1]));
                    seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,param2set[2]));
                    seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,param2set[3]));
                    seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,param2set[4]));
                }
                else
                {
                    if(!sqlManage.Select(buttonId,"上",String.valueOf(((TextView)findViewById(R.id.state)).getText()))){
                        sqlManage.roll=0.0f;
                        sqlManage.pitch=0.0f;
                        sqlManage.yaw=0.0f;
                        sqlManage.speed1=0;
                        sqlManage.speed2=0;
                    }
                    seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,sqlManage.pitch));
                    seekBar_roll.setProgress(floatToProgress(seekBar_roll,sqlManage.roll));
                    seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,sqlManage.yaw));
                    seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,sqlManage.speed1));
                    seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,sqlManage.speed2));
                }
                break;
            case R.id.gun_right:
                ((TextView)findViewById(R.id.gun_num)).setText("右");
                 if(param2set!=null && init_state[1].equals("右") && init_state[2].equals(String.valueOf(((TextView)findViewById(R.id.state)).getText())))
                {
                    seekBar_roll.setProgress(floatToProgress(seekBar_roll,param2set[0]));
                    seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,param2set[1]));
                    seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,param2set[2]));
                    seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,param2set[3]));
                    seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,param2set[4]));
                }
                else
                {
                    if(!sqlManage.Select(buttonId,"右",String.valueOf(((TextView)findViewById(R.id.state)).getText()))){
                        sqlManage.roll=0.0f;
                        sqlManage.pitch=0.0f;
                        sqlManage.yaw=0.0f;
                        sqlManage.speed1=0;
                        sqlManage.speed2=0;
                    }
                    seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,sqlManage.pitch));
                    seekBar_roll.setProgress(floatToProgress(seekBar_roll,sqlManage.roll));
                    seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,sqlManage.yaw));
                    seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,sqlManage.speed1));
                    seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,sqlManage.speed2));
                }
                break;
            case R.id.button_param_shotball://打球
                ((TextView)findViewById(R.id.state)).setText("打球");
                 if(param2set!=null && init_state!=null)
                {
                    if(init_state[1].equals(String.valueOf(((TextView)findViewById(R.id.gun_num)).getText())) && init_state[2].equals("打球"))
                    {
                        seekBar_roll.setProgress(floatToProgress(seekBar_roll,param2set[0]));
                        seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,param2set[1]));
                        seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,param2set[2]));
                        seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,param2set[3]));
                        seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,param2set[4]));
                    }
                }
                else
                {
                    if(!sqlManage.Select(buttonId,String.valueOf(((TextView)findViewById(R.id.gun_num)).getText()),"打球")){
                        sqlManage.roll=0.0f;
                        sqlManage.pitch=0.0f;
                        sqlManage.yaw=0.0f;
                        sqlManage.speed1=0;
                        sqlManage.speed2=0;
                    }
                    seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,sqlManage.pitch));
                    seekBar_roll.setProgress(floatToProgress(seekBar_roll,sqlManage.roll));
                    seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,sqlManage.yaw));
                    seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,sqlManage.speed1));
                    seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,sqlManage.speed2));
                }
                break;
            case R.id.button_param_shotfrisbee://打飞盘
                ((TextView)findViewById(R.id.state)).setText("打盘");
                 if(param2set!=null && init_state!=null)
                {
                    if( init_state[1].equals(String.valueOf(((TextView)findViewById(R.id.gun_num)).getText())) && init_state[2].equals("打盘"))
                    {
                        seekBar_roll.setProgress(floatToProgress(seekBar_roll,param2set[0]));
                        seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,param2set[1]));
                        seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,param2set[2]));
                        seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,param2set[3]));
                        seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,param2set[4]));
                    }
                }
                else
                {
                    if(!sqlManage.Select(buttonId,String.valueOf(((TextView)findViewById(R.id.gun_num)).getText()),"打盘")){
                        sqlManage.roll=0.0f;
                        sqlManage.pitch=0.0f;
                        sqlManage.yaw=0.0f;
                        sqlManage.speed1=0;
                        sqlManage.speed2=0;
                    }
                    seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,sqlManage.pitch));
                    seekBar_roll.setProgress(floatToProgress(seekBar_roll,sqlManage.roll));
                    seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,sqlManage.yaw));
                    seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,sqlManage.speed1));
                    seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,sqlManage.speed2));
                }
                break;
            case R.id.button_param_fly: //只是扔
                ((TextView)findViewById(R.id.state)).setText("扔");
                if(param2set!=null && init_state!=null)
                {
                    if(init_state[1].equals(String.valueOf(((TextView)findViewById(R.id.gun_num)).getText())) && init_state[2].equals("扔"))
                    {
                        seekBar_roll.setProgress(floatToProgress(seekBar_roll,param2set[0]));
                        seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,param2set[1]));
                        seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,param2set[2]));
                        seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,param2set[3]));
                        seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,param2set[4]));
                    }
                }
                else
                {
                    if(!sqlManage.Select(buttonId,String.valueOf(((TextView)findViewById(R.id.gun_num)).getText()),"扔")){
                        sqlManage.roll=0.0f;
                        sqlManage.pitch=0.0f;
                        sqlManage.yaw=0.0f;
                        sqlManage.speed1=0;
                        sqlManage.speed2=0;
                    }
                    seekBar_pitch.setProgress(floatToProgress(seekBar_pitch,sqlManage.pitch));
                    seekBar_roll.setProgress(floatToProgress(seekBar_roll,sqlManage.roll));
                    seekBar_yaw.setProgress(floatToProgress(seekBar_yaw,sqlManage.yaw));
                    seekBar_speed1.setProgress(floatToProgress(seekBar_speed1,sqlManage.speed1));
                    seekBar_speed2.setProgress(floatToProgress(seekBar_speed2,sqlManage.speed2));
                }
                break;
            default:
                Log.e("button","no case for button click");
                finish();
                break;
        }
        if(editText!=null&&seekBar!=null){
            int count=0;
            switch (v.getId())
            {
                case R.id.pitch_increase:   count++;
                case R.id.pitch_decrease:   count++;
                case R.id.roll_increase:    count++;
                case R.id.roll_decrease:    count++;
                case R.id.yaw_increase:     count++;
                case R.id.yaw_decrease:     count++;
                    valueF=Float.parseFloat(editText.getText().toString());
                    float stepSize=0.0f;
                    if(count==1||count==2)
                        stepSize=stepPitch;
                    if(count==3||count==4)
                        stepSize=stepRoll;
                    if(count==5||count==6)
                        stepSize=stepYaw;
                    if(count%2==1)
                        stepSize=-stepSize;

                        valueF+=stepSize;
                    seekBar.setProgress(floatToProgress(seekBar,valueF));
                    break;
                case R.id.speed1_increase:
                case R.id.speed2_increase:
                    valueI=Integer.parseInt(editText.getText().toString());
                    valueI+=stepSpeed;
                    seekBar.setProgress(floatToProgress(seekBar,valueI));
                    break;
                case R.id.speed1_decrease:
                case R.id.speed2_decrease:
                    valueI=Integer.parseInt(editText.getText().toString());
                    valueI-=stepSpeed;
                    seekBar.setProgress(floatToProgress(seekBar,valueI));
                    break;
                default:
                    Log.e("button","no case for button click");
                    finish();
                    break;
            }
        }
        String gunNum=((TextView)findViewById(R.id.gun_num)).getText().toString();
        String gunState=((TextView)findViewById(R.id.state)).getText().toString();
        String column=((TextView)findViewById(R.id.column_num)).getText().toString();
        if(gunNum.equals("上")){

            if(gunState.equals("打盘")&&column.equals("column: 7")){
                gain_roll=1;
                setRange(  40,    districtNum,    20,  maxSpeed,
                           -5,              0,   -20,  minSpeed,
                         0.1f,           1.0f,  0.2f,  stepSpeed);
                ((TextView)findViewById(R.id.roll_or_district)).setText("区域");
            }else{
                gain_roll=10;
                setRange(  40,    0,   20, maxSpeed,
                           -5,    0,  -20, minSpeed,
                         0.1f, 0.0f, 0.2f, stepSpeed);
                ((TextView)findViewById(R.id.roll_or_district)).setText("翻滚");
            }
            ((ProgressBar)findViewById(R.id.progress_speed2)).setMax(0);
        }
        else {
            gain_roll=10;
            setRange(  40,   45,   50, maxSpeed,
                      -10,    0,  -50, minSpeed,
                     0.5f, 0.5f, 0.5f, stepSpeed);
            ((TextView)findViewById(R.id.roll_or_district)).setText("翻滚");
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

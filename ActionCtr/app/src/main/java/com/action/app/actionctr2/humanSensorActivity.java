package com.action.app.actionctr2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.action.app.actionctr2.ble.BleService;
import com.action.app.actionctr2.ble.bleDataProcess;

import java.util.ArrayList;

import static com.action.app.actionctr2.R.id.start_on;

public class humanSensorActivity extends BasicActivity implements View.OnClickListener{


    private ArrayList<Button> buttonsBallList=new ArrayList<>();
    private ArrayList<Button> buttonsFrisbeeList=new ArrayList<>();
    private ArrayList<Button> buttonsColumnList=new ArrayList<>();
    private ArrayList<Button> buttonsDefendList=new ArrayList<>();

    private bleDataProcess bleDataManage;

    private boolean isDestroy=false;

    private Button starton;
    private Button goback;
    private Button wayleft;
    private Button waymiddle;
    private Button wayright;

    private void changeColorByMCU(final Context context)
    {
        final Handler handler=new Handler();
//        写个循环函数还搞新花样
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bleDataManage.getBinder() != null) {
                    final byte[] info = bleDataManage.getMCUinfo();
                    if (info != null) {
                      //  Log.d("humanSensor","check heartbeats info");
                        if (info.length == BleService.bleDataLen) {
                            Resources r = context.getResources();
                            int topGunDefendOrAttack=info[BleService.bleDataLen - 3];
                            Button btn=(Button) findViewById(R.id.human_topgun_defend_or_attack);
                            Button btnCopy=(Button) findViewById(R.id.human_topgun_defend_or_attack_copy);
                            if(topGunDefendOrAttack==0){
                                btn.setBackground(r.getDrawable(R.drawable.common_google_signin_btn_text_light));
                                btnCopy.setBackground(r.getDrawable(R.drawable.common_google_signin_btn_text_light));
                            } else if(topGunDefendOrAttack==1){
                                btn.setBackground(r.getDrawable(R.drawable.common_google_signin_btn_text_dark_disabled));
                                btnCopy.setBackground(r.getDrawable(R.drawable.common_google_signin_btn_text_light));
                            } else{
                                Toast.makeText(getApplicationContext(),"收到的蓝牙心跳包标志进攻防守错误",Toast.LENGTH_SHORT);
                            }

//                            通过心跳包进行回数
//                            心跳包的最后两个字节储存信息，一个字节八位，0到6位代表七个盘（球），1代表有命令
                            int ballInfo = info[BleService.bleDataLen - 2];
                            int frisbeeInfo = info[BleService.bleDataLen - 1];
                            for (int i = 0; i < 7; i++) {
                                int checkFrisbee = frisbeeInfo % 2;
                                int checkBall = ballInfo % 2;
                                if (checkFrisbee == 1) {
                                    buttonsFrisbeeList.get(i).setBackground(r.getDrawable(R.drawable.common_plus_signin_btn_text_dark));
                                 //   Log.d("humanSensor", "Frisbee  i： "+String.valueOf(i));
                                } else {
                                    buttonsFrisbeeList.get(i).setBackground(r.getDrawable(R.drawable.common_google_signin_btn_text_light_pressed));
                                  //  Log.d("humanSensor", "Frisbee ： "+String.valueOf(i));
                                }
                                if (checkBall == 1) {
                                    buttonsBallList.get(i).setBackground(r.getDrawable(R.drawable.common_plus_signin_btn_text_dark));
                                 //   Log.d("humanSensor", "Ball  i： "+String.valueOf(i));
                                } else {
                                    buttonsBallList.get(i).setBackground(r.getDrawable(R.drawable.common_google_signin_btn_text_light_pressed));
                                //    Log.d("humanSensor", "Ball  ： "+String.valueOf(i));
                                }
                                ballInfo /= 2;
                                frisbeeInfo /= 2;
                            }
                        } else {
                            Log.e("humanSensor", "请检查代码，心跳包长度不正常： "+String.valueOf(info.length));
                        }
                    }
                }
                if (!isDestroy) {
                    handler.postDelayed(this, 170);
                }
            }
        }, 300);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_human_sensor);

        starton=(Button)findViewById(start_on);
        goback=(Button)findViewById(R.id.goback);
        wayleft=(Button)findViewById(R.id.sun_onthewayleft);
        waymiddle=(Button)findViewById(R.id.sun_onthewaymiddle);
        wayright=(Button)findViewById(R.id.sun_onthewayright);
        goback.setOnClickListener(this);
        starton.setOnClickListener(this);
        wayleft.setOnClickListener(this);
        waymiddle.setOnClickListener(this);
        wayright.setOnClickListener(this);

        bleDataManage=new bleDataProcess(this);
//        按键的初始化
        for(int i=0;i<7;i++){
            buttonsColumnList.add(null);
            buttonsBallList.add(null);
            buttonsFrisbeeList.add(null);
            if(i<6)
                buttonsDefendList.add(null);
        }

        RelativeLayout layout=(RelativeLayout)findViewById(R.id.activity_human_sensor);
//        获取刚刚初始化的所有按键
        ArrayList<Button> list=myTool.getAllButton(layout);
//        把每个按键设定好监听，并加入到各自对应的button列表
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
        changeColorByMCU(this);
        if(getSharedPreferences("data",MODE_PRIVATE).getBoolean("topGunDefendOrAttack",false)){
            Log.d("humanSensor","test");
            ((Button)findViewById(R.id.human_topgun_defend_or_attack)).setTextColor(Color.parseColor("#000000"));
            ((Button)findViewById(R.id.human_topgun_defend_or_attack_copy)).setTextColor(Color.parseColor("#000000"));
        }

    }
    @Override
    public void onClick(final View v) {
//        对每个按钮进行循环，如果文字和我们点击的文字一样就发数
//        实际上就是替代了switch
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
        //射6和射7
        if(v.getId()==R.id.human_sensor_defend_shot6frisbee) {
            bleDataManage.sendCmd((byte)(20+5));
        }
        else if(v.getId()==R.id.human_sensor_defend_shot7frisbee){
            bleDataManage.sendCmd((byte)(20+6));
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
            case R.id.human_topgun_defend_or_attack:
            case R.id.human_topgun_defend_or_attack_copy:
                Button btn=(Button)findViewById(R.id.human_topgun_defend_or_attack);
                Button btnCopy=(Button)findViewById(R.id.human_topgun_defend_or_attack_copy);
                if(btn.getCurrentTextColor()!=(btnCopy.getCurrentTextColor())){
                    Toast.makeText(getApplicationContext(),"上枪状态错误",Toast.LENGTH_SHORT).show();
                }
                byte cmd=61;
                Log.d("humanSensor",String.valueOf(btn.getCurrentTextColor()));
                if(btn.getCurrentTextColor()== 0xde000000 ||btn.getCurrentTextColor()==0xffff0000){
                    cmd=60;
                    btn.setTextColor(Color.parseColor("#000000"));
                    btnCopy.setTextColor(Color.parseColor("#000000"));
                }
                else if(btn.getCurrentTextColor()== 0xff000000){
                    cmd=61;
                    btn.setTextColor(Color.parseColor("#FF0000"));
                    btnCopy.setTextColor(Color.parseColor("#FF0000"));
                }
                else{
                    Toast.makeText(getApplicationContext(),"上枪进攻防御切换时发生错误",Toast.LENGTH_SHORT).show();
                    Log.e("humanSensor","上枪进攻防御切换时发生错误");
                }
                bleDataManage.sendCmd(cmd);
                Log.d("humanSensor","send: "+String.valueOf(cmd));
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
                SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
                Boolean topGunState=false;
                if(((Button)findViewById(R.id.human_topgun_defend_or_attack)).getCurrentTextColor()==0xff000000){
                    topGunState=true;
                }
                Log.d("humanSensor","top:"+String.valueOf(topGunState));
                editor.putBoolean("topGunDefendOrAttack",topGunState);
                editor.commit();
                Intent intent= new Intent(this,BeginActivity.class);
                startActivity(intent);
                finish();
                break;
            case start_on:
                bleDataManage.sendCmd((byte)70);
                break;
            case R.id.goback:
                AlertDialog.Builder dialog1= new AlertDialog.Builder(humanSensorActivity.this);
                dialog1.setTitle("你真的确定要重装！！？？");
                dialog1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(v.getId()==R.id.human_sensor_reset)
                            bleDataManage.sendCmd((byte)71);
                    }
                });
                dialog1.show();
                break;
            case R.id.sun_onthewayleft:
                bleDataManage.sendCmd((byte)80);
                break;
            case R.id.sun_onthewaymiddle:
                bleDataManage.sendCmd((byte)81);
                break;
            case R.id.sun_onthewayright:
                bleDataManage.sendCmd((byte)82);
                break;
        }
    }

    @Override
    public void onDestroy() {
        isDestroy=true;
        super.onDestroy();
        bleDataManage.unbind();
    }

}

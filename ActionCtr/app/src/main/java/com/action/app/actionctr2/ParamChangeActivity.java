package com.action.app.actionctr2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.action.app.actionctr2.ble.bleDataProcess;
import com.action.app.actionctr2.sqlite.Manage;

import java.util.ArrayList;


public class ParamChangeActivity extends BasicActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    /*部分参数值的初始化设定*/
    private static final int STATE_NOT_READY_FOR_SERVICE = 0;
    private static final int STATE_RECEIVE_COPY = 1;
    private static final int STATE_RECEIVE_NONE = 2;
    //区域0~5
    private final int districtNum = 5;

    //航向角的最大值和最小值以及步长
    private int maxYaw = 50;
    private int minYaw = -50;
    private float stepYaw = 0.5f;

    //翻滚角的最大值和最小值以及步长
    private int maxRoll = 45;
    private int minRoll = -45;
    private float stepRoll = 0.5f;
    //增益  因为上枪柱子七，打盘的区域的增益和普通翻滚的增益不相同
    private int gain_roll = 10;

    //俯仰角的最大值和最小值以及步长
    private int maxPitch = 40;
    private int minPitch = 7;
    private float stepPitch = 0.5f;

    //最大速度和最小速度以及步长
    private int maxSpeed = 200;
    private int minSpeed = 0;
    private float stepSpeed = 0.5f;

    //    SQL数据库类的初始化
    private Manage sqlManage;
    /*控件的定义*/
    private bleDataProcess bleDataManage;

    //   手动输入框的定义
    private EditText editTextRoll;
    private EditText editTextPitch;
    private EditText editTextYaw;
    private EditText editTextSpeed1;
    private EditText editTextSpeed2;

    //    滑动输入框的定义
    private SeekBar seekBar_pitch;
    private SeekBar seekBar_roll;
    private SeekBar seekBar_yaw;
    private SeekBar seekBar_speed1;
    private SeekBar seekBar_speed2;

    static int countforMaxTime = 0;

    //    左上右按钮的集合
    private ArrayList<Button> gunList = new ArrayList<>();

    //    扔打球打盘的集合
    private ArrayList<Button> buttonParamList = new ArrayList<>();

    //    点改变的进度条
    private ProgressDialog progressDialog;

    //点击上述的六个按钮时颜色会改变
    private void checkButtonColor() {
//        对左上右按钮进行轮询
        for (Button button : gunList) {
            TextView textView = ((TextView) findViewById(R.id.gun_num));
//            如果按钮的文本和目前文本相同就变色
            if (button.getText().equals("枪." + textView.getText().toString())) {
                button.setTextColor(Color.parseColor("#6495ED"));
            } else {
                button.setTextColor(Color.parseColor("#000000"));
            }
        }
//        对扔打盘打球进行轮询，如果一样就变色
        for (Button button : buttonParamList) {
            TextView textView = ((TextView) findViewById(R.id.state));
            if (button.getText().equals(textView.getText().toString())) {
                button.setTextColor(Color.parseColor("#6495ED"));
            } else {
                button.setTextColor(Color.parseColor("#000000"));
            }
        }
    }

    //得出正确相对应的值，前两个精确到小数点一位，第三个根据情况而定，后两个都是整数
    private float progressToFloat(SeekBar seekBar, int val) {
        switch (seekBar.getId()) {
            case R.id.progress_yaw:
                return (val + minYaw * 10) / 10.0f;
            case R.id.progress_pitch:
                return (val + minPitch * 10) / 10.0f;
            case R.id.progress_roll:
                return (val + minRoll * gain_roll) / ((float) gain_roll);
            case R.id.progress_speed1:
                return (val + minSpeed * 10) / 10.0f;
            case R.id.progress_speed2:
                return (val + minSpeed * 10) / 10.0f;
            default:
                Log.e("paramChange", "err progressToFloat");
                return 0.0f;
        }
    }

    //    改变提示内容和范围大小
    private int floatToProgress(SeekBar seekBar, float val) {
//        枪，状态，柱子的指示文本
        String gunNum = ((TextView) findViewById(R.id.gun_num)).getText().toString();
        String gunState = ((TextView) findViewById(R.id.state)).getText().toString();
        String column = ((TextView) findViewById(R.id.column_num)).getText().toString();
//        初始化个对应的seekbar的值
        if (gunNum.equals("上")) {
            if (gunState.equals("打盘") && column.equals("column7")) {
//                此时应该是区域
                gain_roll = 1;
                /* (最大俯仰，最大翻滚（范围）, 最大航向, 最大速度,
                    最小俯仰，最小翻滚, 最小航向, 最小速度,
                    俯仰步长，翻滚步长, 航向步长, 速度步长)*/
                setRange(40, districtNum, 50, maxSpeed,
                        -10, 0, -50, minSpeed,
                        0.1f, 1.0f, 0.2f, stepSpeed);
                ((TextView) findViewById(R.id.roll_or_district)).setText("区域");
            } else {
                gain_roll = 10;
                setRange(40, 0, 50, maxSpeed,
                        -10, 0, -50, minSpeed,
                        0.1f, 0.0f, 0.2f, stepSpeed);
                ((TextView) findViewById(R.id.roll_or_district)).setText("翻滚");
            }
//            使上枪的速度2为0
            // ((ProgressBar) findViewById(R.id.progress_speed2)).setMax(0);
        } else {
            gain_roll = 10;
            setRange(40, 45, 50, maxSpeed,
                    7, -45, -50, minSpeed,
                    0.5f, 0.5f, 0.5f, stepSpeed);
            ((TextView) findViewById(R.id.roll_or_district)).setText("翻滚");
        }
//防止输入的值过大，并返回与下届的分度值总数
        switch (seekBar.getId()) {
            case R.id.progress_yaw:
                if (val < minYaw)
                    val = minYaw;
                if (val > maxYaw)
                    val = maxYaw;
                return Math.round((val - minYaw) * 10);
            case R.id.progress_pitch:
                if (val < minPitch)
                    val = minPitch;
                if (val > maxPitch)
                    val = maxPitch;
                return Math.round((val - minPitch) * 10);
            case R.id.progress_roll:
                if (val < minRoll)
                    val = minRoll;
                if (val > maxRoll)
                    val = maxRoll;
                return Math.round((val - minRoll) * gain_roll);
            case R.id.progress_speed1:
            case R.id.progress_speed2:
                if (val < minSpeed)
                    val = minSpeed;
                if (val > maxSpeed)
                    val = maxSpeed;
                return Math.round((val - minSpeed) * 10);
            default:
                Log.e("paramChange", "err floatToProgress");
                return 0;
        }
    }

    //改变参数范围值的大小
    private void setRange(int maxX, int maxY, int maxZ, int maxS,
                          int minX, int minY, int minZ, int minS,
                          float stepX, float stepY, float stepZ, float stepS) {
        maxYaw = maxZ;
        minYaw = minZ;
        stepYaw = stepZ;

        maxRoll = maxY;
        minRoll = minY;
        stepRoll = stepY;

        maxPitch = maxX;
        minPitch = minX;
        stepPitch = stepX;

        maxSpeed = maxS;
        minSpeed = minS;
        stepSpeed = stepS;

        seekBar_yaw.setMax((maxYaw - minYaw) * 10);
        seekBar_pitch.setMax((maxPitch - minPitch) * 10);
        seekBar_roll.setMax((maxRoll - minRoll) * gain_roll);
        seekBar_speed1.setMax((maxSpeed - minSpeed) * 10);
        seekBar_speed2.setMax((maxSpeed - minSpeed) * 10);
    }

    //  停靠位置中间，左，右分别对应着0,1,2
    private int readOntheWay() {
        int inOntheWay = 0;
        if (String.valueOf(((TextView) findViewById(R.id.column_onTheWay)).getText()).equals("途中：左")) {
            inOntheWay = 1;
        } else if (String.valueOf(((TextView) findViewById(R.id.column_onTheWay)).getText()).equals("途中：右")) {
            inOntheWay = 2;
        }
        return inOntheWay;
    }

    //    把数据库的值调出来放到滚动条上
    private void setProgressAll(Manage sqlManage) {
        seekBar_pitch.setProgress(floatToProgress(seekBar_pitch, sqlManage.pitch));
        seekBar_roll.setProgress(floatToProgress(seekBar_roll, sqlManage.roll));
        seekBar_yaw.setProgress(floatToProgress(seekBar_yaw, sqlManage.yaw));
        seekBar_speed1.setProgress(floatToProgress(seekBar_speed1, sqlManage.speed1));
        seekBar_speed2.setProgress(floatToProgress(seekBar_speed2, sqlManage.speed2));
    }

    //    把这个数组的值依次复制给滚动条
    private void setProgressAll(float[] param2set) {
        seekBar_roll.setProgress(floatToProgress(seekBar_roll, param2set[0]));
        seekBar_pitch.setProgress(floatToProgress(seekBar_pitch, param2set[1]));
        seekBar_yaw.setProgress(floatToProgress(seekBar_yaw, param2set[2]));
        seekBar_speed1.setProgress(floatToProgress(seekBar_speed1, param2set[3]));
        seekBar_speed2.setProgress(floatToProgress(seekBar_speed2, param2set[4]));
    }

    //    读layout中输入的值，并复制给自己的private参数
    private void readFromLayout(Manage sqlManage) {
        sqlManage.roll = Float.parseFloat(editTextRoll.getText().toString());
        sqlManage.pitch = Float.parseFloat(editTextPitch.getText().toString());
        sqlManage.yaw = Float.parseFloat(editTextYaw.getText().toString());
        sqlManage.speed1 = Float.parseFloat(editTextSpeed1.getText().toString());
        sqlManage.speed2 = Float.parseFloat(editTextSpeed2.getText().toString());
    }

    //识别区域并更新数据
    private void updateByRegion() {
        TextView textView = (TextView) findViewById(R.id.roll_or_district);
        if (textView.getText().equals("区域")) {
            float region = Float.parseFloat(((TextView) findViewById(R.id.edit_roll)).getText().toString());
            if (sqlManage.select("column" + String.valueOf(buttonId), String.valueOf(((TextView) findViewById(R.id.gun_num)).getText()), String.valueOf(((TextView) findViewById(R.id.state)).getText()), region)) {
                setProgressAll(sqlManage);
            }
        }
    }

    //柱子号
    private int buttonId;
    //参数值
    float[] param2set = new float[5];
    //三种状态
    String[] init_state = new String[3];

    @Override
    protected void onCreate(Bundle s) {

//      主构造函数初始化，界面加载，数据库管理器创建实例，ble管理器
        super.onCreate(s);
        setContentView(R.layout.activity_param_change);
        sqlManage = new Manage(this);
        bleDataManage = new bleDataProcess(this);

//        发射键
        findViewById(R.id.button_param_shot).setOnClickListener(this);//射

//        自动手动切换
        ((ToggleButton) findViewById(R.id.button_param_mode_change)).setOnCheckedChangeListener(this);

//        取消，保存，参数改变
        findViewById(R.id.button_param_cancel).setOnClickListener(this);
        findViewById(R.id.button_param_save).setOnClickListener(this);
        findViewById(R.id.button_param_change).setOnClickListener(this);

//        横滚，俯仰，航向，速度的加减按钮
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

//        左上右枪设置监听器，并添加到gunlist类里
        findViewById(R.id.gun_left).setOnClickListener(this);
        gunList.add((Button) findViewById(R.id.gun_left));
        findViewById(R.id.gun_right).setOnClickListener(this);
        gunList.add((Button) findViewById(R.id.gun_right));
        findViewById(R.id.gun_up).setOnClickListener(this);
        gunList.add((Button) findViewById(R.id.gun_up));

//        途中，左，右
        findViewById(R.id.gun_onTheWay).setOnClickListener(this);

//        打球，打盘，扔
        findViewById(R.id.button_param_shotball).setOnClickListener(this);
        buttonParamList.add((Button) findViewById(R.id.button_param_shotball));
        findViewById(R.id.button_param_shotfrisbee).setOnClickListener(this);
        buttonParamList.add((Button) findViewById(R.id.button_param_shotfrisbee));
        findViewById(R.id.button_param_fly).setOnClickListener(this);
        buttonParamList.add((Button) findViewById(R.id.button_param_fly));

//        初始化滚动条
        seekBar_pitch = ((SeekBar) findViewById(R.id.progress_pitch));
        seekBar_roll = ((SeekBar) findViewById(R.id.progress_roll));
        seekBar_yaw = ((SeekBar) findViewById(R.id.progress_yaw));
        seekBar_speed1 = ((SeekBar) findViewById(R.id.progress_speed1));
        seekBar_speed2 = ((SeekBar) findViewById(R.id.progress_speed2));

//        初始化滚动条的范围
        setRange(maxPitch, maxRoll, maxYaw, maxSpeed,
                minPitch, minRoll, minYaw, minSpeed,
                stepPitch, stepRoll, stepYaw, stepSpeed);

//        设置滚动条的监听器
        seekBar_pitch.setOnSeekBarChangeListener(this);
        seekBar_roll.setOnSeekBarChangeListener(this);
        seekBar_yaw.setOnSeekBarChangeListener(this);
        seekBar_speed1.setOnSeekBarChangeListener(this);
        seekBar_speed2.setOnSeekBarChangeListener(this);

        //由主机界面到来时，跳到相应的柱子上，数据库有内容时，更新数据库
        Intent intent = getIntent();
        buttonId = intent.getIntExtra("button_id", 0);
        if (buttonId != 0) {
            Log.d("paraChange", "buttonId: " + String.valueOf(buttonId));
            ((TextView) findViewById(R.id.column_num)).setText("column" + String.valueOf(buttonId));
            if (!sqlManage.Select(buttonId, "左", "扔", 0)) {
                sqlManage.setZero();
            }
        }

//        文本输入框功能
        editTextRoll = (EditText) findViewById(R.id.edit_roll);
        editTextPitch = (EditText) findViewById(R.id.edit_pitch);
        editTextYaw = (EditText) findViewById(R.id.edit_yaw);
        editTextSpeed1 = (EditText) findViewById(R.id.edit_speed1);
        editTextSpeed2 = (EditText) findViewById(R.id.edit_speed2);

//        在文本框里初始化数据库的内容
        (editTextRoll).setText(String.valueOf(sqlManage.roll));
        (editTextPitch).setText(String.valueOf(sqlManage.pitch));
        (editTextYaw).setText(String.valueOf(sqlManage.yaw));
        (editTextSpeed1).setText(String.valueOf(sqlManage.speed1));
        (editTextSpeed2).setText(String.valueOf(sqlManage.speed2));

//      接受由参数储存界面返回的数据
        param2set = intent.getFloatArrayExtra("param2set");
        init_state = intent.getStringArrayExtra("state2set");
//      MODE_PRIVATE  之内被创建他的应用打开  getBoolean(key, default value)
//        设定默认值
        ((ToggleButton) findViewById(R.id.button_param_mode_change)).setChecked(getSharedPreferences("data", MODE_PRIVATE).getBoolean("gun_mode_left", false));

//      虽然init_state在初始化的时候不是null，但是经过一次赋值之后变成了null
//        所以这里应该是参数界面导出值时用
        if (init_state != null) {
            int id = 0;
            ((TextView) findViewById(R.id.column_num)).setText(init_state[0]);
            ((TextView) findViewById(R.id.gun_num)).setText(init_state[1]);
//            左上右区分自动，手动
            if (init_state[1].equals("右")) {
                ((ToggleButton) findViewById(R.id.button_param_mode_change)).setChecked(getSharedPreferences("data", MODE_PRIVATE).getBoolean("gun_mode_right", false));
            }
            if (init_state[1].equals("上")) {
                ((ToggleButton) findViewById(R.id.button_param_mode_change)).setChecked(getSharedPreferences("data", MODE_PRIVATE).getBoolean("gun_mode_top", false));
            }
            ((TextView) findViewById(R.id.state)).setText(init_state[2]);
            ((TextView) findViewById(R.id.column_onTheWay)).setText(init_state[3]);
//            判断到底是柱子几  这就体现了学长代码归一性不是很好
            switch (init_state[0]) {
                case "column7":
                    id++;
                case "column6":
                    id++;
                case "column5":
                    id++;
                case "column4":
                    id++;
                case "column3":
                    id++;
                case "column2":
                    id++;
                case "column1":
                    id++;
                    buttonId = id;
                    break;
            }

            String gunNum = init_state[1];
            String gunState = init_state[2];
            String column = init_state[0];
            if (gunNum.equals("上")) {
                if (gunState.equals("打盘") && column.equals("column7")) {
                    gain_roll = 1;
                    /* (最大俯仰，最大翻滚（范围）, 最大航向, 最大速度,
                    最小俯仰，最小翻滚, 最小航向, 最小速度,
                    俯仰步长，翻滚步长, 航向步长, 速度步长)*/
                    setRange(40, districtNum, 50, maxSpeed,
                            -10, 0, -50, minSpeed,
                            0.1f, 1.0f, 0.2f, stepSpeed);
                    ((TextView) findViewById(R.id.roll_or_district)).setText("区域");
                } else {
                    gain_roll = 10;
                    setRange(40, 45, 50, maxSpeed,
                            -10, -45, -50, minSpeed,
                            0.1f, 0.0f, 0.2f, stepSpeed);
                    ((TextView) findViewById(R.id.roll_or_district)).setText("翻滚");
                }
                // ((ProgressBar) findViewById(R.id.progress_speed2)).setMax(0);
            } else {
                gain_roll = 10;
                setRange(40, 45, 50, maxSpeed,
                        7, -45, -50, minSpeed,
                        0.5f, 0.5f, 0.5f, stepSpeed);
                ((TextView) findViewById(R.id.roll_or_district)).setText("翻滚");
            }
        }
//      采用传过来的参数
        if (param2set != null) {
            setProgressAll(param2set);
        } else {
            setProgressAll(sqlManage);
        }

//        改变颜色
        checkButtonColor();

//        按钮组
        ViewGroup select_column = (ViewGroup) findViewById(R.id.param_change_select_column);
//        通过一个layout获得里面的所有按钮集合
        ArrayList<Button> arrayList = myTool.getAllButton(select_column);
        for (final Button button : arrayList) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    取第三个数字num（柱子num）
                    buttonId = Integer.parseInt(button.getText().subSequence(2, 3).toString());
                    ((TextView) findViewById(R.id.column_num)).setText("column" + String.valueOf(buttonId));
                    int inOntheWay = readOntheWay();
//                   更新数据类的值
                    if (!sqlManage.Select(buttonId, String.valueOf(((TextView) findViewById(R.id.gun_num)).getText()), String.valueOf(((TextView) findViewById(R.id.state)).getText()), inOntheWay)) {
                        sqlManage.setZero();
                    }
//                    把数据类的值填上去
                    setProgressAll(sqlManage);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        float valueF;
        float valueI;
        EditText editText = null;
        SeekBar seekBar = null;
        switch (v.getId()) {
//            点“射”，发送id号
            case R.id.button_param_shot:
                byte id = 0;
                switch (String.valueOf(((TextView) findViewById(R.id.gun_num)).getText())) {
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
//            点保存数据
            case R.id.button_param_save: {
                final int inOntheWay = readOntheWay();
//                用文本框里的值更新数据库
                readFromLayout(sqlManage);
//                是否需要加注释
//                创建一个文本框，把文本框里的内容进行插入
                if (!myTool.isForTestPara) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ParamChangeActivity.this);
                    final EditText commentText = new EditText(ParamChangeActivity.this);
                    dialog.setTitle("注意");
                    dialog.setMessage("您是否确认需要将改组参数保存到数据库?下面请输入注释");
                    dialog.setView(commentText);
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sqlManage.comment = commentText.getText().toString();
                            sqlManage.Insert(buttonId, String.valueOf(((TextView) findViewById(R.id.gun_num)).getText()), String.valueOf(((TextView) findViewById(R.id.state)).getText()), inOntheWay);
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
                } else {
                    sqlManage.comment = "";
                    sqlManage.Insert(buttonId, String.valueOf(((TextView) findViewById(R.id.gun_num)).getText()), String.valueOf(((TextView) findViewById(R.id.state)).getText()), inOntheWay);
                    Toast.makeText(ParamChangeActivity.this, "save ok", Toast.LENGTH_SHORT).show();
                }
            }
            break;
//            参数改变
            case R.id.button_param_change:
                boolean dialogIsShowing = false;
//                如果创建了加载框，就更新状态
                if (progressDialog != null) {
                    if (progressDialog.isShowing())
                        dialogIsShowing = true;
                }
                Log.d("change", "changeDialog is showing:    " + String.valueOf(dialogIsShowing));
//                如没有在转
                if (!dialogIsShowing) {
//                    从文本框里读数据
                    readFromLayout(sqlManage);
//
                    progressDialog = new ProgressDialog(ParamChangeActivity.this);
                    progressDialog.setTitle("data sending,please wait......");
                    progressDialog.setCancelable(false);
//                    仅仅设置点击屏幕不会返回
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
//                    循环设置
                    final Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        //                        再进入这个函数时，依然会初始化成0
                        private byte id = 0;
                        private byte id2 = 0;

                        @Override
                        public void run() {
                            //                            用两个switch使id2能表达出他所指的是什么枪的什么状态
                            switch (String.valueOf(((TextView) findViewById(R.id.state)).getText())) {
                                case "打球":
                                    id2 = 0;
                                    break;
                                case "打盘":
                                    id2 = 1;
                                    break;
                                case "扔":
                                    id2 = 2;
                                    break;
                            }
                            id2 = (byte) (id2 * 3);
                            switch (String.valueOf(((TextView) findViewById(R.id.gun_num)).getText())) {
                                case "左":
                                    id2 += 0;
                                    break;
                                case "右":
                                    id2 += 1;
                                    break;
                                case "上":
                                    id2 += 2;
                                    break;
                            }
                            int inOntheWay = readOntheWay();
                            id2 = (byte) (id2 + inOntheWay * 80);
//如果没连上，五个就这么直接过去了，只用WiFi发
                            if (((bleDataManage.checkSendOkFirst()||bleDataManage.checkSendOkSecond()) && bleDataManage.getBinder() != null)
                            ||(!bleDataManage.isReadyForDataFirst()&&!bleDataManage.isReadyForDataSecond())){
                                switch (id) {
                                    case 0:
                                        bleDataManage.sendParam((byte) (id + buttonId * 5 - 5), id2, sqlManage.roll);
                                        Log.d("datasend", "id is " + String.valueOf(id));
                                        break;
                                    case 1:
                                        bleDataManage.sendParam((byte) (id + buttonId * 5 - 5), id2, sqlManage.pitch);
                                        Log.d("datasend", "id is " + String.valueOf(id));
                                        break;
                                    case 2:
                                        bleDataManage.sendParam((byte) (id + buttonId * 5 - 5), id2, sqlManage.yaw);
                                        Log.d("datasend", "id is " + String.valueOf(id));
                                        break;
                                    case 3:
                                        bleDataManage.sendParam((byte) (id + buttonId * 5 - 5), id2, sqlManage.speed1);
                                        Log.d("datasend", "id is " + String.valueOf(id));
                                        break;
                                    case 4:
                                        bleDataManage.sendParam((byte) (id + buttonId * 5 - 5), id2, sqlManage.speed2);
                                        Log.d("datasend", "id is " + String.valueOf(id));
                                        break;
                                    case 5:
                                        progressDialog.cancel();
                                        break;
                                    default:
                                        progressDialog.cancel();
                                        Log.d("datasend", "id is " + String.valueOf(id));
                                        id=0;
                                        Log.e("change button", "onclick run err run err!!!!!");
                                        break;
                                }
//                                当其
                                if (id != 5) {
                                    handler.postDelayed(this, 50);
                                }
                                Log.d("datasend", "id ++ ");
                                id++;
                            } else {
                                countforMaxTime++;
                                if (countforMaxTime > 40) {
                                    progressDialog.cancel();
                                    countforMaxTime = 0;
                                    id=0;
                                    Log.d("bletrack", "reconnect " );
                                    Toast.makeText(ParamChangeActivity.this, "建议你重启app", Toast.LENGTH_SHORT).show();
                                }
                                else
                                handler.postDelayed(this, 50);
                            }
                        }
                    };
                    handler.postDelayed(runnable, 50);
                }
                break;
//            返回主界面
            case R.id.button_param_cancel:
                Intent intent = new Intent(this, BeginActivity.class);
                startActivity(intent);
                finish();
                break;
//            对加减按钮进行注册
            case R.id.pitch_increase:
                editText = (EditText) findViewById(R.id.edit_pitch);
                seekBar = (SeekBar) findViewById(R.id.progress_pitch);
                break;
            case R.id.roll_increase:
                editText = (EditText) findViewById(R.id.edit_roll);
                seekBar = (SeekBar) findViewById(R.id.progress_roll);
                break;
            case R.id.yaw_increase:
                editText = (EditText) findViewById(R.id.edit_yaw);
                seekBar = (SeekBar) findViewById(R.id.progress_yaw);
                break;
            case R.id.pitch_decrease:
                editText = (EditText) findViewById(R.id.edit_pitch);
                seekBar = (SeekBar) findViewById(R.id.progress_pitch);
                break;
            case R.id.roll_decrease:
                editText = (EditText) findViewById(R.id.edit_roll);
                seekBar = (SeekBar) findViewById(R.id.progress_roll);
                break;
            case R.id.yaw_decrease:
                editText = (EditText) findViewById(R.id.edit_yaw);
                seekBar = (SeekBar) findViewById(R.id.progress_yaw);
                break;
            case R.id.speed1_increase:
                editText = (EditText) findViewById(R.id.edit_speed1);
                seekBar = (SeekBar) findViewById(R.id.progress_speed1);
                break;
            case R.id.speed2_increase:
                editText = (EditText) findViewById(R.id.edit_speed2);
                seekBar = (SeekBar) findViewById(R.id.progress_speed2);
                break;
            case R.id.speed1_decrease:
                editText = (EditText) findViewById(R.id.edit_speed1);
                seekBar = (SeekBar) findViewById(R.id.progress_speed1);
                break;
            case R.id.speed2_decrease:
                editText = (EditText) findViewById(R.id.edit_speed2);
                seekBar = (SeekBar) findViewById(R.id.progress_speed2);
                break;
//            点击左枪时
            case R.id.gun_left:
//                设置提示文本
                ((TextView) findViewById(R.id.gun_num)).setText("左");
//                初始化手动按钮
                ((ToggleButton) findViewById(R.id.button_param_mode_change)).setChecked(getSharedPreferences("data", MODE_PRIVATE).getBoolean("gun_mode_left", true));
//                先判断是不是从数据加载页面跳过来的，然后去更新数据
                if (param2set != null && init_state[1].equals("左") && init_state[2].equals(String.valueOf(((TextView) findViewById(R.id.state)).getText()))) {
                    setProgressAll(param2set);
                } else {
                    int inOntheWay = readOntheWay();
                    if (!sqlManage.Select(buttonId, String.valueOf(((TextView) findViewById(R.id.gun_num)).getText()), String.valueOf(((TextView) findViewById(R.id.state)).getText()), inOntheWay)) {
                        sqlManage.setZero();
                    }
                    setProgressAll(sqlManage);
                }

                break;
            case R.id.gun_up:
                ((TextView) findViewById(R.id.gun_num)).setText("上");

                ((ToggleButton) findViewById(R.id.button_param_mode_change)).setChecked(getSharedPreferences("data", MODE_PRIVATE).getBoolean("gun_mode_top", true));

                if (param2set != null && init_state[1].equals("上") && init_state[2].equals(String.valueOf(((TextView) findViewById(R.id.state)).getText()))) {
                    setProgressAll(param2set);
                } else {
                    int inOntheWay = readOntheWay();
                    if (!sqlManage.Select(buttonId, String.valueOf(((TextView) findViewById(R.id.gun_num)).getText()), String.valueOf(((TextView) findViewById(R.id.state)).getText()), inOntheWay)) {
                        sqlManage.setZero();
                    }
                    setProgressAll(sqlManage);
                }
                break;
            case R.id.gun_right:
                ((TextView) findViewById(R.id.gun_num)).setText("右");

                ((ToggleButton) findViewById(R.id.button_param_mode_change)).setChecked(getSharedPreferences("data", MODE_PRIVATE).getBoolean("gun_mode_right", true));

                if (param2set != null && init_state[1].equals("右") && init_state[2].equals(String.valueOf(((TextView) findViewById(R.id.state)).getText()))) {
                    setProgressAll(param2set);
                } else {
                    int inOntheWay = readOntheWay();
                    if (!sqlManage.Select(buttonId, String.valueOf(((TextView) findViewById(R.id.gun_num)).getText()), String.valueOf(((TextView) findViewById(R.id.state)).getText()), inOntheWay)) {
                        sqlManage.setZero();
                    }
                    setProgressAll(sqlManage);
                }
                break;
//            点途中时则变换一次状态，然后根据文本的具体内容进行更新数据
            case R.id.gun_onTheWay: {
                if (String.valueOf(((TextView) findViewById(R.id.column_onTheWay)).getText()).equals("中间")) {
                    ((TextView) findViewById(R.id.column_onTheWay)).setText("途中：左");
                } else if (String.valueOf(((TextView) findViewById(R.id.column_onTheWay)).getText()).equals("途中：左")) {
                    ((TextView) findViewById(R.id.column_onTheWay)).setText("途中：右");
                } else {
                    ((TextView) findViewById(R.id.column_onTheWay)).setText("中间");
                }
                if (param2set != null && init_state[1].equals(String.valueOf(((TextView) findViewById(R.id.gun_num)).getText())) && init_state[2].equals(String.valueOf(((TextView) findViewById(R.id.state)).getText()))) {
                    setProgressAll(param2set);
                } else {
                    int inOntheWay = readOntheWay();
                    if (!sqlManage.Select(buttonId, String.valueOf(((TextView) findViewById(R.id.gun_num)).getText()), String.valueOf(((TextView) findViewById(R.id.state)).getText()), inOntheWay)) {
                        sqlManage.setZero();
                    }
                    setProgressAll(sqlManage);
                }
                break;
            }
//             一个套路
            case R.id.button_param_shotball://打球
                ((TextView) findViewById(R.id.state)).setText("打球");
                if (param2set != null && init_state != null) {
                    if (init_state[1].equals(String.valueOf(((TextView) findViewById(R.id.gun_num)).getText())) && init_state[2].equals("打球")) {
                        setProgressAll(param2set);
                    }
                } else {
                    int inOntheWay = readOntheWay();
                    if (!sqlManage.Select(buttonId, String.valueOf(((TextView) findViewById(R.id.gun_num)).getText()), String.valueOf(((TextView) findViewById(R.id.state)).getText()), inOntheWay)) {
                        sqlManage.setZero();
                    }
                    setProgressAll(sqlManage);
                }
                break;
            case R.id.button_param_shotfrisbee://打飞盘
                ((TextView) findViewById(R.id.state)).setText("打盘");
                if (param2set != null && init_state != null) {
                    if (init_state[1].equals(String.valueOf(((TextView) findViewById(R.id.gun_num)).getText())) && init_state[2].equals("打盘")) {
                        setProgressAll(param2set);
                    }
                } else {
                    int inOntheWay = readOntheWay();
                    if (!sqlManage.Select(buttonId, String.valueOf(((TextView) findViewById(R.id.gun_num)).getText()), String.valueOf(((TextView) findViewById(R.id.state)).getText()), inOntheWay)) {
                        sqlManage.setZero();
                    }
                    setProgressAll(sqlManage);
                }
                break;
            case R.id.button_param_fly: //只是扔
                ((TextView) findViewById(R.id.state)).setText("扔");
                if (param2set != null && init_state != null) {
                    if (init_state[1].equals(String.valueOf(((TextView) findViewById(R.id.gun_num)).getText())) && init_state[2].equals("扔")) {
                        setProgressAll(param2set);
                    }
                } else {
                    int inOntheWay = readOntheWay();
                    if (!sqlManage.Select(buttonId, String.valueOf(((TextView) findViewById(R.id.gun_num)).getText()), String.valueOf(((TextView) findViewById(R.id.state)).getText()), inOntheWay)) {
                        sqlManage.setZero();
                    }
                    setProgressAll(sqlManage);
                }
                break;
            default:
                Log.e("button", "no case for button click");
                finish();
                break;
        }
        if (editText != null && seekBar != null) {
            int count = 0;
            switch (v.getId()) {
                case R.id.pitch_increase:
                    count++;
                case R.id.pitch_decrease:
                    count++;
                case R.id.roll_increase:
                    count++;
                case R.id.roll_decrease:
                    count++;
                case R.id.yaw_increase:
                    count++;
                case R.id.yaw_decrease:
                    count++;
//                    每次点击按钮都会创建出一个对应的文本编辑
//                    获得一个值进行编辑
                    valueF = Float.parseFloat(editText.getText().toString());
                    float stepSize = 0.0f;
                    if (count == 5 || count == 6)
                        stepSize = stepPitch;
                    if (count == 3 || count == 4)
                        stepSize = stepRoll;
                    if (count == 1 || count == 2)
                        stepSize = stepYaw;
                    if (count % 2 == 1)
                        stepSize = -stepSize;
//                    对值进行修改
                    valueF += stepSize;
//                    进行值填入，并区分是否是上枪七台打盘
                    seekBar.setProgress(floatToProgress(seekBar, valueF));
                    if (seekBar.getId() == R.id.progress_roll) {
                        updateByRegion();
                    }
                    break;
                case R.id.speed1_increase:
                case R.id.speed2_increase:
                    valueI = Float.parseFloat(editText.getText().toString());
                    Log.d("change", String.valueOf(valueI));
                    valueI += stepSpeed;
                    Log.d("change", String.valueOf(valueI));
                    seekBar.setProgress(floatToProgress(seekBar, valueI));
                    break;
                case R.id.speed1_decrease:
                case R.id.speed2_decrease:
                    valueI = Float.parseFloat(editText.getText().toString());
                    Log.d("change", String.valueOf(valueI));
                    valueI -= stepSpeed;
                    Log.d("change", String.valueOf(valueI));
                    seekBar.setProgress(floatToProgress(seekBar, valueI));
                    break;
                default:
                    Log.e("button", "no case for button click");
                    finish();
                    break;
            }
        }
//        改颜色
        checkButtonColor();
    }

    //    通过进度条去调试文本框
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.progress_pitch:
                editTextPitch.setText(String.valueOf(progressToFloat(seekBar, progress)));
                break;
            case R.id.progress_roll:
                if (fromUser) {
                    updateByRegion();
                }
                editTextRoll.setText(String.valueOf(progressToFloat(seekBar, progress)));
                break;
            case R.id.progress_yaw:
                editTextYaw.setText(String.valueOf(progressToFloat(seekBar, progress)));
                break;
            case R.id.progress_speed1:
                editTextSpeed1.setText(String.valueOf(progressToFloat(seekBar, progress)));
                break;
            case R.id.progress_speed2:
                editTextSpeed2.setText(String.valueOf(progressToFloat(seekBar, progress)));
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    //
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        左上右的自动与手动,共六种
        int gunId = 3;
        switch (String.valueOf(((TextView) findViewById(R.id.gun_num)).getText())) {
            case "左":
                gunId += 1;
                break;
            case "右":
                gunId += 2;
                break;
            case "上":
                gunId += 3;
                break;
        }
        boolean[] gun_mode = new boolean[3];
        SharedPreferences dataSt = getSharedPreferences("data", MODE_PRIVATE);
        gun_mode[0] = dataSt.getBoolean("gun_mode_left", false);
        gun_mode[1] = dataSt.getBoolean("gun_mode_right", false);
        gun_mode[2] = dataSt.getBoolean("gun_mode_top", false);
        gun_mode[gunId - 4] = isChecked;
        if (isChecked) {
            gunId += 3;
        }
        bleDataManage.sendCmd((byte) (gunId));

        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putBoolean("gun_mode_left", gun_mode[0]);
        editor.putBoolean("gun_mode_right", gun_mode[1]);
        editor.putBoolean("gun_mode_top", gun_mode[2]);
        editor.commit();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sqlManage.close();

        bleDataManage.unbind();
    }
}

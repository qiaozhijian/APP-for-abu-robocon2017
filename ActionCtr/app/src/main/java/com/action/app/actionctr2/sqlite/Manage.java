package com.action.app.actionctr2.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.OkHttpClient;

/**
 * Created by 56390 on 2016/12/16.
 */

public class Manage {
    public float roll;
    public float pitch;
    public float yaw;
    public float speed1;
    public float speed2;
    public String ward;
    public String comment;

    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase dbRead;
    private SQLiteDatabase dbwrite;

    //      实例化一个数据库帮助类，实例化两个数据库
    public Manage(Context context) {
//        用谷歌浏览器看data
        Stetho.initializeWithDefaults(context);
        new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor()).build();
//        第二个参数为数据库名，要指定文件格式.db
        dbHelper = new MyDatabaseHelper(context, "DataStore.db", null, 3);
//         以下两个数据库的实例都是通过dbHelper获得的
        dbwrite = dbHelper.getWritableDatabase();
//        当数据库已满时，以只读的方式去打开数据库
        dbRead = dbHelper.getReadableDatabase();
    }

    //    数据库帮助类的close
    public void close() {
        dbHelper.close();
    }

    //    输入柱子编号，左上右枪，枪的状态，车停在哪个地方（中间还是两边）,就把各种数据插入dbwrite库
    public void Insert(int num, String gun, String state, int isOnTheWay) {

//        时间戳 y年 m月 d天 h时 m分 s秒
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        获得时间戳字符串
        String date = dateFormat.format(new Date(System.currentTimeMillis()));
        String cmd;

//      停在赛道的左中右
        if (isOnTheWay == 1) {
            cmd = "insert into onTheWay_column" + String.valueOf(num) + gun + state + " (roll,pitch,yaw,speed1,speed2,direction,save_date,note_comment) ";
        } else if (isOnTheWay == 2) {
            cmd = "insert into onTheWay2_column" + String.valueOf(num) + gun + state + " (roll,pitch,yaw,speed1,speed2,direction,save_date,note_comment) ";
        } else {
            cmd = "insert into column" + String.valueOf(num) + gun + state + " (roll,pitch,yaw,speed1,speed2,direction,save_date,note_comment) ";
        }

//        涉及到的各种参数值
        String data = "values(" + String.valueOf(roll) + ","
                + String.valueOf(pitch) + ","
                + String.valueOf(yaw) + ","
                + String.valueOf(speed1) + ","
                + String.valueOf(speed2) + ","
                + "'" + ward + "'" + ","
                + "'" + date + "'" + ","
                + "'" + comment + "'" + ");";
//        insert into 表名 （参数） values（值）
        dbwrite.execSQL(cmd + data);
        Log.d("database", "insert sql: " + cmd + data);
    }

    //    查询dbRead数据库的东西
    public int getDataBaseCount(String column, String gun, String state) {
//        通过查询表格的名称  表格的名称格式  column + gun + state 但是没有停靠位置？？？？
        Cursor cursor = dbRead.query(column + gun + state, null, null, null, null, null, null);
//        返回表格的函数的行数
        return (cursor.getCount());
    }

    //  查询表，由台柱号，枪，状态，停靠位置进行识别，然后更新manage类的参数
    public boolean Select(int num, String gun, String state, int isOnTheWay) {
        Cursor cursor;
        if (isOnTheWay == 1) {
            cursor = dbRead.query("onTheWay_column" + String.valueOf(num) + gun + state, null, null, null, null, null, null);
        } else if (isOnTheWay == 2) {
            cursor = dbRead.query("onTheWay2_column" + String.valueOf(num) + gun + state, null, null, null, null, null, null);
        } else {
            cursor = dbRead.query("column" + String.valueOf(num) + gun + state, null, null, null, null, null, null);
        }

//        如果表格的行数不为0，可能有不同的行，因为多次保存
        if (cursor.getCount() != 0) {
//            把光标移到最后一行（取最新的数据）
            cursor.moveToLast();
//            获取其列的索引，然后把值取出（但存的时候都是转成字符串存）
            roll = cursor.getFloat(cursor.getColumnIndex("roll"));
            pitch = cursor.getFloat(cursor.getColumnIndex("pitch"));
            yaw = cursor.getFloat(cursor.getColumnIndex("yaw"));
            speed1 = cursor.getFloat(cursor.getColumnIndex("speed1"));
            speed2 = cursor.getFloat(cursor.getColumnIndex("speed2"));
            ward = cursor.getString(cursor.getColumnIndex("direction"));
            comment = cursor.getString(cursor.getColumnIndex("note_comment"));
            Log.d("database", "data read:");

            Log.d("database", "roll: " + String.valueOf(roll));
            Log.d("database", "pitch: " + String.valueOf(pitch));
            Log.d("database", "yaw: " + String.valueOf(yaw));
            Log.d("database", "speed1: " + String.valueOf(speed1));
            Log.d("database", "speed2: " + String.valueOf(speed2));
            Log.d("database", "direction: " + ward);
            Log.d("database", "note_comment: " + comment);
            return true;
        } else {
            return false;
        }
    }

    //    把manage的参数都置0
    public boolean setZero() {
        roll = 0.0f;
        pitch = 0.0f;
        yaw = 0.0f;
        speed1 = 0;
        speed2 = 0;
        ward = "";
        comment = "";
        return true;
    }

    //    只查询停在中间时的数据库
    public boolean Select(int num, String gun, String state) {
        return Select(num, gun, state, 0);
    }

    //    提供台柱号，枪，状态，用ArrayList返回所有的查询结果
    public ArrayList<dataSt> selectAll(String num, String gun, String state) {
        Cursor cursor = dbRead.query(num + gun + state, null, null, null, null, null, null);
        ArrayList<dataSt> dataList = new ArrayList<>();
        while (cursor.moveToNext()) {
            dataSt data = new dataSt();
            data.roll = cursor.getFloat(cursor.getColumnIndex("roll"));
            data.pitch = cursor.getFloat(cursor.getColumnIndex("pitch"));
            data.yaw = cursor.getFloat(cursor.getColumnIndex("yaw"));
            data.speed1 = cursor.getFloat(cursor.getColumnIndex("speed1"));
            data.speed2 = cursor.getFloat(cursor.getColumnIndex("speed2"));
            data.date = cursor.getString(cursor.getColumnIndex("save_date"));
            data.direction = cursor.getString(cursor.getColumnIndex("direction"));
            data.note = cursor.getString(cursor.getColumnIndex("note_comment"));
            dataList.add(data);
        }
        return dataList;
    }

    //只查询第三个参数是区域的数据（七台的打盘） 然后返回相应数据
    public ArrayList<dataSt> selectColumn(String num, String gun, String state, String region) {

        Cursor cursor = dbRead.query(num + gun + state, null, "roll=?", new String[]{region}, null, null, null);
        ArrayList<dataSt> dataList = new ArrayList<>();
        while (cursor.moveToNext()) {
            dataSt data = new dataSt();
            data.roll = cursor.getFloat(cursor.getColumnIndex("roll"));
            data.pitch = cursor.getFloat(cursor.getColumnIndex("pitch"));
            data.yaw = cursor.getFloat(cursor.getColumnIndex("yaw"));
            data.speed1 = cursor.getFloat(cursor.getColumnIndex("speed1"));
            data.speed2 = cursor.getFloat(cursor.getColumnIndex("speed2"));
            data.date = cursor.getString(cursor.getColumnIndex("save_date"));
            data.direction = cursor.getString(cursor.getColumnIndex("direction"));
            data.note = cursor.getString(cursor.getColumnIndex("note_comment"));
            dataList.add(data);
        }
        return dataList;
    }

    //    提供台柱号，枪，状态，然后光标调到position位，用ArrayList返回所有的查询结果
    public dataSt selectOne(String num, String gun, String state, int position) {
        Cursor cursor = dbRead.query(num + gun + state, null, null, null, null, null, null);
        cursor.move(position);
        dataSt data = new dataSt();
        data.roll = cursor.getFloat(cursor.getColumnIndex("roll"));
        data.pitch = cursor.getFloat(cursor.getColumnIndex("pitch"));
        data.yaw = cursor.getFloat(cursor.getColumnIndex("yaw"));
        data.speed1 = cursor.getFloat(cursor.getColumnIndex("speed1"));
        data.speed2 = cursor.getFloat(cursor.getColumnIndex("speed2"));
        data.date = cursor.getString(cursor.getColumnIndex("save_date"));
        data.direction = cursor.getString(cursor.getColumnIndex("direction"));
        data.note = cursor.getString(cursor.getColumnIndex("note_comment"));
        return data;
    }

//    提供台柱号，枪，状态，区域，用manage类纪录数据
    public boolean select(String num, String gun, String state, float region) {
        try {
            Cursor cursor = dbRead.query(num + gun + state, null, null, null, null, null, null);
            cursor.moveToLast();
            while (true) {
                if (cursor.getFloat(cursor.getColumnIndex("roll")) == region) {
                    break;
                } else {
                    cursor.moveToPrevious();
                }
            }
            roll = cursor.getFloat(cursor.getColumnIndex("roll"));
            pitch = cursor.getFloat(cursor.getColumnIndex("pitch"));
            yaw = cursor.getFloat(cursor.getColumnIndex("yaw"));
            speed1 = cursor.getFloat(cursor.getColumnIndex("speed1"));
            speed2 = cursor.getFloat(cursor.getColumnIndex("speed2"));
            ward = cursor.getString(cursor.getColumnIndex("direction"));
            comment = cursor.getString(cursor.getColumnIndex("note_comment"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    //    提供台柱号，枪，状态，区域，跳到position位，用manage类纪录数据
    public dataSt selectOne(String num, String gun, String state, int position, float region) {
        try {
            Cursor cursor = dbRead.query(num + gun + state, null, null, null, null, null, null);
            cursor.moveToFirst();
            for (int i = 1; ; ) {
                if (cursor.getFloat(cursor.getColumnIndex("roll")) == region) {
                    i++;
                }
                if (i <= position)
                    cursor.moveToNext();
                else
                    break;
            }
            dataSt data = new dataSt();
            data.roll = cursor.getFloat(cursor.getColumnIndex("roll"));
            data.pitch = cursor.getFloat(cursor.getColumnIndex("pitch"));
            data.yaw = cursor.getFloat(cursor.getColumnIndex("yaw"));
            data.speed1 = cursor.getFloat(cursor.getColumnIndex("speed1"));
            data.speed2 = cursor.getFloat(cursor.getColumnIndex("speed2"));
            data.date = cursor.getString(cursor.getColumnIndex("save_date"));
            data.direction = cursor.getString(cursor.getColumnIndex("direction"));
            data.note = cursor.getString(cursor.getColumnIndex("note_comment"));
            roll = data.roll;
            pitch = data.pitch;
            yaw = data.yaw;
            speed1 = data.speed1;
            speed2 = data.speed2;
            comment = data.note;
            return data;
        } catch (Exception e) {
            dataSt data = new dataSt();
            return data;
        }

    }

//    删除某台柱某枪某状态某日期的数据y
    public void deleteOne(String num, String gun, String state, String date) {
        String table = num + gun + state;
        dbwrite.execSQL("delete from " + table + " where save_date=" + "'" + date + "'");
    }

    public class dataSt {
        public float roll;
        public float pitch;
        public float yaw;
        public float speed1;
        public float speed2;
        public String date;
        public String direction;
        public String note;
    }

    public class MyDatabaseHelper extends SQLiteOpenHelper {

        private Context mContext;

        //构造方法：第一个参数Context，第二个参数数据库名，第三个参数cursor允许我们在查询数据的时候返回一个自定义的光标位置，一般传入的都是null，第四个参数表示目前库的版本号（用于对库进行升级）
        public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            mContext = context;
        }

        private void createTable(String name, SQLiteDatabase db) {
            //调用SQLiteDatabase中的execSQL（）执行建表语句。
            String create = "create table " + name;
            String param = "( roll real," +
                    "pitch real," +
                    "yaw real," +
                    "speed1 real," +
                    "speed2 real," +
                    "direction text," +
                    "save_date text," +
                    "note_comment text);";
//建立7*3*3个数据库，七个柱子，三把枪，三种状态，这些数据库都属于传入的SQLiteDatabase
            for (int i = 1; i < 8; i++) {
                for (int j = 1; j < 4; j++) {
                    for (int k = 1; k < 4; k++) {
                        switch (j) {
                            case 1:
                                switch (k) {
                                    case 1:
                                        db.execSQL(create + String.valueOf(i) + "右" + "打球" + param);
                                        break;
                                    case 2:
                                        db.execSQL(create + String.valueOf(i) + "右" + "打盘" + param);
                                        break;
                                    case 3:
                                        db.execSQL(create + String.valueOf(i) + "右" + "扔" + param);
                                        break;
                                }
                                break;
                            case 2:
                                switch (k) {
                                    case 1:
                                        db.execSQL(create + String.valueOf(i) + "上" + "打球" + param);
                                        break;
                                    case 2:
                                        db.execSQL(create + String.valueOf(i) + "上" + "打盘" + param);
                                        break;
                                    case 3:
                                        db.execSQL(create + String.valueOf(i) + "上" + "扔" + param);
                                        break;
                                }
                                break;
                            case 3:
                                switch (k) {
                                    case 1:
                                        db.execSQL(create + String.valueOf(i) + "左" + "打球" + param);
                                        break;
                                    case 2:
                                        db.execSQL(create + String.valueOf(i) + "左" + "打盘" + param);
                                        break;
                                    case 3:
                                        db.execSQL(create + String.valueOf(i) + "左" + "扔" + param);
                                        break;
                                }
                                break;
                        }
                    }
                }
            }
        }

        //创建三张表
        @Override
        public void onCreate(SQLiteDatabase db) {
//            停靠点一，二，三
            createTable("column", db);
            createTable("onTheWay_column", db);
            createTable("onTheWay2_column", db);
            Toast.makeText(mContext, "database create", Toast.LENGTH_SHORT).show();
        }

        //这里注意，升级数据库的方式等版本确定后
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Toast.makeText(mContext, "database update", Toast.LENGTH_SHORT).show();
            if (oldVersion == 2 && newVersion == 3) {
                createTable("onTheWay2_column", db);
            }


        }
    }
}

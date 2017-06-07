package com.action.app.actionctr2.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import okhttp3.OkHttpClient;

/**
 * Created by 56390 on 2016/12/16.
 */

public class Manage {
    public float roll;
    public float pitch;
    public float yaw;
    public int   speed1;
    public int   speed2;
    public String   ward;
    public String   comment;

    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase dbRead;
    private SQLiteDatabase dbwrite;

    public Manage(Context context) {
        Stetho.initializeWithDefaults(context);
        new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor()).build();
        dbHelper = new MyDatabaseHelper(context,"DataStore.db",null,3);
        dbwrite=dbHelper.getWritableDatabase();
        dbRead=dbHelper.getReadableDatabase();
    }
    public void close(){
        dbHelper.close();
    }

    public void Insert(int num,String gun,String state,int isOnTheWay){
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date=dateFormat.format(new Date(System.currentTimeMillis()));
        String cmd;
        if(isOnTheWay==1) {
            cmd="insert into onTheWay_column"+String.valueOf(num)+gun+state+" (roll,pitch,yaw,speed1,speed2,direction,save_date,note_comment) ";
        }
        else if(isOnTheWay==2){
            cmd="insert into onTheWay2_column"+String.valueOf(num)+gun+state+" (roll,pitch,yaw,speed1,speed2,direction,save_date,note_comment) ";
        }
        else {
            cmd="insert into column"+String.valueOf(num)+gun+state+" (roll,pitch,yaw,speed1,speed2,direction,save_date,note_comment) ";
        }
         String data="values("+String.valueOf(roll)+","
                            +String.valueOf(pitch)+","
                            +String.valueOf(yaw)+","
                            +String.valueOf(speed1)+","
                            +String.valueOf(speed2)+","
                            +"'"+ward+"'"+","
                            +"'"+date+"'"+","
                            +"'"+comment+"'"+");";
        dbwrite.execSQL(cmd+data);
        Log.d("database","insert sql: "+cmd+data);
    }
    public  int getDataBaseCount(String column,String gun,String state){
        Cursor cursor=dbRead.query(column+gun+state,null,null,null,null,null,null);
        return(cursor.getCount());
    }
    public boolean Select(int num,String gun,String state,int isOnTheWay){
        Cursor cursor;
        if(isOnTheWay==1){
            cursor=dbRead.query("onTheWay_column"+String.valueOf(num)+gun+state,null,null,null,null,null,null);
        }
        else if(isOnTheWay==2){
            cursor=dbRead.query("onTheWay2_column"+String.valueOf(num)+gun+state,null,null,null,null,null,null);
        }
        else{
            cursor=dbRead.query("column"+String.valueOf(num)+gun+state,null,null,null,null,null,null);
        }

        if(cursor.getCount()!=0) {
            cursor.moveToLast();
            roll=cursor.getFloat(cursor.getColumnIndex("roll"));
            pitch=cursor.getFloat(cursor.getColumnIndex("pitch"));
            yaw=cursor.getFloat(cursor.getColumnIndex("yaw"));
            speed1=cursor.getInt(cursor.getColumnIndex("speed1"));
            speed2=cursor.getInt(cursor.getColumnIndex("speed2"));
            ward=cursor.getString(cursor.getColumnIndex("direction"));
            comment=cursor.getString(cursor.getColumnIndex("note_comment"));
            Log.d("database","data read:");

            Log.d("database","roll: "+String.valueOf(roll));
            Log.d("database","pitch: "+String.valueOf(pitch));
            Log.d("database","yaw: "+String.valueOf(yaw));
            Log.d("database","speed1: "+String.valueOf(speed1));
            Log.d("database","speed2: "+String.valueOf(speed2));
            Log.d("database","direction: "+ward);
            Log.d("database","note_comment: "+comment);
            return true;
        }
        else {
            return false;
        }
    }
    public boolean setZero(){
        roll=0.0f;
        pitch=0.0f;
        yaw=0.0f;
        speed1=0;
        speed2=0;
        ward="";
        comment="";
        return true;
    }
    public boolean Select(int num,String gun,String state){
        return Select(num,gun,state,0);
    }


    public ArrayList<dataSt> selectAll(String num,String gun,String state){
        Cursor cursor=dbRead.query(num+gun+state,null,null,null,null,null,null);
        ArrayList<dataSt> dataList=new ArrayList<>();
        while (cursor.moveToNext()) {
            dataSt data=new dataSt();
            data.roll=cursor.getFloat(cursor.getColumnIndex("roll"));
            data.pitch=cursor.getFloat(cursor.getColumnIndex("pitch"));
            data.yaw=cursor.getFloat(cursor.getColumnIndex("yaw"));
            data.speed1=cursor.getInt(cursor.getColumnIndex("speed1"));
            data.speed2=cursor.getInt(cursor.getColumnIndex("speed2"));
            data.date=cursor.getString(cursor.getColumnIndex("save_date"));
            data.direction=cursor.getString(cursor.getColumnIndex("direction"));
            data.note=cursor.getString(cursor.getColumnIndex("note_comment"));
            dataList.add(data);
        }
        return dataList;
    }
    public ArrayList<dataSt> selectColumn(String num,String gun,String state,String region){

        Cursor cursor=dbRead.query(num+gun+state,null,"roll=?",new String[]{region},null,null,null);
        ArrayList<dataSt> dataList=new ArrayList<>();
        while (cursor.moveToNext()) {
            dataSt data=new dataSt();
            data.roll=cursor.getFloat(cursor.getColumnIndex("roll"));
            data.pitch=cursor.getFloat(cursor.getColumnIndex("pitch"));
            data.yaw=cursor.getFloat(cursor.getColumnIndex("yaw"));
            data.speed1=cursor.getInt(cursor.getColumnIndex("speed1"));
            data.speed2=cursor.getInt(cursor.getColumnIndex("speed2"));
            data.date=cursor.getString(cursor.getColumnIndex("save_date"));
            data.direction=cursor.getString(cursor.getColumnIndex("direction"));
            data.note=cursor.getString(cursor.getColumnIndex("note_comment"));
            dataList.add(data);
        }
        return dataList;
    }
    public dataSt selectOne(String num,String gun,String state,int position){
        Cursor cursor=dbRead.query(num+gun+state,null,null,null,null,null,null);
        cursor.move(position);
        dataSt data=new dataSt();
        data.roll=cursor.getFloat(cursor.getColumnIndex("roll"));
        data.pitch=cursor.getFloat(cursor.getColumnIndex("pitch"));
        data.yaw=cursor.getFloat(cursor.getColumnIndex("yaw"));
        data.speed1=cursor.getInt(cursor.getColumnIndex("speed1"));
        data.speed2=cursor.getInt(cursor.getColumnIndex("speed2"));
        data.date=cursor.getString(cursor.getColumnIndex("save_date"));
        data.direction=cursor.getString(cursor.getColumnIndex("direction"));
        data.note=cursor.getString(cursor.getColumnIndex("note_comment"));
        return data;
    }
    public boolean select(String num,String gun,String state,float region){
        try {
            Cursor cursor=dbRead.query(num+gun+state,null,null,null,null,null,null);
            cursor.moveToLast();
            while (true){
                if(cursor.getFloat(cursor.getColumnIndex("roll"))==region) {
                    break;
                }
                else{
                    cursor.moveToPrevious();
                }
            }
            roll=cursor.getFloat(cursor.getColumnIndex("roll"));
            pitch=cursor.getFloat(cursor.getColumnIndex("pitch"));
            yaw=cursor.getFloat(cursor.getColumnIndex("yaw"));
            speed1=cursor.getInt(cursor.getColumnIndex("speed1"));
            speed2=cursor.getInt(cursor.getColumnIndex("speed2"));
            ward=cursor.getString(cursor.getColumnIndex("direction"));
            comment=cursor.getString(cursor.getColumnIndex("note_comment"));
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
    public dataSt selectOne(String num,String gun,String state,int position,float region){
        try {
            Cursor cursor=dbRead.query(num+gun+state,null,null,null,null,null,null);
            cursor.moveToFirst();
            for(int i=1;;) {
                if(cursor.getFloat(cursor.getColumnIndex("roll"))==region) {
                    i++;
                }
                if(i<=position)
                    cursor.moveToNext();
                else
                    break;
            }
            dataSt data=new dataSt();
            data.roll=cursor.getFloat(cursor.getColumnIndex("roll"));
            data.pitch=cursor.getFloat(cursor.getColumnIndex("pitch"));
            data.yaw=cursor.getFloat(cursor.getColumnIndex("yaw"));
            data.speed1=cursor.getInt(cursor.getColumnIndex("speed1"));
            data.speed2=cursor.getInt(cursor.getColumnIndex("speed2"));
            data.date=cursor.getString(cursor.getColumnIndex("save_date"));
            data.direction=cursor.getString(cursor.getColumnIndex("direction"));
            data.note=cursor.getString(cursor.getColumnIndex("note_comment"));
            roll=data.roll;
            pitch=data.pitch;
            yaw=data.yaw;
            speed1=data.speed1;
            speed2=data.speed2;
            comment=data.note;
            return data;
        }
        catch (Exception e){
            dataSt data=new dataSt();
            return data;
        }

    }
    public void deleteOne(String num,String gun,String state,String date){
        String table=num+gun+state;
        dbwrite.execSQL("delete from "+table+" where save_date="+"'"+date+"'");
    }
    public class dataSt{
        public float roll;
        public float pitch;
        public float yaw;
        public int   speed1;
        public int   speed2;
        public String date;
        public String direction;
        public String note;
    }

    public class MyDatabaseHelper extends SQLiteOpenHelper {

        private Context mContext;

        //构造方法：第一个参数Context，第二个参数数据库名，第三个参数cursor允许我们在查询数据的时候返回一个自定义的光标位置，一般传入的都是null，第四个参数表示目前库的版本号（用于对库进行升级）
        public  MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory , int version){
            super(context,name ,factory,version);
            mContext = context;
        }


        private void createTable(String name,SQLiteDatabase db) {
            //调用SQLiteDatabase中的execSQL（）执行建表语句。
            String create="create table "+name;
            String param= "( roll real," +
                    "pitch real," +
                    "yaw real," +
                    "speed1 integer," +
                    "speed2 integer,"+
                    "direction text,"+
                    "save_date text,"+
                    "note_comment text);";

            for(int i= 1;i< 8 ;i++)
            {
                for(int j = 1 ;j<4;j++)
                {
                    for(int k=1; k<4;k++)
                    {
                        switch(j)
                        {
                            case 1:
                                switch (k)
                                {
                                    case 1:
                                        db.execSQL(create+String.valueOf(i)+"右"+"打球"+param);
                                        break;
                                    case 2:
                                        db.execSQL(create+String.valueOf(i)+"右"+"打盘"+param);
                                        break;
                                    case 3:
                                        db.execSQL(create+String.valueOf(i)+"右"+"扔"+param);
                                        break;
                                }
                                break;
                            case 2:
                                switch (k)
                                {
                                    case 1:
                                        db.execSQL(create+String.valueOf(i)+"上"+"打球"+param);
                                        break;
                                    case 2:
                                        db.execSQL(create+String.valueOf(i)+"上"+"打盘"+param);
                                        break;
                                    case 3:
                                        db.execSQL(create+String.valueOf(i)+"上"+"扔"+param);
                                        break;
                                }
                                break;
                            case 3:
                                switch (k)
                                {
                                    case 1:
                                        db.execSQL(create+String.valueOf(i)+"左"+"打球"+param);
                                        break;
                                    case 2:
                                        db.execSQL(create+String.valueOf(i)+"左"+"打盘"+param);
                                        break;
                                    case 3:
                                        db.execSQL(create+String.valueOf(i)+"左"+"扔"+param);
                                        break;
                                }
                                break;
                        }
                    }
                }
            }
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            createTable("column",db);
            createTable("onTheWay_column",db);
            createTable("onTheWay2_column",db);
            Toast.makeText(mContext, "database create", Toast.LENGTH_SHORT).show();
        }

        @Override  //这里注意，升级数据库的方式等版本确定后
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Toast.makeText(mContext,"database update",Toast.LENGTH_SHORT).show();
            if(oldVersion==2&&newVersion==3) {
                createTable("onTheWay2_column",db);
            }


        }
    }
}
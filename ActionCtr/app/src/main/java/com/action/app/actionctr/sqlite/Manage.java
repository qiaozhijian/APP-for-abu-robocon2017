package com.action.app.actionctr.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.action.app.actionctr.ParamChangeActivity;
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


    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase dbRead;
    private SQLiteDatabase dbwrite;

    public Manage(Context context) {
        Stetho.initializeWithDefaults(context);
        new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor()).build();
        dbHelper = new MyDatabaseHelper(context,"DataStore.db",null,1);
        dbwrite=dbHelper.getWritableDatabase();
        dbRead=dbHelper.getReadableDatabase();
    }
    public void Insert(int num){
        String cmd="insert into column"+String.valueOf(num)+" (roll,pitch,yaw,speed1,speed2) ";
        String data="values("+String.valueOf(roll)+","
                +String.valueOf(pitch)+","
                +String.valueOf(yaw)+","
                +String.valueOf(speed1)+","
                +String.valueOf(speed2)+");";

        dbwrite.execSQL(cmd+data);
        Log.d("database","insert sql: "+cmd+data);
    }

    public boolean Select(int num){
        Cursor cursor=dbRead.query("column"+String.valueOf(num),null,null,null,null,null,null);

        if(cursor.getCount()!=0) {
            cursor.moveToLast();
            roll=cursor.getFloat(cursor.getColumnIndex("roll"));
            pitch=cursor.getFloat(cursor.getColumnIndex("pitch"));
            yaw=cursor.getFloat(cursor.getColumnIndex("yaw"));
            speed1=cursor.getInt(cursor.getColumnIndex("speed1"));
            speed2=cursor.getInt(cursor.getColumnIndex("speed2"));
            Log.d("database","data read:");

            Log.d("database","roll: "+String.valueOf(roll));
            Log.d("database","pitch: "+String.valueOf(pitch));
            Log.d("database","yaw: "+String.valueOf(yaw));

            Log.d("database","speed1: "+String.valueOf(speed1));
            Log.d("database","speed2: "+String.valueOf(speed2));

            return true;
        }
        else {
            return false;
        }
    }

    public class MyDatabaseHelper extends SQLiteOpenHelper {

        private Context mContext;

        //构造方法：第一个参数Context，第二个参数数据库名，第三个参数cursor允许我们在查询数据的时候返回一个自定义的光标位置，一般传入的都是null，第四个参数表示目前库的版本号（用于对库进行升级）
        public  MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory , int version){
            super(context,name ,factory,version);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //调用SQLiteDatabase中的execSQL（）执行建表语句。
            String create="create table column";
            String param= "( roll real," +
                    "pitch real," +
                    "yaw real," +
                    "speed1 integer," +
                    "speed2 integer);";

            db.execSQL(create+"1"+param);
            db.execSQL(create+"2"+param);
            db.execSQL(create+"3"+param);
            db.execSQL(create+"4"+param);
            db.execSQL(create+"5"+param);
            db.execSQL(create+"6"+param);
            db.execSQL(create+"7"+param);

            Toast.makeText(mContext, "database create", Toast.LENGTH_SHORT).show();
        }

        @Override  //这里注意，升级数据库的方式等版本确定后要  改成更好的
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Toast.makeText(mContext,"database updata",Toast.LENGTH_SHORT).show();
        }
    }
}

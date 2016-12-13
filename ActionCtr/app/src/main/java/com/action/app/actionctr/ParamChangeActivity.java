package com.action.app.actionctr;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;

/**
 * Created by 56390 on 2016/12/8.
 */

public class ParamChangeActivity extends BasicActivity implements View.OnClickListener {

    private int buttonId;
    private MyDatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle s)
    {
        super.onCreate(s);
        setContentView(R.layout.activity_param_change);

        //数据库监视器
        Stetho.initializeWithDefaults(this);//File  -> Project Sturcture -> dependences
        new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
        dbHelper = new MyDatabaseHelper(this,"DataStore.db",null,1);//注意最后一个参数 >1 才能升级数据库
        //软件卸载后可重新创建数据库



        findViewById(R.id.button_param_cancel).setOnClickListener(this);
        findViewById(R.id.button_param_save).setOnClickListener(this);
        findViewById(R.id.button_param_change).setOnClickListener(this);

        Intent intent=getIntent();
        buttonId=intent.getIntExtra("button_id",0);
        Log.d("buttonId",String.valueOf(buttonId));




    }
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_param_save:
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                /*删除指定行
                db.delete("Book","id=1",new String[]{});
                */
                /*查询数据
                Cursor cursor = db.query("Book",null,null,null,null,null,null);
                //调用moveToFirst()将数据指针移动到第一行的位置。
                if (cursor.moveToFirst()){
                    do {
                        //然后通过Cursor的getColumnIndex()获取某一列中所对应的位置的索引
                        String name = cursor.getString(cursor.getColumnIndex("name"));
                        String author = cursor.getString(cursor.getColumnIndex("author"));
                        double price = cursor.getDouble(cursor.getColumnIndex("price"));
                        Log.i("MainActivity","book name is "+name);
                        Log.i("MainActivity","book author is "+author);
                        Log.i("MainActivity","book price is "+price);
                    }while(cursor.moveToNext());
                }
                cursor.close();
                */
               /*更新指定行
                values.put("price",9.99);
                //仔细update中提示的参数（String table,ContentValues,String whereClause,String[] whereArgs）
                //第三滴四行指定具体更新那几行。注意第三个参数中的？是一个占位符，通过第四个参数为第三个参数中占位符指定相应的内容。
                db.update("Book",values,"id=1",new String[]{});
                */
                /*添加数据
                values.put("name","The Da Vinci Code");
                values.put("price",20);
                values.put("author","Scott");
                //insert（）方法中第一个参数是表名，第二个参数是表示给表中未指定数据的自动赋值为NULL。第三个参数是一个ContentValues对象
                db.insert("Book",null,values);
                values.clear();

                values.put("name","The Lost Symbol");
                values.put("author","Scott");
                values.put("price",30);
                db.insert("Book",null,values);
                */
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

    public class MyDatabaseHelper extends SQLiteOpenHelper {

        public static final String CREATE_BOOK = "create table BPNetwork(" +
                //primary key 将id列设为主键    autoincrement表示id列是自增长的
                "id integer primary key autoincrement," +
                "wL real," +
                "wR real," +
                "b_L real," +
                "b_R real)" ;

        private Context mContext;

        //构造方法：第一个参数Context，第二个参数数据库名，第三个参数cursor允许我们在查询数据的时候返回一个自定义的光标位置，一般传入的都是null，第四个参数表示目前库的版本号（用于对库进行升级）
        public  MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory , int version){
            super(context,name ,factory,version);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //调用SQLiteDatabase中的execSQL（）执行建表语句。
            db.execSQL(CREATE_BOOK);

            //创建成功
            Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
        }

        @Override  //这里注意，升级数据库的方式等版本确定后要  改成更好的
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //如果Book、Category表已存在则删除表
            db.execSQL("drop table if exists Book");
            onCreate(db);
            Toast.makeText(mContext,"updata",Toast.LENGTH_SHORT).show();
        }
    }
}

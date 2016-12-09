package com.action.app.actionctr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by 56390 on 2016/12/8.
 */

public class ParamChangeActivity extends BasicActivity implements View.OnClickListener {

    private int buttonId;
    @Override
    protected void onCreate(Bundle s)
    {
        super.onCreate(s);
        setContentView(R.layout.activity_param_change);
        findViewById(R.id.button_param_cancel).setOnClickListener(this);
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

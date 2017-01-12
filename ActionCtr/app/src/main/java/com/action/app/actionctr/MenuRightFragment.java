package com.action.app.actionctr;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by lenovo on 2017/1/12.
 */

public class MenuRightFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView menuList;
    private ArrayAdapter<String> adapter;
    private String[] menuItems = { "1", "2" ,"3","4","5","6","7","8"};
    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View view = inflater.inflate(R.layout.layout_meu, container, false);
                   menuList = (ListView) view.findViewById(R.id.list_debug);
                   menuList.setAdapter(adapter);
                   menuList.setOnItemClickListener(this);

            return view;

        }

    @Override
        public void onAttach(Context context) {
              super.onAttach(context);
              adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, menuItems);
        }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        switch(i)
        {
            case 0:
                Log.d("111","111");
                break;

        }

    }
}

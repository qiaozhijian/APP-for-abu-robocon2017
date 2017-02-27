package com.action.app.actionctr.excel;

import android.os.Environment;
import android.util.Log;

import com.action.app.actionctr.sqlite.Manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Created by lenovo on 2016/12/21.
 */
/*
   attention:
   2007~03版本的excel一个工作表最多有65536行
 */
public class Excel {

    private WritableWorkbook book;
    private File file;
    private String filename;
    private WritableSheet sheet_column1;
    private WritableSheet sheet_column2;
    private WritableSheet sheet_column3;
    private WritableSheet sheet_column4;
    private WritableSheet sheet_column5;
    private WritableSheet sheet_column6;
    private WritableSheet sheet_column7;
    private WritableSheet sheet_column;
    ArrayList<WritableSheet> StoreData;

    public Excel(String name) //形式 "....xls"
    {
        filename = name ;
    }
    //得到 excel 存储路径
    public static String getExcelDir() {
        // 外部存储区域，包括SD卡  以及 手机内部存储
        String sdcardPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        File dir = new File(sdcardPath);
        if (dir.exists()) {
            return dir.toString();
        } else {
            dir.mkdirs();
            Log.e("EXCEL", "path not found!!!");
            return dir.toString();
        }
    }
    public void storeExcel(ArrayList<String> debugData) //用于wifi保存
    {
        Label label;

        String sheetName;

        int sheetNumber;
        sheetNumber = debugData.size()/65536;
        if(debugData.size()%65536 != 0)
            sheetNumber = sheetNumber+1;

        StoreData = new ArrayList<>();
        try {
            file = new File((getExcelDir() + File.separator + filename));
            if (!file.exists()) {
                book = Workbook.createWorkbook(file);
                for(int j = 0;j<sheetNumber;j++)
                {
                    StoreData.add( book.createSheet("DebugDataSheet"+toString().valueOf(j),j));
                }
                //生成工作表
                for(int j=0;j<sheetNumber;j++)
                {
                    if(j == sheetNumber-1) {
                        for(int i=0;i <  debugData.size()%65536 ; i++){
                            label = new Label(0,i,debugData.get(i + 65536*j));
                            StoreData.get(j).addCell(label);
                        }
                    }else{
                        for(int i=0;i<65536;i++){
                            label = new Label(0,i,debugData.get(i + 65536*j));
                            StoreData.get(j).addCell(label);
                        }
                    }

                }
                book.write();
                book.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }catch (WriteException e) {
            e.printStackTrace();
        }
    }
    //用于数据库保存
    public void storeExcel(Manage database,String column,String gun,String state) {
        ArrayList<Manage.dataSt> dataList = new ArrayList<>();
        Label label;
        jxl.write.Number number;
        try {

            file = new File((getExcelDir() + File.separator +column+gun+state+ filename));
            if (!file.exists()) {
                //新建文件
                book = Workbook.createWorkbook(file);
                // 生成工作表，参数0表示是第一页
                sheet_column = book.createSheet(column+gun+state, 0);
                dataList = database.selectAll(column,gun,state);
                if (database.getDataBaseCount( column,gun,state ) != 0) {
                    for (int i = 0; i < database.getDataBaseCount(column,gun,state); i++) {
                        number = new jxl.write.Number(0,i,dataList.get(i).roll);// 生成一个保存数字的单元格，必须使用Number的完整包路径，否则有语法歧义。单元格位置是第二列，第一行，值为123
                        sheet_column.addCell(number);
                        number = new jxl.write.Number(1,i, dataList.get(i).pitch);// 生成一个保存数字的单元格，必须使用Number的完整包路径，否则有语法歧义。单元格位置是第二列，第一行，值为123
                        sheet_column.addCell(number);
                        number = new jxl.write.Number(2,i, dataList.get(i).yaw);// 生成一个保存数字的单元格，必须使用Number的完整包路径，否则有语法歧义。单元格位置是第二列，第一行，值为123
                        sheet_column.addCell(number);
                        number = new jxl.write.Number(3,i, dataList.get(i).speed1);// 生成一个保存数字的单元格，必须使用Number的完整包路径，否则有语法歧义。单元格位置是第二列，第一行，值为123
                        sheet_column.addCell(number);
                        number = new jxl.write.Number(4,i, dataList.get(i).speed2);// 生成一个保存数字的单元格，必须使用Number的完整包路径，否则有语法歧义。单元格位置是第二列，第一行，值为123
                        sheet_column.addCell(number);
                        label = new Label(5, i, dataList.get(i).direction);//在Label对象的构造子中前两个参数坐标(0,0)，第三个参数内容
                        sheet_column.addCell(label);
                        label = new Label(6,i, dataList.get(i).date);
                        sheet_column.addCell(label);
                    }
                }

                book.write();
                book.close();
            }
        } catch (WriteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.example.myapp5;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("DefaultLocale")
public class UserDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "UserDBHelper";
    private static final String DB_NAME = "mooduser.db"; // 数据库的名称
    private static UserDBHelper mHelper = null; // 数据库帮助器的实例
    private SQLiteDatabase mDB = null; // 数据库的实例
    private static final int mVersion = 1;
    protected SQLiteDatabase mWriteDB;
    protected SQLiteDatabase mReadDB;
    public static final String TABLE_NAME = "user_info"; // 表的名称

    private UserDBHelper(Context context) {
        super(context, DB_NAME, null, mVersion);
        mWriteDB = this.getWritableDatabase();
        mReadDB = this.getReadableDatabase();
    }
    // 利用单例模式获取数据库帮助器的唯一实例
    public static UserDBHelper getInstance(Context context) {
        if (mHelper == null) {
            mHelper = new UserDBHelper(context);
        }
        return mHelper;
    }

    // 打开数据库的读连接
    public SQLiteDatabase openReadLink() {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mHelper.getReadableDatabase();
        }
        return mDB;
    }

    // 打开数据库的写连接
    public SQLiteDatabase openWriteLink() {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mHelper.getWritableDatabase();
        }
        return mDB;
    }

    // 关闭数据库连接
    public void closeLink() {
        if (mDB != null && mDB.isOpen()) {
            mDB.close();
            mDB = null;
        }
    }

    // 创建数据库，执行建表语句
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        String drop_sql = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
        Log.d(TAG, "drop_sql:" + drop_sql);
        db.execSQL(drop_sql);
        String create_sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL,"
                + "date VARCHAR NOT NULL," + "month INTEGER NOT NULL,"
                + "create_time VARCHAR NOT NULL," + "update_time VARCHAR NULL,"
                //+ "name VARCHAR NOT NULL," + "age INTEGER NOT NULL,"
                //+ "height INTEGER NOT NULL," + "weight FLOAT NOT NULL,"
                + "title VARCHAR NOT NULL," + "text VARCHAR NOT NULL,"
                +"Anger INTEGER NOT NULL,"+"Disgust INTEGER NOT NULL,"
                +"Fear INTEGER NOT NULL,"+"Happy INTEGER NOT NULL,"
                +"Neutral INTEGER NOT NULL,"+"Sad INTEGER NOT NULL,"
                +"Surprise INTEGER NOT NULL"
                //演示数据库升级时要先把下面这行注释
                //+ ",phone VARCHAR" + ",password VARCHAR"
                + ");";
        Log.d(TAG, "create_sql:" + create_sql);
        db.execSQL(create_sql); // 执行完整的SQL语句
    }



    // 升级数据库，执行表结构变更语句
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // 根据指定条件删除表记录
    public void delete(int id) {
        String delete_sql = String.format("delete from %s where _id=%d;", TABLE_NAME,id);
        Log.d(TAG, "delete sql="+delete_sql);
        mWriteDB.execSQL(delete_sql);
    }

    // 删除该表的所有记录
    public int deleteAll() {
        // 执行删除记录动作，该语句返回删除记录的数目
        return mDB.delete(TABLE_NAME, "1=1", null);
    }

    // 往该表添加一条记录
    public long insert(UserInfo info) {
        List<UserInfo> infoList=new ArrayList<UserInfo>();
        infoList.add(info);
        return insert(infoList);
    }


    // 往该表添加多条记录
    public long insert(List<UserInfo> infoList) {
        long result = -1;
        for (int i = 0; i < infoList.size(); i++) {
            UserInfo info = infoList.get(i);
            ContentValues cv = new ContentValues();
            cv.put("date", info.date);
            cv.put("month", info.month);
            cv.put("create_time", info.create_time);
            cv.put("update_time", info.update_time);
            cv.put("title", info.title);
            cv.put("text", info.text);
            cv.put("Anger", info.Anger);
            cv.put("Disgust", info.Disgust);
            cv.put("Happy", info.Happy);
            cv.put("Fear", info.Fear);
            cv.put("Neutral", info.Neutral);
            cv.put("Surprise", info.Surprise);
            cv.put("Sad", info.Sad);
            // 执行插入记录动作，该语句返回插入记录的行号

            //test="INSERT INTO user_info VALUES (info.title,info.text);";
            //mDB.execSQL(test);
            result = mWriteDB.insert(TABLE_NAME, "", cv);

            if (result == -1) { // 添加成功则返回行号，添加失败则返回-1
                return result;
            }
        }
        return result;
    }

    // 根据条件更新指定的表记录
    public int update(UserInfo info, String condition) {
        ContentValues cv = new ContentValues();
        cv.put("date", info.date);
        cv.put("month", info.month);
        cv.put("create_time", info.create_time);
        cv.put("update_time", info.update_time);
        cv.put("title", info.title);
        cv.put("text", info.text);
        cv.put("Anger", info.Anger);
        cv.put("Disgust", info.Disgust);
        cv.put("Happy", info.Happy);
        cv.put("Fear", info.Fear);
        cv.put("Neutral", info.Neutral);
        cv.put("Surprise", info.Surprise);
        cv.put("Sad", info.Sad);
        // 执行更新记录动作，该语句返回更新的记录数量
        mWriteDB.update(TABLE_NAME, cv, condition, null);
        return mWriteDB.update(TABLE_NAME, cv, condition, null);

    }

    public int update(UserInfo info) {
        // 执行更新记录动作，该语句返回更新的记录数量
        return update(info, "rowid=" + info.rowid);

    }

    // 根据指定条件查询记录，并返回结果数据列表
    public List<UserInfo>query(String condition) {
        String sql = String.format("select rowid,_id,date,month,title,text,create_time,update_time,Anger,Disgust,Fear,Happy,Neutral,Sad,Surprise " +
                "from %s where %s;", TABLE_NAME, condition);
        Log.d(TAG, "query sql: " + sql);
        List<UserInfo> infoList = new ArrayList<>();
        // 执行记录查询动作，该语句返回结果集的游标
        Cursor cursor = mReadDB.rawQuery(sql, null);
        // 循环取出游标指向的每条记录
        while (cursor.moveToNext()) {
            UserInfo info = new UserInfo();
            info.rowid = cursor.getLong(0); // 取出长整型数
            info.xuhao = cursor.getInt(1); // 取出整型数
            info.date = cursor.getString(2); // 取出字符串
            info.month = cursor.getInt(3); // 取出整型数
            info.title = cursor.getString(4);// 取出字符串
            info.text = cursor.getString(5);
            info.create_time = cursor.getString(6); // 取出字符串
            info.update_time = cursor.getString(7); // 取出字符串
            info.Anger= cursor.getInt(8);
            info.Disgust= cursor.getInt(9);
            info.Fear= cursor.getInt(10);
            info.Happy= cursor.getInt(11);
            info.Neutral= cursor.getInt(12);
            info.Sad= cursor.getInt(13);
            info.Surprise= cursor.getInt(14);
            infoList.add(info);
        }
        cursor.close(); // 查询完毕，关闭数据库游标
        return infoList;
    }
    public List<UserInfo> queryByMonth(int month) {
        return query("month="+month+" order by date asc");
    }
    public List<UserInfo> queryById(int id) {
        String sql = " _id=" + id + ";";
        return query(sql);
    }
    public int sumScore(String condition) {
        String sql = String.format("select sum(%s)" +
                "from %s ;", condition, TABLE_NAME);
        Log.d(TAG, "query sql: " + sql);
        List<UserInfo> infoList = new ArrayList<>();
        // 执行记录查询动作，该语句返回结果集的游标
        Cursor cursor = mReadDB.rawQuery(sql, null);
        int sum = 0;
        if (cursor!=null)
        {
            if (cursor.moveToFirst())
            {
                do
                {
                    sum=cursor.getInt(0);
                } while (cursor.moveToNext());
            }
        }
        return sum;
    }
    public void save(UserInfo bill) {
        // 根据序号寻找对应的账单记录
        List<UserInfo> bill_list = (List<UserInfo>) queryById(bill.xuhao);
        UserInfo info = null;
        if (bill_list.size() > 0) { // 有找到账单记录
            info = bill_list.get(0);
        }
        if (info != null) { // 已存在该账单信息，则更新账单
            bill.rowid = info.rowid;
            bill.create_time = info.create_time;
            bill.update_time = DateUtil.getNowDateTime("");
            update(bill); // 更新数据库记录
        } else { // 未存在该账单信息，则添加账单
            bill.create_time = DateUtil.getNowDateTime("");
            insert(bill); // 添加数据库记录
        }
    }
    // 根据序号查询记录


}

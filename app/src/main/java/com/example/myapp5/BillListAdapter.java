package com.example.myapp5;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BillListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener{
    private static final String TAG = "BillListAdapter";
    private Context mContext; // 声明一个上下文对象
    private List<UserInfo> mBillList = new ArrayList<UserInfo>(); // 账单信息列表

    public BillListAdapter(Context context, List<UserInfo> billList) {
        mContext = context;
        mBillList = billList;
    }

    @Override
    public int getCount() {
        return mBillList.size();
    }

    @Override
    public Object getItem(int position) {
        return mBillList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            // 根据布局文件item_bill.xml生成转换视图对象
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_bill, null);
            holder.date = convertView.findViewById(R.id.tv_date);
            holder.title = convertView.findViewById(R.id.tv_desc);
            holder.text = convertView.findViewById(R.id.tv_amount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        UserInfo bill = mBillList.get(position);
        holder.date.setText(bill.date);
        holder.title.setText(bill.title);
        holder.text.setText("心情值：");
        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= mBillList.size()-1) { // 合计行不响应点击事件
            return;
        }
        Log.d(TAG, "onItemClick position=" + position);
        UserInfo bill = mBillList.get(position);
        // 以下跳转到账单填写页面
        Intent intent = new Intent(mContext, SQLiteWriteActivity.class);
        intent.putExtra("xuhao", bill.xuhao); // 携带账单序号，表示已存在该账单
        mContext.startActivity(intent); // 因为已存在该账单，所以跳过去实际会编辑账单
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if (position >= mBillList.size()-1) { // 合计行不响应长按事件
            return true;
        }
        Log.d(TAG, "onItemLongClick position=" + position);
        UserInfo bill = mBillList.get(position); // 获得当前位置的账单信息
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String desc = String.format("是否删除以下日记？\n%s %s", bill.date,bill.title );
        builder.setMessage(desc); // 设置提醒对话框的消息文本
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteBill(position); // 删除该账单
            }
        });
        builder.setNegativeButton("否", null);
        builder.create().show(); // 显示提醒对话框
        return true;
    }

    // 删除该账单
    private void deleteBill(int position) {
        UserInfo bill = mBillList.get(position);
        mBillList.remove(position);
        notifyDataSetChanged(); // 通知适配器发生了数据变化
        // 获得数据库帮助器的实例
        UserDBHelper helper = UserDBHelper.getInstance(mContext);
        helper.delete(bill.xuhao); // 从数据库删除指定序号的账单
    }

    public final class ViewHolder {
        public TextView date;
        public TextView title;
        public TextView text;
    }

}

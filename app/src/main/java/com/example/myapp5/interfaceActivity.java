package com.example.myapp5;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapp5.cameraUtils.SPUtils;
import com.google.android.material.imageview.ShapeableImageView;

public class interfaceActivity extends AppCompatActivity {

    private Button mBtnRiv1;
    private TextView mTv1;
    private ShapeableImageView mShap;

    private RequestOptions requestOptions = RequestOptions.circleCropTransform()
            .diskCacheStrategy(DiskCacheStrategy.NONE)//不做磁盘缓存
            .skipMemoryCache(true);//不做内存缓存

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);
        mTv1=findViewById((R.id.rig_2));
        mBtnRiv1=findViewById(R.id.rig_3);
        mShap = findViewById(R.id.rig_1);

        //状态栏背景透明
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        //取出缓存
        String imageUrl = SPUtils.getString("imageUrl",null,this);
        if(imageUrl != null){
            Glide.with(this).load(imageUrl).apply(requestOptions).into(mShap);
        }

        mBtnRiv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(interfaceActivity.this,PersonalActivity.class);
                //startActivity(intent);
                //接收返回
                startActivityForResult(intent,0);
                startActivityForResult(intent,1);

            }
        });
    }
    //接受返回昵称
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mTv1.setText(data.getExtras().getString("name"));
        //接收返回头像并转string为image 未完！！！
//        if (resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            byte[] b = extras.getByteArray("picture");
//            Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
//            ImageView image = (ImageView) findViewById(R.id.rig_1);
//
//            image.setImageBitmap(bmp);
//        }
    }
}
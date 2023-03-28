package com.example.myapp5;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.myapp5.cameraUtils.BitmapUtils;
import com.example.myapp5.cameraUtils.CameraUtils;
import com.example.myapp5.cameraUtils.SPUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.tbruyelle.rxpermissions3.RxPermissions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class MessageBody {
    public UserInfo info;
    public String response;
    public String text_response;
    public SQLiteWriteActivity parent;
};

public class SQLiteWriteActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = "connectservererror";
    private UserDBHelper mHelper; // 声明一个用户数据库帮助器的对象
    private EditText title;
    private EditText text;
    private TextView date;
    private Button btn1, btn2;
    private String AllDiary = " ";
    private int xuhao;
    private int Anger = 0;
    private int Happy = 0;
    private int Surprise = 0;
    private int Sad = 0;
    private int Disguse = 0;
    private int Fear = 0;
    private int Neutral = 0;
    private Calendar calendar = Calendar.getInstance();

    private Context mContext;
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;

    //权限请求
    private RxPermissions rxPermissions;

    //是否拥有权限
    private boolean hasPermissions = false;

    //底部弹窗
    private BottomSheetDialog bottomSheetDialog;
    //弹窗视图
    private View bottomView;

    //存储拍完照后的图片
    private File outputImagePath;

    //图片路径
    private String chooseImagePath;
    //存储拍完照后的图片的上一级
    private String fatherImagePath;
    //启动相机标识
    public static final int TAKE_PHOTO = 1;
    //启动相册标识
    public static final int SELECT_PHOTO = 2;

    //图片控件
    private ShapeableImageView ivHead;
    //Base64
    private String base64Pic;
    //拍照和相册获取图片的Bitmap
    private Bitmap orc_bitmap;
    private Uri imageUri;

    private SharedPreferences preferences;

    //Glide请求图片选项配置
    private RequestOptions requestOptions = RequestOptions.circleCropTransform()
            .diskCacheStrategy(DiskCacheStrategy.NONE)//不做磁盘缓存
            .skipMemoryCache(true);//不做内存缓存

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_sqlite);
        title = findViewById(R.id.title);
        text = findViewById(R.id.text);
        date = findViewById(R.id.date);
        btn1 = findViewById(R.id.face_recognition);
        btn2 = findViewById(R.id.voice_recognition);
        ivHead = findViewById(R.id.choose_view);
        findViewById(R.id.save_diary).setOnClickListener(this);
        findViewById(R.id.date).setOnClickListener(this);
        findViewById(R.id.back_write).setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        // 先启动 Python 解释器
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }




        //检查版本
        checkVersion();
    }


    @Override
    protected void onResume() {
        super.onResume();
        xuhao = getIntent().getIntExtra("xuhao", -1);
        mHelper = UserDBHelper.getInstance(this); // 获取账单数据库的帮助器对象
        if (xuhao != -1) { // 序号有值，就展示数据库里的账单详情
            List<UserInfo> bill_list = (List<UserInfo>) mHelper.queryById(xuhao);
            if (bill_list.size() > 0) { // 已存在该账单
                UserInfo bill = bill_list.get(0); // 获取账单信息
                Date date = DateUtil.formatString(bill.date);
                calendar.set(Calendar.YEAR, date.getYear() + 1900);
                calendar.set(Calendar.MONTH, date.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, date.getDate());
                title.setText(bill.title);
                text.setText(bill.text);
            }
        }
        date.setText(DateUtil.getDate(calendar)); // 设置账单的发生时间

    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        //handleMessage方法运行在主线程，处理子线程发送回来的数据。
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                MessageBody data = (MessageBody) msg.obj;
                //Toast.makeText(data.parent, data.response,Toast.LENGTH_SHORT).show();
                JSONObject json = null;
                JSONObject json_text = null;
                try {
                    json = new JSONObject(data.response);
                    json_text = new JSONObject(data.text_response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int maxScore = 0;
                double scores = 0;
                try {
                    assert json != null;
                    maxScore = json.getInt("value");
                    assert json_text != null;
                    scores = json_text.getDouble("value");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                UserInfo info = data.info;
                String str = "已保存";
                switch (maxScore) {
                    case 0:
                        info.Anger = ++Anger;
                        str = "心情：生气";
                        break;
                    case 1:
                        info.Disgust = ++Disguse;
                        str = "心情：恶心";
                        break;
                    case 2:
                        info.Fear = ++Fear;
                        str = "心情：害怕";
                        break;
                    case 3:
                        info.Happy = ++Happy;
                        str = "心情：高兴";
                        break;
                    case 4:
                        info.Neutral = ++Neutral;
                        str = "心情：平静";
                        break;
                    case 5:
                        info.Sad = ++Sad;
                        str = "心情：悲伤";
                        break;
                    case 6:
                        info.Surprise = ++Surprise;
                        str = "心情：惊喜";
                        break;
                    default:
                        str = "未知类别";
                }
                info.score=scores;
                mHelper.save(info); // 把账单信息保存到数据库
                Toast.makeText(data.parent, str, Toast.LENGTH_SHORT).show();
                data.parent.finish();
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_diary) {
            String title_string = title.getText().toString();
            String text_string = text.getText().toString();
            LoginActivity LA = new LoginActivity();

            preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
            String serverId = preferences.getString("serverId", "192.168.56.10");


            //String serverId="192.168.43.2";
            AllDiary = title_string + text_string;

            Log.d("SQLiteWriteActivity", "onClick: " + title_string);
            if (TextUtils.isEmpty(title_string)) {
                Toast toast = Toast.makeText(this, "请先填写题目", Toast.LENGTH_SHORT);
                toast.show();
                return;
            } else if (TextUtils.isEmpty(text_string)) {
                Toast toast_string = Toast.makeText(this, "请先输入内容", Toast.LENGTH_SHORT);
                toast_string.show();
                return;
            }
            // 以下声明一个用户信息对象，并填写它的各字段值
            UserInfo info = new UserInfo();
            //Toast.makeText(this, "保存",Toast.LENGTH_SHORT).show();
            info.title = title_string;
            info.text = text_string;
            info.xuhao = xuhao;
            info.date = date.getText().toString();
            info.month = 100 * calendar.get(Calendar.YEAR) + (calendar.get(Calendar.MONTH) + 1);

            Toast.makeText(this, "请求文字服务器中...", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "请求图片服务器中...", Toast.LENGTH_SHORT).show();
            new Thread(() -> {
                MessageBody mb = new MessageBody();
                try {
                    InputStream is = new FileInputStream(chooseImagePath);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
                    {
                        byte[] b = new byte[1000];
                        int n;
                        while ((n = is.read(b)) != -1) {
                            bos.write(b, 0, n);
                        }
                        is.close();
                    }
                    byte[] data = Base64.encode(bos.toByteArray(), Base64.DEFAULT);
                    bos.close();
                    OkHttpClient client = new OkHttpClient.Builder()
                            .retryOnConnectionFailure(true)
                            .build();
                    RequestBody body = RequestBody.create(data, MediaType.parse("text/plain; charset=utf-8"));
                    Request request = new Request.Builder()
                            .url("http://" + serverId + "/backend/executor/facial_classification")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    mb.response = response.body().string();
                    //Toast.makeText(this, mb.response, Toast.LENGTH_SHORT).show();

                    byte[] text_data = Base64.encode(AllDiary.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
                    OkHttpClient text_client = new OkHttpClient.Builder()
                            .retryOnConnectionFailure(true)
                            .build();
                    RequestBody text_body = RequestBody.create(text_data, MediaType.parse("text/plain; charset=utf-8"));
                    Request text_request = new Request.Builder()
                            .url("http://" + serverId + "/backend/executor/text_rating")
                            .post(text_body)
                            .build();
                    Response text_response = text_client.newCall(text_request).execute();
                    mb.text_response = text_response.body().string();
                    //Toast.makeText(this, score, Toast.LENGTH_SHORT).show();

                } catch (IOException | RuntimeException e) {
                    mb.response = "{\"value\":-1}";
                    System.out.println(e.toString());
                    //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();

                }

                mb.info = info;
                mb.parent = this;
                // Blocked waiting here

                //Toast.makeText(this, mb.text_response, Toast.LENGTH_SHORT).show();

                Message message = new Message();
                message.what = 0;
                message.obj = mb;
                handler.sendMessage(message);

            }).start();
        }
        if (v.getId() == R.id.date) {
            DatePickerDialog dialog = new DatePickerDialog(this, this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        }
        if (v.getId() == R.id.back_write) {
            finish();
        }
        //图片识别
        if (v.getId() == R.id.face_recognition) {
            changeAvatar(v);
        }
        //语音识别
        if (v.getId() == R.id.voice_recognition) {
            Intent intent = new Intent(SQLiteWriteActivity.this, RecordActivity.class);
            startActivity(intent);
        }


    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        date.setText(DateUtil.getDate(calendar));
    }

    /**
     * 检查版本
     */
    @SuppressLint("CheckResult")
    private void checkVersion() {
        //Android6.0及以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //如果你是在Fragment中，则把this换成getActivity()
            rxPermissions = new RxPermissions(this);
            //权限请求
            rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(granted -> {
                        if (granted) {//申请成功
                            showMsg("已获取权限");
                            hasPermissions = true;
                        } else {//申请失败
                            showMsg("权限未开启");
                            hasPermissions = false;
                        }
                    });
        } else {
            //Android6.0以下
            showMsg("无需请求动态权限");
        }
    }

    /**
     * 更换头像
     *
     * @param view
     */
    @SuppressLint("InflateParams")
    public void changeAvatar(View view) {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomView = getLayoutInflater().inflate(R.layout.dialog_bottom, null);
        bottomSheetDialog.setContentView(bottomView);
        bottomSheetDialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet).setBackgroundColor(Color.TRANSPARENT);
        TextView tvTakePictures = bottomView.findViewById(R.id.tv_take_pictures);
        TextView tvOpenAlbum = bottomView.findViewById(R.id.tv_open_album);
        TextView tvCancel = bottomView.findViewById(R.id.tv_cancel);

        //拍照
        tvTakePictures.setOnClickListener(v -> {
            takePhoto();
            showMsg("拍照");
            bottomSheetDialog.cancel();
        });
        //打开相册
        tvOpenAlbum.setOnClickListener(v -> {
            openAlbum();
            showMsg("打开相册");
            bottomSheetDialog.cancel();
        });
        //取消
        tvCancel.setOnClickListener(v -> {
            bottomSheetDialog.cancel();
        });
        //底部弹窗显示
        bottomSheetDialog.show();
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        openCamera();
    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        if (!hasPermissions) {
            showMsg("未获取到权限");
            checkVersion();
            return;
        }
        startActivityForResult(CameraUtils.getSelectPhotoIntent(), SELECT_PHOTO);
    }

    /**
     * 返回到Activity
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //拍照后返回
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //显示图片
//                    displayImage(outputImagePath.getAbsolutePath());
                    chooseImagePath = outputImagePath.getAbsolutePath();
                    fatherImagePath = outputImagePath.getParentFile().getAbsolutePath();
                    displayImage(chooseImagePath);
                }
                break;
            //打开相册后返回
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    String imagePath = null;
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        //4.4及以上系统使用这个方法处理图片
                        imagePath = CameraUtils.getImageOnKitKatPath(data, this);
                    } else {
                        imagePath = CameraUtils.getImageBeforeKitKatPath(data, this);
                    }
                    //显示图片
//                    displayImage(imagePath);
                    chooseImagePath = imagePath;
                    displayImage(chooseImagePath);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 通过图片路径显示图片
     */
    private void displayImage(String imagePath) {
        if (!TextUtils.isEmpty(imagePath)) {

            //放入缓存
//            SPUtils.putString("imageUrl",imagePath,this);

            //显示图片
            Glide.with(this).load(imagePath).apply(requestOptions).into(ivHead);

            //压缩图片
            orc_bitmap = CameraUtils.compression(BitmapFactory.decodeFile(imagePath));
            //转Base64
            base64Pic = BitmapUtils.bitmapToBase64(orc_bitmap);

        } else {
            showMsg("图片获取失败");
        }
    }


    /**
     * Toast提示
     *
     * @param msg
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //确保有相机来处理Intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            outputImagePath = saveFileName();
            fatherImagePath = saveFileName().getParent();
            if (outputImagePath != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                    imageUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.myapp5.fileprovider", outputImagePath);
                } else {
                    imageUri = getDestinationUri();
                }
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, TAKE_PHOTO);
            }
        }
    }

    private Uri getDestinationUri() {
        String fileName = String.format("face_%s.npg", System.currentTimeMillis());
        File cropFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        return Uri.fromFile(cropFile);
    }

    //当前路径，拍照回调后需要使用
    private String currentPath = null;

    /**
     * 保存照片路径
     *
     * @return
     */
    private File saveFileName() {
        File newFolder = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        String name = format.format(date) + ".npg";

        File ji = null;
        try {
            ji = new File(newFolder + "/" + name);
            ji.createNewFile();
            currentPath = ji.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ji;
    }

    public String getChooseImagePath() {
        return chooseImagePath;
    }

    public String getFatherImagePath() {
        return fatherImagePath;
    }

    public String getAllDiary() {
        return AllDiary;
    }

    public void saveArray(List<Float> list) {
        preferences = getSharedPreferences("score", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("score_size", list.size());

        for (int i = 0; i < list.size(); i++) {
            editor.remove("score_" + i);
            editor.putFloat("score_" + i, list.get(i));
            //editor.putString("score_time_"+i,);
        }
        editor.apply();
    }

    public void loadArray(List<Float> list) {

        preferences = getSharedPreferences("score", Context.MODE_PRIVATE);
        list.clear();
        int size = preferences.getInt("score_size", 0);
        for (int i = 0; i < size; i++) {
            list.add(preferences.getFloat("score_" + i, 0));

        }
    }


}



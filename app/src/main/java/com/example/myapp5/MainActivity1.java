package com.example.myapp5;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.pytorch.IValue;

import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;;
import org.pytorch.MemoryFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity1 extends AppCompatActivity {

    private SQLiteWriteActivity ImagePath;
    private int maxScoreIdx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Bitmap bitmap = null;
        Module module = null;
        ImagePath = new SQLiteWriteActivity();
        String filename = ImagePath.getChooseImagePath();
        float[] scores;
        float[] sum = new float[0];
        try {
            bitmap = BitmapFactory.decodeStream(getAssets().open(filename));
            module = Module.load(assetFilePath(this, "PrivateTest_model_new.pt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // showing image on UI
        ImageView imageView = findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);


        try {

            //调用剪裁函数存储25张图片到filename
            //预处理开始

            filename = ImagePath.getFatherImagePath();
            Bitmap bi = resize(bitmap);
            //int[][] rgbArray=convertImageToArray(bi);
            int width = bi.getWidth();
            int height = bi.getHeight();
            // 设置想要的大小
            int newWidth = 44;
            int newHeight = 44;
            // 计算缩放比例
            //float scaleWidth = ((float) newWidth) / width;
            //float scaleHeight = ((float) newHeight) / height;
            // 取得想要缩放的matrix参数
            //Matrix matrix = new Matrix();
            //matrix.postScale(scaleWidth, scaleHeight);
            int k=0;
            for (int i = 0; i < width - 1; i = i + 44) {
                for (int j = 0; j < height - 1; j = j + 44) {
                    // System.out.println(i + " " + j);
                    // 得到新的图片
                    Bitmap newbi = Bitmap.createBitmap(bi, i, j, newWidth, newHeight);
                    saveBitmap(newbi,filename + k++ + ".jpg");
                }

            }
            //预处理结束


            for(int m = 0; m < 25; m = m+1) {
                filename = filename + m+ ".jpg";
                bitmap = BitmapFactory.decodeStream(getAssets().open(filename));
                bitmap = Bitmap.createScaledBitmap(bitmap, 44, 44, true);

                // preparing input tensor
                final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);

                // running the model
                final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
                System.out.print(outputTensor + "\n");
                // getting tensor content as java array of floats
                scores = outputTensor.getDataAsFloatArray();//7种表情的得分
                for(int cont=0;cont<7;cont++) {
                    sum[cont] = sum[cont]+scores[cont];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

/*
        // preparing input tensor
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);

        // running the model
        final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
        System.out.print(outputTensor + "\n");
        // getting tensor content as java array of floats
        final float[] scores = outputTensor.getDataAsFloatArray();//7种表情的得分

 */
        for (int i = 0; i < sum.length; i++) {
            System.out.print(i + ":" + sum[i] + " \n");
        }
        // searching for the index with maximum score
        float maxScore = -Float.MAX_VALUE;
        maxScoreIdx = -1;
        for (int i = 0; i < sum.length; i++) {
            if (sum[i] > maxScore) {
                maxScore = sum[i];
                maxScoreIdx = i;
            }
        }
        System.out.println(maxScoreIdx);
        String className = ImageNetClasses.IMAGENET_CLASSES[maxScoreIdx];
        // showing className on UI
        TextView textView = findViewById(R.id.text);
        textView.setText(className);

    }

    public int runImageanalysis() {
        return maxScoreIdx;
    }

    public static int[][] convertImageToArray(Bitmap bm) {
        // 获取图片宽度和高度
        int width = bm.getWidth();
        int height = bm.getHeight();

        int[][] rgbArray = new int[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                rgbArray[i][j] = bm.getPixel(i, j);
                ;

                //System.out.print(rgbArray[i][j]);
                //Thread.sleep( 1000 );
            }

        return rgbArray;
    }

    public static Bitmap resize(Bitmap bm) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 设置想要的大小
        int newWidth = 220;
        int newHeight = 220;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;    
    }
    private void saveBitmap(Bitmap bitmap,String filepath) {

        try {
            File file = new File(filepath);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     *
     * @return absolute file path
     */
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
}

package com.example.myapp5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.util.Synthetic;
import com.example.myapp5.widge.PieChartManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class MoodActivity extends AppCompatActivity {
    private int Anger=0;
    private int Happy=0;
    private int Surprise=0;
    private int Sad=0;
    private int Disgust=0;
    private int Fear=0;
    private int Neutral=0;
    private PieChart pieChart;
    private LineChart lineChart;
    private XAxis xAxis;                //X轴
    private YAxis leftYAxis;            //左侧Y轴
    private YAxis rightYaxis;           //右侧Y轴
    private Legend legend;              //图例
    private LimitLine limitLine;        //限制线
    //  private MyMarkerView markerView;    //标记视图 即点击xy轴交点时弹出展示信息的View 需自定义
    private SharedPreferences preferences;
    private UserDBHelper mHelper;
    public void getScore(){
        mHelper = UserDBHelper.getInstance(this);
        Anger=mHelper.sumScore("Anger");
        Happy=mHelper.sumScore("Happy");
        Surprise=mHelper.sumScore("Surprise");
        Sad=mHelper.sumScore("Sad");
        Neutral=mHelper.sumScore("Neutral");
        Fear=mHelper.sumScore("Fear");
        Disgust=mHelper.sumScore("Disgust");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);
        //状态栏背景透明
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        //饼状图
        pieChart = (PieChart) findViewById(R.id.pieChar);
        showhodlePieChart();
        //折线图
        lineChart = findViewById(R.id.lineChart);
        initChart(lineChart);
        showLineChart("心情值", Color.CYAN);
        Drawable drawable = getResources().getDrawable(R.drawable.fade_blue);
        setChartFillDrawable(drawable);
//        LineChartBean lineChartBean = LocalJsonAnalyzeUtil.JsonToObject(this,
//                "chart.json", LineChartBean.class);
//        List<IncomeBean> list = lineChartBean.getGRID0().getResult().getClientAccumulativeRate();
//        showLineChart(list, "我的收益", Color.CYAN);
    }

//    /**
//     * 我的收益
//     */
//
//    public class IncomeBean {
//        /**
//         * tradeDate : 20180502
//         * value : 0.03676598
//         */
//        private String tradeDate;
//        private double value;
//    }
//
//    /**
//     * 沪深创指数
//     */
//    public class CompositeIndexBean {
//        /**
//         * rate : -0.00034196
//         * tradeDate : 20180502
//         */
//        private String rate;
//        private String tradeDate;
//    }

    /**
     * 初始化图表
     */
    private void initChart(LineChart lineChart) {
        /***图表设置***/
        //是否展示网格线
        lineChart.setDrawGridBackground(false);
        //是否显示边界
        lineChart.setDrawBorders(false);
        //是否可以拖动
        lineChart.setDragEnabled(false);
        //是否有触摸事件
        lineChart.setTouchEnabled(true);
        //设置XY轴动画效果
        lineChart.animateY(2500);
        lineChart.animateX(1500);

        /***XY轴的设置***/
        xAxis = lineChart.getXAxis();
        leftYAxis = lineChart.getAxisLeft();
        rightYaxis = lineChart.getAxisRight();
        //X轴设置显示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        //保证Y轴从0开始，不然会上移一点
        leftYAxis.setAxisMinimum(0f);
        rightYaxis.setAxisMinimum(0f);
        //去掉网格线
        xAxis.setDrawGridLines(false);
        rightYaxis.setDrawGridLines(false);
        leftYAxis.setDrawGridLines(true);
        //leftYAxis.enableGridDashedLine(10f, 10f, 0f);
        leftYAxis.enableGridDashedLine(10f, 10f, 0f);
        //目标效果图没有右侧Y轴，所以去掉右侧Y轴
        rightYaxis.setEnabled(false);
        /***折线图例 标签 设置***/
        legend = lineChart.getLegend();
        //设置显示类型，LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看LegendForm 即可
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        //显示位置 左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);
    }

    /**
     * 曲线初始化设置 一个LineDataSet 代表一条曲线
     *
     * @param lineDataSet 线条
     * @param color       线条颜色
     * @param mode
     */
    private void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode) {
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        //设置曲线值的圆点是实心还是空心
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(10f);
        //不显示点
        lineDataSet.setDrawCircles(false);
        //不显示值
        lineDataSet.setDrawValues(false);
        //设置折线图填充
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormSize(15.f);
        if (mode == null) {
            //设置曲线展示为圆滑曲线（如果不设置则默认折线）
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            lineDataSet.setMode(mode);
        }
    }


    public void showLineChart( String name, int color) {

        ArrayList<Float> sites=new ArrayList<Float>();
        loadArray(sites);

        List<Entry>list=new ArrayList<>();
        for(int i=0;i<sites.size();i++){
            list.add(new Entry(i,sites.get(i)));
        }
         LineDataSet lineDataSet = new LineDataSet(list, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.LINEAR);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
    }

    /**
     * 设置线条填充背景颜色
     *
     * @param drawable
     */
    public void setChartFillDrawable(Drawable drawable) {
        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            LineDataSet lineDataSet = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            //避免在 initLineDataSet()方法中 设置了 lineDataSet.setDrawFilled(false); 而无法实现效果
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillDrawable(drawable);
            lineChart.invalidate();
        }
    }

    private void showhodlePieChart() {
        // 设置每份所占数量
        getScore();
        List<PieEntry> yvals = new ArrayList<>();
        yvals.add(new PieEntry(Happy, "开心"));
        yvals.add(new PieEntry(Sad, "难过"));
        yvals.add(new PieEntry(Neutral, "平静"));
        yvals.add(new PieEntry(Anger, "生气"));
        yvals.add(new PieEntry(Fear, "恐惧"));
        yvals.add(new PieEntry(Disgust, "恶心"));
        yvals.add(new PieEntry(Surprise, "惊讶"));
        //设置每份的颜色
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#33ffff"));
        colors.add(Color.parseColor("#33ccff"));
        colors.add(Color.parseColor("#3399ff"));
        colors.add(Color.parseColor("#3366ff"));
        colors.add(Color.parseColor("#3333ff"));
        colors.add(Color.parseColor("#3300cc"));
        colors.add(Color.parseColor("#330066"));

        PieChartManager pieChartManager=new PieChartManager(pieChart);
        pieChartManager.showSolidPieChart(yvals,colors);
    }

    public void loadArray(List<Float> list) {

        preferences = getSharedPreferences("score",Context.MODE_PRIVATE);
        list.clear();
        int size = preferences.getInt("score_size", 0);
        for(int i=0;i<size;i++) {
            list.add(preferences.getFloat("score_" + i,0));

        }
    }


}


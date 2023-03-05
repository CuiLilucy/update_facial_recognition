package com.example.myapp5;

import android.content.Context;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TextModule {
    // 单例 避免重复加载
    private static final TextModule instance = new TextModule();
    private TextModule() {}

    public static TextModule getInstance() {
        return instance;
    }

    Python pyInstance = null;
    Module mModule = null;

    PyObject textProcess;
    PyObject tokenPassage;
    PyObject tokenSentence;
    PyObject w2i;

    final long[] TENSOR_SHAPE = {1, 30};

    public void doInit(String modelPath) {
        pyInstance = Python.getInstance();
        pyInstance.getModule("emo_dict").get("ed_init").call();

        textProcess = pyInstance.getModule("text_process");
        tokenPassage = pyInstance.getModule("ptokenize").get("tokenize_passage");
        tokenSentence = pyInstance.getModule("ptokenize").get("tokenize_sentence");
        w2i = textProcess.get("w2i");

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Loading Module...");
                try {
                    mModule = LiteModuleLoader.load(modelPath);
                } catch (Exception e) {
                    System.err.println("Load module failed.");
                    e.printStackTrace();
                }
                System.out.println("Load Complete");
            }
        }).start();
        System.out.println("Init Finished");
    }

    public float getScore(String passage) {
        // Python Instances
        List<PyObject> tokenizedPassageWithLength = tokenPassage.call(passage).asList();
        List<PyObject> sentences = tokenizedPassageWithLength.get(0).asList();
        List<PyObject> lengths = tokenizedPassageWithLength.get(1).asList();
        List<PyObject> indexList = new ArrayList<>();
        List<Long> lengthList = new ArrayList<>();
        List<Tensor> inputTensors = new ArrayList<>();

        for (PyObject sentence : sentences) {
            indexList.add(w2i.call(tokenSentence.call(sentence)));
        }

        for (PyObject index : indexList) { // 把索引列表转成张量
            short i = 0;
            long[] indexValue = new long[30];
            List<PyObject> indexV = index.asList();
            for (PyObject _index : indexV) {
                indexValue[i++] = _index.toLong();
            }
            inputTensors.add(Tensor.fromBlob(indexValue, TENSOR_SHAPE));
        }

        int lenSum = 0;
        for (PyObject length : lengths) {
            long toLong = length.toLong();
            lengthList.add(toLong);
            lenSum += toLong;
        }

        ArrayList<Float> scorePerSentence = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            boolean status;
            float difference;

            float dict_score = Objects.requireNonNull(textProcess.get("in_text")).call(passage).toFloat();
            Tensor outputTensor = mModule.forward(IValue.from(inputTensors.get(i))).toTensor();

            System.out.println("[" +
                    outputTensor.getDataAsFloatArray()[0] + ", " +
                    outputTensor.getDataAsFloatArray()[1] + "]"); // debug

            status = !(outputTensor.getDataAsFloatArray()[0] > outputTensor.getDataAsFloatArray()[1]);
            difference = Math.abs(outputTensor.getDataAsFloatArray()[0] - outputTensor.getDataAsFloatArray()[1]);

            float outScore = convertScore(dict_score, status, difference);
            scorePerSentence.add(outScore);
            System.out.println("Dict Score: " + dict_score + ", Net Status: " + status + ", Difference: " + difference + ", Out: " + outScore); // debug


        }

        float result = 0.0f;
        for (int i = 0; i < scorePerSentence.size(); i++) {
            result += scorePerSentence.get(i) * ((float)lengthList.get(i) / (float)lenSum);
        }

        return overScore(result);
    }

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

    private static float convertScore(float rawScore, boolean status, float difference) { // 计算单句得分
        int base = (status) ? 1 : -1;
        float resultScore = rawScore;
        if (isConsistent(rawScore, status)) {
            if (difference > 1) {
                if (rawScore == 50) {
                    resultScore += base * 50 * (difference - 1) * 0.45;
                } else {
                    resultScore += base * Math.abs(50 - rawScore) * (difference - 1) * 0.35;
                }
            }
        } else {
            if (difference > 0.8) {
                resultScore += base * Math.abs(50 - rawScore) * difference * 1.285f;
            } else {
                resultScore += base * Math.abs(50 - rawScore) * difference;
            }
        }
        return resultScore;
    }

    private static boolean isConsistent(float rawScore, boolean status) {
        return rawScore >= 50 && status || rawScore < 50 && !status;
    }

    private static float sigmoid(float x) {
        return 1 / (1 + (float)Math.pow(Math.E, -x));
    }

    private static float overScore(float x) {
        if (x == 50) return 50;

        boolean isPositive = x > 50;
        if (!isPositive)
            x = 100 - x;
        x = (float)Math.log(x - 50) / (float)Math.log(6);
        x = 100 * sigmoid(x);

        System.out.println(x);
        if (!isPositive) {
            return 100 - x;
        } else return x;
    }
}

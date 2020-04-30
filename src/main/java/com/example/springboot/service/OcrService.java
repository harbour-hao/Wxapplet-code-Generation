package com.example.springboot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.springboot.util.Base64Util;
import com.example.springboot.util.FileUtil;
import com.example.springboot.util.HttpUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_CCOMP;
import static org.opencv.imgproc.Imgproc.RETR_TREE;

@Service
public class OcrService {
    public static final String APP_ID = "xxxxx";
    public static final String API_KEY = "xxxxx";
    public static final String SECRET_KEY = "xxxxxxx";
    private JSONArray data;

    //获取全图片文字位置
    public void doOcr(String path){
        this.data=advancedGeneral(path);
    }

    public  List<Rect> getFontRect(){
        if(data==null) return null;
        List<Rect> list=new ArrayList<>();
        for (int i=0;i<data.size();i++){
            JSONObject word=data.getJSONObject(i);
            JSONObject location=word.getJSONObject("location");
            //int width=location.getInt("width");
            Rect rect=new Rect(location.getInteger("left"),location.getInteger("top"),location.getInteger("width"),location.getInteger("height"));
            list.add(rect);
        }
        return list;
    }

    public  List<String> getFontContent(){
        List<String> list=new ArrayList<>();
        for (int i=0;i<data.size();i++){
            JSONObject word=data.getJSONObject(i);
            list.add(word.getString("words"));
        }
        return list;
    }



    //获取图片文字位置
    public static JSONArray advancedGeneral(String path) {
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate";
        try {
            // 本地文件路径
            byte[] imgData = FileUtil.readFileByBytes(path);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "image=" + imgParam;
            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = getAuth(API_KEY,SECRET_KEY);
            String result = HttpUtil.post(url, accessToken, param);
            //String result="{\"log_id\": 7496362660759576959, \"words_result_num\": 7, \"words_result\": [{\"location\": {\"width\": 189, \"top\": 36, \"left\": 43, \"height\": 46}, \"words\": \"报名信息\"}, {\"location\": {\"width\": 562, \"top\": 183, \"left\": 90, \"height\": 41}, \"words\": \"第七期国际资产架构师高级研修班\"}, {\"location\": {\"width\": 530, \"top\": 260, \"left\": 91, \"height\": 30}, \"words\": \" 7th Advanced Workshop for International\"}, {\"location\": {\"width\": 204, \"top\": 294, \"left\": 90, \"height\": 31}, \"words\": \" Asset Architects\"}, {\"location\": {\"width\": 189, \"top\": 422, \"left\": 144, \"height\": 38}, \"words\": \"深圳市南山区\"}, {\"location\": {\"width\": 260, \"top\": 473, \"left\": 143, \"height\": 35}, \"words\": \"07-21周日09:00\"}, {\"location\": {\"width\": 111, \"top\": 526, \"left\": 100, \"height\": 30}, \"words\": \"￥50\"}]}\n" ;
            JSONObject jsonObject= JSON.parseObject(result);
            return jsonObject.getJSONArray("words_result");
//            JSONObject word=jsonData.getJSONObject(0);
//            String content=word.getString("words");
//            JSONObject location=word.getJSONObject("location");
//            int width=location.getInt("width");
//            System.out.println(result);
//            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAuth(String ak, String sk) {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ak
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 获取所有响应头字段
            java.util.Map<String, java.util.List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = "";
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            /**
             * 返回结果示例
             */
            System.err.println("result:" + result);
            JSONObject jsonObject = JSON.parseObject(result);
            String access_token = jsonObject.getString("access_token");
            return access_token;
        } catch (Exception e) {
            System.err.printf("获取token失败！");
            e.printStackTrace(System.err);
        }
        return null;
    }

    public BufferedImage Mat2BufImg (Mat matrix, String fileExtension) {
        // convert the matrix into a matrix of bytes appropriate for
        // this file extension
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(fileExtension, matrix, mob);
        // convert the "matrix of bytes" into a byte array

        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
    }

}

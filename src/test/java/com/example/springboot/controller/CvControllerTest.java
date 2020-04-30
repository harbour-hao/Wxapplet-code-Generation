package com.example.springboot.controller;

import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.net.URL;

import static org.junit.Assert.*;

public class CvControllerTest {

    @Test
    public void scan() {
        //URL url = ClassLoader.getSystemResource("lib/opencv_java410.dll");
        System.load("D:\\java\\jar包补充\\opencv_java410.dll");
        String path="D://IDEA//projects//springboot//src//main//resources//static//image//fontArea.jpg";
        Mat src = Imgcodecs.imread(path);
        Mat img = src.clone();
        // 彩色转灰度
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
    }
}
package com.example.springboot.Model;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

public class Border {
    private int epsilon=3;//拟合精度
    private int kind=2;//0为普通边框，1为文字，2为图片,3为纯色实体，4为文字外接框
    private  List<MatOfPoint2f> point2fs=new ArrayList<>();//拟合后的数据
    private List<Integer> No=new ArrayList<>();//边界编号
    private int width;//轮廓的宽度
    private int radius;//数据个数
    private Rect rect;//最小矩形形
    private String content;

    public int getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(int epsilon) {
        this.epsilon = epsilon;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public List<MatOfPoint2f> getPoint2fs() {
        return point2fs;
    }

    public void setPoint2fs(List<MatOfPoint2f> point2fs) {
        this.point2fs = point2fs;
    }

    public List<Integer> getNo() {
        return No;
    }

    public void setNo(List<Integer> no) {
        No = no;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

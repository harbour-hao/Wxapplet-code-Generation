package com.example.springboot.Model;

import java.util.ArrayList;
import java.util.List;

public class Section {
    public List<TreeNode> border=new ArrayList<>();
    public double start;//动态变化初始点
    public double end;
    public int direct;
    public double x;//当前组件的初始点
    public double y;
    public double height;
    public double width;
}

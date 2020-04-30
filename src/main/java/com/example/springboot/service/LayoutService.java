package com.example.springboot.service;

import com.example.springboot.Model.Border;
import com.example.springboot.Model.Direction;
import com.example.springboot.Model.Section;
import com.example.springboot.Model.TreeNode;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LayoutService {
    public void Optimize(Section Sec, Section section){
        section.x=section.start;
        section.y=Sec.y;
        if(section.direct==0){
            section.x=section.start;
            section.y=Sec.y;
            section.width=section.end-section.start;
            section.height=Sec.height;
        }else {
            section.x=Sec.x;
            section.y=section.start;
            section.width=Sec.width;
            section.height=section.end-section.start;
        }
    }
    //将Section的内容继承父组件
    public void Optimize(TreeNode node, Section section){
        if(node.val.getRect()==null){
            node.val.setRect( Imgproc.minAreaRect(node.val.getPoint2fs().get(0)).boundingRect());
        }
        if(section.direct==0){
            section.x=section.start;
            section.y=node.val.getRect().y;
            section.height=node.val.getRect().height;
            section.width=section.end-section.start;
        }else {
            section.x=node.val.getRect().x;
            section.y=section.start;
            section.width=node.val.getRect().width;
            section.height=section.end-section.start;
        }
    }
    //通过便利纵向和横向，找最远边分割
    public Direction Seperate(List<TreeNode> list){
        if(list==null||list.size()==0)return null;
        List<Section> row=getLayout(list,0);
        List<Section> col=getLayout(list,2);
        Direction direction=new Direction();
        if(row.size()>=col.size()){
            direction.direction=0;
            direction.list=row;
        }else {
            direction.direction=1;
            //反转成从上到下遍历
            //Collections.reverse(col);
            direction.list=col;
        }
        return direction;
    }
    //0为获取横向的，2为获取纵向的,获取分割部分放到List当中，重叠部分将融合在一起（按坐标的最小值从小到大排序）
    //若list非空，必定返回非空的list<section>
    public List<Section> getLayout (List<TreeNode> list,int status){
        if (list==null||list.size()==0)return null;
        //先根据每一个border的横纵向的最小值排序，防止复杂重叠情况发生(横向纵向排序从小到大)
        list.sort((o1, o2) -> {
            if (getRxtremun(o1.val, status) > getRxtremun(o2.val, status)) {
                return 1;
            } else if (getRxtremun(o1.val, status) < getRxtremun(o2.val, status)) {
                return -1;
            }
            return 0;
        });
        //记录上一条border的极值
        double minOverlap=0;
        double maxOverlap=0;
        //row返回横向的区域分割
        List<Section> row=new ArrayList<>();
        for (int i=0;i<list.size();i++){
            // System.out.println(list.get(i).val.point2fs.get(0).dump());//左下角为原点
            Border border=list.get(i).val;
            //max(A.start,B.start)<=min(A.end,B,end)，即可判断A，B重叠
            if(Math.max(getRxtremun(border,status),minOverlap)<=Math.min(getRxtremun(border,1+status),maxOverlap)&&i!=0){
                //将row的最新的那个扩大
                row.get(row.size()-1).border.add(list.get(i));
                //row.get(row.size()-1).start=Math.min(getRxtremun(point2f,status),minOverlap);因为排好序了，start值不用再计算了
                row.get(row.size()-1).end=Math.max(getRxtremun(border,1+status),maxOverlap);
                //minOverlap=row.get(row.size()-1).start;
                maxOverlap=row.get(row.size()-1).end;
            }else {
                //这里包括了初始化的过程
                Section section=new Section();
                section.border.add(list.get(i));
                section.start=getRxtremun(border,status);
                section.end=getRxtremun(border,1+status);
                section.direct=status==0?0:1;
                minOverlap=section.start;
                maxOverlap=section.end;
                row.add(section);
            }
            //更新Section的极值，取当前border或者之前的section的边界值
            maxOverlap=Math.max(getRxtremun(border,status+1),maxOverlap);
            //minOverlap=Math.min(getRxtremun(point2f,status),minOverlap);
        }
        return row;
    }
    //获得边界的极值
    public double getRxtremun(MatOfPoint2f point2f, int status) {
        ArrayList<Double> row = new ArrayList<>();
        ArrayList<Double> col = new ArrayList<>();
        Point[] points = point2f.toArray();
        for (int i = 0; i < points.length; i++) {
            row.add(points[i].x);
            col.add(points[i].y);
        }
        Collections.sort(row);Collections.sort(col);
//        System.out.println(point2f.dump());
//        System.out.println(row);
//        System.out.println(col);
        switch (status){
            case 0:
                return row.get(0);//获取x轴最小
            case 1:
                return row.get(row.size()-1);//获取x轴最大
            case 2:
                return col.get(0);//获取y轴最小
            case 3:
                return col.get(col.size()-1);
        }
        return 0;
    }

    //获得边界的极值
    private double getRxtremun(Border border, int status) {
        if(border.getRect()==null){
            border.setRect(Imgproc.minAreaRect(border.getPoint2fs().get(0)).boundingRect());
        }
        switch (status){
            case 0:
                return border.getRect().x;//获取x轴最小
            case 1:
                return border.getRect().x+border.getRect().width;//获取x轴最大
            case 2:
                return border.getRect().y;//获取y轴最小
            case 3:
                return border.getRect().y+border.getRect().height;
        }
        return 0;
    }
}

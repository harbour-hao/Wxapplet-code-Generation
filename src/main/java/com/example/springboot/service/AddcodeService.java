package com.example.springboot.service;

import com.example.springboot.Model.Border;
import com.example.springboot.Model.Direction;
import com.example.springboot.Model.Section;
import com.example.springboot.Model.TreeNode;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST,proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AddcodeService {
    @Value("${file.screenshot.path}")
    private String filePath;

    @Value("${project.request.path}")
    private String requestPath;

    @Value("${file.screenshot.relative}")
    private String screenshotPath;


    private    String builder="";
    private  int section=0;
    public  int border=0;
    private  int text=0;
    private  int block=0;
    private   int image=0;
    private HashMap<String,List<String>> map=new HashMap<>();
    private NumberFormat numberFormat;
    private Mat src;
    private float rate;

    public String getBuilder() {
        return builder;
    }

    public void addBuilder(String builder) {
        this.builder += builder;
    }

    public HashMap<String, List<String>> getMap() {
        return map;
    }

    public void addMap(String key,List<String> value) {
        this.map.put(key,value);
    }

    public void intialCode(Mat src){
        this.src=src;
        this.rate=(float)750.4/(float)src.width();
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后0位
        numberFormat.setMaximumFractionDigits(0);
        this.numberFormat=numberFormat;
    }


    public String addSection(Direction direction, int i, Section Section){
        Rect Rect=new Rect((int)Section.x,(int)Section.y,(int)Section.width,(int)Section.height);
        builder+="<view class=\"section"+section+"\">\n";
        addSectionStyle(direction,i,Rect,"section"+section);
        section++;
        return "section"+(section-1);
    }

    public String addSection(Direction direction,int i,TreeNode node){
        Rect Rect=node.val.getRect();
        builder+="<view class=\"section"+section+"\">\n";
        addSectionStyle(direction,i,Rect,"section"+section);
        section++;
        return "section"+(section-1);
    }

    public void endCode(String name,int direct){
        if(name.contains("border")||name.contains("section")) {
            builder += "</view>";
            if (direct == 1) {
                map.get(name).add("flex-direction: column;");
            } else if (direct == 3) {//居中的情况
                List<String> list = map.get(name);
                list.add("justify-content: center;");
                list.add("align-items: center;");
            }
        }
    }
    public void endCode(){
        builder+="</view>";
    }

    //当一个块只有一个node执行
    public String addTreeNode(Direction direction,int i,Section section){
        Rect rect=new Rect((int)section.x,(int)section.y,(int)section.width,(int)section.height);
        return addNode(direction,i,rect);
    }
    public String addTreeNode(Direction direction,int i,TreeNode head){
        return addNode(direction,i,head.val.getRect());
    }
    //Rect为父Rect
    private String addNode(Direction direction,int i,Rect Rect){
        TreeNode recentNode=direction.list.get(i).border.get(0);//取的是直接组件，不是Section
        switch (recentNode.val.getKind()) {
            case 0:
                builder += ("<view class=\"border" + border + "\">\n");
                addStyle(direction,i,Rect,"border"+border,0);
                addBorder(recentNode,"border"+border);
                border++;
                return "border"+(border-1);
            case 1:
                builder += ("<view class=\"text" + text + "\" >" + recentNode.val.getContent() + "</view>\n");
                addStyle(direction,i,Rect,"text"+text,1);
                text++;
                return "text"+(text-1);
            case 2:
                String path=savePic(src,recentNode);
                builder += ("<image class=\"image"+image+"\" src=\""+path+"\" ></image>\n");
                addStyle(direction,i,Rect,"image"+image,2);
                addImage(recentNode.val.getRect(),"image"+image);
                image++;
                return "image"+(image-1);
            case 3:
                builder += ("<view class=\"block" + block + "\"></view>\n");
                addStyle(direction,i,Rect,"block"+block,3);
                addBlock(recentNode.val.getRect(),"block"+block);
                block++;
                return "block"+(block-1);
        }
        return null;
    }

    private void addSectionStyle(Direction direction,int i,Rect Rect,String name){
        Section section=direction.list.get(i);
        Rect recentRect=new Rect((int) section.x,(int)section.y,(int)section.width,(int)section.height);
        //分割的是Section，一个Section至少有2块以上的才会执行，所以不用Singlepart
        if (i == 0) {
            Fontpart(Rect,recentRect,direction.direction,name);
        } else if (i == direction.list.size() - 1) {
            Lastpart(Rect,recentRect,direction.direction,name,direction.list.get(i-1));
        } else {
            Middlepart(Rect,recentRect,direction.direction,name,direction.list.get(i-1));
        }
        List<String> list=map.get(name);String length;
        if(direction.direction==0){
            length=numberFormat.format((float)recentRect.width/(float)Rect.width*100);
            list.add("width:"+length+"%;");
        }else {
            length=numberFormat.format((float)recentRect.height/(float)Rect.height*100);
            list.add("height:"+length+"%;");
        }
        list.add("display: flex;");
    }
    //Direction,第i个，父组件的Rect，name
    private void addStyle(Direction direction,int i,Rect Rect,String name,int type){
        Border recent=direction.list.get(i).border.get(0).val;
        if (recent.getRect() == null) {
            recent.setRect(Imgproc.minAreaRect(recent.getPoint2fs().get(0)).boundingRect());
        }
        if(direction.list.size()==1){
            Singlepart(Rect,recent.getRect(),direction.direction,name);
        }else {
            if (i == 0) {
                Fontpart(Rect,recent.getRect(),direction.direction,name);
            } else if (i == direction.list.size() - 1) {
                Lastpart(Rect,recent.getRect(),direction.direction,name,direction.list.get(i-1));
            } else {
                Middlepart(Rect,recent.getRect(),direction.direction,name,direction.list.get(i-1));
            }
        }
        String width = numberFormat.format((float)recent.getRect().width/(float)Rect.width*100);
        List<String> list=map.get(name);
        if(type!=1){
            list.add("width:"+width+"%;");
        }else {
            list.add("font-size:"+RateTran(fontSizeHandle(recent,list,width))+"rpx;");
        }
        if(direction.direction==0){
            list.add("margin-top:"+RateTran(recent.getRect().y-Rect.y)+"rpx;");
        }else {
            list.add("margin-left:"+numberFormat.format((float)(recent.getRect().x-Rect.x)/(float)Rect.width*100)+"%;");
        }

    }
    //单独的一个的组件
    private void Singlepart(Rect Rect1,Rect rect2,int direct,String name){
        List<String> list=new ArrayList<>();
        String fontDistance,backDistance;
        if(direct==0){
            fontDistance=numberFormat.format((float)(rect2.x-Rect1.x)/(float)Rect1.width*100);
            list.add("margin-left:"+fontDistance+"%;");
//            backDistance=numberFormat.format((float)(Rect1.x+Rect1.width-rect2.x-rect2.width)/(float)Rect1.width*100);
//            list.add("margin-right:"+backDistance+"%;");
        }else {
            fontDistance=RateTran(rect2.y-Rect1.y);
            list.add("margin-top:"+fontDistance+"rpx;");
//            backDistance=RateTran(Rect1.y+Rect1.height-rect2.y-rect2.height);
//            list.add("margin-bottom:"+backDistance+"rpx;");
        }
        map.put(name,list);
    }
    //作为第一个组件
    private void Fontpart(Rect Rect1,Rect rect2,int direct,String name){
        List<String> list=new ArrayList<>();
        String borderDistance;
        if(direct==0){
            borderDistance=numberFormat.format(marginHandle((float)(rect2.x-Rect1.x)/(float)Rect1.width*100));
            list.add("margin-left:"+borderDistance+"%;");
        }else {
            borderDistance=RateTran(rect2.y-Rect1.y);
            list.add("margin-top:"+borderDistance+"rpx;");
        }
        map.put(name,list);
    }
    //作为最后一个组件
    //父Rect，当前rect2，邻边section
    private void Lastpart(Rect Rect1,Rect rect2,int direct,String name,Section section){
        List<String> list=new ArrayList<>();
        String borderDistance,neightDistance;
        if(direct==0){
            borderDistance=numberFormat.format(marginHandle((float)(Rect1.x+Rect1.width-rect2.x-rect2.width)/(float)Rect1.width*100-3));
            list.add("margin-right:"+borderDistance+"%;");
            neightDistance=numberFormat.format(marginHandle((float)(rect2.x-section.x-section.width)/(float)Rect1.width*100));
            list.add("margin-left:"+neightDistance+"%;");
        }else {
            borderDistance=RateTran(Rect1.y+Rect1.height-rect2.y-rect2.height-3);
            list.add("margin-bottom:"+borderDistance+"rpx;");
            neightDistance=RateTran((int) (rect2.y-section.y-section.height));
            list.add("margin-top:"+neightDistance+"rpx;");
        }
        map.put(name,list);
    }
    //作为中间一个组件
    private void Middlepart(Rect Rect1,Rect rect2,int direct,String name,Section section){
        List<String> list=new ArrayList<>();
        String neightDistance;
        if(direct==0){
            float margin=(float)(rect2.x-section.x-section.width)/(float)Rect1.width*100;
            neightDistance=numberFormat.format(marginHandle(margin));
            list.add("margin-left:"+neightDistance+"%;");
        }else {
            neightDistance=RateTran((int) (rect2.y-section.y-section.height));
            list.add("margin-top:"+neightDistance+"rpx;");
        }
        map.put(name,list);
    }
    //当前的node
    private void addBorder(TreeNode node,String name){
        List<String> border=map.get(name);
        border.add("display: flex;");
        border.add("border-radius:"+1+node.val.getRadius()/5+"%;");
        border.add("border-style: solid;");
        if(node.parent!=-1) {
            border.add("height:" + RateTran(node.val.getRect().height) + "rpx;");
        }
    }

    //当前的rect
    private void addBlock(Rect rect,String name){
        List<String> block=map.get(name);
        block.add("height:"+RateTran(rect.height)+"rpx;");
        block.add("background-color: grey;");
    }

    private void addImage(Rect rect,String name){
        List<String> image=map.get(name);
        image.add("height:"+RateTran(rect.height)+"rpx;");
    }

    //将图片保存下来
    private String savePic(Mat src , TreeNode node){
        Mat imgROI=new Mat(src,node.val.getRect());
        String date=Long.toString(new Date().getTime());
        String path="image"+ date + image + ".jpg";
        Imgcodecs.imwrite(filePath+"//"+path, imgROI);
        return requestPath+screenshotPath+path;
    }

    //分辨率比率设置
    private String RateTran(int num){
        float height=num*rate;
        return numberFormat.format(height);
    }

    //判断是否在中心，只对Singlepart有用
    public int judgeCenter(TreeNode head,TreeNode node,String name,int direction){
        Rect Rect=head.val.getRect();Rect rect=node.val.getRect();
        int x=Math.abs(Rect.x+Rect.width/2-rect.x-rect.width/2);
        int y=Math.abs(Rect.y+Rect.height/2-rect.y-rect.height/2);
        if(x<rect.width*0.1&&y<rect.height*0.1){
            List<String> list=new ArrayList<>();
            for (String str:map.get(name)){
                if(!str.contains("margin")){
                    list.add(str);
                }
            }
            map.put(name,list);
            return 3;
        }
        return direction;
    }

    private int fontSizeHandle(Border border,List<String> list,String width){
        int rate=border.getRect().width/border.getRect().height;
//        if(rate*1.5<border.getContent().length()){
//            list.add("width:"+width+"%;");
//            return border.getRect().height/2;
//        }
        int percent=Integer.parseInt(numberFormat.format((float)border.getRect().width/(float)src.width()*100));//文字长度占整张图片的比例
        int Sub=0;
        if(rate>5){
            Sub+=3;
        }
        if(percent>70){
            Sub+=6;
        }
        return Math.abs(border.getRect().height-Sub);
    }

    private float marginHandle(float distance){
        return Math.abs(distance-distance/10);
    }
}

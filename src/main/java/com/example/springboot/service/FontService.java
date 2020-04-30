package com.example.springboot.service;

import com.example.springboot.Model.Border;
import com.example.springboot.Model.TreeNode;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FontService {
    public List<Rect> rectList;
    private  List[] FontNo;
    private List[] FontArea;
    @Autowired
    OcrService ocr;

    public void FontOptimize(TreeNode head, Mat src, String path){
        ocr.doOcr(path);
        this.rectList=ocr.getFontRect();
        if(rectList==null)return;
        this.FontArea=new List[rectList.size()];
        this.FontNo=new List[rectList.size()];
        SeperateFont(head,src);
    }
    private void SeperateFont(TreeNode head, Mat src){
        if(head==null)return;
        HashMap<Integer,List<TreeNode>> map=new HashMap<>();//放置要待处理的文字区域(key为负的是带外接框的那个文字区域)
        HashMap<Integer,List<TreeNode>> Rmap=new HashMap<>();//放要合成的文字区域编号和节点（已处理）
        int status=0;
        for (TreeNode item:head.children) {
            List<MatOfPoint2f> matOfPoint2fs = item.val.getPoint2fs();
            for (int i = 0; i < rectList.size(); i++) {
                //找到该border属于哪个文字区域则不用再遍历
                Rect rect = Imgproc.minAreaRect(matOfPoint2fs.get(0)).boundingRect();
                item.val.setRect(rect);
                if (judgeIntersect(rectList.get(i), rect)) {
                    if (judgeHorzBlock(matOfPoint2fs.get(0), rect) &&
                            judgeVertBlock(matOfPoint2fs.get(0), rect)&&rect.area()>100) {//如果是文字的外接框
                        item.val.setKind(4);
                    } else {
                        item.val.setKind(1);
                    }
                    item.val.setContent(ocr.getFontContent().get(i));
                    mapAddItem(item, map, i+1);//将数据添加到map中
                    if (FontArea[i] == null || FontArea[i].size() == 0) {
                        FontArea[i] = new ArrayList<>();
                    }
                    if (FontNo[i] == null) {
                        FontNo[i] = new ArrayList<>();
                    }
                    FontNo[i].add(item.val.getNo().get(0));
                    FontArea[i].add(matOfPoint2fs.get(0));
                    break;//若有其中一个点在区域中，则不用再遍历其他点（status的作用）
                }
            }
            SeperateFont(item, src);
        }
        handleMap(map,Rmap,src);
        removeRepeat(Rmap,head);
    }

    //将重复的文字节点合成一个
    public void removeRepeat( HashMap<Integer,List<TreeNode>> Rmap,TreeNode head){
        //每一个List代表一个文字区域
        for (Map.Entry<Integer,List<TreeNode>> item:Rmap.entrySet()){
            if(item.getValue()==null||item.getValue().size()<=1)
                continue;
            Border border=new Border();
            MatOfPoint2f matOfPoint2f=new MatOfPoint2f();
            List<Point> points=new ArrayList<>();
            String content=item.getValue().get(0).val.getContent();//内容是已经放进里面了的，取第一个
            for (TreeNode node:item.getValue()){//将所有符合这文字区域的边框合在一个points
                points.addAll(node.val.getPoint2fs().get(0).toList());
                border.getNo().addAll(node.val.getNo());
                head.children.remove(node);
            }
            matOfPoint2f.fromList(points);
            Rect rect=Imgproc.minAreaRect(matOfPoint2f).boundingRect();
            border.getPoint2fs().add(getRectMat(rect));
            border.setKind(1);
            border.setRect(rect);
            border.setContent(content);
            TreeNode node=new TreeNode(border);
            node.setParent(node.parent);
            head.addChildren(node);
        }
    }

    private void mapAddItem(TreeNode node,HashMap<Integer,List<TreeNode>> map,int i){
        if(map.containsKey(-i)){//已经存在了带框的map
            map.get(-i).add(node);
        }else {//未有负map
            if(node.val.getKind()==1){
                if(map.containsKey(i)){
                    map.get(i).add(node);//存在正的直接加入
                }else {
                    List<TreeNode> list=new ArrayList<>();
                    list.add(node);
                    map.put(i,list);
                }
            }else {//如果是带框的
                if(map.containsKey(i)){//把正的转过去，再加上新的那个
                    map.put(-i,map.get(i));
                    map.get(-i).add(node);
                    map.remove(i);
                }else {
                    List<TreeNode> list=new ArrayList<>();
                    list.add(node);
                    map.put(-i,list);
                }
            }
        }
    }
    //将需要融合的文字区域放到map（要符合连续的）
    private void handleMap(HashMap<Integer,List<TreeNode>> map,HashMap<Integer,List<TreeNode>> Rmap,Mat src) {
        int size=0;
        for (Map.Entry<Integer, List<TreeNode>> item : map.entrySet()) {
            if(item.getValue()==null||item.getValue().size()==0)continue;
            item.getValue().sort((o1, o2) -> {
                if (o1.val.getRect().x > o2.val.getRect().x) {
                    return 1;
                } else if (o1.val.getRect().x < o2.val.getRect().x) {
                    return -1;
                } else {
                    return 0;
                }
            });
            Border firstBorder=item.getValue().get(0).val;//用于处理第一个图标被识别成文字的情况
            RotatedRect rotatedRect=Imgproc.minAreaRect(firstBorder.getPoint2fs().get(0));
            double Long=Math.max(rotatedRect.size.width,rotatedRect.size.height);
            double Short=Math.min(rotatedRect.size.width,rotatedRect.size.height);
            if (Long / Short < 1.30&&rotatedRect.size.area()>100) {//首位组件为正方形视为图片
                firstBorder.setKind(2);
                firstBorder.setContent(null);
                item.getValue().remove(0);//移除第一个
            }
            if(item.getKey()>=0){
                if (fontSeperate(item.getValue())){
                    handleSeperateFont(item.getValue());
                }else {
                    Rmap.put(size,item.getValue());
                }
                size++;
            }else {//存在有带框文字区域
                int status=0;//0为初始状态或遍历了带框的文字区域
                int position=0;String content=item.getValue().get(0).val.getContent();
                for (TreeNode node:item.getValue()){
                    if(node.val.getKind()==1){//需要融合
                        if(status==0) {//之前是带框的文字，新加入
                            List<TreeNode> list = new ArrayList<>();
                            list.add(node);
                            Rmap.put(size, list);
                            status = 1;
                        }else {
                            Rmap.get(size).add(node);
                        }
                    }else {
                        if(status==1) {//上一个是无框的文字区域集合
                            content = FontResult(Rmap.get(size).get(0), content, item.getValue().size(), Rmap.get(size), position-1);//处理上一个的无框文字
                        }
                        content=SQfontSize(node, content);//处理其本身
                        status=0;
                        size++;
                    }
                    position++;
                }
                size++;//防止上一段文字区域被覆盖
            }
        }
    }

    //判断文字是否该分开,如果整个文字区域大于文字内容字数，则认为分开
    private boolean fontSeperate(List<TreeNode> list){
        if (list.size()<=2)return false;
        int contentLength=list.get(0).val.getContent().length();
        MatOfPoint2f matOfPoint2f=new MatOfPoint2f();
        List<Point> points=new ArrayList<>();
        for (TreeNode node:list){//将所有符合这文字区域的边框合在一个points
            points.addAll(node.val.getPoint2fs().get(0).toList());
        }
        matOfPoint2f.fromList(points);
        Rect rect=Imgproc.minAreaRect(matOfPoint2f).boundingRect();
        if(rect.width/rect.height>contentLength)
            return true;
        return false;
    }
    private void handleSeperateFont(List<TreeNode> list){
        String content=list.get(0).val.getContent();int startIndex=0,endIndex=0, i=0;
        for( i=0;i<list.size()-1;i++){
            startIndex=i*content.length()/list.size();endIndex=startIndex+content.length()/list.size();
            list.get(i).val.setContent(content.substring(startIndex,endIndex));
        }
        list.get(i).val.setContent(content.substring(endIndex));
    }
    //将一个Rect转化成matofpoint
    public static MatOfPoint2f getRectMat(Rect rect){
        MatOfPoint2f matOfPoint2f=new MatOfPoint2f();
        List<Point> points=new ArrayList<>();
        points.add(new Point(rect.x,rect.y));
        points.add(new Point(rect.x+rect.size().width,rect.y));
        points.add(new Point(rect.x+rect.size().width,rect.y+rect.size().height));
        points.add(new Point(rect.x,rect.y+rect.size().height));
        matOfPoint2f.fromList(points);
        return matOfPoint2f;
    }


    public List<MatOfPoint2f>[] getFontArea(){
        return FontArea;
    }

    public List<Integer>[] getFontNo(){
        return FontNo;
    }

    //判断两个Rect是否相交
    private boolean judgeIntersect(Rect fontRect,Rect rect){
        int minx=Math.max(fontRect.x,rect.x);
        int miny=Math.max(fontRect.y,rect.y);
        int maxx=Math.min(fontRect.x+fontRect.width,rect.x+rect.width);
        int maxy=Math.min(fontRect.y+fontRect.height,rect.y+rect.height);
        if(minx<=maxx&&miny<=maxy){
            return true;
        }
        return false;
    }

    //水平block的情况
    private boolean judgeHorzBlock(MatOfPoint2f point2f, Rect rect) {
        List<Point> points = point2f.toList();
        if (points.size() < 4) return false;
        if (points.size() == 4) return true;
        points.sort((o1, o2) -> {
            if (o1.y > o2.y) {
                return 1;
            } else if(o1.y<o2.y) {
                return -1;
            }else {
                return 0;
            }
        });
        //水平block的情况
        if (points.get(0).y == points.get(1).y) {
            double max=Math.max(points.get(0).x,points.get(1).x);
            double min=Math.min(points.get(0).x,points.get(1).x);
            int length = Math.abs((int) (points.get(0).x - points.get(1).x));
            if (length > rect.width * 0.8) {
                for (Point point:points){
                    if(point.x<max&&point.x>min){//存在一个点夹在中间都认为是不符合
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    //垂直block的情况
    private boolean judgeVertBlock(MatOfPoint2f point2f,Rect rect){
        List<Point> points = point2f.toList();
        if (points.size() < 4) return false;
        if (points.size() == 4) return true;
        points.sort((o1, o2) -> {
            if (o1.x > o2.x) {
                return 1;
            } else if(o1.x<o2.x) {
                return -1;
            }else {
                return 0;
            }
        });
        //垂直的block
        //水平block的情况
        if (points.get(0).x == points.get(1).x) {
            int length = Math.abs((int) (points.get(0).y - points.get(1).y));
            if (length > rect.height * 0.6) {
                return true;
            }
        }
        return false;
    }

    //node当前节点，content剩下的内容，size本个map的大小，listRmap的已存的list，position对应size的位置
    private String FontResult(TreeNode node,String content,int size,List<TreeNode> list,int position){
        if(content==null)return null;
        int length=0;
        if(position==size-1){//最后一个
            node.val.setContent(content.length()>2?content.substring(content.length()-2):content);
            return null;
        }
        for (TreeNode item:list){
            length+=item.val.getRect().width/item.val.getRect().height;
        }
        if(length>content.length()-1)length=content.length()-1;
        node.val.setContent(content.substring(0,length));
        return content.substring(length);
    }

    private String SQfontSize(TreeNode node,String content){
        if(content==null)return null;
        if(content.length()>2) {
            node.val.setContent(content.substring(0, 2));
            return content.substring(2);
        }else {
            node.val.setContent(content);
            return null;
        }
    }
}

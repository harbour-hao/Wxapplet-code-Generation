package com.example.springboot.service;

import com.example.springboot.Model.Border;
import com.example.springboot.Model.TreeNode;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Service
public class BorderService {
    private static Stack<TreeNode> stack = new Stack<>();//存放当前遍历的树枝（带有文字区域的）
    private static List<Stack<TreeNode>> list = new ArrayList<>();
    private Mat src;
    public void findBorder(TreeNode head,Mat src) {
        this.src=src;
        NodeHandle(head);//遍历一次，找出文字上一层的border
        handleStack();
        Loop(head);//处理block和带框的文字
    }

    //遍历树，把一条条包含文字的树枝存起来
    private void NodeHandle(TreeNode node) {
        if (node == null) return;
        for (TreeNode item : node.children) {
            if ((item.val.getKind() == 1||item.val.getKind() == 4) && !stack.empty()&&item.children.size()==0) {
                list.add((Stack<TreeNode>) stack.clone());
                continue;//children都为0，下面执不执行都一样
            }
            stack.push(item);
            NodeHandle(item);
            stack.pop();
        }
    }

    //文字的父节点必定为边框0，默认设置为
    private void handleStack() {
        List<Integer> nodeNum = new ArrayList<>();
        for (Stack<TreeNode> stack : list) {
            while (!stack.empty()) {
                TreeNode node = stack.pop();
                //作一个重复节点不执行判断
                if (!nodeNum.contains(node.val.getNo().get(0))) {
                    nodeNum.add(node.val.getNo().get(0));
                    node.val.setKind(0);
                    node.val.setRadius( node.val.getPoint2fs().get(0).toArray().length);
                    distinguishBorder(node);
                }
            }
        }
    }

    //在Fontoptimize里实现多个border的邻接中的block
    private void distinguishBorder(TreeNode head) {
        if (head.val.getPoint2fs().size() == 2) {
            RotatedRect rect1 = Imgproc.minAreaRect(head.val.getPoint2fs().get(0));
            RotatedRect rect2 = Imgproc.minAreaRect(head.val.getPoint2fs().get(1));
            head.val.setWidth ((int) (rect1.size.width - rect2.size.width) / 2);
            head.val.setRadius((head.val.getPoint2fs().get(0).toArray().length + head.val.getPoint2fs().get(1).toArray().length) / 2);//弯度
            head.val.setRect( rect1.boundingRect());
        } else {//若多个内边，只需在孩子中添加邻接(只处理了只有竖向排列的情况)
            sortBorder(head.val.getPoint2fs());
            //把每个内框的最下边的拿出来放到孩子中
            for (int i = 1; i < head.val.getPoint2fs().size() - 1; i++) {
                RotatedRect rect1 = Imgproc.minAreaRect(head.val.getPoint2fs().get(i));
                RotatedRect rect2 = Imgproc.minAreaRect(head.val.getPoint2fs().get(i + 1));
                MatOfPoint2f matOfPoint2f = getAdjacent(rect1.boundingRect(), rect2.boundingRect());
                Border border = new Border();
                border.getPoint2fs().add(matOfPoint2f);
                border.setKind( 3);
                border.setRect(Imgproc.minAreaRect(matOfPoint2f).boundingRect());
                TreeNode node = new TreeNode(border);
                head.children.add(node);
            }
        }
    }

    //将内边框从高到低排序
    private void sortBorder(List<MatOfPoint2f> list) {
        list.sort((o1, o2) -> {
            if (o1.get(0, 0)[1] > o2.get(0, 0)[1]) {
                return 1;
            } else {
                return -1;
            }
        });
    }

    //获取两个rect的邻接边
    private MatOfPoint2f getAdjacent(Rect rect1, Rect rect2) {
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        List<Point> points = new ArrayList<>();
        int height = rect2.y - rect1.y - rect1.height;
        points.add(new Point(rect1.x, rect1.y + rect1.height));
        points.add(new Point(rect1.x + rect1.width, rect1.y + rect1.height));
        points.add(new Point(rect1.x + rect1.width, rect1.y + rect1.height + height));
        points.add(new Point(rect1.x, rect1.y + rect1.height + height));
        matOfPoint2f.fromList(points);
        return matOfPoint2f;
    }

    //识别出矩形，判断是否为block和处理font的外接框
    private void optimizeSquare(TreeNode node) {
        if (node.val.getRect() == null) {
            node.val.setRect( Imgproc.minAreaRect(node.val.getPoint2fs().get(0)).boundingRect());
        }
        if (node.children.size() != 0) return;
        Rect rect=node.val.getRect();
        if ( node.val.getKind() == 2 ) {//判断是否block的情况
            int whrate = rect.width / rect.height;
            int hwrate = rect.height / rect.width;
            if ((whrate > 2 || hwrate > 2) && node.val.getPoint2fs().size() == 1) {//扁平数据
                if (judgeHorzBlock(node.val.getPoint2fs().get(0), rect)||
                        judgeVertBlock(node.val.getPoint2fs().get(0), rect)) {
                    node.val.setKind(3);
                }
            }
        }else if(node.val.getKind()==4){//处理文字外接边框的情况
            Border border=new Border();
            border.setRect(new Rect(rect.x+rect.width/4,rect.y+rect.height/4, rect.width/2,rect.height/2));
            border.setKind(1);
            border.setContent(node.val.getContent());
            border.getPoint2fs().add(FontService.getRectMat(border.getRect()));
            TreeNode childNode=new TreeNode(border);
            node.children.add(childNode);
            node.val.setKind(0);
            node.val.setContent(null);
            node.val.setRadius(node.val.getPoint2fs().get(0).toArray().length/10);
        }
    }

    //水平block的情况
    private boolean judgeHorzBlock(MatOfPoint2f point2f, Rect rect) {
        List<Point> points = point2f.toList();
        if (points.size() < 4) return false;
        if (points.size() == 4) return true;
        points.sort((o1, o2) -> {
            if (o1.y > o2.y) {
                return 1;
            } else {
                return -1;
            }
        });
        //水平block的情况
        if (points.get(0).y == points.get(1).y) {
            int length = Math.abs((int) (points.get(0).x - points.get(1).x));
            if (length > rect.width * 0.9) {
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
            } else {
                return -1;
            }
        });
        //垂直的block
        //水平block的情况
        if (points.get(0).x == points.get(1).x) {
            int length = Math.abs((int) (points.get(0).y - points.get(1).y));
            if (length > rect.height * 0.9) {
                return true;
            }
        }
        return false;
    }

    private void correctRect(Rect rect){
        if(rect==null)return;
        if(rect.x<0)rect.x=0;
        if(rect.x>src.width())rect.x=src.width();
        if (rect.y<0)rect.y=0;
        if(rect.y>src.height())rect.y=src.height();
    }

    private void Loop(TreeNode head){
        if(head==null)return;
        for (TreeNode node:head.children){
            optimizeSquare(node);//识别block和处理带框的文字区域
            correctRect(node.val.getRect());
            Loop(node);
        }
    }
}

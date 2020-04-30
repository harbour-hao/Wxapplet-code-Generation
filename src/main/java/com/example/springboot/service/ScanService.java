package com.example.springboot.service;
import com.example.springboot.Model.Border;
import com.example.springboot.Model.TreeNode;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_CCOMP;
import static org.opencv.imgproc.Imgproc.RETR_TREE;

@Service
public class ScanService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ScanService.class);

    /**上传地址*/
    @Value("${opencv.ready.path}")
    private String dllPath;

    @Autowired
    FontService fontService;

    @Autowired
    BorderService borderService;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.load(dllPath+"/opencv_java410.dll");
    }

    public TreeNode doScan(String path){
        Mat src = Imgcodecs.imread(path);
        Mat img = src.clone();
        // 彩色转灰度
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        // 高斯滤波，降噪
        Imgproc.GaussianBlur(img, img, new Size(3,3), 2, 2);
        // Canny边缘检测
        Imgproc.Canny(img, img, 0, 100, 3, false);//可调阈值的高低

        //反复膨胀和腐蚀
        Imgproc.dilate(img, img, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)), new Point(-1, -1), 3, 1, new Scalar(1));
        Imgproc.erode(img, img, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)), new Point(-1, -1), 2, 1, new Scalar(1));

        List<MatOfPoint> COMP_contours = new ArrayList<>();List<MatOfPoint> TREE_contours = new ArrayList<>();
        Mat COMP_hierarchy = new Mat();Mat TREE_hierarchy = new Mat();
        //找出轮廓
        Imgproc.findContours(img, COMP_contours, COMP_hierarchy,RETR_CCOMP, CHAIN_APPROX_SIMPLE);
        Imgproc.findContours(img, TREE_contours, TREE_hierarchy,RETR_TREE, CHAIN_APPROX_SIMPLE);
        TreeNode head=CreateTree(TREE_contours,TREE_hierarchy,COMP_contours,COMP_hierarchy, src);
        fontService.FontOptimize(head,src,path);//文字处理
        borderService.findBorder(head,src);//边框处理
        return head;
    }

    //以Tree为主,Comp为辅
    private TreeNode CreateTree(List<MatOfPoint> Tree_contours, Mat Tree_hierarchy, List<MatOfPoint> Comp_contours, Mat Comp_hierarchy, Mat src ){
        int []TCindex=GetIndex(Tree_contours,Comp_contours).get(0);//记录Tree_contours对应哪一个Comp_contours
        int []CTindex=GetIndex(Tree_contours,Comp_contours).get(1);//记录Tree_contours对应哪一个Comp_contours
        TreeNode head=null;
        for (int i=0;i<Tree_contours.size();i++){
            Border border=new Border();
            double childBorder=Comp_hierarchy.get(0,TCindex[i])[2];//在Comp中对应子轮廓的编号
            double parentBorder=Comp_hierarchy.get(0,TCindex[i])[3];//在Comp中对应父轮廓的编号
            if(parentBorder==-1.0){//处理那些Comp中的父节点
                border.getNo().add(i);//记录的是Tree的编号
                MatOfPoint2f origin=new MatOfPoint2f(Tree_contours.get(i).toArray());
                border.getPoint2fs().add(origin);
                if(head==null){
                    head=InitialTree(src,border);//树的最初的头，以图片为大小
                }else {
                    int parent=(int) Tree_hierarchy.get(0,border.getNo().get(0))[3];
                    TreeNode node=new TreeNode(border);
                    //此Border肯定是在Comp中为父的（为主），直接添加节点即可
                    if(parent!=-1){
                        node.setParent(parent);
                        addChild(head,node);
                    }else {
                        node.setParent(-1);
                        head.children.add(node);
                    }
                }
            }else {
                //此Border必定在Comp中为子，所以要附庸其主(在Tree中，从外到内，所以Comp的主节点必定先被创建)
                //添加Comp中附属的border，即Comp中其所有的子Border都将添加在val的list中
                addSub(new MatOfPoint2f(Tree_contours.get(i).toArray()),head,CTindex[(int)parentBorder],i);
            }
        }
        return head;
    }

    //树的初始化。以图像的大小的头
    private TreeNode InitialTree(Mat src,Border childBorder){
        MatOfPoint2f matOfPoint2f=new MatOfPoint2f();
        List<Point> pointList=new ArrayList<>();
        pointList.add(new Point(0,0));
        pointList.add(new Point(src.width(),0));
        pointList.add(new Point(src.width(),src.height()));
        pointList.add(new Point(0,src.height()));
        matOfPoint2f.fromList(pointList);
        Border border=new Border();
        border.getNo().add(-1);
        border.setKind(0);
        border.getPoint2fs().add(matOfPoint2f);
        border.setRect(new Rect(0,0,src.width(),src.height()));
        TreeNode head =new TreeNode(border);
        head.setParent(-2);
        TreeNode child=new TreeNode(childBorder);
        child.setParent(-1);
        head.children.add(child);
        return head;
    }
    private List<int[]> GetIndex(List<MatOfPoint> Tree_contours,List<MatOfPoint> Comp_contours){
        int[] TCindex=new int[Tree_contours.size()];//Tree对应对应Comp是哪一个
        int[] CTindex=new int[Comp_contours.size()];
        for (int i=0;i<Tree_contours.size();i++) {
            int j = 0;
            for (j = 0; j < Comp_contours.size(); j++) {//判断对应的Comp_contours对应哪一个
                if (Tree_contours.get(i).dump().equals(Comp_contours.get(j).dump())) {
                    break;
                }
            }
            TCindex[i] = j;
            CTindex[j] = i;
        }
        List<int[]> list=new ArrayList<>();
        list.add(TCindex);
        list.add(CTindex);
        return list;
    }

    // 向指定多叉树节点添加子节点
    private void addChild(TreeNode head,TreeNode node){
        if(head==null)return;
        if(head.val.getNo().contains(node.parent)){
            head.children.add(node);
        }else {
            for (TreeNode item:head.children) {
                if (item.val.getNo().contains(node.parent)) {
                    item.children.add(node);
                    break;
                } else {
                    addChild(item, node);
                }
            }
        }
    }
    //向指定节点的val添加附属的border
    private  void addSub(MatOfPoint2f matOfPoint2f,TreeNode head,int parent,int No){
        if(head==null)return;
        if(head.val.getNo().contains(parent)){
            head.val.getPoint2fs().add(matOfPoint2f);
            head.val.getNo().add(No);
        }else {
            for (TreeNode item:head.children) {
                if (item.val.getNo().contains(parent)) {
                    item.val.getPoint2fs().add(matOfPoint2f);
                    item.val.getNo().add(No);
                    break;
                } else {
                    addSub(matOfPoint2f,item, parent,No);
                }
            }
        }
    }
}

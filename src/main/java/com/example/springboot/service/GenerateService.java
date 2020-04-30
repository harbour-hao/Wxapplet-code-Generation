package com.example.springboot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.springboot.Model.Border;
import com.example.springboot.Model.Direction;
import com.example.springboot.Model.Section;
import com.example.springboot.Model.TreeNode;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class GenerateService {
    public  StringBuilder styleBuilder=new StringBuilder();
    @Autowired
    AddcodeService addcodeService;
    @Autowired
    LayoutService layoutService;

    public  int height=300;
    //创建一层的代码
    public void Layercode (TreeNode node, Mat src){
        addcodeService.intialCode(src);
        //获得处理好的分割模块
        addcodeService.addBuilder("<view class=\"container \">\n");
        handleHead(iteratorTree(node));
        addcodeService.endCode();

    }
    public String getContent(){
        return addcodeService.getBuilder();
    }

    public String getStyle(){
        showStyle();
        return styleBuilder.toString();
    }

    //返回分割的方向
    private int iteratorTree(TreeNode head){
        if (head==null)return -1;
        if(head.children.size()==0)return -1;
        Direction direct=layoutService.Seperate(head.children);
        //遍历分解后的模块
        if(direct==null)return -1;//树的遍历到尽头了
        for (int i=0;i<direct.list.size();i++){
            Section section=direct.list.get(i);
            layoutService.Optimize(head,section);
            //若一个块中包含不止一个则继续分解
            if(section.border!=null&&section.border.size()>1){
                String name=addcodeService.addSection(direct,i,head);
                int direction;
                direction=DirectSeperate(section);
                addcodeService.endCode(name,direction);
            }else {
                if(section.border!=null&&section.border.size()==1) {
                    String name=addcodeService.addTreeNode(direct,i,head);
                    int direction=-1;//父Border里的组件分解后的方向
                    if(section.border.get(0).val.getKind()!=2)direction=iteratorTree(section.border.get(0));
                    addcodeService.endCode(name,direction);
                    if(direct.list.size()==1&&head.parent!=-2){
                        return addcodeService.judgeCenter(head,direct.list.get(0).border.get(0),name,direct.direction);
                    }
                }
            }
        }
        return direct.direction;
    }

    private int DirectSeperate(Section Section){
        Direction direction=layoutService.Seperate(Section.border);
        List<Section> sections=direction.list;
        //每一块section里至少有一个border
        for (int i=0;i<sections.size();i++){
            Section section=sections.get(i);
            layoutService.Optimize(Section,section);
            //存在一个section里有两个border则继续分解
            if(section.border.size()>1){
                String name=addcodeService.addSection(direction,i,Section);
                int direct;
                direct=DirectSeperate(section);
                addcodeService.endCode(name,direct);
            }else if(section.border.size()==1) {
                //已分解到尽头且还有子树的继续遍历
                String name=addcodeService.addTreeNode(direction,i,Section);
                int direct=-1;
                if(section.border.get(0).val.getKind()!=2)direct=iteratorTree(section.border.get(0));
                if(name!=null)addcodeService.endCode(name,direct);
            }
        }
        return direction.direction;
    }

    private String getRandomName(){
        Random random=new Random();
        StringBuilder builder=new StringBuilder();
        for (int i=0;i<5;i++){
            builder.append((char)(random.nextInt(26)+'a'));
        }
        return builder.toString();
    }

    private void showStyle(){
        styleBuilder.delete(0,styleBuilder.length());
        for(Map.Entry<String, List<String>> entry: addcodeService.getMap().entrySet()){
            styleBuilder.append("."+entry.getKey()+"{"+"\n");
            for (String s:entry.getValue()){
                styleBuilder.append(s+"\n");
            }
            styleBuilder.append("}\n");
        }
    }

    private void handleHead(int i){
        if(i==0) {
            List<String> list=new ArrayList<>();
            list.add("display:flex");
            addcodeService.addMap("container",list);
        }
    }

    //把Json转为Tree
    public TreeNode JsonToTree(String json){
        JSONObject node= JSON.parseObject(json);
        JSONObject val=node.getJSONObject("val");
        JSONArray children=node.getJSONArray("children");
        TreeNode head=new TreeNode(ValToBorder(val));
        AddToChildren(head,children);
        return head;
    }

    //传入父节点，和该节点对应的children的jsonArray
    private void AddToChildren(TreeNode node,JSONArray childrens){
        for (int i=0;i<childrens.size();i++){
            JSONObject jsonObject=childrens.getJSONObject(i);
            Border border=ValToBorder(jsonObject.getJSONObject("val"));
            TreeNode child=new TreeNode(border);
            node.addChildren(child);
            AddToChildren(node.children.get(i),jsonObject.getJSONArray("children"));
        }
    }

    private Border ValToBorder(JSONObject val){
        JSONObject rectJson=val.getJSONObject("rect");
        Border border=new Border();
        border.setRect(new Rect(rectJson.getInteger("x"),rectJson.getInteger("y"),
                rectJson.getInteger("width"),rectJson.getInteger("height")));
        border.setContent(val.getString("content"));
        border.setWidth(val.getInteger("width"));
        border.setKind(val.getInteger("kind"));
        return border;
    }
}

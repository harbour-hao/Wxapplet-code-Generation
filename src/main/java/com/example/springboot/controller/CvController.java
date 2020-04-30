package com.example.springboot.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.springboot.Model.Test;
import com.example.springboot.Model.TreeNode;
import com.example.springboot.service.GenerateService;
import com.example.springboot.service.ScanService;
import com.example.springboot.util.JsonUtil;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Controller
public class CvController {

    @Value("${file.upload.path}")
    private String filePath;

    @Autowired
    ScanService scanService;

    @Autowired
    GenerateService generateService;
    
    @RequestMapping(path = {"/scan"}, method = {RequestMethod.POST})
    @ResponseBody
    public String scan(@RequestParam("filename") String filename){
        TreeNode node=scanService.doScan(filePath+"/"+filename);
        if(node==null){
            return JsonUtil.getJSONString(1,"无法扫描图片");
        }
        return JsonUtil.getJSONString(0,node);
    }

    @RequestMapping(path = {"/generate"}, method = {RequestMethod.POST})
    @ResponseBody
    public String generate(@RequestParam("head") String head,@RequestParam("filename") String filename){
        TreeNode node=generateService.JsonToTree(head);
        String path=filePath+"/"+filename;
        Mat src = Imgcodecs.imread(path);
        generateService.Layercode(node,src);
        HashMap<String,String> map=new HashMap<>();
        if(generateService.getContent()==null){
            return JsonUtil.getJSONString(1,"无法生成代码");
        }
        map.put("content",generateService.getContent());
        map.put("style",generateService.getStyle());
        return JsonUtil.getJSONString(0,map);
    }
}

package com.example.springboot.controller;

import com.example.springboot.util.JsonUtil;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
public class FileController {
    /**上传地址*/
    @Value("${file.upload.path}")
    private String filePath;

    @Value("${file.upload.path.relative}")
    private String fileRelativePath;

    // 跳转上传页面
    @RequestMapping("test")
    public String test() {
        return "picUpload";
    }

    // 执行上传
    @RequestMapping(path = {"/upload"}, method = {RequestMethod.POST})
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file) {
        // 获取上传文件名
        String filename = file.getOriginalFilename();
        // 定义上传文件保存路径
        String path = filePath;
        // 新建文件
        File filepath = new File(path, filename);
        // 判断路径是否存在，如果不存在就创建一个
        if (!filepath.getParentFile().exists()) {
            filepath.getParentFile().mkdirs();
        }
        try {
            // 写入文件
            file.transferTo(new File(path + File.separator + filename));
        } catch (IOException e) {
            e.printStackTrace();
            return JsonUtil.getJSONString(-1,e.getMessage());
        }
        // 将src路径发送至html页面
        return JsonUtil.getJSONString(0,fileRelativePath+filename);
    }
}

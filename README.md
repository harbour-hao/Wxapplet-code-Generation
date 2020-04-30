# Wxapplet-code-Generation
微信小程序代码生成springboot+Vue，界面如下图
 ![image](https://github.com/harbour-hao/Wxapplet-code-Generation/blob/master/image/index.png)
启动方法：maven 安装所需的包成功后
去百度智能云注册一个账号，申请通用带位置文字识别的api使用权限，分别将APP_ID,API_KEY,SECRET_KEY填入OcrService.java文件的对应参数中
再把application.properties里面的绝对路径更改成为你项目对应的文件夹路径，相对路径无需更改
file.upload.path：为上传设计图的图片存放位置
opencv.ready.path：opencv的运行包的位置
file.screenshot.path：识别过程生成的截图存放位置

package com.tisai.daguai.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.tisai.daguai.common.CustomException;
import com.tisai.daguai.common.SystemException;
import com.tisai.daguai.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

import static com.tisai.daguai.utils.SystemContents.BATH_PATH;

/**
 * 文件的上传与下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    /**
     * 文件上传
     * @param file 参数名必须要与前端传过来的name一致
     * @return r
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file){
        //file是一个临时文件,需要转存到特定位置,不然本次请求结束后文件消失

        //获得原始文件名,即上传时的文件名,但可能会出现重复覆盖问题
        String originalFilename = file.getOriginalFilename();//xxx.xxx

        //获得原文件后缀
        //使用substring方法截取,substring(index)是把原字符串index位前面的字符截下去得到一个新字符串,例如unhappy.substring(2)=happy
        //lastIndexOf(string)是得到最后一个string出现的index
        if(originalFilename==null){
            throw new CustomException("originalFilename illegal");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//suffix=.xxx

        //使用UUID重新生成随机文件名,方法:UUID.randomUUID()
        String fileName = UUID.randomUUID().toString() + suffix;

        //创建一个目录对象,路径为basePath
        File dir = new File(BATH_PATH);
        //判断当前目录是否存在
        if(!dir.exists()){
            //目录不存在,需要创建File.mkdir
            var b = dir.mkdir();
            if(!b){
                throw new SystemException("mkdir error");
            }
        }

        //log.info("原始文件名: {}\n后缀suffix= {}\n新文件名: {}",originalFilename,suffix,fileName);
        try {
            file.transferTo(new File(BATH_PATH+fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //log.info(file.toString());

        //浏览器需要文件名存到表单以便最后传给后端存数据库,所以需要返回fileName
        return Result.ok(fileName);
    }

    /**
     * 文件下载
     * @param name :文件名
     * @param response :图片需要通过二进制输出流写回到浏览器,需要用到response
     * 无返回值,因为返回格式为二进制输出流,而不是json
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            if(StringUtils.isEmpty(name)){
                //名字为空,结束读取
                return;
            }
            //输入流,通过输入流读取文件内容
            //文件输入流对象,把文件创建为文件输出流对象FileInputStream
            FileInputStream fileInputStream=new FileInputStream(new File(BATH_PATH+name));//File对象既可以表示一个路径,也可以表示一个文件
            //输出流,通过输出流将文件写回浏览器,在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            //设置响应返回数据格式为图片image/jpeg
            response.setContentType("image/jpeg");
            int len;
            byte[] bytes = new byte[1024];
            //read(bytes)读取最多bytes.length的数据写到bytes数组中,返回值为读取到"缓冲区buffer"的总数,读取完成时返回值为-1
            while ((len = fileInputStream.read(bytes))!= -1){
                //log.info(""+len+"");
                outputStream.write(bytes,0,len);
                //刷新
                outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

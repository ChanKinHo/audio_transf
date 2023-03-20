package com.house.audiotransf.controller;


import com.alibaba.fastjson.JSON;
import com.house.audiotransf.constant.BaseVo;
import com.house.audiotransf.constant.RespConstant;
import com.house.audiotransf.service.AudioGeneration;
import com.house.audiotransf.synthesizer.tencent.TencAudioHandler;
import com.house.audiotransf.untils.ConvertUtils;
import com.house.audiotransf.untils.WaveHeader;
import com.iflytek.cloud.speech.*;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Controller
public class AudioTransController {

    private final Logger logger = LoggerFactory.getLogger(AudioTransController.class);

    @Resource
    private AudioGeneration generation;

    private final Properties props = new Properties();

    @Value("${com.house.hot.path}")
    private String hotPath;


    @RequestMapping("/audiotransf/tts")
    @ResponseBody
    public BaseVo ttsSpeaker(@RequestParam(value = "text", required = false) String text, @RequestParam(value = "channel") String channel) {

        if (StringUtils.isBlank(text)) {
            return BaseVo.fail(RespConstant.TEXT_VOID_CODE,RespConstant.TEXT_VOID_MSG);
        }

        String path;
        try{
            path = generation.generateAudio(text,channel);
        } catch (Exception e) {
            logger.error("ttsSpeaker generate audio err", e);
            return BaseVo.fail(RespConstant.GENERATION_FAIL_CODE,RespConstant.GENERATION_FAIL_MSG);
        }

        return BaseVo.succ(path);
    }

    @RequestMapping(value = "/audiotransf/downloadBase64",method = RequestMethod.POST)
    @ResponseBody
    public BaseVo downloadAudioBase64(@RequestParam(value = "path") String path) {

        String targetPath;
        try {
            targetPath = ConvertUtils.convertToMp3(path);
        } catch (Exception e) {
            logger.error("转成mp3格式失败",e);
            return BaseVo.fail("-3","转成mp3格式失败");
        }

        String base64Str = "";
        FileInputStream fis = null;
        try {
            File file = new File(targetPath);
            fis = new FileInputStream(file);
            byte[] bytes=new byte[(int)file.length()];
            fis.read(bytes);
            base64Str = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            logger.error("文件转化失败",e);
            return BaseVo.fail("-2","文件转化base64失败");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    logger.error("流关闭失败",e);
                }
            }
        }

        return BaseVo.succ(base64Str);
    }

    @PostMapping(value = "/audiotransf/base64ToFile")
    @ResponseBody
    public BaseVo base64ToFile(@RequestBody Map<String,Object> baseObj){

//        logger.info("传进来的base64对象: " + JSON.toJSONString(baseObj));

        try {
            props.load(new FileInputStream(hotPath));
        } catch (Exception e) {
            logger.error("base64ToFile load props err ",e);
            return BaseVo.fail();
        }

        String str = (String) baseObj.get("baseStr");

        logger.info("传进来的base64字符串: " + str);


        File file = null;
        //创建文件目录
        String filePath = props.getProperty("decode_Base_Path");
        File dir = new File(filePath);
        //判断是否存在文件夹
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdirs();
        }
        UUID uuid = UUID.randomUUID();
        String fileName = uuid + ".mp3";
        BufferedOutputStream bos = null;
        java.io.FileOutputStream fos = null;
        try {
            byte[] bytes = Base64.getMimeDecoder().decode(str);
            file=new File(filePath + fileName);
            fos = new java.io.FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            logger.error("",e);
            return BaseVo.fail();
        } finally {
            // 关闭流
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception e) {
                    logger.error("",e);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    logger.error("",e);
                }
            }
        }
        return BaseVo.succ();
    }

}

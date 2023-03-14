package com.house.audiotransf.controller;


import com.alibaba.fastjson.JSON;
import com.house.audiotransf.constant.BaseVo;
import com.house.audiotransf.constant.RespConstant;
import com.house.audiotransf.service.AudioGeneration;
import com.house.audiotransf.untils.WaveHeader;
import com.iflytek.cloud.speech.*;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


    @RequestMapping("/audiotransf/hello")
    public String testNew(@RequestParam(value = "name",required = false) String name, ModelMap map){

        map.put("msg", "hello," + name);

        return "welcome";
    }


    @RequestMapping("/audiotransf/tts")
    @ResponseBody
    public BaseVo ttsSpeaker(@RequestParam(value = "text", required = false) String text, HttpServletResponse response) {

        if (StringUtils.isBlank(text)) {
            return BaseVo.fail(RespConstant.TEXT_VOID_CODE,RespConstant.TEXT_VOID_MSG);
        }

        String path;
        try{
            path = generation.generateAudio(text);
        } catch (Exception e) {
            logger.error("ttsSpeaker generate audio err", e);
            return BaseVo.fail(RespConstant.GENERATION_FAIL_CODE,RespConstant.GENERATION_FAIL_MSG);
        }

        return BaseVo.succ(path);
    }

    @RequestMapping(value = "/audiotransf/downloadBase64",method = RequestMethod.POST)
    @ResponseBody
    public BaseVo downloadAudioBase64(@RequestParam(value = "path") String path) {

        Map<String, String> map = new HashMap<>();
        try {
            map = convertToMp3(path);
        } catch (Exception e) {
            logger.error("转成mp3格式失败",e);
            return BaseVo.fail("-3","转成mp3格式失败");
        }

        String base64Str = "";
        FileInputStream fis = null;
        try {
            File file = new File(map.get("path"));
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


    @RequestMapping(value = "/audiotransf/download",method = RequestMethod.POST)
    @ResponseBody
    public BaseVo downloadAudio(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "path") String path) throws Exception {

        if (StringUtils.isBlank(path)) {
            return BaseVo.fail("000001","路径不能为空!");
        }

        logger.info("1111111111111111111111111111111111" + path);


        //pcm格式转mp3
        Map<String,String> map = new HashMap<>();
        try {
            map = convertToMp3(path);

            System.out.println("转换后: " + JSON.toJSONString(map));
        } catch (Exception e) {
            logger.error("转换mp3格式失败",e);
            throw e;
        }

        File file  = new File(map.get("path"));
        if (file.exists()) {
            //设置响应头信息
            response.reset();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename="+map.get("fileName"));//使下载的文件名与原文件名对应
            response.setHeader("Connection", "close");

            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;

            try{
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream outputStream = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    outputStream.write(buffer,0,i);
                    i = bis.read(buffer);

                }
            } catch (Exception e) {
                logger.error("3333333333333333333333333333333333",e);
            } finally {
                try {
                    assert bis != null;
                    bis.close();
                    assert fis != null;
                    fis.close();
                } catch (Exception e) {
                    logger.error("66666666666666666666666666666666666666666",e);
                }

            }
        }


        return BaseVo.succ();
    }

    private Map<String,String> convertToMp3(String path) throws Exception {

        String separator = File.separator;
        logger.info("路径分隔符: " + separator);
        String[] strings = path.split(separator);
//        String[] strings = path.split("\\\\");

        String sourceFileName = strings[strings.length - 1];
        logger.info("文件名：" + sourceFileName);

        System.out.println("源路径文件：" + path);

        String realPath = path.substring(0,path.length()-sourceFileName.length());
        String targetFileName = sourceFileName.substring(0,sourceFileName.length()-4) + ".mp3";
        String targetFilePath =realPath + targetFileName;

        System.out.println("目标文件路径：" + targetFilePath);


        FileInputStream fis = new FileInputStream(path);
        FileOutputStream fos = new FileOutputStream(targetFilePath);

        File file = new File(path);
        long length = file.length();
        System.out.println("length:" + length);


//        int size = fis.read();
//        System.out.println("size:" + size);
        int pcmSize = (int) length;
//        while (size != -1) {
//            pcmSize += size;
//            size = fis.read(buf);
//        }
//        fis.close();

        System.out.println("pcmsize:" + pcmSize);

        WaveHeader header = new WaveHeader();
        header.fileLength = pcmSize + (44 - 8);
        header.FmtHdrLeth = 16;
        header.BitsPerSample = 16;
        header.Channels = 1;
        header.FormatTag = 0x0001;
        header.SamplesPerSec = 16000;
        header.BlockAlign = (short) (header.Channels* header.BitsPerSample/8);
        header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
        header.DataHdrLeth = pcmSize;

        byte[] h = header.getHeader();
        assert h.length == 44;

        fos.write(h,0,h.length);


        byte[] buf = new byte[1024 * 4];
        fis = new FileInputStream(path);
        int size = fis.read(buf);
        while (size != -1){
            fos.write(buf,0,size);
            size = fis.read(buf);
        }

        fis.close();
        fos.close();

        Map<String,String> map = new HashMap<>();
        map.put("path",targetFilePath);
        map.put("fileName",targetFileName);

        return map;
    }

    public static void main(String[] args) {
        String fileName = "123.PCM";
        String s = fileName.substring(0, fileName.length() - 4);
        System.out.println(s);
    }

}

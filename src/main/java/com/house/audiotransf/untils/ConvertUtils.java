package com.house.audiotransf.untils;

import com.house.audiotransf.synthesizer.tencent.TencAudioHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ConvertUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConvertUtils.class);

    public static String convertToMp3(String originPath) throws Exception{
        String separator = File.separator;
        logger.info("路径分隔符: " + separator);
//        String[] strings = originPath.split(separator);
        String[] strings = originPath.split("\\\\");

        String sourceFileName = strings[strings.length - 1];
        logger.info("文件名：" + sourceFileName);

        System.out.println("源路径文件：" + originPath);

        String realPath = originPath.substring(0,originPath.length()-sourceFileName.length());
        String targetFileName = sourceFileName.substring(0,sourceFileName.length()-4) + ".mp3";
        String targetFilePath =realPath + targetFileName;

        System.out.println("目标文件路径：" + targetFilePath);


        FileInputStream fis = new FileInputStream(originPath);
        FileOutputStream fos = new FileOutputStream(targetFilePath);

        File file = new File(originPath);
        long length = file.length();
        System.out.println("length:" + length);

        int pcmSize = (int) length;

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
        fis = new FileInputStream(originPath);
        int size = fis.read(buf);
        while (size != -1){
            fos.write(buf,0,size);
            size = fis.read(buf);
        }

        fis.close();
        fos.close();

        return targetFilePath;
    }

}

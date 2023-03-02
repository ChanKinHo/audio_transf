package com.house.audiotransf.controller;


import com.alibaba.fastjson.JSON;
import com.house.audiotransf.constant.BaseVo;
import com.house.audiotransf.untils.WaveHeader;
import com.iflytek.cloud.speech.*;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Controller
public class AudioTransController {

    private final static String STORE_PLACE = "C:"+ File.separator + "Users" +File.separator +"ckh"+File.separator +"Desktop"+File.separator +"pcm"+File.separator ;
    private final static String LINUX_STORE_PLACE = File.separator + "houseapps"+ File.separator + "audiotransf"+ File.separator + "audiofiles"+ File.separator;


    // 语音合成对象
    private SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer();

    private final Logger logger = LoggerFactory.getLogger(AudioTransController.class);

    private String mText = "";

    private SynthesizeToUriListener synthesize = new SynthesizeToUriListener() {
        @Override
        public void onBufferProgress(int i) {

        }

        @Override
        public void onSynthesizeCompleted(String uri, SpeechError speechError) {
            if (speechError == null) {
                logger.info("*************合成成功*************");
                logger.info("合成音频生成路径：" + uri);
            } else {
                logger.error("*************" + speechError.getErrorCode()
                        + "*************");
            }


        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, int arg3, Object obj1, Object obj2) {

            if( SpeechEvent.EVENT_TTS_BUFFER == eventType ){
                logger.info( "onEvent: type="+eventType
                        +", arg1="+arg1
                        +", arg2="+arg2
                        +", arg3="+arg3
                        +", obj2="+(String)obj2 );
                ArrayList<?> bufs = null;
                if( obj1 instanceof ArrayList<?> ){
                    bufs = (ArrayList<?>) obj1;
                }else{
                    logger.info( "onEvent error obj1 is not ArrayList !" );
                }//end of if-else instance of ArrayList

                if( null != bufs ){
                    for( final Object obj : bufs ){
                        if( obj instanceof byte[] ){
                            final byte[] buf = (byte[]) obj;
                            logger.error( "onEvent buf length: "+buf.length );
                        }else{
                            logger.error( "onEvent error element is not byte[] !" );
                        }
                    }//end of for
                }//end of if bufs not null
            }//end of if tts buffer event
        }
    };

    private SynthesizerListener mSynListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
        }

        @Override
        public void onBufferProgress(int progress, int beginPos, int endPos,
                                     String info) {
            logger.info("--onBufferProgress--progress:" + progress
                    + ",beginPos:" + beginPos + ",endPos:" + endPos);
        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int progress, int beginPos, int endPos) {
            logger.info("onSpeakProgress enter progress:" + progress
                    + ",beginPos:" + beginPos + ",endPos:" + endPos);

            logger.info( "onSpeakProgress leave" );
        }

        @Override
        public void onCompleted(SpeechError error) {
            logger.info( "onCompleted enter" );

            logger.info( "onCompleted leave" );
        }


        @Override
        public void onEvent(int eventType, int arg1, int arg2, int arg3, Object obj1, Object obj2) {
            if( SpeechEvent.EVENT_TTS_BUFFER == eventType ){
                logger.info( "onEvent: type="+eventType
                        +", arg1="+arg1
                        +", arg2="+arg2
                        +", arg3="+arg3
                        +", obj2="+(String)obj2 );
                ArrayList<?> bufs = null;
                if( obj1 instanceof ArrayList<?> ){
                    bufs = (ArrayList<?>) obj1;
                }else{
                    logger.info( "onEvent error obj1 is not ArrayList !" );
                }//end of if-else instance of ArrayList

                if( null != bufs ){
                    for( final Object obj : bufs ){
                        if( obj instanceof byte[] ){
                            final byte[] buf = (byte[]) obj;
                            logger.info( "onEvent buf length: "+buf.length );
                        }else{
                            logger.info( "onEvent error element is not byte[] !" );
                        }
                    }//end of for
                }//end of if bufs not null
            }//end of if tts buffer event
            //以下代码用于调试，如果出现问题可以将sid提供给讯飞开发者，用于问题定位排查
			/*else if(SpeechEvent.EVENT_SESSION_ID == eventType) {
				DebugLog.Log("sid=="+(String)obj2);
			}*/
        }
    };

    @RequestMapping("/audiotransf/hello")
    public String testNew(@RequestParam(value = "name",required = false) String name, ModelMap map){

        map.put("msg", "hello," + name);

        return "welcome";
    }


    @RequestMapping("/audiotransf/tts")
    @ResponseBody
    public BaseVo ttsSpeaker(@RequestParam(value = "text", required = false) String text, HttpServletResponse response) {

        SpeechUtility.createUtility( SpeechConstant.APPID +"=6dbfa26f ");

        if (StringUtils.isBlank(text)) {
            return BaseVo.fail("-1","输入内容不能为空！");
        }

        mText = text;

        mTts.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_CLOUD);
        mTts.setParameter(SpeechConstant.VOICE_NAME,"小宇");
        mTts.setParameter(SpeechConstant.BACKGROUND_SOUND,"0");
        mTts.setParameter(SpeechConstant.SPEED,"50");
        mTts.setParameter(SpeechConstant.PITCH,"50");
        mTts.setParameter(SpeechConstant.VOLUME,"50");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, null);
        mTts.setParameter( SpeechConstant.TTS_BUFFER_EVENT, "1" );

//        mTts.startSpeaking( mText, mSynListener );
        UUID preName = UUID.randomUUID();
        String fileName = preName + ".pcm";

        String path = LINUX_STORE_PLACE +fileName;
//        String path = STORE_PLACE +fileName;
        logger.info("wenjianming:" + path);

        try {
            mTts.synthesizeToUri(mText,path,synthesize);
        } catch (Exception e) {
            logger.error("生成音频文件错误",e);
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

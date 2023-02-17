package com.house.audiotransf.controller;


import com.house.audiotransf.constant.BaseVo;
import com.iflytek.cloud.speech.*;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;

@Controller
public class AudioTransController {

    private final static String STORE_PLACE = "C:\\Users\\ckh\\Desktop\\pcm\\";

    // 语音合成对象
    private SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer();

    private final Logger logger = LoggerFactory.getLogger(AudioTransController.class);

    private String mText = "";

    private SynthesizeToUriListener synthesize = new SynthesizeToUriListener() {
        @Override
        public void onBufferProgress(int i) {

        }

        @Override
        public void onSynthesizeCompleted(String s, SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, int i3, Object o, Object o1) {

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
    public BaseVo ttsSpeaker(@RequestParam(value = "text", required = false) String text) {

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
        String fileName = "1.PCM";

        String path = STORE_PLACE +fileName;

        mTts.synthesizeToUri(mText,path,synthesize);



        return BaseVo.succ();
    }


    @RequestMapping("/audiotransf/download")
    public String downloadAudio(HttpServletRequest request, HttpServletResponse response,ModelMap map) {

        String fileName = "1.PCM";
        String path = STORE_PLACE +fileName;

        logger.info("1111111111111111111111111111111111" + path);

        File file  = new File(path);
        if (file.exists()) {
            //设置响应头信息
            response.setHeader("Content-disposition", "attachment;filename="+fileName);//使下载的文件名与原文件名对应

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





        return null;
    }



}

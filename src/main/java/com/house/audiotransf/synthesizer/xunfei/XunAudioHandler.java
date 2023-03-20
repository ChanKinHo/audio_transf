package com.house.audiotransf.synthesizer.xunfei;

import com.house.audiotransf.service.impl.AudioGenerationImpl;
import com.iflytek.cloud.speech.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

@Service
public class XunAudioHandler {

    private final Logger logger = LoggerFactory.getLogger(XunAudioHandler.class);

    // 讯飞语音合成对象
    private final SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer();

    private final Properties props = new Properties();

    @Value("${com.house.hot.path}")
    private String hotPath;

    @Value("${com.house.save.path}")
    private String savePath;

    public String generateAudio(String text) throws Exception {

//        props.load(this.getClass().getResourceAsStream("/hot_config.properties"));
        logger.info("读取配置的热部署文件路径: " + hotPath);
        props.load(new FileInputStream(hotPath));

        String rootPath = props.getProperty("linux_root_path");
        String appId = props.getProperty("xunfei_appId");

        SpeechUtility.createUtility( SpeechConstant.APPID + "=" + appId);

        mTts.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_CLOUD);
        mTts.setParameter(SpeechConstant.VOICE_NAME,"小宇");
        mTts.setParameter(SpeechConstant.BACKGROUND_SOUND,"0");
        mTts.setParameter(SpeechConstant.SPEED,"50");
        mTts.setParameter(SpeechConstant.PITCH,"50");
        mTts.setParameter(SpeechConstant.VOLUME,"50");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, null);
        mTts.setParameter( SpeechConstant.TTS_BUFFER_EVENT, "1" );

        //该方法直接合成并播放
//        mTts.startSpeaking( mText, mSynListener );
        UUID preName = UUID.randomUUID();
        String fileName = preName + ".pcm";

//        String path = rootPath +fileName;
        String path = savePath + fileName;
        logger.info("合成的语音文件路径:" + path);

        try {
            mTts.synthesizeToUri(text,path,synthesize);
        } catch (Exception e) {
            logger.error("生成音频文件错误",e);
            throw e;
        }

        return path;
    }

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

}

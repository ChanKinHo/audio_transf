package com.house.audiotransf.synthesizer.tencent;

import com.house.audiotransf.service.impl.AudioGenerationImpl;
import com.tencent.SpeechClient;
import com.tencent.tts.model.SpeechSynthesisRequest;
import com.tencent.tts.model.SpeechSynthesisResponse;
import com.tencent.tts.service.SpeechSynthesisListener;
import com.tencent.tts.service.SpeechSynthesizer;
import com.tencent.tts.utils.Ttsutils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TencAudioHandler {
    private static final Logger logger = LoggerFactory.getLogger(TencAudioHandler.class);

    private final Properties props = new Properties();

    private static String codec = "pcm";
    private static int sampleRate = 16000;
    private static byte[] datas = new byte[0];

    public String generateAudio(String text) throws Exception{

        //当配置文件在src/main/resource目录下时只能通过次方式读取
        props.load(this.getClass().getResourceAsStream("/hot_config.properties"));

        String appId = props.getProperty("tencent_appId");
        String secretId = props.getProperty("tencent_secretId");
        String secretKey = props.getProperty("tencent_secretKey");

        //创建SpeechSynthesizerClient实例，目前是单例
        SpeechClient client = SpeechClient.newInstance(appId, secretId, secretKey);
        //初始化SpeechSynthesizerRequest，SpeechSynthesizerRequest包含请求参数
        SpeechSynthesisRequest request = SpeechSynthesisRequest.initialize();
        request.setCodec(codec);
        request.setVoiceType(1001);
        //使用客户端client创建语音合成实例
        SpeechSynthesizer speechSynthesizer = client.newSpeechSynthesizer(request, new MySpeechSynthesizerListener());
        //执行语音合成
        speechSynthesizer.synthesis(text);

        return "fine";
    }

    public static void main(String[] args) {
        String appId = "1317245277";
        String secretId = "AKIDd8Ucct5A4rTsBNM6uWt31jNuEQWP4Vhf";
        String secretKey = "rFQN7dvFSRBgxxxskQvFMKBrLbieNtDL";

        //创建SpeechSynthesizerClient实例，目前是单例
        SpeechClient client = SpeechClient.newInstance(appId, secretId, secretKey);
        //初始化SpeechSynthesizerRequest，SpeechSynthesizerRequest包含请求参数
        SpeechSynthesisRequest request = SpeechSynthesisRequest.initialize();
        request.setCodec(codec);
        request.setVoiceType(1003);
        //使用客户端client创建语音合成实例
        SpeechSynthesizer speechSynthesizer = client.newSpeechSynthesizer(request, new MySpeechSynthesizerListener());
        //执行语音合成
        String text = "dog shit";
        speechSynthesizer.synthesis(text);
    }

    public static class MySpeechSynthesizerListener extends SpeechSynthesisListener {

        private final AtomicInteger sessionId = new AtomicInteger(0);

        @Override
        public void onComplete(SpeechSynthesisResponse response) {
            logger.info("onComplete");
            String s = "";
            if (response.getSuccess()) {
                //根据具体的业务选择逻辑处理

                Ttsutils.saveResponseToFile(response.getAudio(),"C:\\Users\\ckh\\Desktop\\pcm\\" + response.getSessionId() + ".pcm");
//                if ("pcm".equals(codec)) {
//                    //pcm 转 wav
//                     s = Ttsutils.responsePcm2Wav(sampleRate, response.getAudio(), response.getSessionId());
//                }

            }
            System.out.println("结束：" + response.getSuccess() + " " + response.getCode()
                    + " " + response.getMessage() + " " + response.getEnd() + " " + s);

        }

        @Override
        public void onMessage(byte[] data) {
            logger.info("onMessage length:" + data.length);
            sessionId.incrementAndGet();

        }

        @Override
        public void onFail(SpeechSynthesisResponse exception) {
            logger.info("onFail");
            logger.error(exception.getCode() + "---" + exception.getMessage());
        }
    }

}

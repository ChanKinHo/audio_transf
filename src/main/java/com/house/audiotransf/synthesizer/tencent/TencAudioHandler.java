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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TencAudioHandler {
    private static final Logger logger = LoggerFactory.getLogger(TencAudioHandler.class);


    private static final Properties props = new Properties();

    private static final String codec = "pcm";

    private static String path = "";

    private static String rootPath = "";

    @Value("${com.house.hot.path}")
    private String hotPath;

    @Value("${com.house.save.path}")
    private String savePath;

    public String generateAudio(String text) throws Exception{

        //当配置文件在src/main/resource目录下时只能通过次方式读取
//        props.load(this.getClass().getResourceAsStream("/hot_config.properties"));
        logger.info("读取配置的热部署文件路径: " + hotPath);
        props.load(new FileInputStream(hotPath));
//        rootPath = props.getProperty("linux_root_path");
        rootPath = savePath;

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

        return path;
    }

    public static class MySpeechSynthesizerListener extends SpeechSynthesisListener {

        private final AtomicInteger sessionId = new AtomicInteger(0);

        @Override
        public void onComplete(SpeechSynthesisResponse response) {
            logger.info("onComplete");

            if (response.getSuccess()) {
                path = rootPath + response.getSessionId() + ".pcm";
                //根据具体的业务选择逻辑处理
                Ttsutils.saveResponseToFile(response.getAudio(),path);

            }
            logger.info("结束：" + response.getSuccess() + " " + response.getCode()
                    + " " + response.getMessage() + " " + response.getEnd() + " " + path);

        }

        @Override
        public void onMessage(byte[] data) {
            logger.info("tencent onMessage length:" + data.length);
            sessionId.incrementAndGet();

        }

        @Override
        public void onFail(SpeechSynthesisResponse exception) {
            logger.error("tencent onFail " + exception.getCode() + "---" + exception.getMessage());
        }
    }


}

package com.house.audiotransftest;


import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechUtility;
import javafx.event.ActionEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.iflytek.cloud.speech.SpeechSynthesizer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application.properties"})
public class AudioTransfDemoTest {

    @Test
    public void demo1(){
        SpeechUtility utility = SpeechUtility.createUtility(SpeechConstant.APPID + "=6dbfa26f ");

    }


}

package com.house.audiotransf.service.impl;

import com.house.audiotransf.constant.ChannelEnum;
import com.house.audiotransf.service.AudioGeneration;
import com.house.audiotransf.synthesizer.tencent.TencAudioHandler;
import com.house.audiotransf.synthesizer.xunfei.XunAudioHandler;
import com.iflytek.cloud.speech.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class AudioGenerationImpl implements AudioGeneration {

    private final Logger logger = LoggerFactory.getLogger(AudioGenerationImpl.class);

    @Autowired
    private TencAudioHandler tencAudioHandler;

    @Autowired
    private XunAudioHandler xunAudioHandler;

    @Override
    public String generateAudio(String text, String channel) throws Exception {

        String path;
        if (ChannelEnum.XUNFEI.getId().equals(channel)) {
            path = xunAudioHandler.generateAudio(text);
        } else if (ChannelEnum.TENCENT.getId().equals(channel)) {
            path = tencAudioHandler.generateAudio(text);
        } else {
            path = "";
        }
        logger.info("generateAudio path " + path);
        return path;
    }
}

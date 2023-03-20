package com.house.audiotransf.constant;

public enum ChannelEnum {
    XUNFEI("讯飞","1"),
    TENCENT("腾讯","2");


    private String name;
    private String id;



    ChannelEnum(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

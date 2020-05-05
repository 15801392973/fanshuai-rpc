package com.fanshuai.domain;

public class ChannelOption {
    //心跳时间间隔
    public static int heartBeat = 5;
    //心跳检测失败多少秒后关闭tcp连接
    public static int heartBeatFailedCloseSeconds = 15;

    //断线重连时间
    public static int tcpCloseRetrySeconds = 1;
}

package com.github.zjiajun.adult.pic;

import com.github.zjiajun.adult.connection.ConnectionRequest;
import com.github.zjiajun.adult.download.DownloadRequest;
import com.github.zjiajun.adult.download.AdultDownload;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.github.zjiajun.adult.connection.AdultConnection.*;
import static com.github.zjiajun.adult.tool.AdultConfig.*;
/**
 * Created by zhujiajun
 * 16/6/26 20:43
 */
public class RosixzTask implements Runnable {

    private final String pageUrl;

    public RosixzTask(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    @Override
    public void run() {
       doWork();
    }

    /**
     * 实际工作
     */
    private void doWork() {
        Map<String, String> detailInfo = getDetailInfoByPageUrl(pageUrl);
        detailInfo.forEach(this::getPicInfoByDetailUrl);
    }

    /**
     * 解析详细url得到实际图片href,确定下载主目录
     * @param detailUrl     详细url
     * @param detailContent 详细说明
     */
    private void getPicInfoByDetailUrl(String detailUrl,String detailContent) {
        String downloadPath = picDownloadPath() + detailContent + File.separator;
        ConnectionRequest request = new ConnectionRequest.Builder()
                .url(detailUrl).userAgent(randomUA())
                .sleep(true).sleepSeconds(5).build();
        Document detailDoc = connect(request);
        Elements detailElements = detailDoc.select(".imglist a[href]");
        detailElements.forEach(e -> downloadDetailImg(e.absUrl("href"),downloadPath));
    }

    /**
     * 下载图片
     * @param detailImgUrl 图片href
     * @param downloadPath 下载主目录
     */
    private void downloadDetailImg(String detailImgUrl,String downloadPath) {
        String detailImgName = detailImgUrl.substring(detailImgUrl.lastIndexOf("/") + 1, detailImgUrl.length());
        DownloadRequest downloadRequest = new DownloadRequest.Builder().url(detailImgUrl).filePath(downloadPath).fileName(detailImgName).build();
        AdultDownload.down2File(downloadRequest);
    }


    /**
     * 获取当前页中的列表详细信息,包含图片详细页url,和图片详细说明
     * @param pageUrl 当前页
     * @return Map[detailUrl->detailContent]
     */
    private Map<String,String> getDetailInfoByPageUrl(String pageUrl) {
        Map<String,String> detailMap = new HashMap<>();
        ConnectionRequest request = new ConnectionRequest.Builder()
                .url(pageUrl).userAgent(randomUA())
                .sleep(true).sleepSeconds(5).build();
        Document pageDoc = connect(request);
        Elements detailElements = pageDoc.select(".photo a[href]");
        detailElements.forEach(e -> {
            String detailUrl = e.absUrl("href");
            String detailContent = e.select("[alt]").attr("alt");
            detailMap.put(detailUrl,detailContent);
        });
        return detailMap;
    }



}

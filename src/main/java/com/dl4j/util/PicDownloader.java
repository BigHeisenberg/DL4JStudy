package com.dl4j.util;


import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * 图片下载
 *
 * @date 2019年9月17日 上午11:06:57
 */
public class PicDownloader {

    /**
     * 专利信息服务平台验证码：
     * http://search.cnipr.com/RandomCode?nocache=1568689373726
     */
    @Test
    public void downloaderPic() {
//        String picUrlStr = "http://search.cnipr.com/RandomCode?nocache=1568689373726";
        String picUrlStr = "http://qyxy.baic.gov.cn/CheckCodeYunSuan?currentTimeMillis=1573466055189&r=0.24243357711089764";
        try {
            int i = 7;
            while (i < 18) {
                ++i;
                System.out.println("正在下载图片:" + i);
                URL picUrl = new URL(picUrlStr);
                InputStream is = picUrl.openStream();
                FileOutputStream fos = new FileOutputStream(new File("F:\\验证码图片\\专利信息服务平台验证码\\01\\" + i + ".jpg"));
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                int len = 0;
                byte[] bte = new byte[2048];
                while ((len = is.read(bte)) > 0) {
                    bos.write(bte, 0, len);
                    bos.flush();
                }
                bos.close();
                is.close();
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.dl4j.util;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 此类用于上传验证码文件 调用  验证码识别服务
 * @date 2019年9月19日 上午10:38:15
 */
public class UploadPic {
	private static Logger logger=LoggerFactory.getLogger(UploadPic.class);
	
	@Test
	public void testUploadImg() {
		String recognitionApi="http://localhost:8080/captchaRecognition";
		File captchaImage=new File("F:\\验证码图片\\专利信息服务平台验证码\\01\\6.jpg");
		String captchaRecognition = captchaRecognition(recognitionApi, captchaImage);
		System.out.println(captchaRecognition);
	}
	
	
	/**
	 * @param recognitionApi http://localhost:8080/captchaRecognition
	 * @param captchaImage 验证码图片
	 * @param webSiteFlag 需要识别的网站标识sh sswy
	 */
	private String captchaRecognition(String recognitionApi,File captchaImage) {
		logger.info("正在进行验证码识别");
		String result=null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost=new HttpPost(recognitionApi);
		try {
			//MultipartEntityBuilder.create()
			//创建一个附件实体
			HttpEntity multipartEntity =MultipartEntityBuilder.create()
					.addBinaryBody("captchaImage", captchaImage)
					//.addTextBody("webSiteFlag", webSiteFlag)
					.build();
			httpPost.setEntity(multipartEntity);
				
			CloseableHttpResponse response = httpClient.execute(httpPost);
			HttpEntity responseEntity = response.getEntity();
			result= EntityUtils.toString(responseEntity, "utf-8");
			
			EntityUtils.consume(responseEntity);
			if (response != null) {
				response.close();
			}
		} catch (Exception e) {
			logger.info("识别验证码时发生异常:{}",e.getMessage());
		}finally {
			httpPost.abort();
		}
		return result;
	}

}

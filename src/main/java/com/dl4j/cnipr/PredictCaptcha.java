package com.dl4j.cnipr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 预测验证码
 * @createTime 2019-02-22 08:30:09
 */
public class PredictCaptcha {
	private static final Logger logger = LoggerFactory.getLogger(PredictCaptcha.class);
	
	private static int height=36;
	private static int width=70;
	private static int channels=1;
	private static int labelLength=4;
	private static boolean caseSensitive=false;
	

	private static String modelPath ="F:\\验证码图片\\专利信息服务平台验证码\\model\\validateCodeCheckModel01.model";
	private static File myModelFile = new File(modelPath);
	//加载网络模型到内存
	private static ComputationGraph myModel = null;
	static {
		try {
			myModel=ModelSerializer.restoreComputationGraph(myModelFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	private static String predictImgPath="F:\\验证码图片\\专利信息服务平台验证码\\01\\10.jpg";
	private static String imagesFolder="F:\\验证码图片\\专利信息服务平台验证码\\01";
	
	public static void main(String[] args) {
		//predictCode(); //预测一张验证码
		predictCodes(); //预测一批验证码
		//从网络上下载一张图片并预测
		//downloadImgAndPredict();
	}
	
	
	/**
	 * 网络下载验证码图片并预测
	 */
	public static void downloadImgAndPredict() {
		try {
			String picUrlStr="http://search.cnipr.com/RandomCode";
			URL picUrl=new URL(picUrlStr);
			InputStream is = picUrl.openStream();
			BufferedImage image = ImageIO.read(is);
			
			//保存原始验证码图片到本地
			//File newFile = new File("F:\\验证码图片\\专利信息服务平台验证码\\01\\" + UUID.randomUUID().toString()+"-old.jpg");
			//ImageIO.write(image, "jpg", newFile);
			
			//灰度化图片
			BufferedImage grayImage=grayImage(image);
			
			//保存灰度化验证码图片到本地
			//File grayNewFile = new File("F:\\验证码图片\\专利信息服务平台验证码\\01\\" + UUID.randomUUID().toString()+"gray-.jpg");
			//ImageIO.write(grayImage, "jpg", grayNewFile);
			
			
			
			try {
				
				String code = modelPredict(myModel, grayImage);
				System.out.println("验证码:"+code);
			} catch (IOException e) {
				logger.info("发生异常");
			}
			if(is!=null) {
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * 预测文件夹下一批验证码
	 */
	public static void predictCodes(){
		File myModelFile = new File(modelPath);
		logger.info("正在读取网络模型");
		try {
			ComputationGraph myModel = ModelSerializer.restoreComputationGraph(myModelFile);
			File imgfolder = new File(imagesFolder);
			File[] listFiles = imgfolder.listFiles();
			for (File img : listFiles) {
				System.out.println(img.getName());
				modelPredict(myModel, img);
			}
		} catch (IOException e) {
			logger.info("发生异常");
		}
	}
	
	
	public static void predictCode(){
		File myModelFile = new File(modelPath);
		logger.info("正在读取网络模型");
		try {
			ComputationGraph myModel = ModelSerializer.restoreComputationGraph(myModelFile);
			modelPredict(myModel, new File(predictImgPath));
		} catch (IOException e) {
			logger.info("发生异常");
		}
	}

	
	/**
	 * 预测
	 * @param model
	 * @throws IOException
	 */
	private static String modelPredict(ComputationGraph model,BufferedImage image) throws IOException {
		NativeImageLoader loader=new NativeImageLoader(height, width, channels);
		INDArray feature = loader.asMatrix(image); // 提取图片特征
		feature.muli(1.0 / 255.0); 
		INDArray[] output = model.output(feature); 
		
        String peLabel = ""; //每张图的预测标签
        INDArray preOutput = null;
        for (int digit = 0; digit < labelLength; digit++) {
            preOutput = output[digit].getRow(0);
            peLabel +=intToStringCode( Nd4j.argMax(preOutput, 1).getInt(0),caseSensitive);
        }
        return peLabel;
	}
	
	
	
	/**
	 * 预测
	 * @param model
	 * @throws IOException
	 */
	private static void modelPredict(ComputationGraph model,File image) throws IOException {
		NativeImageLoader loader=new NativeImageLoader(height, width, channels);
		INDArray feature = loader.asMatrix(grayImage(image)); // 提取图片特征
		feature.muli(1.0 / 255.0); 
		INDArray[] output = model.output(feature); 
		
        String peLabel = ""; //每张图的预测标签
        INDArray preOutput = null;
        for (int digit = 0; digit < labelLength; digit++) {
            preOutput = output[digit].getRow(0);
            peLabel +=intToStringCode( Nd4j.argMax(preOutput, 1).getInt(0),caseSensitive);
        }
        System.out.println(peLabel);
	}
	
	
	
	/**
	 * 灰度化验证码图片
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage grayImage(BufferedImage image) throws IOException {
		int width = image.getWidth();
		int height = image.getHeight();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				
				//处理边界转为白色，后边容易转为黑色
				if(x<=3 || x>=68 || y<=3 || y>=34) { 
					image.setRGB(x, y, Color.WHITE.getRGB()); 
				}
				
				Color color = new Color(image.getRGB(x, y));
				int num = color.getRed() + color.getGreen() + color.getBlue();
				if (num < 300) {
					image.setRGB(x, y, Color.WHITE.getRGB()); //验证码转白色
				}else {
					image.setRGB(x, y, Color.BLACK.getRGB()); //背景转黑色
				}
			}
		}
		return image;
	}
	
	
	
	
	
	/**
	 * 灰度化验证码图片
	 * @param img
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage grayImage(File img) throws IOException {
		BufferedImage image = ImageIO.read(img);
		int width = image.getWidth();
		int height = image.getHeight();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				
				//处理边界转为白色，后边容易转为黑色
				if(x<=3 || x>=68 || y<=3 || y>=34) { 
					image.setRGB(x, y, Color.WHITE.getRGB()); 
				}
				
				Color color = new Color(image.getRGB(x, y));
				int num = color.getRed() + color.getGreen() + color.getBlue();
				if (num < 300) {
					image.setRGB(x, y, Color.WHITE.getRGB()); //验证码转白色
				}else {
					image.setRGB(x, y, Color.BLACK.getRGB()); //背景转黑色
				}
				
			}
		}
		return image;
	}
	
	/**
	 * 灰度化验证码图片
	 * @param codeImage
	 */
	public static BufferedImage grayCode(File codeImage) {
		try {
			BufferedImage image = ImageIO.read(codeImage);
			int width = image.getWidth();
			int height = image.getHeight();
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					Color color = new Color(image.getRGB(i, j));
					int num = color.getRed() + color.getGreen() + color.getBlue();
					if (num > 630) {
						image.setRGB(i, j, Color.WHITE.getRGB());
					}else {
						image.setRGB(i, j, Color.BLACK.getRGB());
					}
				}
			}
			return image;
		} catch (IOException e) {
			logger.info("发生异常:{}",e.getMessage());
		}
		return null;
	}
	
	
	/**
	 * 
	 * @param peNum
	 * @return
	 */
	public static String intToStringCode(int peIndex,boolean caseSensitive) {
		String code="";
		if(peIndex<10) {
			char c=(char) (peIndex+48);
			code=String.valueOf(c);
		}else if(peIndex>=10 && peIndex<=35) {
			char c=(char) (peIndex+87);
			code=String.valueOf(c);
		}else {
			char c=(char) (peIndex+29);
			code=String.valueOf(c);
		}
		if(!caseSensitive) {
			code=code.toLowerCase();
		}
		return code;
	}
	
}

package com.dl4j.util;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


/**
 * 此类中方法用于灰度化图片
 * @date 2019年9月17日 下午2:39:24
 */
public class ImgInit {
	
	/**
	 * 字母白色 背景黑色
	 */
	@Test
	public void test05() {
		File imgFolder = new File("F:\\验证码图片\\专利信息服务平台验证码\\已打码");
		try {
			grayImage03(imgFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void grayImage03(File imgFolder) throws IOException {
		File[] images = imgFolder.listFiles();
		for (File img : images) {
			String imgName = img.getName();
			BufferedImage image = ImageIO.read(img);

			int width = image.getWidth();
			int height = image.getHeight();

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					
					//处理边界转为白色，后边容易转为黑色
					if(x<=3 || x>=68 || y<=3 || y>=34) { 
						image.setRGB(x, y, Color.WHITE.getRGB()); //背景转黑色
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
			File newFile = new File("F:\\验证码图片\\专利信息服务平台验证码\\灰度图\\train\\" + imgName);
			ImageIO.write(image, "jpg", newFile);
		}
	}

	
	
	/*
	 * 灰度化
	 * 不用此方法
	 */
	@Test
	public void test02() {
		File imgFolder = new File("F:\\验证码图片\\test\\01");
		try {
			grayImage(imgFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 不用此方法
	 * @param imgFolder
	 * @throws IOException
	 */
	public void grayImage(File imgFolder) throws IOException {
		File[] images = imgFolder.listFiles();
		for (File img : images) {
			String imgName = img.getName();
			BufferedImage image = ImageIO.read(img);

			int width = image.getWidth();
			int height = image.getHeight();

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int rgb = image.getRGB(x, y);
					int red = (rgb >> 16) & 0xff;
					int green = (rgb >> 8) & 0xff;
					int blue = rgb & 0xff;
					
					int total = (red + green + blue);
					if (total >450) {
						image.setRGB(x, y, new Color(0, 0, 0).getRGB());
					} else {
						image.setRGB(x, y, new Color(254, 254, 254).getRGB());
					}
				}
			}

			File newFile = new File("F:\\验证码图片\\test\\grayimg\\" + imgName);
			ImageIO.write(image, "jpg", newFile);
		}
	}

	
	// 二值化图片
	@Test
	public void test04() {
		try {
			binaryImage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void binaryImage() throws IOException {
		File file = new File("D:\\DL4J\\data\\code01\\graytrain\\00z9.jpg");
		BufferedImage image = ImageIO.read(file);
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
		File newFile = new File("D:\\DL4J\\data\\code01\\graytrain\\00z9.jpg");
		ImageIO.write(image, "jpg", newFile);
	}

	@Test
	public void test03() {
		File imgFolder = new File("C:\\Users\\Desktop\\graycode");
		try {
			translateImage(imgFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 效果不好
	 * 不推荐使用
	 * @param imgFolder
	 * @throws IOException
	 */
	private void translateImage(File imgFolder) throws IOException {
		File[] images = imgFolder.listFiles();
		for (File img : images) {
			String imgName = img.getName();
			BufferedImage image = ImageIO.read(img);
			int width = image.getWidth();
			int height = image.getHeight();
			BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					int val = image.getRGB(i, j);
					int red = (val >> 16) & 0xff;
					int green = (val >> 8) & 0xff;
					int blue = val & 0xff;

					red = 255 - red;
					green = 255 - green;
					blue = 255 - blue;
					int alpha = 0xff;
					if ((red + green + blue) / 3 >= 0xff) {
						alpha = 0x00;
					}
					int pixel = (alpha << 24) | (red << 16) | (green << 8) | (blue);
					target.setRGB(i, j, pixel);
				}
			}
			File newFile = new File("C:\\Users\\Desktop\\graycode2\\" + imgName);
			ImageIO.write(target, "jpg", newFile);
		}
	}

	 /*
	  * https://www.cnblogs.com/a8167270/p/5103395.html
     * 图片缩放,w，h为缩放的目标宽度和高度
     * src为源文件目录，dest为缩放后保存目录
     */
    public static void zoomImage(String src,String dest,int w,int h) throws Exception {
        
        double wr=0,hr=0;
        File srcFile = new File(src);
        File destFile = new File(dest);

        BufferedImage bufImg = ImageIO.read(srcFile); //读取图片
        Image Itemp = bufImg.getScaledInstance(w, h, bufImg.SCALE_SMOOTH);//设置缩放目标图片模板
        
        wr=w*1.0/bufImg.getWidth();     //获取缩放比例
        hr=h*1.0 / bufImg.getHeight();

        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
        Itemp = ato.filter(bufImg, null);
        try {
            ImageIO.write((BufferedImage) Itemp,dest.substring(dest.lastIndexOf(".")+1), destFile); //写入缩减后的图片
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    
    /**
     * 注意:缩放后图片质量会变差
     * @throws Exception
     */
    @Test
    public void toScalaImage() throws Exception {
    	Double rate=0.6;
    	String imgageType="bmp";
    	String imageFolderStr="D:\\DL4J\\data\\SSWYBJ\\susongwuyouBJ\\init\\";
    	File imageFolder=new File(imageFolderStr);
    	File[] images = imageFolder.listFiles();
		for (File srcImage : images) {
			zoomImage(srcImage, new File(imageFolderStr+srcImage.getName()), rate, imgageType);
		}
    }
    
   
    public static void zoomImage(File srcImage,File destImage,Double rate,String imgageType) throws Exception {
        BufferedImage bufImg = ImageIO.read(srcImage);
        Image Itemp = bufImg.getScaledInstance(bufImg.getWidth(), bufImg.getHeight(), bufImg.SCALE_SMOOTH);
            
        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(rate, rate), null);
        Itemp = ato.filter(bufImg, null);
        try {
            ImageIO.write((BufferedImage) Itemp,imgageType,destImage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
}

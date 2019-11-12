package com.dl4j.cnipr;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.transform.ImageTransform;
import org.nd4j.linalg.api.concurrency.AffinityManager;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.MultiDataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 多数据加载
 * @createTime 2019-02-22 10:00:24
 */
public class MyMulRecordDataLoader extends NativeImageLoader implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(MyMulRecordDataLoader.class);

	private int height;
	private int width;
	private int channels;
	private int labelLength;
	private boolean caseSensitive=false;
	private File fullDir = null; //所有训练图片文件夹或者所有测试文件文件夹
	private Iterator<File> imageIterator; //全局的imageIterator能记录自己的下标
	private int numExample = 0; // 图片数
	private String dataSetType;
	public MyMulRecordDataLoader(int height, int width, int channels,int labelLength,boolean caseSensitive,ImageTransform imageTransform, String captchaImageDirPath,String dataSetType) {
		super(height, width, channels, imageTransform);
		this.height = height;
		this.width = width;
		this.channels = channels;
		this.labelLength=labelLength;
		this.caseSensitive=caseSensitive;
		this.dataSetType=dataSetType;
		this.fullDir = new File(captchaImageDirPath, dataSetType);
		loadImage(dataSetType);
	}

	/**
	 * 加载图片并实例化图片迭代器
	 */
	protected void loadImage(String dataSetType) {
		try {
			// 搜索匹配的文件
			List<File> dataFiles = (List<File>) FileUtils.listFiles(fullDir, new String[] { "jpg","bmp"}, true);
			Collections.shuffle(dataFiles); // 打乱顺序
			imageIterator = dataFiles.iterator();
			numExample = dataFiles.size();
			if(dataSetType.contains("train")) {
				logger.info("训练集数据总数量:{}",numExample);
			}else {
				logger.info("测试集数据总数量:{}",numExample);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 把batchSize张原始图片和其标签转换为特征数据
	 * @param batchSize 每批验证码大小
	 * @param labelLength 标签长度
	 * @param caseSensitive 大小写敏感
	 * @return
	 * @throws IOException
	 */
	public MultiDataSet convertDataSet(int batchSize) throws Exception{
		int index = 0; //转化第几张图片
		INDArray[] featuresMask = null;
		INDArray[] labelMask = null;
		
		List<MultiDataSet> multiDataSetList = new ArrayList<>();
		while (index < batchSize && imageIterator.hasNext()) {
			File image = imageIterator.next(); 
			String imageName =image.getName().replace(".jpg", "");
			String[] imageNames = imageName.split("");// 切割图片名提取验证码标签
			INDArray feature = asMatrix(image); // 提取图片特征
			// 就地标量乘法 把数字转为0-1之间
			// 注意这里一定要有小数点!!!!!
			feature.muli(1.0 / 255.0); 
			
			INDArray[] features = new INDArray[] { feature };
			INDArray[] labels = new INDArray[labelLength];
			
			//此方法将给定的INDarray传播到指定位置
			Nd4j.getAffinityManager().ensureLocation(feature, AffinityManager.Location.DEVICE);
			if (imageNames.length != labelLength ) {
				logger.warn("文件名错误:{}",imageName);
				continue; // 打标签错误的数据略过
			}
			for (int i = 0; i < labelLength; i++) {
				if(caseSensitive) {
					/*
					 * char值码 
					 * 		0~9 48~57
					 * 		a~z 97~122
					 *      A~Z 65~90
					 * 	自定义标签向量
					 * 		0~9 0~9
					 * 		a~z	10~35
					 * 		A~z	36~61
					 */
					char charVal = imageNames[i].charAt(0);
					if(charVal>=48 && charVal<=57) { //数字 0:0 9:9
						labels[i] = Nd4j.zeros(1, 62).putScalar(new int[] { 0, (int) charVal-48}, 1);
					}else if(charVal>=97 && charVal<=122){ //小写字母 a:10 z:35
						labels[i] = Nd4j.zeros(1, 62).putScalar(new int[] { 0, (int) charVal-87 }, 1);
					}else {//大写字母 A:36 Z:61
						labels[i] = Nd4j.zeros(1, 62).putScalar(new int[] { 0, (int)(charVal-29) }, 1);
					}
				}else {
					char charVal = imageNames[i].toLowerCase().charAt(0);
					//0:0 1:1 2:2 .... a:10 b:11 ... z:35
					if (charVal>=48 && charVal<=57) { //数字
						labels[i] = Nd4j.zeros(1, 36).putScalar(new int[] { 0, (int) charVal-48}, 1);
					} else if(charVal>=97 && charVal<=122){ //小写字母
						labels[i] = Nd4j.zeros(1, 36).putScalar(new int[] { 0, (int) charVal-87 }, 1);
					}
				}
			}
			multiDataSetList.add(new MultiDataSet(features, labels, featuresMask, labelMask));
			index++;
		}
		MultiDataSet reuslt = MultiDataSet.merge(multiDataSetList);
		return reuslt;
	}

	public MultiDataSet next(int batchSize) {
		try {
			MultiDataSet result = convertDataSet(batchSize);
			return result;
		} catch (Exception e) {
			logger.error("发生异常:{}",e.getMessage());
		}
		return null;
	}
	
	public void reLoadImage() {
		loadImage(dataSetType);
	}
	public int totalExamples() {
		return numExample;
	}
}

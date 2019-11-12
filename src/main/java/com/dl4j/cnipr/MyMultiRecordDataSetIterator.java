package com.dl4j.cnipr;

import org.datavec.image.transform.ImageTransform;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.MultiDataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;

public class MyMultiRecordDataSetIterator implements MultiDataSetIterator {
	private static final long serialVersionUID = -8193487759413204967L;
	
	private int batchSize=0; //每批次数据大小
	private int index=0; //迭代器维护自身的index，用于记录迭代到第几张图
	private int numExample=0; //图片总数
	
	private MyMulRecordDataLoader loader;
	private MultiDataSetPreProcessor preProcessor;
	
	public MyMultiRecordDataSetIterator(int height, int width, int channels,int labelLength,boolean caseSensitive,int batchSize,ImageTransform imageTransform,String captchaImageDirPath,String dataSetType) {
		this.batchSize=batchSize;
		loader=new MyMulRecordDataLoader(height, width, channels,labelLength,caseSensitive,imageTransform, captchaImageDirPath, dataSetType);
		numExample=loader.totalExamples();
	}
	
	@Override
	public boolean hasNext() {
		if(index<numExample) {
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	public MultiDataSet next(int batchSize) {
		index+=batchSize;
		MultiDataSet mds=loader.next(batchSize); //每次获取batchSize张图片
		if(preProcessor!=null) {
			 preProcessor.preProcess(mds);
		}
		return mds;
	}
	
	@Override
	public MultiDataSet next() {
		return next(batchSize);
	}

	

	@Override
	public void setPreProcessor(MultiDataSetPreProcessor preProcessor) {
		this.preProcessor=preProcessor;
	}

	@Override
	public MultiDataSetPreProcessor getPreProcessor() {
		return preProcessor;
	}

	@Override
	public boolean resetSupported() {
		return true;
	}

	@Override
	public boolean asyncSupported() {
		return true;
	}

	@Override
	public void reset() {
		index=0;
		loader.reLoadImage();
	}

}

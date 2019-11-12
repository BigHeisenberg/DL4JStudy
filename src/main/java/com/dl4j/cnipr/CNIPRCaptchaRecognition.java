package com.dl4j.cnipr;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.PoolingType;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 专利信息服务平台验证码识别
 * 
 * 利用计算图功能构建复杂网络实现 验证码识别 
 * https://deeplearning4j.org/cn/compgraph
 * 
 * @createTime 2019-02-22 08:30:09
 */
public class CNIPRCaptchaRecognition {
	private static final Logger logger = LoggerFactory.getLogger(CNIPRCaptchaRecognition.class);

	private static int seed = 123;
	private static int epochs = 100; 				//最大训练代数，超过此代数停止训练
	private static int batchSize = 32;
	private static int expect_correct_rate=98; 		//期望正确率，达到次正确率停止训练
	
	
	private static int height=36;					//图片高度
	private static int width=70;					//图片宽度
	private static int channels=1;					//图片通道数 灰度图1
	private static int labelLength=4;				//预测验证码个数
	private static boolean caseSensitive=false;		//大小写敏感
	

	
	private static String modelPath = "F:\\验证码图片\\专利信息服务平台验证码\\model\\validateCodeCheckModel01.model";
	
	private static String dirPath="F:\\验证码图片\\专利信息服务平台验证码\\灰度图";
	private static String trainImgType="train";
	private static String testImgType="test";
	
	private static DecimalFormat df = new DecimalFormat("#.00");
	
	public static void startRrainModel(int expectCorrectRate) throws IOException {
		expect_correct_rate=expectCorrectRate;
		File myModelFile = new File(modelPath);
		boolean modelExists = myModelFile.exists();
		ComputationGraph myModel=null;
		if(modelExists) {
			logger.info("正在读取网络模型");
			//测试
			myModel=ModelSerializer.restoreComputationGraph(myModelFile);
		}else {
			logger.info("正在创建网络模型");
			myModel = createModel01();
		}
		
		//monitor the model score
		//UIServer uiServer = UIServer.getInstance();
		StatsStorage statsStorage = new InMemoryStatsStorage();
		//uiServer.attach(statsStorage);
		myModel.setListeners(new ScoreIterationListener(10), new StatsListener(statsStorage));
		
		//训练集
		MyMultiRecordDataSetIterator trainMulIterator = new MyMultiRecordDataSetIterator(height,width,channels,labelLength,caseSensitive,batchSize,null,dirPath,trainImgType);
		//测试集
		MyMultiRecordDataSetIterator testMulIterator = new MyMultiRecordDataSetIterator(height,width,channels,labelLength,caseSensitive,batchSize,null,dirPath,testImgType);
		
		int i=1;
		while(true) {
			if(i>epochs) break;
			
			logger.info("=============Epochs {}===============", i);
			myModel.fit(trainMulIterator);
			//存储模型到本地磁盘
			ModelSerializer.writeModel(myModel, modelPath, true);
			modelPredict(myModel, testMulIterator);
			
			i++;
		}
	}
	

	/**
	 * 训练集多，nOut参数可以配置小点
	 * @return
	 */
	private static ComputationGraph createModel01() {
	      ComputationGraphConfiguration config = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
            .l2(1e-3)
            .updater(new Adam(1e-3))
            .weightInit( WeightInit.XAVIER_UNIFORM)
            .graphBuilder()
            .addInputs("trainFeatures")
            .setInputTypes(InputType.convolutional(height, width, channels))
            .setOutputs("out1", "out2", "out3", "out4")
            .addLayer("cnn1",  new ConvolutionLayer.Builder(new int[]{2, 2}, new int[]{1, 1}, new int[]{0, 0})
                .nIn(channels) //卷积层，这是输入通道
                .nOut(32)
                .activation( Activation.RELU).build(), "trainFeatures")
            .addLayer("maxpool1",  new SubsamplingLayer.Builder(PoolingType.MAX, new int[]{2,2}, new int[]{2, 2}, new int[]{0, 0})
                .build(), "cnn1")
           .addLayer("cnn2",  new ConvolutionLayer.Builder(new int[]{2, 2}, new int[]{1, 1}, new int[]{0, 0})
                .nOut(64).activation( Activation.RELU).build(), "maxpool1")
            .addLayer("maxpool2",  new SubsamplingLayer.Builder(PoolingType.MAX, new int[]{2,2}, new int[]{2, 2}, new int[]{0, 0})
                .build(), "cnn2")
            .addLayer("cnn3",  new ConvolutionLayer.Builder(new int[]{2, 2}, new int[]{1, 1}, new int[]{0, 0})
                .nOut(128)
                .activation( Activation.RELU).build(), "maxpool2")
            .addLayer("maxpool3",  new SubsamplingLayer.Builder(PoolingType.MAX, new int[]{2,2}, new int[]{2, 2}, new int[]{0, 0})
                .build(), "cnn3")
            .addLayer("ffn0",  new DenseLayer.Builder()
          	  .nOut(512) //3072
                .build(), "maxpool3")
            .addLayer("ffn1",  new DenseLayer.Builder()
          	  .nOut(512)//3072
                .build(), "ffn0")
            .addLayer("out1", new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(36).activation(Activation.SOFTMAX).build(), "ffn1") //36表示36种可能 26个字母+10个数字
            .addLayer("out2", new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(36).activation(Activation.SOFTMAX).build(), "ffn1")
            .addLayer("out3", new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(36).activation(Activation.SOFTMAX).build(), "ffn1")
            .addLayer("out4", new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(36).activation(Activation.SOFTMAX).build(), "ffn1")
            .build();

        ComputationGraph model = new ComputationGraph(config);
        model.init();
        return model;
	}

	
	
	/**
	 * 使用测试集进行 模型预测，计算正确率
	 * @param model
	 * @param iterator
	 */
	private static void modelPredict(ComputationGraph model,MyMultiRecordDataSetIterator iterator) {
		int sumCount=0;
		int correctCount=0;
		
		while(iterator.hasNext()) {
			MultiDataSet mds = iterator.next();//获取测试图片集
			INDArray[] output = model.output(mds.getFeatures()); //使用训练好的模型获取测试图片集的预测输出
			INDArray[] labels = mds.getLabels(); //获取测试图片集标签特征
			
			//计算本批次有多少数据
			int totalNum = output[0].rows();
            for (int dataIndex = 0;  dataIndex < totalNum; dataIndex++) {
                String reLabel = ""; //每张图的真实标签
                String peLabel = ""; //每张图的预测标签
                INDArray preOutput = null;
                INDArray realLabel = null; 
                for (int digit = 0; digit < labelLength; digit++) {
                	realLabel = labels[digit].getRow(dataIndex);
                    reLabel +=intToStringCode( Nd4j.argMax(realLabel, 1).getInt(0),caseSensitive);
                    
                    preOutput = output[digit].getRow(dataIndex);
                    peLabel +=intToStringCode( Nd4j.argMax(preOutput, 1).getInt(0),caseSensitive);
                }
                sumCount ++;
            	
                
                if (peLabel.equals(reLabel)) {
                	//logger.info("验证码真实值:{}\t预测值:{}\t预测结果:{}",reLabel,peLabel,"√");
                    correctCount++;
                }else {
                	logger.info("验证码真实值:{}\t预测值:{}\t预测结果:{}",reLabel,peLabel,"×");
                }
            }
		}
		iterator.reset();
		
		Double totalAccuracyRate=(correctCount/(double)sumCount)*100;
		logger.info("测试集总数:{},预测正确数:{},准确率:{}%",sumCount,correctCount,df.format(totalAccuracyRate));
		
		if(totalAccuracyRate>=expect_correct_rate) {
			logger.info("预测符合预期,结束训练!");
			System.exit(0);
		}
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

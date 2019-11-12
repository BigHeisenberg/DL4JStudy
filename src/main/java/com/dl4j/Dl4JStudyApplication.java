package com.dl4j;

import com.dl4j.cnipr.CNIPRCaptchaRecognition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Dl4JStudyApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(Dl4JStudyApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		/*if(args.length==0) {
			System.out.println("请输入期望正确率!");
			System.exit(0);
		}
		int expectCorrectRate = NumberUtils.toInt(args[0]);*/
		int expectCorrectRate=100;
		CNIPRCaptchaRecognition.startRrainModel(expectCorrectRate);
	}
}

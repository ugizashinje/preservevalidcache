package org.ugizashinje.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateDropFileJob implements Runnable{

	private Logger log = LoggerFactory.getLogger(CreateDropFileJob.class);
	
	@Autowired 
	String fileName;

	Path path = Paths.get("file.tmp");
	
	
	
	@Override
	public void run() {
		try {
			Files.write(path, "FIRST".getBytes(), StandardOpenOption.CREATE_NEW);
			log.info("First created " + fileName + " !");
			Thread.sleep(1000);
			Files.delete(path);
			log.info("First deleted ");
			Thread.sleep(1000);
			Files.write(path, "SECOND".getBytes(), StandardOpenOption.CREATE_NEW);
			log.info("Second created ");		
			Thread.sleep(1000);
			Files.delete(path);
			log.info("Second deleted ");
			Thread.sleep(1000);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}

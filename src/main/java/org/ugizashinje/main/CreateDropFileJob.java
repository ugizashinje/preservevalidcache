package org.ugizashinje.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Autowired;

public class CreateDropFileJob implements Runnable{

	@Autowired 
	String fileName;

	Path path = Paths.get("file.tmp");
	
	
	
	@Override
	public void run() {
		try {
			Files.write(path, "FIRST".getBytes(), StandardOpenOption.CREATE_NEW);
			System.out.println("First created " + fileName + " !");
			Thread.sleep(1000);
			Files.delete(path);
			System.out.println("First deleted ");
			Thread.sleep(1000);
			Files.write(path, "SECOND".getBytes(), StandardOpenOption.CREATE_NEW);
			System.out.println("Second created ");		
			Thread.sleep(1000);
			Files.delete(path);
			System.out.println("Second deleted ");
			Thread.sleep(1000);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}

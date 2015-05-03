package org.ugizashinje.main;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	CachedService cachedService;

	public void run() {
		System.out.println("Main started");
		int i = 0;
		try {
			while (i++ < 18) {
				Thread.sleep(300);
				System.out.println("service call " + cachedService.getContent("xxxxx"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		Main main = new Main();
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"applicationContext.xml");
		main.cachedService = (CachedService) context.getBean("cachedService");
		(new Thread(new CreateDropFileJob())).start();
		main.run();

	}

}

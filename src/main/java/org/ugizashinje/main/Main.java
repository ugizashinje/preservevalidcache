package org.ugizashinje.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main implements  Runnable {

	private Logger log = LoggerFactory.getLogger(Main.class);
	CachedService cachedService;

	@Override
	public void run() {
		log.info("Main started");
		int i = 0;
		try {
			while (i++ < 18) {
				Thread.sleep(300);
				log.info("service call " + cachedService.getContent("someKey"));
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
		(new Thread(new CreateDropFileJob(),"file")).start();
		(new Thread(main,"main")).start();


	}

}

package org.ugizashinje.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;


@Configuration
public class EventHandleListenerConf {
	private Logger log = LoggerFactory.getLogger(EventHandleListenerConf.class);

	@Bean
	public ApplicationListener<ContextStartedEvent> startEventHandler(){
		return new ApplicationListener<ContextStartedEvent>() {
			@Override
			public void onApplicationEvent(ContextStartedEvent event) {
				log.info("Event : " + event);
			}
		};
	}
}

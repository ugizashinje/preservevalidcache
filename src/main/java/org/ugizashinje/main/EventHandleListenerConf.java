package org.ugizashinje.main;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;

@Configuration
public class EventHandleListenerConf {
	@Bean
	public ApplicationListener<ContextStartedEvent> startEventHandler(){
		return new ApplicationListener<ContextStartedEvent>() {
			@Override
			public void onApplicationEvent(ContextStartedEvent event) {
				System.out.println("Event : " + event);
			}
		};
	}
}

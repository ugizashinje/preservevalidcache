package org.ugizashinje.main;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CachedService {

	private Logger log = LoggerFactory.getLogger(CachedService.class);;
	Path path = Paths.get("file.tmp");

	@PreserveValidCache(value = "simple", key = "#someKey")
	public String getContent(String someKey) throws Exception{

		    String line =Files.readAllLines(path, Charset.forName("UTF-8")).get(0);
			log.info("Called caching method : " + line);
			return  line;
	}
}

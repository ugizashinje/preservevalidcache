package org.ugizashinje.main;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PreserveValidCacheAspect {

	private Logger log = LoggerFactory.getLogger(PreserveValidCache.class);

	private class Entry {
		public long timeStamp;
		public Object value;

		public Entry(Object object) {
			timeStamp = System.currentTimeMillis();
			this.value = object;
		}

		public boolean isExpired() {
			if (System.currentTimeMillis() > timeStamp + timeout) {
				timeStamp += timeout;
				return true;
			} else
				return false;
		}

		public Object getValue() {
			return this.value;
		}
		public void touch(){
			this.timeStamp += timeout;
		}
	}

	public long timeout = 1000;

	public Map<String, Map<String, Entry>> cacheMap = new ConcurrentHashMap<String, Map<String, Entry>>();

	@Around("@annotation(preserveValidCache)")
	public Object doBasicProfiling(ProceedingJoinPoint pjp,
			PreserveValidCache preserveValidCache) throws Throwable {
		
		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(preserveValidCache.key());
		EvaluationContext spelContext = new StandardEvaluationContext();
		String argNames[] = ((MethodSignature) pjp.getSignature())
				.getParameterNames();

		for (int i = 0; i < argNames.length; i++)
		{
			spelContext.setVariable(argNames[i], pjp.getArgs()[i]);
		}

		log.info("looking for cache with key " + exp.getValue(spelContext));
		Map<String, Entry> cache = cacheMap.get(preserveValidCache.value());
		if (cache == null){
			cache = new ConcurrentHashMap<String, Entry>();
			cacheMap.put(preserveValidCache.value(),cache );
		}

		String key = (String) exp.getValue(spelContext);
		Entry entry = cache.get(key);
		Object retVal = null;
		if (entry == null  || entry.isExpired()) {
			try {
				retVal = pjp.proceed();
				if (retVal != null){
				cache.put(key, new Entry(retVal));
				} else
					cache.get(key).touch();
			} catch (Exception e) {
				log.info(pjp.getSignature() + " throwed " + e);
			}
			if (retVal == null) {
				retVal = cache.get(key).getValue();
			}
		} else {
			return entry.getValue();
		}
		return retVal;
	}

}

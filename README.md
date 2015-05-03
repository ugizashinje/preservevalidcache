# What is this
short story about this code 

### plot

### action

We were runing services that often needed to read some text files, do some logic on this data
and return response. Not that we were super stupid, business reqeirement demanded it. Apps generaly 
need external services and data which are time expensive, but sometimes that price is not very obvious.
 Since spring 3.1 there is mighty @Cacheable annotation that twists your calls to cache fetches and 
 improves your performace by order of magintude instantly. Actualy it is sooo cooool that you take it 
 for granted and use if for everything and you want it to work in every situation. I mean file is loaded, 
 content processed and put to cache for 10 minutes, and somebody delete it by mistake after 2 minutes,
your method will for next 8 minutes return good stuff, how cool is that! I remember me, spolied brat, saying 
minutes are short, i want this to last, i want this cache to hold last valid cache while i am playing with configuration files.
I want to be able mess things up and service to continue work. Sounds pretty sane right. If value can stay in 
cache for 10 minutes why would not it stand until config files are fixed? what is problem with that? Problem is in cache,
fundamentaly. Cache is small portion of memory that serves frequently requested data. Not any data, not any time. Hour ago 
some data was frequent, now it is not and must be expeled to make room for data frequent at this very moment. This is in 
coalision with requerements. I want data to be stored for unknown time until it is requested again, then it should return
new value or last valid state. Cache won't do it, it will expell it after some time, or in case of permenent cache it will 
never try to load value from old file. So what should i do? Ditch ehCache just like that? What about logic? Some methods 
create cache keys from arguments, do it your own way. I was looking for solutions on web, few posts i've found related to this
problem looked more like carbonara or amatriciana then explanation how to solve my problem. First i tried to use cacheManager for this,
but it was dead end. Eviction event is fired only when conditions are met, entry may reside in memory indefinety, very unreliable. 

// TOD0 provide code for ehCache manager 

I was running in circles for while, it did cross my mind to implement it on my own but it seemed that is such big task. 
Eventualy i said if i don't make in day I will give it up. Actualy it was quite easy, just one simple annotation with
same fields like Cacheable and simple Aspect around it. 

first you have to enable in application context.

'''
<aop:aspectj-autoproxy />
'''

then define your Annontation

'''java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PreserveValidCache {
	public String value();
	public String 	key();
}
'''


Keep your fields same, so you can easely move from @Cacheable to @PreserveValidCache.

'''java

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

		for (int i = 0; i < argNames.length; i++) {
			spelContext.setVariable(argNames[i], pjp.getArgs()[i]);
		}

		System.out.println("From annotation " + exp.getValue(spelContext));
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
				System.out.println("Exception catched within PreserveValidCacheAspect from " + pjp.getSignature());
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

'''

Thing with this class is that it needs debug information during runtime, because we need to evaluate cache key after service call. In my case 
methods did have complex arguments 
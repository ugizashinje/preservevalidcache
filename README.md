### How to return last valid ehcache entry? Spring @Cacheable can be very usefull, improves performance but does not protect from exceptions in underlying service. If you just had it in cache and service throws exception, it is natural to return cache last state ... or maybe not.
 
   We were runing services that often needed to read some text files, do some logic on this data
and return response. Not a super happy situation, we were limited by the other end. Apps generaly 
need expensive external services and data. This situation is common, just performance problem in our case is very obvious.
Since spring 3.1 there is mighty @Cacheable annotation that twists your service calls to cache fetches and 
improves your performance by order of magintude instantly. Actualy it is so cool that you take it for hammer 
and whole world looks like a nails. I mean file is loaded, content processed and put to cache 
for 10 minutes from where it is served instantly without computation. How cool it that?! This 
approach some hidden benefits. If file is corrupted your service will return valid cache value for some time. 
  I remember me, spolied brat, saying minutes are short, I want this to last! I want this cache to hold last 
valid state while I am messing with configuration files. Sounds pretty sane, right? If value can stay 
in cache for 10 minutes why would not it stand until config files are fixed? What is problem with that? 
Problem is in cache, fundamentaly. The cache is a small portion of memory that serves frequently requested data. 
Not any data, not any time. Hour ago some data was frequent, now it is not and must be evicted to make room 
for data frequent at this very moment. This is in coalision with requerements. I want data to be stored for 
unknown time until it is requested again, then it should return new value or last valid state. Cache won't do it, 
it will expell it after some time, or in case of permenent cache it will never try to load value from old file.
You could register eviction event handler, but that is not milk and honey. Entry is evicted and service is called, then you have
handle exception, at that time value is not in cache anymore. So what should i do? Ditch ehCache just like that? What about logic? 
Some methods create cache keys from arguments, they use key attribute in @Cacheable, I will have to handle this same as spring. 
I was looking for solutions on web, few posts i've found related to this problem looked more like carbonara or amatriciana then 
explanation how to crack this. 

### Solution

   I was running in circles for while, it did cross my mind to implement it on my own but it seemed as a big task. 
Eventualy i said if i don't make in day I will give it up. Actualy it was quite easy, just one simple annotation with
same fields like Cacheable and simple Aspect around it. Additionaly because i have to preserve data undefinetly it will
not expire so i am not obliged to use cache, HashMap/TreeMap will do fine.

first you have to enable in application context.

```
<aop:aspectj-autoproxy />
```

then define your Annontation

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PreserveValidCache {
	public String value();
	public String 	key();
}
```


  Keep your fields names, so you can easely move from @Cacheable to @PreserveValidCache. Sometimes keys we used for @Cacheable
are constructed from method argument, so this case had to be covered. That is basicaly first half of method body, second is 
handling entries and method calls, and you got me cheating. I have picked permenent cache for data store instead HashMap
simply because ehcache will not throw OutOfMemoryError and it will gently write content to file. 


```java

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
		String key = (String) exp.getValue(spelContext);

		Map<String, Entry> cache = cacheMap.get(preserveValidCache.value());
		if (cache == null){
			cache = new ConcurrentHashMap<String, Entry>();
			cacheMap.put(preserveValidCache.value(),cache );
		}
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

```
### additional resources

http://stackoverflow.com/questions/28118339/spring-cacheable-preserve-old-value-on-error

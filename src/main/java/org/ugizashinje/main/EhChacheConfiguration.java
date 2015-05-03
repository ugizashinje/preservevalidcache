package org.ugizashinje.main;

 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
 
@Configuration
@EnableCaching
public class EhChacheConfiguration {
     
    @Bean
    public EhCacheManagerFactoryBean ehCahceFactory(){      
        EhCacheManagerFactoryBean ehCache = new EhCacheManagerFactoryBean();
        ehCache.setConfigLocation(new ClassPathResource("ehcache.xml"));
        ehCache.setCacheManagerName("customerCache");
        ehCache.setShared(true);
        return ehCache;
    }
     
    @Bean
    public EhCacheCacheManager cachceManager(EhCacheManagerFactoryBean ehCahceFactory){
        EhCacheCacheManager ehCacheCacheManager = new EhCacheCacheManager();
        ehCacheCacheManager.setCacheManager(ehCahceFactory.getObject());
        return ehCacheCacheManager;
    }
    
}
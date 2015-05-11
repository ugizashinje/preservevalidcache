/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ugizashinje.main;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class SBapiCacheEventListener implements CacheEventListener {
	private Logger log = LoggerFactory.getLogger(SBapiCacheEventListener.class);

	private Map<Object, Element> oldObjects = Collections.synchronizedMap( new HashMap<Object, Element>());

	@Override
	public void notifyElementRemoved(Ehcache cache, Element el)
			throws CacheException {
		log.info("removed");
	}

	@Override
	public void notifyElementPut(Ehcache cache, Element el)
			throws CacheException {
		if (el.getObjectValue() == null) {
			log.info("	Old objects " + oldObjects);
			cache.removeQuiet(el.getObjectKey());
			cache.putQuiet(oldObjects.get(el.getObjectKey()));
			log.info("	On Put Element: " + oldObjects.get(el.getObjectKey()));
		}
	}

	@Override
	public void notifyElementUpdated(Ehcache cache, Element el)
			throws CacheException {
		log.info("update");
	}

	@Override
	public void notifyElementExpired(Ehcache cache, Element el) {
		log.info("On Expired Keys: " + cache.getKeys());
		log.info("On Expired element : " + el.toString());
		el.setCreateTime();
		oldObjects.put(el.getObjectKey(), el);

	}

	@Override
	public void notifyElementEvicted(Ehcache cache, Element el) {
		log.info("evicted ");
	}

	@Override
	public void notifyRemoveAll(Ehcache cache) {
		log.info("remove all");

	}

	@Override
	public void dispose() {
	}

	@Override
	public SBapiCacheEventListener clone() throws CloneNotSupportedException {
		log.info("clone");

		return (SBapiCacheEventListener) super.clone();
	}

}

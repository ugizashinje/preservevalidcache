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

import org.terracotta.modules.ehcache.transaction.SoftLockManagerProvider;

public class SBapiCacheEventListener implements CacheEventListener {
	private Map<Object, Element> oldObjects = Collections.synchronizedMap( new HashMap<Object, Element>());

	@Override
	public void notifyElementRemoved(Ehcache cache, Element el)
			throws CacheException {
		System.out.println("removed");
	}

	@Override
	public void notifyElementPut(Ehcache cache, Element el)
			throws CacheException {
		if (el.getObjectValue() == null) {
			System.out.println("	Old objects " + oldObjects);
			cache.removeQuiet(el.getObjectKey());
			cache.putQuiet(oldObjects.get(el.getObjectKey()));
			System.out.println("	On Put Element: " + oldObjects.get(el.getObjectKey()));
		}
	}

	@Override
	public void notifyElementUpdated(Ehcache cache, Element el)
			throws CacheException {
		System.out.println("update");
	}

	@Override
	public void notifyElementExpired(Ehcache cache, Element el) {
		System.out.println("On Expired Keys: " + cache.getKeys());
		System.out.println("On Expired element : " + el.toString());
		el.setCreateTime();
		oldObjects.put(el.getObjectKey(), el);

	}

	@Override
	public void notifyElementEvicted(Ehcache cache, Element el) {
		System.out.println("evicted ");
	}

	@Override
	public void notifyRemoveAll(Ehcache cache) {
		System.out.println("remove all");

	}

	@Override
	public void dispose() {
	}

	@Override
	public SBapiCacheEventListener clone() throws CloneNotSupportedException {
		System.out.println("clone");

		return (SBapiCacheEventListener) super.clone();
	}

}

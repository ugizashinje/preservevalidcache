/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ugizashinje.main;

import java.util.Properties;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;

/**
 *
 * @author nsijakinjic
 */
public class SBapiCacheListenerFactory extends CacheEventListenerFactory{

    @Override
    public CacheEventListener createCacheEventListener(Properties prprts) {
        return new SBapiCacheEventListener();
    }
    
}

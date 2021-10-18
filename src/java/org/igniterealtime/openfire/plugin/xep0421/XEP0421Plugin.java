/*
 * Copyright (C) 2017 Ignite Realtime Foundation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.igniterealtime.openfire.plugin.xep0421;

import java.io.File;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.SystemProperty;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Openfire plugin that integrates XEP-0421.
 *
 * @author 
 */
public class XEP0421Plugin implements Plugin
{
    private static final Logger Log = LoggerFactory.getLogger( XEP0421Plugin.class );

    private XEP0421IQHandler xep0421Handler = null;

    public static final SystemProperty<Boolean> XMPP_OCCUPANTIDENTIFIERS_ENABLED = SystemProperty.Builder.ofType(Boolean.class)
            .setKey("xmpp.xep0421.enabled")
            .setPlugin( "xep0421" )
            .setDefaultValue(false)
            .setDynamic(true)
            .build();
    
    private Cache<String, String> cache = null;
    private DatabaseController db = null;

    @Override
    public void initializePlugin( PluginManager manager, File pluginDirectory )
    {
        SystemProperty.removePropertiesForPlugin("xep0421");
        Log.info("Initialize XEP-0421 Plugin enabled:"+XMPP_OCCUPANTIDENTIFIERS_ENABLED.getDisplayValue());
        this.xep0421Handler = new XEP0421IQHandler(this);
        InterceptorManager.getInstance().addInterceptor(this.xep0421Handler);
        if (JiveGlobals.getLongProperty("cache.XEP0421.maxLifetime", 0)==0)
        {
            JiveGlobals.setProperty("cache.XEP0421.maxLifetime","3600000");
        }
        if (JiveGlobals.getLongProperty("cache.XEP0421.size", 0)==0)
        {
            JiveGlobals.setProperty("cache.XEP0421.size","20971520");
        }
        cache = CacheFactory.createCache("XEP0421");
        db = new DatabaseController();
    }

    @Override
    public void destroyPlugin()
    {
        Log.info("Destroy XEP-0421 Plugin");
        InterceptorManager.getInstance().removeInterceptor(this.xep0421Handler);
        this.xep0421Handler = null;
    }

    public Cache<String, String> getCache() {
        return cache;
    }

    public DatabaseController getDB() {
        return db;
    }
}

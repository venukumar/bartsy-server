dataSource {
    pooled = true
    driverClassName = "" //configured in /usr/Bartsy-config.properties file
    dialect = "" //configured in /usr/Bartsy-config.properties file
    username = "" //configured in /usr/Bartsy-config.properties file
    password = "" //configured in /usr/Bartsy-config.properties file
	
	properties {
		maxActive = -1
		minEvictableIdleTimeMillis=1800000
		timeBetweenEvictionRunsMillis=1800000
		numTestsPerEvictionRun=3
		testOnBorrow=true
		testWhileIdle=true
		testOnReturn=true
		validationQuery="SELECT 1"
	 }
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "" ////configured in /usr/Bartsy-config.properties file
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "" ////configured in /usr/Bartsy-config.properties file
        }
    }
    production {
        dataSource {
            dbCreate = "update"
			url = "" //configured in /usr/Bartsy-config.properties file

        }
    }
}

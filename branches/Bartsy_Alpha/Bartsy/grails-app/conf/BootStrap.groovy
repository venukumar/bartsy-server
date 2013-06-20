import bartsy.BartsyConfiguration
import bartsy.IngredientCategory;
import bartsy.IngredientType;

class BootStrap {

    def init = { servletContext ->
		if(!BartsyConfiguration.count()){
			new BartsyConfiguration(configName:'timer',value:'true').save(flush:true)
			new BartsyConfiguration(configName:'heartbeat',value:'true').save(flush:true)
			new BartsyConfiguration(configName:'userTimeout',value:'30').save(flush:true)
			new BartsyConfiguration(configName:'venueTimeout',value:'30').save(flush:true)
			new BartsyConfiguration(configName:'apiVersion',value:'1').save(flush:true)
		}
			  }
    def destroy = {
    }
}

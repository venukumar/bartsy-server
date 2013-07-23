import bartsy.BartsyConfiguration
import bartsy.AdminUser
import bartsy.IngredientCategory;
import bartsy.IngredientType;

class BootStrap {

    def init = { servletContext ->
		if(!BartsyConfiguration.count()){
			new BartsyConfiguration(configName:'timer',value:'true').save(flush:true)
			new BartsyConfiguration(configName:'heartbeat',value:'true').save(flush:true)
			new BartsyConfiguration(configName:'userTimeout',value:'30').save(flush:true)
			new BartsyConfiguration(configName:'venueTimeout',value:'30').save(flush:true)
			new BartsyConfiguration(configName:'apiVersion',value:'3').save(flush:true)
			new BartsyConfiguration(configName:'payment',value:'Sand Box').save(flush:true)
			new BartsyConfiguration(configName:'authId',value:'493ZG9zm2jH4').save(flush:true)
			new BartsyConfiguration(configName:'authPassword',value:'2q7GcGA3h6KD2W2Y').save(flush:true)
		}
		if(!AdminUser.count()){
			new AdminUser(username:'admin',password:'B@rt$y',userType:'Admin').save(flush:true)
			
		}
			  }
    def destroy = {
    }
}

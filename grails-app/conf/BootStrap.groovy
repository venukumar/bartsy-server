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
			new BartsyConfiguration(configName:'apiVersion',value:'2').save(flush:true)
		}
		if(!AdminUser.count()){
			new AdminUser(userName:'admin',password:'B@rt$y',userType:'Admin').save(flush:true)
			
		}
			  }
    def destroy = {
    }
}

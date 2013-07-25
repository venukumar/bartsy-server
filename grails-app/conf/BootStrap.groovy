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
			new BartsyConfiguration(configName:'payment',value:'Environment.SANDBOX').save(flush:true)
			new BartsyConfiguration(configName:'authId',value:'75x2yLLj').save(flush:true)
			new BartsyConfiguration(configName:'authPassword',value:'5Lq4dG24m63qncQ4').save(flush:true)
		}
		if(!AdminUser.count()){
			new AdminUser(username:'admin',password:'B@rt$y',userType:'Admin').save(flush:true)
			
		}
			  }
    def destroy = {
    }
}

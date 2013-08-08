import bartsy.BartsyConfiguration
import bartsy.AdminUser
import bartsy.IngredientCategory;
import bartsy.IngredientType;
import bartsy.BartsyConstants

class BootStrap {

    def init = { servletContext ->
		if(!BartsyConfiguration.count()){
			new BartsyConfiguration(configName:BartsyConstants.TIMER,value:'true').save(flush:true)
			new BartsyConfiguration(configName:BartsyConstants.HEARTBEAT,value:'true').save(flush:true)
			new BartsyConfiguration(configName:BartsyConstants.USER_TIMEOUT,value:'30').save(flush:true)
			new BartsyConfiguration(configName:BartsyConstants.VENUE_TIMEOUT,value:'30').save(flush:true)
			new BartsyConfiguration(configName:BartsyConstants.API_VERSION,value:'3').save(flush:true)
			new BartsyConfiguration(configName:BartsyConstants.PAYMENT,value:'Environment.SANDBOX').save(flush:true)
			new BartsyConfiguration(configName:BartsyConstants.AUTH_ID,value:'75x2yLLj').save(flush:true)
			new BartsyConfiguration(configName:BartsyConstants.AUTH_PASSWORD,value:'5Lq4dG24m63qncQ4').save(flush:true)
			new BartsyConfiguration(configName:BartsyConstants.TRADING_DAY,value:'18').save(flush:true)
		}
		if(!AdminUser.count()){
			new AdminUser(username:'admin',password:'B@rt$y',userType:'Admin').save(flush:true)
			
		}
			  }
    def destroy = {
    }
}

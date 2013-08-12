import bartsy.BartsyConfiguration
import bartsy.AdminUser
import bartsy.IngredientCategory;
import bartsy.IngredientType;
import bartsy.CommonConstants

class BootStrap {

    def init = { servletContext ->
		if(!BartsyConfiguration.count()){
			new BartsyConfiguration(configName:CommonConstants.TIMER,value:'true').save(flush:true)
			new BartsyConfiguration(configName:CommonConstants.HEARTBEAT,value:'true').save(flush:true)
			new BartsyConfiguration(configName:CommonConstants.USER_TIMEOUT,value:'30').save(flush:true)
			new BartsyConfiguration(configName:CommonConstants.VENUE_TIMEOUT,value:'30').save(flush:true)
			new BartsyConfiguration(configName:CommonConstants.API_VERSION,value:'3').save(flush:true)
			new BartsyConfiguration(configName:CommonConstants.PAYMENT,value:'Environment.SANDBOX').save(flush:true)
			new BartsyConfiguration(configName:CommonConstants.AUTH_ID,value:'75x2yLLj').save(flush:true)
			new BartsyConfiguration(configName:CommonConstants.AUTH_PASSWORD,value:'5Lq4dG24m63qncQ4').save(flush:true)
			new BartsyConfiguration(configName:CommonConstants.TRADING_DAY,value:'18').save(flush:true)
		}
		if(!AdminUser.count()){
			new AdminUser(username:'admin',password:'B@rt$y',userType:'Admin').save(flush:true)
			
		}
			  }
    def destroy = {
    }
}

package bartsy

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject;

class OrderService {

	def serviceMethod() {
	}
	Orders order
	def reOrder(Orders order,Venue venue,UserProfile user){
		try{
			def itemName = order.itemName
			def itemsList = order.itemsList
			if(itemName){
				def locuMenu = JSON.parse(URLDecoder.decode(venue.locuMenu))
				if(locuMenu && locuMenu.toString().contains(itemName)){
					return true
				}else{
					def ingredients = Ingredients.findByName(itemName)
					if(ingredients){
						return true
					}else{
						return false
					}
				}
			}
			if(itemsList){
				def itemsListForCheck = new JSONArray(itemsList)
				if(itemsListForCheck){
					itemsListForCheck.each{
						def ing = it
						def ingts = Ingredients.findByName(ing)
						if(!ingts){
							return false
						}
					}
				}
			}
		}catch(Exception e){
			log.info("Exception in reOrder service ===> "+e.getMessage())
		}
	}
}

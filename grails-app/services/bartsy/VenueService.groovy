package bartsy

import grails.converters.JSON

class VenueService {


	/**
	 * 
	 * @param json
	 * @param output
	 * @return
	 */
	def getLocuMenu(json,Map output) {
		try{
			Venue venue =checkForVenue(json)
			if(venue){
				def menu = JSON.parse(URLDecoder.decode(venue.menu))
				//output.put("menu", menu)


				output.put("parsedData",menu)

				//				def specialHours = json.has("specialHours")?json.specialHours.toString().trim():""
				//				def menuHeaders = json.has("menuHeaders")?json.menuHeaders:""

			}else{
				output.put("errorCode", 3)
				output.put("errorMessage","Venue does not exists")
			}
		}catch(Exception e){
			log.info("Exception found in getLocuMenu(VenueService) "+e.getMessage())
			output.put("errorCode", 200)
			output.put("errorMessage",e.getMessage())
		}
	}
	/**
	 * 
	 * @param json
	 * @param output
	 * @return
	 */
	def getLocuMenuHeaders(json,Map output) {
		try{
			def venue=Venue.findByVenueId(json.venueId)
			if(venue){
				if(venue.locuMenu){
					def locuMenu = JSON.parse(URLDecoder.decode(venue.locuMenu))
					if(locuMenu.menus){
						def menus = locuMenu.menus
						if(menus){
							def menueHeaders=[]
							menus.each {
								def menueObj = it
								menueHeaders.add(menueObj.menu_name)
							}
							output.put("menuHeaders",menueHeaders)
							output.put("errorCode",0)
							output.put("currentTime",new Date().toGMTString())
						}else{
							output.put("errorCode", 6)
							output.put("errorMessage","Menue headers not available")
						}
					}else{
						output.put("errorCode", 5)
						output.put("errorMessage","Locu Menue not available")
					}
				}else{
					output.put("errorCode", 4)
					output.put("errorMessage","Locu Data not available")
				}
			}else{
				output.put("errorCode", 3)
				output.put("errorMessage","Venue does not exists")
			}
		}catch(Exception e){
			log.info("Exception found in getLocuMenu(VenueService) "+e.getMessage())
			output.put("errorCode", 200)
			output.put("errorMessage",e.getMessage())
		}
	}
	/**
	 * 
	 * To verify whether venue is exist or not. If venue exists we are returning venueId
	 * 
	 * @param json
	 * @return
	 */
	def checkForVenue(json){
		Venue venue
		if(json.has("venueId")){
			venue=Venue.findByVenueId(json.venueId)
		}
		return venue
	}
}

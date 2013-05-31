package bartsy

class InventoryService {

	def getCocktails(venueId){
		// for returning response of the service
		def output=[:]
		// get venue details from DB based on the venueId
		def venue = Venue.findByVenueId(venueId)
		// Added venueid to response
		output.put("VenueId", venueId)
		// if venue id exists in db
		if(venue){
			// to get all available cocktails from db based on the venue id
			def cocktail = Cocktails.findAllByVenue(venue)
			// if cocktails exists
			if(cocktail){
			// For store the list of cocktails
				def cocktailsList=[]
				cocktail.each {
					def cocktailObject = it
					def cocktailMap=[:]
					// added all cocktail details cocktailsMap
					cocktailMap.put("cocktailId", cocktailObject.cocktailId)
					cocktailMap.put("name", cocktailObject.name)
					cocktailMap.put("category", cocktailObject.category)
					cocktailMap.put("glass", cocktailObject.glass)
					cocktailMap.put("alcohol", cocktailObject.alcohol)
					cocktailMap.put("ingredients", cocktailObject.ingredients)
					cocktailMap.put("instructions", cocktailObject.instructions)
					cocktailMap.put("price", cocktailObject.price)
					cocktailMap.put("available", cocktailObject.available)
					// stored every cocktailsMap into cocktailsList
					cocktailsList.add(cocktailMap)
					}
				output.put("cocktails", cocktails)
				output.put("errorCode", 0)
				output.put("errorMessage", "Cocktails are available")
				return output
			}
			else{
				// if cocktails doen't exists
				return ifCocktailsDoesNotExists(output)
			}

		}
		else{
			// If venue id doesn't exist in db, just return the errror message to request
			return ifVenueIdDoesNotExit(output)
		}
	}
	/**
	 * @methodName : ifVenueIdDoesNotExit
	 * 		Calling when venue id is doesn't exist in db.
	 * @return
	 */
	def ifVenueIdDoesNotExit(response){
		response.put("errorCode", 1)
		response.put("errorMessage", "Vneue ID does not exist")
		return response
	}
	/**
	 * @methodName : ifCocktailsDoesNotExists
	 * 		Calling when cocktails doesn't exist in db.
	 * @return
	 */
	def ifCocktailsDoesNotExists(response){
		response.put("errorCode", 1)
		response.put("errorMessage", "Cocktails are not available")
		return response

	}
}

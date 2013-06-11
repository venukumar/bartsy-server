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
				output.put("cocktails", cocktailsList)
				output.put("errorCode", 0)
				output.put("errorMessage", "Cocktails are available")
				return output
			}
			else{
				// if cocktails doesn't exists
				return returnNegativeResponse(output,"Cocktails are not available")
			}

		}
		else{
			// If venue id doesn't exist in db, just return the errror message to request
			return returnNegativeResponse(output,"Vneue ID does not exist")
		}
	}
	/**
	 * @methodName : This method we are used to delete the ingredient from the DB.
	 * 
	 * @param venueId
	 * @param ingredientId
	 * @return
	 */
	def deleteIngredient(venueId,ingredientId){
		// for returning response of the service
		def output=[:]
		// get venue details from DB based on the venueId
		def venue = Venue.findByVenueId(venueId)
		// get ingredient from DB based on the ingredientId
		def ingredient=Ingredients.findByIngredientId(ingredientId)
		// Added venueid to response
		output.put("venueId", venueId)
		// checking venue id exists in DB
		if(venue){
			// checking ingredient id exists in DB
			if(ingredient){
				output.put("ingredientId", ingredientId)
			def iingredient=Ingredients.get(ingredientId)
			def status = iingredient.delete()
			println "statussssss "+status
			output.put("status", status)
			}else{
			// If venue id doesn't exist in db, just return the errror message to client
			return returnNegativeResponse(output,"Ingredient ID does not exist")
			}
		}else{
			// If venue id doesn't exist in db, just return the errror message to client
			return returnNegativeResponse(output,"Vneue ID does not exist")
		}
		
	}
	/**
	 * @methodName : returnNegativeResponse
	 * 		Calling when you didn't get from data from client return response to client
	 * @return response
	 */
	def returnNegativeResponse(response,message){
		response.put("errorCode", 1)
		response.put("errorMessage", message)
		return response
	}

	
}

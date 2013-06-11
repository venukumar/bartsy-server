package bartsy

import javassist.bytecode.stackmap.BasicBlock.Catch;
import grails.converters.JSON

class InventoryController {
	def inventoryService
	def saveIngredients = {
		try{
			def response = [:]
			def json =  JSON.parse(request)
			def venue = Venue.findByVenueId(json.venueId)
			if(venue){				
				def type = IngredientType.findByType(json.type)
				if(!type){
					type =  new IngredientType()
					type.setType(json.type)
					type.save(flush:true)
				}
				def category =  IngredientCategory.findByCategoryAndType(json.category,type)
				if(!category){
					category =  new IngredientCategory()
					category.setCategory(json.category)
					category.setType(type)
					category.save(flush:true)
				}
				def ingredients = json.ingredients
				if(ingredients) {
					ingredients.each{
						def ingredient =  it
						def ingredientToSave = Ingredients.findByIngredientId(ingredient.ingredientId as long)
						if(ingredientToSave){
							ingredientToSave.setPrice(ingredient.price as int)
							ingredientToSave.setAvailable(ingredient.available)
						}
						else{
							ingredientToSave =  new Ingredients()
							ingredientToSave.setIngredientId(ingredient.ingredientId as long)
							ingredientToSave.setName(ingredient.name)
							ingredientToSave.setPrice(ingredient.price as int)
							ingredientToSave.setAvailable(ingredient.available)
							ingredientToSave.setCategory(category)
							ingredientToSave.setVenue(venue)
						}
						if(ingredientToSave.save(flush:true)) {
							response.put("errorCode","0")
							response.put("errorMessage","Ingredients saved successfully")
						}
					}
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage","No Ingredients to Save")
				}
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage","Venue is not registered")
			}
			render(text:response as JSON, contentType:"application/json")
		}
		catch(Exception e){
			println e.getMessage()
		}
	}
	
	def saveCocktails= {
		try {
			def response = [:]
			def json =  JSON.parse(request)
			def venue = Venue.findByVenueId(json.venueId)
			if(venue) {
				def cocktails = json.cocktails
				if(cocktails) {
					cocktails.each{
						def cocktail =  it
						def cocktailsToSave = Cocktails.findByCocktailId(cocktail.cocktailId as long)
						if(cocktailsToSave){
							cocktailsToSave.setPrice(cocktail.price as int)
							cocktailsToSave.setAvailable(cocktail.available)
						}
						else{
							cocktailsToSave =  new Cocktails()
							cocktailsToSave.setCocktailId(cocktail.cocktailId as long)
							cocktailsToSave.setName(cocktail.name)
							cocktailsToSave.setCategory(cocktail.category)
							cocktailsToSave.setGlass(cocktail.glass)
							cocktailsToSave.setAlcohol(cocktail.alcohol)
							cocktailsToSave.setIngredients(cocktail.ingredients)
							cocktailsToSave.setInstructions(cocktail.instructions)
							cocktailsToSave.setPrice(cocktail.price as int)
							cocktailsToSave.setAvailable(cocktail.available)
							cocktailsToSave.setVenue(venue)
						}
						if(cocktailsToSave.save(flush:true)) {
							response.put("errorCode","0")
							response.put("errorMessage","Cocktails saved successfully")
						}
					}
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage","No Cocktails to Save")
				}
			}
			render(text:response as JSON, contentType:"application/json")
		}catch(Exception e){
			println "save cocktails ::: "+e.getMessage()
		}
	}
	
	def getIngredients = {
		
		def json = JSON.parse(request)
		def venue = Venue.findByVenueId(json.venueId.toString())
		def response = [:]
		if(venue){
			response.put("venueId", json.venueId)
			def types =  IngredientType.getAll()
			if(types){
				def listOfTypes=[]
				types.each{
					def typeMap=[:]
					def type = it
					
					typeMap.put("typeName", type.type)
					def categories =  IngredientCategory.findAllByType(type)
					if(categories){
						def listOfCategories=[]
						categories.each{
							def categoryMap=[:]
							def categoryObject =  it
							categoryMap.put("categoryName",categoryObject.category)
							def ingredients = Ingredients.findAllByCategoryAndVenue(categoryObject,venue)
							
							if(ingredients){
								def ingredientsList=[]
								ingredients.each{
									def ingredient = it
									def ingredientMap = [:]
									if(ingredient.available.equals("true")){
									ingredientMap.put("ingredientId",ingredient.ingredientId)
									ingredientMap.put("name",ingredient.name)
									ingredientMap.put("price",ingredient.price)
									ingredientMap.put("available",ingredient.available)
									ingredientsList.add(ingredientMap)
							}
								}
								categoryMap.put("ingredients", ingredientsList)
							}
								
						else{
								response.put("errorCode","1")
								response.put("errorMessage","No Ingredients Available")
								render(text:response as JSON ,  contentType:"application/json")
								return
							}
						listOfCategories.add(categoryMap)
						}
						typeMap.put("categories", listOfCategories)
					}
					else{
						response.put("errorCode","1")
						response.put("errorMessage","No Categories Available")
						render(text:response as JSON ,  contentType:"application/json")
						return
					}
					listOfTypes.add(typeMap);
				}
				response.put("errorCode","0")
				response.put("ingredients",listOfTypes)
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage","No Types Available")
			}
		}
		else{
			response.put("errorCode","1")
			response.put("errorMessage","Venue Does not exist")
		}
		render(text:response as JSON ,  contentType:"application/json")
	}
	
	/**
	 * To get the list of cocktails from DB and send to the client
	 */
	def getCocktails={
		// To get request from client
		def json = JSON.parse(request)
		// get requested venue id from the json
		def venueId=json.venueId.toString();
		// created a map object for returning the response
		def response = [:]
		// checking if the venue ID is null or not
		if(venueId){
			response=inventoryService.getCocktails(venueId);
		}else{
		response.put("errorCode", 1)
		response.put("errorMessage", "Vneue ID is empty or null")
		}
		render(text:response as JSON, contentType:"application/json")
	}	
	
	/**
	 * To delete an Ingredient based on client request
	 *
	 */
	def deleteIngredient={
		// To get body of request
		def json = JSON.parse(request)
		
		println "delete ingredient"
		
		// get requested venue id from the json
		def venueId=json.venueId;
		def ingredientId=json.ingredientId
		// created a map object for returning the response
		def response = [:]
		// checking if the venue ID is null or not
		if(venueId){
			// checking if the Ingredient ID is null or not
			if(ingredientId){
				response=inventoryService.deleteIngredient(venueId,ingredientId);
			}else{
			handleNegativeResponse(response,"Ingredient ID is empty or null")
			}
		}
		else{
			handleNegativeResponse(response,"Vneue ID is empty or null")
		}
		// sent response to client
		render(text:response as JSON, contentType:"application/json")
	}
	/**
	 * To return negative response
	 *
	 * @param response
	 * @param message
	 * @return
	 */
	def handleNegativeResponse(response,message){
		response.put("errorCode", 1)
		response.put("errorMessage", message)
		return response
	}
	
	
}

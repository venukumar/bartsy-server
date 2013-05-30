package bartsy

import javassist.bytecode.stackmap.BasicBlock.Catch;
import grails.converters.JSON

class InventoryController {

	def saveIngredients = {
		try{
			def response = [:]
			def json =  JSON.parse(request)
			def venue = Venue.findByVenueId(json.venueId)
			if(venue){				
				def category =  IngredientCategory.findByCategory(json.category)
				if(!category){
					category =  new IngredientCategory()
					category.setCategory(json.category)
					category.save(flush:true)
				}
				def type = IngredientType.findByType(json.type)
				if(!type){
					type =  new IngredientType()
					type.setType(json.type)
					type.setCategory(category)
					type.save(flush:true)
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
							ingredientToSave.setType(type)
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
			def types =  IngredientType.getAll()
			if(types){
				def listOfTypes=[]
				types.each{
					def typeMap=[:]
					def type = it
					typeMap.put("venueId", venue)
					typeMap.put("typeName", type.name)
					def categories =  IngredientCategory.findAllByType(type)
					if(categories){
						def listOfCategories=[]
						categories.each{
							def categoryMap=[:]
							def category =  it
							categoryMap.put("name",category.category)
							def ingredients = Ingredients.findAllByCategoryAndVenue(category,venue)
							
							if(ingredients){
								def ingredientsList=[]
								ingredients.each{
									def ingredient = it
									def ingredientMap = [:]
									ingredientMap.put("ingredientId",ingredient.ingredientId)
									ingredientMap.put("name",ingredient.name)
									ingredientMap.put("price",ingredient.price)
									ingredientMap.put("available",ingredient.available)
									ingredientsList.add(ingredientMap)
								}
								categoryMap.put("ingredients", ingredientsList)
							}
								
						else{
								response.put("errorCode","1")
								response.put("errorMessage","No Ingredients Available")
							}
						listOfCategories.add(categoryMap)
						}
						typeMap.put("categories", listOfCategories)
					}
					else{
						response.put("errorCode","1")
						response.put("errorMessage","No Categories Available")
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
}

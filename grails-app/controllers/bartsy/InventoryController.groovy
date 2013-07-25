package bartsy

import javassist.bytecode.stackmap.BasicBlock.Catch;
import grails.converters.JSON

class InventoryController {

	def inventoryService

	def saveIngredients = {
		def response = [:]
		try{
			def json =  JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
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
							def ingredientToSave = Ingredients.findByIngredientIdAndVenue(ingredient.ingredientId as long,venue)
							if(ingredientToSave){
								ingredientToSave.setPrice(Float.parseFloat(ingredient.price))
								ingredientToSave.setAvailable(ingredient.available)
							}
							else{
								ingredientToSave =  new Ingredients()
								ingredientToSave.setIngredientId(ingredient.ingredientId as long)
								ingredientToSave.setName(ingredient.name)
								ingredientToSave.setPrice(Float.parseFloat(ingredient.price))
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
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON, contentType:"application/json")
	}

	def saveCocktails= {
		def response = [:]
		try{
			def json =  JSON.parse(request)
			println"json "+json
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(Integer.parseInt(apiVersion.value) == Integer.parseInt(json.apiVersion.toString())){
				def venue = Venue.findByVenueId(json.venueId)
				if(venue) {
					def cocktails = json.cocktails
					if(cocktails) {
						def failedcocks=[]
						cocktails.each{
							def cocktail =  it
							def cocktailsToSave = Cocktails.findByCocktailIdAndVenue(Long.parseLong(cocktail.cocktailId),venue)
							if(cocktailsToSave){
								cocktailsToSave.setPrice(Float.parseFloat(cocktail.price))
								cocktailsToSave.setAvailable(cocktail.available)
							}
							else{
								if(cocktail.ingredients && cocktail.shopping){
									println"cocktail.shopping "+cocktail.shopping
									//def strIngr = cocktail.ingredients.trim().split(",")
									def categoryList = cocktail.shopping.trim().split(",")
									def categories = checkForCategorys(categoryList)
									if(categories){
										//def ingForcheck = Ingredients.findByName(ingredint)
										cocktailsToSave =  new Cocktails()
										cocktailsToSave.setCocktailId(cocktail.name?Long.parseLong(cocktail.cocktailId):0.0)
										cocktailsToSave.setName(cocktail.name?cocktail.name:"")
										cocktailsToSave.setCategory(cocktail.category?cocktail.category:"")
										cocktailsToSave.setGlass(cocktail.glass?cocktail.glass:"")
										cocktailsToSave.setAlcohol(cocktail.alcohol?cocktail.alcohol:"")
										cocktailsToSave.setInstructions(cocktail.instructions?cocktail.instructions:"")
										cocktailsToSave.setPrice(cocktail.price?Float.parseFloat(cocktail.price):0.0)
										cocktailsToSave.setAvailable(cocktail.available?cocktail.available:"false")
										cocktailsToSave.setIngredients(cocktail.ingredients)
										cocktailsToSave.setDescription(categories.description?categories.description:"")
										cocktailsToSave.setShopping(categories.categorys?categories.categorys:"")
										cocktailsToSave.setVenue(venue)

										if(!cocktailsToSave.save(flush:true)) {
											failedcocks.add(cocktail.cocktailId)
										}
									}
								}else{
									failedcocks.add(cocktail.cocktailId)
								}
							}
						}
						if(failedcocks && failedcocks.size()>0){
							response.put("errorCode","1")
							response.put("errorMessage","Cocktails not saved successfully")
							response.put("failedCocktails",failedcocks)
						}else{
							response.put("errorCode","0")
							response.put("errorMessage","Cocktails saved successfully")
						}
					}
					else{
						response.put("errorCode","1")
						response.put("errorMessage","No Cocktails to Save")
					}
				}else{
					response.put("errorCode","1")
					response.put("errorMessage","Venue does not exists")
				}
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		println "response "+response
		render(text:response as JSON, contentType:"application/json")
	}
	/*
	 * Checking for all ingredients are available or not
	 * 
	 */
	def checkForCategorys(ingredients){
		def result=[:]
		String description
		String categorys
		if(ingredients && ingredients.size()>0){
			ingredients.each {
				def ingredient=it
				def ingForcheck = IngredientCategory.findByCategory(ingredient.toString().trim())
				if(ingForcheck){
					if(!categorys)
						categorys=ingForcheck.id
					else
						categorys=categorys+","+ingForcheck.id
				}else{
					if(!description)
						description=ingredient
					else
						description=description+","+ingredient
				}
			}
			result.put("description",description)
			result.put("categorys",categorys)
		}
		return result
	}
	/*
	 * This method used to get the ingredients in locu format
	 */
	def getIngredientsInLocuFormat={
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def venue = Venue.findByVenueId(json.venueId.toString())
				if(venue){
					response.put("venueId", json.venueId)
					response=inventoryService.getIngredients(venue)
				}else{
					response.put("errorCode","1")
					response.put("errorMessage","Venue Does not exist")
				}
			}else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON, contentType:"application/json")
	}


	def getIngredients = {
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def venue = Venue.findByVenueId(json.venueId.toString())
				if(venue){
					response.put("venueId", json.venueId)
					def types =  IngredientType.getAll()
					if(types){
						def listOfTypes=[]
						types.each{
							def typeMap=[:]
							def type = it
							def categories =  IngredientCategory.findAllByType(type)
							if(categories){
								typeMap.put("typeName", type.type)
								def listOfCategories=[]
								categories.each{
									def categoryMap=[:]
									def categoryObject =  it
									def ingredients = Ingredients.findAllByCategoryAndVenue(categoryObject,venue)
									if(ingredients){
										categoryMap.put("categoryName",categoryObject.category)
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

									listOfCategories.add(categoryMap)
								}
								typeMap.put("categories", listOfCategories)
							}
							listOfTypes.add(typeMap);
						}
						response.put("errorCode","0")
						response.put("ingredients",listOfTypes)
						response.put("currentTime",new Date().toGMTString())
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
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}

	/**
	 * To get the list of cocktails from DB and send to the client
	 */
	def getCocktails={
		def response = [:]
		try{
			// To get request from client
			def json = JSON.parse(request)
			try{
				def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
				if(Integer.parseInt(apiVersion.value) == Integer.parseInt(json.apiVersion)){
					// get requested venue id from the json
					def venueId=json.venueId.toString();
					// created a map object for returning the response
					// checking if the venue ID is null or not
					if(venueId){
						response=inventoryService.getCocktails(venueId);
					}else{
						response.put("errorCode", 1)
						response.put("errorMessage", "Vneue ID is empty or null")
					}
				}
				else{
					response.put("errorCode","100")
					response.put("errorMessage","API version do not match")
				}
				response.put("currentTime",new Date().toGMTString())
			}
			catch(Exception e){
				log.info("Exception is ===> "+e.getMessage())
				response.put("errorCode",200)
				response.put("errorMessage",e.getMessage())
			}
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON, contentType:"application/json")
	}

	/**
	 * To delete an Ingredient based on client request
	 *
	 */
	def deleteIngredient={
		def response = [:]
		try{
			// To get body of request
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				// get requested venue id from the json
				def venueId=json.venueId;
				def ingredientId=json.ingredientId
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
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
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

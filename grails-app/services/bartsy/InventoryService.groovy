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
			def cocktails = Cocktails.findAllByVenue(venue)
			// if cocktails exists
			if(cocktails){
				def option_group=[]
				def menus=[]
				def menusMap=[:]
				menusMap.put("menu_name","Bar")
				def sections=[]
				def sectionsMap=[:]
				sectionsMap.put("section_name","Cocktails")
				def contents=[]
				def subSections=[]
				def subSectionsMap=[:]
				cocktails.each {
					def cocktail=it
					if(cocktail.available && cocktail.available.equalsIgnoreCase("true")){
						def contentsMap=[:]
						def ingredients=[]
						def photos=[]
						def options=[]
						contentsMap=getCocktailMap(cocktail)
						contentsMap.put("photos",photos)
						if(cocktail.shopping && cocktail.shopping.length()>0){
							def categoryList = cocktail.shopping.trim().split(",")
							if(categoryList){
								categoryList.each {
									def categoryId = it
									def category=IngredientCategory.findById(categoryId)
									if(category){
										String name =category.category
										//									if(!option_group.contains(categoryId)){
										//										def map=[:]
										//										map.put("categoryId",categoryId)
										//										map.put("ingredients",cocktail.ingredients)
										//										option_group.add(map)
										//									}

										if(!options.contains(name))
											options.add(name)
									}
								}
							}
						}
						def options_groups=[]
						def options_groups_map=[:]
						options_groups_map.put("type","OPTION_SELECT")
						options_groups_map.put("text","")
						options_groups_map.put("options",options)
						options_groups.add(options_groups_map)
						contentsMap.put("option_groups",options_groups)
						contents.add(contentsMap)
					}
				}
				subSectionsMap.put("contents",contents)
				subSectionsMap.put("subsection_name","")
				subSections.add(subSectionsMap)
				sectionsMap.put("subsections",subSections)
				//				if(option_group && option_group.size()>0){
				//					option_group.each {
				//						def optionMap = it
				//						def categoryId=optionMap.categoryId
				//						def ingredients=optionMap.ingredients
				//						def category=IngredientCategory.findById(categoryId)
				//						def ingObjs = Ingredients.findAllByVenueAndCategory(venue,category)
				//						String key="option_"+category.category
				//						if(ingObjs){
				//							def categoryIngredients=[]
				//							def detailsOfIngList=[]
				//							def detailsOfIng=[:]
				//							detailsOfIng.put("name",category.category)
				//							ingObjs.each {
				//								def ingObj=it
				//								def option_group_map=[:]
				//								option_group_map.put("ingredientId",ingObj.ingredientId)
				//								option_group_map.put("name",ingObj.name)
				//								option_group_map.put("price",ingObj.price)
				//								option_group_map.put("available",ingObj.available)
				//								categoryIngredients.add(option_group_map)
				//							}
				//							detailsOfIng.put("options", categoryIngredients)
				//							detailsOfIngList.add(detailsOfIng)
				//							sectionsMap.put(key,detailsOfIngList)
				//						}
				//					}
				//				}
				sections.add(sectionsMap)
				menusMap.put("sections",sections)
				def menuJson=[]
				menuJson.add(menusMap)
				output.put("menus", menuJson)
				output.put("errorCode", 0)
				if(contents.size()>0)
					output.put("errorMessage", "Cocktails are available")
				else
					output.put("errorMessage", "contents are not available")
				return output
			}
			else{
				// if cocktails doesn't exists
				return returnNegativeResponse(output,"Cocktails are not available")
			}
		}
		else{
			// If venue id doesn't exist in db, just return the errror message to request
			return returnNegativeResponse(output,"Venue ID does not exist")
		}
	}

	/*
	 * This is the method used to create locu format of ingredients
	 */
	def getIngredients(Venue venue){
		def output=[:]
		try{
			def types =  IngredientType.getAll()
			if(types){
				def menus=[]
				def menusMap=[:]
				menusMap.put("menu_name","Bar")
				def sectionsList=[]
				types.each {
					def type=it
					def sectionsMap=[:]
					sectionsMap.put("section_name",type.type)
					def categories =  IngredientCategory.findAllByType(type)
					if(categories){
						def subSectionList=[]
						categories.each {
							def category =it
							def subSectionMap=[:]
							subSectionMap.put("subsection_name",category.category)
							def ingredients = Ingredients.findAllByCategoryAndVenue(category,venue)
							if(ingredients){
								def ingredientsList=[]
								ingredients.each{
									def ingredient = it
									def ingredientMap = [:]
									if(ingredient.available.equals("true")){
										ingredientMap.put("ingredientId",ingredient.id)
										ingredientMap.put("name",ingredient.name)
										ingredientMap.put("price",ingredient.price.toString())
										ingredientsList.add(ingredientMap)
									}
								}
								subSectionMap.put("contents",ingredientsList)
							}
							subSectionList.add(subSectionMap)
						}
						sectionsMap.put("subsections",subSectionList)
					}else{
					}
					sectionsList.add(sectionsMap)
				}
				menusMap.put("sections",sectionsList)
				menus.add(menusMap)
				output.put("menus", menus)
				output.put("errorCode", 0)
				output.put("errorMessage", "Ingredients are available")
				return output
			}
			else{
				output.put("errorCode","1")
				output.put("errorMessage","No Types Available")
				return output
			}
		}catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			output.put("errorCode",200)
			output.put("errorMessage",e.getMessage())
			return output
		}
	}


	def getCocktailMap(Cocktails cocktail){
		def contentsMap=[:]
		contentsMap.put("id",cocktail.id)
		//contentsMap.put("cocktailId",cocktail.cocktailId)
		contentsMap.put("name", cocktail.name)
		contentsMap.put("category",cocktail.category)
		contentsMap.put("alcohol",cocktail.alcohol)
		contentsMap.put("price", cocktail.price.toString())
		contentsMap.put("type", "BARTSY_ITEM")
		contentsMap.put("name", cocktail.name)
		contentsMap.put("glass", cocktail.glass)
		contentsMap.put("ingredients", cocktail.ingredients)
		contentsMap.put("description", cocktail.description)
		contentsMap.put("instructions", cocktail.instructions)
		//contentsMap.put("available", cocktail.available)
		return contentsMap
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

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
				menusMap.put("menu_name","Cocktails")
				def sections=[]
				def sectionsMap=[:]
				sectionsMap.put("section_name","")
				def contents=[]
				def subSections=[]
				def subSectionsMap=[:]
				int price=0
				int check=0
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
										if(!options.contains(name))
											options.add(name)
										def ingredient = Ingredients.findByCategoryAndVenue(category,venue)
										if(ingredient && check==0){
											ingredient.each {
												def ing =it
												price=ing.price
												check++
											}
										}
									}
								}
							}
						}
						if(options.size()>0)
						{
							def options_groups=[]
							def options_groups_map=[:]
							options_groups_map.put("type","OPTION_SELECT")
							options_groups_map.put("text","")
							def optionsMapList=[]
							options.each {
								def name = it
								def optionMap=[:]
								optionMap.put("name",name)
								if(check==1){
									optionMap.put("price",price)
								}
								optionsMapList.add(optionMap)
							}

							options_groups_map.put("options",optionsMapList)
							options_groups.add(options_groups_map)
							contentsMap.put("option_groups",options_groups)
						}
						contents.add(contentsMap)
					}
				}
				subSectionsMap.put("contents",contents)
				subSectionsMap.put("subsection_name","")
				subSections.add(subSectionsMap)
				sectionsMap.put("subsections",subSections)

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
	def getMixedDrinks(Venue venue){
		def output=[:]
		try{
			def types =  IngredientType.getAll()
			if(types){
				def menus=[]
				types.each {
					def type=it

					def categories =  IngredientCategory.findAllByType(type)
					if(categories){

						def menusMap=[:]
						if(type.type.toString().equalsIgnoreCase("Mixer")){
							menusMap.put("menu_name","Mixers")
							menusMap.put("show_menu","No")
						}else{
							menusMap.put("menu_name","Mixed Drinks")
						}
						def sectionsList=[]


						def sectionsMap=[:]
						def subSectionList=[]

						sectionsMap.put("section_name","")
						def subSectionMap=[:]
						subSectionMap.put("subsection_name","")
						def ingredientsList=[]
						categories.each {
							def category =it

							def ingredients = Ingredients.findAllByCategoryAndVenue(category,venue)
							if(ingredients){
								def itemMap=[:]
								itemMap.put("name", category.category)
								itemMap.put("type","ITEM_SELECT")
								itemMap.put("description","")
								def option_groups=[]

								def options=[]
								ingredients.each{
									def ingredient = it
									def ingredientMap = [:]
									if(ingredient.available.equals("true")){
										ingredientMap.put("id",ingredient.id)
										ingredientMap.put("name",ingredient.name)
										ingredientMap.put("price",ingredient.price.toString())
										if(options.size()==0){
											ingredientMap.put("text","Recommended")
											ingredientMap.put("selected","true")
										}
										options.add(ingredientMap)
									}
								}
								if(options.size()>0){
									def option_Groups_Map=[:]

									if(category.category.toString().contains("Add-ons"))
										option_Groups_Map.put("type","OPTION_ADD")
									else
										option_Groups_Map.put("type","OPTION_CHOOSE")
									option_Groups_Map.put("text",category.category)
									option_Groups_Map.put("options",options)
									option_groups.add(option_Groups_Map)
									if(!type.type.toString().equalsIgnoreCase("Mixer")){
										def mixersMap =[:]
										mixersMap.put("type","OPTION_SELECT")
										mixersMap.put( "text","Mixers")
										def mixerOptions=[]
										def ingTypeObj = IngredientType.findByType("Mixer")
										if(ingTypeObj){
											def categorys = IngredientCategory.findAllByType(ingTypeObj)
											if(categorys){
												categorys.each{
													def name = it
													mixerOptions.add(name.category)
												}
											}
										}
										mixersMap.put("options",mixerOptions)
										option_groups.add(mixersMap)
									}
									itemMap.put("option_groups",option_groups)
								}
								ingredientsList.add(itemMap)
							}
						}
						subSectionMap.put("contents",ingredientsList)
						subSectionList.add(subSectionMap)
						sectionsMap.put("subsections",subSectionList)
						sectionsList.add(sectionsMap)

						menusMap.put("sections",sectionsList)
						menus.add(menusMap)
					}else{
					}
				}
				output.put("menus", menus.reverse())
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

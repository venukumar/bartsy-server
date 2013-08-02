package bartsy

class FavoriteService {

	def serviceMethod() {
	}
	def saveFavoriteDrink(UserProfile user,Venue venue,json){
		def output=[:]
		try{
			def itemsList = json.itemsList
			if(itemsList){
				String category
				itemsList.each {
					def item = it
					def name = item.itemName
					if(name){
						def ingredients = Ingredients.findByName(name)
						if(ingredients){
							def categoryId = ingredients.category.id
							if(category){
								println "category "+category
								println "categoryId "+categoryId
								if(!category.contains(categoryId.toString()))
									category=category+","+categoryId
							}
							else
								category=categoryId
						}
					}
				}
				def description
				def specialInstructions
				def title
				if(json.has("description")){
					description=json.description
				}
				if(json.has("specialInstructions")){
					specialInstructions=json.specialInstructions
				}
				if(json.has("title")){
					title=json.title
				}
				def usrFvtDrnk = new UserFavoriteDrinks()
				usrFvtDrnk.setItemsList(itemsList.toString())
				usrFvtDrnk.setCategorys(category)
				usrFvtDrnk.setDescription(description)
				usrFvtDrnk.setSpecialInstructions(specialInstructions)
				usrFvtDrnk.setTitle(title)
				usrFvtDrnk.setUser(user)
				usrFvtDrnk.setVenue(venue)
				if(usrFvtDrnk.save(flush:true)) {
					output.put("errorCode","0")
					output.put("errorMessage","Your favorite drink is saved successfully")
					output.put("favoriteDrinkId",usrFvtDrnk.id)
				}else{
					output.put("errorCode","5")
					output.put("errorMessage","Your favorite drink is not saved successfully")
				}
			}else{
				output.put("errorCode","4")
				output.put("errorMessage","No itemsList found in your request")
			}
		}catch(Exception e){

			println"errors "+e.printStackTrace()
			log.info("Exception found in saveFavoriteDrink service "+e.getMessage())
			println"Exception found in saveFavoriteDrink service "+e.getMessage()

			output.put("errorCode",200)
			output.put("errorMessage","Error occured while processing your request. Please verify json")
			return output
		}
		return output
	}

	def getFavoriteDrinks(UserProfile user,Venue venue){
		def output=[:]
		try{
			def favoriteDrinks = UserFavoriteDrinks.findAllByUser(user)
			if(favoriteDrinks){
				def menu=[]
				def menuMap=[:]
				menuMap.put("menu_name","Available favorites")
				def section =[]
				favoriteDrinks.each {
					def fvrtDrink=it
					def sectionMap=[:]
					def title
					if(fvrtDrink.title){
						title=fvrtDrink.title
					}
					else{
						title=""
					}
					sectionMap.put("section_name",fvrtDrink.title)
					sectionMap.put("favoriteDrinkId",fvrtDrink.id)
					sectionMap.put("description",fvrtDrink.description)
					sectionMap.put("specialInstructions",fvrtDrink.specialInstructions)

					def subSections=[]
					def category = fvrtDrink.categorys
					if(category){
						def categoryList = category.trim().split(",")
						if(categoryList){
							categoryList.each {
								def categoryId = it

								def categoryObj=IngredientCategory.findById(categoryId)
								if(categoryObj){
									def subSectionsMap=[:]
									subSectionsMap.put("subsection_name",categoryObj.category)
									def contents=[]
									def contentsMap=[:]
									contentsMap.put("name", categoryObj.category)
									contentsMap.put("type","ITEM_SELECT")
									contentsMap.put("description","")
									contentsMap.put("price","0")
									def options=[]
									def ingredients = Ingredients.findAllByCategoryAndVenue(categoryObj,venue)
									ingredients.each{
										def ingredient = it
										def ingredientMap = [:]
										if(ingredient.available.equals("true")){
											//ingredientMap.put("id",ingredient.id)
											ingredientMap.put("name",ingredient.name)
											ingredientMap.put("price",ingredient.price.toString())
											if(fvrtDrink.itemsList.contains(ingredient.name)){
												//ingredientMap.put("text","Recommended")
												ingredientMap.put("selected","true")
											}
											options.add(ingredientMap)
										}
									}

									if(options.size()>0){
										def options_groups_map=[:]
										//options_groups_map.put("type","OPTION_SELECT")
										if(categoryObj.category.toString().contains("Add-ons"))
											options_groups_map.put("type","OPTION_ADD")
										else
											options_groups_map.put("type","OPTION_CHOOSE")

										options_groups_map.put("text","")
										options_groups_map.put("options",options)

										contentsMap.put("option_groups",options_groups_map)
									}
									contents.add(contentsMap)

									subSectionsMap.put("contents",contents)
									subSections.add(subSectionsMap)
								}
							}
						}
						sectionMap.put("subsections",subSections)
						section.add(sectionMap)
					}
				}
				menuMap.put("sections",section)
				menu.add(menuMap)
				output.put("menus",menu)
				if(section.size()>0){
					output.put("errorCode","0")
					output.put("errorMessage","Favorite Drinks Available")
				}else{
					output.put("errorCode","3")
					output.put("errorMessage","No favorite drinks are available")
				}
			}else{
				output.put("errorCode","3")
				output.put("errorMessage","No favorite drinks are available")
			}
		}catch(Exception e){
			log.info("Exception found in getFavoriteDrinks service "+e.getMessage())
			println"Exception found in getFavoriteDrinks service "+e.getMessage()
			output.put("errorCode",200)
			output.put("errorMessage","Error occured while processing your request. Please verify json")
		}

		return output
	}

}

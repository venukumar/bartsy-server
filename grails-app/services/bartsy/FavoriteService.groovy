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
							if(category)
								category=category+","+categoryId
							else
								category=categoryId
						}
					}
				}
				def description
				def specialInstructions
				if(!json.description){
					description=json.description
				}else{
					description="s"
				}
				if(!json.specialInstructions){
					specialInstructions=json.specialInstructions
				}else{
					specialInstructions="s"
				}
				def usrFvtDrnk = new UserFavoriteDrinks()
				println"json "+json
				println"itemName "+itemsList.toString()
				println"category "+category
				println"description "+json.description
				println"specialInstructions "+json.specialInstructions
				/*usrFvtDrnk.itemName=itemsList.toString()
				 usrFvtDrnk.categorys=category
				 usrFvtDrnk.description="description"
				 usrFvtDrnk.specialInstructions="special"
				 usrFvtDrnk.user=user
				 usrFvtDrnk.venue=venue*/


				usrFvtDrnk.setItemsList(itemsList.toString())
				usrFvtDrnk.setCategorys(category)
				usrFvtDrnk.setDescription(description)
				usrFvtDrnk.setSpecialInstructions(specialInstructions)
				usrFvtDrnk.setUser(user)
				usrFvtDrnk.setVenue(venue)
				println"before save"
				if(usrFvtDrnk.save(flush:true)) {
					println"if"
					output.put("errorCode","0")
					output.put("errorMessage","Your favorite drink is saved successfully")
				}else{
					println"else"
					output.put("errorCode","5")
					output.put("errorMessage","Your favorite drink is not saved successfully")
				}
				println"after save"
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
				menuMap.put("menu_name","Bar")
				def section =[]
				def sectionMap=[:]
				sectionMap.put("section_name","Favorite Drinks")
				def contents=[]
				def subSections=[]
				def subSectionsMap=[:]
				def photo=[]
				favoriteDrinks.each {
					def fvrtDrink=it
					def contentsMap=[:]
					def options=[]
					contentsMap.put("id",fvrtDrink.id)
					contentsMap.put("description",fvrtDrink.description)
					contentsMap.put("specialInstructions",fvrtDrink.specialInstructions)
					contentsMap.put("photos",photo)
					def category = fvrtDrink.categorys
					def categoryList = category.trim().split(",")
					if(categoryList){
						categoryList.each {
							def categoryId = it
							def categoryObj=IngredientCategory.findById(categoryId)
							if(categoryObj){
								def ingredients = Ingredients.findAllByCategoryAndVenue(categoryObj,venue)
								String name =categoryObj.category
								if(!options.contains(name))
									options.add(name)
							}
						}
					}
					def options_groups_map=[:]
					options_groups_map.put("type","OPTION_SELECT")
					options_groups_map.put("text","")
					options_groups_map.put("options",options)
					contentsMap.put("option_groups",options_groups_map)
					contents.add(contentsMap)
				}
				subSectionsMap.put("subsection_name","")
				subSectionsMap.put("contents",contents)
				subSections.add(subSectionsMap)
				sectionMap.put("subsections",subSections)
				section.add(sectionMap)
				menuMap.put("sections",section)
				menu.add(menuMap)
				output.put("menus",menu)
				output.put("errorCode","0")
				output.put("errorMessage","Favorite Drinks Available")
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

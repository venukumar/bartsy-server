package bartsy

import org.codehaus.groovy.grails.web.json.JSONArray
import grails.converters.JSON
class FavoriteService {

	def serviceMethod() {
	}
	def saveFavoriteDrink(UserProfile user,Venue venue,json){
		def output=[:]
		try{
			def itemsList = json.itemsList
			if(itemsList){
				itemsList.each {
					def itemDetails = it

					def itemName = itemDetails.itemName
					def title = itemDetails.title
					def name = itemDetails.name
					def options_description = itemDetails.options_description
					def type = itemDetails.type
					def order_price = itemDetails.order_price
					def basePrice = itemDetails.price
					def quantity =itemDetails.quantity


					def option_groups=itemDetails.option_groups
					String category
					String selectedItems
					if(option_groups && option_groups.size()>0){
						option_groups.each {
							def option = it
							def text = option.text
							if(text){
								def categoryObj = IngredientCategory.findByCategory(text.trim())
								if(category && !category.contains(categoryObj.id.toString())){
									category=category+","+categoryObj.id
								}
								else{
									category=categoryObj.id
								}
							}
							def options = option.options
							if(options && options.size()>0){
								options.each {
									def ingredient=it
									def selected= ingredient.selected
									if(selected && selected.toBoolean()){
										def ingredientName =ingredient.name
										if(selectedItems){
											selectedItems=selectedItems+","+ingredientName
										}
										else{
											selectedItems=ingredientName
										}
									}
								}
							}
						}
					}
					def usrFvtDrnk = new UserFavoriteDrinks()
					usrFvtDrnk.setItemsList(itemsList.toString())
					usrFvtDrnk.setItemName(itemName)
					usrFvtDrnk.setTitle(title)
					usrFvtDrnk.setBasePrice(basePrice)
					usrFvtDrnk.setName(name)
					usrFvtDrnk.setDescription(options_description)
					usrFvtDrnk.setQuantity(quantity)
					usrFvtDrnk.setQuantity(quantity)
					usrFvtDrnk.setType(type)
					usrFvtDrnk.setUser(user)
					usrFvtDrnk.setVenue(venue)
					usrFvtDrnk.setCategorys(category)
					usrFvtDrnk.setSelectedItems(selectedItems)
					println"END ******************** "
					if(usrFvtDrnk.save(flush:true)) {
						output.put("errorCode","0")
						output.put("errorMessage","Your favorite drink is saved successfully")
						output.put("favoriteDrinkId",usrFvtDrnk.id)
					}else{
						output.put("errorCode","5")
						output.put("errorMessage","Your favorite drink is not saved successfully")
					}
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
				def sectionMap=[:]
				def subSections=[]
				def subSectionsMap=[:]
				def contents=[]
				favoriteDrinks.each {
					def fvrtDrink=it
					def contentsMap=[:]
					def title
					if(fvrtDrink.title){
						contentsMap.put("title",fvrtDrink.title)
					}

					contentsMap.put("type",fvrtDrink.type)
					contentsMap.put("favorite_id",fvrtDrink.id)
					if(fvrtDrink.description){
						contentsMap.put("options_description",fvrtDrink.description)
					}
					if(fvrtDrink.quantity){
						contentsMap.put("quantity",fvrtDrink.quantity)
					}
					if(fvrtDrink.itemName){
						contentsMap.put("itemName",fvrtDrink.itemName)
					}
					if(fvrtDrink.name){
						contentsMap.put("name",fvrtDrink.name)
					}
					contentsMap.put("price","0.0")

					def options_group=[]
					float price=0.0

					def category = fvrtDrink.categorys
					if(category){
						def categoryList = category.trim().split(",")
						if(categoryList){
							categoryList.each {
								def categoryId = it
								def categoryObj=IngredientCategory.findById(categoryId)
								if(categoryObj){
									if(!contentsMap.containsKey("name")){
										contentsMap.put("name", categoryObj.category)
									}
									def options=[]
									def ingredients = Ingredients.findAllByCategoryAndVenue(categoryObj,venue)
									boolean check=false
									ingredients.each{
										def ingredient = it
										def ingredientMap = [:]
										if(ingredient.available.equals("true")){
											//ingredientMap.put("id",ingredient.id)
											ingredientMap.put("name",ingredient.name)
											if(ingredient.price > 1.0){
												ingredientMap.put("price",ingredient.price.toString())
											}
											if(fvrtDrink.selectedItems){
												if(fvrtDrink.selectedItems.contains(ingredient.name)){
													//ingredientMap.put("text","Recommended")
													ingredientMap.put("selected",Boolean.TRUE)
													if(ingredient.price)
														price=price+Float.parseFloat(ingredient.price.toString())
													check=true
												}
											}
											options.add(ingredientMap)
										}
									}

									if(!check && options && options.size()>0){

										def ingredientObj = options[0]
										ingredientObj.put("selected",Boolean.TRUE)
										if(ingredientObj.price)
											price=price+Float.parseFloat(ingredientObj.price.toString())
									}

									if(options.size()>0){
										def options_groups_map=[:]
										//options_groups_map.put("type","OPTION_SELECT")
										if(categoryObj.category.toString().contains("Add-ons"))
											options_groups_map.put("type","OPTION_ADD")
										else
											options_groups_map.put("type","OPTION_CHOOSE")

										options_groups_map.put("text",categoryObj.category)
										options_groups_map.put("options",options)

										options_group.add(options_groups_map)
									}
								}
							}
							contentsMap.put("order_price",price.toString())
							contentsMap.put("option_groups",options_group)
							contents.add(contentsMap)
						}
					}else{
						if(fvrtDrink.itemsList){
							def itemsList = new JSONArray(fvrtDrink.itemsList)
							if(itemsList){
								def locuMenu = JSON.parse(URLDecoder.decode(venue.locuMenu))
								itemsList.each{
									def item = it
									if(locuMenu.toString().contains(item.itemName)){
									contentsMap.put("price",item.price)
									if(item.description){
										contentsMap.put("description",item.description)
									}
									if(item.itemName){
										contentsMap.put("name",item.itemName)
									}
									contentsMap.put("type","ITEM")
									contentsMap.put("option_groups",options_group)

									contents.add(contentsMap)
								}
								}
							}
							//subSectionMap.put("contents",contents)
							//subSections.add(subSectionMap)
							//sectionMap.put("subsections",subSections)
						}
					}
				}
				subSectionsMap.put("contents",contents)
				subSections.add(subSectionsMap)
				sectionMap.put("subsections",subSections)
				section.add(sectionMap)
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






	def getFavoriteDrinksOld(UserProfile user,Venue venue){
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
					sectionMap.put("section_name",title)
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
									contentsMap.put("type","ITEM")
									contentsMap.put("description","")
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

					}else{
						if(fvrtDrink.itemsList){
							def subSectionMap=[:]
							subSectionMap.put("subsection_name","Menu Item")
							def contents=[]
							def itemsList = new JSONArray(fvrtDrink.itemsList)
							if(itemsList){
								itemsList.each{
									def item = it
									def contentsMap=[:]
									contentsMap.put("price",item.basePrice)
									contentsMap.put("description",item.description)
									contentsMap.put("name",item.itemName)
									contentsMap.put("type","ITEM")
									contents.add(contentsMap)
								}
							}
							subSectionMap.put("contents",contents)
							subSections.add(subSectionMap)
							sectionMap.put("subsections",subSections)
						}
					}
					section.add(sectionMap)
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

package bartsy

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject;

class OrderService {

	def serviceMethod() {
	}
	Orders order
	def reOrder(Orders order,Venue venue,UserProfile user){
		try{
			def itemName = order.itemName
			def itemsList = order.itemsList
			if(itemName){
				def locuMenu = JSON.parse(URLDecoder.decode(venue.locuMenu))
				if(locuMenu && locuMenu.toString().contains(itemName)){
					return true
				}else{
					def ingredients = Ingredients.findByName(itemName)
					if(ingredients){
						return true
					}else{
						return false
					}
				}
			}
			if(itemsList){
				def itemsListForCheck = new JSONArray(itemsList)
				if(itemsListForCheck){
					itemsListForCheck.each{
						def ing = it
						def ingts = Ingredients.findByName(ing)
						if(!ingts){
							return false
						}
					}
				}
			}
		}catch(Exception e){
			log.info("Exception in reOrder service ===> "+e.getMessage())
		}
	}


	def getRecentOrders(UserProfile user,Venue venue){
		def output=[:]

		def orders = Orders.createCriteria()

		def recentOrders = orders.list(){
			eq("user",user)
			eq("orderStatus","10")
			order("orderId", "desc")
			maxResults(5)
		}
		if(recentOrders && recentOrders.size()>0){

			def menu=[]
			def menuMap=[:]
			menuMap.put("menu_name","Recently ordered")
			def section =[]
			def sectionMap=[:]
			def subSections=[]
			def subSectionsMap=[:]
			def contents=[]

			recentOrders.each {
				def fvrtDrink=it
				String category
				println"fvrtDrink "+fvrtDrink.orderId
				def itemsListOfOrder = fvrtDrink.itemsList
				JSONArray listOfItems = new JSONArray(itemsListOfOrder)
				contents.add(listOfItems)
				
				
				if(listOfItems){
					listOfItems.each {
						
						def item = it
						
						
						
						//contents.add(item)
						
						
						
						/*
						def item = it
						def name = item.itemName
						if(name){
							def categoryObj = IngredientCategory.findByCategory(name)
							if(categoryObj){
								def ingredients = Ingredients.findByCategory(categoryObj)
								if(ingredients){
									def categoryId = ingredients.category.id

									if(category){
										if(!category.contains(categoryId.toString()))
											category=category+","+categoryId
									}
									else
										category=categoryId
								}
							}
						}
					*/}
				}
				/*def contentsMap=[:]
				contentsMap.put("price","0.0")
				contentsMap.put("type","ITEM")

				if(category){
					println"category "+category
					def categoryList = category.trim().split(",")
					if(categoryList){
						categoryList.each {
							def categoryId = it

							def categoryObj=IngredientCategory.findById(categoryId)
							if(categoryObj){
								if(fvrtDrink.description){
									contentsMap.put("description",fvrtDrink.description)
								}
								if(fvrtDrink.specialInstructions){
									contentsMap.put("specialInstructions",fvrtDrink.specialInstructions)
								}
								contentsMap.put("name", categoryObj.category)
								float price=0.0
								def options=[]
								def ingredients = Ingredients.findAllByCategoryAndVenue(categoryObj,venue)
								ingredients.each{
									def ingredient = it
									def ingredientMap = [:]
									if(ingredient.available.equals("true")){
										ingredientMap.put("name",ingredient.name)
										if(ingredient.price && !ingredient.price.toString().equalsIgnoreCase("0"))
											ingredientMap.put("price",ingredient.price.toString())
										if(fvrtDrink.itemsList.contains(ingredient.name)){
											ingredientMap.put("selected","true".toBoolean())
											price=price+Float.parseFloat(ingredient.price.toString())
										}
										options.add(ingredientMap)
									}
								}

								if(options.size()>0){
									def optionsGroupList=[]
									def options_groups_map=[:]
									//options_groups_map.put("type","OPTION_SELECT")
									if(categoryObj.category.toString().contains("Add-ons"))
										options_groups_map.put("type","OPTION_ADD")
									else
										options_groups_map.put("type","OPTION_CHOOSE")

									options_groups_map.put("text",categoryObj.category)
									options_groups_map.put("options",options)
									optionsGroupList.add(options_groups_map)
									contentsMap.put("option_groups",optionsGroupList)
								}
								contentsMap.put("order_price",price.toString())
								contents.add(contentsMap)
							}
						}
					}
				}else{
					if(fvrtDrink.itemsList){
						def itemsList = new JSONArray(fvrtDrink.itemsList)
						if(itemsList){
							def options=[]
							def map=[:]
							map.put("orderId",fvrtDrink.orderId)
							map.put("order_price",fvrtDrink.totalPrice)
							map.put("type","ITEM")
							float price=0.0

							itemsList.each{
								def item = it
								def optionsGroup=[:]
								optionsGroup.put("price",item.basePrice)
								optionsGroup.put("description",item.description)
								optionsGroup.put("name",item.itemName)
								options.add(optionsGroup)
								price=price+Float.parseFloat(item.basePrice.toString())
							}
							map.put("price",price.toString())
							map.put("option_groups",options)
							contents.add(map)
						}
					}
				}*/
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
				output.put("errorMessage","Recent Orders are Available")
			}else{
				output.put("errorCode","3")
				output.put("errorMessage","No Recent orders are available")
			}
		}else{
			output.put("errorCode","5")
			output.put("errorMessage","Recent orders not available")
		}

		return output
	}

	def getRecentOrdersOld(UserProfile user,Venue venue){
		def output=[:]

		def orders = Orders.createCriteria()

		def recentOrders = orders.list(){
			eq("user",user)
			eq("orderStatus","10")
			order("orderId", "desc")
			maxResults(3)
		}
		if(recentOrders && recentOrders.size()>0){

			def menu=[]
			def menuMap=[:]
			menuMap.put("menu_name","Recently ordered")
			def section =[]
			recentOrders.each {
				def fvrtDrink=it
				def sectionMap=[:]
				sectionMap.put("section_name","")
				sectionMap.put("orderId",fvrtDrink.orderId)
				if(fvrtDrink.description){
					sectionMap.put("description",fvrtDrink.description)
				}
				if(fvrtDrink.specialInstructions){
					sectionMap.put("specialInstructions",fvrtDrink.specialInstructions)
				}

				def subSections=[]
				String category

				def itemsListOfOrder = fvrtDrink.itemsList
				JSONArray listOfItems = new JSONArray(itemsListOfOrder)
				println"itemsListOfOrder "+listOfItems
				if(listOfItems){
					listOfItems.each {
						def item = it
						def name = item.itemName
						if(name){
							def categoryObj = IngredientCategory.findByCategory(name)
							if(categoryObj){
								def ingredients = Ingredients.findByCategory(categoryObj)
								if(ingredients){
									def categoryId = ingredients.category.id

									if(category){
										if(!category.contains(categoryId.toString()))
											category=category+","+categoryId
									}
									else
										category=categoryId
								}
							}
						}
					}
				}
				if(category){
					def categoryList = category.trim().split(",")
					if(categoryList){
						categoryList.each {
							def categoryId = it

							def categoryObj=IngredientCategory.findById(categoryId)
							if(categoryObj){
								def subSectionsMap=[:]
								subSectionsMap.put("subsection_name",categoryObj.category)
								subSectionsMap.put("dateCreated",fvrtDrink.dateCreated.toGMTString())
								subSectionsMap.put("order_price",fvrtDrink.totalPrice)
								def contents=[]
								def contentsMap=[:]
								contentsMap.put("name", categoryObj.category)
								contentsMap.put("type","ITEM")
								def options=[]
								def ingredients = Ingredients.findAllByCategoryAndVenue(categoryObj,venue)
								ingredients.each{
									def ingredient = it
									def ingredientMap = [:]
									if(ingredient.available.equals("true")){
										//ingredientMap.put("id",ingredient.id)
										ingredientMap.put("name",ingredient.name)
										if(ingredient.price && !ingredient.price.toString().equalsIgnoreCase("0"))
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
						subSectionMap.put("dateCreated",fvrtDrink.dateCreated.toGMTString())
						subSectionMap.put("order_price",fvrtDrink.totalPrice)
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
				output.put("errorMessage","Recent Orders are Available")
			}else{
				output.put("errorCode","3")
				output.put("errorMessage","No Recent orders are available")
			}


		}else{
			output.put("errorCode","5")
			output.put("errorMessage","Recent orders not available")
		}

		return output
	}
}

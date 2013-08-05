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
			maxResults(3)
		}
		if(recentOrders && recentOrders.size()>0){

			def menu=[]
			def menuMap=[:]
			menuMap.put("menu_name","Recent Orders")
			def section =[]
			recentOrders.each {
				def fvrtDrink=it
				def sectionMap=[:]
				sectionMap.put("section_name","")
				sectionMap.put("orderId",fvrtDrink.orderId)
				sectionMap.put("description",fvrtDrink.description)
				sectionMap.put("specialInstructions",fvrtDrink.specialInstructions)

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
						subSectionMap.put("dateCreated",fvrtDrink.dateCreated.toGMTString())
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
			output.put("errorCode",4)
			output.put("errorMessage","Recent orders not available")
		}

		return output
	}
}

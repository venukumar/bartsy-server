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
		try{
			def orders = Orders.createCriteria()

			def recentOrders = orders.list(){
				eq("user",user)
				order("orderId", "desc")
				maxResults(5)
			}
			if(recentOrders){
				def menu=[]
				def menuMap=[:]
				menuMap.put("menu_name","Recently ordered")
				def section =[]
				def sectionMap=[:]
				def subSections=[]
				def subSectionsMap=[:]
				def contents=[]
				println"recentOrders "+recentOrders.size()

				recentOrders.each {
					def recentOrder=it
					def itemsOfOrder=OrderItems.findAllByOrder(recentOrder)
					if(itemsOfOrder){

						itemsOfOrder.each{
							def order=it
							def contentsMap=[:]
							if(order.title){
								contentsMap.put("title",order.title)
							}
							contentsMap.put("type",order.type)
							if(order.quantity){
								contentsMap.put("quantity",order.quantity)
							}
							if(order.itemName){
								contentsMap.put("itemName",order.itemName)
							}
							if(order.name){
								contentsMap.put("name",order.name)
							}
							if(order.specialInstructions){
								contentsMap.put("special_instructions",order.specialInstructions)
							}
							contentsMap.put("price","0.0")

							def options_group=[]
							float price=0.0
							def category = order.categorys
							if(category){
								if(order.description){
									contentsMap.put("options_description",order.description)
								}
								def categoryList = category.trim().split(",")
								if(categoryList){
									println"categoryList "+categoryList
									boolean categoryCheck=false
									categoryList.each {
										def categoryId = it
										def categoryObj=IngredientCategory.findById(categoryId)
										if(categoryObj){
											println"categoryObj "+categoryObj.category
											if(!contentsMap.containsKey("name")){
												contentsMap.put("name", categoryObj.category)
											}
											def options=[]
											def ingredients = Ingredients.findAllByCategoryAndVenue(categoryObj,venue)
											boolean check=false
											println"Ingredients "+Ingredients
											ingredients.each{
												def ingredient = it
												def ingredientMap = [:]
												if(ingredient.available.equals("true")){
													//ingredientMap.put("id",ingredient.id)
													ingredientMap.put("name",ingredient.name)
													if(ingredient.price > 1.0){
														ingredientMap.put("price",ingredient.price.toString())
													}
													if(order.selectedItems){
														def selectedItems=order.selectedItems.toString().split(",")
														if(selectedItems.contains(ingredient.name)){
															//ingredientMap.put("text","Recommended")
															ingredientMap.put("selected",Boolean.TRUE)
															if(ingredient.price)
																price=price+Float.parseFloat(ingredient.price.toString())
															check=true
															categoryCheck=true
														}
													}
													options.add(ingredientMap)
												}
											}

											if(!check){
												if(order.selectedItems && order.basePrice &&  order.basePrice.equalsIgnoreCase("0.0") ){
													if(!categoryCheck && options && options.size()>0){
														categoryCheck=true
														def ingredientObj = options[0]
														ingredientObj.put("selected",Boolean.TRUE)
														if(ingredientObj.price)
															price=price+Float.parseFloat(ingredientObj.price.toString())
													}
												}
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
								if(order.itemList){
									println"order.itemList "+order.itemList
									def itemsList = new JSONObject(order.itemList)
									if(itemsList){
										def locuMenu = JSON.parse(URLDecoder.decode(venue.locuMenu))
										if(locuMenu.toString().contains(itemsList.itemName)){
											contentsMap.put("price",itemsList.price)
											if(itemsList.description){
												contentsMap.put("description",itemsList.description)
											}
											if(itemsList.itemName){
												contentsMap.put("name",itemsList.itemName)
											}
											if(itemsList.specialInstructions){
												contentsMap.put("special_instructions",itemsList.specialInstructions)
											}
											contentsMap.put("type","ITEM")
											contentsMap.put("option_groups",options_group)

											contents.add(contentsMap)
										}

									}
									//subSectionMap.put("contents",contents)
									//subSections.add(subSectionMap)
									//sectionMap.put("subsections",subSections)
								}
							}
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
					output.put("errorMessage","Recent Orders are Available")
				}else{
					output.put("errorCode","3")
					output.put("errorMessage","No Recent orders are available")
				}
			}else{
				output.put("errorCode","5")
				output.put("errorMessage","Recent orders not available")
			}
		}catch(Exception e){
			log.info("Exception found in getRecentOrders service "+e.getMessage())
			println"Exception found in getFavoriteDrinks service "+e.getMessage()
			output.put("errorCode",200)
			output.put("errorMessage","Error occured while processing your request. Please verify json")
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
				def order=it
				def sectionMap=[:]
				sectionMap.put("section_name","")
				sectionMap.put("orderId",order.orderId)
				if(order.description){
					sectionMap.put("description",order.description)
				}
				if(order.specialInstructions){
					sectionMap.put("specialInstructions",order.specialInstructions)
				}

				def subSections=[]
				String category

				def itemsListOfOrder = order.itemsList
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
								subSectionsMap.put("dateCreated",order.dateCreated.toGMTString())
								subSectionsMap.put("order_price",order.totalPrice)
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
										if(order.itemsList.contains(ingredient.name)){
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
					if(order.itemsList){
						def subSectionMap=[:]
						subSectionMap.put("subsection_name","Menu Item")
						subSectionMap.put("dateCreated",order.dateCreated.toGMTString())
						subSectionMap.put("order_price",order.totalPrice)
						def contents=[]
						def itemsList = new JSONArray(order.itemsList)
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

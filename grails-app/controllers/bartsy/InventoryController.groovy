package bartsy

import grails.converters.JSON

class InventoryController {

	def saveIngredients = {
		try{
			def response = [:]
			def json =  JSON.parse(request)
			def venue = Venue.findByVenueId(json.venueId)
			if(venue){
				def type = IngredientType.findByType(json.type)
				if(!type){
					type =  new IngredientType()
					type.setType(json.type)
					type.save(flush:true)
				}
				def category =  IngredientCategory.findByCategory(json.category)
				if(!category){
					category =  new IngredientCategory()
					category.setCategory(json.category)
					category.save(flush:true)
				}
				def ingredients = json.ingredients
				if(ingredients)
				{
					ingredients.each{
						def ingredient =  it
						def ingredientToSave = Ingredients.findByIngredientId(ingredient.ingredientId as long)
						if(ingredientToSave){
							ingredientToSave.setPrice(ingredient.price as int)
							ingredientToSave.setAvailable(ingredient.available)
							ingredientToSave.save(flush:true)
						}
						else{
						ingredientToSave =  new Ingredients()
						ingredientToSave.setIngredientId(ingredient.ingredientId as long)
						ingredientToSave.setName(ingredient.name)
						ingredientToSave.setPrice(ingredient.price as int)
						ingredientToSave.setAvailable(ingredient.available)
						ingredientToSave.setType(type)
						ingredientToSave.setCategory(category)
						ingredientToSave.setVenue(venue)
						ingredientToSave.save(flush:true)
						}
					}
				}
				else{
					response.put("errorCode","0")
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
}

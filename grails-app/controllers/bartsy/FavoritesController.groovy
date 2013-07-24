package bartsy

import javassist.bytecode.stackmap.BasicBlock.Catch;
import grails.converters.JSON

class FavoritesController {

	def saveUserFavoritePeople={
		def response=[:]
		try{
			def json = JSON.parse(request)
			if(json){

				if(json.has("bartsyId")){
					if(json.has("favoriteBartsyId")){
						if(json.has("status")){

							def userId=json.bartsyId
							def favoriteUserId=json.favoriteBartsyId
							def status=json.status

							boolean user=verifyBartsyId(userId)
							if(!user)
								handleNegativeResponse(response,"BartsyId does not exists")
							else{
								boolean favoriteUser = verifyBartsyId(favoriteUserId)
								if(!favoriteUser)
									handleNegativeResponse(response,"Favorite BartsyId does not exists")
								else{
									def userFavoritePeople = new UserFavoritePeople()
									userFavoritePeople.setFavoriteBartsyId(favoriteUserId)
									userFavoritePeople.setStaus(status)
									if(userFavoritePeople.save(flush:true)){
										response.put("errorCode", 1)
										response.put("errorMessage", "Data saved ")
									}
								}
							}
						}else
							handleNegativeResponse(response,"Status should not be empty or null")
					}else
						handleNegativeResponse(response,"Favorite BartsyId should not be empty or null")
				}else
					handleNegativeResponse(response,"BartsyId should not be empty or null")
			}else
				handleNegativeResponse(response,"Json should not be empty")

			response.put("currentTime",new Date().toGMTString())
		}catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}finally{
			render(text:response as JSON,contentType:"application/json")
		}
	}

	/*	def getUserFavoritePeople={
	 def response=[:]
	 try{
	 def json = JSON.parse(request)
	 if(json){
	 if(json.has("bartsyId")){
	 def 
	 }else
	 handleNegativeResponse(response,"BartsyId should not be empty or null")
	 }else
	 handleNegativeResponse(response,"Json should not be empty")
	 }catch(Exception e){
	 log.info("Exception is ===> "+e.getMessage())
	 response.put("errorCode",200)
	 response.put("errorMessage",e.getMessage())
	 }finally{
	 render(text:response as JSON,contentType:"application/json")
	 }
	 }*/

	/**
	 * To verify the user profile exists or not
	 */
	def verifyBartsyId(bartsyId){
		boolean user = false
		def userProfile = UserProfile.findByBartsyId(bartsyId)
		if(userProfile)
			user=true
		return user
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


	def saveFavoriteDrink={
		def response=[:]
		try{
			def json = JSON.parse(request)
			if(json){
				def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
				if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
					
					
					
					
					
				}else{
					response.put("errorCode",100)
					response.put("errorMessage","API version do not match")
				}
			}
		}catch(Exception e){
			log.info("Exception found in saveFavoriteDrink "+e.getMessage())
			println"Exception found in saveFavoriteDrink "+e.getMessage()
			response.put("errorCode",200)
			response.put("errorMessage","Error occured while processing your request. Please verify json")
		}
		finally{
			response.put("currentTime",new Date().toGMTString())
			render(text:response as JSON,contentType:"application/json")
		}
	}

	def favoriteVenues={
		def response=[:]
		try{
			def json = JSON.parse(request)
			if(json){
				def bartsyId
				def venueId
				def venue
				def user

				if(json.has("bartsyId"))
					bartsyId=json.bartsyId
				if(json.has("venueId"))
					venueId = json.venueId
				if(venueId)
					venue = Venue.findByVenueId(venueId)
				if(bartsyId)
					user=UserProfile.findByBartsyId(bartsyId)

				if(venue){
					if(user){

						def fvtVenue = UserFavoriteVenues.findByUserAndVenue(user,venue)
						if(!fvtVenue){
							UserFavoriteVenues userFvtVenue = new UserFavoriteVenues()
							userFvtVenue.venue=venue
							userFvtVenue.user=user
							if(userFvtVenue.save(flush:true)){
								response.put("errorCode","0")
								response.put("errorMessage",venue.venueName+" successfully saved as favorite venue")
							}else{
								handleNegativeResponse(response,venue.venueName+" not saved successfully")
							}
						}
					}else{
						handleNegativeResponse(response,"User does not exists")
					}
				}	else{
					handleNegativeResponse(response,"Venue does not exists")
				}
			}
			else
				handleNegativeResponse(response,"Request should not be empty")
		}catch(Exception e){
			log.info("Exception found in favoritesVenue "+e.getMessage())
			println("Exception found in favoritesVenue "+e.getMessage())
		}
	}
}

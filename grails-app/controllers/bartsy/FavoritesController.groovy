package bartsy

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
}

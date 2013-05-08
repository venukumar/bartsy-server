package bartsy

import grails.converters.JSON


class UserController {

	def index() {
	}
	
	/**
	 * Author 		: Swetha Bhatnagar
	 * Description	: This is a webservice to be called while user regsitration to store user profile data into the server		
	 */
	def saveUserProfile = {
		def json = JSON.parse(request)
		Map response = new HashMap()
		def userProfile = UserProfile.findByUserName(json.userName)
		if(userProfile) {
			response.put("bartsyUserId",userProfile.bartsyId)
			response.put("errorCode","1")
			response.put("errorMessage","UserId already exists")
		}
		else{
			UserProfile userProfileToSave = new UserProfile()
			userProfileToSave.setUserName(json.userName)
			userProfileToSave.setName(json.name)
			userProfileToSave.setLoginId(json.loginId)
			userProfileToSave.setLoginType(json.loginType)
			userProfileToSave.setGender(json.gender)
			userProfileToSave.setDeviceToken(json.deviceToken)
			userProfileToSave.setDeviceType(json.deviceType as int)
			def maxId = UserProfile.createCriteria().get { projections { max "bartsyId"
				} } as Long
			if(maxId){
				maxId = maxId+1
			}
			else{
				maxId = 100001
			}
			userProfileToSave.setBartsyId(maxId)
			if(userProfileToSave.save()){
				response.put("bartsyUserId",maxId)
				response.put("errorCode","0")
				response.put("errorMessage","Save Successful")
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage","Save not successful")
			}
		}
		render(text:response as JSON ,  contentType:"application/json")
	}
	
	
	/**
	 * Author 		: Swetha Bhatnagar
	 * Description	: This is a webservice to be called while user checks in to store user status into the server
	 */
	def userCheckIn = {
		def json = JSON.parse(request)
		def userProfile = UserProfile.findByBartsyId(json.bartsyId)
		def venue = Venue.findByVenueId(json.venueId)
		def response = [:]
		if(userProfile && venue){
			if(CheckedInUsers.findByUserProfileAndVenue(userProfile,venue)){
				response.put("errorCode","1")
				response.put("errorMessage","User already Checked In")
			}
			else{
				def checkedInUsers = new CheckedInUsers()
				checkedInUsers.setUserProfile(userProfile)
				checkedInUsers.setVenue(venue)
				if(checkedInUsers.save(flush:true)){
					response.put("errorCode","0")
					response.put("errorMessage","User Checked In Successfully")
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage","User check in failed")
				}
			}
		}
		else{
			response.put("errorCode","1")
			response.put("errorMessage","User ID or Venue ID does not exist")
		}
		render(text:response as JSON ,  contentType:"application/json")
	}
}

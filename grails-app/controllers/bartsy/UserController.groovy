package bartsy

import grails.converters.JSON


class UserController {

	def androidPNService

	/**
	 * This is the webservice to save the user registration details received from the customer application.
	 *
	 * @author Swetha Bhatnagar
	 *
	 * @errorCodes 1 : failure, 0 : success
	 *
	 * @param userName         		userName of google+ or facebook
	 * @param deviceToken    		GCM registration id for Android and device id for IOS
	 * @param gender    			gender of the user
	 * @param userImage       		profile picture of the user 
	 * @param name   				name of the user
	 * @param loginId  				google+ or facebook loginId
	 * @param deviceType		    deviceType is Android=0 or IOS=1
	 * @param loginType   			login type : facebook or google
	 *
	 * @return  {
	 * @return      errorCode 		: success/failure code
	 * @return      errorMessage 	: success/failure message
	 * @return      bartsyUserId	: bartsy id generated for that user
	 * @return  }
	 *
	 **/
	def saveUserProfile = {
		try{
			def json = JSON.parse(params.details)
			Map response = new HashMap()
			UserProfile userProfileToSave = new UserProfile()
			if(json.deviceToken.equals("") || json.deviceToken == null ){
				response.put("errorCode","1")
				response.put("errorMessage","GCM Registration ID is empty")
			}
			else{
				def userProfile = UserProfile.findByUserName(json.userName)
				if(userProfile) {
					userProfile.setName(json.name)
					userProfile.setLoginId(json.loginId.toString())
					userProfile.setLoginType(json.loginType)
					userProfile.setGender(json.gender)
					userProfile.setDeviceToken(json.deviceToken)
					userProfile.setDeviceType(json.deviceType as int)
					def userImageFile = request.getFile("userImage")
					def webRootDir = servletContext.getRealPath("/")
					def userDir = new File("Bartsy/userImages/")
					userDir.mkdirs()
					String tmp = userProfile.bartsyId.toString()
					userImageFile.transferTo( new File( userDir, tmp))
					def userImagePath = "Bartsy/userImages/"+tmp
					userProfile.setUserImage(userImagePath)
					if(userProfile.save()){
						response.put("bartsyUserId",userProfile.bartsyId)
						response.put("errorCode","0")
						response.put("errorMessage","User Profile updated")
					}
					else{
						response.put("bartsyUserId",userProfile.bartsyId)
						response.put("errorCode","1")
						response.put("errorMessage","Save not successful")
					}
					response.put("userExists","0")
				}
				else{
					userProfileToSave.setUserName(json.userName)
					userProfileToSave.setName(json.name)
					userProfileToSave.setLoginId(json.loginId.toString())
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
					def userImageFile = request.getFile("userImage")
					def webRootDir = servletContext.getRealPath("/")
					def userDir = new File("Bartsy/userImages/")
					userDir.mkdirs()
					String tmp = maxId.toString()
					userImageFile.transferTo( new File( userDir, tmp))
					def userImagePath = "Bartsy/userImages/"+tmp
					userProfileToSave.setUserImage(userImagePath)
					if(userProfileToSave.save()){
						response.put("bartsyUserId",maxId)
						response.put("errorCode","0")
						response.put("errorMessage","Save Successful")
					}
					else{
						response.put("errorCode","1")
						response.put("errorMessage","Save not successful")
					}
					response.put("userExists","1")
				}
			}
			render(text:response as JSON ,  contentType:"application/json")
		}catch(Exception e){
			println "Exception:"+e.getMessage()
		}
	}

	/**
	 * This is the webservice to check in a user into a venue
	 *
	 * @author Swetha Bhatnagar
	 *
	 * @errorCodes 1 : failure, 0 : success
	 *
	 * @param venueId         		server generated id for the venue
	 * @param bartsyId    			server generated id for the user
	 *
	 * @return  {
	 * @return      errorCode 		: success/failure code
	 * @return      errorMessage 	: success/failure message
	 * @return  }
	 *
	 **/
	def userCheckIn = {
		def json = JSON.parse(request)
		def userProfile = UserProfile.findByBartsyId(json.bartsyId)
		def venue = Venue.findByVenueId(json.venueId)
		def response = [:]
		CheckedInUsers userCheckedIn
		if(userProfile && venue){
			userCheckedIn = CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,1)
			if(userCheckedIn){
				response.put("errorCode","0")
				response.put("errorMessage","User already Checked In the selected venue")
			}
			else{
				userCheckedIn = CheckedInUsers.findByUserProfileAndStatus(userProfile,1)
				if(userCheckedIn){
					userCheckedIn.setStatus(0)
					userCheckedIn.save(flush:true)
				}
				def checkedInUsers = CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,0)
				if(!checkedInUsers){
					checkedInUsers = new CheckedInUsers()
				}
				checkedInUsers.setUserProfile(userProfile)
				checkedInUsers.setVenue(venue)
				checkedInUsers.setStatus(1)
				if(checkedInUsers.save(flush:true)){
					response.put("errorCode","0")
					response.put("errorMessage","User Checked In Successfully")
					Map pnMessage = new HashMap()
					pnMessage.put("bartsyId",userProfile.getBartsyId())
					pnMessage.put("gender",userProfile.getGender())
					pnMessage.put("name",userProfile.getName())
					pnMessage.put("messageType","userCheckIn")
					pnMessage.put("userImagePath",userProfile.getUserImage())
					androidPNService.sendPN(pnMessage,venue.deviceToken)
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

	/**
	 * This is the webservice to check out a user from a venue
	 *
	 * @author Swetha Bhatnagar
	 *
	 * @errorCodes 1 : failure, 0 : success
	 *
	 * @param venueId         		server generated id for the venue
	 * @param bartsyId    			server generated id for the user
	 *
	 * @return  {
	 * @return      errorCode 		: success/failure code
	 * @return      errorMessage 	: success/failure message
	 * @return  }
	 *
	 **/
	def userCheckOut = {
		def json = JSON.parse(request)
		def userProfile = UserProfile.findByBartsyId(json.bartsyId)
		def venue = Venue.findByVenueId(json.venueId)
		def response = [:]
		if(userProfile && venue){
			if(CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,0)){
				response.put("errorCode","0")
				response.put("errorMessage","User already Checked out from the selected venue")
			}
			else{
				def checkedInUsers = CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,1)
				if(!checkedInUsers){
					checkedInUsers = new CheckedInUsers()
				}
				checkedInUsers.setUserProfile(userProfile)
				checkedInUsers.setVenue(venue)
				checkedInUsers.setStatus(0)
				if(checkedInUsers.save(flush:true)){
					response.put("errorCode","0")
					response.put("errorMessage","User Checked Out Successfully")
					Map pnMessage = new HashMap()
					pnMessage.put("bartsyId",userProfile.bartsyId)
					pnMessage.put("gender",userProfile.gender)
					pnMessage.put("name",userProfile.name)
					pnMessage.put("messageType","userCheckOut")
					pnMessage.put("userImagePath",userProfile.getUserImage())
					androidPNService.sendPN(pnMessage,venue.deviceToken)
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage","User check in failed")
				}
			}
		}
		else{
			response.put("errorCode","0")
			response.put("errorMessage","User ID or Venue ID does not exist")
		}
		render(text:response as JSON ,  contentType:"application/json")
	}
}

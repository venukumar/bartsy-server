package bartsy

import grails.converters.JSON


class UserController {
	
	def androidPNService

	def index() {
	}
	
	/**
	 * Author 		: Swetha Bhatnagar
	 * Description	: This is a webservice to be called while user regsitration to store user profile data into the server		
	 */
	def saveUserProfile = {
		def json = JSON.parse(request)
		Map response = new HashMap()
		UserProfile userProfileToSave = new UserProfile()
		def userProfile = UserProfile.findByUserNameAndDeviceType(json.userName,json.deviceType)
		if(userProfile) {
			userProfileToSave.setName(json.name)
			userProfileToSave.setLoginId(json.loginId)
			userProfileToSave.setLoginType(json.loginType)
			userProfileToSave.setGender(json.gender)
			userProfileToSave.setDeviceToken(json.deviceToken)
			userProfileToSave.setUserImage("Swetha")
			if(userProfileToSave.save()){
			response.put("bartsyUserId",userProfile.bartsyId)
			response.put("errorCode","1")
			response.put("errorMessage","User Profile updated")
			}
			else{
				response.put("bartsyUserId",userProfile.bartsyId)
				response.put("errorCode","1")
				response.put("errorMessage","User Profile already exists")
			}
		}
		else{			
			userProfileToSave.setUserName(json.userName)
			userProfileToSave.setName(json.name)
			userProfileToSave.setLoginId(json.loginId)
			userProfileToSave.setLoginType(json.loginType)
			userProfileToSave.setGender(json.gender)
			userProfileToSave.setDeviceToken(json.deviceToken)
			userProfileToSave.setDeviceType(json.deviceType as int)
			userProfileToSave.setUserImage("Swetha")
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
					pnMessage.put("bartsyId",userProfile.bartsyId)
					pnMessage.put("gender",userProfile.gender)
					pnMessage.put("name",userProfile.name)
					pnMessage.put("messageType","userCheckIn")
					//userProfileMap.put("userImage",userProfile.userImage)
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
					//userProfileMap.put("userImage",userProfile.userImage)
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
	
	def saveUserProfileTest = {
		def json = JSON.parse(params.details)
		println "josn:"+ json
		Map response = new HashMap()
		UserProfile userProfileToSave = new UserProfile()
		def userProfile = UserProfile.findByUserNameAndDeviceType(json.userName,json.deviceType)
		if(userProfile) {
			userProfileToSave.setName(json.name)
			userProfileToSave.setLoginId(json.loginId)
			userProfileToSave.setLoginType(json.loginType)
			userProfileToSave.setGender(json.gender)
			userProfileToSave.setDeviceToken(json.deviceToken)
			def userImageFile = request.getFile("userImage")
			def webRootDir = servletContext.getRealPath("/")
			def userDir = new File("web-app/userImages/")
			userDir.mkdirs()
			String tmp = json.userName.toString()
			userImageFile.transferTo( new File( userDir, tmp))
			def userImagePath = "userImages/"+tmp
			println userImagePath
			userProfileToSave.setUserImage(userImagePath)
			if(userProfileToSave.save()){
			response.put("bartsyUserId",userProfile.bartsyId)
			response.put("errorCode","1")
			response.put("errorMessage","User Profile updated")
			}
			else{
				response.put("bartsyUserId",userProfile.bartsyId)
				response.put("errorCode","1")
				response.put("errorMessage","User Profile already exists")
			}
		}
		else{
			userProfileToSave.setUserName(json.userName)
			userProfileToSave.setName(json.name)
			userProfileToSave.setLoginId(json.loginId)
			userProfileToSave.setLoginType(json.loginType)
			userProfileToSave.setGender(json.gender)
			userProfileToSave.setDeviceToken(json.deviceToken)
			userProfileToSave.setDeviceType(json.deviceType as int)
			def userImageFile = request.getFile("userImage")
			def webRootDir = servletContext.getRealPath("/")
			def userDir = new File("web-app/userImages/")
			userDir.mkdirs()
			String tmp = json.userName.toString()
			userImageFile.transferTo( new File( userDir, tmp))
			def userImagePath = "userImages/"+tmp
			println userImagePath
			userProfileToSave.setUserImage(userImagePath)
			//userProfileToSave.setUserImage(json.userImage)
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
}

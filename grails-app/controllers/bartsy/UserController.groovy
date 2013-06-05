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
					userProfile.setFirstName(json.firstname)
					userProfile.setLastName(json.lastname)
					userProfile.setDateOfBirth(json.dateofbirth)
					userProfile.setNickName(json.nickname)
					userProfile.setDescription(json.description)
					userProfile.setOrientation(json.orientation)
					userProfile.setStatus(json.status)
					userProfile.setLoginId(json.loginId.toString())
					userProfile.setLoginType(json.loginType)
					userProfile.setGender(json.gender)
					userProfile.setDeviceToken(json.deviceToken)
					userProfile.setDeviceType(json.deviceType as int)
					userProfile.setShowProfile("ON")
					userProfile.setShowProfileUpdated(new Date())
					def userImageFile = request.getFile("userImage")
					def webRootDir = servletContext.getRealPath("/")
					def userDir = new File(message(code:'userimage.path'))
					userDir.mkdirs()
					String tmp = userProfile.bartsyId.toString()
					userImageFile.transferTo( new File( userDir, tmp))
					def userImagePath = message(code:'userimage.path.save')+tmp
					userProfile.setUserImage(userImagePath)
					if(userProfile.save()){
						response.put("bartsyUserId",userProfile.bartsyId)
						response.put("errorCode","0")
						response.put("errorMessage","User Profile updated")
						def userCheckedIn = CheckedInUsers.findByUserProfileAndStatus(userProfile,1)
						if(userCheckedIn){
							response.put("userCheckedIn","0")
							response.put("venueId",userCheckedIn.venue.venueId)
							response.put("venueName",userCheckedIn.venue.venueName)
						}
						else{
							response.put("userCheckedIn","1")
						}
					}
					else{
						response.put("bartsyUserId",userProfile.bartsyId)
						response.put("errorCode","1")
						response.put("errorMessage","Save not successful")
					}
				}
				else{
					userProfileToSave.setUserName(json.userName)
					userProfileToSave.setFirstName(json.firstname)
					userProfileToSave.setLastName(json.lastname)
					userProfileToSave.setDateOfBirth(json.dateofbirth)
					userProfileToSave.setNickName(json.nickname)
					userProfileToSave.setDescription(json.description)
					userProfileToSave.setOrientation(json.orientation)
					userProfileToSave.setStatus(json.status)
					userProfileToSave.setName(json.name)
					userProfileToSave.setLoginId(json.loginId.toString())
					userProfileToSave.setLoginType(json.loginType)
					userProfileToSave.setGender(json.gender)
					userProfileToSave.setDeviceToken(json.deviceToken)
					userProfileToSave.setDeviceType(json.deviceType as int)
					userProfileToSave.setShowProfile("ON")
					userProfileToSave.setShowProfileUpdated(new Date())
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
					def userDir = new File(message(code:'userimage.path'))
					userDir.mkdirs()
					String tmp = maxId.toString()
					userImageFile.transferTo( new File( userDir, tmp))
					def userImagePath = message(code:'userimage.path.save')+tmp
					userProfileToSave.setUserImage(userImagePath)
					if(userProfileToSave.save()){
						response.put("bartsyUserId",maxId)
						response.put("errorCode","0")
						response.put("errorMessage","Save Successful")
						response.put("userCheckedIn","1")
					}
					else{
						response.put("errorCode","1")
						response.put("errorMessage","Save not successful")
					}
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
			if(venue.status.equals("CLOSED")){
				response.put("errorCode","1")
				response.put("errorMessage","Venue is Closed")
			}
			else{
				userCheckedIn = CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,1)
				if(userCheckedIn){
					response.put("errorCode","0")
					response.put("errorMessage","User already Checked In the selected venue")
				}
				else{
					userCheckedIn = CheckedInUsers.findByUserProfileAndStatus(userProfile,1)
					if(userCheckedIn){
						userCheckedIn.setStatus(0)
						if(userCheckedIn.save(flush:true)){
							def cancelledOrders = cancelOpenOrders(userCheckedIn.userProfile)
							Map pnMessage = new HashMap()
							pnMessage.put("cancelledOrders",cancelledOrders)
							pnMessage.put("bartsyId",userProfile.bartsyId)
							pnMessage.put("gender",userProfile.gender)
							pnMessage.put("name",userProfile.name)
							pnMessage.put("messageType","userCheckOut")
							pnMessage.put("userImagePath",userProfile.getUserImage())
							androidPNService.sendPN(pnMessage,userCheckedIn.venue.deviceToken)
						}
					}
					def checkedInUsers = CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,0)
					if(!checkedInUsers){
						checkedInUsers = new CheckedInUsers()
					}
					checkedInUsers.setUserProfile(userProfile)
					checkedInUsers.setVenue(venue)
					checkedInUsers.setStatus(1)
					checkedInUsers.setLastHBResponse(new Date())
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
					def cancelledOrders = cancelOpenOrders(checkedInUsers.userProfile)
					Map pnMessage = new HashMap()
					pnMessage.put("cancelledOrders",cancelledOrders)
					pnMessage.put("bartsyId",userProfile.bartsyId)
					pnMessage.put("messageType","userCheckOut")
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

	def List cancelOpenOrders(UserProfile userProfile){
		def cancelledOrders = []
		Orders order = new Orders()
		def openOrdersCriteria = Orders.createCriteria()
		def openOrders = openOrdersCriteria.list {
			eq("user",userProfile)
			and{
				'in'("orderStatus",["0", "2", "3"])
			}
		}
		if(openOrders){
			openOrders.each{
				order=it
				cancelledOrders.add(order.orderId)
				order.setOrderStatus("7")
				//order.setUpdateTime(new Date())
				order.save(flush:true)
			}
		}
		return cancelledOrders
	}

	def heartBeat = {
		def json = JSON.parse(request)
		def userProfile = UserProfile.findByBartsyId(json.bartsyId)
		def venue = Venue.findByVenueId(json.venueId)
		def response = [:]
		if(userProfile && venue){
			def checkedInUsers = CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,1)
			if(checkedInUsers){
				checkedInUsers.setLastHBResponse(new Date())
				checkedInUsers.save(flush:true)
			}
		}
		response.put("errorCode","0")
		response.put("errorMessage","Request Received")
		render(text:response as JSON ,  contentType:"application/json")
	}

	def setShowProfile = {
		def json =  JSON.parse(request)
		def userProfile = UserProfile.findByBartsyId(json.bartsyId as long)
		if(userProfile){
			use(groovy.time.TimeCategory){
				def diff = new Date() - user.showProfileUpdated
				//log.warn("difference in minutes"+diff.minutes)
				if(diff.hours >= 24){
					userProfile.setStatus(json.status)
					userProfile.setShowProfileUpdated(new Date())
					if(userProfile.save(flush:true))
					{
						response.put("errorCode","0")
						response.put("errorMessage","Show profile updated")
					}
					else{
						response.put("errorCode","1")
						response.put("errorMessage","Show profile not updated")
					}
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage","Cannot Update this setting for 24 hrs from last update")
				}
			}
		}
		else{
			response.put("errorCode","1")
			response.put("errorMessage","User Id does not exists")
		}
		render(text:response as JSON ,  contentType:"application/json")
	}
}

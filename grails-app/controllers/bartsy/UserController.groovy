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
			def userImageFile = request.getFile("userImage")
			// checking user image is posted or not
			if(userImageFile){
			if(json.deviceToken.equals("") || json.deviceToken == null ){
				response.put("errorCode","1")
				response.put("errorMessage","GCM Registration ID is empty")
			}else if(json.password && json.userName ){
			
			response.put("errorCode","1")
			response.put("errorMessage","Please post username and password")
			}
			else{
				def userProfile = UserProfile.findByUserName(json.userName)
				if(userProfile) {
					userProfile.setName(json.name ?: "")
					userProfile.setFirstName(json.firstname ?: "")
					userProfile.setLastName(json.lastname ?: "")
					userProfile.setDateOfBirth(json.dateofbirth ?: "")
					userProfile.setNickName(json.nickname ?: "")
					userProfile.setDescription(json.description ?: "")
					userProfile.setOrientation(json.orientation ?: "")
					userProfile.setStatus(json.status ?: "")
					userProfile.setLoginId(json.loginId.toString() ?: "")
					userProfile.setLoginType(json.loginType ?: "")
					userProfile.setGender(json.gender ?: "")
					userProfile.setDeviceToken(json.deviceToken ?: "")
					userProfile.setDeviceType(json.deviceType as int)
					userProfile.setShowProfile("ON")
					userProfile.setShowProfileUpdated(new Date())
					userProfile.setEmailId(json.emailId ?: "")
					userProfile.setPassword(json.password?:"")
					//def userImageFile = request.getFile("userImage")
					def webRootDir = servletContext.getRealPath("/")
					def userDir = new File(message(code:'userimage.path'))
					userDir.mkdirs()
					String tmp = userProfile.bartsyId.toString()
					userImageFile.transferTo( new File( userDir, tmp))
					def userImagePath = message(code:'userimage.path.save')+tmp
					userProfile.setUserImage(userImagePath)
					if(userProfile.save()){
						response.put("bartsyId",userProfile.bartsyId)
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
						response.put("bartsyId",userProfile.bartsyId)
						response.put("errorCode","1")
						response.put("errorMessage","Save not successful")
					}
				}
				else{
					userProfileToSave.setUserName(json.userName ?: "")
					userProfileToSave.setFirstName(json.firstname ?: "")
					userProfileToSave.setLastName(json.lastname ?: "")
					userProfileToSave.setDateOfBirth(json.dateofbirth ?: "")
					userProfileToSave.setNickName(json.nickname ?: "")
					userProfileToSave.setDescription(json.description ?: "")
					userProfileToSave.setOrientation(json.orientation ?: "")
					userProfileToSave.setStatus(json.status ?: "")
					userProfileToSave.setName(json.name ?: "")
					userProfileToSave.setLoginId(json.loginId.toString() ?: "")
					userProfileToSave.setLoginType(json.loginType ?: "")
					userProfileToSave.setGender(json.gender ?: "")
					userProfileToSave.setDeviceToken(json.deviceToken ?: "")
					userProfileToSave.setDeviceType(json.deviceType as int)
					userProfileToSave.setShowProfile("ON")
					userProfileToSave.setShowProfileUpdated(new Date())
					userProfileToSave.setEmailId(json.emailId ?: "")
					def maxId = UserProfile.createCriteria().get {
						projections {
							max "bartsyId"
						}
					} as Long
					if(maxId){
						maxId = maxId+1
					}
					else{
						maxId = 100001
					}
					userProfileToSave.setBartsyId(maxId)
					def webRootDir = servletContext.getRealPath("/")
					def userDir = new File(message(code:'userimage.path'))
					userDir.mkdirs()
					String tmp = maxId.toString()
					userImageFile.transferTo( new File( userDir, tmp))
					def userImagePath = message(code:'userimage.path.save')+tmp
					userProfileToSave.setUserImage(userImagePath)
					if(userProfileToSave.save()){
						response.put("bartsyId",maxId)
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
		}else{
				response.put("errorCode","1")
				response.put("errorMessage","Please post your picture")
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
		def userProfile = UserProfile.findByBartsyId(json.bartsyId as long)
		def venue = Venue.findByVenueId(json.venueId)
		
		println "json.bartsyId  "+json.bartsyId 
		println "json.venueId   "+json.venueId
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
						def userCount = CheckedInUsers.findAllByVenueAndStatus(venue,1)
						response.put("userCount",userCount.size())
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
	/**
	 * This is the service for checking the user. User is already exist in db or not
	 */
	def syncUserProfile={
		// to get request object
		def json = JSON.parse(request)
		def userName=json.userName
		println "userName :: "+userName
		def response=[:]
		// checking user name is empty or not
		if(userName)
		{
			// get user name from DB
			def user = UserProfile.findByUserName(userName)
			// checking user is exist or not
			if(user){
				response.put("errorCode", 0)
				response.put("userExist", "YES")
			}else{
			response.put("errorCode", 0)
			response.put("userExist", "NO")
			}

		}else{
		// if username is null we are sending negative response
		response=handleNegativeResponse(response,"UserName should not be empty")
		}
		// sending response to client
		render(text:response as JSON ,  contentType:"application/json")
	}
	/**
	 * This service used to checking whether a user checkin into a venue or not
	 * 
	 * If user checkIn into a venue we are sending venue Id and venue Name
	 */
	def syncCheckInDetails = {
		// to get request object
		def json = JSON.parse(request)
		// getting bartsy id from request
		def bartsyId=json.bartsyId
		
		def response=[:]
		println "bartsyId ::: "+bartsyId
			// checking bartsy id
			if(bartsyId){
				def userProfile = UserProfile.findByBartsyId(bartsyId as long)
				if(userProfile){
					
					def checkedInUserVenue= CheckedInUsers.findByUserProfileAndStatus(userProfile,1)
					if(checkedInUserVenue){
						response.put("errorCode", 0)
						response.put("venueId", checkedInUserVenue.venue.venueId)
						response.put("venueName", checkedInUserVenue.venue.venueName)
					}else{
					// if user does not checkedIn in any venue in DB. We are sending negative response
					response=handleNegativeResponse(response,"User not checkedIn into any venue")
					}
				}else{
				// if userProfile does not exists in DB. We are sending negative response
				response=handleNegativeResponse(response,"userProfile does not exists")
				}
							
			}else{
			// if bartsy id is null we are sending negative response
			response=handleNegativeResponse(response,"Bartsy ID should not be empty")
			}
			

		
		// sending response to client
		render(text:response as JSON ,  contentType:"application/json")
	}
	
	def syncUserDetails = {
		def json = JSON.parse(request)
		def response = [:]
		def userProfile
		if(json.type.equals("login")){
			userProfile = UserProfile.findByUserName(json.userName)			
		}
		else{
			userProfile = UserProfile.findByBartsyId(json.bartsyId)
		}
		if(userProfile){
			if(json.type.equals("login")){
			userProfile.setDeviceToken(json.deviceToken)
			userProfile.setDeviceType(json.deviceType as int)
			userProfile.save()
			}
			response.put("errorCode", 0)
			response.put("bartsyId",userProfile.bartsyId)
			def checkedInUser = CheckedInUsers.findByUserProfileAndStatus(userProfile,1)
			if(checkedInUser){
				def openOrdersList = []
				response.put("venueId",checkedInUser.venue.venueId)
				response.put("venueName",checkedInUser.venue.venueName)
				def openOrdersCriteria = Orders.createCriteria()
				def openOrders = openOrdersCriteria.list {
					eq("venue",checkedInUser.venue)
					and{
						eq("user",userProfile)
					}
					and{
						'in'("orderStatus",["0", "2", "3"])
					}
				}
				if(openOrders){					
					openOrders.each{
						def order = it
						openOrdersList.addAll(order.orderId)
					}
				}
				if(openOrdersList.size()){
					response.put("openOrders",openOrdersList)
					response.put("orderCount",openOrdersList.size())
				}
				def checkedInUsers = CheckedInUsers.findAllByVenueAndStatus(checkedInUser.venue,1)
				if(checkedInUsers){
					response.put("userCount",checkedInUsers.size())
				}
			}
		}
		else{
			if(json.type.equals("login")){
				response=handleNegativeResponse(response,"User not registered")
			}
			else{
				response=handleNegativeResponse(response,"User not checked In")
			}
		}
		render(text:response as JSON ,  contentType:"application/json")
	}
	/*
	 * To login with bartsy credentials
	 * 
	 */
	
	def userBartsyLogin={
		// to get client request body
		def json = JSON.parse(request)
		def response=[:]
		def bartsyUserName = json.userName
		def bartsyPassword = json.password 
		// checking username null or not
		if(bartsyUserName){
			// checking password is null or not
			if(bartsyPassword){
				
			def userProfile = UserProfile.findByUserNameAndPassword(bartsyUserName, bartsyPassword)
				if(userProfile){
					response.put("bartsyId",userProfile.bartsyId)
					response.put("errorCode","0")
					response.put("errorMessage","User Profile Exists")
					def userCheckedIn = CheckedInUsers.findByUserProfileAndStatus(userProfile,1)
					if(userCheckedIn){
						response.put("userCheckedIn","0")
						response.put("venueId",userCheckedIn.venue.venueId)
						response.put("venueName",userCheckedIn.venue.venueName)
					}
					else{
						response.put("userCheckedIn","1")
					}
				}else{
				response.put("errorCode","1")
				response.put("errorMessage","Invalid username or password")
				
				}
				
			}else{
			
			handleNegativeResponse(response,"Password should not be empty")
			}
			
		}else{
		
		handleNegativeResponse(response,"UserName should not be empty")
		
		}
		
		render(text:response as JSON ,  contentType:"application/json")
		
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

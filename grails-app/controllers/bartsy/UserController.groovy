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
		def response = [:]
		try{
			println params.details
			def json = JSON.parse(params.details)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				UserProfile userProfileToSave = new UserProfile()
				def userImageFile = request.getFile("userImage")
				// checking user image is posted or not
				if(userImageFile){
					def save
					if(json.deviceToken.equals("") || json.deviceToken == null ){
						response.put("errorCode","1")
						response.put("errorMessage","GCM Registration ID is empty")
					}
					else if(!((json.has("bartsyPassword") && json.has("bartsyLogin")) ||(json.has("facebookUserName") && json.has("facebookId")) || (json.has("googleUserName") && json.has("googleId")) ) ){
						response.put("errorCode","1")
						response.put("errorMessage","Atleast one of the following credentials are needed to save- Facebook, Google or Bartsy")
						render(text:response as JSON ,  contentType:"application/json")
						return 
					}
					else{
						def userProfile 
				if(json.has("bartsyPassword") && json.has("bartsyLogin")){
					userProfile = UserProfile.findByBartsyLoginAndBartsyPassword(json.bartsyLogin,json.bartsyPassword)
				}
				else if(json.has("facebookUserName") && json.has("facebookId")){
					userProfile =  UserProfile.findByFacebookUserNameAndFacebookId(json.facebookUserName,json.facebookId.trim())
				}
				else if(json.has("googleUserName") && json.has("googleId")){
					userProfile =  UserProfile.findByGoogleUserNameAndGoogleId(json.googleUserName,json.googleId.trim())
				}
						if(userProfile) {
							userProfile.setName(json.name ?: "")
							userProfile.setFirstName(json.firstname ?: "")
							userProfile.setLastName(json.lastname ?: "")
							userProfile.setDateOfBirth(json.dateofbirth ?: "")
							userProfile.setNickName(json.nickname ?: "")
							userProfile.setDescription(json.description ?: "")
							userProfile.setOrientation(json.orientation ?: "")
							userProfile.setStatus(json.status ?: "")
							userProfile.setGoogleId(json.googleId ?json.googleId.trim(): "")
							userProfile.setFacebookId(json.facebookId?json.facebookId.trim() : "")
							userProfile.setFacebookUserName(json.facebookUserName ?: "")
							userProfile.setGoogleUserName(json.googleUserName ?: "")
							userProfile.setEmail(json.email ?: "")
//							userProfile.setLoginId(json.loginId.toString() ?: "")
//							userProfile.setLoginType(json.loginType ?: "")
							userProfile.setGender(json.gender ?: "")
							userProfile.setDeviceToken(json.deviceToken ?: "")
							userProfile.setDeviceType(json.deviceType as int)
//							userProfile.setEmailId(json.emailId ?: "")
//							userProfile.setPassword(json.password.toString() ?:"")
							userProfile.setCreditCardNumber(json.creditCardNumber.toString() ?: "")
							userProfile.setExpMonth(json.expMonth.toString() ?: "")
							userProfile.setExpYear(json.expYear.toString() ?: "")
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
								response.put("errorCode","1")
								response.put("errorMessage","Save not successful")
							}												
						}
						else{
//							userProfileToSave.setUserName(json.userName ?: "")
							userProfileToSave.setFirstName(json.firstname ?: "")
							userProfileToSave.setLastName(json.lastname ?: "")
							userProfileToSave.setDateOfBirth(json.dateofbirth ?: "")
							userProfileToSave.setNickName(json.nickname ?: "")
							userProfileToSave.setDescription(json.description ?: "")
							userProfileToSave.setOrientation(json.orientation ?: "")
							userProfileToSave.setStatus(json.status ?: "")
							userProfileToSave.setName(json.name ?: "")
//							userProfileToSave.setLoginId(json.loginId.toString() ?: "")
//							userProfileToSave.setLoginType(json.loginType ?: "")
							userProfileToSave.setGender(json.gender ?: "")
							userProfileToSave.setDeviceToken(json.deviceToken ?: "")
							userProfileToSave.setDeviceType(json.deviceType as int)
							userProfileToSave.setShowProfile(json.showProfile ?: "OFF")
							userProfileToSave.setShowProfileUpdated(new Date())
							userProfileToSave.setGoogleId(json.googleId ?json.googleId.trim(): "")
							userProfileToSave.setFacebookId(json.facebookId?json.facebookId.trim() : "")
							userProfileToSave.setFacebookUserName(json.facebookUserName ?: "")
							userProfileToSave.setGoogleUserName(json.googleUserName ?: "")
							userProfileToSave.setEmail(json.email ?: "")
							userProfileToSave.setBartsyPassword(json.bartsyPassword.toString() ?: "")
							userProfileToSave.setBartsyLogin(json.bartsyLogin ?: "")
//							userProfileToSave.setEmailId(json.emailId ?: "")
//							userProfileToSave.setPassword(json.password.toString()?:"")
							userProfileToSave.setCreditCardNumber(json.creditCardNumber.toString() ?: "")
							userProfileToSave.setExpMonth(json.expMonth.toString() ?: "")
							userProfileToSave.setExpYear(json.expYear.toString() ?: "")
							def maxId = UserProfile.createCriteria().get { projections { max "bartsyId" } } as Long
							if(maxId){
								maxId = maxId+1
							}
							else{
								maxId = 2342352565343
							}
							userProfileToSave.setBartsyId(maxId.toString())
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
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage","Please post your picture")
				}
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
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
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def userProfile = UserProfile.findByBartsyId(json.bartsyId)
				def venue = Venue.findByVenueId(json.venueId)
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
									pnMessage.put("messageType","userCheckOut")
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
								pnMessage.put("name",userProfile.getNickName())
								pnMessage.put("messageType","userCheckIn")
								pnMessage.put("showProfile",userProfile.showProfile)
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
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
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
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def userProfile = UserProfile.findByBartsyId(json.bartsyId)
				def venue = Venue.findByVenueId(json.venueId)
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
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
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
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def userProfile = UserProfile.findByBartsyId(json.bartsyId)
				def venue = Venue.findByVenueId(json.venueId)
				if(userProfile && venue){
					def checkedInUsers = CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,1)
					if(checkedInUsers){
						checkedInUsers.setLastHBResponse(new Date())
						checkedInUsers.save(flush:true)
					}
				}
				response.put("errorCode","0")
				response.put("errorMessage","Request Received")
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}

	def setShowProfile = {
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def userProfile = UserProfile.findByBartsyId(json.bartsyId)
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
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}

	def syncUserDetails = {
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def userProfile
				if(json.type.equals("facebook")){
					userProfile = UserProfile.findByFacebookId(json.facebookId)
				}
				else if(json.type.equals("google")){
					userProfile = UserProfile.findByGoogleId(json.googleId.toString())
				}
				else if(json.type.equals("checkin")){
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
							and{ eq("user",userProfile) }
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
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}
	/*
	 * To login with bartsy credentials
	 * 
	 */

	def bartsyUserLogin={
		def response = [:]
		// to get client request body
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def bartsyLogin = json.bartsyLogin
				def bartsyPassword = json.bartsyPassword
				// checking username null or not
				if(bartsyLogin){
					// checking password is null or not
					if(bartsyPassword){

						def userProfile = UserProfile.findByBartsyLoginAndBartsyPassword(bartsyLogin, bartsyPassword)
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

					handleNegativeResponse(response,"Email ID should not be empty")

				}
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
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

	def getUserProfile = {
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def type
				if(!((json.has("bartsyPassword") && json.has("bartsyLogin")) ||(json.has("facebookUserName") && json.has("facebookId")) || (json.has("googleUserName") && json.has("googleId")))  ){
					response.put("errorCode","1")
					response.put("errorMessage","Atleast one of the following credentials are needed to get the profile- Facebook, Google or Bartsy")
					render(text:response as JSON ,  contentType:"application/json")
					return
				}
				def userProfile 
				if(json.has("bartsyPassword") && json.has("bartsyLogin")){
					userProfile = UserProfile.findByBartsyLoginAndBartsyPassword(json.bartsyLogin,json.bartsyPassword)
					type = "bartsy"
				}
				else if(json.has("facebookUserName") && json.has("facebookId")){
					userProfile =  UserProfile.findByFacebookUserNameAndFacebookId(json.facebookUserName,json.facebookId)
					type = "facebook"
				}
				else if(json.has("googleUserName") && json.has("googleId")){
					userProfile =  UserProfile.findByGoogleUserNameAndGoogleId(json.googleUserName.toString(),json.googleId.toString())
					type = "google"
				}
				if(userProfile){
					response.put("errorCode",0)
					response.put("errorMessage","User Profile Exists")
					response.put("bartsyId",userProfile.getBartsyId())
					response.put("creditCardNumber",userProfile.getCreditCardNumber())
					response.put("dateofbirth",userProfile.getDateOfBirth())
					response.put("description",userProfile.getDescription())
					response.put("expMonth",userProfile.getExpMonth())
					response.put("expYear",userProfile.getExpYear())
					response.put("nickname",userProfile.getNickName())
					response.put("gender",userProfile.getGender())
					response.put("orientation",userProfile.getOrientation())
					response.put("showProfile",userProfile.getShowProfile())
					response.put("status",userProfile.getStatus())
					response.put("userImage",userProfile.getUserImage())
					if(type.equals("bartsy")){
						response.put("bartsyLogin",userProfile.getBartsyLogin())
						response.put("bartsyPassword",userProfile.getBartsyPassword())
					}
					else if(type.equals("facebook")){
						response.put("facebookUserName",userProfile.getFacebookUserName())
						response.put("facebookId",userProfile.getFacebookId())
					}
					else if(type.equals("google")){
						response.put("googleId",userProfile.getGoogleId())
						response.put("googleUserName",userProfile.getGoogleUserName())
					}
				}
				else{
					handleNegativeResponse(response,"User does not exists")
				}
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}
}

package bartsy

import grails.converters.JSON
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import org.codehaus.groovy.grails.web.json.JSONArray
import bartsy.CommonMethods


class UserController {

	def androidPNService
	def paymentService
	def grailsApplication

	/**
	 * This is the webservice to save the user registration details received from the customer application.
	 *
	 * @author Swetha Bhatnagar
	 *
	 **/
	def saveUserProfile = {
		//defining a map to return as a response for this syscall
		def response = [:]

		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(params.details)
			//varibale to check if email is updated
			def emailUpdated = false
			def sessionCode = System.currentTimeMillis().toString()
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def userImageFile = request.getFile("userImage")

				// checking user image is posted or not
				if(userImageFile){
					//check if deviceToken is present
					if(json.deviceToken.equals("") || json.deviceToken == null ){
						response.put("errorCode","1")
						response.put("errorMessage","GCM Registration ID is empty")
						render(text:response as JSON ,  contentType:"application/json")
						return
					}
					//check if atleast any one of the following credentials are mentioned in the syscall request - facebook, google or bartsy
					if(!((json.has("bartsyPassword") && json.has("bartsyLogin")) ||(json.has("facebookUserName") && json.has("facebookId")) || (json.has("googleUserName") && json.has("googleId")) ) ){
						response.put("errorCode","1")
						response.put("errorMessage","Atleast one of the following credentials are needed - Facebook, Google or Bartsy")
						render(text:response as JSON ,  contentType:"application/json")
						return
					}
					//if atleast one is given credential is given go to else part
					else{
						//user profile to be retrieved from DB based on given credentials
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
						//check if user profile present based on given credentials
						if(userProfile) {
							//if user profile present, update it with values sent in the syscall request
							if(json.name){
								userProfile.setName(json.name ?: "")
							}
							if(json.firstname){
								userProfile.setFirstName(json.firstname ?: "")
							}
							if(json.lastname){
								userProfile.setLastName(json.lastname ?: "")
							}
							if(json.dateofbirth){
								userProfile.setDateOfBirth(json.dateofbirth ?: "")
							}
							if(json.nickname){
								userProfile.setNickName(json.nickname ?: "")
							}
							if(json.description){
								userProfile.setDescription(json.description ?: "")
							}
							if(json.orientation){
								userProfile.setOrientation(json.orientation ?: "")
							}
							if(json.status){
								userProfile.setStatus(json.status ?: "")
							}
							if(json.googleId){
								userProfile.setGoogleId(json.googleId ?json.googleId.trim(): "")
							}
							if(json.facebookId){
								userProfile.setFacebookId(json.facebookId?json.facebookId.trim() : "")
							}
							if(json.facebookUserName){
								userProfile.setFacebookUserName(json.facebookUserName ?: "")
							}
							if(json.googleUserName){
								userProfile.setGoogleUserName(json.googleUserName ?: "")
							}
							if(json.bartsyPassword){
								userProfile.setBartsyPassword(json.bartsyPassword.toString() ?: "")
							}
							if(json.bartsyLogin){
								userProfile.setBartsyLogin(json.bartsyLogin ?: "")
							}
							if(json.zipCode){
								userProfile.setZipCode(json.zipCode.toString() ?: "")
							}
							if(json.homeCity){
								userProfile.setHomeCity(json.homeCity ?: "")
							}
							if(json.state){
								userProfile.setState(json.state ?: "")
							}
							if(json.ethnicity){
								userProfile.setEthnicity(json.ethnicity ?: "")
							}
							if(json.redactedCardNumber){
								userProfile.setRedactedCardNumber(json.redactedCardNumber)
							}
							if(json.has("email") && !json.email.equals(userProfile.email)){
								userProfile.setEmail(json.email ?: "")
								emailUpdated = true
							}
							if(json.gender){
								userProfile.setGender(json.gender)
							}
							if(json.deviceToken){
								userProfile.setDeviceToken(json.deviceToken ?: "")
							}
							if(json.deviceType){
								userProfile.setDeviceType(json.deviceType as int)
							}

							if(json.creditCardNumber && json.creditCardNumber.toString().length()>0)
							{
								userProfile.setCreditCardNumber(json.creditCardNumber.toString())
							}
							if(json.encryptedCreditCard){
								userProfile.setEncryptedCreditCard(json.encryptedCreditCard.toString() ?: "")
							}
							if(json.expMonth){
								userProfile.setExpMonth(json.expMonth.toString())
							}
							if(json.expYear){
								userProfile.setExpYear(json.expYear.toString())
							}
							userProfile.setSessionCode(sessionCode)
							//code to read the image file sent in the request to the syscall and save it locally
							def webRootDir = servletContext.getRealPath("/")
							def userDir = new File(grailsApplication.config.userimage.path)
							userDir.mkdirs()
							String tmp = userProfile.bartsyId.toString()
							userImageFile.transferTo( new File( userDir, tmp))
							def userImagePath = grailsApplication.config.userimage.savePath+tmp
							//set the location of the image to the user profile
							userProfile.setUserImage(userImagePath)
							//save the user profile
							if(userProfile.save()){
								if(emailUpdated){
									//send Email for email address verification
									sendVerificationMailToUser(userProfile.getEmail(),userProfile.getBartsyId())
								}
								//if save successful return the bartsy ID along with errorCode 1 and the message given below
								response.put("bartsyId",userProfile.bartsyId)
								response.put("errorCode","0")
								response.put("errorMessage","User Profile updated")
								//check if the user is checked into any venue as per the server
								def userCheckedIn = CheckedInUsers.findByUserProfileAndStatus(userProfile,1)
								if(userCheckedIn){
									//if checked in also return the flag userCheckedIn as 0, the venueId and the venueName of the venue where the user is checked in as per the server
									response.put("userCheckedIn","0")
									response.put("venueId",userCheckedIn.venue.venueId)
									response.put("venueName",userCheckedIn.venue.venueName)
									response.put("venueImagePath",userCheckedIn.venue.venueImagePath)
								}
								else{
									//if not checked in send the flag userCheckedIn as 1
									response.put("userCheckedIn","1")
								}
							}
							else{
								//if user profile save was not successful send the errorCode as 1 along with the message given below
								response.put("errorCode","1")
								response.put("errorMessage","Save not successful")
							}
						}
						else{
							//if user profile does not exist with given credentials create a new user profile object
							UserProfile userProfileToSave = new UserProfile()
							userProfileToSave.setFirstName(json.firstname ?: "")
							userProfileToSave.setLastName(json.lastname ?: "")
							userProfileToSave.setDateOfBirth(json.dateofbirth ?: "")
							userProfileToSave.setNickName(json.nickname ?: "")
							userProfileToSave.setDescription(json.description ?: "")
							userProfileToSave.setOrientation(json.orientation ?: "")
							userProfileToSave.setStatus(json.status ?: "")
							userProfileToSave.setName(json.name ?: "")
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
							userProfileToSave.setCreditCardNumber(json.creditCardNumber.toString() ?: "")
							userProfileToSave.setEncryptedCreditCard(json.encryptedCreditCard.toString() ?: "")
							userProfileToSave.setExpMonth(json.expMonth.toString() ?: "")
							userProfileToSave.setExpYear(json.expYear.toString() ?: "")
							userProfileToSave.setSessionCode(sessionCode)
							userProfileToSave.setEmailVerified("false")

							userProfileToSave.setZipCode(json.zipCode.toString() ?: "")
							userProfileToSave.setHomeCity(json.homeCity ?: "")
							userProfileToSave.setState(json.state ?: "")
							userProfileToSave.setEthnicity(json.ethnicity ?: "")

							userProfileToSave.setRedactedCardNumber(json.has("redactedCardNumber")?json.redactedCardNumber:"")
							userProfileToSave.setAdminUser(0)
							//							def admin = AdminUser.findByPromoterCode(json.has("promoCode")?json.promoCode:"")
							//							if(admin){
							//								userProfileToSave.setAdminUser(admin.id)
							//							}
							//							else{
							//								userProfileToSave.setAdminUser(0)
							//							}
							//retrieve the max bartsyId from DB and increment it by 1
							def maxId = UserProfile.createCriteria().get { projections { max "bartsyId" } } as Long
							if(maxId){
								maxId = maxId+1
							}
							else{
								maxId = 2342352565343
							}
							//set the maxId value as bartsyId to user profile object
							userProfileToSave.setBartsyId(maxId.toString())
							//code to read the image file sent in the request to the syscall and save it locally
							def webRootDir = servletContext.getRealPath("/")
							def userDir = new File(grailsApplication.config.userimage.path)
							userDir.mkdirs()
							String tmp = maxId.toString()
							userImageFile.transferTo( new File( userDir, tmp))
							def userImagePath = grailsApplication.config.userimage.savePath+tmp
							//set the location of the image to the user profile
							userProfileToSave.setUserImage(userImagePath)
							println "userProfileToSave "
							//save the user profile object
							if(userProfileToSave.save()){
								def paymentCheck
								if(json.has("creditCardNumber")&&json.has("expMonth")&&json.has("expYear")){

									paymentCheck = paymentService.authorizePaymentInSaveUserProfile(userProfileToSave,"0.01",userProfileToSave.bartsyId)
								}
								response.put("bartsyId",maxId)
								response.put("userCheckedIn","1")
								if(paymentCheck?.authApproved)
								{
									//if save successful send the bartsyId along with errorCode 0 and given errorMessage. Also send the userCheckedIn flag as 1 as new user would not have been checked in earlier
									response.put("errorCode","0")
									response.put("errorMessage","Save Successful")

								}else{
									println "else "
									response.put("errorCode","10")
									response.put("errorMessage","Your credit card number or name are invalid. Please enter them again.")
								}
								//send Email for email address verification
								sendVerificationMailToUser(userProfileToSave.getEmail(),userProfileToSave.getBartsyId())
							}
							else{
								//if user profile save was not successful send the errorCode as 1 along with the message given below
								response.put("errorCode","1")
								response.put("errorMessage","Save not successful")
							}
						}
					}
				}
				else{
					//if image not available send the errorCode as 1 along with the message given below
					response.put("errorCode","1")
					response.put("errorMessage","Please post your picture")
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("oauthCode", sessionCode)
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> In saveUserProfile "+e.getMessage())
			println"excep "+e.getMessage()
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}

		render(text:response as JSON ,  contentType:"application/json")
	}

	def randomTest = {
		println" randomTest "
		CommonMethods common = new CommonMethods()
		String sessionCode = common.promoCode(6)
		println "sessionCode "+sessionCode
	}

	/**
	 * This is the webservice to check in a user into a venue
	 *
	 * @author Swetha Bhatnagar
	 *
	 **/
	def userCheckIn = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				//retrieve the user profile and venue objects based on the bartsyId sent in the syscall request
				def userProfile = UserProfile.findByBartsyId(json.bartsyId)
				def venue = Venue.findByVenueId(json.venueId)
				//check if user profile and venue both exists
				if(userProfile && venue){
					//if user profile and venue both exists check if venue is CLOSED
					if(venue.status.equals("CLOSED")){
						//if venue is CLOSED send errorCode 1 along with the given message
						response.put("errorCode","1")
						response.put("errorMessage","Venue is Closed")
					}
					else{
						//if venue is not CLOSED define a variable to get the checked in object for that user profile
						def userCheckedIn
						//get the entry for the user profile in that venue with status 1(i.e. user is checked in)
						userCheckedIn = CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,1)
						if(userCheckedIn){
							//if user is already checked in send the givem message along with error code 1
							response.put("errorCode","0")
							response.put("errorMessage","User already Checked In the selected venue")
						}
						else{
							//if user is not checked in the given venue check if user checked into any other venue
							userCheckedIn = CheckedInUsers.findByUserProfileAndStatus(userProfile,1)
							if(userCheckedIn){
								//if user checked into other venue..set checked in status to 0(i.e. check the user out of that venue)
								userCheckedIn.setStatus(0)
								//save the entry with updated status
								if(userCheckedIn.save(flush:true)){
									//if save successful cancel any open orders
									def cancelledOrders = cancelOpenOrders(userCheckedIn.userProfile)
									//send the venue a PN stating that the user was checked out of that venue
									Map pnMessage = new HashMap()
									pnMessage.put("cancelledOrders",cancelledOrders)
									pnMessage.put("bartsyId",userProfile.bartsyId)
									pnMessage.put("messageType","userCheckOut")
									pnMessage.put("currentTime",new Date().toGMTString())
									androidPNService.sendPN(pnMessage,userCheckedIn.venue.deviceToken)
									//save the user checkout notfication
									def notification = new Notifications()
									notification.setUser(userProfile)
									notification.setVenue(userCheckedIn.venue)
									notification.setType("checkout")
									notification.setMessage("You checked out from the venue : "+userCheckedIn.venue.venueName)
									notification.save(flush:true)
								}
							}
							//check if an entry is there for that user profile for the venue sent in the syscall request
							def checkedInUsers = CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,0)
							def userCheckedInDeatils
							if(!checkedInUsers){
								//if entry not present create a new object
								checkedInUsers = new CheckedInUsers()
							}
							// to generate random number for user sessionCode
							String sessionCode = generateSessionCode()

							userCheckedInDeatils=new UserCheckInDetails()
							//set the values to the object
							checkedInUsers.setUserProfile(userProfile)
							checkedInUsers.setVenue(venue)
							checkedInUsers.setStatus(1)
							checkedInUsers.setUserSessionCode(sessionCode)
							checkedInUsers.setLastHBResponse(new Date())
							// set user checked in details
							userCheckedInDeatils.setUserProfile(userProfile)
							userCheckedInDeatils.setVenue(venue)
							userCheckedInDeatils.setCheckedInDate(new Date())


							//save the object
							if(checkedInUsers.save(flush:true)){
								//if save successful send error code 0 and given message
								response.put("errorCode","0")
								response.put("errorMessage","User Checked In Successfully")
								//get the checked in users list for that venue
								def userCount = CheckedInUsers.findAllByVenueAndStatus(venue,1)
								//also send thhe number of checked in users for that venue in the response
								response.put("userCount",userCount.size())
								//send a PN to the venue that the user has checked into that venue
								Map pnMessage = new HashMap()
								pnMessage.put("bartsyId",userProfile.getBartsyId())
								pnMessage.put("gender",userProfile.getGender())
								pnMessage.put("name",userProfile.getNickName())
								pnMessage.put("messageType","userCheckIn")
								pnMessage.put("showProfile",userProfile.showProfile)
								pnMessage.put("userImagePath",userProfile.getUserImage())
								pnMessage.put("currentTime",new Date().toGMTString())
								androidPNService.sendPN(pnMessage,venue.deviceToken)
								//save the user checkin notification
								def notification = new Notifications()
								notification.setUser(userProfile)
								notification.setVenue(checkedInUsers.venue)
								notification.setType("checkin")
								notification.setMessage("You checked into the venue : "+checkedInUsers.venue.venueName)
								notification.save(flush:true)
								userCheckedInDeatils.save(flush:true)
							}
							else{
								//if save not successful send error code 1 along with given message
								response.put("errorCode","1")
								response.put("errorMessage","User check in failed")
							}
						}
					}
				}
				else{
					//if either user profile or venue does not exist send the given error message along with error code 1
					response.put("errorCode","1")
					response.put("errorMessage","User ID or Venue ID does not exist")
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> userCheckIn "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}

	def generateSessionCode(){
		CommonMethods common = new CommonMethods()
		String sessionCode = common.randomNumString(3)

		def user = CheckedInUsers.findAllByUserSessionCode(sessionCode)
		if(user.size()>0){
			generateSessionCode()
		}
		else{
			return sessionCode
		}
	}

	/**
	 * This is the webservice to check out a user from a venue
	 *
	 * @author Swetha Bhatnagar
	 *
	 **/
	def userCheckOut = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				//retrieve the user profile and venue objects based on the bartsyId sent in the syscall request
				def userProfile = UserProfile.findByBartsyId(json.bartsyId)
				def venue = Venue.findByVenueId(json.venueId)
				//check if user profile and venue both exists
				if(userProfile && venue){
					//if user profile and venue exists check if user already checked out from requesting venue
					if(CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,0)){
						//if already checked out send errorcode 0 alogn with given message
						response.put("errorCode","0")
						response.put("errorMessage","User already Checked out from the selected venue")
					}
					else{
						//check if an entry is there for that user profile for the venue sent in the syscall request
						def checkedInUsers = CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,1)
						//def userCheckedInDeatils
						if(!checkedInUsers){
							//if entry not present create a new object
							checkedInUsers = new CheckedInUsers()
						}
						//userCheckedInDeatils=new UserCheckInDetails()
						//set the values to the object
						checkedInUsers.setUserProfile(userProfile)
						checkedInUsers.setVenue(venue)
						checkedInUsers.setStatus(0)
						// set user checked in details
						//						userCheckedInDeatils.setUserProfile(userProfile)
						//						userCheckedInDeatils.setVenue(venue)
						//						userCheckedInDeatils.setCheckedInDate(new Date())
						checkedInUsers.setUserSessionCode(null)
						//save the object
						if(checkedInUsers.save(flush:true)){
							//if save successful send error code 0 and error message given below
							response.put("errorCode","0")
							response.put("errorMessage","User Checked Out Successfully")
							//cancel any open orders for that user in that venue
							def cancelledOrders = cancelOpenOrders(checkedInUsers.userProfile)
							Map pnMessage = new HashMap()
							//send a PN to the venue that the user has checked out of that venue
							println"cancelledOrders "+cancelledOrders.size()
							pnMessage.put("cancelledOrders",cancelledOrders)
							pnMessage.put("bartsyId",userProfile.bartsyId)
							pnMessage.put("messageType","userCheckOut")
							pnMessage.put("currentTime",new Date().toGMTString())
							androidPNService.sendPN(pnMessage,venue.deviceToken)
							//save the user checkout notification
							def notification = new Notifications()
							notification.setUser(userProfile)
							notification.setVenue(checkedInUsers.venue)
							notification.setType("checkout")
							notification.setMessage("User checked out from the venue : "+checkedInUsers.venue.venueName)
							notification.save(flush:true)
							//userCheckedInDeatils.save(flush:true)
						}
						else{
							//if save not successful send error code 1 along with given message
							response.put("errorCode","1")
							response.put("errorMessage","User check out failed")
						}
					}
				}
				else{
					//if either user profile or venue does not exist send the given error message along with error code 1
					response.put("errorCode","0")
					response.put("errorMessage","User ID or Venue ID does not exist")
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> userCheckOut "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}

	/**
	 * This is the method to be called to cancel open orders of a user
	 *
	 * @author Swetha Bhatnagar
	 *
	 **/
	def List cancelOpenOrders(UserProfile userProfile){
		//define a list to add cancelled orders
		def cancelledOrders = []
		//retrieve the list of open orders for that user from the DB
		def openOrdersCriteria = Orders.createCriteria()
		def openOrders = openOrdersCriteria.list {
			or{
				eq("user",userProfile)
				eq("receiverProfile",userProfile)
			}
			and{
				'in'("orderStatus",[
					"0",
					"1",
					"2",
					"3",
					"4",
					"5",
					"6",
					"7",
					"8",
					"9"
				])
				//'in'("orderStatus",["0","2", "3","9"])
			}
		}
		//check if any open orders are present
		if(openOrders){
			//if open orders are present loop through the list
			openOrders.each{
				def order=it

				if(order.drinkOffered){
					println "drink offered"
					def rBartsyId = order.receiverProfile.bartsyId.toString().trim()
					def sBartsyId = userProfile.bartsyId.toString().trim()
					if(rBartsyId.equalsIgnoreCase(sBartsyId)){
						//if order was not accepted/rejected then do not charge the user...else charge
						if(order.orderStatus.equals("2")||order.orderStatus.equals("3")){
							order = paymentService.makePayment(order)
							if(order.getCaptureApproved().toBoolean()){
								//Calculating reward points
								CommonMethods common = new CommonMethods()
								common.calculateRewardPoints(order)
							}
						}
						//set the status to 10 i.e. pastOrders
						order.setOrderStatus("10")
						//save the order
						if(order.save(flush:true)){
							//if save succesful add the order id to the cancelled orders list defined earlier
							cancelledOrders.add(order.orderId)
							println "order.orderId "+order.orderId
							println"order.satus "+order.orderStatus
						}else{

						}
					}

				}else{
					//if order was not accepted/rejected then do not charge the user...else charge
					if(order.orderStatus.equals("2")||order.orderStatus.equals("3")){
						order = paymentService.makePayment(order)
						if(order.getCaptureApproved().toBoolean()){
							//Calculating reward points
							CommonMethods common = new CommonMethods()
							common.calculateRewardPoints(order)
						}
					}
					//set the status to 10 i.e. pastOrders
					order.setOrderStatus("10")
					//save the order
					if(order.save(flush:true)){
						//if save succesful add the order id to the cancelled orders list defined earlier
						cancelledOrders.add(order.orderId)
					}else{

					}
				}
			}
		}
		//return the cancelled orders list
		return cancelledOrders
	}

	/**
	 * This is the webservice to be called by the customer app when heartbeat PN is received
	 *
	 * @author Swetha Bhatnagar
	 *
	 **/
	def heartBeat = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(json.apiVersion && apiVersion.value.toString().equalsIgnoreCase(json.apiVersion.toString())){
				if(json.bartsyId && json.venueId){
					//retrieve the user profile and venue objects based on the bartsyId sent in the syscall request
					def userProfile = UserProfile.findByBartsyId(json.bartsyId)
					def venue = Venue.findByVenueId(json.venueId)
					//check if user profile and venue both exists
					if(userProfile && venue){
						//check whether venue is online or not
						if(!venue.status.toString().equalsIgnoreCase("online")){
							//if user profile and venue both exists check if user is checked into that venue
							def checkedInUsers = CheckedInUsers.findByUserProfileAndVenueAndStatus(userProfile,venue,1)
							if(checkedInUsers){
								//if checked in into that venue update the lastHBResponse column for that user with current date time
								checkedInUsers.setLastHBResponse(new Date())
								//save the object
								checkedInUsers.save(flush:true)
								def checkedInUsersList = []
								def userList = CheckedInUsers.findAllByVenueAndStatus(venue,1)
								if(userList){
									userList.each{
										def user = it
										checkedInUsersList.add(user.userProfile.bartsyId)
									}
									def openOrdersCriteria = Orders.createCriteria()
									def openOrders = openOrdersCriteria.list {
										or{
											eq("user",userProfile)
											eq("receiverProfile",userProfile)
										}
										and{ eq("venue",venue) }
										and{
											'in'("orderStatus",[
												"0",
												"1",
												"2",
												"3",
												"4",
												"5",
												"6",
												"7",
												"8",
												"9"
											])
										}
									}
									def ordersList = []
									if(openOrders){
										openOrders.each{
											def order=it
											ordersList.add(order.orderId)
										}
									}
									response.put("bartsyId",userProfile.bartsyId)
									response.put("venueId",venue.venueId)
									response.put("venueName",venue.venueName)
									response.put("venueImagePath",venue.venueImagePath)
									response.put("messageType","heartBeat")
									response.put("userCount",checkedInUsersList.size())
									response.put("openOrders",ordersList)
									response.put("orderCount",ordersList.size())
									response.put("checkedInUsersList",checkedInUsersList)
									response.put("currentTime",new Date().toGMTString())

									def unReadNotifications = Notifications.findAllByUserAndStatus(userProfile,0)
									if(unReadNotifications){
										response.put("unReadNotifications",unReadNotifications.size())
									}else{
										response.put("unReadNotifications",0)
									}

								}
							}
							//send the error code 0 acknowleding the request received
							response.put("errorCode","0")
							response.put("errorMessage","Request Received")
						}else{
							response.put("errorCode","1")
							response.put("errorMessage","Venue is closed")
						}
					}else{
						response.put("errorCode","1")
						response.put("errorMessage","Venue or User does not exists")
					}
				}else{
					//if apiVersion do not match send errorCode 100
					response.put("errorCode","1")
					response.put("errorMessage","BartsyId or venueId is missing in your request")
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> heartBeat "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}

	/**
	 * This is the webservice to be called to update the show profile configuration of a user
	 *
	 * @author Swetha Bhatnagar
	 *
	 **/
	def setShowProfile = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				//retrieve the user profile based on bartsyId sent in the syscall request
				def userProfile = UserProfile.findByBartsyId(json.bartsyId)
				//check if user profile exists with that bartsy Id
				if(userProfile){
					//if user profile exists check if showProfileUpdated column falls within 24 hours from current date time
					use(groovy.time.TimeCategory){
						def diff = new Date() - user.showProfileUpdated
						//log.warn("difference in minutes"+diff.minutes)
						if(diff.hours >= 24){
							//if does not fall within 24 hours then set the value of show profile and show profile updated
							userProfile.setShowProfile(json.showProfile)
							userProfile.setShowProfileUpdated(new Date())
							//save the user profile object
							if(userProfile.save(flush:true))
							{
								//if save successful send error code 0 with given error message
								response.put("errorCode","0")
								response.put("errorMessage","Show profile updated")
							}
							else{
								//if save not successful send error code 1 with given error message
								response.put("errorCode","1")
								response.put("errorMessage","Show profile not updated")
							}
						}
						else{
							//if falls within 24 hours send error code 1 with the given error message
							response.put("errorCode","1")
							response.put("errorMessage","Cannot Update this setting for 24 hrs from last update")
						}
					}
				}
				else{
					//if user profile not exists send error code 1 with given error message
					response.put("errorCode","1")
					response.put("errorMessage","User Id does not exists")
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===>setShowProfile  "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}

	/**
	 * This is the webservice to be called to sync user details
	 *
	 * @author Swetha Bhatnagar
	 *
	 **/
	def syncUserDetails = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			def sessionCode = System.currentTimeMillis().toString()
			if(apiVersion.value.toString().equalsIgnoreCase(json.apiVersion.toString())){
				//define the variable to retrieve and store the user profile object
				def userProfile
				if(json.type.equals("facebook")){
					userProfile = UserProfile.findByFacebookId(json.facebookId)
				}
				else if(json.type.equals("google")){
					userProfile = UserProfile.findByGoogleId(json.googleId.toString())
				}
				else if(json.type.equals("checkin") || json.type.equals("login")){
					userProfile = UserProfile.findByBartsyId(json.bartsyId)
				}
				//check if user profile exists
				if(userProfile){
					println"user exists"
					//if user profile exists set device type and device token sent in the syscall request
					userProfile.setDeviceToken(json.deviceToken)
					if(json.deviceType){
						userProfile.setDeviceType(json.deviceType as int)
					}
					userProfile.setSessionCode(sessionCode)
					//save the user profile object
					userProfile.save()
					//return error code 0 and bartsyId
					response.put("errorCode", 0)
					response.put("bartsyId",userProfile.bartsyId)
					response.put("oauthCode", userProfile.sessionCode)
					//check if user checked into any venue as per server
					def checkedInUser = CheckedInUsers.findByUserProfileAndStatus(userProfile,1)
					if(checkedInUser){
						//if checked into a venue send venueId and venuename of that venue
						response.put("venueId",checkedInUser.venue.venueId)
						response.put("venueName",checkedInUser.venue.venueName)
						response.put("venueImagePath",checkedInUser.venue.venueImagePath)

						response.put("latitude",checkedInUser.venue.getLat())
						response.put("longitude",checkedInUser.venue.getLongtd())
						response.put("venueStatus",checkedInUser.venue.getStatus())
						response.put("wifiPresent",checkedInUser.venue.getWifiPresent().toString())
						response.put("wifiName",checkedInUser.venue.getWifiName())
						response.put("wifiPassword",checkedInUser.venue.getWifiPassword())
						def address = checkedInUser.venue.getStreetAddress()+","+checkedInUser.venue.getLocality()+","+checkedInUser.venue.getCountry()+","+checkedInUser.venue.getPostalCode()
						response.put("address",address)
						response.put("totalTaxRate",checkedInUser.venue.totalTaxRate)
						response.put("orderTimeout",checkedInUser.venue.getCancelOrderTime())
						response.put("typeOfAuthentication",checkedInUser.venue.getTypeOfAuthentication())


						response.put("tableOrdering",checkedInUser.venue.tableOrdering ?: Boolean.FALSE)
						response.put("isPickupLocation",checkedInUser.venue.isPickupLocution ?: Boolean.FALSE)

						if(checkedInUser.venue.deliveryTables){
							response.put("deliveryTables",checkedInUser.venue.deliveryTables ?: "")
						}
						if(checkedInUser.venue.pickupLocations){
							response.put("pickupLocations",checkedInUser.venue.pickupLocations ?: "")
						}


						def orders = Orders.findAllByUser(userProfile)
						if(orders){
							response.put("unlocked","true")
						}
						else{
							response.put("unlocked","false")
						}
						//Find the list of checkedIn users for that venue
						def checkedInUsers = CheckedInUsers.findAllByVenueAndStatus(checkedInUser.venue,1)

						//varibale to count number of private checked in users
						def privateUsers = 0
						if(checkedInUsers){
							//loop through the checkedin users list and check for private users
							checkedInUsers.each{
								def checkedIn = it
								if(checkedIn.userProfile.getShowProfile().equals("OFF")){
									privateUsers = privateUsers+1
								}
							}
						}
						response.put("checkedInUsers",checkedInUsers?.size())
						response.put("privateUsers",privateUsers)
						//get the checked in users list in that venue and send the count
						if(checkedInUsers){
							response.put("userCount",checkedInUsers.size())
						}

						//retrieve any open orders for that user in that venue
						def openOrdersCriteria = Orders.createCriteria()
						def openOrders = openOrdersCriteria.list {
							eq("venue",checkedInUser.venue)
							and{ eq("user",userProfile) }
							and{
								'in'("orderStatus",[
									"0",
									"1",
									"2",
									"3",
									"4",
									"5",
									"6",
									"7",
									"8",
									"9"
								])
							}
						}
						//retrieve the order Ids and add it to list
						def openOrdersList = []
						if(openOrders){
							openOrders.each{
								def order = it
								openOrdersList.add(order.orderId)
							}
						}
						// check if open orders list not empty
						if(openOrdersList.size()){
							//if not empty send the list of open orders and count of open orders
							response.put("openOrders",openOrdersList)
							response.put("orderCount",openOrdersList.size())
							response.put("orderTimeout",checkedInUser.venue.cancelOrderTime)
						}
					}
				}
				else{
					println"user not exists"
					//if user profile does not exists send negative response
					if(json.type.equals("facebook") || json.type.equals("google") || json.type.equals("login")){
						response=handleNegativeResponse(response,"User not registered")
					}
					else{
						response=handleNegativeResponse(response,"User not checked In")
					}
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> syncUserDetails "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}

	/**
	 * This is the webservice to be called to login using bartsyLogin and bartsyPassword
	 *
	 * @author Swetha Bhatnagar
	 *
	 **/
	def bartsyUserLogin={
		//defining a map to return as a response for this syscall
		def response = [:]
		// to get client request body
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			def sessionCode = System.currentTimeMillis().toString()
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def bartsyLogin = json.bartsyLogin
				def bartsyPassword = json.bartsyPassword
				// checking bartsyLogin null or not
				if(bartsyLogin){
					// checking bartsyPassword is null or not
					if(bartsyPassword){
						//if bartsyLogin and bartsyLogin not null retrieve the user profile based on those details
						def userProfile = UserProfile.findByBartsyLoginAndBartsyPassword(bartsyLogin, bartsyPassword)
						if(userProfile){
							userProfile.setSessionCode(sessionCode)
							userProfile.save(flush:true)
							//if user profile exists return the bartsyId of the user along with error code 0
							response.put("bartsyId",userProfile.bartsyId)
							response.put("oauthCode", userProfile.sessionCode)
							response.put("errorCode","0")
							response.put("errorMessage","User Profile Exists")
							//check if user checked into any venue as per server
							def userCheckedIn = CheckedInUsers.findByUserProfileAndStatus(userProfile,1)
							if(userCheckedIn){
								//if checked into a venue send venueId and venuename of that venue along with userCheckedIn flag as 0
								response.put("userCheckedIn","0")
								response.put("venueId",userCheckedIn.venue.venueId)
								response.put("venueName",userCheckedIn.venue.venueName)
							}
							else{
								//if not checked in send the userCheckedIn flag as 1
								response.put("userCheckedIn","1")
							}
						}else{
							//if user profile does not exists send error code 1 along with the following message
							response.put("errorCode","1")
							response.put("errorMessage","Invalid username or password")
						}
					}
					else{
						//if bartsyPassword empty return error code 1
						handleNegativeResponse(response,"Password should not be empty")
					}
				}
				else{
					//if bartsyLogin empty return error code 1
					handleNegativeResponse(response,"Login should not be empty")
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> bartsyUserLogin "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")

	}


	/**
	 * To return negative response
	 */
	def handleNegativeResponse(response,message){
		response.put("errorCode", 1)
		response.put("errorMessage", message)
		return response
	}
	/**
	 * To get user public details
	 */
	def getUserPublicDetails={
		try{
			println"getUserPublicDetails "
			def json = JSON.parse(request)
			def response=[:]
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			def apiVersionNumber=json.apiVersion

			if(json){
				if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
					if(json.has("bartsyId")){
						def bartsyId = json.bartsyId
						def userProfile = UserProfile.findByBartsyId(bartsyId)
						if(userProfile){
							CommonMethods commonMethods = new CommonMethods()
							def age
							if(userProfile.getDateOfBirth())
								age= commonMethods.getAge(userProfile.getDateOfBirth())

							response.put("errorCode", 0)
							response.put("bartsyId", bartsyId)
							response.put("gender", userProfile.getGender())
							if(age){
								response.put("age", age)
							}
							response.put("orientation", userProfile.getOrientation())
							response.put("showProfile", userProfile.getShowProfile())
							response.put("userImagePath", userProfile.getUserImage())
							response.put("status",userProfile.status)
							response.put("description",userProfile.description)
							println"response "+response
						}else{
							handleNegativeResponse(response,"Userprofile does not exists")
						}
					}
					else{
						handleNegativeResponse(response,"BartsyId should not be empty or null")
					}
				}
				else{
					//if apiVersion do not match send errorCode 100
					response.put("errorCode","100")
					response.put("errorMessage","API version do not match")
				}
			}else{
				handleNegativeResponse(response,"Your post data is empty")
			}
			response.put("currentTime",new Date().toGMTString())
			render(text:response as JSON ,  contentType:"application/json")
		}catch (Exception e) {
			log.info("Exception Found !!!! getUserPublicDetails "+e.getMessage())
			render(text:response as JSON ,  contentType:"application/json")
		}

	}



	/**
	 * This is the webservice to be called to get the profile of a user
	 *
	 * @author Swetha Bhatnagar
	 *
	 **/
	def getUserProfile = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				//check if atleast any one of the following credentials are mentioned in the syscall request - facebook, google or bartsy
				if(!((json.has("bartsyPassword") && json.has("bartsyLogin")) ||(json.has("facebookUserName") && json.has("facebookId")) || (json.has("googleUserName") && json.has("googleId")))  ){
					response.put("errorCode","1")
					response.put("errorMessage","Atleast one of the following credentials are needed to get the profile- Facebook, Google or Bartsy")
					render(text:response as JSON ,  contentType:"application/json")
					return
				}
				//if atleast one is given credential is given go to else part
				else{
					//user profile to be retrieved from DB based on given credentials
					def userProfile
					if(json.has("bartsyPassword") && json.has("bartsyLogin")){
						userProfile = UserProfile.findByBartsyLoginAndBartsyPassword(json.bartsyLogin,json.bartsyPassword)
					}
					else if(json.has("facebookUserName") && json.has("facebookId")){
						userProfile =  UserProfile.findByFacebookUserNameAndFacebookId(json.facebookUserName,json.facebookId)
					}
					else if(json.has("googleUserName") && json.has("googleId")){
						userProfile =  UserProfile.findByGoogleUserNameAndGoogleId(json.googleUserName.toString(),json.googleId.toString())
					}
					if(userProfile){
						//if user profile exists return error code 0 along with the following details of the user
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
						response.put("firstname",userProfile.getFirstName())
						response.put("lastname",userProfile.getLastName())
						response.put("email",userProfile.getEmail())
						response.put("bartsyLogin",userProfile.getBartsyLogin())
						response.put("bartsyPassword",userProfile.getBartsyPassword())
						response.put("facebookUserName",userProfile.getFacebookUserName())
						response.put("facebookId",userProfile.getFacebookId())
						response.put("googleId",userProfile.getGoogleId())
						response.put("googleUserName",userProfile.getGoogleUserName())
						response.put("oauthCode", userProfile.getSessionCode())
						response.put("redactedCardNumber",userProfile.getRedactedCardNumber())
						response.put("zipCode",userProfile.getZipCode())
						response.put("homeCity",userProfile.getHomeCity())
						response.put("state",userProfile.getState())
						response.put("ethnicity",userProfile.getEthnicity())
					}
					else{
						//if user profile does not exists send error code 1
						handleNegativeResponse(response,"User does not exists")
					}
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===>getUserProfile "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}

		render(text:response as JSON ,  contentType:"application/json")
	}

	/**
	 * This method used to send bartsy verification mail to user email
	 */
	def sendVerificationMailToUser(String emailId,String bartsyId){
		try{
			def userId = bartsyId.bytes.encodeBase64().toString()
			String url =  request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/"+grailsApplication.getMetadata().getApplicationName()
			println"email id "+emailId.trim()
			sendMail {
				from "Bartsy <accounts-do-not-reply@vendsy.com>"
				to emailId.trim()
				subject "Bartsy email verification"
				html g.render(template:'/user/mailTemplate', model:[url:url,userId:userId])
			}

		}catch(Exception e){
			log.info("Exception found In sendVerificationMailToUser !!!!! "+e.getMessage())
		}
	}

	def verifyEmailId={
		try{
			def decoded = new String(params.id.decodeBase64())
			println"decoded String "+decoded

			def userProfile = UserProfile.findByBartsyId(decoded)
			if(userProfile.emailVerified.toString().equalsIgnoreCase("false")){
				userProfile.emailVerified="true"
				if(userProfile.save()){
					flash.message="Your Bartsy Account is Verified"
				}else{
					flash.message="Please try again later"
				}
			}else{
				flash.message="Your Bartsy Account was Already Verified"
			}
		}catch(Exception e){
			log.info("Exception found In verifyEmailId !!!!! "+e.getMessage())
		}
	}

	def createServerKeys(){
		String privateKeyFile = "bartsy_privateKey.pem"
		String publicKeyFile = "bartsy_publicKey.pem"
		String csrFile = "bartsy_office.csr"
		String certFile = "bartsy_office.crt"
		String caCert = "bartsy_ca.crt"
		String caPrivKey = "bartsy_CAkey.pem"
		String cryptoPath = servletContext.getRealPath("/")+"images/"
		CryptoUtil.createCAKeys(cryptoPath+caPrivKey,cryptoPath+caCert)
		CryptoUtil.createRSAKeys(cryptoPath+privateKeyFile,cryptoPath+publicKeyFile)
		String subj1 = "/C=US/ST=Florida/L=West Palm Beach/O=Bartsy Owner LLC/OU=Support/CN=Bartsy/emailAddress=info@bartsy.com"
		CryptoUtil.createCSR(cryptoPath+privateKeyFile,cryptoPath+csrFile,subj1)
		CryptoUtil.createUserSignedCert(cryptoPath+csrFile,cryptoPath+certFile,cryptoPath+caCert,cryptoPath+caPrivKey)
	}



	def getServerPublicKey(){
		try{
			def pubKeyFile = grailsApplication.mainContext.getResource("images/bartsy_publicKey.pem").getFile()
			def pubKeyFileStream= new FileInputStream(pubKeyFile)
			response.setHeader("Content-disposition", "filename=bartsyPublicKey.pem")
			response.outputStream << pubKeyFileStream
			response.outputStream.flush()
		}catch(Exception e){
			log.error(e.getMessage())
		}


	}

	def getDecryptCredit(String encCard){
		def baseFolder = servletContext.getRealPath("/")
		PrivateKey bartsyPrivateKey=AsymmetricCipherTest.getPemPrivateKey(baseFolder+"images/bartsy_privateKey.pem","RSA")
		Base64 b64 = new Base64();
		//def hexMesg=b64.encode(hexOficeAES.getBytes());
		byte[] bb=b64.decode(encCard)
		log.info("decode with base 64")
		byte[] bDecryptedKey = AsymmetricCipherTest.decrypt(bb,bartsyPrivateKey)
		log.info("decrypt with Bartsy private key")
		String decCredit = new String(bDecryptedKey, "UTF8")
		decCredit = decCredit.trim();
		return decCredit
	}

	def getEncryptDecryptedKey(){
		def baseFolder = servletContext.getRealPath("/")
		PublicKey userPublicKey=CryptoUtil.readPublicKey(baseFolder+"images/bartsy_office.crt")
		String creditInfo = "4121212212121212122"
		String hexOficeAES=AsymmetricCipherTest.encrypt(creditInfo.getBytes(),userPublicKey)
		PrivateKey bartsyPrivateKey=AsymmetricCipherTest.getPemPrivateKey(baseFolder+"images/bartsy_privateKey.pem","RSA")
		Base64 b64 = new Base64();
		//def hexMesg=b64.encode(hexOficeAES.getBytes());
		byte[] bb=b64.decode(hexOficeAES)
		log.info("decode with base 64")
		byte[] bDecryptedKey = AsymmetricCipherTest.decrypt(bb,bartsyPrivateKey)
		log.info("decrypt with Bartsy private key")
		String decCredit = new String(bDecryptedKey, "UTF8")
		decCredit = decCredit.trim();
		println "zxzx "+decCredit
	}

}

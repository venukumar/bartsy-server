package bartsy

import grails.converters.JSON


class DataController {

	def applePNService
	def androidPNService

	/**
	 * This is the webservice to sync the data upon customer app start up
	 *
	 * @author Swetha Bhatnagar
	 *
	 * @errorCodes 1 : failure, 0 : success
	 *
	 * @param bartsyId				server generated id of the user
	 *
	 * @return  {
	 * @return      errorCode 		: success/failure code
	 * @return      errorMessage 	: success/failure message
	 * @return      venueId 		: venue id where user was checked into
	 * @return      venueName 		: name of the venue where the user was checked into
	 * @return      orders			: orders list placed by the user
	 * @return  }
	 *
	 **/
	def getUserOrders = {
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def bartsyId = json.bartsyId
				def totalOrders = []
				def userProfile = UserProfile.findByBartsyId(bartsyId.toString())
				if(userProfile){
					//def openOrders = Orders.findAllByUserAndOrderStatusNotEqual(userProfile,"0")
					def openOrdersCriteria = Orders.createCriteria()
					def openOrders = openOrdersCriteria.list {
						'in'("orderStatus",["0", "2", "3"])
						or{
							eq("user",userProfile)
							eq("receiverProfile",userProfile)
						}
					}
					if(openOrders){
						openOrders.each{
							def order = it
							def orderMap = [:]
							orderMap.put("orderId",order.orderId)
							orderMap.put("senderBartsyId",order.user.bartsyId)
							orderMap.put("recieverBartsyId",order.receiverProfile.bartsyId)
							orderMap.put("drinkOffered",order.drinkOffered)
							orderMap.put("orderTime",order.dateCreated.toGMTString())
							orderMap.put("orderStatus",order.orderStatus)
							orderMap.put("basePrice",order.basePrice)
							orderMap.put("totalPrice",order.totalPrice)
							orderMap.put("tipPercentage",order.tipPercentage)
							orderMap.put("itemName",order.itemName)
							orderMap.put("itemId",order.itemId)
							orderMap.put("description",order.description)
							orderMap.put("updateTime",order.lastUpdated.toGMTString())
							orderMap.put("specialInstructions",order.specialInstructions)
							totalOrders.add(orderMap)
						}
						response.put("errorCode","0")
						response.put("orders",totalOrders)
					}
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage","User Does not exist")
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
	 * This is the webservice to return the list of users checked into a venue
	 *
	 * @author Swetha Bhatnagar
	 *
	 * @errorCodes 1 : failure, 0 : success
	 *
	 * @param venueId         		server generated id of the venue
	 *
	 * @return  {
	 * @return      errorCode 		: success/failure code
	 * @return      errorMessage 	: success/failure message
	 * @return      checkedInUsers	: list of users checked in a particular venue
	 * @return  }
	 *
	 **/
	def checkedInUsersList = {
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def venueId = json.venueId
				def checkedInUsersList = []
				def venue = Venue.findByVenueId(venueId.toString())
				if(venue){
					def checkedInUsers = CheckedInUsers.findAllByVenueAndStatus(venue,1)
					if(checkedInUsers){
						checkedInUsers.each{
							def checkedInUser = it
							def checkedInUsersMap = [:]
							checkedInUsersMap.put("bartsyId",checkedInUser.userProfile.bartsyId.toString())
							checkedInUsersMap.put("nickName",checkedInUser.userProfile.nickName)
							checkedInUsersMap.put("userImagePath",checkedInUser.userProfile.userImage)
							checkedInUsersMap.put("gender",checkedInUser.userProfile.gender)
							checkedInUsersMap.put("orientation",checkedInUser.userProfile.orientation)
							checkedInUsersMap.put("status",checkedInUser.userProfile.status)
							checkedInUsersMap.put("description",checkedInUser.userProfile.description)
							checkedInUsersMap.put("dateOfBirth",checkedInUser.userProfile.dateOfBirth)
							checkedInUsersMap.put("showProfile",checkedInUser.userProfile.showProfile)
							checkedInUsersList.add(checkedInUsersMap)
						}
						response.put("checkedInUsers",checkedInUsersList)
					}
					else{
						response.put("errorCode","1")
						response.put("errorMessage","No Checked in Users")
					}
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage","Venue Does not exist")
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
	 * This is the webservice to sync the data upon bartender app start up
	 *
	 * @author Swetha Bhatnagar
	 *
	 * @errorCodes 1 : failure, 0 : success
	 *
	 * @param venueId				server generated id of the venue
	 *
	 * @return  {
	 * @return      errorCode 		: success/failure code
	 * @return      errorMessage 	: success/failure message
	 * @return      venueId 		: venue id where user was checked into
	 * @return      venueName 		: name of the venue where the user was checked into
	 * @return      orders			: orders list placed by the user
	 * @return  }
	 *
	 **/
	def syncBartenderApp = {
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def venue = Venue.findByVenueId(json.venueId.toString())
				def checkedInUsersList = []
				def ordersList = []
				if(venue){
					response.put("venueStatus",venue.status)
					def checkedInUsers = CheckedInUsers.findAllByVenueAndStatus(venue,1)
					if(checkedInUsers){
						checkedInUsers.each{
							def checkedInUser = it
							def checkedInUsersMap = [:]
							checkedInUsersMap.put("bartsyId",checkedInUser.userProfile.bartsyId)
							checkedInUsersMap.put("name",checkedInUser.userProfile.nickName)
							checkedInUsersMap.put("userImagePath",checkedInUser.userProfile.userImage)
							checkedInUsersMap.put("gender",checkedInUser.userProfile.gender)
							checkedInUsersMap.put("showProfile",checkedInUser.userProfile.showProfile)
							checkedInUsersList.add(checkedInUsersMap)
						}
						response.put("checkedInUsers",checkedInUsersList)
					}
					//def orders = Orders.findAllByVenue(venue)
					def ordersCriteria = Orders.createCriteria()
					def orders = ordersCriteria.list{
						eq("venue",venue)
						and{
							'in'("orderStatus",["0", "2", "3"])
						}
					}
					if(orders){
						orders.each{
							def order = it
							def ordersMap = [:]
							ordersMap.put("bartsyId",order.user.bartsyId)
							ordersMap.put("orderStatus",order.orderStatus)
							ordersMap.put("orderId",order.orderId)
							ordersMap.put("itemName",order.itemName)
							ordersMap.put("orderTime",order.dateCreated.toGMTString())
							ordersMap.put("basePrice",order.basePrice)
							ordersMap.put("tipPercentage",order.tipPercentage)
							ordersMap.put("totalPrice", order.totalPrice)
							ordersMap.put("description",order.description)
							ordersMap.put("updateTime",order.lastUpdated.toGMTString())
							ordersList.add(ordersMap)
						}
						response.put("orders",ordersList)
					}
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage","Venue Does not exist")
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

	def setConfigValues = {
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def userTimeout = json.userTimeout
				def venueTimeout =  json.venueTimeout
				def timer = BartsyConfiguration.findByConfigName("venueTimeout")
				timer.setValue(venueTimeout)
				timer.save()
				timer=BartsyConfiguration.findByConfigName("userTimeout")
				timer.setValue(userTimeout)
				timer.save()
				response.put("errorCode","0")
				response.put("errorMessage","Success")
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

	def sendMail={
		def json = JSON.parse(request)
		def mailId=json.mailId
		def message=json.message
		println "mailID" +mailId
		println "message"+message
		println "forget password !!!!!!!!!!! "
		sendMail {
			to mailId
			subject "Hello Friend"
			body message
		}
	}
	/**
	 * This method is used to recover the bartsy-login password
	 */
	def forgotPassword={
		def json = JSON.parse(request)
		def response = [:]
		def mailId=json.email
		def message=json.message
		println "mailID" +mailId
		println "message"+message
		try{
			def userProfile=UserProfile.findByEmail(mailId)
			println"email :::::::: "+userProfile
			def password=userProfile.bartsyPassword
			// Checking userProfile and password exists or not
			if(userProfile&&password){
				sendMail {
					to mailId
					subject "Recover bartsy password"
					body "Your bartsy login password is : "+password
				}
				response.put("errorCode","0")
				response.put("errorMessage","Password was sent your EmailId")
			}else{
				response.put("errorCode","1")
				response.put("errorMessage","Email ID doesn't exists")
			}
		}catch(Exception e){

			println "Exception Found !!!! "+e.getMessage()
		}
		render(text:response as JSON ,  contentType:"application/json")
	}

	/**
	 * This method is used to verify the user email id
	 */
	def userEmailVerification={ println "userEmailVerification" }

	def getNotifications = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				//retrieve the user profile based on bartsyId sent in the request to syscall
				def userProfile = UserProfile.findByBartsyId(json.bartsyId.toString())
				//retrieve the venue based on venueId sent in the request to syscall
				def venue = Venue.findByVenueId(json.venueId)
				//check if user profile and venue both exists
				if(userProfile && venue){
					//if user profile and venue both exists retrieve the notifications
					def notifications = Notifications.findAllByUserAndVenue(userProfile,venue)
					if(notifications){
						def notificationsList = []
						notifications.each{
							def notification = it
							def notificationMap = [:]
							//Add common details of all notifications
							notificationMap.put("id",notification.getId())
							notificationMap.put("message",notification.getMessage())
							notificationMap.put("type",notification.getType())
							notificationMap.put("userImage",notification.user.getUserImage())
							//add venueName for check in and check out notifications
							if(notification.getType().equals("checkin") || notification.getType().equals("checkout")){
								notificationMap.put("venueName",notification.venue.venueName)
							}
							//add order specific details for notifications like place order or update order
							if(notification.getType().equals("placeOrder") || notification.getType().equals("updateorder")){
								notificationMap.put("orderId",notification.order.orderId)
								notificationMap.put("orderStatus",notification.order.orderStatus)
								notificationMap.put("itemName",notification.order.itemName)
								notificationMap.put("totalPrice",notification.order.totalPrice)
								notificationMap.put("orderType",notification.getOrderType())
								//add receiver specific details if orderType is offer
								if(notification.getOrderType().equals("offer")){
									notificationMap.put("recieverName",notification.order.receiverProfile.nickName)
									notificationMap.put("recieverImage",notification.order.receiverProfile.userImage)
								}
							}
							//Add every notification to the list
							notificationsList.add(notificationMap)
						}
						//Add the list to response
						response.put("errorCode","0")
						response.put("notifications",notificationsList)
					}
					else{
						//Add errorcode 1 to response if notificatios are not available
						response.put("errorCode","1")
						response.put("errorMessage","No Notifications Available")
					}
				}
				else{
					//Add errorcode 1 to response if user or venue does not exist
					response.put("errorCode","1")
					response.put("errorMessage","User or Venue does not exists")
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
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON,contentType:"application/json")
	}

	def sendMessage = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				//retrieve the sender and receiver user profiles based on senderId and receiverId sent in the request to syscall
				def senderProfile = UserProfile.findByBartsyId(json.senderId.toString())
				def receiverProfile = UserProfile.findByBartsyId(json.receiverId.toString())
				//retrieve the venue based on venueId sent in the request to syscall
				def venue = Venue.findByVenueId(json.venueId)
				//check if user profiles and venue both exists
				if(senderProfile && receiverProfile && venue){
					//if user profiles and venue both exists save the message
					def message = new Messages()
					message.setSender(senderProfile)
					message.setReceiver(receiverProfile)
					message.setVenue(venue)
					message.setMessage(json.message)
					//save the message
					if(message.save()){
						def pnMessage = [:]
						pnMessage.put("message",json.message)
						pnMessage.put("senderId",senderProfile.bartsyId)
						pnMessage.put("messageType","message")
						pnMessage.put("receiverId",receiverProfile.bartsyId)
						//send PN to receiver device
						if(receiverProfile.deviceType == 1){
							applePNService.sendPN(pnMessage, receiverProfile.deviceToken, 1 , "You have recieved a new message")
						}
						else{
							androidPNService.sendPN(pnMessage, receiverProfile.deviceToken)
						}
						response.put("errorCode","0")
						response.put("errorMessage","Message sent")
					}
					else{
						//if message saving fails
						response.put("errorCode","1")
						response.put("errorMessage","Message could not be sent")
					}
				}
				else{
					//Add errorcode 1 to response if users or venue does not exist
					response.put("errorCode","1")
					response.put("errorMessage","Sender, Receiver or Venue does not exists")
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
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON,contentType:"application/json")
	}

}

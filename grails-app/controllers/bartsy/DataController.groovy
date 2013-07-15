package bartsy

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray


class DataController {

	def applePNService
	def androidPNService
	def grailsApplication

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
						or{
							eq("user",userProfile)
							eq("receiverProfile",userProfile)
						}
					}
					if(openOrders){

						openOrders.each{
							def order = it
							def checkedInuser = CheckedInUsers.findByUserProfileAndVenue(userProfile,order.venue)
							def orderMap = [:]
							orderMap.put("orderId",order.orderId)
							orderMap.put("senderBartsyId",order.user.bartsyId)
							orderMap.put("recieverBartsyId",order.receiverProfile.bartsyId)
							orderMap.put("senderNickname",order.user.nickName)
							orderMap.put("recipientNickname",order.receiverProfile.nickName)
							orderMap.put("SenderImagePath",order.user.userImage)
							orderMap.put("recipientImagePath",order.receiverProfile.userImage)
							orderMap.put("drinkOffered",order.drinkOffered)
							orderMap.put("orderTime",order.dateCreated.toGMTString())
							orderMap.put("orderStatus",order.orderStatus)
							orderMap.put("basePrice",order.basePrice)
							orderMap.put("totalPrice",order.totalPrice)
							orderMap.put("tipPercentage",order.tipPercentage)
							def itemsListStr
							if(order.itemsList){
								itemsListStr = new JSONArray(order.itemsList)
								orderMap.put("itemsList",itemsListStr)
							}else{
								orderMap.put("itemName",order.itemName)
								orderMap.put("itemId",order.itemId)
								orderMap.put("description",order.description)
							}
							orderMap.put("updateTime",order.lastUpdated.toGMTString())
							orderMap.put("specialInstructions",order.specialInstructions)
							orderMap.put("orderTimeout",order.venue.getCancelOrderTime())
							orderMap.put("currentTime",new Date().toGMTString())
							orderMap.put("userSessionCode",checkedInuser.userSessionCode)
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
				def bartsyId=json.bartsyId
				def userProfile = UserProfile.findByBartsyId(bartsyId)
				if(venue && userProfile){
					def checkedInUsers = CheckedInUsers.findAllByVenueAndStatus(venue,1)
					println"checkedInUsers "+checkedInUsers.size()
					if(checkedInUsers){
						checkedInUsers.each{
							def checkedInUser = it
							def checkedInUsersMap = [:]
							def userFavorite = UserFavoritePeople.findByUserBartsyIdAndFavoriteBartsyId(json.bartsyId, checkedInUser.userProfile.bartsyId)

							if(json.has("getUserDetails")&&json.getUserDetails&&json.getUserDetails.toString().equalsIgnoreCase("venue")){
								// Added order and checked in details of user to response
								getUserOrderAndChekedInDetails(venue,checkedInUser.userProfile,checkedInUsersMap)
							}

							checkedInUsersMap.put("bartsyId",checkedInUser.userProfile.bartsyId.toString())
							checkedInUsersMap.put("nickName",checkedInUser.userProfile.nickName)
							checkedInUsersMap.put("userImagePath",checkedInUser.userProfile.userImage)
							checkedInUsersMap.put("gender",checkedInUser.userProfile.gender)
							checkedInUsersMap.put("orientation",checkedInUser.userProfile.orientation)
							checkedInUsersMap.put("status",checkedInUser.userProfile.status)
							checkedInUsersMap.put("description",checkedInUser.userProfile.description)
							checkedInUsersMap.put("dateOfBirth",checkedInUser.userProfile.dateOfBirth)
							checkedInUsersMap.put("showProfile",checkedInUser.userProfile.showProfile)
							if(userFavorite)
								checkedInUsersMap.put("like",userFavorite.getStaus())
							else
								checkedInUsersMap.put("like","1")
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
					response.put("errorMessage","Venue or Bartsy Id Does not exist")
				}
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			println"Exception is ===> "+e.getMessage()
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}


	def getUserOrderAndChekedInDetails(Venue venue,UserProfile checkedInUser,checkedInUsersMap){
		// getting orders based on venue and bartsy id(user)
		def totalOrdersOrderedByUser = Orders.findAllByUserAndVenue(checkedInUser,venue)
		// current date
		def date = new Date()
		// last 30th date
		def last30thdate = date-30

		if(totalOrdersOrderedByUser)
		{
			def last30DaysOrdersOrderedByuser = Orders.createCriteria()

			def last30daysCount = last30DaysOrdersOrderedByuser.list(){
				and{
					eq("venue",venue)
					eq("user",checkedInUser)
				}
				gt("dateCreated",last30thdate)
				order("dateCreated", "desc")
			}

			def firstOrderDate =  Orders.createCriteria().list{
				eq("venue",venue)
				eq("user",checkedInUser)

				order("dateCreated", "asc")
			}
			checkedInUsersMap.put("firstOrderDate",firstOrderDate?firstOrderDate[0].dateCreated.toGMTString():"")
			checkedInUsersMap.put("orderCount",totalOrdersOrderedByUser.size())
			checkedInUsersMap.put("last30DaysOrderCount",last30daysCount?last30daysCount.size():"")

		}
		// getting checkedIn details of user based on venue
		def userCheckedInDetails = UserCheckInDetails.findAllByUserProfileAndVenue(checkedInUser,venue)
		if(userCheckedInDetails){
			def last30DaysCheckInCount = UserCheckInDetails.createCriteria()
			def last30DaysCheckIns = last30DaysCheckInCount.list(){
				and{
					eq("venue",venue)
					eq("userProfile",checkedInUser)
				}
				gt("checkedInDate",last30thdate)
				order("checkedInDate", "asc")
			}

			def firstCheckIn = UserCheckInDetails.createCriteria().list{
				and{
					eq("venue",venue)
					eq("userProfile",checkedInUser)
				}
				order("checkedInDate", "asc")
			}
			checkedInUsersMap.put("firstCheckInDate",firstCheckIn?firstCheckIn[0].checkedInDate.toGMTString():"")
			checkedInUsersMap.put("checkInCount",userCheckedInDetails.size())
			checkedInUsersMap.put("last30DaysCheckInCount",last30DaysCheckIns?last30DaysCheckIns.size():"")
		}
	}
	// getting order details of user based on venue
	def orderDetailsOfUserInVenue(Venue venue,UserProfile checkedInUser,checkedInUsersMap){
		// getting orders based on venue and bartsy id(user)
		def totalOrdersOrderedByUser = Orders.findAllByUserAndVenue(checkedInUser,venue)
		// current date
		def date = new Date()
		// last 30th date
		def last30thdate = date-30

		if(totalOrdersOrderedByUser)
		{
			def last30DaysOrdersOrderedByuser = Orders.createCriteria()

			def last30daysCount = last30DaysOrdersOrderedByuser.list(){
				and{
					eq("venue",venue)
					eq("user",checkedInUser)
				}
				gt("dateCreated",last30thdate)
				order("dateCreated", "desc")
			}

			def firstOrderDate =  Orders.createCriteria().list{
				eq("venue",venue)
				eq("user",checkedInUser)

				order("dateCreated", "asc")
			}
			checkedInUsersMap.put("firstOrderDate",firstOrderDate?firstOrderDate[0].dateCreated:"")
			checkedInUsersMap.put("orderCount",totalOrdersOrderedByUser.size())
			checkedInUsersMap.put("last30DaysOrderCount",last30daysCount?last30daysCount.size():"")

		}

	}

	// getting checkedIn details of user based on venue
	def userCheckedInDetails(Venue venue,UserProfile checkedInUser,checkedInUsersMap){
		// current date
		def date = new Date()
		// last 30th date
		def last30thdate = date-30
		// getting checkedIn details of user based on venue
		def userCheckedInDetails = UserCheckInDetails.findAllByUserProfileAndVenue(checkedInUser,venue)
		if(userCheckedInDetails){
			def last30DaysCheckInCount = UserCheckInDetails.createCriteria()
			def last30DaysCheckIns = last30DaysCheckInCount.list(){
				and{
					eq("venue",venue)
					eq("userProfile",checkedInUser)
				}
				gt("checkedInDate",last30thdate)
				order("checkedInDate", "asc")
			}

			def firstCheckIn = UserCheckInDetails.createCriteria().list{
				and{
					eq("venue",venue)
					eq("userProfile",checkedInUser)
				}
				order("checkedInDate", "asc")
			}
			checkedInUsersMap.put("firstCheckInDate",firstCheckIn?firstCheckIn[0].checkedInDate:"")
			checkedInUsersMap.put("checkInCount",userCheckedInDetails.size())
			checkedInUsersMap.put("last30DaysCheckInCount",last30DaysCheckIns?last30DaysCheckIns.size():"")
		}
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
				// This list used to send expiredO orders to bartender
				def expiredOrders=[]
				if(venue){
					response.put("venueStatus",venue.status)
					response.put("orderTimeout",venue.cancelOrderTime)
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
							checkedInUsersMap.put("userSessionCode",checkedInUser.userSessionCode)
							//userCheckedInDetails(venue,checkedInUser.userProfile,checkedInUsersMap)

							if(json.has("getUserDetails")&&json.getUserDetails&&json.getUserDetails.toString().equalsIgnoreCase("venue")){
								// Added order and checked in details of user to response
								getUserOrderAndChekedInDetails(venue,checkedInUser.userProfile,checkedInUsersMap)
							}
							checkedInUsersList.add(checkedInUsersMap)
						}
						response.put("checkedInUsers",checkedInUsersList)
					}
					//def orders = Orders.findAllByVenue(venue)
					def ordersCriteria = Orders.createCriteria()
					def orders = ordersCriteria.list{
						eq("venue",venue)
						and{
							'in'("orderStatus",["0", "2", "3", "7"])
						}
					}
					if(orders){
						orders.each{
							def order = it
							def ordersMap = [:]

							def userProfile = UserProfile.findByBartsyId(order.user.bartsyId)
							def checkedInuser = CheckedInUsers.findByUserProfileAndVenue(userProfile,venue)

							ordersMap.put("senderBartsyId",order.user.bartsyId)
							ordersMap.put("recipientBartsyId",order.receiverProfile.bartsyId)
							ordersMap.put("senderNickname",order.user.nickName)
							ordersMap.put("recipientNickname",order.receiverProfile.nickName)
							ordersMap.put("SenderImagePath",order.user.userImage)
							ordersMap.put("recipientImagePath",order.receiverProfile.userImage)
							ordersMap.put("orderStatus",order.orderStatus)
							ordersMap.put("orderId",order.orderId)
							def itemsListStr
							if(order.itemsList){
								itemsListStr = new JSONArray(order.itemsList)
								ordersMap.put("itemsList",itemsListStr)
							}else{
								ordersMap.put("itemName",order.itemName)
								ordersMap.put("itemId",order.itemId)
								ordersMap.put("description",order.description)
							}
							ordersMap.put("orderTime",order.dateCreated.toGMTString())
							ordersMap.put("basePrice",order.basePrice)
							ordersMap.put("tipPercentage",order.tipPercentage)
							ordersMap.put("totalPrice", order.totalPrice)
							ordersMap.put("updateTime",order.lastUpdated.toGMTString())
							//orderDetailsOfUserInVenue(venue,userProfile,ordersMap)
							if(!order.orderStatus.equalsIgnoreCase("7")){
								ordersMap.put("userSessionCode",checkedInuser.userSessionCode)
								ordersList.add(ordersMap)
							}
							else
								expiredOrders.add(ordersMap)
						}
						response.put("orders",ordersList)
						response.put("expiredOrders",expiredOrders)
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
				//	def venue = Venue.findByVenueId(json.venueId)
				//check if user profile and venue both exists
				if(userProfile){
					//if user profile and venue both exists retrieve the notifications
					def listOfNotifications = Notifications.createCriteria()
					if(listOfNotifications){

						def criteriaParams = [:]
						int index,noOfResults
						if(json.has("index")){
							index =  json.index
							params.offset = index
						}
						if(json.has("noOfResults")){
							noOfResults =  json.noOfResults
							params.max = noOfResults
						}
						criteriaParams.putAll(params)
						def	notifications = listOfNotifications.list(criteriaParams) {
							eq("user",userProfile)
							order("dateCreated", "desc")
						}
						def notificationsList = []
						notifications.each{
							def notification = it
							def notificationMap = [:]
							//Add common details of all notifications
							notificationMap.put("id",notification.getId())
							notificationMap.put("message",notification.getMessage())
							notificationMap.put("type",notification.getType())
							notificationMap.put("userImage",notification.user.getUserImage())
							notificationMap.put("venueImage",notification.venue.getVenueImagePath())
							notificationMap.put("createdTime",notification.getDateCreated().toGMTString())
							notificationMap.put("venueName",notification.venue.getVenueName())
							notificationMap.put("venueId",notification.venue.venueId)

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
							notificationsList.add(notificationMap)
						}
						//Add the list to response
						response.put("errorCode","0")
						response.put("errorMessage","Notifications Available")
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
					if(message.save(flush:true)){
						def pnMessage = [:]
						pnMessage.put("message",json.message)
						pnMessage.put("senderId",senderProfile.bartsyId)
						pnMessage.put("messageType","message")
						pnMessage.put("receiverId",receiverProfile.bartsyId)
						pnMessage.put("body","You have recieved a new message")
						//send PN to receiver device
						if(receiverProfile.deviceType == 1){
							applePNService.sendPN(pnMessage, receiverProfile.deviceToken, "1" , "You have recieved a new message")
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

	def getMessages = {
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
					//if user profiles and venue both exists retrieve the messages

					def criteriaParams = [:]
					int index,noOfResults
					if(json.has("index")){
						index =  json.index
						params.offset = index
					}
					if(json.has("noOfResults")){
						noOfResults =  json.noOfResults
						params.max = noOfResults
					}
					criteriaParams.putAll(params)


					/*def query = {
					 eq("venue",venue)
					 eq("sender",senderProfile)
					 eq("receiver",receiverProfile)
					 }
					 def queryRec = {
					 eq("venue",venue)
					 eq("sender",receiverProfile)
					 eq("receiver",senderProfile)
					 }*/
					def messages = Messages.createCriteria().list(criteriaParams){

						//eq("venue",venue)
						eq("sender",senderProfile)
						eq("receiver",receiverProfile)
						order("dateCreated","desc")

					}
					def messagesRec = Messages.createCriteria().list(criteriaParams){

						//eq("venue",venue)
						eq("sender",receiverProfile)
						eq("receiver",senderProfile)
						order("dateCreated","desc")
					}
					def compList = []

					if(messages)
						compList.addAll(messages.toList())
					if(messagesRec)
						compList.addAll(messagesRec.toList())

					if(compList){
						def messagesList = []
						response.put("errorCode",0)
						response.put("errorMessage","Messages sent")
						compList.each{
							def message = it
							def messageMap = [:]
							messageMap.put("id",message.id)
							messageMap.put("message",message.message)
							messageMap.put("senderId",message.sender.bartsyId)
							messageMap.put("receiverId",message.receiver.bartsyId)
							messageMap.put("date",message.dateCreated)
							messagesList.add(messageMap)
						}
						response.put("messages",messagesList)
					}
					else{
						//Add errorcode 1 to response if messages do not exist
						response.put("errorCode","1")
						response.put("errorMessage","No Messages to be displayed")
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


	/**
	 * This method used to change User bartsy password
	 */
	def changeUserPassword={
		def json = JSON.parse(request)
		def response=[:]
		def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
		if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
			if(json){
				if(json.has("bartsyLogin")){
					def userProfile = UserProfile.findByBartsyLogin(json.bartsyLogin)
					if(userProfile){
						def oldPassword
						def newPassword
						def existingPassword = userProfile.bartsyPassword
						if(json.has("oldPassword")){
							oldPassword = json.oldPassword
						}
						if(json.has("newPassword")){
							newPassword = json.newPassword
						}

						if(oldPassword&&oldPassword.toString().equalsIgnoreCase(existingPassword)){

							if(newPassword){
								userProfile.bartsyPassword=newPassword
								if(userProfile.save(flush:true)){
									response.put("errorCode", 0)
									response.put("errorMessage", "Your password is changed")
								}
							}else{
								response.put("errorCode", 1)
								response.put("errorMessage", "Please Enter new password")
							}
						}else{
							response.put("errorCode", 1)
							response.put("errorMessage", "Your oldPassword is wrong")
						}

					}else{
						response.put("errorCode", 1)
						response.put("errorMessage", "Your bartsy login doesn't exist")
					}
				}else{
					response.put("errorCode", 1)
					response.put("errorMessage", "Please Enter Bartsy Login")
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage", "Please Enter All Details")

			}
		}
		else{
			response.put("errorCode", 1)
			response.put("errorMessage","API version do not match")
		}
		render(text:response as JSON ,  contentType:"application/json")
	}
	/**
	 * This method used to send the bartsy password to user email
	 */
	def forgotPassword={
		def json = JSON.parse(request)
		def response=[:]
		def mailId=json.email
		println "mailID" +mailId
		try{
			def email=UserProfile.findByEmail(mailId)
			println"email :::::::: "+email
			if(email){
				sendMail {
					to mailId
					subject "Hello Friend"
					body "Your bartsy password is : "+email.bartsyPassword
				}
				response.put("errorCode", 0)
				response.put("errorMessage", "Your bartsy password was sent to Email")
			}else{
				response.put("errorCode", 1)
				response.put("errorMessage", "Your email id doesn't exists")
			}
		}catch(Exception e){

			println "Exception Found !!!! "+e.getMessage()
		}
		render(text:response as JSON ,  contentType:"application/json")
	}


}

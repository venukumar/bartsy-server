package bartsy

import grails.converters.JSON


class DataController {

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
		def json = JSON.parse(request)
		def bartsyId = json.bartsyId
		def response = [:]
		def totalOrders = []
		def userProfile = UserProfile.findByBartsyId(bartsyId)
		if(userProfile){
				//def openOrders = Orders.findAllByUserAndOrderStatusNotEqual(userProfile,"0")
				def openOrdersCriteria = Orders.createCriteria()
				def openOrders = openOrdersCriteria.list {
					eq("user",userProfile)
					and{
						'in'("orderStatus",["0","2","3"])
					}
				}
				if(openOrders){
					openOrders.each{
						def order = it
						def orderMap = [:]
						orderMap.put("orderId",order.orderId)
						orderMap.put("orderTime",order.dateCreated.toGMTString())
						orderMap.put("orderStatus",order.orderStatus)
						orderMap.put("basePrice",order.basePrice)
						orderMap.put("totalPrice",order.totalPrice)
						orderMap.put("tipPercentage",order.tipPercentage)
						orderMap.put("itemName",order.itemName)
						orderMap.put("itemId",order.itemId)
						orderMap.put("description",order.description)
						orderMap.put("updateTime",order.lastUpdated.toGMTString())
						totalOrders.add(orderMap)
					}
					response.put("orders",totalOrders)
				}
			}
		else{
			response.put("errorCode","1")
			response.put("errorMessage","User Does not exist")
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
		def json = JSON.parse(request)
		def venueId = json.venueId
		def response = [:]
		def checkedInUsersList = []
		def venue = Venue.findByVenueId(venueId.toString())
		if(venue){
			def checkedInUsers = CheckedInUsers.findAllByVenueAndStatus(venue,1)
			if(checkedInUsers){
				checkedInUsers.each{
					def checkedInUser = it
					if(checkedInUser.userProfile.showProfile.equals("ON")){
					def checkedInUsersMap = [:]
					checkedInUsersMap.put("bartsyId",checkedInUser.userProfile.bartsyId.toString())
					checkedInUsersMap.put("nickName",checkedInUser.userProfile.nickName)
					checkedInUsersMap.put("userImagePath",checkedInUser.userProfile.userImage)
					checkedInUsersMap.put("gender",checkedInUser.userProfile.gender)
					checkedInUsersMap.put("orientation",checkedInUser.userProfile.orientation)
					checkedInUsersMap.put("status",checkedInUser.userProfile.status)
					checkedInUsersMap.put("description",checkedInUser.userProfile.description)
					checkedInUsersMap.put("dateOfBirth",checkedInUser.userProfile.dateOfBirth)
					checkedInUsersList.add(checkedInUsersMap)
					}
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
		def json = JSON.parse(request)
		def venue = Venue.findByVenueId(json.venueId.toString())
		def checkedInUsersList = []
		def ordersList = []
		def response = [:]
		if(venue){
			response.put("venueStatus",venue.status)
			def checkedInUsers = CheckedInUsers.findAllByVenueAndStatus(venue,1)
			if(checkedInUsers){
				checkedInUsers.each{
					def checkedInUser = it
					def checkedInUsersMap = [:]
					checkedInUsersMap.put("bartsyId",checkedInUser.userProfile.bartsyId)
					checkedInUsersMap.put("name",checkedInUser.userProfile.name)
					checkedInUsersMap.put("userImagePath",checkedInUser.userProfile.userImage)
					checkedInUsersMap.put("gender",checkedInUser.userProfile.gender)
					checkedInUsersList.add(checkedInUsersMap)
				}
				response.put("checkedInUsers",checkedInUsersList)
			}
			//def orders = Orders.findAllByVenue(venue)
			def ordersCriteria = Orders.createCriteria()
			def orders = ordersCriteria.list{
				eq("venue",venue)
				and{
						'in'("orderStatus",["0","2","3"])
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
		render(text:response as JSON ,  contentType:"application/json")
	}
	
	def setConfigValues = {
		def json = JSON.parse(request)
		def userTimeout = json.userTimeout
		def venueTimeout =  json.venueTimeout
		def timer = BartsyConfiguration.findByConfigName("venueTimeout")
		timer.setValue(venueTimeout)
		timer.save()
		timer=BartsyConfiguration.findByConfigName("userTimeout")
		timer.setValue(userTimeout)
		timer.save()
		render(text:'success',  contentType:"application/text")
	}
}

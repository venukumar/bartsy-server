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
	def syncCustomerApp = {
		def json = JSON.parse(request)
		def bartsyId = json.bartsyId
		def response = [:]
		def totalOrders = []
		def userProfile = UserProfile.findByBartsyId(bartsyId)
		if(userProfile){
			def checkedInto = CheckedInUsers.findByUserProfileAndStatus(userProfile,1)
			if(checkedInto){
				response.put("errorCode","0")
				response.put("venueId",checkedInto.venue.venueId)
				response.put("venueName",checkedInto.venue.venueName)
				def openOrders = Orders.findAllByUserAndOrderStatusNotEqual(userProfile,"0")
				if(openOrders){
					println openOrders.size()
					openOrders.each{
						def order = it
						def orderMap = [:]
						orderMap.put("orderId",order.orderId)
						orderMap.put("orderTime",order.orderTime)
						orderMap.put("orderStatus",order.orderStatus)
						orderMap.put("basePrice",order.basePrice)
						orderMap.put("totalPrice",order.totalPrice)
						orderMap.put("tipPercentage",order.tipPercentage)
						orderMap.put("itemName",order.itemName)
						orderMap.put("itemId",order.itemId)
						orderMap.put("description",order.description)
						totalOrders.add(orderMap)
					}
					response.put("orders",totalOrders)
				}
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage","User not checked into any venue")
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
		println venueId
		def response = [:]
		def checkedInUsersList = []
		def venue = Venue.findByVenueId(venueId.toString())
		if(venue){
			def checkedInUsers = CheckedInUsers.findAllByVenue(venue)
			if(checkedInUsers){
				println checkedInUsers.size()
				checkedInUsers.each{
					def checkedInUser = it
					def checkedInUsersMap = [:]
					checkedInUsersMap.put("name",checkedInUser.userProfile.name)
					checkedInUsersMap.put("userImage",checkedInUser.userProfile.userImage)
					checkedInUsersMap.put("gender",checkedInUser.userProfile.gender)
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
		render(text:response as JSON ,  contentType:"application/json")
	}
}

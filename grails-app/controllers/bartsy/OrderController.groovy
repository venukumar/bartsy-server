package bartsy

import grails.converters.JSON


class OrderController {

	def applePNService
	def androidPNService

	def index() {
	}

	/**
	 * Author 		: Swetha Bhatnagar
	 * Description	: This is a webservice to be called while user places an order
	 */
	def placeOrder = {
		def json = JSON.parse(request)
		Map response = new HashMap()
		Orders order = new Orders()
		UserProfile userprofile = UserProfile.findByBartsyId(json.bartsyId)
		Venue venue = Venue.findByVenueId(json.venueId)
		if(userprofile && venue){
			println "userProfile and venue exists"
			def maxId = Orders.createCriteria().get { projections { max "orderId" } } as Long
			if(maxId){
				maxId = maxId+1
			}
			else{
				maxId = 100001
			}
			order.setOrderId(maxId)
			order.setBasePrice(json.basePrice)
			order.setItemId(json.itemId)
			order.setItemName(json.itemName)
			order.setTipPercentage(json.tipPercentage)
			order.setOrderStatus("NEW")
			order.setTotalPrice(json.totalPrice)
			order.setOrderTime(new Date())
			order.setUser(userprofile)
			order.setVenue(venue)

			println "Device Token:"+venue.deviceToken
			if(order.save()){
				response.put("orderId",maxId)
				response.put("orderStatus","NEW")
				response.put("errorCode","0")
				response.put("errorMessage","Order Placed")
				androidPNService.sendPlaceOrderPN("0", maxId.toString(), json.itemName, new Date().toString(), json.basePrice, json.tipPercentage, json.totalPrice, venue.deviceToken,"placeOrder")
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage","Order placing Failed")
			}
		}
		else{
			response.put("errorCode","1")
			response.put("errorMessage","Venue Id or User Id does not exists")
		}
		render(text:response as JSON,contentType:"application/json")
	}

	/**
	 * Author 		: Swetha Bhatnagar
	 * Description	: This is a webservice to be called while user updates the status of an order
	 */
	def updateOrderStatus = {
		def json = JSON.parse(request)
		Map response =  new HashMap()
		Orders order = Orders.findByOrderId(json.orderId)
		if(order) {
			order.setOrderStatus(json.orderStatus)
			if(order.save()){
				response.put("errorCode","0")
				response.put("errorMessage","Order Status Changed")
				if(json.orderStatus){
					println "device type:"+order.user.deviceType
					println "order status"+json.orderStatus
					if(order.user.deviceType == 1 ){

						applePNService.sendPN(order.orderStatus.toString(),order.orderId.toString(), order.user.deviceToken, "1","Your Order Has been Accepted","updateOrderStatus")
					}
					else{

						androidPNService.sendPN(order.orderStatus.toString(), order.orderId.toString(), order.user.deviceToken,"updateOrderStatus")
					}
				}
				else{
					response.put("errorCode","1")
				response.put("errorMessage","Please send order status flag")
				}
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage","Order Status Change Failed")
			}
			render(text:response as JSON,contentType:"application/json")
		}
		else{
			response.put("errorCode","1")
			response.put("errorMessage","Order Id does not exist")
			render(text:response as JSON,contentType:"application/json")
		}
	}

	//	def testPN = {
	//		applePNService.sendPN("Test", "1c0cd4989cd90e41350e114490832276ecace8bc622bb6c5878a4ddd2cba3c6f", "1")
	//	}
	//
	def testPNAndroid = {
		androidPNService.sendPN("ACCEPTED", "100001", "AQ5458PM84","Test")
		//println message(code:'venue.exists')
	}
}

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
				Map pnMessage = new HashMap()
				pnMessage.put("bartsyId",json.bartsyId)
				pnMessage.put("orderStatus","0")
				pnMessage.put("orderId",maxId.toString())
				pnMessage.put("itemName",json.itemName)
				pnMessage.put("orderTime",new Date().toString())
				pnMessage.put("basePrice",json.basePrice)
				pnMessage.put("tipPercentage",json.tipPercentage)
				pnMessage.put("totalPrice", json.totalPrice)	
				pnMessage.put("messageType","placeOrder")
				androidPNService.sendPN(pnMessage, venue.deviceToken)
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
		Map pnMessage = new HashMap()
		
		Orders order = Orders.findByOrderId(json.orderId)
		if(order) {
			order.setOrderStatus(json.orderStatus.toString())
			if(order.save()){
				response.put("errorCode","0")
				response.put("errorMessage","Order Status Changed")
				if(json.orderStatus){
					println "device type:"+order.user.deviceType
					println "order status"+json.orderStatus
					pnMessage.put("orderStatus",json.orderStatus.toString())
					pnMessage.put("orderId",json.orderId.toString())
					pnMessage.put("messageType","updateOrderStatus")
					if(order.user.deviceType == 1 ){
						applePNService.sendPN(pnMessage, order.user.deviceToken, "1","Your Order Has been Accepted")
					}
					else{

						androidPNService.sendPN(pnMessage,order.user.deviceToken)
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

	}

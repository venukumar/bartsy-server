package bartsy

import grails.converters.JSON


class OrderController {

	def applePNService
	def androidPNService

	/**
	 * This is the webservice to be called to place an order
	 *
	 * @author Swetha Bhatnagar
	 *
	 * @errorCodes 1 : failure, 0 : success
	 *
	 * @param venueId         		server generated id for venue
	 * @param bartsyId   	 		server generated id for the user
	 * @param baseprice    			base price of the item
	 * @param itemId       			locu id for the item
	 * @param itemName   			name of the item
	 * @param totalPrice  			base price+tipPercentage+bartsyFee
	 * @param tipPercentage		    percentage of tip offered by the user
	 * @param description   		description of the item
	 *
	 * @return  {
	 * @return      errorCode 		: success/failure code
	 * @return      errorMessage 	: success/failure message
	 * @return      orderId 		: order id generated for that order
	 * @return      orderStatus		: status of the order
	 * @return  }
	 *
	 **/
	def placeOrder = {
		try{
			def json = JSON.parse(request)
			Map response = new HashMap()
			Orders order = new Orders()
			UserProfile userprofile = UserProfile.findByBartsyId(json.bartsyId)
			Venue venue = Venue.findByVenueId(json.venueId)
			if(userprofile && venue){
				def maxId = Orders.createCriteria().get { projections { max "orderId"
					} } as Long
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
				order.setOrderTime(new Date().toGMTString())
				order.setDescription(json.description)
				order.setUser(userprofile)
				order.setVenue(venue)
				order.setUpdateTime(new Date().toGMTString())
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
					pnMessage.put("orderTime",new Date().toGMTString())
					pnMessage.put("basePrice",json.basePrice)
					pnMessage.put("tipPercentage",json.tipPercentage)
					pnMessage.put("totalPrice", json.totalPrice)
					pnMessage.put("messageType","placeOrder")
					pnMessage.put("description",json.description)
					pnMessage.put("updateTime",new Date().toGMTString())
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
		catch(Exception e){
			log.warn(e.message)
		}
	}

	/**
	 * This is the webservice to be called to update the status of the order
	 *
	 * @author Swetha Bhatnagar
	 *
	 * @errorCodes 1 : failure, 0 : success
	 *
	 * @param orderId         		server generated id for order
	 * @param orderStatus  	 		status to be updated for the order
	 *
	 * @return  {
	 * @return      errorCode 		: success/failure code
	 * @return      errorMessage 	: success/failure message
	 * @return  }
	 *
	 **/
	def updateOrderStatus = {
		def json = JSON.parse(request)
		Map response =  new HashMap()
		Map pnMessage = new HashMap()

		Orders order = Orders.findByOrderId(json.orderId)
		if(order) {
			order.setOrderStatus(json.orderStatus.toString())
			order.setUpdateTime(new Date().toGMTString())
			if(order.save()){
				response.put("errorCode","0")
				response.put("errorMessage","Order Status Changed")
				if(json.orderStatus){
					println "device type:"+order.user.deviceType
					println "order status"+json.orderStatus
					pnMessage.put("orderStatus",json.orderStatus.toString())
					pnMessage.put("orderId",json.orderId.toString())
					pnMessage.put("messageType","updateOrderStatus")
					pnMessage.put("updateTime",new Date().toGMTString())
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

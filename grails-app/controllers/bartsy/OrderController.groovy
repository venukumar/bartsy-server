package bartsy

import grails.converters.JSON

class OrderController {

	def applePNService
	def androidPNService
	def paymentService

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
			Date orderDate = new Date()
			UserProfile userprofile = UserProfile.findByBartsyId(json.bartsyId)
			Venue venue = Venue.findByVenueId(json.venueId)
			if(userprofile && venue){
				if(venue.status.equals("OPEN")){
					//def authorizeResponse = paymentService.authorizePayment()
					//def authorizeResponseParsed = JSON.parse(authorizeResponse)
					//println authorizeResponse.responseCodeText
					//authorizeResponse.responseCodeText.equals("Approved")
					if(true	){
						def maxId = Orders.createCriteria().get { projections { max "orderId" } } as Long
						if(maxId){
							maxId = maxId+1
						}
						else{
							maxId = 100001
						}
						//order.setAuthTransactionId(authorizeResponse.transactionId as long)
						order.setOrderId(maxId)
						order.setBasePrice(json.basePrice)
						order.setItemId(json.itemId.toString())
						order.setItemName(json.itemName)
						order.setTipPercentage(json.tipPercentage)
						order.setOrderStatus("0")
						order.setTotalPrice(json.totalPrice)
						order.setDescription(json.description)
						order.setUser(userprofile)
						order.setVenue(venue)
						if(order.save()){
							if(json.type != null && json.type.equals("custom")){
								addDrinkIngredients(json.ingredients,order)
							}
							def openOrdersCriteria = Orders.createCriteria()
							def openOrders = openOrdersCriteria.list {
								eq("venue",venue)
								and{
									eq("user",userprofile)
								}
								and{
									'in'("orderStatus",["0", "2", "3"])
								}
							}
							response.put("orderCount",openOrders.size())
							response.put("orderId",maxId)
							response.put("errorCode","0")
							response.put("errorMessage","Order Placed")
							Map pnMessage = new HashMap()
							pnMessage.put("bartsyId",json.bartsyId)
							pnMessage.put("orderStatus","0")
							pnMessage.put("orderId",maxId.toString())
							pnMessage.put("itemName",json.itemName)
							pnMessage.put("orderTime",orderDate.toGMTString())
							pnMessage.put("basePrice",json.basePrice)
							pnMessage.put("tipPercentage",json.tipPercentage)
							pnMessage.put("totalPrice", json.totalPrice)
							pnMessage.put("messageType","placeOrder")
							pnMessage.put("description",json.description)
							pnMessage.put("updateTime",orderDate.toGMTString())
							androidPNService.sendPN(pnMessage, venue.deviceToken)
						}
						else{
							response.put("errorCode","1")
							response.put("errorMessage","Order placing Failed")

						}
					}
					else{
						response.put("errorCode","1")
						response.put("errorMessage","Card not authorized")
					}

				}

				else{
					response.put("errorCode","1")
					response.put("errorMessage","Venue is CLOSED or OFFLINE")
				}
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage","Venue Id or User Id does not exists")
			}
			render(text:response as JSON,contentType:"application/json")
		}
		catch(Exception e){
			println "error Message"+e.getMessage()
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
		Date orderDate = new Date()
		Orders order = Orders.findByOrderId(json.orderId)
		if(order) {
			def body
			switch(json.orderStatus.toString()){
				case "1" :
					body = "Your order has been Rejected"
					break
				case "2" :
					body = "Your order has been Accepted"
					break
				case "3" :
					body = "Your order is Complete"
					break
				case "4" :
					body = "Your order has Failed"
					break
				case "5" :
					body = "You have picked up the order"
					break
				case "6" :
					body = "order is cancelled due to NOSHOW"
					break
			}
			if(true){
				if(json.orderStatus){
				order.setOrderStatus(json.orderStatus.toString())			
			if(order.save()){
				response.put("errorCode","0")
				response.put("errorMessage","Order Status Changed")
				def openOrdersCriteria = Orders.createCriteria()
				def openOrders = openOrdersCriteria.list {
					eq("venue",order.venue)
					and{
						eq("user",order.user)
					}
					and{
						'in'("orderStatus",["0", "2", "3"])
					}
				}
					pnMessage.put("orderCount",openOrders.size())
					pnMessage.put("orderStatus",json.orderStatus.toString())
					pnMessage.put("orderId",json.orderId.toString())
					pnMessage.put("messageType","updateOrderStatus")
					pnMessage.put("updateTime",orderDate.toGMTString())
					if(order.user.deviceType == 1 ){
						applePNService.sendPN(pnMessage, order.user.deviceToken, "1",body)
					}
					else{

						androidPNService.sendPN(pnMessage,order.user.deviceToken)
					}
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage","Order Status Change Failed")
				}
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage","Please send order status flag")
			}
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage","Payment failed")
			}
			render(text:response as JSON,contentType:"application/json")
		
		}
		else{
			response.put("errorCode","1")
			response.put("errorMessage","Order Id does not exist")
			render(text:response as JSON,contentType:"application/json")
		}
	}

//	def testPayment = {
//		//paymentService.capturePayment()
//		paymentService.authorizePayment()
//		//def responseText = paymentService.authorizePayment()
//		//render(text:responseText as JSON,contentType:"application/json")
//		paymentService.testing()
//	}

	def addDrinkIngredients(ingredients,order){
		ingredients.each{
			def ingredientId =  it
			def drinkIngredients = new DrinkIngredients()
			def ingredient =  Ingredients.findByIngredientId(ingredientId)
			drinkIngredients.setIngredient(ingredient)
			drinkIngredients.setOrder(order)
			drinkIngredients.save()
		}
	}
}

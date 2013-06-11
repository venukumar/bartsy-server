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
			UserProfile	recieverUserprofile = UserProfile.findByBartsyId(json.recieverBartsyId)
			Venue venue = Venue.findByVenueId(json.venueId)
			if(userprofile && venue){
				if(venue.status.equals("OPEN")){
					def authorizeResponse = paymentService.authorizePayment(userprofile,json.totalPrice)
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
					//order.setOrderStatus("0")
					order.setTotalPrice(json.totalPrice)
					order.setDescription(json.description)
					// Receiver bartsy id
					order.setRecieverBartsyId(json.recieverBartsyId as long)
					order.setUser(userprofile)
					order.setVenue(venue)
					if(authorizeResponse.get("authApproved").toBoolean()){
						order.setOrderStatus("0")
						order.setAuthApproved("true")
						order.setAuthCode(authorizeResponse.get("authCode"))
						order.setAuthTransactionNumber(authorizeResponse.get("authTransactionNumber"))
					}
					else{
						order.setOrderStatus("7")
						order.setAuthApproved("false")
						order.setAuthErrorMessage(authorizeResponse.get("authErrorMessage"))
						order.setLastState("0")
						order.setErrorReason("Payment Auth Failed")
						order.save()
						response.put("errorCode",1)
						response.put("errorMessage",authorizeResponse.get("authErrorMessage"))
						render(text:response as JSON,contentType:"application/json")
						return
					}
					if(order.save()){
						if(json.type != null && json.type.equals("custom")){
							addDrinkIngredients(json.ingredients,order)
						}
						def openOrdersCriteria = Orders.createCriteria()
						def openOrders = openOrdersCriteria.list {
							eq("venue",venue)
							and{ eq("user",userprofile) }
							and{
								'in'("orderStatus",["0", "2", "3"])
							}
						}
						Map pnMessage = new HashMap()
						pnMessage.put("orderStatus","0")
						pnMessage.put("orderId",maxId.toString())
						pnMessage.put("itemName",json.itemName)
						pnMessage.put("orderTime",orderDate.toGMTString())
						pnMessage.put("basePrice",json.basePrice)
						pnMessage.put("tipPercentage",json.tipPercentage)
						pnMessage.put("totalPrice", json.totalPrice)
						pnMessage.put("description",json.description)
						pnMessage.put("updateTime",orderDate.toGMTString())
						pnMessage.put("orderTimeout",venue.cancelOrderTime)
						if(recieverUserprofile && !userprofile.equals(recieverUserprofile)){
							println "userprofiles are not same"
							def body="You have been offered a drink "+json.itemName+" by "+order.user.nickName
							pnMessage.put("SenderBartsyId",json.bartsyId)
							pnMessage.put("messageType","DrinkOffered")
							pnMessage.put("bartsyId",json.recieverBartsyId)
							pnMessage.put("recieverBartsyId",json.bartsyId)
							pnMessage.put("orderTimeout",venue.cancelOrderTime)
							response.put("orderCount",openOrders.size())
							response.put("orderId",maxId)
							response.put("errorCode","0")
							response.put("errorMessage","Drink Sent")
							response.put("orderTimeout",venue.cancelOrderTime)
							if(recieverUserprofile.deviceType == 1 ){
								try{
									applePNService.sendPN(pnMessage, recieverUserprofile.deviceToken, "1",body)
								}catch(Exception e){
									println "Exception "+e.getMessage()
								}
							}
							else{

								androidPNService.sendPN(pnMessage,recieverUserprofile.deviceToken)
							}
						}else{
							pnMessage.put("bartsyId",json.bartsyId)
							pnMessage.put("messageType","placeOrder")
							response.put("orderCount",openOrders.size())
							response.put("orderId",maxId)
							response.put("errorCode","0")
							response.put("errorMessage","Order Placed")
							response.put("orderTimeout",venue.cancelOrderTime)
							androidPNService.sendPN(pnMessage, venue.deviceToken)
						}

					}
					else{
						response.put("errorCode","1")
						response.put("errorMessage","Order placing Failed")

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
			order.setOrderStatus(json.orderStatus.toString())
			def body
			switch(json.orderStatus.toString()){
				case "1" :
					body = "Your order has been Rejected"
					order.setLastState("0")
					order.setErrorReason("Order Rejected")
					break
				case "2" :
					body = "Your order has been Accepted"
					break
				case "3" :
					body = "Your order is Complete"
					break
				case "4" :
					order.setLastState("2")
					order.setErrorReason("Order Failed")
					body = "Your order has Failed"
					break
				case "5" :
					order = paymentService.makePayment(order)
					if(order.getCaptureApproved().toBoolean()){
						body = "You have picked up the order"
					}
					else{
						body = "Order has been cancelled due to payment failure"
					}
					break

				case "6" :
					body = "order is cancelled due to NOSHOW"
					order.setLastState("3")
					order.setErrorReason("NOSHOW")
					break
			}
			if(order.save()){
				if(!json.orderStatus.toString().equals("5") || order.getCaptureApproved().toBoolean()){
					response.put("errorCode","0")
					response.put("errorMessage","Order Status Changed")
					response.put("orderTimeout",order.venue.cancelOrderTime)
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage","Order has been cancelled due to payment failure")
				}

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
				pnMessage.put("body",body)
				pnMessage.put("orderTimeout",order.venue.cancelOrderTime)
				if(order.recieverBartsyId && order.recieverBartsyId > 0){
					def recieverUser = UserProfile.findByBartsyId(order.recieverBartsyId)
					if(recieverUser.deviceType == 1 ){
						applePNService.sendPN(pnMessage, recieverUser.deviceToken, "1",body)
					}
					else{
						androidPNService.sendPN(pnMessage,recieverUser.deviceToken)
					}
				}
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
			render(text:response as JSON,contentType:"application/json")
		}
		else{
			response.put("errorCode","1")
			response.put("errorMessage","Order Id does not exist")
			render(text:response as JSON,contentType:"application/json")
		}
	}
	/**
	 * To Update status of the offered drink order
	 */

	def updateOfferedDrinkStatus = {
		def json = JSON.parse(request)
		Map response =  new HashMap()
		Map pnMessage = new HashMap()
		Date orderDate = new Date()
		// get the order from DB by orderId
		Orders order = Orders.findByOrderId(json.orderId)
		Venue venue = Venue.findByVenueId(json.venueId)
		def recieveUser = UserProfile.findByBartsyId(json.bartsyId)
		// checking the order is empty
		if(order) {
			if(json.orderStatus){
				order.setOrderStatus(json.orderStatus.toString())
				def body
				switch(json.orderStatus.toString()){
					case "8" :
						body = "Your offer is rejected by "+recieveUser.nickName
						order.setLastState("0")
						order.setErrorReason("Offer rejected")
						if(order.save()){
							response.put("errorCode","0")
							response.put("errorMessage","Success")
							response.put("orderTimeout",venue.cancelOrderTime)
							pnMessage.put("orderId",json.orderId.toString())
							pnMessage.put("messageType","DrinkOfferRejected")
							pnMessage.put("updateTime",orderDate.toGMTString())
							pnMessage.put("orderTimeout",venue.cancelOrderTime)
							// checking deviceType android is 0 and iphone is 1
							if(order.user.deviceType == 1 ){
								// Sending push notification to the iphone
								applePNService.sendPN(pnMessage, order.user.deviceToken, "1",body)
							}
							else{
								// Sending push notification to the android device
								androidPNService.sendPN(pnMessage,order.user.deviceToken)
							}
						}
						else{
							response.put("errorCode","1")
							response.put("errorMessage","Order Status Change Failed")
						}
						break
					case "0" :
						body = "Your offer is accepted by "+recieveUser.nickName
						if(order.save()){
							response.put("errorCode","0")
							response.put("errorMessage","Success")
							response.put("orderTimeout",venue.cancelOrderTime)
							pnMessage.put("orderId",json.orderId.toString())
							pnMessage.put("messageType","DrinkOfferAccepted")
							pnMessage.put("updateTime",orderDate.toGMTString())
							pnMessage.put("bartsyId",json.bartsyId)
							pnMessage.put("messageType","placeOrder")
							pnMessage.put("orderStatus","0")
							pnMessage.put("orderId",order.orderId)
							pnMessage.put("itemName",order.itemName)
							pnMessage.put("orderTime",orderDate.toGMTString())
							pnMessage.put("basePrice",order.basePrice)
							pnMessage.put("tipPercentage",order.tipPercentage)
							pnMessage.put("totalPrice", order.totalPrice)
							pnMessage.put("description",order.description)
							pnMessage.put("orderTimeout",venue.cancelOrderTime)
							//sending PN to Bartender
							androidPNService.sendPN(pnMessage, venue.deviceToken)
							// checking deviceType android is 0 and iphone is 1
							if(order.user.deviceType == 1 ){
								//sending PN to iphone device
								applePNService.sendPN(pnMessage, order.user.deviceToken, "1",body)
							}
							else{
								//sending PN to android device
								androidPNService.sendPN(pnMessage,order.user.deviceToken)
							}
						}
						else{
							response.put("errorCode","1")
							response.put("errorMessage","Order Status Change Failed")
						}
						break
				}
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage","Please send order status flag")
			}
		}
		else{
			response.put("errorCode","1")
			response.put("errorMessage","Order Id does not exist")
			render(text:response as JSON,contentType:"application/json")
		}
		render(text:response as JSON,contentType:"application/json")
	}

	def placeOrderOld = {
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
								and{ eq("user",userprofile) }
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


	def testPayment = {
		def response = paymentService.authorizePayment()
		render(text:response as JSON,contentType:"application/json")
	}

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

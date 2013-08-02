package bartsy

import grails.converters.JSON
import java.text.SimpleDateFormat
import org.codehaus.groovy.grails.web.json.JSONArray

class OrderController {

	def applePNService
	def androidPNService
	def paymentService
	def orderService

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
		def response = [:]
		try{
			def json
			if(params.orderId){
				json=params
			}else{
				json = JSON.parse(request)
			}
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				Orders order = new Orders()
				Date orderDate = new Date()
				UserProfile userprofile = UserProfile.findByBartsyId(json.bartsyId)
				UserProfile	recieverUserprofile = UserProfile.findByBartsyId(json.recieverBartsyId)
				Venue venue = Venue.findByVenueId(json.venueId)
				if(userprofile && venue){
					if(venue.status.equals("OPEN")){
						CommonMethods common = new CommonMethods()

						if(json.totalPrice && common.isInteger(json.totalPrice)){

							def maxId = Orders.createCriteria().get { projections { max "orderId"
								} } as Long
							if(maxId){
								maxId = maxId+1
							}
							else{
								maxId = 100001
							}
							if(json.itemsList){

								order.setItemsList(json.itemsList.toString())
							}
							println "json.itemId  "+json.itemId
							println"json.itemName "+json.itemName

							order.setOrderId(maxId)
							order.setBasePrice(json.basePrice)
							if(json.itemId){
								order.setItemId(json.itemId)
							}
							if(json.itemName){
								order.setItemName(json.itemName)
							}
							//order.setItemId(json.itemId?json.itemId.toString():"")
							//order.setItemName(json.itemName?json.itemName:"")
							order.setTipPercentage(json.tipPercentage)
							order.setTotalPrice(json.totalPrice)
							order.setDescription(json.description)
							order.setSpecialInstructions(json.specialInstructions)
							order.setDateOffered(new Date())
							// Receiver bartsy id
							order.setReceiverProfile(recieverUserprofile)
							order.setUser(userprofile)
							order.setVenue(venue)
							order.setOrderStatus("100")
							order.setAuthApproved("false")
							order.save(flush:true)

							if(order){
								if(json.itemsList){
									json.itemsList.each{
										def itemInfo = it
										OrderItems orderItem = new OrderItems()
										orderItem.setVersion(1)
										orderItem.setItemName(itemInfo.itemName)
										orderItem.setItemId(itemInfo.itemId.toString())
										orderItem.setBasePrice(itemInfo.basePrice)
										orderItem.setDescription(itemInfo.description)
										orderItem.setOrder(order)
										orderItem.save(flush:true)
									}

								}else{
									OrderItems orderItem = new OrderItems()
									orderItem.setItemName(json.itemName?json.itemName:"")
									orderItem.setItemId(json.itemId?json.itemId.toString():"")
									orderItem.setBasePrice(json.basePrice)
									orderItem.setDescription(json.description)
									orderItem.setOrder(order)
									orderItem.save(flush:true)
								}
							}
							Orders orderUpdate = Orders.findByOrderId(order.orderId)
							def authorizeResponse = paymentService.authorizePayment(userprofile,json.totalPrice,orderUpdate?.orderId)
							//order.setAuthTransactionId(authorizeResponse.transactionId as long)

							if(authorizeResponse.get("authApproved").toBoolean()){

								if(!json.bartsyId.toString().equals(json.recieverBartsyId.toString())){

									orderUpdate.setOrderStatus("9")
								}else{

									orderUpdate.setOrderStatus("0")

								}

								orderUpdate.setAuthApproved("true")
								orderUpdate.setAuthCode(authorizeResponse.get("authCode"))
								orderUpdate.setAuthTransactionNumber(authorizeResponse.get("authTransactionNumber"))
							}
							else{

								orderUpdate.setOrderStatus("7")
								orderUpdate.setAuthApproved("false")
								orderUpdate.setAuthErrorMessage(authorizeResponse.get("authErrorMessage"))
								orderUpdate.setLastState("0")
								orderUpdate.setErrorReason("Payment Auth Failed")

								orderUpdate.save(flush:true)
								response.put("errorCode",1)
								response.put("errorMessage",authorizeResponse.get("authErrorMessage"))
								render(text:response as JSON,contentType:"application/json")
								return
							}

							if(orderUpdate.save(flush:true)){
								//create the place order notification
								def notification = new Notifications()
								notification.setUser(userprofile)
								notification.setVenue(venue)
								notification.setOrder(orderUpdate)
								notification.setType("placeOrder")
								if(json.type && json.type.equals("custom")){
									addDrinkIngredients(json.ingredients,orderUpdate)
								}
								def openOrdersCriteria = Orders.createCriteria()
								def openOrders = openOrdersCriteria.list {
									eq("venue",venue)
									and{ eq("user",userprofile) }
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
								Map pnMessage = new HashMap()
								if(!json.bartsyId.toString().equals(json.recieverBartsyId.toString())){
									pnMessage.put("orderStatus","9")
								}else{
									pnMessage.put("orderStatus","0")
								}
								pnMessage.put("orderId",orderUpdate?.orderId?.toString())
								pnMessage.put("itemName",json.itemName?json.itemName:"")
								pnMessage.put("orderTime",orderDate.toGMTString())
								pnMessage.put("basePrice",json.basePrice)
								pnMessage.put("tipPercentage",json.tipPercentage)
								pnMessage.put("totalPrice", json.totalPrice)
								pnMessage.put("description",json.description)
								pnMessage.put("updateTime",orderDate.toGMTString())
								pnMessage.put("orderTimeout",venue.cancelOrderTime)
								pnMessage.put("specialInstructions",json.specialInstructions ?: "")
								pnMessage.put("itemsList",json.itemsList?json.itemsList.toString():"")
								if(!json.bartsyId.toString().equals(json.recieverBartsyId.toString())){
									def name = json.itemName?json.itemName:""
									def body="You have been offered a drink "+name+" by "+orderUpdate.user.nickName
									pnMessage.put("messageType","DrinkOffered")
									pnMessage.put("bartsyId",json.recieverBartsyId)
									pnMessage.put("senderBartsyId",json.bartsyId)
									pnMessage.put("orderTimeout",venue.cancelOrderTime)
									pnMessage.put("body",body)
									response.put("orderCount",openOrders.size())
									response.put("orderId",maxId)
									response.put("errorCode","0")
									response.put("errorMessage","Drink Sent")
									response.put("orderTimeout",venue.cancelOrderTime)
									if(recieverUserprofile.deviceType == 1 ){
										try{
											pnMessage.put("unReadNotifications",common.getNotifictionCount(recieverUserprofile))
											applePNService.sendPN(pnMessage,recieverUserprofile.deviceToken, "1",body)
										}catch(Exception e){

											log.info("Exception "+e.getMessage())
										}
									}
									else{
										androidPNService.sendPN(pnMessage,recieverUserprofile.deviceToken)
									}
								}else{
									pnMessage.put("bartsyId",json.bartsyId)
									pnMessage.put("messageType","placeOrder")
									def map=[:]
									common.getUserOrderAndChekedInDetails(venue, orderUpdate.user, map)
									pnMessage.put("checkInAndOrderDetailsOfUser",map)
									response.put("orderCount",openOrders.size())
									response.put("orderId",orderUpdate?.orderId)
									if(orderUpdate.user.emailVerified.toString().equalsIgnoreCase("true")){
										response.put("errorCode","0")
										response.put("errorMessage","Order Placed")
									}else{
										response.put("errorCode","99")
										response.put("errorMessage","Order Placed. Please verify your account to start collecting rewards")

									}
									response.put("orderStatus",orderUpdate.getOrderStatus())
									response.put("orderTimeout",venue.cancelOrderTime)
									androidPNService.sendPN(pnMessage, venue.deviceToken)
									//save the place order for self notification
									notification.setOrderType("self")
									notification.setMessage("you ordered a drink "+ orderUpdate.getItemName())
									notification.save(flush:true)
								}
							}
							else{
								response.put("errorCode","1")
								response.put("errorMessage","Order placing Failed")
							}
						}else{
							response.put("errorCode","2")
							response.put("errorMessage","Total price contains not a valid data")
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
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			log.info("Exception in place order ===> "+e.getMessage())
			println"Exception in place order "+e.getMessage()
			response.put("errorCode",200)
			response.put("errorMessage","Error occured while processing your request. Please verify json")
		}
		render(text:response as JSON,contentType:"application/json")
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
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def listOfOrders = json.has("orderList")?json.orderList:""
				if(listOfOrders){
					def failureOrders=[]
					listOfOrders.each{
						def orderObject = it
						def order = Orders.findByOrderId(orderObject.orderId)
						Map pnMessage = new HashMap()
						Date orderDate = new Date()
						if(order) {
							def body
							switch(orderObject.orderStatus.toString()){
								case "1" :
									body = "Your order has been Rejected"
									order.setLastState("0")
									order.setErrorReason(orderObject.has("errorReason")?orderObject.errorReason:"Order Rejected")
									break
								case "2" :
									body = "Your order has been Accepted"
									break
								case "3" :
									body = "Your order is Complete"
									break
								case "4" :
									order.setLastState("2")
									order.setErrorReason(orderObject.has("errorReason")?orderObject.errorReason:"Order Failed")
									body = "Your order has Failed"
									break
								case "5" :
									order = paymentService.makePayment(order)
									if(order.getCaptureApproved().toBoolean()){
										body = "You have picked up the order"
										//Calculating reward points
										CommonMethods common = new CommonMethods()
										common.calculateRewardPoints(order)
									}
									else{
										body = "Order has been cancelled due to payment failure"
									}
									break
								case "6" :
									body = "order is cancelled due to NOSHOW"
									order.setLastState("3")
									order.setErrorReason(orderObject.has("errorReason")?orderObject.errorReason:"NOSHOW")
									break
								case "10" :
									order.setLastState(order.orderStatus)
									order.setErrorReason(orderObject.has("errorReason")?orderObject.errorReason:"Dismiss")
									break;
							}
							order.setOrderStatus(orderObject.orderStatus.toString())
							if(order.save()){
								//create the place order notification
								def notification = new Notifications()
								notification.setUser(order.user)
								notification.setVenue(order.venue)
								notification.setOrder(order)
								notification.setType("updateorder")
								if(!orderObject.orderStatus.toString().equals("10")){
									response.put("orderTimeout",order.venue.cancelOrderTime)
									if(!orderObject.orderStatus.toString().equals("5") || order.getCaptureApproved().toBoolean()){
										//response.put("errorCode","0")
										//response.put("errorMessage","Order Status Changed")
									}
									else{
										def failure = [:]
										failure.put("errorCode",3)
										failure.put("orderId",order.orderId)
										failure.put("errorReason","Order has been cancelled due to payment failure")
										failure.put("orderTimeout",order.venue.cancelOrderTime)
										failureOrders.add(failure)
										//response.put("errorCode","1")
										//response.put("errorMessage","Order has been cancelled due to payment failure")
									}


									def openOrdersCriteria = Orders.createCriteria()
									def openOrders = openOrdersCriteria.list {
										eq("venue",order.venue)
										and{
											eq("user",order.user)
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
										}
									}
									pnMessage.put("orderCount",openOrders.size())
									pnMessage.put("orderStatus",orderObject.orderStatus.toString())
									pnMessage.put("orderId",orderObject.orderId.toString())
									pnMessage.put("messageType","updateOrderStatus")
									pnMessage.put("updateTime",orderDate.toGMTString())
									pnMessage.put("currentTime",new Date().toGMTString())
									pnMessage.put("body",body)
									pnMessage.put("orderTimeout",order.venue.cancelOrderTime)
									CommonMethods common = new CommonMethods()
									if(order.receiverProfile.bartsyId && !order.receiverProfile.bartsyId.equals(order.user.bartsyId))
									{
										def recieverUser = UserProfile.findByBartsyId(order.receiverProfile.bartsyId)
										if(recieverUser.deviceType == 1 ){

											pnMessage.put("unReadNotifications",common.getNotifictionCount(recieverUser))
											applePNService.sendPN(pnMessage, recieverUser.deviceToken, "1",body)
										}
										else{
											androidPNService.sendPN(pnMessage,recieverUser.deviceToken)
										}
										//save the place order for others notification
										notification.setOrderType("offer")
										notification.setMessage(body)
										notification.save(flush:true)
									}
									else{
										//save the place order for self notification
										notification.setOrderType("self")
										notification.setMessage(body)
										notification.save(flush:true)
									}
									if(order.user.deviceType == 1 ){
										pnMessage.put("unReadNotifications",common.getNotifictionCount(order.user))
										applePNService.sendPN(pnMessage, order.user.deviceToken, "1",body)
									}
									else{
										androidPNService.sendPN(pnMessage,order.user.deviceToken)
									}
								}else{
									//								response.put("errorCode","0")
									//								response.put("errorMessage","Order Status Changed")
								}
							}
							else{
								def failure=[:]
								failure.put("errorCode",2)
								failure.put("orderId",order.orderId)
								failure.put("errorReason","Order Status Change Failed")
								failure.put("orderTimeout",order.venue.cancelOrderTime)
								failureOrders.add(failure)
								//							response.put("errorCode","1")
								//							response.put("errorMessage","Order Status Change Failed")
							}
						}
						else{
							def failure=[:]
							failure.put("errorCode",1)
							failure.put("orderId",orderObject.orderId)
							failure.put("errorReason","Order Id does not exist")
							failureOrders.add(failure)
							//						response.put("errorCode","1")
							//						response.put("errorMessage","Order Id does not exist")
						}
					}
					if(failureOrders.size()>0){
						response.put("errorCode","1")
						response.put("errorCodes",failureOrders)
					}else{
						response.put("errorCode","0")
						response.put("errorMessage","Order Status Changed")
					}


				}else{
					response.put("errorCode","1")
					response.put("errorMessage","Order Id's are missing please send again")
				}
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON,contentType:"application/json")
	}
	/**
	 * To Update status of the offered drink order
	 */

	def updateOfferedDrinkStatus = {
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				Map pnMessage = new HashMap()
				Date orderDate = new Date()
				// get the order from DB by orderId
				Orders order = Orders.findByOrderId(json.orderId)
				Venue venue = Venue.findByVenueId(json.venueId)
				def recieveUser = UserProfile.findByBartsyId(json.bartsyId)
				// checking the order is empty
				if(order) {
					CommonMethods common = new CommonMethods()
					if(json.orderStatus){
						order.setOrderStatus(json.orderStatus.toString())
						def body
						switch(json.orderStatus.toString()){
							case "8" :
								body = "Your offer is rejected by "+recieveUser.nickName
								order.setLastState("9")
								order.setErrorReason("Offer rejected")
								order.setDateCreated(new Date())
								if(order.save()){
									response.put("errorCode","0")
									response.put("errorMessage","Success")
									response.put("orderTimeout",venue.cancelOrderTime)
									pnMessage.put("orderId",json.orderId.toString())
									pnMessage.put("messageType","DrinkOfferRejected")
									pnMessage.put("updateTime",orderDate.toGMTString())
									pnMessage.put("orderTimeout",venue.cancelOrderTime)
									pnMessage.put("currentTime",new Date().toGMTString())
									pnMessage.put("body",body)
									// checking deviceType android is 0 and iphone is 1
									if(order.user.deviceType == 1 ){

										pnMessage.put("unReadNotifications",common.getNotifictionCount(order.user))
										// Sending push notification to the iphone
										applePNService.sendPN(pnMessage, order.user.deviceToken, "1",body)
									}
									else{
										// Sending push notification to the android device
										androidPNService.sendPN(pnMessage,order.user.deviceToken)
									}
									//save the update order notification
									def notification = new Notifications()
									notification.setUser(order.user)
									notification.setVenue(order.venue)
									notification.setOrder(order)
									notification.setType("updateorder")
									notification.setOrderType("offer")
									notification.setMessage(body)
									notification.save(flush:true)
								}
								else{
									response.put("errorCode","1")
									response.put("errorMessage","Order Status Change Failed")
								}
								break
							case "0" :
								body = "Your offer is accepted by "+recieveUser.nickName
								order.setOrderStatus("0")
								if(order.save(flush:true)){
									response.put("errorCode","0")
									response.put("errorMessage","Success")
									response.put("orderTimeout",venue.cancelOrderTime)
									pnMessage.put("orderId",json.orderId.toString())
									pnMessage.put("updateTime",orderDate.toGMTString())
									pnMessage.put("bartsyId",order.receiverProfile.getBartsyId())
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
									pnMessage.put("currentTime",new Date().toGMTString())
									pnMessage.put("body",body)
									//sending PN to Bartender
									androidPNService.sendPN(pnMessage, venue.deviceToken)
									// checking deviceType android is 0 and iphone is 1
									if(order.user.deviceType == 1 ){
										//sending PN to iphone device
										pnMessage.put("messageType","DrinkOfferAccepted")
										pnMessage.put("unReadNotifications",common.getNotifictionCount(order.user))
										applePNService.sendPN(pnMessage, order.user.deviceToken, "1",body)
									}
									else{
										//sending PN to android device
										pnMessage.put("messageType","DrinkOfferAccepted")
										androidPNService.sendPN(pnMessage,order.user.deviceToken)
									}
									//save the update order notification
									def notification = new Notifications()
									notification.setUser(order.user)
									notification.setVenue(order.venue)
									notification.setOrder(order)
									notification.setType("updateorder")
									notification.setOrderType("offer")
									notification.setMessage(body)
									notification.save(flush:true)
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
				}
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
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

	def getPastOrders = {
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def venueId,bartsyId,startDate,endDate
				def dateReceived
				def user,venue
				int index,noOfResults
				def pastOrders
				def pastOrdersList=[]
				def criteriaParams = [:]
				def openOrdersCriteria = Orders.createCriteria()
				if(json.has("date")){
					SimpleDateFormat fromUser = new SimpleDateFormat("MM/dd/yyyy");
					SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
					dateReceived = myFormat.format(fromUser.parse(json.date))
					def dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					startDate = dateTimeFormat.parse("${dateReceived} 00:00:00");
					endDate = dateTimeFormat.parse("${dateReceived} 23:59:59");
				}

				if(json.has("venueId")){
					venueId =  json.venueId
					venue = Venue.findByVenueId(json.venueId)
				}
				if(json.has("bartsyId")){
					bartsyId =  json.bartsyId
					user = UserProfile.findByBartsyId(json.bartsyId)
				}
				if(json.has("index")){
					index =  json.index
					params.offset = index
				}
				if(json.has("noOfResults")){
					noOfResults =  json.noOfResults
					params.max = noOfResults
				}
				criteriaParams.putAll(params)

				if(dateReceived && venueId && bartsyId){
					pastOrders = openOrdersCriteria.list(criteriaParams) {
						eq("venue",venue)
						between("dateCreated",startDate,endDate)
						or{
							eq("user",user)
							eq("receiverProfile",user)
						}
						order "id","desc"
					}
				}
				else if(dateReceived && venueId){
					pastOrders = openOrdersCriteria.list(criteriaParams) {
						eq("venue",venue)
						between("dateCreated",startDate,endDate)

						order "id","desc"
					}
				}
				else if(dateReceived && bartsyId){
					pastOrders = openOrdersCriteria.list(criteriaParams) {
						between("dateCreated",startDate,endDate)
						or{
							eq("user",user)
							eq("receiverProfile",user)
						}

						order "id","desc"
					}
				}
				else if(venueId && bartsyId){
					pastOrders = openOrdersCriteria.list(criteriaParams) {
						eq("venue",venue)
						or{
							eq("user",user)
							eq("receiverProfile",user)
						}

						order "id","desc"
					}
				}
				else if(venueId){
					pastOrders = openOrdersCriteria.list(criteriaParams) {
						eq("venue",venue)

						order "id","desc"
					}
				}
				else if(bartsyId){
					pastOrders = openOrdersCriteria.list(criteriaParams) {
						or{
							eq("user",user)
							eq("receiverProfile",user)
						}

						order "id","desc"
					}
				}
				else if(dateReceived){
					pastOrders = openOrdersCriteria.list(criteriaParams) {
						between("dateCreated",startDate,endDate)

						order "id","desc"
					}
				}
				else{
					pastOrders = openOrdersCriteria.list(criteriaParams){ order "id","desc" }
				}
				pastOrders.each{
					def order=it
					def pastOrdersMap = [:]
					if(order.getOrderStatus()&&order.getOrderStatus().toString().equalsIgnoreCase("10")){
						pastOrdersMap.put("orderId",order.getOrderId())
						pastOrdersMap.put("orderStatus",order.getOrderStatus())
						pastOrdersMap.put("venueId",order.venue.getVenueId())
						pastOrdersMap.put("authApproved",order.getAuthApproved())
						pastOrdersMap.put("authErrorMessage",order.getAuthErrorMessage())
						pastOrdersMap.put("venueName",order.venue.getVenueName())
						pastOrdersMap.put("totalPrice",order.getTotalPrice())
						pastOrdersMap.put("tipPercentage",order.getTipPercentage())
						pastOrdersMap.put("captureApproved",order.getCaptureApproved())
						pastOrdersMap.put("captureErrorMessage",order.getCaptureErrorMessage())
						pastOrdersMap.put("errorReason",order.getErrorReason())
						pastOrdersMap.put("basePrice",order.getBasePrice())
						pastOrdersMap.put("lastState",order.getLastState())
						pastOrdersMap.put("recipientBartsyId",order.receiverProfile.getBartsyId())
						pastOrdersMap.put("specialInstructions",order.getSpecialInstructions())
						pastOrdersMap.put("dateCreated",order.getLastUpdated())
						def itemsListStr
						if(order.itemsList){
							itemsListStr = new JSONArray(order.itemsList)
							pastOrdersMap.put("itemsList",itemsListStr)
						}else{
							pastOrdersMap.put("itemName",order.itemName)
							pastOrdersMap.put("itemId",order.itemId)
							pastOrdersMap.put("description",order.description)
						}
						pastOrdersMap.put("senderNickname",order.user.getNickName())
						pastOrdersMap.put("senderBartsyId",order.user.getBartsyId())
						pastOrdersMap.put("recipientNickname",order.receiverProfile.getNickName())
						pastOrdersMap.put("SenderImagePath",order.user.getUserImage())
						pastOrdersMap.put("recipientImagePath",order.receiverProfile.getUserImage())
						pastOrdersMap.put("currentTime",new Date().toGMTString())
						pastOrdersList.add(pastOrdersMap)
					}
				}
				response.put("errorCode",0)
				response.put("pastOrders",pastOrdersList)
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
		render(text:response as JSON,contentType:"application/json")
	}

	def reOrder={
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(json.apiVersion && apiVersion.value.toInteger() == Integer.parseInt(json.apiVersion.toString())){
				if(json.has("bartsyId") && json.has("venueId")){
					def venue = Venue.findByVenueId(json.venueId)
					if(venue){
						def user = UserProfile.findByBartsyId(json.bartsyId)
						def reciever = UserProfile.findByBartsyId(json.recieverBatsyId)
						if(user && reciever){
							if(json.has("orderId")){
								def order = Orders.findByOrderId(json.orderId)
								if(order){
									boolean validate=orderService.reOrder(order,venue,user)
									if(validate){
										def orderId = order.orderId
										def specialInstructions = order.specialInstructions
										def bartsyId = json.bartsyId
										def tipPercentage = json.tipPercentage
										def itemName=order.itemName
										def description=json.description
										def recieverBartsyId =json.recieverBatsyId
										def venueId=json.venueId
										def orderStatus="0"
										def basePrice=json.basePrice
										def totalPrice=json.totalPrice
										def itemsList=order.itemsList
										forward(controller:'order',action:'placeOrder',params:[orderId:orderId,bartsyId:bartsyId,specialInstructions:specialInstructions,apiVersion:json.apiVersion,tipPercentage:tipPercentage,itemName:itemName,description:description,recieverBartsyId:recieverBartsyId,venueId:venueId,orderStatus:orderStatus,basePrice:basePrice,totalPrice:totalPrice,itemsList:itemsList])
									}else{
										response.put("errorCode","10")
										response.put("errorMessage","Your order doesn't exists in this venue : "+venue.venueName)
									}
								}else{
									response.put("errorCode","5")
									response.put("errorMessage","Your order doesn't exists.")
								}
							}else{
								response.put("errorCode","4")
								response.put("errorMessage","OrderId is missing in your request. Please check for same.")
							}
						}else{
							response.put("errorCode","3")
							response.put("errorMessage","Sender User or Reciever User doesn't exists.")
						}
					}else{
						response.put("errorCode","2")
						response.put("errorMessage","Venue doesn't exists.")
					}

				}else{
					response.put("errorCode","1")
					response.put("errorMessage","VenueId or BartsyId is missing in your request. Please check for same.")
				}
			}else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}catch(Exception e){
			log.info("Exception in reOrder ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON,contentType:"application/json")
	}

}

package bartsy

import grails.converters.JSON
import java.text.SimpleDateFormat
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

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
			def apiVersion = BartsyConfiguration.findByConfigName(CommonConstants.API_VERSION)
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				Orders order = new Orders()
				Date orderDate = new Date()
				UserProfile userprofile = UserProfile.findByBartsyId(json.bartsyId)
				UserProfile	recieverUserprofile = UserProfile.findByBartsyId(json.receiverBartsyId)
				Venue venue = Venue.findByVenueId(json.venueId)
				if(userprofile && venue){
					if(venue.status.equals(CommonConstants.OPEN)){
						CommonMethods common = new CommonMethods()

						if(json.totalPrice && common.isInteger(json.totalPrice)){

							def maxId = Orders.createCriteria().get { projections { max "orderId" } } as Long
							if(maxId){
								maxId = maxId+1
							}else{
								maxId = 100001
							}
							if(json.itemsList){
								order.setItemsList(json.itemsList.toString())
							}

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
							order.setOrderStatus(OrderConstants.ORDER_STATUS_100)
							
							// Save order status date
							def dateOrderStatus
							dateOrderStatus = prepareOrderStatusDateResp(OrderConstants.ORDER_STATUS_100, order)
							order.setDateOrderStatus(dateOrderStatus)
							
							order.setAuthApproved(CommonConstants.FALSE)
							order.save(flush:true)
							
							if(order){
								if(json.itemsList){
									//json.itemsList.each{
										/*def itemInfo = it
										OrderItems orderItem = new OrderItems()
										orderItem.setVersion(1)
										orderItem.setItemName(itemInfo.itemName)
										orderItem.setItemId(itemInfo.itemId.toString())
										orderItem.setBasePrice(itemInfo.basePrice)
										orderItem.setDescription(itemInfo.description)
										orderItem.setOrder(order)
										orderItem.save(flush:true)
									}*/
									parseAndSavePlaceOrderItems(json.itemsList, order)

								}/*else{
									OrderItems orderItem = new OrderItems()
									orderItem.setItemName(json.itemName?json.itemName:"")
									orderItem.setItemId(json.itemId?json.itemId.toString():"")
									orderItem.setBasePrice(json.basePrice)
									orderItem.setDescription(json.description)
									orderItem.setOrder(order)
									orderItem.save(flush:true)
								}*/
							}
							Orders orderUpdate = Orders.findByOrderId(order.orderId)
							def authorizeResponse = paymentService.authorizePayment(userprofile,json.totalPrice,orderUpdate?.orderId)
							//order.setAuthTransactionId(authorizeResponse.transactionId as long)

							if(authorizeResponse.get(CommonConstants.AUTH_APPROVED).toBoolean()){
								
								if(!json.bartsyId.toString().equals(json.receiverBartsyId.toString())){
									orderUpdate.setOrderStatus(OrderConstants.ORDER_STATUS_OFFERED_DRINK)
									
									dateOrderStatus = prepareOrderStatusDateResp(OrderConstants.ORDER_STATUS_OFFERED_DRINK, orderUpdate)
								}else{
									orderUpdate.setOrderStatus(OrderConstants.ORDER_STATUS_NEW)
									
									dateOrderStatus = prepareOrderStatusDateResp(OrderConstants.ORDER_STATUS_NEW, orderUpdate)
								}
								orderUpdate.setDateOrderStatus(dateOrderStatus)
								orderUpdate.setAuthApproved(CommonConstants.TRUE)
								orderUpdate.setAuthCode(authorizeResponse.get(CommonConstants.AUTH_CODE))
								orderUpdate.setAuthTransactionNumber(authorizeResponse.get(CommonConstants.AUTH_TRANSACTION_NUMBER))
							}else{
								orderUpdate.setOrderStatus(OrderConstants.ORDER_STATUS_ORDER_TIMEOUT)
								
								dateOrderStatus = prepareOrderStatusDateResp(OrderConstants.ORDER_STATUS_ORDER_TIMEOUT, orderUpdate)
								orderUpdate.setDateOrderStatus(dateOrderStatus)
								
								orderUpdate.setAuthApproved(CommonConstants.FALSE)
								orderUpdate.setAuthErrorMessage(authorizeResponse.get(CommonConstants.AUTH_ERROR_MESSAGE))
								orderUpdate.setLastState(CommonConstants.LAST_STATE)
								orderUpdate.setErrorReason(CommonConstants.PAYMENT_AUTH_FAILED)

								orderUpdate.save(flush:true)
								response.put(CommonConstants.ERROR_CODE, CommonConstants.ERROR_CODE_FAILURE)
								response.put(CommonConstants.ERROR_MESSAGE, authorizeResponse.get(CommonConstants.AUTH_ERROR_MESSAGE))
								render(text:response as JSON,contentType:"application/json")
								return
							}

							if(orderUpdate.save(flush:true)){
								//create the place order notification
								def notification = new Notifications()
								notification.setUser(userprofile)
								notification.setVenue(venue)
								notification.setOrder(orderUpdate)
								notification.setType(CommonConstants.NOTIFICATION_TYPE_PLACE_ORDER)
								if(json.type && json.type.equals("custom")){
									addDrinkIngredients(json.ingredients,orderUpdate)
								}
								def openOrdersCriteria = Orders.createCriteria()
								def openOrders = openOrdersCriteria.list {
									eq("venue",venue)
									and{ eq("user",userprofile) }
									and{
										'in'("orderStatus",[
											OrderConstants.ORDER_STATUS_NEW,
											OrderConstants.ORDER_STATUS_REJECTED,
											OrderConstants.ORDER_STATUS_ACCEPTED,
											OrderConstants.ORDER_STATUS_COMPLETE,
											OrderConstants.ORDER_STATUS_FAILED,
											OrderConstants.ORDER_STATUS_PICKED_UP,
											OrderConstants.ORDER_STATUS_NOSHOW,
											OrderConstants.ORDER_STATUS_ORDER_TIMEOUT,
											OrderConstants.ORDER_STATUS_OFFERED_DRINK_REJECTION,
											OrderConstants.ORDER_STATUS_OFFERED_DRINK
										])
									}
								}
								Map pnMessage = new HashMap()
								if(!json.bartsyId.toString().equals(json.receiverBartsyId.toString())){
									pnMessage.put(OrderConstants.ORDER_STATUS, OrderConstants.ORDER_STATUS_OFFERED_DRINK)
								}else{
									pnMessage.put(OrderConstants.ORDER_STATUS, OrderConstants.ORDER_STATUS_NEW)
								}
								pnMessage.put(OrderConstants.ORDER_ID, orderUpdate?.orderId?.toString())
								pnMessage.put(ItemConstants.ITEM_NAME, json.itemName?json.itemName:"")
								pnMessage.put(OrderConstants.ORDER_TIME, orderDate.toGMTString())
								pnMessage.put(CommonConstants.BASE_PRICE, json.basePrice)
								pnMessage.put(CommonConstants.TIP_PERCENTAGE, json.tipPercentage)
								pnMessage.put(CommonConstants.TOTAL_PRICE, json.totalPrice)
								pnMessage.put(CommonConstants.DESCRIPTION, json.description)
								pnMessage.put(CommonConstants.UPDATE_TIME, orderDate.toGMTString())
								pnMessage.put(OrderConstants.ORDER_TIMEOUT, venue.cancelOrderTime)
								pnMessage.put(CommonConstants.SPECIAL_INSTRUCTIONS, json.specialInstructions ?: "")
								pnMessage.put(ItemConstants.ITEMS_LIST, json.itemsList?json.itemsList.toString():"")
								if(!json.bartsyId.toString().equals(json.receiverBartsyId.toString())){
									def name = json.itemName?json.itemName:""
									def body="You have been offered a drink "+name+" by "+orderUpdate.user.nickName
									pnMessage.put(CommonConstants.MESSAGE_TYPE, CommonConstants.DRINK_OFFERED)
									pnMessage.put(CommonConstants.BARTSY_ID, json.receiverBartsyId)
									pnMessage.put(CommonConstants.SENDER_BARTSY_ID, json.bartsyId)
									pnMessage.put(OrderConstants.ORDER_TIMEOUT, venue.cancelOrderTime)
									pnMessage.put(CommonConstants.BODY, body)
									response.put(OrderConstants.ORDER_COUNT, openOrders.size())
									response.put(OrderConstants.ORDER_ID, maxId)
									response.put(CommonConstants.ERROR_CODE, CommonConstants.ERROR_CODE_SUCCESS)
									response.put(CommonConstants.ERROR_MESSAGE, CommonConstants.DRINK_SENT)
									response.put(OrderConstants.ORDER_TIMEOUT, venue.cancelOrderTime)
									if(recieverUserprofile.deviceType == 1 ){
										try{
											pnMessage.put(CommonConstants.UN_READ_NOTIFICATIONS, common.getNotifictionCount(recieverUserprofile))
											applePNService.sendPN(pnMessage, recieverUserprofile.deviceToken, "1", body)
										}catch(Exception e){
											log.info("Exception "+e.getMessage())
										}
									}else{
										androidPNService.sendPN(pnMessage,recieverUserprofile.deviceToken)
									}
								}else{
									pnMessage.put(CommonConstants.BARTSY_ID, json.bartsyId)
									pnMessage.put(CommonConstants.MESSAGE_TYPE, CommonConstants.NOTIFICATION_TYPE_PLACE_ORDER)
									def map=[:]
									common.getUserOrderAndChekedInDetails(venue, orderUpdate.user, map)
									pnMessage.put(CommonConstants.CHECK_IN_AND_ORDER_DETAILS_OF_USER, map)
									response.put(OrderConstants.ORDER_COUNT, openOrders.size())
									response.put(OrderConstants.ORDER_ID, orderUpdate?.orderId)
									if(orderUpdate.user.emailVerified.toString().equalsIgnoreCase(CommonConstants.TRUE)){
										response.put(CommonConstants.ERROR_CODE, CommonConstants.ERROR_CODE_SUCCESS)
										response.put(CommonConstants.ERROR_MESSAGE, OrderConstants.ORDER_PLACED)
									}else{
										response.put(CommonConstants.ERROR_CODE, "99")
										response.put(CommonConstants.ERROR_MESSAGE, "Order Placed. Please verify your account to start collecting rewards")

									}
									response.put(OrderConstants.ORDER_STATUS, orderUpdate.getOrderStatus())
									response.put(OrderConstants.ORDER_TIMEOUT, venue.cancelOrderTime)
									androidPNService.sendPN(pnMessage, venue.deviceToken)
									//save the place order for self notification
									notification.setOrderType(OrderConstants.ORDER_TYPE_SELF)
									notification.setMessage("you ordered a drink "+ orderUpdate.getItemName())
									notification.save(flush:true)
								}
							}
							else{
								response.put(CommonConstants.ERROR_CODE, CommonConstants.ERROR_CODE_FAILURE)
								response.put(CommonConstants.ERROR_MESSAGE, "Order placing Failed")
							}
						}else{
							response.put(CommonConstants.ERROR_CODE, "2")
							response.put(CommonConstants.ERROR_MESSAGE, "Total price contains not a valid data")
						}
					}

					else{
						response.put(CommonConstants.ERROR_CODE, CommonConstants.ERROR_CODE_FAILURE)
						response.put(CommonConstants.ERROR_MESSAGE, "Venue is CLOSED or OFFLINE")
					}
				}
				else{
					response.put(CommonConstants.ERROR_CODE, CommonConstants.ERROR_CODE_FAILURE)
					response.put(CommonConstants.ERROR_MESSAGE, "Venue Id or User Id does not exists")
				}
			}
			else{
				response.put(CommonConstants.ERROR_CODE, "100")
				response.put(CommonConstants.ERROR_MESSAGE, "API version do not match")
			}
			response.put(CommonConstants.CURRENT_TIME, new Date().toGMTString())
		}
		catch(Exception e){
			log.info("Exception in place order ===> "+e.getMessage())
			println"Exception in place order "+e.getMessage()
			response.put(CommonConstants.ERROR_CODE, 200)
			response.put(CommonConstants.ERROR_MESSAGE, "Error occured while processing your request. Please verify json")
		}
		render(text:response as JSON,contentType:"application/json")
	}
	
	/**
	 * Method to parse 'placeOrder' input JSON and to save each item in 'order_items' table
	 * @param JSON's itemsList, Order object
	 */
	def parseAndSavePlaceOrderItems(def itemsList, Orders order){
		itemsList.each {
			def itemDetails = it
			
			String category, selectedItems
			def description
			
			def itemName = itemDetails.itemName
			def title = itemDetails.title
			def name = itemDetails.name
			def option_groups = itemDetails.option_groups
			if(option_groups && option_groups.size()>0){
				description = itemDetails.options_description
			}else{
				description = itemDetails.description
			}
			def type = itemDetails.type
			def order_price = itemDetails.order_price
			def basePrice = itemDetails.price
			def quantity = itemDetails.quantity

			if(option_groups && option_groups.size()>0){
				option_groups.each {
					def option = it
					def text = option.text
					if(text){
						def categoryObj = IngredientCategory.findByCategory(text.trim())
						if(category && !category.contains(categoryObj.id.toString())){
							category = category+","+categoryObj.id
						}else{
							category = categoryObj.id
						}
					}
					def options = option.options
					if(options && options.size()>0){
						options.each {
							def ingredient = it
							def selected = ingredient.selected
							if(selected){
								def ingredientName = ingredient.name
								if(selectedItems){
									selectedItems = selectedItems+","+ingredientName
								}else{
									selectedItems = ingredientName
								}
							}
						}
					}
				}
			}
			
			OrderItems orderItem = new OrderItems()
			orderItem.setVersion(1)
			orderItem.setItemName(itemName)
			orderItem.setTitle(title)
			orderItem.setBasePrice(basePrice)
			orderItem.setName(name)
			orderItem.setDescription(description)
			orderItem.setQuantity(quantity)
			orderItem.setType(type)
			orderItem.setCategorys(category)
			orderItem.setSelectedItems(selectedItems)
			orderItem.setOrder(order)
			orderItem.save(flush:true)
		}
	}
	
	/**
	 * Method to prepare 'dateOrderStatus' column value of 'Orders' table
	 * @param order
	 * @return JSON containing order status as key and date as value
	 */
	def prepareOrderStatusDateResp(def status, Orders order){
		def orderStatusJSON
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
		if (order.dateOrderStatus){
			orderStatusJSON = new JSONObject(order.dateOrderStatus)
			orderStatusJSON.put(status, dateFormat.format(new Date()))
		}else{
			orderStatusJSON = new JSONObject()
			orderStatusJSON.put(status, dateFormat.format(new Date()))
		}
		return orderStatusJSON.toString()
	}
	
	/**
	 * This is the webservice to be called to update the status of the order
	 * @author Swetha Bhatnagar
	 * @errorCodes 1 : failure, 0 : success
	 * @param orderId         		server generated id for order
	 * @param orderStatus  	 		status to be updated for the order
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
			def apiVersion = BartsyConfiguration.findByConfigName(CommonConstants.API_VERSION)
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
							
							// Save order status date
							def dateOrderStatus = prepareOrderStatusDateResp(orderObject.orderStatus.toString(), order)
							order.setDateOrderStatus(dateOrderStatus)
							
							if(order.save()){
								//create the place order notification
								def notification = new Notifications()
								notification.setUser(order.user)
								notification.setVenue(order.venue)
								notification.setOrder(order)
								notification.setType(CommonConstants.NOTIFICATION_TYPE_UPDATE_ORDER)
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
	 * This service is to Update status of the offered drink order
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
								
								// Save order status date
								def dateOrderStatus = prepareOrderStatusDateResp(OrderConstants.ORDER_STATUS_OFFERED_DRINK_REJECTION.toString(), order)
								order.setDateOrderStatus(dateOrderStatus)
								
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
								
								// Save order status date
								def dateOrderStatus = prepareOrderStatusDateResp(OrderConstants.ORDER_STATUS_NEW.toString(), order)
								order.setDateOrderStatus(dateOrderStatus)
								
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
										def receiverBartsyId =json.receiverBartsyId
										def venueId=json.venueId
										def orderStatus="0"
										def basePrice=json.basePrice
										def totalPrice=json.totalPrice
										def itemsList=order.itemsList
										forward(controller:'order',action:'placeOrder',params:[orderId:orderId,bartsyId:bartsyId,specialInstructions:specialInstructions,apiVersion:json.apiVersion,tipPercentage:tipPercentage,itemName:itemName,description:description,receiverBartsyId:receiverBartsyId,venueId:venueId,orderStatus:orderStatus,basePrice:basePrice,totalPrice:totalPrice,itemsList:itemsList])
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
	/**
	 * To get the recent orders of the user
	 */
	def getRecentOrders={
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(json.apiVersion && apiVersion.value.toString().equalsIgnoreCase(json.apiVersion.toString())){
				if(json.has("bartsyId") && json.has("venueId")){
					def venue = Venue.findByVenueId(json.venueId)
					if(venue){
						def user = UserProfile.findByBartsyId(json.bartsyId)
						if(user){
							response=orderService.getRecentOrders(user,venue)
						}else{
							response.put("errorCode","3")
							response.put("errorMessage","User doesn't exists.")
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
			log.info("Exception in getRecentOrders ===> "+e.getMessage())
			println"Exception in getRecentOrders ===>  "+e.getMessage()
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		response.put("currentTime",new Date().toGMTString())
		render(text:response as JSON,contentType:"application/json")
	}

}

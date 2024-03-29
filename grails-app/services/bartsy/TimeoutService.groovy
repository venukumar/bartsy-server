package bartsy

class TimeoutService {
	def androidPNService
	def applePNService
	def paymentService

	def timeout(){
		def timer = BartsyConfiguration.findByConfigName("timer")
		if(timer){
			if(timer.value.toBoolean()){
				orderTimeout()
				userTimeout()
				venueTimeout()
			}
		}
	}

	def orderTimeout(){
		def response = [:]
		log.warn("order timeout")
		def venueList = Venue.getAll()
		if(venueList){
			venueList.each{
				def venue = it
				def ordersCancelled = []
				def openOrdersCriteria = Orders.createCriteria()
				def openOrders = openOrdersCriteria.list {
					eq("venue",venue)
					and{
						'in'("orderStatus",["0", "2", "3", "9"])
					}
				}
				if(openOrders){
					openOrders.each{
						//def order = ""
						def order = it
						use(groovy.time.TimeCategory){
							def diff = new Date() - order.lastUpdated
							log.warn("difference in minutes"+diff.minutes)
							if(diff.minutes >= venue.cancelOrderTime){
								def orderStatus = order.orderStatus.toString()
								order.setLastState(orderStatus)
								order.setErrorReason("Order timeout")
								order.setOrderStatus("7")
								if(orderStatus.equals("3")){
									order = paymentService.makePayment(order)
									if(order.getCaptureApproved().toBoolean()){
										//Calculating reward points
										CommonMethods common = new CommonMethods()
										common.calculateRewardPoints(order)
									}
								}
								if(!order.save(flush:true)){
									println "order timeout error"
								}else{
								CommonMethods common = new CommonMethods()
									def pnMessage = [:]
									pnMessage.put("orderStatus","7")
									pnMessage.put("cancelledOrder",order.orderId)
									pnMessage.put("messageType","orderTimeout")
									ordersCancelled.add(order.orderId)

									if(order.user.deviceType == 0){
										androidPNService.sendPN(pnMessage,order.user.deviceToken)
									}
									else{
										pnMessage.put("unReadNotifications",common.getNotifictionCount(order.user))
										applePNService.sendPNOrderTimeout(pnMessage, order.user.deviceToken, "1","Your Order "+order.orderId+" has been cancelled due to timeout")
									}
									if(order.getDrinkOffered()){
										if(order.receiverProfile.deviceType == 0){
											androidPNService.sendPN(pnMessage,order.receiverProfile.deviceToken)
										}
										else{
											pnMessage.put("unReadNotifications",common.getNotifictionCount(order.user))
											applePNService.sendPNOrderTimeout(pnMessage, order.receiverProfile.deviceToken, "1","Your Order "+order.orderId+" has been cancelled due to timeout")
										}
									}
								}
							}
						}
					}
					if(ordersCancelled.size()){
						def pnMessage = [:]
						pnMessage.put("ordersCancelled",ordersCancelled)
						pnMessage.put("messageType","orderTimeout")
						androidPNService.sendPN(pnMessage,venue.deviceToken)
					}

				}

			}
		}
	}

	def heartBeat(){

		log.warn("heart beat")
		def timer = BartsyConfiguration.findByConfigName("heartbeat")
		if(timer){
			if(timer.value.toBoolean()){
				//println"heart beat"
				//heartBeatCustomer()
				//heartBeatVenue()
				toChangeOrdersToPastOrders()
			}
		}
	}
	/**
	 *  Check if an order is in idle state for more than 15 min we changing order status to 10.
	 * @return
	 */
	def toChangeOrdersToPastOrders(){
		//println"change orders to past orders "+new Date()
		//println "Date "+new Date().getDateString()
		//println "Time "+new Date().getTimeString()
		//println "Day "+new Date().getDateTimeString()
		log.info("change orders to past orders")
		def openOrdersCriteria = Orders.createCriteria()
		def openOrders = openOrdersCriteria.list {
			'in'("orderStatus",["1", "4", "5", "6", "7", "8"])
		}
		println "openOrders "+openOrders.size()
		if(openOrders){
			openOrders.each{
				//def order = ""
				def order = it
				use(groovy.time.TimeCategory){
					def diff = new Date() - order.lastUpdated
					log.warn("difference in minutes"+diff.minutes)
					def orderStatus = order.orderStatus.toString()
					if(diff.minutes >= 15){
						order.setLastState(orderStatus)
						order.setErrorReason("Past order")
						order.setOrderStatus("10")
						if(!order.save(flush:true)){
							log.info("order moved to past order")
						}else{
							log.info("order not moved to past order")

						}
					}
				}
			}
		}
	}

	def userTimeout(){

		log.warn("userTimeout")
		def venueList = Venue.getAll()
		if(venueList){
			venueList.each{
				def venue = it
				def usersCheckedOut = []
				def ordersCancelled = []
				def userList = CheckedInUsers.findAllByVenueAndStatus(venue,1)
				if(userList){
					userList.each{
						def user = it
						use(groovy.time.TimeCategory){
							if(user.lastHBResponse){
								def diff = new Date() - user.lastHBResponse
								//log.warn("difference in minutes"+diff.minutes)
								def userTimeout = BartsyConfiguration.findByConfigName("userTimeout")
								if(diff.minutes >= (userTimeout.value.toInteger())){
									log.warn("Check out the user")
									user.setStatus(0)
									if(!user.save(flush:true)){
										println "User timeout save error"
									}else{
										usersCheckedOut.add(user.userProfile.bartsyId)
									}
									//									def openOrdersCriteria = Orders.createCriteria()
									//									def openOrders = openOrdersCriteria.list {
									//										eq("user",user.userProfile)
									//										and{
									//											'in'("orderStatus",["0", "2", "3"])
									//										}
									//									}
									//									if(openOrders){
									//										openOrders.each{
									//											def order = it
									//											order.setOrderStatus("7")
									//											if(order.save()){
									//												ordersCancelled.add(order.orderId)
									//											}
									//										}
									//									}
									//									if(user.userProfile.deviceType == 0){
									//										def pnMessage = [:]
									//										pnMessage.put("messageType","userTimeout")
									//										pnMessage.put("bartsyId",user.userProfile.bartsyId)
									//										androidPNService.sendPN(pnMessage,user.userProfile.deviceToken)
									//									}
									//									else{
									//										def pnMessage = [:]
									//										pnMessage.put("messageType","userTimeout")
									//										pnMessage.put("bartsyId",user.userProfile.bartsyId)
									//										applePNService.sendPNUserTimeout(pnMessage,user.userProfile.deviceToken,"1", "You were checked out from "+venue.venueName+"due to timeout")
									//									}
								}
							}
						}
					}
					if(usersCheckedOut.size()){
						def pnMessage = [:]
						//pnMessage.put("ordersCancelled",ordersCancelled)
						pnMessage.put("usersCheckedOut",usersCheckedOut)
						pnMessage.put("messageType","userTimeout")
						androidPNService.sendPN(pnMessage,venue.deviceToken)
					}
				}
			}
		}
	}

	def heartBeatCustomer(){
		def venueList = Venue.getAll()
		if(venueList){
			venueList.each{
				def venue = it
				def checkedInUsersList = []
				def userList = CheckedInUsers.findAllByVenueAndStatus(venue,1)
				if(userList){
					userList.each{
						def user = it
						checkedInUsersList.add(user.userProfile.bartsyId)
					}
					userList.each{
						def user = it
						def ordersList = []
						def openOrdersCriteria = Orders.createCriteria()
						def openOrders = openOrdersCriteria.list {
							eq("user",user)
							and{ eq("venue",venue) }
							and{
								'in'("orderStatus",["0", "2", "3", "9"])
							}
						}
						if(openOrders){
							openOrders.each{
								def order=it
								ordersList.add(order.orderId)
							}
						}
						if(user.userProfile.deviceType == 0){
							def pnMessage = [:]
							pnMessage.put("bartsyId",user.userProfile.bartsyId)
							pnMessage.put("venueId",venue.venueId)
							pnMessage.put("venueName",venue.venueName)
							pnMessage.put("messageType","heartBeat")
							pnMessage.put("userCount",checkedInUsersList.size())
							pnMessage.put("openOrders",ordersList)
							pnMessage.put("orderCount",ordersList.size())
							pnMessage.put("checkedInUsersList",checkedInUsersList)
							androidPNService.sendPN(pnMessage,user.userProfile.deviceToken)
						}
						else{
							def pnMessage = [:]
							pnMessage.put("bartsyId",user.userProfile.bartsyId)
							pnMessage.put("venueId",venue.venueId)
							pnMessage.put("venueName",venue.venueName)
							pnMessage.put("messageType","heartBeat")
							pnMessage.put("userCount",checkedInUsersList.size())
							pnMessage.put("openOrders",ordersList)
							pnMessage.put("orderCount",ordersList.size())
							pnMessage.put("checkedInUsersList",checkedInUsersList)
							applePNService.sendPNHeartBeat(pnMessage,user.userProfile.deviceToken, "1", "")
						}
					}
				}
			}
		}
	}

	def heartBeatVenue(){
		def venueList = Venue.findAllByStatusNotEqual("CLOSED")
		if(venueList){
			venueList.each{
				def venue = it
				def checkedInUsersList = []
				def ordersList = []
				def userList = CheckedInUsers.findAllByVenueAndStatus(venue,1)
				if(userList){
					userList.each{
						def user = it
						checkedInUsersList.add(user.userProfile.bartsyId)
					}
				}
				def openOrdersCriteria = Orders.createCriteria()
				def openOrders = openOrdersCriteria.list {
					eq("venue",venue)
					and{
						'in'("orderStatus",["0", "2", "3", "9"])
					}
				}
				if(openOrders){
					openOrders.each{
						def order=it
						ordersList.add(order.orderId)
					}
				}
				def pnMessage = [:]
				pnMessage.put("messageType","heartBeat")
				pnMessage.put("checkedInUsersList",checkedInUsersList)
				pnMessage.put("ordersList",ordersList)
				println "Sending venueHeartBeat"
				log.warn("Sending venueHeartBeat")
				androidPNService.sendPN(pnMessage,venue.deviceToken)
			}
		}
	}

	def venueTimeout(){
		def usersCheckedOut = []
		log.warn("Venue Timeout")
		def venueCriteria = Venue.createCriteria()
		def venueList = venueCriteria.list {
			'in'("status",["OPEN", "IDLE"])
		}

		//def venueList = Venue.findAllByStatus("OPEN")
		if(venueList){
			venueList.each{
				def venue = it
				use(groovy.time.TimeCategory){
					if(venue.lastHBResponse){
						def diff = new Date() - venue.lastHBResponse
						//log.warn("difference in minutes"+diff.minutes)
						def venueTimeout = BartsyConfiguration.findByConfigName("venueTimeout")
						if(diff.minutes >= 3){
							if(!venue.status.equals("OFFLINE")){
								log.warn("Alert the venue")
								sendMailTemplate("srikanth.asila@techva.com","The internet connection of your bartender tablet seems to be down. Please check the same.","Bartsy WIFI Alert")
							}
						}
						if(diff.minutes >= (venueTimeout.value.toInteger())){
							log.warn("Move venue to OFFLINE state")
							venue.setStatus("OFFLINE")
							venue.save(flush:true)
						}
					}
				}
			}
		}
	}

	// Alert for bartender when venue. If venue is offline
	def sendMailTemplate(String emailId,String message,String subjectSent){

		println "mailID" +emailId
		println "message"+message
		/*sendMail {
		 to emailId
		 subject subjectSent
		 body message
		 }*/
	}
}

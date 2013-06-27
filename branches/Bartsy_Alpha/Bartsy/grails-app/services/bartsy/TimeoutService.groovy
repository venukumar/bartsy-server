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
						'in'("orderStatus",["0", "2", "3"])
					}
				}
				if(openOrders){
					openOrders.each{
						def order=it
						use(groovy.time.TimeCategory){
							def diff = new Date() - order.lastUpdated
							//log.warn("difference in minutes"+diff.minutes)
							if(diff.minutes >= venue.cancelOrderTime){
								def orderStatus = order.orderStatus.toString()							
								order.setLastState(orderStatus)
								order.setErrorReason("Order timeout")
								order.setOrderStatus("7")
								if(orderStatus.equals("3")){
								order = paymentService.makePayment(order)
								}
								if(!order.save(flush:true)){
									println "order timeout error"
								}else{								
									if(order.user.deviceType == 0){
										def pnMessage = [:]
										pnMessage.put("orderStatus","7")
										pnMessage.put("cancelledOrder",order.orderId)
										pnMessage.put("messageType","orderTimeout")
										androidPNService.sendPN(pnMessage,order.user.deviceToken)
										ordersCancelled.add(order.orderId)
									}
									else{
										def pnMessage = [:]
										pnMessage.put("orderStatus","7")
										pnMessage.put("cancelledOrder",order.orderId)
										pnMessage.put("messageType","orderTimeout")
										applePNService.sendPNOrderTimeout(pnMessage, order.user.deviceToken, "1","Your Order "+order.orderId+" has been cancelled due to timeout")
										ordersCancelled.add(order.orderId)
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
				heartBeatCustomer()
				heartBeatVenue()
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
							and{
								eq("venue",venue)
							}
							and{
								'in'("orderStatus",["0", "2", "3"])
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
						'in'("orderStatus",["0", "2", "3"])
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
		log.warn("userTimeout")
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
								sendMailTemplate("srikanth.talasila@techvedika.com","The internet connection of your bartender tablet seems to be down. Please check the same.","Bartsy WIFI Alert")
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
	
	// Alert for bartender when venue is offline
	def sendMailTemplate(String emailId,String message,String subjectSent){
		
		println "mailID" +emailId
		println "message"+message
		println "forget password !!!!!!!!!!! "
		sendMail {
			to emailId
			subject subjectSent
			body message
		}
	}
}

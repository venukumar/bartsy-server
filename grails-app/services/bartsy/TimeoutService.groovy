package bartsy

class TimeoutService {
	def androidPNService
	def applePNService

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
		def ordersCancelled = []
		log.warn("order timeout")
		def venueList = Venue.getAll()
		if(venueList){
			venueList.each{
				def venue = it
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
								order.setOrderStatus("7")
								if(order.save()){
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
		def usersCheckedOut = []
		def ordersCancelled = []
		log.warn("userTimeout")
		def venueList = Venue.getAll()
		if(venueList){
			venueList.each{
				def venue = it
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
									user.save(flush:true)
									usersCheckedOut.add(user.userProfile.bartsyId)
									def openOrdersCriteria = Orders.createCriteria()
									def openOrders = openOrdersCriteria.list {
										eq("user",user.userProfile)
										and{
											'in'("orderStatus",["0", "2", "3"])
										}
									}
									if(openOrders){
										openOrders.each{
											def order = it
											order.setOrderStatus("7")
											if(order.save()){
												ordersCancelled.add(order.orderId)
											}
										}
									}
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
						pnMessage.put("ordersCancelled",ordersCancelled)
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
							pnMessage.put("messageType","heartBeat")
							pnMessage.put("checkedInUsersList",checkedInUsersList)
							pnMessage.put("ordersList",ordersList)
							androidPNService.sendPN(pnMessage,user.userProfile.deviceToken)
						}
						else{
							def pnMessage = [:]
							pnMessage.put("messageType","heartBeat")
							pnMessage.put("checkedInUsersList",checkedInUsersList)
							pnMessage.put("ordersList",ordersList)
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
				androidPNService.sendPN(pnMessage,venue.deviceToken)
			}
		}
	}

	def venueTimeout(){
		def usersCheckedOut = []
		log.warn("userTimeout")
		def venueList = Venue.findAllByStatus("OPEN")
		if(venueList){
			venueList.each{
				def venue = it
				use(groovy.time.TimeCategory){
					if(venue.lastHBResponse){
						def diff = new Date() - venue.lastHBResponse
						//log.warn("difference in minutes"+diff.minutes)
						def venueTimeout = BartsyConfiguration.findByConfigName("venueTimeout")
						if(diff.minutes >= (venueTimeout.value.toInteger())){
							log.warn("Move venue to IDLE state")
							venue.setStatus("IDLE")
							venue.save(flush:true)
						}
					}
				}
			}
		}
	}
}

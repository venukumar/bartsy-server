package bartsy

import org.codehaus.groovy.grails.web.json.JSONArray
import org.hsqldb.util.CSVWriter

class AdminController {

	def index() {
		println"index"
		if(session.user){
			forward(action:"ordersList")
		}
	}

	def adminLogin(){
		try{
			println "admin login "+params
			if(params){
				println"params "+params.username
				println "pass "+params.password
				def adminInstance = AdminUser.findByUsernameAndPassword(params.username, params.password)
				println"adminInstance 11111111"
				println"adminInstance "+adminInstance
				if(adminInstance)
					println "adminInstance ok"
				else
					println "adminInstance not ok"
				if(!adminInstance){
					flash.errors = message(code:"default.admin.not.exists", default:"Invalid Username/Password")
					render(view:"index", model: [adminUserInstance:params])
					return
				}
				println"admin login"
				session.user = adminInstance
				forward(action:"ordersList")
			}else{
				println"params else"
			}
		}catch(Exception e){
			log.error("Exception found in admin login =====>"+ e.getMessage())
		}
	}

	def venueList(){
		try{
			def venuelist = Venue.list()
			def venuelistTotal = Venue.count()

			[venueList:venuelist, venueTotal:venuelistTotal]
		}catch(Exception e){
			log.error("Error in Venue List ==>"+e.getMessage())
		}
	}
	def orderDetails(){
		try{
			if(params){
				def order = Orders.findByOrderId(params.id)
				def orderStatusArr = []
				orderStatusArr.putAt(0,"New")
				orderStatusArr.putAt(1,"Rejected")
				orderStatusArr.putAt(2,"Accepted")
				orderStatusArr.putAt(3,"Complete")
				orderStatusArr.putAt(4,"Failed")
				orderStatusArr.putAt(5,"Picked up")
				orderStatusArr.putAt(6,"Noshow")
				orderStatusArr.putAt(7,"OrderTimeout") // or AuthApproved is false
				orderStatusArr.putAt(8,"Offered drink rejection")
				orderStatusArr.putAt(9,"Offered drink new")
				orderStatusArr.putAt(10,"Past order")

				int lastOrderState
				int orderStatusNumber = Integer.parseInt(order.orderStatus)
				def lastState = order.lastState
				if(lastState)
					lastOrderState = Integer.parseInt(lastState)
				else
					lastOrderState = 0

				def orderStatus = orderStatusArr[orderStatusNumber]
				def orderLastState = orderStatusArr[lastOrderState]
				
				def itemName = order.itemName
				def itemsList = order.itemsList
				if(itemsList){
					def listOfItems = new JSONArray(itemsList)
					listOfItems.each{
						def item = it
						if(itemName){
							itemName=itemName+","+item.itemName
						}else{
							itemName=item.itemName
						}
					}
				}

				render(view:"orderDetails",model:[selectedOrder:order,orderStatus:orderStatus,orderLastState:orderLastState,itemName:itemName])

			}
		}catch(Exception e){
			log.error("Error in order details ==>"+e.getMessage())
		}
	}

	def usersList(){
		params.max = Math.min(params.max ? params.int('max') : 50, 100)
		try{
			def userlist = UserProfile.createCriteria().list(params){ order "id", "desc" }
			def userlistTotal = UserProfile.count()
			[usersList:userlist, usersTotal:userlistTotal]
		}catch(Exception e){
			log.error("Error in Users List ==>"+e.getMessage())
		}
	}

	def ordersList(){
		params.max = Math.min(params.max ? params.int('max') : 50, 100)
		try{
			def orderslist = Orders.createCriteria().list(params){ order "id", "desc" }
			def orderlistTotal = Orders.count()
			def orders=[:]
			if(orderslist){
				orderslist.each {
					def order=it
					def key = order.orderId
					def itemName = order.itemName
					def itemsList = order.itemsList
					if(itemsList){
						def listOfItems = new JSONArray(itemsList)
						listOfItems.each{
							def item = it
							if(itemName){
								itemName=itemName+","+item.itemName
							}else{
								itemName=item.itemName
							}
						}
					}


					orders.put(key,itemName)
				}
			}
			println"orders "+orders
			[ordersList:orderslist, ordersTotal:orderlistTotal, itemsNames:orders]
		}catch(Exception e){
			log.error("Error in Orders List ==>"+e.getMessage())
		}
	}

	def downloadCSV(){
		try{
			def orderslist = Orders.createCriteria().list(params){ order "id", "desc" }
			if(orderslist){
				response.setHeader("Content-disposition",
						"attachment; filename=ordersList.csv")
				response.contentType = "text/csv"

				def outs = response.outputStream
				def orderStatusArr = []
				orderStatusArr.putAt(0,"New")
				orderStatusArr.putAt(1,"Rejected")
				orderStatusArr.putAt(2,"Accepted")
				orderStatusArr.putAt(3,"Complete")
				orderStatusArr.putAt(4,"Failed")
				orderStatusArr.putAt(5,"Picked up")
				orderStatusArr.putAt(6,"Noshow")
				orderStatusArr.putAt(7,"OrderTimeout") // or AuthApproved is false
				orderStatusArr.putAt(8,"Offered drink rejection")
				orderStatusArr.putAt(9,"Offered drink new")
				orderStatusArr.putAt(10,"Past order")
				def cols = [:]
				outs << "Name;Nick Name;Bartsy Id;Venue Id;Venue Name;Order Id;Item Id;Item Name;Drink Offered;Receiver Bartsy Id;Receiver Name;Date Created;Last Updated;Order Status;Last State;Base Price;Tip Percentage;Total Price;Auth Approved;Auth Code;Auth Error Message;Auth Transaction Number;Capture Approved;Capture Error Message;Capture Transaction Number;Error Reason;Special Instructions;\n"
				orderslist.each{
					def order = it
					int lastOrderState
					int orderStatus = Integer.parseInt(order.orderStatus)
					def lastState = order.lastState
					if(lastState)
						lastOrderState = Integer.parseInt(lastState)
					else
						lastOrderState = 0
					outs << order.user.name+";"
					outs << order.user.nickName+";"
					outs << order.user.bartsyId+";"
					outs << order.venue.venueId+";"
					outs << order.venue.venueName+";"
					outs << order.orderId+";"
					outs << order.itemId+";"
					outs << order.itemName+";"
					outs << order.drinkOffered.toString()+";"
					outs << order.receiverProfile.bartsyId+";"
					outs << order.receiverProfile.nickName+";"
					outs << order.dateCreated.format('yyyy-MM-dd hh:mm:ss')+";"
					outs << order.lastUpdated.format('yyyy-MM-dd hh:mm:ss')+";"
					outs << orderStatusArr[orderStatus]+";"
					outs << orderStatusArr[lastOrderState]+";"
					outs << order.basePrice+";"
					outs << order.tipPercentage+";"
					outs << order.totalPrice+";"
					outs << order.authApproved+";"
					outs << order.authCode+";"
					outs << order.authErrorMessage+";"
					outs << order.authTransactionNumber+";"
					outs << order.captureApproved+";"
					outs << order.captureErrorMessage+";"
					outs << order.captureTransactionNumber+";"
					outs << order.errorReason+";"
					outs << order.specialInstructions+";"
					outs << "\n"
				}
				outs.flush()
				outs.close()
			}

		}catch(Exception e){
			log.error("Error in Orders CSV List ==>"+e.getMessage())
		}
	}

	def appSettings(){
		if(params.userTimeout){
			int err = 0
			if(params.timer){
				def timerreq = BartsyConfiguration.findByConfigName("timer")
				timerreq.value = params.timer
				if(!timerreq.save(flush:true)){
					log.error("Timer setting update failed ==>"+timerreq.errors)
					err++
				}
			}
			if(params.userTimeout){
				def userTime = BartsyConfiguration.findByConfigName("userTimeout")
				userTime.value = params.userTimeout
				if(!userTime.save(flush:true)){
					log.error("User Timeout setting update failed ==>"+userTime.errors)
					err++
				}
			}
			if(params.venueTimeout){
				def venueTime = BartsyConfiguration.findByConfigName("venueTimeout")
				venueTime.value = params.venueTimeout
				if(!venueTime.save(flush:true)){
					log.error("Venue Timeout setting update failed ==>"+venueTime.errors)
					err++
				}
			}

			if(params.payment){
				def paymentreq = BartsyConfiguration.findByConfigName("payment")
				paymentreq.value = params.payment
				if(!paymentreq.save(flush:true)){
					log.error("payment setting update failed ==>"+paymentreq.errors)
					err++
				}
			}

			if(params.authId){
				def authIdreq = BartsyConfiguration.findByConfigName("authId")
				authIdreq.value = params.authId
				if(!authIdreq.save(flush:true)){
					log.error("authId update failed ==>"+authIdreq.errors)
					err++
				}
			}

			if(params.authPwd){
				def authPwdreq = BartsyConfiguration.findByConfigName("authPassword")
				authPwdreq.value = params.authPwd
				if(!authPwdreq.save(flush:true)){
					log.error("authPwd update failed ==>"+authPwdreq.errors)
					err++
				}
			}

			if(err > 0){
				flash.errors = "App Settings saving failed"
			}else{
				flash.message = "App Settings updated successfully"

			}
		}
	}

	def saleuserList(){
		try{
			def salesList = AdminUser.findAllByUserTypeInList(["SalesUser", "SalesManager"])
			def salesCnt = salesList.size()
			[salesList:salesList, salesCnt:salesCnt]
		}catch(Exception e){
			log.error("Error in sales user list"+e.getMessage())
		}
	}

	def createSaleUser(){
		try{
			if(params.act){
				def saleInfo = AdminUser.findAllByUsernameAndUserType(params.username,params.userType)
				if(!saleInfo){
					def saleAcc = new AdminUser()
					saleAcc.setFirstName(params.firstName)
					saleAcc.setLastName(params.lastName)
					saleAcc.setEmail(params.email)
					saleAcc.setUsername(params.username)
					saleAcc.setPassword(params.password)
					saleAcc.setUserType(params.userType)
					def promoCode = generatePromoCode()
					println "promoCode "+promoCode
					saleAcc.setPromoterCode(promoCode)
					saleAcc.save(flush:true)
					flash.message =  "Sales Account created successfully"
					forward(action:"saleuserList")
				}else{
					flash.error = "Sales Account already exists with the same Username"
					[saleInstance:params]
				}

			}
			[saleInstance:params]

		}catch(Exception e){
			log.error("Exception in create Sale User ==>"+e.getMessage())
		}
	}

	def editSaleUser(){
		try{
			def saleInfo = AdminUser.get(params.id)
			if(saleInfo){
				if(params.act){
					saleInfo.setFirstName(params.firstName)
					saleInfo.setLastName(params.lastName)
					saleInfo.setEmail(params.email)
					saleInfo.setPassword(params.password)
					saleInfo.setUserType(params.userType)
					if(!saleInfo.save(flush:true)){
						flash.error =  "Sales Account updation failed"
						[saleInstance:params]
					}else{
						flash.message =  "Sales Account updated successfully"
						forward(action:"saleuserList")
					}
				}
				[saleInstance:saleInfo]
			}else{
				flash.error = "Sale Account doesn't exist"
				forward(action:"saleuserList")
			}
		}catch(Exception e){
			log.error("Exception in create Sale User ==>"+e.getMessage())
		}
	}

	def deleteSaleUser(){
		try{
			def saleInfo = AdminUser.get(params.id)
			if(saleInfo){
				saleInfo.delete(flush:true)
				flash.message =  "Sales Account deleted successfully"
				forward(action:"saleuserList")
			}else{
				flash.error = "Sale Account doesn't exist"
				forward(action:"saleuserList")
			}
		}catch(Exception e){
			log.error("Exception in create Sale User ==>"+e.getMessage())
		}
	}
	def venueConfig(){
		try{
			if(params){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				def configList= VenueConfig.findAllByVenue(venue)
				def configListSize = configList.size()
				println "configListSize "+configListSize
				if(venue){
					[venue:venue,venueList:configList,configListSize:configListSize]

				}else{
					flash.message = "Venue Not Found"
				}
			}

		}catch(Exception e){
			log.error("Exception in create Sale User ==>"+e.getMessage())
		}
	}
	def saveVenueConfig(){
		try{
			println"saveVenueConfig saveVenueConfig"
			println"params "+params
			def rewardPoints
			def description
			def type
			def value
			def venueId
			if(params.rewardPoints){
				rewardPoints=params.rewardPoints}
			if(params.description){
				description=params.description}
			if(params.type){
				type=params.type}
			if(params.value){
				value=params.value
			}
			if(params.venue){
				venueId=params.venue}
			def venue = Venue.findByVenueId(venueId)
			println "venue "+venue
			if(rewardPoints && description && type && venue){
				VenueConfig configVenue = new VenueConfig()
				configVenue.setRewardPoints(Integer.parseInt(rewardPoints))
				configVenue.setDescription(description)
				configVenue.setValue(value)
				configVenue.setType(type)
				configVenue.setVenue(venue)
				if(!configVenue.save(flush:true)){
					flash.message = "Please try again"
				}
			}else{
				println "else "
				flash.message = "Please fill all the fields"
			}
			redirect(action:"venueConfig",id:venue.venueId)
		}catch(Exception e){

			log.error("Exception found "+e.getMessage())

		}
	}
	def generatePromoCode(){
		CommonMethods common = new CommonMethods()
		String promoCode = common.promoCode(8)

		def user = AdminUser.findAllByPromoterCode(promoCode)
		if(user.size()>0){
			generatePromoCode()
		}
		else{
			return promoCode
		}
	}

	def logout(){
		if(session.user){
			session.user = null
		}
		forward(action:"index")
	}
}

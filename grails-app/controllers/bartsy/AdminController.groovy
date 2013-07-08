package bartsy

import org.hsqldb.util.CSVWriter

class AdminController {

	def index() {
		if(session.user){
			forward(action:"ordersList")
		}
	}

	def adminLogin(){
		try{
			if(params){
				def adminInstance = AdminUser.findByUsernameAndPassword(params.username, params.password)
				if(!adminInstance){
					flash.errors = message(code:"default.admin.not.exists", default:"Invalid Username/Password")
					render(view:"index", model: [adminUserInstance:params])
					return
				}

				session.user = adminInstance
				forward(action:"ordersList")
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

				render(view:"orderDetails",model:[selectedOrder:order,orderStatus:orderStatus,orderLastState:orderLastState])
				
			}
		}catch(Exception e){
			log.error("Error in order details ==>"+e.getMessage())
		}
	}

	def usersList(){
		try{
			def userlist = UserProfile.list()
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

			[ordersList:orderslist, ordersTotal:orderlistTotal]
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
			if(err > 0){
				flash.errors = "App Settings saving failed"
			}else{
				flash.message = "App Settings updated successfully"

			}
		}
	}

	def saleuserList(){
		try{
			def salesList = AdminUser.findAllByUserType("SalesUser")
			def salesCnt = salesList.size()
			[salesList:salesList, salesCnt:salesCnt]
		}catch(Exception e){
			log.error("Error in sales user list"+e.getMessage())
		}
	}

	def createServerKeys(){
		String privateKeyFile = "bartsy_privateKey.pem"
		String publicKeyFile = "bartsy_publicKey.pem"
		String csrFile = "bartsy_office.csr"
		String certFile = "bartsy_office.crt"
		String caCert = "bartsy_ca.crt"
		String caPrivKey = "bartsy_CAkey.pem"
		String cryptoPath = message(code:'userimage.path')
		CryptoUtil.createCAKeys(cryptoPath+caPrivKey,cryptoPath+caCert)
		CryptoUtil.createRSAKeys(cryptoPath+privateKeyFile,cryptoPath+publicKeyFile)
		String subj1 = "/C=US/ST=Florida/L=West Palm Beach/O=Bartsy Owner LLC/OU=Support/CN=Bartsy/emailAddress=info@bartsy.com"
		CryptoUtil.createCSR(cryptoPath+privateKeyFile,cryptoPath+csrFile,subj1)
		CryptoUtil.createUserSignedCert(cryptoPath+csrFile,cryptoPath+certFile,cryptoPath+caCert,cryptoPath+caPrivKey)
	}

	def logout(){
		if(session.user){
			session.user = null
		}
		forward(action:"index")
	}
}

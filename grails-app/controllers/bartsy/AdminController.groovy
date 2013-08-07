package bartsy

import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import org.codehaus.groovy.grails.web.json.JSONArray
import org.hsqldb.util.CSVWriter

class AdminController {
	def exportService
	def grailsApplication  //inject GrailsApplication
	
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
	
	/**
	 * Service to retrieve venue details
	 * Like venue Id, name, checkIns, orders, checkIns/orders of last 30 days
	 * And venue configuration, edit and delete options
	 * @return venue Id, name, checkIns, orders, checkIns/orders of last 30 days
	 */
	def venueList(){
		try{
			def currentDate = new Date()
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
			def  sDate = dateFormat.format(currentDate - 30)
			def  eDate = dateFormat.format(currentDate)
			Date startDate = dateFormat.parse(sDate)
			Date endDate = dateFormat.parse(eDate)
			
			def checkInsList, ordersList, checkInsLast30DaysList, ordersLast30DaysList
			def checkInsMap = [:], ordersMap = [:], checkInsLast30DaysMap = [:], ordersLast30DaysMap = [:]
			def venuelist = Venue.list()
			def venuelistTotal = Venue.count()
			
			venuelist.each {
				def venueObj = it
				// Total checkins
				checkInsList = UserCheckInDetails.createCriteria().list {
					eq("venue", venueObj)
				}
				checkInsMap.put(venueObj.id, checkInsList.size())
				
				// Total orders
				ordersList = Orders.createCriteria().list {
					eq("venue", venueObj)
				}
				ordersMap.put(venueObj.id, ordersList.size())
				
				// Total checkins in last 30 days
				checkInsLast30DaysList = UserCheckInDetails.createCriteria().list {
					eq("venue", venueObj)
					and{
						between("checkedInDate", startDate, endDate)
					}
				}
				checkInsLast30DaysMap.put(venueObj.id, checkInsLast30DaysList.size())
				
				// Total orders in last 30 days
				ordersLast30DaysList = Orders.createCriteria().list {
					eq("venue", venueObj)
					and{
						between("dateCreated", startDate, endDate)
					}
				}
				ordersLast30DaysMap.put(venueObj.id, ordersLast30DaysList.size())
			}
				
			[venueList:venuelist, venueTotal:venuelistTotal, checkIns:checkInsMap, orders:ordersMap, checkInsLast30Days:checkInsLast30DaysMap, , ordersLast30Days:ordersLast30DaysMap]
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
	
	/**
	 * Service to retrieve orders details
	 * Like order Id, item name, sender, recipient, gross, tax, comp, %, net and totals of all. 
	 * @return each item name, sender, recipient, etc.. 
	 */
	def ordersList(){
		params.max = Math.min(params.max ? params.int('max') : 50, 100)
		try{
			Date toDate = new Date()
			DateFormat jQdateFormat = new SimpleDateFormat("MM/dd/yyyy");
			DateFormat dateFormatn = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date date = new java.util.Date();
			java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd hh")
			def tradeTime = BartsyConfiguration.findByConfigName("tradingDay")
			Date  startDate, endDate
			if(params.startDate)
				startDate = dateFormat.parse(dateFormatn.format(new Date(params.startDate))+" "+tradeTime.value)
			else startDate = dateFormat.parse(dateFormatn.format(date)+" "+tradeTime.value)
			if(params.endDate)
				endDate = dateFormat.parse(dateFormatn.format(new Date(params.endDate))+" "+tradeTime.value)
			else
				endDate = new Date(startDate.getTime() + (1000 * 60 * 60 * 24));
			def jqStart = jQdateFormat.format(startDate)
			def jqEnd = jQdateFormat.format(endDate)
			def query = {
				between("dateCreated", startDate, endDate)
				
				order "id", "desc"
			}	
			println "start date "+startDate+" "+endDate		
			def orders=[:], itemsGross = [:], orderTax = [:], tipPercentage = [:], comp = [:], net = [:] 
			def totalGuests = [:], grossTotal = [:], taxTotal = [:], compTotal = [:], percentageTotal = [:], netTotal = [:]
			def guests = 0, grossTot = 0, taxTot = 0, compTot = 0, percentageTot = 0, netTot = 0
			def avgGrossTotal = [:], avgTaxTotal = [:], avgCompTotal = [:], avgNetTotal = [:]
			def avgGrossTot = 0, avgTaxTot = 0, avgCompTot = 0, avgNetTot = 0
			def perGuestGrossTotal = [:], perGuestTaxTotal = [:], perGuestCompTotal = [:], perGuestNetTotal = [:]
			def perGuestGrossTot = 0, perGuestTaxTot = 0, perGuestCompTot = 0, perGuestNetTot = 0
			Set uniqueGuest = new HashSet()
			//def orderslist = Orders.createCriteria().list(params){ order "id", "desc" }
			//def orderlistTotal = Orders.count()
			def orderslist = Orders.createCriteria().list(params, query)
			def orderlistTotal = Orders.createCriteria().count(query)
			if(orderslist){
				orderslist.each {
					def order=it
					def key = order.orderId
					/*def itemName = order.itemName
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

					orders.put(key,itemName)*/
					
					// To retrieve items from orderItems for each order 
					def itemNames = "", gross = 0
					def orderItemsList = OrderItems.createCriteria().list {
							eq("order", order)
					}
					if (orderItemsList){
						orderItemsList.each {
							def orderItem = it
							def finalBasePrice = formatNumStr(orderItem.basePrice)
							
							itemNames += orderItem.itemName + " - \$ " + finalBasePrice + "</br>"
							
							Double base = new Double(finalBasePrice)
							gross += base
						}
					}
					def tip = order.tipPercentage
					// items
					orders.put(key, itemNames)
					// gross
					def formattedGross = formatNumStr(gross.toString())
					itemsGross.put(key, '$ '+formattedGross)
					// tax
					def formattedTax = formatNumStr(order.venue.totalTaxRate)
					orderTax.put(key, '$ '+formattedTax)
					// tip
					tipPercentage.put(key, tip+'%')
					// comp
					def compVal = (gross * new Double(tip)) / 100
					def formattedCompVal = formatNumStr(compVal.toString())
					comp.put(key, '$ '+formattedCompVal)
					// net
					def netVal = new Double(formattedGross) + new Double(formattedTax) + new Double(formattedCompVal)
					def formattedNetVal = formatNumStr(netVal.toString())
					net.put(key, '$ '+formattedNetVal)
					
					// Total calculation
					// unique guests
					if (uniqueGuest.add(order.userId)){
						guests++
					}
					totalGuests.put("totalGuests", guests)
					// gross total
					grossTot += new Double(formattedGross)
					def formattedGrossTotal = formatNumStr(grossTot.toString())
					grossTotal.put("grossTotal", '$ '+formattedGrossTotal)
					// tax total
					taxTot += new Double(formattedTax)
					def formattedTaxTotal = formatNumStr(taxTot.toString())
					taxTotal.put("taxTotal", '$ '+formattedTaxTotal)
					// comp total
					compTot += compVal
					def formattedCompTot = formatNumStr(compTot.toString())
					compTotal.put("compTotal", '$ '+formattedCompTot)
					// percentage total
					percentageTot += new Double(tip)
					def pTot = percentageTot/orderlistTotal
					percentageTotal.put("percentageTotal", pTot+'%')
					// net total
					netTot += netVal
					def formattedNetTot = formatNumStr(netTot.toString())
					netTotal.put("netTotal", '$ '+formattedNetTot)
					
					// Average calculation
					// gross total average
					avgGrossTot = new Double(formattedGrossTotal) / orderlistTotal
					def formattedAvgGrossTotal = formatNumStr(avgGrossTot.toString())
					avgGrossTotal.put("avgGrossTotal", '$ '+formattedAvgGrossTotal)
					// tax total average
					avgTaxTot = new Double(formattedTaxTotal) / orderlistTotal
					def formattedAvgTaxTotal = formatNumStr(avgTaxTot.toString())
					avgTaxTotal.put("avgTaxTotal", '$ '+formattedAvgTaxTotal)
					// comp total average
					avgCompTot = new Double(formattedCompTot) / orderlistTotal
					def formattedAvgCompTotal = formatNumStr(avgCompTot.toString())
					avgCompTotal.put("avgCompTotal", '$ '+formattedAvgCompTotal)
					// net total average
					avgNetTot = new Double(formattedNetTot) / orderlistTotal
					def formattedAvgNetTotal = formatNumStr(avgNetTot.toString())
					avgNetTotal.put("avgNetTotal", '$ '+formattedAvgNetTotal)
					
					// Per guest calculation
					// gross total per guest
					perGuestGrossTot = new Double(formattedGrossTotal) / guests
					def formattedPerGuestGrossTotal = formatNumStr(perGuestGrossTot.toString())
					perGuestGrossTotal.put("perGuestGrossTotal", '$ '+formattedPerGuestGrossTotal)
					// tax total per guest
					perGuestTaxTot = new Double(formattedTaxTotal) / guests
					def formattedPerGuestTaxTotal = formatNumStr(perGuestTaxTot.toString())
					perGuestTaxTotal.put("perGuestTaxTotal", '$ '+formattedPerGuestTaxTotal)
					// comp total per guest
					perGuestCompTot = new Double(formattedCompTot) / guests
					def formattedPerGuestCompTotal = formatNumStr(perGuestCompTot.toString())
					perGuestCompTotal.put("perGuestCompTotal", '$ '+formattedPerGuestCompTotal)
					// net total per guest
					perGuestNetTot = new Double(formattedNetTot) / guests
					def formattedPerGuestNetTotal = formatNumStr(perGuestNetTot.toString())
					perGuestNetTotal.put("perGuestNetTotal", '$ '+formattedPerGuestNetTotal)
					
				}
			}
			
			if(params?.format && params.format != "html"){
				response.contentType = grailsApplication.config.grails.mime.types[params.format]
				response.setHeader("Content-disposition", "attachment; filename=Orders_${jqStart}.${params.extension}")
				List fields = ["time", "tid", "item", "sender", "receipient","gross","taxl","compl","tipl","nett"]
				Map labels = ["time": "Time", "tid": "Transaction Id", "item": "Item", "sender":"Sender","receipient":"Receipient","gross":"Gross","taxl":"Tax","compl":"Comp","tipl":"Tip %","nett":"Net"]
				// Formatter closure
				def upperCase = { domain, value ->
					return value.toUpperCase()
				}
				def time = { domain, value ->
					return domain?.dateCreated
				}
				def tid = { domain, value ->
					return domain?.orderId
				}
				def sender = { domain, value ->
					return domain?.user.nickName
				}
				def receipient = { domain, value ->
					return domain?.receiverProfile.nickName
				}
				def item = {domain, value ->
					def listOfItems = new JSONArray(domain?.itemsList)
					def uniPar=""
					listOfItems.each{
						def itemInfo = it
						def finalBasePrice = formatNumStr(itemInfo.basePrice)
						uniPar+=itemInfo.itemName + " - \$ " + finalBasePrice + "\n"
					}
					return uniPar
				}
				def gross = {domain, value ->
					def listOfItems = new JSONArray(domain?.itemsList)
					def gross=0
					listOfItems.each{
						def itemInfo = it
						def finalBasePrice = formatNumStr(itemInfo.basePrice)
						Double base = new Double(finalBasePrice)
						gross += base
					}
					return formatNumStr(gross.toString())
				}
				def taxl = {domain, value ->
					def formattedTax = formatNumStr(domain?.venue?.totalTaxRate)
					return '$ '+formattedTax
				}
				def compl = {domain, value ->
					def listOfItems = new JSONArray(domain?.itemsList)
					def grossl=0
					listOfItems.each{
						def itemInfo = it
						def finalBasePrice = formatNumStr(itemInfo.basePrice)
						Double base = new Double(finalBasePrice)
						grossl += base
					}
					def tipls = domain?.tipPercentage
					def compVal = (grossl * new Double(tipls)) / 100
					def formattedCompVal = formatNumStr(compVal.toString())
					return '$ '+formattedCompVal
				}
				def tipl = {domain, value ->
					def tipL = domain?.tipPercentage
					return tipL+"%"
				}
				def nett = {domain, value->
					def total = domain?.totalPrice
					def formattedTotal = formatNumStr(total.toString())
					return '$ '+formattedTotal
				}				
				Map formatters = [time:time,tid:tid,item:item,sender:sender,receipient:receipient,gross:gross,taxl:taxl,compl:compl,tipl:tipl,nett:nett]
				Map parameters
				exportService.export(params.format,response.outputStream,orderslist, fields, labels, formatters, parameters)
			}
			
			[jqStart:jqStart,jqEnd:jqEnd,ordersList:orderslist, ordersTotal:orderlistTotal, itemsNames:orders, gross:itemsGross, tax:orderTax, tip:tipPercentage, comp:comp, net:net, 
				totalGuests:totalGuests, grossTotal:grossTotal, taxTotal:taxTotal, compTotal:compTotal, percentageTotal:percentageTotal, netTotal:netTotal, 
				avgGrossTotal:avgGrossTotal, avgTaxTotal:avgTaxTotal, avgCompTotal:avgCompTotal, avgNetTotal:avgNetTotal,
				perGuestGrossTotal:perGuestGrossTotal, perGuestTaxTotal:perGuestTaxTotal, perGuestCompTotal:perGuestCompTotal, perGuestNetTotal:perGuestNetTotal]
		}catch(Exception e){
			log.error("Error in Orders List ==>"+e.getMessage())
		}
	}
	
	/**
	 * Method to convert a string representing a number to '##.##' format 
	 * @return formatted string
	 */
	def formatNumStr(def inputNumStr) {
		String tempBasePrice, finalBasePrice
		DecimalFormat decimalFormat = new DecimalFormat("#.##")
		
		if (inputNumStr.contains(".")){
			tempBasePrice = inputNumStr.substring(inputNumStr.indexOf(".") + 1)
			if (tempBasePrice.length() == 1){
				finalBasePrice = inputNumStr + '0'
			}else if(tempBasePrice.length() > 2){
				Double basePriceDouble = new Double(inputNumStr)
				finalBasePrice = decimalFormat.format(basePriceDouble)
			}else{
				finalBasePrice = inputNumStr
			}
		}else{
			finalBasePrice = inputNumStr + ".00"
		}
		
		return finalBasePrice
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

			if(params.tradingDay){
				def tradingreq = BartsyConfiguration.findByConfigName("tradingDay")
				tradingreq.value = params.tradingDay
				if(!tradingreq.save(flush:true)){
					log.error("Trading time update failed ==>"+tradingreq.errors)
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
			def query={
				if(params.userType)
					eq("userType",params.userType)
				if(params.status)
					eq("status", Integer.parseInt(params.status.toString()))
				if(params.keyword && (!params.keyword.equals("null") || !params.keyword.equals(""))){
						or{
							like("firstName",params.keyword+"%")
							like("lastName",params.keyword+"%")
						}
					}				
			}
			//def salesList = AdminUser.findAllByUserTypeInList(["SalesUser", "SalesManager"])
			def salesList = AdminUser.createCriteria().list(query)
			def salesCnt = salesList.size()
			[salesList:salesList, salesCnt:salesCnt, saleParam:params]
		}catch(Exception e){
			log.error("Error in sales user list"+e.getMessage())
		}
	}

	def createSaleUser(){
		try{
			if(params.act){
				def saleInfo = AdminUser.findAllByUsernameAndUserType(params.username,params.userType)
				if(!saleInfo){
					int stat = 1
					def saleAcc = new AdminUser()
					saleAcc.setFirstName(params.firstName)
					saleAcc.setLastName(params.lastName)
					saleAcc.setEmail(params.email)
					saleAcc.setUsername(params.username)
					saleAcc.setPassword(params.password)
					saleAcc.setUserType(params.userType)
					saleAcc.setStatus(stat)
					def promoCode = generatePromoCode()
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
	
	/**
	 * Service to retrieve venue details such as rep name, manager, wifi, etc..
	 * @return venue rep name, manager, wifi, hours, etc..
	 */
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
	
	/**
	 * Method to retrieve summary like total guests, total checks, guests avg etc..
	 * @return total guests, total checks etc.. as int
	 */
	def summary() {
		[totalGuests : 159, totalChecks : 87, guestsAvg : 12.26, checksAvg : 22.22]
	}
	
	def categories() {
		
	}
}

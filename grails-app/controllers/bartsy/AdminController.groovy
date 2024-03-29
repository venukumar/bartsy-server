package bartsy

import groovy.time.TimeDuration
import groovy.time.TimeCategory

import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hsqldb.util.CSVWriter

class AdminController {
	def exportService
	def grailsApplication  //inject GrailsApplication
	
	def index() {
		if(session.user){
			forward(action:"summary")
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
				forward(action:"summary")
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
			def checkInsList, ordersList, checkInsLast30DaysList, ordersLast30DaysList
			def checkInsMap = [:], ordersMap = [:], checkInsLast30DaysMap = [:], ordersLast30DaysMap = [:], 
				avgAcceptTimeMap = [:], avgCompleteTimeMap = [:], avgPickupTimeMap = [:], rejectionRateMap = [:]
			def venuelist = Venue.createCriteria().list {
				ne("status", "DELETED")	
			}
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
						between("checkedInDate", currentDate-30, currentDate)
					}
				}
				checkInsLast30DaysMap.put(venueObj.id, checkInsLast30DaysList.size())
				
				// Total orders in last 30 days
				ordersLast30DaysList = Orders.createCriteria().list {
					eq("venue", venueObj)
					and{
						between("dateCreated", currentDate-30, currentDate)
					}
				}
				ordersLast30DaysMap.put(venueObj.id, ordersLast30DaysList.size())
				
				// Average accept time of venue
				avgAcceptTimeMap = calcAvgOrderAcceptTime(venueObj, avgAcceptTimeMap)
				
				// Average complete time of venue
				avgCompleteTimeMap = calcAvgOrderCompleteTime(venueObj, avgCompleteTimeMap)
				
				// Average pickup time of venue
				avgPickupTimeMap = calcAvgOrderPickupTime(venueObj, avgPickupTimeMap)
				
				// Rejection rate of venue
				def orderRejectionList = Orders.createCriteria().list {
					eq("venue", venueObj)
					and{
						eq("orderStatus", OrderConstants.ORDER_STATUS_REJECTED)
					}
				}
				def rejectionRate
				if (orderRejectionList){
					rejectionRate =  (orderRejectionList.size() * 100) / ordersList.size()
				}
				rejectionRateMap.put(venueObj.id, rejectionRate?:0)
			}
				
			[venueList:venuelist, venueTotal:venuelistTotal, checkIns:checkInsMap, orders:ordersMap, checkInsLast30Days:checkInsLast30DaysMap, 
				ordersLast30Days:ordersLast30DaysMap, avgAcceptTime:avgAcceptTimeMap, avgCompleteTime:avgCompleteTimeMap, avgPickupTime:avgPickupTimeMap,
				rejectionRate:rejectionRateMap]
		}catch(Exception e){
			log.error("Error in Venue List ==>"+e.getMessage())
		}
	}
	
	/**
	 * Method to calculate average accept time of order by venue
	 * @param venueObj
	 * @param avgAcceptTimeMap
	 * @return average accept time map containing venue id and avg. accept time
	 */
	def calcAvgOrderAcceptTime(def venueObj, def avgAcceptTimeMap){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
		def orderAcceptedList = Orders.createCriteria().list {
			eq("venue", venueObj)
			and{
				eq("orderStatus", OrderConstants.ORDER_STATUS_ACCEPTED)
			}
		}
		if (orderAcceptedList){
			def totalMin = 0
			orderAcceptedList.each{
				def orderObj = it
				def orderStatusDateJSON = orderObj.dateOrderStatus
				if (orderStatusDateJSON){
					def jsonObj = new JSONObject(orderStatusDateJSON)
					def orderStatusDateNew = jsonObj.get(OrderConstants.ORDER_STATUS_NEW)
					def orderStatusDateAccept = jsonObj.get(OrderConstants.ORDER_STATUS_ACCEPTED)
					Date newDate = dateFormat.parse(orderStatusDateNew)
					Date acceptDate = dateFormat.parse(orderStatusDateAccept)
					TimeDuration duration = TimeCategory.minus(acceptDate, newDate)
					totalMin += duration.minutes
				}
			}
			def avgAcceptMins = totalMin / orderAcceptedList.size()
			avgAcceptTimeMap.put(venueObj.id, avgAcceptMins+" minutes")
		}
		return avgAcceptTimeMap
	}
	
	/**
	 * Method to calculate average complete time of order by venue
	 * @param venueObj
	 * @param avgCompleteTimeMap
	 * @return average complete time map containing venue id and avg. complete time
	 */
	def calcAvgOrderCompleteTime(def venueObj, def avgCompleteTimeMap){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
		def orderCompletedList = Orders.createCriteria().list {
			eq("venue", venueObj)
			and{
				eq("orderStatus", OrderConstants.ORDER_STATUS_COMPLETE)
			}
		}
		if (orderCompletedList){
			def totalMin = 0
			orderCompletedList.each{
				def orderObj = it
				def orderStatusDateJSON = orderObj.dateOrderStatus
				if (orderStatusDateJSON){
					def jsonObj = new JSONObject(orderStatusDateJSON)
					def orderStatusDateAccept = jsonObj.get(OrderConstants.ORDER_STATUS_ACCEPTED)
					def orderStatusDateComplete = jsonObj.get(OrderConstants.ORDER_STATUS_COMPLETE)
					Date acceptDate = dateFormat.parse(orderStatusDateAccept)
					Date completeDate = dateFormat.parse(orderStatusDateComplete)
					TimeDuration duration = TimeCategory.minus(completeDate, acceptDate)
					totalMin += duration.minutes
				}
			}
			
			def avgCompleteMins = totalMin / orderCompletedList.size()
			avgCompleteTimeMap.put(venueObj.id, avgCompleteMins+" minutes")
		}
		return avgCompleteTimeMap
	}
	
	/**
	 * Method to calculate average pickup time of order by venue
	 * @param venueObj
	 * @param avgPickupTimeMap
	 * @return average pickup time map containing venue id and avg. pickup time
	 */
	def calcAvgOrderPickupTime(def venueObj, def avgPickupTimeMap){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
		def orderPickedupList = Orders.createCriteria().list {
			eq("venue", venueObj)
			and{
				eq("orderStatus", OrderConstants.ORDER_STATUS_PICKED_UP)
			}
		}
		if (orderPickedupList){
			def totalMin = 0
			orderPickedupList.each{
				def orderObj = it
				def orderStatusDateJSON = orderObj.dateOrderStatus
				if (orderStatusDateJSON){
					def jsonObj = new JSONObject(orderStatusDateJSON)
					def orderStatusDateComplete = jsonObj.get(OrderConstants.ORDER_STATUS_COMPLETE)
					def orderStatusDatePickedup = jsonObj.get(OrderConstants.ORDER_STATUS_PICKED_UP)
					Date completeDate = dateFormat.parse(orderStatusDateComplete)
					Date pickedupDate = dateFormat.parse(orderStatusDatePickedup)
					TimeDuration duration = TimeCategory.minus(pickedupDate, completeDate)
					totalMin += duration.minutes
				}
			}
			def avgPickupMins = totalMin / orderPickedupList.size()
			avgPickupTimeMap.put(venueObj.id, avgPickupMins+" minutes")
		}
		return avgPickupTimeMap
	}
	
	/**
	 * Method to retrieve venue rewards
	 * @return list of venue rewards
	 */
	def venueRewards() {
		try{
			if(params){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				def configList = VenueConfig.createCriteria().list() {
					eq("venue", venue)
					order("rewardPoints", "asc")
				}
				def configListSize = configList.size()
				if(venue){
					[venue:venue, venueList:configList, configListSize:configListSize]
				}else{
					flash.message = "Venue Not Found"
				}
			}

		}catch(Exception e){
			log.error("Exception in retrieving venue list ==>"+e.getMessage())
		}
	}
	
	/**
	 * Method to retrieve venue rewards
	 * @return list of venue rewards
	 */
	def addEditVenueReward() {
		try{
			if (params.id){
				def venueRewardId = params.id
				def venueReward = VenueConfig.findById(venueRewardId)
				render(view:"addEditVenueReward", model:[venueReward:venueReward, venueId:params.venueId])
			}else{
				render(view:"addEditVenueReward", model:[venueId:params.venueId])
			}
		}catch(Exception e){
			log.error("Exception in retrieving venue list ==>"+e.getMessage())
		}
	}
	
	/**
	 * Service to save venue rewards
	 * @return success/failure
	 */
	def saveVenueReward(){
		try{
			def rewardPoints, description, type, value, venueRewardId
			VenueConfig configVenue
			boolean isEdit = false
			
			if(params.rewardPoints){
				rewardPoints = params.rewardPoints
			}
			if(params.description){
				description = params.description
			}
			if(params.type){
				type = params.type
			}
			if(params.value){
				value = params.value
			}
			if(params.venueRewardId){
				venueRewardId = params.venueRewardId
				isEdit = true
			}
			
			if (isEdit){
				configVenue = VenueConfig.findById(venueRewardId)
			}else{
				configVenue = new VenueConfig()
			}
			Venue venue = Venue.findByVenueId(params.venueId)
			if(rewardPoints && type && venue){
				configVenue.setRewardPoints(Integer.parseInt(rewardPoints))
				configVenue.setDescription(description)
				configVenue.setValue(value)
				configVenue.setType((type.toString().equals("1") ? "Discount" : "General"))
				configVenue.setVenue(venue)
				if(!configVenue.save(flush:true)){
					flash.message = "Please try again."
				}else{
					flash.message = "Venue reward saved successfully."
				}
			}else{
				flash.message = "Please fill reward points."
			}
			redirect(action:"venueConfig", id:venue.venueId, params: [rew: 8])
		}catch(Exception e){
			log.error("Exception found "+e.getMessage())
		}
	}
	
	/**
	 * Service to delete a venue reward
	 * @return
	 */
	def deleteVenueReward(){
		try{
			def venueRewardId
			if(params.id){
				venueRewardId = params.id
				VenueConfig configVenue = VenueConfig.findById(venueRewardId)
				if (configVenue){
					configVenue.delete()
				}else{
					flash.mesage = "Venue reward not found."
				}
			}
			Venue venue = Venue.findByVenueId(params.venueId)
			redirect(action:"venueRewards", id:venue.venueId)
		}catch(Exception e){
			log.error("Exception found "+e.getMessage())
		}
	}
	
	/**
	 * Service to retrieve order details - venue, user name, order Id, order status, etc..
	 * @return list of order details
	 */
	def orderDetails(){
		try{
			if(params){
				int lastOrderState
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
				
				// order status
				int orderStatusNumber = Integer.parseInt(order.orderStatus)
				def orderStatus = orderStatusArr[orderStatusNumber]
				// order last state
				def lastState = order.lastState
				if(lastState){
					lastOrderState = Integer.parseInt(lastState)
				}else{
					lastOrderState = 0
				}
				def orderLastState = orderStatusArr[lastOrderState]
				// item name
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
				// base price
				def basePrice = order.basePrice
				def formattedBasePrice = formatNumStr(basePrice.toString())
				// tip percentage
				def tip = order.tipPercentage
				def formattedTip = formatNumStr(tip.toString())
				// total price
				def totPrice = order.totalPrice
				def formattedTotPrice = formatNumStr(totPrice.toString())
				render(view:"orderDetails",model:[selectedOrder:order, orderStatus:orderStatus, orderLastState:orderLastState, itemName:itemName, 
					tipPercentage:formattedTip+'%', totalPrice:'$'+formattedTotPrice, , basePrice:'$'+formattedBasePrice])
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
			Date  startDate, endDate
			Date date = new Date()
			DateFormat jQdateFormat = new SimpleDateFormat("MM/dd/yyyy")
			DateFormat dateFormatn = new SimpleDateFormat("yyyy-MM-dd")
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh")
			
			def tradeTime = BartsyConfiguration.findByConfigName("tradingDay")
			if(params.startDate){
				startDate = dateFormat.parse(dateFormatn.format(new Date(params.startDate))+" "+tradeTime.value)
			}else{
			 	startDate = dateFormat.parse(dateFormatn.format(date)+" "+tradeTime.value)
			}
			if(params.endDate){
				endDate = dateFormat.parse(dateFormatn.format(new Date(params.endDate))+" "+tradeTime.value)
			}else{
				endDate = new Date(startDate.getTime() + (1000 * 60 * 60 * 24));
			}
			def jqStart = jQdateFormat.format(startDate)
			def jqEnd = jQdateFormat.format(endDate)
			def query = {
				between("dateCreated", startDate, endDate)
				order "id", "desc"
			}	
					
			def orders=[:], itemsGross = [:], orderTax = [:], tipPercentage = [:], comp = [:], net = [:] 
			def totalGuests = [:], grossTotal = [:], taxTotal = [:], compTotal = [:], percentageTotal = [:], netTotal = [:]
			def guests = 0, grossTot = 0, taxTot = 0, compTot = 0, percentageTot = 0, netTot = 0
			def avgGrossTotal = [:], avgTaxTotal = [:], avgCompTotal = [:], avgNetTotal = [:]
			def avgGrossTot = 0, avgTaxTot = 0, avgCompTot = 0, avgNetTot = 0
			def perGuestGrossTotal = [:], perGuestTaxTotal = [:], perGuestCompTotal = [:], perGuestCompPerTotal = [:], perGuestNetTotal = [:]
			def perGuestGrossTot = 0, perGuestTaxTot = 0, perGuestCompTot = 0, perGuestCompPerTot = 0, perGuestNetTot = 0
			Set uniqueGuest = new HashSet()
			
			def orderslist = Orders.createCriteria().list(params, query)
			def orderlistTotal = Orders.createCriteria().count(query)
			if(orderslist){
				orderslist.each {
					def order=it
					def key = order.orderId
					// To retrieve items from orderItems for each order 
					def itemNames = "", gross = 0
					def orderItemsList = OrderItems.createCriteria().list {
							eq("order", order)
					}
					if (orderItemsList){
						orderItemsList.each {
							def orderItem = it
							def basePrice = orderItem.basePrice
							if (!basePrice){
								basePrice = 0
							}
							def finalBasePrice = formatNumStr(basePrice.toString())
							itemNames += orderItem.itemName + " - \$" + finalBasePrice + "</br>"
							Double base = new Double(finalBasePrice)
							gross += base
						}
					}
					// items
					orders.put(key, itemNames)
					
					// gross
					def formattedGross = formatNumStr(gross.toString())
					itemsGross.put(key, '$'+formattedGross)
					
					// tax
					def totTaxRate = order.venue.totalTaxRate
					if (!totTaxRate){
						totTaxRate = 0
					}
					def calcTax = (gross * new Double(totTaxRate)) / 100
					def formattedTax = formatNumStr(calcTax.toString())
					orderTax.put(key, '$'+formattedTax)
					
					// comp and tip are same
					def tip = order.tipPercentage
					if (!tip){
						tip = 0
					}
					def formattedTip = formatNumStr(tip.toString())
					comp.put(key, '$'+formattedTip)
					
					// comp percentage or tip percentage
					def tipPercent
					if (gross > 0){
						tipPercent = (100 * new Double(tip)) / gross
					}else{
						tipPercent = 0
					}
					def formattedTipPercent = formatTipNumStr(tipPercent.toString())
					tipPercentage.put(key, formattedTipPercent+'%')
					
					// net
					//def netVal = new Double(formattedGross) + new Double(formattedTax) + new Double(formattedTip)
					def totPrice = order.totalPrice
					if (!totPrice){
						totPrice = 0
					}
					def netVal = new Double(totPrice)
					def formattedNetVal = formatNumStr(netVal.toString())
					net.put(key, '$'+formattedNetVal)
					
					// Totals calculation
					// unique guests
					if (uniqueGuest.add(order.userId)){
						guests++
					}
					totalGuests.put("totalGuests", guests)
					
					// gross total
					grossTot += new Double(formattedGross)
					
					// tax total
					taxTot += new Double(formattedTax)
					
					// comp = tip total
					compTot += new Double(tip)
					
					// comp or tip percentage total
					percentageTot += new Double(tipPercent)
					
					// net total
					netTot += netVal
				}
				// Total calculation
				// gross total
				def formattedGrossTotal = formatNumStr(grossTot.toString())
				grossTotal.put("grossTotal", '$'+formattedGrossTotal)
				
				// tax total
				def formattedTaxTotal = formatNumStr(taxTot.toString())
				taxTotal.put("taxTotal", '$'+formattedTaxTotal)
				
				// comp = tip total
				def formattedCompTot = formatNumStr(compTot.toString())
				compTotal.put("compTotal", '$'+formattedCompTot)
				
				// tip percentage total
				def pTot = percentageTot/orderlistTotal
				def formattedPTot = formatTipNumStr(pTot.toString())
				percentageTotal.put("percentageTotal", formattedPTot+'%')
				
				// net total
				def formattedNetTot = formatNumStr(netTot.toString())
				netTotal.put("netTotal", '$'+formattedNetTot)
				
				// Average calculation
				// gross total average
				avgGrossTot = new Double(formattedGrossTotal) / orderlistTotal
				def formattedAvgGrossTotal = formatNumStr(avgGrossTot.toString())
				avgGrossTotal.put("avgGrossTotal", '$'+formattedAvgGrossTotal)
				
				// tax total average
				avgTaxTot = new Double(formattedTaxTotal) / orderlistTotal
				def formattedAvgTaxTotal = formatNumStr(avgTaxTot.toString())
				avgTaxTotal.put("avgTaxTotal", '$'+formattedAvgTaxTotal)
				
				// comp total average
				avgCompTot = new Double(formattedCompTot) / orderlistTotal
				def formattedAvgCompTotal = formatNumStr(avgCompTot.toString())
				avgCompTotal.put("avgCompTotal", '$'+formattedAvgCompTotal)
				
				// net total average
				avgNetTot = new Double(formattedNetTot) / orderlistTotal
				def formattedAvgNetTotal = formatNumStr(avgNetTot.toString())
				avgNetTotal.put("avgNetTotal", '$'+formattedAvgNetTotal)
				
				// Per guest calculation
				// gross total per guest
				perGuestGrossTot = new Double(formattedGrossTotal) / guests
				def formattedPerGuestGrossTotal = formatNumStr(perGuestGrossTot.toString())
				perGuestGrossTotal.put("perGuestGrossTotal", '$'+formattedPerGuestGrossTotal)
				
				// tax total per guest
				perGuestTaxTot = new Double(formattedTaxTotal) / guests
				def formattedPerGuestTaxTotal = formatNumStr(perGuestTaxTot.toString())
				perGuestTaxTotal.put("perGuestTaxTotal", '$'+formattedPerGuestTaxTotal)
				
				// comp total per guest
				perGuestCompTot = new Double(formattedCompTot) / guests
				def formattedPerGuestCompTotal = formatNumStr(perGuestCompTot.toString())
				perGuestCompTotal.put("perGuestCompTotal", '$'+formattedPerGuestCompTotal)
				
				// comp percentage total per guest
				perGuestCompPerTot = percentageTot / guests
				def fPerGuestCompPerTotal = formatTipNumStr(perGuestCompPerTot.toString())
				perGuestCompPerTotal.put("perGuestCompPerTotal", fPerGuestCompPerTotal+'%')
				
				// net total per guest
				perGuestNetTot = new Double(formattedNetTot) / guests
				def formattedPerGuestNetTotal = formatNumStr(perGuestNetTot.toString())
				perGuestNetTotal.put("perGuestNetTotal", '$'+formattedPerGuestNetTotal)
			}
			
			// Export
			if(params?.format && params.format != "html"){
				response.contentType = grailsApplication.config.grails.mime.types[params.format]
				response.setHeader("Content-disposition", "attachment; filename=Orders_${jqStart}.${params.extension}")
				List fields = ["dateCreated", "orderId", "itemsList", "sender", "recipient", "gross", "taxl", "compl", "tipl", "nett"]
				Map labels = ["dateCreated": "Time", "orderId": "Id", "itemsList": "Item", "sender":"Sender", "recipient":"Recipient", "gross":"Gross", "taxl":"Tax", "compl":"Comp", "tipl":"Tip %", "nett":"Net"]
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				
				// Formatter closure
				def upperCase = { domain, value ->
					return value.toUpperCase()
				}
				def time = { domain, value ->
					def dateCreated = domain?.dateCreated
					if (dateCreated){
						return sdf.format(dateCreated)
					}
					//return domain?.dateCreated
				}
				def tid = { domain, value ->
					return domain?.orderId
				}
				def sender = { domain, value ->
					return domain?.user.nickName
				}
				def recipient = { domain, value ->
					return domain?.receiverProfile.nickName
				}
				def item = {domain, value ->
					def listOfItems = new JSONArray(domain?.itemsList)
					def uniPar = ""
					listOfItems.each{
						def itemInfo = it
						def basePrice = itemInfo.price
						if (!basePrice){
							basePrice = 0
						}
						def finalBasePrice = formatNumStr(basePrice.toString())
						uniPar += itemInfo.itemName + " - \$" + finalBasePrice + "\n"
					}
					return uniPar
				}
				def gross = {domain, value ->
					def listOfItems = new JSONArray(domain?.itemsList)
					def gross=0
					listOfItems.each{
						def itemInfo = it
						def basePrice1 = itemInfo.price
						if (!basePrice1){
							basePrice1 = 0
						}
						def finalBasePrice = formatNumStr(basePrice1.toString())
						Double base = new Double(finalBasePrice)
						gross += base
					}
					def formattedGross = formatNumStr(gross.toString())
					return '$'+formattedGross
				}
				def taxl = {domain, value ->
					def listOfItems = new JSONArray(domain?.itemsList)
					def grossTax = 0
					listOfItems.each{
						def itemInfo = it
						def basePrice2 = itemInfo.price
						if (!basePrice2){
							basePrice2 = 0
						}
						def finalBasePrice = formatNumStr(basePrice2.toString())
						Double base = new Double(finalBasePrice)
						grossTax += base
					}
					def totTax = domain?.venue?.totalTaxRate
					if (!totTax){
						totTax = 0
					}
					def taxCalc = (grossTax * new Double(totTax)) / 100
					def formattedTax = formatNumStr(taxCalc.toString())
					return '$'+formattedTax
				}
				def compl = {domain, value ->
					def tipls = domain?.tipPercentage
					if (!tipls){
						tipls = 0
					}
					def formattedTip =  formatNumStr(tipls.toString())
					return '$'+formattedTip
				}
				def tipl = {domain, value ->
					def listOfItems = new JSONArray(domain?.itemsList)
					def grossTipPer = 0
					listOfItems.each{
						def itemInfo = it
						def basePrice3 = itemInfo.price
						if (!basePrice3){
							basePrice3 = 0
						}
						def finalBasePrice = formatNumStr(basePrice3.toString())
						Double base = new Double(finalBasePrice)
						grossTipPer += base
					}
					def tipL = domain?.tipPercentage
					if (!tipL){
						tipL = 0
					}
					def tipPercent
					if (grossTipPer > 0){
						tipPercent = (100 * new Double(tipL)) / grossTipPer
					}else{
						tipPercent = 0
					}
					def formattedTipVal = formatTipNumStr(tipPercent.toString())
					return formattedTipVal+"%"
				}
				def nett = {domain, value->
					def total = domain?.totalPrice
					if (!total){
						total = 0
					}
					def formattedTotal = formatNumStr(total.toString())
					return '$'+formattedTotal
				}
				Map formatters = [dateCreated:time, orderId:tid, itemsList:item, sender:sender, recipient:recipient, gross:gross, taxl:taxl, compl:compl, tipl:tipl, nett:nett]
				Map parameters
				exportService.export(params.format,response.outputStream,orderslist, fields, labels, formatters, parameters)
			}
			
			[jqStart:jqStart,jqEnd:jqEnd,ordersList:orderslist, ordersTotal:orderlistTotal, itemsNames:orders, gross:itemsGross, tax:orderTax, tip:tipPercentage, comp:comp, net:net, 
				totalGuests:totalGuests, grossTotal:grossTotal, taxTotal:taxTotal, compTotal:compTotal, percentageTotal:percentageTotal, netTotal:netTotal, 
				avgGrossTotal:avgGrossTotal, avgTaxTotal:avgTaxTotal, avgCompTotal:avgCompTotal, avgNetTotal:avgNetTotal,
				perGuestGrossTotal:perGuestGrossTotal, perGuestTaxTotal:perGuestTaxTotal, perGuestCompTotal:perGuestCompTotal, perGuestCompPerTotal:perGuestCompPerTotal, perGuestNetTotal:perGuestNetTotal]
		}catch(Exception e){
			log.error("Error in Orders List ==>"+e.getMessage())
		}
	}
	
	/**
	 * Method to convert a string representing a number to '##.##' format 
	 * @return formatted string
	 */
	def formatNumStr(def inputNumStr) {
		String tempNumStr, finalNumStr
		DecimalFormat decimalFormat = new DecimalFormat("#.##")
		
		if (inputNumStr.contains(".")){
			tempNumStr = inputNumStr.substring(inputNumStr.indexOf(".") + 1)
			if (tempNumStr.length() == 1){
				finalNumStr = inputNumStr + '0'
			}else if(tempNumStr.length() > 2){
				Double basePriceDouble = new Double(inputNumStr)
				finalNumStr = decimalFormat.format(basePriceDouble)
			}else{
				finalNumStr = inputNumStr
			}
		}else{
			finalNumStr = inputNumStr + ".00"
		}
		
		return finalNumStr.trim()
	}
	
	/**
	 * Method to convert a trip string representing a number to '##.#' format
	 * @return formatted string
	 */
	def formatTipNumStr(def inputNumStr) {
		String tempNumStr, finalNumStr
		DecimalFormat decimalFormat = new DecimalFormat("#.#")
		
		if (inputNumStr.contains(".")){
			tempNumStr = inputNumStr.substring(inputNumStr.indexOf(".") + 1)
			if(tempNumStr.length() > 1){
				Double basePriceDouble = new Double(inputNumStr)
				finalNumStr = decimalFormat.format(basePriceDouble)
			}else{
				finalNumStr = inputNumStr
			}
		}else{
			finalNumStr = inputNumStr
		}
		
		return finalNumStr.trim()
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

			/*if(params.tradingDay){
				def tradingreq = BartsyConfiguration.findByConfigName("tradingDay")
				tradingreq.value = params.tradingDay
				if(!tradingreq.save(flush:true)){
					log.error("Trading time update failed ==>"+tradingreq.errors)
					err++
				}
			}*/
			if(params.tradingDay && params.tradingTime){
				def tradingreq = BartsyConfiguration.findByConfigName("tradingDay")
				def amPm = params.tradingTime
				def time = Integer.parseInt(params.tradingDay)
				if (amPm.equals("pm") && time != 12){
					time += 12
				}else if (amPm.equals("am") && time == 12){
					time += 12
				}
				tradingreq.value = time.toString()
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
				if(params.userType){
					eq("userType", params.userType)
				}
				if(params.status){
					eq("status", Integer.parseInt(params.status.toString()))
				}
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
				def saleInfo = AdminUser.findAllByUsernameAndUserType(params.username, params.userType)
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
					if (!saleAcc.save(flush:true)){
						flash.error = "Problem creating account"
						[saleInstance:params]
					}else{
						flash.message =  "Account created successfully"
						forward(action:"saleuserList")
					}
				}else{
					flash.error = "Account already exists with the same Username"
					[saleInstance:params]
				}

			}
			[saleInstance:params]

		}catch(Exception e){
			log.error("Exception in create User ==>"+e.getMessage())
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
						flash.error =  "Account updation failed"
						[saleInstance:params]
					}else{
						flash.message =  "Account updated successfully"
						forward(action:"saleuserList")
					}
				}
				[saleInstance:saleInfo]
			}else{
				flash.error = "Account doesn't exist"
				forward(action:"saleuserList")
			}
		}catch(Exception e){
			log.error("Exception in edit User ==>"+e.getMessage())
		}
	}

	def deleteSaleUser(){
		try{
			def saleInfo = AdminUser.get(params.id)
			if(saleInfo){
				saleInfo.delete(flush:true)
				flash.message =  "Account deleted successfully"
				forward(action:"saleuserList")
			}else{
				flash.error = "Account doesn't exist"
				forward(action:"saleuserList")
			}
		}catch(Exception e){
			log.error("Exception in delete User ==>"+e.getMessage())
		}
	}
	
	/**
	 * Service to retrieve venue details such as rep name, manager, wifi, etc..
	 * @return venue rep name, manager, wifi, hours, etc..
	 */
	def venueConfig(){
		try{
			// Edit venue
			if(params.id && params.vc){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				//def configList= VenueConfig.findAllByVenue(venue)
				//def configListSize = configList.size()
				String[] monday, tuesday, wednesday, thursday, friday, saturday, sunday
				if(venue){
					if (venue.openHours){
						def openHoursJSON = new JSONObject(venue.openHours)
						//def hrs = '{"Thursday":[" - "],"Saturday":[" - "],"Monday":[" - "],"Tuesday":[" - "],"Wednesday":[" - "],"Friday":[" - "],"Sunday":[" - "]}'
						//def openHoursJSON = new JSONObject(hrs)
						monday = parseOpenHours(openHoursJSON.get("Monday")) 
						tuesday = parseOpenHours(openHoursJSON.get("Tuesday"))
						wednesday = parseOpenHours(openHoursJSON.get("Wednesday"))
						thursday = parseOpenHours(openHoursJSON.get("Thursday"))
						friday = parseOpenHours(openHoursJSON.get("Friday"))
						saturday = parseOpenHours(openHoursJSON.get("Saturday"))
						sunday = parseOpenHours(openHoursJSON.get("Sunday"))
					}
					//[venue:venue,venueList:configList,configListSize:configListSize]
					[venue:venue, mon:monday, tues:tuesday, wed:wednesday, thurs:thursday, fri:friday, sat:saturday, sun:sunday, params:params]
				}else{
					flash.message = "Venue Not Found"
					
					monday = parseOpenHours(monday)
					tuesday = parseOpenHours(tuesday)
					wednesday = parseOpenHours(wednesday)
					thursday = parseOpenHours(thursday)
					friday = parseOpenHours(friday)
					saturday = parseOpenHours(saturday)
					sunday = parseOpenHours(sunday)
					
					[mon:monday, tues:tuesday, wed:wednesday, thurs:thursday, fri:friday, sat:saturday, sun:sunday, params:params]
				}
			} else if (params.id && params.mgr){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				[venue:venue]
			}else if (params.id && params.vRep){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				[venue:venue]
			}else if (params.id && params.menu){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				[venue:venue]
			}else if (params.id && params.orders){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				[venue:venue]
			}else if (params.id && params.bankAcct){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				[venue:venue]
			}else if (params.id && params.wifi){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				[venue:venue]
			}else if (params.id && params.rew){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				def configList = VenueConfig.createCriteria().list() {
					eq("venue", venue)
					order("rewardPoints", "asc")
				}
				def configListSize = configList.size()
				[venue:venue, venueList:configList, configListSize:configListSize]
			}else if (!params.id && params.vc){
				String[] monday, tuesday, wednesday, thursday, friday, saturday, sunday
				monday = parseOpenHours(monday)
				tuesday = parseOpenHours(tuesday)
				wednesday = parseOpenHours(wednesday)
				thursday = parseOpenHours(thursday)
				friday = parseOpenHours(friday)
				saturday = parseOpenHours(saturday)
				sunday = parseOpenHours(sunday)
				
				[mon:monday, tues:tuesday, wed:wednesday, thurs:thursday, fri:friday, sat:saturday, sun:sunday, params:params]
			}

		}catch(Exception e){
			log.error("Exception in retrieving venue setting ==>"+e.getMessage())
		}
	}
	
	/**
	 * Method to parse venue open hours JSON 
	 * @return String array containing hrs and mins
	 */
	def parseOpenHours(def dayHours) {
		String[] dayHrsTemp = new String[2]
		if (dayHours && dayHours.size() > 0){
			def dayHrs
			dayHours.each{
				dayHrs = it
			}
			if (dayHrs){
				dayHrsTemp = dayHrs.split('-')
			}
		}else{
			dayHrsTemp[0] = "00:00:00"
			dayHrsTemp[1] = "00:00:00"
		}
		if (dayHrsTemp[0].equals(" ") || dayHrsTemp[0] == null){
			dayHrsTemp[0] = "00:00:00"
		}
		if (dayHrsTemp[1].equals(" ") || dayHrsTemp[1] == null){
			dayHrsTemp[1] = "00:00:00"
		}
		return dayHrsTemp
	}
	
	/**
	 * Service to retrieve venue manager details
	 * Name, username, password, cell
	 * @return manager details
	 *//*
	def venueConfigManager() {
		try{
			if(params.id){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				if(venue){
					render(view:"venueConfig", model:[venue:venue])
				}else{
					flash.message = "Venue Not Found"
				}
			}
		}catch(Exception e){
			log.error("Exception in retrieving venue manager details ==>"+e.getMessage())
		}
	}
	
	*//**
	 * Service to retrieve vendsy representative details
	 * Name, email, cell
	 * @return vendsy representative details
	 *//*
	def venueConfigVendsyRep() {
		try{
			if(params.id){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				if(venue){
					render(view:"venueConfig", model:[venue:venue])
				}else{
					flash.message = "Venue Not Found"
				}
			}
		}catch(Exception e){
			log.error("Exception in retrieving vendsy representative details ==>"+e.getMessage())
		}
	}
	
	*//**
	 * Service to retrieve locu menu details
	 * Locu username, password, id, section
	 * @return locu menu details
	 *//*
	def venueConfigMenu() {
		try{
			if(params.id){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				if(venue){
					[venue:venue]
				}else{
					flash.message = "Venue Not Found"
				}
			}
		}catch(Exception e){
			log.error("Exception in retrieving venue menu details ==>"+e.getMessage())
		}
	}
	
	*//**
	 * Service to retrieve order details
	 * Order timeout, total tax rate
	 * @return order details
	 *//*
	def venueConfigOrders() {
		try{
			if(params.id){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				if(venue){
					[venue:venue]
				}else{
					flash.message = "Venue Not Found"
				}
			}
		}catch(Exception e){
			log.error("Exception in retrieving venue orders details ==>"+e.getMessage())
		}
	}
	
	*//**
	 * Service to retrieve account details
	 * Routing number, account number
	 * @return account details
	 *//*
	def venueConfigBankAccount() {
		try{
			if(params.id){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				if(venue){
					[venue:venue]
				}else{
					flash.message = "Venue Not Found"
				}
			}
		}catch(Exception e){
			log.error("Exception in retrieving venue bank account details ==>"+e.getMessage())
		}
	}
	
	*//**
	 * Service to retrieve wifi details
	 * Wifi present, wifi name, wifi code, authentication, network type
	 * @return wifi details
	 *//*
	def venueConfigWifi() {
		try{
			if(params.id){
				def venueId = params.id
				def venue = Venue.findByVenueId(venueId)
				if(venue){
					[venue:venue]
				}else{
					flash.message = "Venue Not Found"
				}
			}
		}catch(Exception e){
			log.error("Exception in retrieving venue wifi details ==>"+e.getMessage())
		}
	}*/
	
	/**
	 * Service to save venue details like name, address and open hours
	 * @return success/failure message
	 */
	def saveVenueConfig(){
		try{
			if (params.venueId && params.vc){
				def venueId = params.venueId
				def venue = Venue.findByVenueId(venueId)
				if (venue){
					String[] monday, tuesday, wednesday, thursday, friday, saturday, sunday
					
					venue.venueName = params.venueName
					venue.address = params.address
					def openHoursUpd = formatVenueOpenHours(params)
					venue.openHours = openHoursUpd.toString()
					if (!venue.save(flush:true)){
						flash.message = "Problem saving venue details."
					}else{
						flash.message = "Venue details saved successfully."
					}
					if (venue.openHours){
						def openHoursJSON = new JSONObject(venue.openHours)
						monday = parseOpenHours(openHoursJSON.get("Monday"))
						tuesday = parseOpenHours(openHoursJSON.get("Tuesday"))
						wednesday = parseOpenHours(openHoursJSON.get("Wednesday"))
						thursday = parseOpenHours(openHoursJSON.get("Thursday"))
						friday = parseOpenHours(openHoursJSON.get("Friday"))
						saturday = parseOpenHours(openHoursJSON.get("Saturday"))
						sunday = parseOpenHours(openHoursJSON.get("Sunday"))
					}
					render(view:"venueConfig",  model:[venue:venue, mon:monday, tues:tuesday, wed:wednesday, thurs:thursday, fri:friday, sat:saturday, sun:sunday, params:params])
				}else{
					flash.message = "Venue not found."
					render(view:"venueConfig",  model:[venue:venue, params:params])
				}
			}else if (params.venueId && params.mgr){
				def venueId = params.venueId
				def venue = Venue.findByVenueId(venueId)
				if (venue){
					def mgrPassword = params.mgrPassword
					def confirmPwd = params.mgrConfirm
					
					if (!mgrPassword.trim().equals(confirmPwd.trim())){
						flash.message = "Password and confirm password does not match."
						render(view:"venueConfig",  model:[venue:venue, params:params])
						return
					}
					
					venue.managerName = params.mgrName
					venue.managerEmail = params.mgrEmail
					venue.managerPassword = params.mgrPassword
					venue.managerCell = params.mgrCell
					if (!venue.save(flush:true)){
						flash.message = "Problem saving venue details."
					}else{
						flash.message = "Venue details saved successfully."
					}
					render(view:"venueConfig",  model:[venue:venue, params:params])
					return
				}else{
					flash.message = "Venue not found."
					render(view:"venueConfig",  model:[venue:venue, params:params])
					return
				}
			}else if (params.venueId && params.vRep){
				def venueId = params.venueId
				def venue = Venue.findByVenueId(venueId)
				if (venue){
					venue.vendsyRepName = params.vendsyRepName
					venue.vendsyRepEmail = params.vendsyRepEmail
					venue.vendsyRepPhone = params.vendsyRepPhone
					if (!venue.save(flush:true)){
						flash.message = "Problem saving venue details."
					}else{
						flash.message = "Venue details saved successfully."
					}
					render(view:"venueConfig",  model:[venue:venue, params:params])
				}else{
					flash.message = "Venue not found."
					render(view:"venueConfig",  model:[venue:venue, params:params])
				}
			}else if (params.venueId && params.menu){
				def venueId = params.venueId
				def venue = Venue.findByVenueId(venueId)
				if (venue){
					venue.locuId = params.locuId
					venue.locuSection = params.locuSection
					venue.locuUsername = params.locuUsername
					venue.locuPassword = params.locuPassword
					if (!venue.save(flush:true)){
						flash.message = "Problem saving venue details."
					}else{
						flash.message = "Venue details saved successfully."
					}
					render(view:"venueConfig",  model:[venue:venue, params:params])
				}else{
					flash.message = "Venue not found."
					render(view:"venueConfig",  model:[venue:venue, params:params])
				}
			}else if (params.venueId && params.orders){
				def venueId = params.venueId
				def venue = Venue.findByVenueId(venueId)
				if (venue){
					venue.cancelOrderTime = Integer.parseInt(params.cancelOrderTime)
					venue.totalTaxRate = params.totalTaxRate
					if (!venue.save(flush:true)){
						flash.message = "Problem saving venue details."
					}else{
						flash.message = "Venue details saved successfully."
					}
					render(view:"venueConfig",  model:[venue:venue, params:params])
				}else{
					flash.message = "Venue not found."
					render(view:"venueConfig",  model:[venue:venue, params:params])
				}
			}else if (params.venueId && params.bankAcct){
				def venueId = params.venueId
				def venue = Venue.findByVenueId(venueId)
				if (venue){
					venue.routingNumber = params.routingNumber
					venue.accountNumber = params.accountNumber
					if (!venue.save(flush:true)){
						flash.message = "Problem saving venue details."
					}else{
						flash.message = "Venue details saved successfully."
					}
					render(view:"venueConfig",  model:[venue:venue, params:params])
				}else{
					flash.message = "Venue not found."
					render(view:"venueConfig",  model:[venue:venue, params:params])
				}
			}else if (params.venueId && params.wifi){
				def venueId = params.venueId
				def venue = Venue.findByVenueId(venueId)
				if (venue){
					if (params.wifiPresent){
						venue.wifiPresent = 1
					}else{
						venue.wifiPresent = 0
					}
					venue.wifiName = params.wifiName
					venue.wifiPassword = params.wifiPassword
					if (params.authType){
						venue.typeOfAuthentication = params.authType
					}
					if (params.networkType){
						venue.wifiNetworkType = params.networkType
					}
					if (!venue.save(flush:true)){
						flash.message = "Problem saving venue details."
					}else{
						flash.message = "Venue details saved successfully."
					}
					render(view:"venueConfig",  model:[venue:venue, params:params])
				}else{
					flash.message = "Venue not found."
					render(view:"venueConfig",  model:[venue:venue, params:params])
				}
			}
			// Add new venue
			else if (!params.venueId && params.vc){
				String[] monday, tuesday, wednesday, thursday, friday, saturday, sunday
				Venue venue = new Venue()
				venue.venueName = params.venueName
				venue.address = params.address
				def openHoursUpd = formatVenueOpenHours(params)
				venue.openHours = openHoursUpd.toString()
				venue.country = ""
				venue.hasLocuMenu = ""
				venue.locuId = ""
				venue.locuSection = ""
				venue.locuUsername = ""
				venue.locuPassword = ""
				venue.totalTaxRate = ""
				venue.routingNumber = ""
				venue.accountNumber = ""
				venue.lat = ""
				venue.locality = ""
				venue.longtd = ""
				venue.phone = ""
				venue.postalCode = ""
				venue.region = ""
				venue.streetAddress = ""
				venue.websiteURL = ""
				venue.hasBarSection = 0
				venue.facebookURL = ""
				venue.twitterId = ""
				//get the latest venueId from DB and increase it by 1 and set it to the venue object
				def maxId = Venue.createCriteria().get { projections { max "venueId" } } as Long
				if(maxId){
					maxId = maxId+1
				}else{
					maxId = 100001
				}
				venue.venueId = maxId
				venue.wifiName = ""
				venue.wifiPassword = ""
				venue.typeOfAuthentication = ""
				venue.deviceToken = ""
				venue.deviceType = ""
				venue.wifiPresent = 0
				venue.dateCreated = new Date()
				venue.lastUpdated = new Date()
				venue.cancelOrderTime = 0
				venue.status = 'OPEN'
				venue.lastHBResponse = new Date()
				venue.vendsyRepName = ""
				venue.vendsyRepEmail = ""
				venue.vendsyRepPhone = ""
				venue.managerName = ""
				venue.managerEmail = ""
				venue.managerPassword = ""
				venue.managerCell = ""
				venue.venueLogin = ""
				venue.venuePassword = ""
				venue.lastActivity = new Date()
				venue.venueImagePath = ""
				venue.phoneNumber = ""
				venue.description = ""
				venue.communityRating = ""
				venue.wifiNetworkType = ""
				venue.pickupLocations = ""
				venue.deliveryTables = ""
				venue.tableOrdering = ""
				venue.isPickupLocution = ""
				
				if (!venue.save(flush:true)){
					flash.message = "Problem saving venue details."
				}else{
					flash.message = "Venue details saved successfully."
				}
				if (venue.openHours){
					def openHoursJSON = new JSONObject(venue.openHours)
					monday = parseOpenHours(openHoursJSON.get("Monday"))
					tuesday = parseOpenHours(openHoursJSON.get("Tuesday"))
					wednesday = parseOpenHours(openHoursJSON.get("Wednesday"))
					thursday = parseOpenHours(openHoursJSON.get("Thursday"))
					friday = parseOpenHours(openHoursJSON.get("Friday"))
					saturday = parseOpenHours(openHoursJSON.get("Saturday"))
					sunday = parseOpenHours(openHoursJSON.get("Sunday"))
				}
				render(view:"venueConfig",  model:[venue:venue, mon:monday, tues:tuesday, wed:wednesday, thurs:thursday, fri:friday, sat:saturday, sun:sunday, params:params])
			}
		}catch(Exception e){
			println "exception -->"+e.getMessage()
			log.error("Exception in saving venue config details ==>"+e.getMessage())
		}
	}
	
	/**
	 * Method to format venue open hours from request
	 * @param params
	 * @return JSON containing open hours
	 */
	def formatVenueOpenHours(def params){
		def monFromHrs = params.monFromHrs
		def monFromMins = params.monFromMins
		def monToHrs = params.monToHrs
		def monToMins = params.monToMins
		def tuesFromHrs = params.tuesFromHrs
		def tuesFromMins = params.tuesFromMins
		def tuesToHrs = params.tuesToHrs
		def tuesToMins = params.tuesToMins
		def wedFromHrs = params.wedFromHrs
		def wedFromMins = params.wedFromMins
		def wedToHrs = params.wedToHrs
		def wedToMins = params.wedToMins
		def thursFromHrs = params.thursFromHrs
		def thursFromMins = params.thursFromMins
		def thursToHrs = params.thursToHrs
		def thursToMins = params.thursToMins
		def friFromHrs = params.friFromHrs
		def friFromMins = params.friFromMins
		def friToHrs = params.friToHrs
		def friToMins = params.friToMins
		def satFromHrs = params.satFromHrs
		def satFromMins = params.satFromMins
		def satToHrs = params.satToHrs
		def satToMins = params.satToMins
		def sunFromHrs = params.sunFromHrs
		def sunFromMins = params.sunFromMins
		def sunToHrs = params.sunToHrs
		def sunToMins = params.sunToMins
		
		def monStr = monFromHrs.trim()+":"+monFromMins.trim()+":00 - "+monToHrs.trim()+":"+monToMins.trim()+":00"
		def tuesStr = tuesFromHrs.trim()+":"+tuesFromMins.trim()+":00 - "+tuesToHrs.trim()+":"+tuesToMins.trim()+":00"
		def wedStr = wedFromHrs.trim()+":"+wedFromMins.trim()+":00 - "+wedToHrs.trim()+":"+wedToMins.trim()+":00"
		def thursStr = thursFromHrs.trim()+":"+thursFromMins.trim()+":00 - "+thursToHrs.trim()+":"+thursToMins.trim()+":00"
		def friStr = friFromHrs.trim()+":"+friFromMins.trim()+":00 - "+friToHrs.trim()+":"+friToMins.trim()+":00"
		def satStr = satFromHrs.trim()+":"+satFromMins.trim()+":00 - "+satToHrs.trim()+":"+satToMins.trim()+":00"
		def sunStr = sunFromHrs.trim()+":"+sunFromMins.trim()+":00 - "+sunToHrs.trim()+":"+sunToMins.trim()+":00"
		
		def monList = new JSONArray(), tuesList = new JSONArray(), wedList = new JSONArray(), thursList = new JSONArray(), 
		friList = new JSONArray(), satList = new JSONArray(), sunList = new JSONArray()
		
		monList.add(monStr)
		tuesList.add(tuesStr)
		wedList.add(wedStr)
		thursList.add(thursStr)
		friList.add(friStr)
		satList.add(satStr)
		sunList.add(sunStr)
		
		def jsonObj = new JSONObject()
		jsonObj.put("Monday", monList)
		jsonObj.put("Tuesday", tuesList)
		jsonObj.put("Wednesday", wedList)
		jsonObj.put("Thursday", thursList)
		jsonObj.put("Friday", friList)
		jsonObj.put("Saturday", satList)
		jsonObj.put("Sunday", sunList)
		
		return jsonObj
	}
	
	/**
	 * Service to delete a venue
	 * @return success/failure
	 */
	def deleteVenue(){
		try{
			if (params.id){
				def venue = Venue.findByVenueId(params.id)
				if (venue){
					venue.status = 'DELETED'
					if (!venue.save(flush:true)){
						flash.message = "Problem deleting venue."
					}else{
						flash.message = "Venue deleted successfully."
					}
				}else{
					flash.message = "Venue not found"
				}
				render(view:"venueList")
			}
		}catch(Exception e){
			log.error("Exception deleting a venue ==>"+e.getMessage())
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
		try{
			/*Date  startDate, endDate
			Date date = new Date()
			DateFormat jQdateFormat = new SimpleDateFormat("MM/dd/yyyy")
			DateFormat dateFormatn = new SimpleDateFormat("yyyy-MM-dd")
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh")
			
			def tradeTime = BartsyConfiguration.findByConfigName("tradingDay")
			if(params.startDate){
				startDate = dateFormat.parse(dateFormatn.format(new Date(params.startDate))+" "+tradeTime.value)
			}else{
				 startDate = dateFormat.parse(dateFormatn.format(date)+" "+tradeTime.value)
			}
			if(params.endDate){
				endDate = dateFormat.parse(dateFormatn.format(new Date(params.endDate))+" "+tradeTime.value)
			}else{
				endDate = new Date(startDate.getTime() + (1000 * 60 * 60 * 24));
			}
			def jqStart = jQdateFormat.format(startDate)
			def jqEnd = jQdateFormat.format(endDate)
			def query = {
				between("dateCreated", startDate, endDate)
				order "id", "desc"
			}*/
			
			// Total guests
			def totalGuests = UserProfile.getAll()
			
			// Get the checked in users list
			def checkedInUsers = CheckedInUsers.findAllByStatus(1)
			
			// Calculate Base, Tax, Comps, Comps% and Total
			def fTotalBase, fTotalTax, fTotalComps, tipTotCompPer, fTotalCompPer, fTotal
			def formattedAvgBase, formattedAvgTax, formattedAvgComp, formattedAvgCompPer, formattedAvgTot
			def fPerGuestGrossTotal, fPerGuestTaxTotal, fPerGuestCompTotal, fPerGuestCompPerTotal, fPerGuestNetTotal
			def guests = 0
			def totalBase = 0, totalTax = 0, compTotal = 0, compPercentTot = 0, total = 0
			def avgBase = 0, avgTax = 0, avgComp = 0, avgCompPer = 0, avgTotal = 0
			Set uniqueGuest = new HashSet()
			
			//def orderslist = Orders.createCriteria().list(params, query)
			def orderslist = Orders.list()
			def ordersCount = orderslist.size()
			if(orderslist){
				orderslist.each {
					def order = it
					// To retrieve items from orderItems for each order
					def orderItemsList = OrderItems.createCriteria().list {
						eq("order", order)
					}
					def gross = 0
					if (orderItemsList){
						orderItemsList.each {
							def orderItem = it
							def basePrice = orderItem.basePrice
							if (!basePrice){
								basePrice = 0
							}
							def finalBasePrice = formatNumStr(basePrice)
							Double base = new Double(finalBasePrice)
							// total base price
							totalBase += base
							gross += base
						}
					}
					// total tax
					def taxCalc = order.venue.totalTaxRate
					if (!taxCalc){
						taxCalc = 0
					}
					totalTax += (gross * new Double(taxCalc)) / 100
					
					// comp = tip 
					def tip = order.tipPercentage
					if (!tip){
						tip = 0
					}
					compTotal += new Double(tip)
					
					// comp = tip percentage
					def compPer
					if (gross > 0){
						compPer = (100 * new Double(tip)) / gross
					}else{
						compPer = 0
					}
					compPercentTot += compPer
					
					// Totals
					def totPrice = order.totalPrice
					if (!totPrice){
						totPrice = 0
					}
					total += new Double(totPrice)
					
					// unique guests
					if (uniqueGuest.add(order.userId)){
						guests++
					}
				}
				
				fTotalBase = formatNumStr(totalBase.toString())
				fTotalTax = formatNumStr(totalTax.toString())
				fTotalComps = formatNumStr(compTotal.toString())
				tipTotCompPer = compPercentTot / ordersCount
				fTotalCompPer = formatTipNumStr(tipTotCompPer.toString())
				fTotal = formatNumStr(total.toString())
				
				// Calculate average
				avgBase = totalBase / ordersCount
				avgTax = totalTax / ordersCount
				avgComp = compTotal / ordersCount
				avgCompPer = compPercentTot / ordersCount
				avgTotal = total / ordersCount
				
				formattedAvgBase = formatNumStr(avgBase.toString())
				formattedAvgTax = formatNumStr(avgTax.toString())
				formattedAvgComp = formatNumStr(avgComp.toString())
				formattedAvgCompPer = formatTipNumStr(avgCompPer.toString())
				formattedAvgTot = formatNumStr(avgTotal.toString())
				
				// Per guest calculation
				// gross total per guest
				def perGuestGrossTot = new Double(fTotalBase) / guests
				fPerGuestGrossTotal = formatNumStr(perGuestGrossTot.toString())
				
				// tax total per guest
				def perGuestTaxTot = new Double(fTotalTax) / guests
				fPerGuestTaxTotal = formatNumStr(perGuestTaxTot.toString())
				
				// comp total per guest
				def perGuestCompTot = new Double(fTotalComps) / guests
				fPerGuestCompTotal = formatNumStr(perGuestCompTot.toString())
				
				// comp percentage total per guest
				def perGuestCompPerTot = compPercentTot / guests
				fPerGuestCompPerTotal = formatTipNumStr(perGuestCompPerTot.toString())
				
				// net total per guest
				def perGuestNetTot = new Double(fTotal) / guests
				fPerGuestNetTotal = formatNumStr(perGuestNetTot.toString())
			}
			
			[totalGuests:totalGuests.size(), totalChecks:checkedInUsers.size(), guestsAvg:12.26, checksAvg:22.22,
				base:fTotalBase, tax:fTotalTax, comps:fTotalComps, compsPer:fTotalCompPer, totals:fTotal,
				avgBase:formattedAvgBase, avgTax:formattedAvgTax, avgComp:formattedAvgComp, avgCompPer:formattedAvgCompPer, avgTot:formattedAvgTot,
				perGuestBase:fPerGuestGrossTotal, perGuestTax:fPerGuestTaxTotal, perGuestComp:fPerGuestCompTotal, perGuestCompPer:fPerGuestCompPerTotal, perGuestNet:fPerGuestNetTotal]
		}catch(Exception e){
			log.error("Exception in retrieving summary data ==>"+e.getMessage())
		}
	}
	
	def categories() {
		
	}
	
	/**
	 * Service to show user profile information
	 * @return user info.
	 */
	def userDetails() {
		try{
			if(params.id){
				def userProfile = UserProfile.findByBartsyId(params.id)
				render(view:"userDetails", model:[selectedUser:userProfile])
			}
		}catch(Exception e){
			log.error("Error in retrieving user details ==>"+e.getMessage())
		}
	}
	
	/**
	 * Service to get venue details
	 * @return venue info.
	 */
	def venueDetails() {
		try{
			if(params.id){
				def venue = Venue.findByVenueId(params.id)
				render(view:"venueDetails", model:[venue:venue])
			}
		}catch(Exception e){
			log.error("Error in retrieving venue details ==>"+e.getMessage())
		}
	}
}

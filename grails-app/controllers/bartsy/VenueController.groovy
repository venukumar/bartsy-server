package bartsy

import grails.converters.JSON
import org.json.simple.parser.JSONParser

/**
 * This is the controller which has all Venue related actions to be performed.
 *
 * @author Swetha Bhatnagar
 *
 **/
class VenueController {

	def inventoryService

	/**
	 * This is a test webservice to save venue details from a hardcoded locu response. To be removed later. 
	 */
	def saveVenueDetailsTest = {
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def resources = grailsApplication.mainContext.getResource("response.txt").file
				def fileContents = resources.text
				def parsedData = JSON.parse(fileContents)
				def hasBarSection = 0
				def menu
				Venue venue = Venue.getAll()
				//Venue venue = Venue.findByLocuId(json.locuId)
				if(venue){
					venue.cancelOrderTime = json.cancelOrderTime as int
					venue.deviceToken = json.deviceToken
					venue.lastHBResponse =  new Date()
					venue.save()
					response.put("venueId",venue.getVenueId())
					response.put("venueName",venue.getVenueName())
					response.put("errorCode","1")
					response.put("errorMessage","Venue Details Updated")
				}
				else{
					parsedData.objects.menus.each {
						// println "iterator "+it
						def parsedData1 = it
						parsedData1.each{
							def parsedData2 = it
							if("Bar".equals(parsedData2.menu_name.toString()))
							{
								hasBarSection = 1
								menu = URLEncoder.encode(parsedData2.sections.toString(),"UTF-8")
								//println menu
								//saveDrinks(menu.toString())
							}
						}
					}
					venue= new Venue()
					venue.venueName = parsedData.objects[0].name
					venue.lat = parsedData.objects[0].lat
					venue.longtd = parsedData.objects[0].long
					venue.phone = parsedData.objects[0].phone
					venue.region = parsedData.objects[0].region
					venue.locuId = parsedData.objects[0].id
					venue.postalCode = parsedData.objects[0].postal_code
					venue.locality = parsedData.objects[0].locality
					venue.streetAddress = parsedData.objects[0].street_address
					venue.websiteURL = parsedData.objects[0].website_url
					venue.country = parsedData.objects[0].country
					venue.hasLocuMenu = parsedData.objects[0].has_menu
					venue.hasBarSection = hasBarSection
					venue.twitterId = parsedData.objects[0].twitter_id
					venue.facebookURL = parsedData.objects[0].facebook_url
					venue.openHours = parsedData.objects[0].open_hours
					venue.cancelOrderTime = json.cancelOrderTime as int
					venue.wifiPresent = json.wifiPresent
					if(json.wifiPresent == 1){
						venue.wifiName = json.wifiName
						venue.wifiPassword = json.wifiPassword
						venue.typeOfAuthentication = json.typeOfAuthentication
					}
					else{
						venue.wifiName = "XYZ"
						venue.wifiPassword = "XYZ"
						venue.typeOfAuthentication = "XYZ"
					}
					if(hasBarSection){
						venue.menu = menu
					}
					venue.deviceToken = json.deviceToken
					venue.deviceType = json.deviceType
					venue.paypalId = json.paypalId
					venue.status = "OPEN"
					venue.lastHBResponse = new Date()
					venue.lastActivity =  new Date()
					def maxId = Venue.createCriteria().get { projections { max "venueId" } } as Long
					if(maxId){
						maxId = maxId+1
					}
					else{
						maxId = 100001
					}
					venue.venueId = maxId

					if(venue.save()){
						response.put("venueId",maxId)
						response.put("venueName",venue.getVenueName())
						response.put("errorCode","0")
						response.put("errorMessage","Save Successful")
					}
					else{
						response.put("errorCode","1")
						response.put("errorMessage","Save not Successful")
					}
				}
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}

	/**
	 * This is the webservice to save the venue registration details received from the bartender application.
	 * 
	 * @author Swetha Bhatnagar
	 *
	 **/
	def saveVenueDetails = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request.details)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def venueImageFile = request.getFile("venueImage")

				def parsedData
				//check for the hardCoded locu Id for Finn McCool
				/*if(json.locuId.equals("beec9320f3921035e4d7")){
					//get the locu response into parsedData varibale for this venue from the harcoded locu response file
					def resources = grailsApplication.mainContext.getResource("response.txt").file
					def fileContents = resources.text
					parsedData = JSON.parse(fileContents)
				}
				//if not Finn McCool go to else part
				else{*/
					//get the locu response for that locuID into the parsedData varibale
					def url = message(code:'app.locu.url')+json.locuId+'/?api_key='+message(code:'app.locu.apikey')
					parsedData = JSON.parse( new URL(url).text )
					
				//}
				def hasBarSection = 0
				def menu
				//Get the venue based on the locu Id sent in the request
				Venue venue = Venue.findByLocuId(json.locuId)
				//if venue exists as of now updating the deviceToken and cancelOrderTime. To be changed later
				if(venue){
					if(venueImageFile){
						def venueImagePath = saveVenueImage(venueImageFile, venue.venueId)
						venue.setVenueImagePath(venueImagePath)
					}
					//venue.venueImagePath=venueImagePath
					venue.deviceToken = json.deviceToken
					venue.cancelOrderTime = json.cancelOrderTime as int
					venue.save()
					response.put("venueId",venue.getVenueId())
					response.put("venueName",venue.getVenueName())
					response.put("errorCode","1")
					response.put("errorMessage","Venue Details Updated")
				}
				//If venue does not exist go to else part
				else{
					// if menu not found then reading menu from file
					if(!parsedData.objects?.menus){
						def resources = grailsApplication.mainContext.getResource("response.txt").file
						def fileContents = resources.text
						parsedData = JSON.parse(fileContents)
					}
					//The following group of code is to parse the locu response and identify if it has a BAR section in its menu
					parsedData.objects.menus.each {
						def parsedData1 = it
						parsedData1.each{
							def parsedData2 = it
							if(message(code:'bar').equals(parsedData2.menu_name.toString()))
							{
								//set this varibale to 1 if BAR section is there and encode the BAR menu JSON
								hasBarSection = 1
								menu = URLEncoder.encode(parsedData2.sections.toString(),"UTF-8")
							}
						}
					}
					//As venue does not exist create a new venue object
					venue= new Venue()
					//set the values to venue object from the locu response
					venue.venueName = parsedData.objects[0].name
					venue.lat = parsedData.objects[0].lat
					venue.longtd = parsedData.objects[0].long
					venue.phone = parsedData.objects[0].phone
					venue.region = parsedData.objects[0].region
					venue.locuId = parsedData.objects[0].id
					venue.postalCode = parsedData.objects[0].postal_code
					venue.locality = parsedData.objects[0].locality
					venue.streetAddress = parsedData.objects[0].street_address
					venue.websiteURL = parsedData.objects[0].website_url
					venue.country = parsedData.objects[0].country
					venue.hasLocuMenu = parsedData.objects[0].has_menu
					venue.twitterId = parsedData.objects[0].twitter_id
					venue.facebookURL = parsedData.objects[0].facebook_url
					venue.openHours = parsedData.objects[0].open_hours
					//set the values of menu and hasBarSection based on the value set earlier
					venue.hasBarSection = hasBarSection
					if(hasBarSection){
						venue.menu = menu
					}
					//set the values received in the request to this syscall
					venue.cancelOrderTime = json.cancelOrderTime as int
					venue.wifiPresent = json.wifiPresent
					if(json.wifiPresent == 1){
						venue.wifiName = json.wifiName
						venue.wifiPassword = json.wifiPassword
						venue.typeOfAuthentication = json.typeOfAuthentication
					}
					else{
						venue.wifiName = "XYZ"
						venue.wifiPassword = "XYZ"
						venue.typeOfAuthentication = "XYZ"
					}
					venue.deviceToken = json.deviceToken
					venue.deviceType = json.deviceType
					venue.paypalId = json.paypalId
					//set the value of status as OPEN by default while registration
					venue.status = "OPEN"
					//inititally we set these values as current time...Later they get updated accordingly
					venue.lastHBResponse = new Date()
					venue.lastActivity =  new Date()
					//get the latest venueId from DB and increase it by 1 and set it to the venue object
					def maxId = Venue.createCriteria().get { projections { max "venueId" } } as Long
					if(maxId){
						maxId = maxId+1
					}
					else{
						maxId = 100001
					}
					venue.venueId = maxId
					if(venueImageFile){
						def venueImagePath = saveVenueImage(venueImageFile, maxId)
						venue.setVenueImagePath(venueImagePath)
					}
					//save the venue object to the DB
					if(venue.save()){
						//if save successful send the following details as response with errorCode 0
						response.put("venueId",maxId)
						response.put("venueName",venue.getVenueName())
						response.put("errorCode","0")
						response.put("errorMessage",message(code:'venue.save'))
					}
					else{
						//if save not successful send the following details as response with errorCode 1
						response.put("errorCode","1")
						response.put("errorMessage",message(code:'venue.save.failed'))
					}
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}

	/**
	 * 
	 * To save venue image
	 * 
	 * @param venueImageFile
	 * @param venueImageName
	 * @return
	 */
	def saveVenueImage(venueImageFile,venueImageName){
		def venueImagePath
		if(venueImageFile){
			def webRootDir = servletContext.getRealPath("/")
			def venueDir = new File(message(code:'venueimage.path'))
			venueDir.mkdirs()
			String tmp = venueImageName.toString()
			venueImageFile.transferTo( new File( venueDir, tmp))
			venueImagePath = message(code:'venueimage.path.save')+tmp
		}
		return venueImagePath
	}

	/**
	 * This is the webservice to return the menu received from locu for a venue.
	 * 
	 * @author Swetha Bhatnagar
	 *
	 **/
	def getMenu = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				//retrieve the venueId from the parsed request and retrieve the venue based on venueId
				def venueId = json.venueId
				def venue = Venue.findByVenueId(venueId)
				//check if venue exists with that venueId
				if(venue){
					//if venue exists retrieve the menu from that venue object and decode it to get the menu in JSON format
					def menuJson = JSON.parse(URLDecoder.decode(venue.menu))
					//send back the menu as response along with errorCode 0
					response.put("menu",menuJson)
					response.put("errorCode","0")
					response.put("errorMessage",message(code:'venue.exists'))
				}
				else{
					//if venue does not exist then send back errorCode 1
					response.put("errorCode","1")
					response.put("errorMessage",message(code:'venue.not.exists'))
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON,contentType:"application/json")
	}

	def getMenuOld = {
		def response = [:]
		try{
			def json = JSON.parse(request)
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def venueId = json.venueId
				def venue = Venue.findByVenueId(venueId)
				if(venue){
					def menuJson = []
					if(venue.hasBarSection){
						menuJson = JSON.parse(URLDecoder.decode(venue.menu))
					}
					def cocktails = Cocktails.findAllByVenue(venue)
					if(cocktails){
						def sectionMap = [:]
						sectionMap.put("section_name","Custom Cocktails")
						def subSectionsList = []
						def subSectionMap = [:]
						def contentsList = []
						def photos = []
						def option_groups = []
						subSectionMap.put("subsection_name","")
						cocktails.each{
							def cocktail = it
							if(cocktail.getAvailable().toBoolean()){
								def cocktailMap=[:]
								// added all cocktail details cocktailsMap
								cocktailMap.put("photos", photos)
								cocktailMap.put("name", cocktail.name)
								cocktailMap.put("description", "")
								cocktailMap.put("option_groups", option_groups)
								cocktailMap.put("price", cocktail.price)
								cocktailMap.put("type", "ITEM")
								// stored every cocktailsMap into cocktailsList
								contentsList.add(cocktailMap)
							}
						}
						subSectionMap.put("contents",contentsList)
						subSectionsList.add(subSectionMap)
						sectionMap.put("subsections",subSectionsList)
						menuJson.add(sectionMap)
					}
					response.put("menu",menuJson)
					response.put("errorCode","0")
					response.put("errorMessage",message(code:'venue.exists'))
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage",message(code:'venue.not.exists'))
				}
			}
			else{
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON,contentType:"application/json")
	}


	/**
	 * This is the webservice to return the list of registered venues.
	 * 
	 * @author Swetha Bhatnagar
	 *
	 **/
	def getVenueList = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				//get the list of venue Objects from DB
				def venueList = Venue.getAll()
				//get the user profile object based on bartsyId sent in the request to the syscall
				def userProfile = UserProfile.findByBartsyId(json.bartsyId.toString())
				//define a list to add venue Objects
				def totalVenueList = []
				//check if atleast one venue exists
				if(venueList){
					//loop though the venue list
					venueList.each{
						def venue = it
						//Find the list of checkedIn users for that venue
						def checkedInUsers = CheckedInUsers.findAllByVenueAndStatus(venue,1)
						//varibale to count number of private checked in users
						def privateUsers = 0
						//loop through the checkedin users list and check for private users
						checkedInUsers.each{
							def checkedInUser = it
							if(checkedInUser.userProfile.getShowProfile().equals("OFF")){
								privateUsers = privateUsers+1
							}
						}
						//check if the user placed an order in that venue
						def orders = Orders.findAllByUser(userProfile)
						//defining a map to store the venue object details
						def venueMap = [:]
						//if order placed mark the venue as unlocked else locked
						if(orders){
							venueMap.put("unlocked","true")
						}
						else{
							venueMap.put("unlocked","false")
						}
						venueMap.put("checkedInUsers",checkedInUsers.size())
						venueMap.put("privateUsers",privateUsers)
						venueMap.put("venueName",venue.getVenueName())
						venueMap.put("venueId",venue.getVenueId())
						venueMap.put("latitude",venue.getLat())
						venueMap.put("longitude",venue.getLongtd())
						venueMap.put("venueStatus",venue.getStatus())
						venueMap.put("wifiPresent",venue.getWifiPresent().toString())
						venueMap.put("wifiName",venue.getWifiName())
						venueMap.put("wifiPassword",venue.getWifiPassword())
						venueMap.put("typeOfAuthentication",venue.getTypeOfAuthentication())
						def address = venue.getStreetAddress()+","+venue.getLocality()+","+venue.getCountry()+","+venue.getPostalCode()
						venueMap.put("address",address)
						venueMap.put("cancelOrderTime",venue.getCancelOrderTime())
						//add the venue object to the list defined earlier
						totalVenueList.add(venueMap)
					}
					//send the list as a response to the syscall
					render(text:totalVenueList as JSON,contentType:"application/json")
				}
				else{
					response.put("errorCode","1")
					response.put("errorMessage","No Venues Available")
					render(text:response as JSON,contentType:"application/json")
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
				render(text:response as JSON,contentType:"application/json")
			}
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
			render(text:response as JSON,contentType:"application/json")
		}

	}

	//	def startTimer(){
	//		def timer = BartsyConfiguration.findByConfigName("timer")
	//		boolean flag = timer.value.toBoolean()
	//		if(!flag){ //value should be false initially
	//			timer.setValue("true")
	//			timer.save(flush:true)
	//
	//		}
	//	}

	/**
	 * This is the webservice to be called by the bartender app when they receive a heartbeat PN.
	 *
	 * @author Swetha Bhatnagar
	 *
	 **/
	def heartBeatVenue = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				//look for a venue based on the venueId sent in the request
				def venue = Venue.findByVenueId(json.venueId)
				if(venue){
					//if venue exists update the column lastHBResponse for venue object to current date time and if venue is in OFFLINE state move it to OPEN state
					venue.setLastHBResponse(new Date())
					if(venue.status.equals("OFFLINE"))
					{
						venue.status =  "OPEN"
					}
					//save the venue object with updated lastHBResponse column value
					venue.save(flush:true)
					//send errorCode 0 as confirmation of request has been received
					response.put("errorCode","0")
					response.put("errorMessage","Request Received")
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
					response.put("messageType","heartBeat")
					response.put("checkedInUsersList",checkedInUsersList)
					response.put("ordersList",ordersList)
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}

	/**
	 * This is the webservice to be called to change the status of a venue.
	 *
	 * @author Swetha Bhatnagar
	 *
	 **/
	def setVenueStatus = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				//look for a venue based on the venueId sent in the request
				def venue = Venue.findByVenueId(json.venueId)
				if(venue){
					//if venue exists set the status of the venue object with the status sent in the request to the syscall
					venue.setStatus(json.status)
					//save the venue object
					venue.save(flush:true)
					//send the errorCode 0 as acknowledgement of the status save
					response.put("errorCode","0")
					response.put("errorMessage","Save Successful")
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON ,  contentType:"application/json")
	}
}

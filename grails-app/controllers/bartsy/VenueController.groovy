package bartsy

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * This is the controller which has all Venue related actions to be performed.
 *
 * @author Swetha Bhatnagar
 *
 **/
class VenueController {

	def inventoryService
	def androidPNService
	def applePNService
	def grailsApplication
	def venueService

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
			def json
			if(params.details)
				json = JSON.parse(params.details)
			else
				json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toString().equalsIgnoreCase(json.apiVersion.toString())){
				def venueImageFile
				if(params.venueImage){
					venueImageFile = request.getFile("venueImage")
				}
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
				// if menu not found then reading menu from file
				if(!parsedData.objects?.menus){
					//def resources = grailsApplication.mainContext.getResource("response.txt").file
					//def fileContents = resources.text
					def fileContents = new File('/usr/response.txt').getText('UTF-8')
					parsedData = JSON.parse(fileContents)
				}
				def locuMenu
				if(parsedData){
					//The following group of code is to parse the locu response and identify if it has a BAR section in its menu
					locuMenu=URLEncoder.encode(parsedData.objects.toString(),"UTF-8")
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
				}
				//Get the venue based on the locu Id sent in the request
				Venue venue = Venue.findByLocuId(json.locuId)
				//if venue exists as of now updating the deviceToken and cancelOrderTime. To be changed later
				if(venue){
					venue.deviceToken = json.deviceToken
					venue.cancelOrderTime = json.cancelOrderTime as int
					if(json.has("venueName")&&json.venueName.trim())
						venue.venueName =json.venueName
					venue.lat = json.has("latitude")?json.latitude:venue.lat
					venue.longtd = json.has("longitude")?json.longitude:venue.longtd
					venue.phone = json.has("phone")?json.phone:venue.phone
					venue.locuId = json.has("locuId")?json.locuId:venue.locuId
					venue.wifiNetworkType=json.has("wifiNetworkType")?json.wifiNetworkType:""
					setValuesToVenue(venue,json)
					if(venueImageFile){
						def venueImagePath = saveVenueImage(venueImageFile, venue.venueId)
						venue.setVenueImagePath(venueImagePath)
					}
					else{
						venue.setVenueImagePath(null)
					}
					venue.locuMenu=locuMenu
					venue.hasBarSection = hasBarSection
					if(hasBarSection){
						venue.menu = menu
					}
					if(json.phoneNumber)
						venue.phoneNumber=json.phoneNumber
					if(json.description)
						venue.description=json.description
					if(json.communityRating)
						venue.communityRating=json.communityRating
					if(json.has("tableOrdering"))
						venue.tableOrdering=json.tableOrdering
					if(json.has("tables"))
						venue.tables=json.tables
					if(json.has("pickupLocation")){
						venue.pickupLocation=json.pickupLocation
					}

					//venue.venueImagePath=venueImagePath

					if(venue.save(flush:true)){
						response.put("venueId",venue.getVenueId())
						response.put("venueName",venue.getVenueName())
						response.put("venueImagePath",venue.venueImagePath)
						response.put("errorCode","0")
						response.put("errorMessage","Venue Details Updated")

						// Parsing loc menu
						parseAndSaveLocuMenuItems(venue)

					}else{
						response.put("venueId",venue.getVenueId())
						response.put("venueName",venue.getVenueName())
						response.put("venueImagePath",venue.venueImagePath)
						response.put("errorCode","1")
						response.put("errorMessage","Venue exists but details are not updated")
					}
				}
				//If venue does not exist go to else part
				else{
					//As venue does not exist create a new venue object
					venue= new Venue()
					//set the values to venue object from the locu response
					setValuesToVenue(venue,json)
					//set the values to venue object from the locu response

					if(json.has("venueName")&&json.venueName.trim())
						venue.venueName =json.venueName
					else
						venue.venueName=parsedData?parsedData.objects[0].name:""
					if(json.has("latitude")&&json.latitude)
						venue.lat =json.latitude
					else
						venue.lat =parsedData?parsedData.objects[0].lat:""
					if(json.has("longitude")&&json.longitude)
						venue.longtd = json.longitude
					else
						venue.longtd =parsedData?parsedData.objects[0].long:""
					if(json.has("phone")&&json.phone)
						venue.phone =json.phone
					else
						venue.phone =parsedData?parsedData.objects[0].phone:""
					if(json.has("locuId")&&json.locuId)
						venue.locuId =json.locuId
					else
						venue.locuId =parsedData?parsedData.objects[0].id:""
					println "open"
					if(json.has("open_hours")&&json.open_hours)
						venue.openHours =json.open_hours.toString()
					else
						venue.openHours =parsedData?parsedData.objects[0].open_hours:""
					venue.wifiNetworkType=json.has("wifiNetworkType")?json.wifiNetworkType:""

					if(json.has("tableOrdering"))
						venue.tableOrdering=json.tableOrdering
					if(json.has("tables"))
						venue.tables=json.tables
					if(json.has("pickupLocation")){
						venue.pickupLocation=json.pickupLocation
					}

					/*	venue.venueName = json.has("venueName")?(json.venueName?json.venueName:parsedData?parsedData.objects[0].name:""):(parsedData?parsedData.objects[0].name:"")
					 venue.venueName = json.has("venueName")?json.venueName:(parsedData?parsedData.objects[0].name:"")
					 venue.lat = json.has("latitude")?json.latitude:(parsedData?parsedData.objects[0].lat:"")
					 venue.longtd = json.has("longitude")?json.longitude:(parsedData?parsedData.objects[0].long:"")
					 venue.phone = json.has("phone")?json.phone:(parsedData?parsedData.objects[0].phone:"")
					 venue.region = parsedData?parsedData.objects[0].region:""
					 venue.locuId = json.has("locuId")?json.locuId:(parsedData?parsedData.objects[0].id:"")
					 venue.openHours =json.has("openHours")?json.openHours:(parsedData?parsedData.objects[0].open_hours:"")*/

					venue.region = parsedData?parsedData.objects[0].region:""
					venue.postalCode = parsedData?parsedData.objects[0].postal_code:""
					venue.locality = parsedData?parsedData.objects[0].locality:""
					venue.streetAddress = parsedData?parsedData.objects[0].street_address:""
					venue.websiteURL =parsedData?parsedData.objects[0].website_url:""
					venue.country = parsedData?parsedData.objects[0].country:""
					venue.hasLocuMenu = parsedData?parsedData.objects[0].has_menu:""
					venue.twitterId = parsedData?parsedData.objects[0].twitter_id:""
					venue.facebookURL = parsedData?parsedData.objects[0].facebook_url:""

					def address
					if(json.has("address")){
						address=json.address
					}
					else{
						address = venue.getStreetAddress()+","+venue.getLocality()+","+venue.getCountry()+","+venue.getPostalCode()
					}
					venue.address=address
					/*venue.venueName = parsedData.objects[0].name
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
					 venue.openHours = parsedData.objects[0].open_hours*/
					//set the values of menu and hasBarSection based on the value set earlier
					venue.hasBarSection = hasBarSection
					if(hasBarSection){
						venue.menu = menu
					}
					if(json.phoneNumber)
						venue.phoneNumber=json.phoneNumber
					if(json.description)
						venue.description=json.description
					if(json.communityRating)
						venue.communityRating=json.communityRating
					venue.locuMenu=locuMenu
					venue.cancelOrderTime = json.cancelOrderTime as int
					//set the values received in the request to this syscall
					/*	venue.cancelOrderTime = json.cancelOrderTime as int
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
					 //set the value of status as OPEN by default while registration
					 venue.status = "OPEN"*/
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
						response.put("venueImagePath",venue.venueImagePath)
						response.put("errorCode","0")
						response.put("errorMessage",message(code:'venue.save'))
						// Parsing loc menu
						parseAndSaveLocuMenuItems(venue.menu)
					}
					else{
						//if save not successful send the following details as response with errorCode 1
						println "error "+venue.errors
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
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		finally{
			println"response "+response
			render(text:response as JSON ,  contentType:"application/json")
		}
	}
	/**
	 * 
	 * @param Venue
	 * @return
	 */
	def parseAndSaveLocuMenuItems(Venue venue){
		try{
			if(venue){
				if(venue.locuMenu){
					def locuData = JSON.parse(URLDecoder.decode(venue.locuMenu))
					if(locuData){
						locuData.each {
							def  menuData = it
							def menus = menuData.menus
							if(menus){
								menus.each {
									def locuMenu = it
									def menuName = locuMenu.menu_name
									if(menuName){
										menuParsing(menuName,locuMenu,venue)
									}
								}
							}
						}
					}
				}
			}

		}catch(Exception e){
			println"exception found in parseAndSaveLocuMenuItems "+e.getMessage()
			log.info("exception found in parseAndSaveLocuMenuItems "+e.getMessage())
		}
	}

	def menuParsing(String menuName,locuMenu,venue){
		def menu = LocuMenuName.findByMenuName(menuName)
		if(menu){
		}else{
			menu = new LocuMenuName()
			menu.setMenuName(menuName)
			menu.save(flush:true)
		}
		def sections = locuMenu.sections
		if(sections){
			sections.each {
				def section = it
				def subSections = section.subsections
				if(subSections){
					subSections.each {
						def subsection = it
						def contents = subsection.contents
						if(contents){
							contents.each{
								def item = it
								def text = item.text
								def type = item.type
								def price = item.price
								def name= item.name
								def description = item.description

								def option_groups = item.option_groups
								def options

								if(option_groups){
									option_groups.each {
										def option = it
										text = option.text
										type = option.type
										options=option.options
									}
								}
								def locuMenuItems = new LocuMenuItems()

								if(!locuMenuItems){
									locuMenuItems = new LocuMenuItems()
								}

								locuMenuItems.setText(text)
								locuMenuItems.setType(type)
								locuMenuItems.setName(name)
								locuMenuItems.setPrice(price)
								locuMenuItems.setDescription(description)
								locuMenuItems.setOption_groups(option_groups?option_groups.toString():"")
								locuMenuItems.setOptions(options?options.toString():"")
								locuMenuItems.setLocuMenu(menu)
								locuMenuItems.setVenue(venue)
								locuMenuItems.save(flush:true)
							}
						}
					}
				}
			}
		}
	}

	def getVenueDetails={
		def response=[:]
		CommonMethods common = new CommonMethods()
		try{
			def json = JSON.parse(request)
			if(json.apiVersion){
				def apiVersion = BartsyConfiguration.findByConfigName("apiVersion").value
				if(json.apiVersion.toString().equalsIgnoreCase(apiVersion)){
					if(json.venueId){

						def venue = Venue.findByVenueId(json.venueId.toString())
						if(venue){
							def venueMap=[:]
							venueMap.put("venueName",venue.getVenueName())
							venueMap.put("venueId",venue.getVenueId())
							venueMap.put("venueImagePath",venue.venueImagePath)
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
							venueMap.put("totalTaxRate",venue.totalTaxRate)
							venueMap.put("currentTime",new Date().toGMTString())
							venueMap.put("wifiNetworkType",venue.wifiNetworkType)
							venueMap.put("tableOrdering",venue.tableOrdering ?: Boolean.FALSE)
							if(venue.tables)
								venueMap.put("tables",new JSONArray(venue.tables))
							venueMap.put("pickupLocation",venue.pickupLocation ?: "")
							venueMap.put("open_hours",venue.openHours ?: "")
							common.response(0, response, "venue Details Available")
							response.put("venueDetails",venueMap)
						}else{
							common.response(3, response, "venue doesn't exist")
						}
					}else{
						common.response(2, response, "VenueId is missing in your request")
					}
				}else{
					common.response(100, response, "Apiversion doesn't match")
				}
			}else{
				common.response(1, response, "Apiversion is missing in your request")
			}

		}catch(Exception e){
			println "Exception found in getVenueDetails "+e.getMessage()
			log.info("Exception found in getVenueDetails "+e.getMessage())
			common.exceptionFound(e, response)
			response.put("currentTime",new Date().toGMTString())
			render(text:response as JSON,contentType:"application/json")
		}
		response.put("currentTime",new Date().toGMTString())
		render(text:response as JSON,contentType:"application/json")

	}

	def setValuesToVenue(Venue venue,json){

		venue.locuSection = json.has("locuSection")?json.locuSection:venue.locuSection
		venue.totalTaxRate = json.has("totalTaxRate")?json.totalTaxRate:venue.totalTaxRate
		venue.routingNumber= json.has("routingNumber")?json.routingNumber:venue.routingNumber
		venue.accountNumber= json.has("accountNumber")?json.accountNumber:venue.accountNumber
		venue.managerName=json.has("managerName")?json.managerName:venue.managerName
		venue.venueLogin=json.has("venueLogin")?json.venueLogin:venue.venueLogin
		venue.venuePassword=json.has("venuePassword")?json.venuePassword:venue.venuePassword
		venue.vendsyRepName=json.has("vendsyRepName")?json.vendsyRepName:venue.vendsyRepName
		venue.vendsyRepEmail=json.has("vendsyRepEmail")?json.vendsyRepEmail:venue.vendsyRepEmail
		venue.vendsyRepPhone=json.has("vendsyRepPhone")?json.vendsyRepPhone:venue.vendsyRepPhone
		venue.openHours =json.has("openHours")?json.openHours:venue.openHours

		venue.wifiPresent = Integer.parseInt(json.wifiPresent)
		if(Integer.parseInt(json.wifiPresent) == 1){
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

		//set the value of status as OPEN by default while registration
		venue.status = "OPEN"

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
			def venueDir = new File(grailsApplication.config.venueimage.path)
			venueDir.mkdirs()
			String tmp = venueImageName.toString()
			venueImageFile.transferTo( new File( venueDir, tmp))
			venueImagePath = grailsApplication.config.venueimage.savePath+tmp
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
			if(apiVersion.value.toInteger() == Integer.parseInt(json.apiVersion.toString())){
				//retrieve the venueId from the parsed request and retrieve the venue based on venueId
				def venueId = json.venueId
				def venue = Venue.findByVenueId(venueId)
				//check if venue exists with that venueId
				if(venue){
					if(venue.locuSection){
						def sections = venue.locuSection.trim().split(",")
						if(sections){
							def menuJson=[]
							def locuMenu = JSON.parse(URLDecoder.decode(venue.locuMenu))
							sections.each {
								def sectionName = it
								locuMenu.menus.each {
									def  menuName= it
									menuName.each{
										def nameMenu =it
										if(sectionName.trim().equalsIgnoreCase(nameMenu.menu_name.trim()))
										{
											def menu = nameMenu
											menuJson.add(menu)
										}
									}
								}
							}
							//send back the menu as response along with errorCode 0
							response.put("menus",menuJson)
							response.put("errorCode","0")
							response.put("errorMessage","Menu sections available")
						}
					}else{
						//if venue does not exist then send back errorCode 1
						response.put("errorCode","2")
						response.put("errorMessage","No locu sections available for this venue")
					}
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
			response.put("currentTime",new Date().toGMTString())
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
	 * This is sys call used to get locu menu list based on the 
	 * 
	 */
	def getLocuMenu = {
		def response=[:]
		try{
			def json = JSON.parse(request)
			if(json){

				def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
				if(json.has("apiVersion") && apiVersion.value.toInteger() == json.apiVersion.toInteger()){
					venueService.getLocuMenu(json,response)
				}else{
					response.put("errorCode", 2)
					response.put("errorMessage", "API version do not match")
				}
			}else{
				response.put("errorCode", 1)
				response.put("errorMessage", "Post data is missing. Please check for same")
			}
		}catch(Exception e){
			log.info("Exception found in getLocuMenuHeaders sys call "+e.getMessage())
			println"Exception found in getLocuMenuHeaders sys call "+e.getMessage()
			println "e "+e.printStackTrace()
			response.put("errorCode", 200)
			response.put("errorMessage",e.getMessage())
		}finally{
			render(text:response as JSON,contentType:"application/json")
		}

	}
	/**
	 * 
	 * This is sys call used to get locu Menu Headers	 
	 **/
	def getLocuMenuHeaders={
		def response=[:]
		try{
			def json = JSON.parse(request)
			if(json){
				def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
				if(json.has("apiVersion") && apiVersion.value.toInteger() == Integer.parseInt(json.apiVersion.toString())){
					venueService.getLocuMenuHeaders(json,response)
				}else{
					response.put("errorCode", 2)
					response.put("errorMessage", "API version do not match")
				}
			}else{
				response.put("errorCode", 1)
				response.put("errorMessage", "Post data is missing. Please check for same")
			}
		}catch(Exception e){
			log.info("Exception found in getLocuMenuHeaders sys call "+e.getMessage())
			response.put("errorCode", 200)
			response.put("errorMessage",e.getMessage())
			println"e "+e.printStackTrace()

		}finally{
			render(text:response as JSON,contentType:"application/json")
		}


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
						if(checkedInUsers){
							//loop through the checkedin users list and check for private users
							checkedInUsers.each{
								def checkedInUser = it
								if(checkedInUser.userProfile.getShowProfile().equals("OFF")){
									privateUsers = privateUsers+1
								}
							}
						}
						//check if the user placed an order in that venue
						def orders
						if(userProfile)
							orders = Orders.findAllByUser(userProfile)
						//defining a map to store the venue object details
						def venueMap = [:]
						//if order placed mark the venue as unlocked else locked
						if(orders){
							venueMap.put("unlocked","true")
						}
						else{
							venueMap.put("unlocked","false")
						}
						venueMap.put("checkedInUsers",checkedInUsers?.size())
						venueMap.put("privateUsers",privateUsers)
						venueMap.put("venueName",venue.getVenueName())
						venueMap.put("venueId",venue.getVenueId())
						venueMap.put("venueImagePath",venue.venueImagePath)
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
						venueMap.put("totalTaxRate",venue.totalTaxRate)
						venueMap.put("currentTime",new Date().toGMTString())
						venueMap.put("wifiNetworkType",venue.wifiNetworkType)
						venueMap.put("tableOrdering",venue.tableOrdering ?: Boolean.FALSE)
						if(venue.tables)
							venueMap.put("tables",venue.tables)
						venueMap.put("pickupLocation",venue.pickupLocation ?: "")
						//add the venue object to the list defined earlier
						totalVenueList.add(venueMap)
					}
					response.put("errorCode","0")
					response.put("errorMessage","Venues available")
					response.put("venues",totalVenueList)
					//send the list as a response to the syscall
					render(text:response as JSON,contentType:"application/json")
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
			println"Exception is ===> "+e.getMessage()
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
					response.put("currentTime",new Date().toGMTString())
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
					def usersCheckedOut = []
					def ordersCancelled = []
					if(json.status.toString().equals("CLOSED")){
						def userList = CheckedInUsers.findAllByVenueAndStatus(venue,1)
						if(userList){
							userList.each{
								def user = it
								log.warn("Check out the user as the Venue is closed")
								user.setStatus(0)
								if(!user.save(flush:true)){
									println "User checkout save error"
								}else{
									usersCheckedOut.add(user.userProfile.bartsyId)
								}
							}
							if(usersCheckedOut.size()){
								def pnMessage = [:]
								//pnMessage.put("ordersCancelled",ordersCancelled)
								pnMessage.put("usersCheckedOut",usersCheckedOut)
								pnMessage.put("messageType","userTimeout")
								pnMessage.put("currentTime",new Date().toGMTString())
								androidPNService.sendPN(pnMessage,venue.deviceToken)
							}
						}

						def openOrdersCriteria = Orders.createCriteria()
						def openOrders = openOrdersCriteria.list {
							eq("venue",venue)
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
						if(openOrders){
							openOrders.each{
								def order = it
								def orderStatus = order.orderStatus.toString()
								order.setLastState(orderStatus)
								order.setErrorReason("Venue Closed")
								order.setOrderStatus("10")
								if(!order.save(flush:true)){
									println "order cancel error"
								}else{
									def pnMessage = [:]
									pnMessage.put("orderStatus","10")
									pnMessage.put("cancelledOrder",order.orderId)
									pnMessage.put("messageType","orderTimeout")
									pnMessage.put("currentTime",new Date().toGMTString())
									ordersCancelled.add(order.orderId)

									if(order.user.deviceType == 0){
										androidPNService.sendPN(pnMessage,order.user.deviceToken)
									}
									else{
										CommonMethods common = new CommonMethods()
										pnMessage.put("unReadNotifications",common.getNotifictionCount(order.user))
										applePNService.sendPNOrderTimeout(pnMessage, order.user.deviceToken, "1","Your Order "+order.orderId+" has been cancelled as the Venue is closed")
									}
									if(order.getDrinkOffered()){
										if(order.receiverProfile.deviceType == 0){
											androidPNService.sendPN(pnMessage,order.receiverProfile.deviceToken)
										}
										else{
											applePNService.sendPNOrderTimeout(pnMessage, order.receiverProfile.deviceToken, "1","Your Order "+order.orderId+" has been cancelled as the Venue is closed")
										}
									}
								}
							}
							if(ordersCancelled.size()){
								def pnMessage = [:]
								pnMessage.put("ordersCancelled",ordersCancelled)
								pnMessage.put("messageType","orderTimeout")
								pnMessage.put("currentTime",new Date().toGMTString())
								androidPNService.sendPN(pnMessage,venue.deviceToken)
							}
						}
					}


					//send the errorCode 0 as acknowledgement of the status save
					response.put("errorCode","0")
					response.put("errorMessage","Save Successful")
					response.put("currentTime",new Date().toGMTString())
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
	 *  To get the user checkedIn venues list info and orders info
	 */
	def getUserVenues={
		//defining a map to return as a response for this syscall
		def response=[:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
				def bartsyId = json.bartsyId
				if(bartsyId){
					// get userprofile based on the bartsyId
					def userProfile = UserProfile.findByBartsyId(bartsyId)
					if(userProfile){
						println"userprofile exists"
						getUserdetails(userProfile,response)
						println"after response"
					}
					else{
						//if UserProfile does not exists send errorCode 1
						response.put("errorCode","1")
						response.put("errorMessage","UserProfile does not exists")
					}

				}
				else{
					//if bartsyId is empty or null send errorCode 1
					response.put("errorCode","1")
					response.put("errorMessage","Bartsy Id should not be empty or null")
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}catch(Exception e){
			log.info("Exception found !!! "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		finally{
			render(text:response as JSON ,  contentType:"application/json")
		}

	}

	def getUserdetails(UserProfile user,output){
		def venueDetails=[]
		try{
			// get all venues of user checkedIn previously
			def userCheckedInVenues = UserCheckInDetails.findAllByUserProfile(user)
			println"userCheckedInVenues  "+userCheckedInVenues.size()
			// checking user venues
			if(userCheckedInVenues){
				CommonMethods commonMethods = new CommonMethods()
				println "before each !!! "
				userCheckedInVenues.each{
					def checkedInuser = it
					def venue = checkedInuser.venue
					def details=[:]
					println"In each !!! "+venue.venueName
					details.put("venueName",venue.venueName?venue.venueName:"")
					details.put("venueId",venue.venueId?venue.venueId:"")
					details.put("venueImagePath",venue.venueImagePath?venue.venueImagePath:"")
					details.put("currentTime",new Date().toGMTString())
					commonMethods.getUserOrderAndChekedInDetails(venue,user,details)
					venueDetails.add(details)
				}
				output.put("errorCode",0)
				output.put("userVenues", venueDetails)

			}else{
				output.put("errorCode",0)
				output.put("errorMessage","User checkedIn venues are not found")
			}

		}catch(Exception e){
			println"Exception in get user details method in venue controller "+e.getMessage()
		}

		//return venueDetails
	}

	/**
	 * To get locu data
	 * 
	 */
	def locuData = {
		def response=[:]
		try{

			def json = JSON.parse(request)
			def url = message(code:'app.locu.url')+json.locuId+'/?api_key='+message(code:'app.locu.apikey')
			def parsedData = JSON.parse( new URL(url).text )
			response.put("locuData",parsedData)

		}catch(Exception e){
			println "exception found in locu data"
			log.info("exception found in locu data")
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())

		}
		finally{
			render(text:response as JSON ,  contentType:"application/json")
		}
	}

	/**
	 *  This sys call used to login the venue based on the venueLogin and venuePassword
	 */
	def venueLogin={
		def response=[:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toString().equalsIgnoreCase(json.apiVersion.toString())){

				def venueLoginId = json.venueLogin
				def venuePassword = json.venuePassword
				if(venueLoginId && venuePassword){
					// get venue based on the venueId
					def venue = Venue.findByVenueLoginAndVenuePassword(venueLoginId,venuePassword)
					if(venue){
						response.put("errorCode","0")
						response.put("errorMessage","Venue exists")
						response.put("venueId",venue.venueId)
						response.put("venueName",venue.venueName)
					}
					else{
						//if UserProfile does not exists send errorCode 1
						response.put("errorCode","2")
						response.put("errorMessage","Venue does not exists")
					}

				}else{
					response.put("errorCode","1")
					response.put("errorMessage","Venue login id or venue password is missing in your request")
				}
			}else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}catch(Exception e){

			println "exception found in venueLogin"
			log.info("exception found in venueLogin")
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())

		}finally{
			render(text:response as JSON ,  contentType:"application/json")
		}
	}

	/**
	 *  This method used to send the message from user to venue or venue to user
	 */
	def sendVenueUserMessage = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toString().equalsIgnoreCase(json.apiVersion.toString())){


				if(json.has("venueId") && json.has("bartsyId") && json.has("isFromVenue")){
					def venue=Venue.findByVenueId(json.venueId)
					if(venue){
						def user = UserProfile.findByBartsyId(json.bartsyId)
						if(user){
							def venueUserMessage = new VenueUserMessages()
							venueUserMessage.setVenue(venue)
							venueUserMessage.setUser(user)
							venueUserMessage.setIsFromVenue(json.isFromVenue)
							venueUserMessage.setMessage(json.message)
							venueUserMessage.setStatus(0)

							//save the message
							if(venueUserMessage.save(flush:true)){
								def pnMessage = [:]
								pnMessage.put("message",json.message)
								pnMessage.put("bartsyId",user.bartsyId)
								pnMessage.put("senderNickName",user.nickName)
								pnMessage.put("senderImage",user.userImage)
								pnMessage.put("messageType","message")
								pnMessage.put("venueId",venue.venueId)
								pnMessage.put("currentTime",new Date().toGMTString())

								if(json.isFromVenue.equalsIgnoreCase("Yes")){
									def body = "Message Form "+venue.venueName
									pnMessage.put("body",body)
									//send PN to receiver device
									if(user.deviceType == 1){
										CommonMethods common = new CommonMethods()
										pnMessage.put("unReadNotifications",common.getNotifictionCount(user))
										applePNService.sendPN(pnMessage, user.deviceToken, "1" ,body)
									}
									else{
										androidPNService.sendPN(pnMessage, user.deviceToken)
									}

								}else{
									def body = "Message Form "+user.nickName
									pnMessage.put("body",body)
									androidPNService.sendPN(pnMessage, venue.deviceToken)
								}

								response.put("errorCode","0")
								response.put("errorMessage","Message sent")
							}
							else{
								//if message saving fails
								response.put("errorCode","1")
								response.put("errorMessage","Message could not be sent")
							}


						}else{
							response.put("errorCode","3")
							response.put("errorMessage","User does not exists")
						}

					}else{
						response.put("errorCode","2")
						response.put("errorMessage","Venue does not exists")
					}

				}else{
					response.put("errorCode","1")
					response.put("errorMessage","venueId or bartsyId or isFromVenue field is missing in your request")
				}
			}
			else{
				//if apiVersion do not match send errorCode 100
				response.put("errorCode","100")
				response.put("errorMessage","API version do not match")
			}
			response.put("currentTime",new Date().toGMTString())
		}
		catch(Exception e){
			//if an exception occurs send errorCode 200 along with the exception message
			log.info("Exception is ===> "+e.getMessage())
			response.put("errorCode",200)
			response.put("errorMessage",e.getMessage())
		}
		render(text:response as JSON,contentType:"application/json")
	}

	def getVenueUserMessages = {
		//defining a map to return as a response for this syscall
		def response = [:]
		try{
			//parse the request sent as input to the syscall
			def json = JSON.parse(request)
			//check to make sure the apiVersion sent in the request matches the correct apiVersion
			def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
			if(apiVersion.value.toString().equalsIgnoreCase(json.apiVersion.toString())){
				//retrieve the sender profiles based on senderId and receiverId sent in the request to syscall
				def user = UserProfile.findByBartsyId(json.bartsyId.toString())
				//retrieve the venue based on venueId sent in the request to syscall
				def venue = Venue.findByVenueId(json.venueId)
				//check if user profiles and venue both exists
				if(user && venue){
					//if user profiles and venue both exists retrieve the messages

					def criteriaParams = [:]
					int index,noOfResults
					if(json.has("index")){
						index =  json.index
						params.offset = index
					}
					if(json.has("noOfResults")){
						noOfResults =  json.noOfResults
						params.max = noOfResults
					}
					criteriaParams.putAll(params)


					/*def query = {
					 eq("venue",venue)
					 eq("sender",senderProfile)
					 eq("receiver",receiverProfile)
					 }
					 def queryRec = {
					 eq("venue",venue)
					 eq("sender",receiverProfile)
					 eq("receiver",senderProfile)
					 }*/
					def messages = VenueUserMessages.createCriteria().list(criteriaParams){

						//eq("venue",venue)
						eq("user",user)
						eq("venue",venue)
						order("dateCreated","asc")

					}

					def compList = []

					if(messages)
						compList.addAll(messages.toList())

					if(compList){
						def messagesList = []
						response.put("errorCode",0)
						response.put("errorMessage","Messages sent")
						compList.each{
							def message = it
							def messageMap = [:]
							messageMap.put("id",message.id)
							messageMap.put("message",message.message)
							messageMap.put("bartsyId",message.user.bartsyId)
							messageMap.put("date",message.dateCreated.toGMTString())
							messageMap.put("venueId",message.venue.venueId)
							messageMap.put("currentTime",new Date().toGMTString())
							messagesList.add(messageMap)

							if(json.bartsyId.trim().equalsIgnoreCase(message.user.bartsyId.trim()))
							{
								message.setStatus(1)
								message.save(flush:true)
							}
						}
						response.put("messages",messagesList)
					}
					else{
						//Add errorcode 1 to response if messages do not exist
						response.put("errorCode","1")
						response.put("errorMessage","No Messages to be displayed")
					}
				}
				else{
					//Add errorcode 1 to response if users or venue does not exist
					response.put("errorCode","1")
					response.put("errorMessage","Sender, Receiver or Venue does not exists")
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


}

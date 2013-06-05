package bartsy

import java.nio.charset.MalformedInputException
import grails.converters.JSON
import org.json.simple.parser.JSONParser

class VenueController {

	/**
	 * This is a test webservice to save venue details from a hardcoded locu response. To be removed later. 
	 */
	def saveVenueDetailsTest = {
		def json = JSON.parse(request)
		def resources = grailsApplication.mainContext.getResource("response.txt").file
		def fileContents = resources.text
		def parsedData = JSON.parse(fileContents)
		def hasBarSection
		def menu
		Map response = new HashMap()
		Venue venue = Venue.findByLocuId(json.locuId)
		if(venue){
			venue.cancelOrderTime = json.cancelOrderTime as int
			venue.deviceToken = json.deviceToken
			venue.lastHBResponse =  new Date()
			venue.save()
			response.put("venueId",venue.getVenueId())
			response.put("venueName",venue.getVenueName())
			response.put("errorCode","1")
			response.put("errorMessage","Venue already exists")
			render(text:response as JSON ,  contentType:"application/json")
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
			venue.menu = menu
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
				startTimer()
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage","Save not Successful")
			}
			render(text:response as JSON ,  contentType:"application/json")
		}
	}

	/**
	 * This is the webservice to save the venue registration details received from the bartender application.
	 * 
	 * @author Swetha Bhatnagar
	 *
	 * @errorCodes 1 : failure, 0 : success
	 *
	 * @param locuId         		locu.com registration id of the venue
	 * @param deviceToken    		GCM registration id for Android and device id for IOS
	 * @param wifiPresent    		flag to represent if wifi is present=1 at the venue
	 * @param wifiName       		wifi name at the venue
	 * @param wifiPassword   		wifi password at the venue
	 * @param typeOfAuthentication  authentication tyoe for wifi is password or passphrase
	 * @param deviceType		    deviceType is Android=0 or IOS=1
	 * @param paypalId   			paypal id of the venue
	 *
	 * @return  {
	 * @return      errorCode 		: success/failure code 
	 * @return      errorMessage 	: success/failure message
	 * @return      venueId 		: venue id generated for that venue
	 * @return      venueName 		: name of the venue
	 * @return  }
	 *
	 **/
	def saveVenueDetails = {
		def json = JSON.parse(request)
		def url = message(code:'app.locu.url')+json.locuId+'/?api_key='+message(code:'app.locu.apikey')
		def parsedData = JSON.parse( new URL(url).text )
		def hasBarSection
		def menu
		Map response = new HashMap()
		Venue venue = Venue.findByLocuId(json.locuId)
		if(venue){
			venue.deviceToken = json.deviceToken
			venue.cancelOrderTime = json.cancelOrderTime as int
			venue.save()
			response.put("venueId",venue.getVenueId())
			response.put("venueName",venue.getVenueName())
			response.put("errorCode","1")
			response.put("errorMessage",message(code:'venue.exists'))
			render(text:response as JSON ,  contentType:"application/json")
		}
		else{
			parsedData.objects.menus.each {
				def parsedData1 = it
				parsedData1.each{
					def parsedData2 = it
					if(message(code:'bar').equals(parsedData2.menu_name.toString()))
					{
						hasBarSection = 1
						menu = URLEncoder.encode(parsedData2.sections.toString(),"UTF-8")
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
			venue.menu = menu
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
				response.put("errorMessage",message(code:'venue.save'))
				startTimer()
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage",message(code:'venue.save.failed'))
			}
			render(text:response as JSON ,  contentType:"application/json")
		}
	}

	/**
	 * This is the webservice to return the locu menu of a venue
	 *
	 * @author Swetha Bhatnagar
	 *
	 * @errorCodes 1 : failure, 0 : success
	 *
	 * @param venueId         		:server generated id for the venue
	 * 
	 * @return  {
	 * @return      errorCode 		: success/failure code
	 * @return      errorMessage 	: success/failure message
	 * @return      menu	 		: json format of the bar section of the locu menu of the venue
	 * @return  }
	 *
	 **/
	def getMenu = {
		def json = JSON.parse(request)
		def venueId = json.venueId
		Map response =  new HashMap()
		try{
			def venue = Venue.findByVenueId(venueId)
			if(venue){
				def menuJson = JSON.parse(URLDecoder.decode(venue.menu))
				response.put("menu",menuJson)
				response.put("errorCode","0")
				response.put("errorMessage",message(code:'venue.exists'))
			}
			else{
				response.put("errorCode","1")
				response.put("errorMessage",message(code:'venue.not.exists'))
			}
			render(text:response as JSON,contentType:"application/json")
		}catch (Exception e) {
			println e.getMessage()
		}
	}

	/**
	 * This is the webservice to return the list of venues registered.
	 *
	 * @author Swetha Bhatnagar
	 *
	 * @errorCodes 1 : failure, 0 : success
	 *
	 * @return  Returns the venueName,venueId,latitude,longitude,address of all the venues registered     
	 **/
	def getVenueList = {
		def venueList = Venue.getAll()
		def response
		def totalVenueList = []
		if(venueList){
			venueList.each{
				def venue = it
				def checkedInUsers = CheckedInUsers.findAllByVenueAndStatus(venue,1)
				def venueMap = [:]
				venueMap.put("checkedInUsers",checkedInUsers.size())
				venueMap.put("venueName",venue.getVenueName())
				venueMap.put("venueId",venue.getVenueId())
				venueMap.put("latitude",venue.getLat())
				venueMap.put("longitude",venue.getLongtd())
				venueMap.put("venueStatus",venue.getStatus())
				def address = venue.getStreetAddress()+","+venue.getLocality()+","+venue.getCountry()+","+venue.getPostalCode()
				venueMap.put("address",address)
				totalVenueList.add(venueMap)
			}

		}
		render(text:totalVenueList as JSON,contentType:"application/json")
	}
	
	def startTimer(){
		def timer = BartsyConfiguration.findByConfigName("timer")
		boolean flag = timer.value.toBoolean()
		if(!flag){ //value should be false initially
			timer.setValue("true")
			timer.save(flush:true)
			
		}
	}
	
	def heartBeatVenue = {
		def json = JSON.parse(request)
		def venue = Venue.findByVenueId(json.venueId)
		def response = [:]
		if(venue){			
		venue.setLastHBResponse(new Date())
		if(venue.status.equals("OFFLINE"))
		{
			venue.status =  "OPEN"
		}
		venue.save(flush:true)
		response.put("errorCode","0")
		response.put("errorMessage","Request Received")
		render(text:response as JSON ,  contentType:"application/json")
		}
	}
	
	def setVenueStatus = {
		def json =  JSON.parse(request)
		def venue = Venue.findByVenueId(json.venueId)
		def response = [:]
		if(venue){
		venue.setStatus(json.status)
		venue.save(flush:true)
		response.put("errorCode","0")
		response.put("errorMessage","Save Successful")
		render(text:response as JSON ,  contentType:"application/json")
		}
	}
}

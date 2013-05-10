package bartsy

import java.nio.charset.MalformedInputException;

import grails.converters.JSON
import org.json.simple.parser.JSONParser




class VenueController {

	def index() {
	}
	
	def saveDrinks(String barMenu){
		def barMenuParsed = JSON.parse(barMenu)
		barMenuParsed.each{
			def parsedData3 = it
			//println "it---->"+it
			parsedData3.subsections.each{
				//println "it>>>>>"+it
				def parsedData4 = it
				//println "contents---->"+parsedData4.contents
				parsedData4.contents.each{
					def parsedData5 =  it
				println "price"+parsedData5.price	
				}
				//println "subsection_name"+parsedData4.contents.id
				//println "size"+parsedData4.contents.id.size()
//							for(int i=0;i<parsedData4.contents.id.size();i++)
//							{
//								println "id---->"+parsedData4.contents.id[i]
//							}
			}
		}
		
	}

	def getVenueDetailsTest = {
		def resources = grailsApplication.mainContext.getResource("response.txt").file
		def fileContents = resources.text
		def parsedData = JSON.parse(fileContents)
		render(text:parsedData as JSON ,  contentType:"application/json")
	}

	def getVenueDetails = {
		def data = JSON.parse( new URL('http://api.locu.com/v1_0/venue/49be3dcf7e507f6b4775/?api_key=4c009dd9b25397d2c45dfde5a5d8435dfffc58a9').text )
		render(text:data as JSON ,  contentType:"application/json")
	}



	def saveVenueDetailsTest = {
		println "saveVenueDetailsTest"
		def json = JSON.parse(request)
		def resources = grailsApplication.mainContext.getResource("response.txt").file
		def fileContents = resources.text
		def parsedData = JSON.parse(fileContents)
		def hasBarSection
		def menu
		Map response = new HashMap()
		Venue venue = Venue.findByLocuId(json.locuId)
		if(venue){
			response.put("venueId",venue.getVenueId())
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
					println menu
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
		venue.accountNumber = json.accountNumber
		venue.bankName = json.bankName
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
		response.put("errorCode","0")
		response.put("errorMessage","Save Successful")
		}
		else{
			response.put("errorCode","1")
			response.put("errorMessage","Save not Successful")
		}
		render(text:response as JSON ,  contentType:"application/json")
		}
	}

	def saveVenueDetails = {
		def json = JSON.parse(request)
		def url = message(code:'app.locu.url')+json.locuId+'/?api_key='+message(code:'app.locu.apikey')
		//println url
		def parsedData = JSON.parse( new URL(url).text )
		println "parsedData==========>"+parsedData
		def hasBarSection
		def menu
		Map response = new HashMap()
		Venue venue = Venue.findByLocuId(json.locuId)
		if(venue){
			venue.setDeviceToken(json.deviceToken)
			venue.save()
			response.put("venueId",venue.getVenueId())
			response.put("errorCode","2")
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
					println "before casting=====>"+parsedData2.sections
					menu = URLEncoder.encode(parsedData2.sections.toString(),"UTF-8")
					println "After Casting=========>"+menu
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
		venue.accountNumber = json.accountNumber
		venue.bankName = json.bankName
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
		response.put("errorCode","0")
		response.put("errorMessage","Save Successful")
		}
		else{
			response.put("errorCode","1")
			response.put("errorMessage","Save not Successful")
		}
		render(text:response as JSON ,  contentType:"application/json")
		}
	}

	//    def sampleJSON = {
	//        def file = grailsApplication.mainContext.getResource("venueResponse.txt").file
	//        def fileContents = file.text
	//        def result = JSON.parse(fileContents)
	//        render(text:result.objects[0] as JSON ,  contentType:"application/json")
	//    }

	//    def getMenuTest = {
	////        def resources = grailsApplication.mainContext.getResource("response.txt").file
	////        def fileContents = resources.text
	//		URL url = new URL("http://api.locu.com/v1_0/venue/49be3dcf7e507f6b4775/");
	//		println "url : "+url
	//		log.info("url : "+url)
	//		 HttpURLConnection httpConn= (HttpURLConnection) url.openConnection();
	//				  httpConn.setRequestMethod('POST');
	//		 httpConn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
	//		 httpConn.setDoOutput(true)
	//		  def os = httpConn.getOutputStream();
	//		 log.info("os  :"+os)
	//		 BufferedWriter osw = new BufferedWriter(new OutputStreamWriter(os));
	//		 osw.write("?api_key=4c009dd9b25397d2c45dfde5a5d8435dfffc58a9");
	//		 osw.flush();
	//		 osw.close();
	//
	//		 BufferedInputStream instream = new BufferedInputStream(httpConn.getInputStream());
	//
	//		 int x = 0;
	//
	//		 StringBuffer sb = new StringBuffer();
	//
	//		 while ((x = instream.read()) != -1) {
	//			 sb.append((char) x);
	//		 }
	//
	//		 instream.close();
	//		 instream = null;
	//
	//
	//		 if (httpConn != null) {
	//			 httpConn.disconnect();
	//		}
	//		if (httpConn.getResponseCode()  == 200) {
	//
	//        def parsedData = JSON.parse(sb.toString())
	//        parsedData.objects.menus.each {
	//            // println "iterator "+it
	//            def parsedData1 = it
	//            parsedData1.each{
	//                def parsedData2 = it
	//                if("Bar".equals(parsedData2.menu_name.toString()))
	//                {
	//                    render (text:parsedData2.sections as JSON  , contentType:"application/json")
	//                }
	//            }
	//        }
	//
	//    }
	//
	//    }
	//
	//	def getMenu = {
	//		def resources = grailsApplication.mainContext.getResource("response.txt").file
	//		def fileContents = resources.text
	//		def parsedData = JSON.parse(fileContents)
	//		parsedData.objects.menus.each {
	//			// println "iterator "+it
	//			def parsedData1 = it
	//			parsedData1.each{
	//				def parsedData2 = it
	//				if("Bar".equals(parsedData2.menu_name.toString()))
	//				{
	//					render (text:parsedData2.sections as JSON  , contentType:"application/json")
	//				}
	//			}
	//	}
	//
	//}

	//	def getMenuSwetha = {
	//		log.info("inside GetMenuSwetha");
	//		def data = JSON.parse( new URL('http://api.locu.com/v1_0/venue/49be3dcf7e507f6b4775/?api_key=4c009dd9b25397d2c45dfde5a5d8435dfffc58a9').text )
	//		println "Data--------->>>>"+data
	//		log.info("inside GetMenuSwetha1");
	//		log.info("Data Received------>"+data)
	//	}

	def getMenu = {
		def json = JSON.parse(request)
		def venueId = json.venueId
		Map response =  new HashMap()
		//println venueId
		try{
			//println "venue Id---->>>"+venueId
			def venue = Venue.findByVenueId(venueId)
			//println "venueObj------->>>"+venueObj
			if(venue){			
			def menuJson = JSON.parse(URLDecoder.decode(venue.menu))
			//println "data from table:"+venue.menu
			println "menuJson:"+menuJson
			response.put("menu",menuJson)
			response.put("errorCode","0")
			response.put("errorMessage","Venue exists")
			}
			
			else{
				response.put("errorCode","1")
				response.put("errorMessage","Venue does not exists")
			}
			
			render(text:response as JSON,contentType:"application/json")
		}catch (Exception e) {
			println e.getMessage()
		}
	}
	
	def getVenueList = {
		def venueList = Venue.getAll()
		def response
		def totalVenueList = []
		if(venueList){
			venueList.each{
				def venue = it
				def venueMap = [:]
				venueMap.put("venueName",venue.getVenueName())
				venueMap.put("venueId",venue.getVenueId())
				venueMap.put("latitude",venue.getLat())
				venueMap.put("longitude",venue.getLongtd())
				def address = venue.getStreetAddress()+","+venue.getLocality()+","+venue.getCountry()+","+venue.getPostalCode()
				venueMap.put("address",address)
				totalVenueList.add(venueMap)
			}
			render(text:totalVenueList as JSON,contentType:"application/json")
		}
	}
}

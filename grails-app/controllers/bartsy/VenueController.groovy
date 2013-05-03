package bartsy

import grails.converters.JSON
import org.json.simple.parser.JSONParser




class VenueController {

    def index() { }
         
    def getVenueDetails = {        
        def data = JSON.parse( new URL( 'http://localhost:8080/Bartsy/venue/sampleJSON' ).text )
        render(text:data as JSON ,  contentType:"application/json")
    } 
       
    def saveVenueDetails = {
        def data = JSON.parse( new URL( 'http://localhost:8080/Bartsy/venue/sampleJSON' ).text )
        Venue venue = new Venue()
        venue.venueName = data.name
        venue.lat = data.lat
        venue.longtd = data.long
        venue.phone = data.phone
        venue.region = data.region
        venue.locuId = data.id
        venue.postalCode = data.postal_code
        venue.locality = data.locality
        venue.streetAddress = data.street_address
        venue.websiteURL = data.website_url
        venue.country = data.country
        venue.hasLocuMenu = data.has_menu
        venue.save()
    }
    
    def sampleJSON = {
        def file = grailsApplication.mainContext.getResource("venueResponse.txt").file
        def fileContents = file.text
        def result = JSON.parse(fileContents)
        render(text:result.objects[0] as JSON ,  contentType:"application/json")
    }

    def getMenuTest = {
//        def resources = grailsApplication.mainContext.getResource("response.txt").file
//        def fileContents = resources.text
		URL url = new URL("http://api.locu.com/v1_0/venue/49be3dcf7e507f6b4775/");
		println "url : "+url
		log.info("url : "+url)
		 HttpURLConnection httpConn= (HttpURLConnection) url.openConnection();
				  httpConn.setRequestMethod('POST');
		 httpConn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
		 httpConn.setDoOutput(true)
		  def os = httpConn.getOutputStream();
		 log.info("os  :"+os)
		 BufferedWriter osw = new BufferedWriter(new OutputStreamWriter(os));
		 osw.write("?api_key=4c009dd9b25397d2c45dfde5a5d8435dfffc58a9");
		 osw.flush();
		 osw.close();
		 
		 BufferedInputStream instream = new BufferedInputStream(httpConn.getInputStream());

		 int x = 0;

		 StringBuffer sb = new StringBuffer();

		 while ((x = instream.read()) != -1) {
			 sb.append((char) x);
		 }

		 instream.close();
		 instream = null;


		 if (httpConn != null) {
			 httpConn.disconnect();
		}
		if (httpConn.getResponseCode()  == 200) {

        def parsedData = JSON.parse(sb.toString())  
        parsedData.objects.menus.each { 
            // println "iterator "+it
            def parsedData1 = it
            parsedData1.each{
                def parsedData2 = it
                if("Bar".equals(parsedData2.menu_name.toString()))
                {
                    render (text:parsedData2.sections as JSON  , contentType:"application/json")
                }  
            }        
        }
                        
    }
    
    }   
	
	def getMenu = {
		def resources = grailsApplication.mainContext.getResource("response.txt").file
		def fileContents = resources.text
		def parsedData = JSON.parse(fileContents)
		parsedData.objects.menus.each {
			// println "iterator "+it
			def parsedData1 = it
			parsedData1.each{
				def parsedData2 = it
				if("Bar".equals(parsedData2.menu_name.toString()))
				{
					render (text:parsedData2.sections as JSON  , contentType:"application/json")
				}
			}
	} 
    
}
	
	def getMenuSwetha = {
		log.info("inside GetMenuSwetha");
		def data = JSON.parse( new URL('http://api.locu.com/v1_0/venue/49be3dcf7e507f6b4775/?api_key=4c009dd9b25397d2c45dfde5a5d8435dfffc58a9').text )
		println "Data--------->>>>"+data
		log.info("inside GetMenuSwetha1");
		log.info("Data Received------>"+data)		
	}
}
    
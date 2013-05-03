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
    
        
    
}
    
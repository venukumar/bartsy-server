package bartsy

class Venue {

    static constraints = {
		//menu(size:0..65535)
    }
	static mapping = {
		menu type:"text"
	}
    String venueName
    String country
    String hasLocuMenu
    String locuId
    String lat
    String locality
    String longtd
    String phone
    String postalCode
    String region
    String streetAddress
    String websiteURL
	int hasBarSection
	String facebookURL
	String openHours
	String twitterId
	String venueId
	String menu
	String wifiName
	String wifiPassword
	String typeOfAuthentication
	String deviceToken
	String deviceType 
	String paypalId
	int wifiPresent
	Date dateCreated
	Date lastUpdated
	int cancelOrderTime 
	String status
	Date lastHBResponse
	Date lastActivity
}

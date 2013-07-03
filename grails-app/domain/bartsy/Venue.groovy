package bartsy

class Venue {

	static constraints = {
		menu(nullable:true)
		venueImagePath(nullable:true)
	}
	static mapping = { menu type:"text" }
	String venueName
	String country
	String hasLocuMenu
	String locuId
	String locuSection
	String totalTaxRate
	String routingNumber
	String accountNumber
	String lat
	String locality
	String longtd
	String phone
	String postalCode
	String region
	String address
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
	int wifiPresent
	Date dateCreated
	Date lastUpdated
	int cancelOrderTime
	String status
	Date lastHBResponse
	String vendsyRepName
	String vendsyRepEmail
	String vendsyRepPhone
	String managerName
	String venueLogin
	String venuePassword
	Date lastActivity
	String venueImagePath
}

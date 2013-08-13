package bartsy

class Venue {

	static constraints = {
		menu(nullable:true)
		venueImagePath(nullable:true)
		locuMenu(nullable:true)
		phoneNumber(nullable:true)
		description(nullable:true)
		communityRating(nullable:true)
		pickupLocation(nullable:true)
		tables(nullable:true)
		tableOrdering(nullable:true)
	}
	static mapping = {
		menu type:"text"
		locuMenu type:"text"
	}
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
	String locuMenu
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
	String phoneNumber
	String description
	String communityRating
	String wifiNetworkType
	String pickupLocation
	String tables
	String tableOrdering
}

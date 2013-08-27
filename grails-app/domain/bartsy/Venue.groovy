package bartsy

class Venue {

	static constraints = {
		menu(nullable:true)
		venueImagePath(nullable:true)
		locuMenu(nullable:true)
		phoneNumber(nullable:true)
		description(nullable:true)
		communityRating(nullable:true)

		pickupLocations(nullable:true)
		deliveryTables(nullable:true)
		tableOrdering(nullable:true)
		isPickupLocution(nullable:true)

		managerEmail(nullable:true)
		managerPassword(nullable:true)
		managerCell(nullable:true)
		locuUsername(nullable:true)
		locuPassword(nullable:true)
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
	String locuUsername
	String locuPassword
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
	String managerEmail
	String managerPassword
	String managerCell
	String venueLogin
	String venuePassword
	Date lastActivity
	String venueImagePath
	String phoneNumber
	String description
	String communityRating
	String wifiNetworkType
	String isPickupLocution
	String pickupLocations
	String deliveryTables
	String tableOrdering
}

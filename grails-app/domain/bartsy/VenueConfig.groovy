package bartsy

class VenueConfig {

	static constraints = {
		description(nullable: true)
		value(nullable: true)
	}
	static belongsTo = [venue : Venue]
	
	int rewardPoints
	String description
	String type
	Date dateCreated
	String value
}

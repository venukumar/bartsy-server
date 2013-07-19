package bartsy

class VenueConfig {

	static constraints = {
	}
	static belongsTo = [venue : Venue]
	
	int rewardPoints
	String description
	String type
	Date dateCreated
	String value
}

package bartsy

import java.util.Date;

class VenueUserMessages {

	static mapping = {
		message type:'text'
		status(nullable:true)
		isFromVenue()
	}

	static belongsTo = [user:UserProfile,venue:Venue]

	String message
	Date dateCreated
	int status
	String isFromVenue
}

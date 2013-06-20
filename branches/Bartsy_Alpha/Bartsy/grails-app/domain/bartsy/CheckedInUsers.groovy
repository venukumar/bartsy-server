package bartsy

class CheckedInUsers {

    static constraints = {
		lastHBResponse(nullable:true)
    }
    
    static belongsTo = [userProfile : UserProfile,venue : Venue]
	
	int status
	Date dateCreated
	Date lastUpdated
	Date lastHBResponse
}

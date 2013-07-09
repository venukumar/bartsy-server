package bartsy

class CheckedInUsers {

    static constraints = {
		lastHBResponse(nullable:true)
		userSessionCode(nullable:true)
    }
    
    static belongsTo = [userProfile : UserProfile,venue : Venue]
	
	String userSessionCode
	int status
	Date dateCreated
	Date lastUpdated
	Date lastHBResponse
}

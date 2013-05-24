package bartsy

class CheckedInUsers {

    static constraints = {
    }
    
    static belongsTo = [userProfile : UserProfile,venue : Venue]
	
	int status
	Date dateCreated
	Date lastUpdated
}
